import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomJgitUtilities {
    //기존 레포지토리 체크 메서드 - 해당 폴더만 검사
    public static boolean isGitRepository(String path) {
        try {
            Repository repository = Git.open(new File(path + "/.git")).getRepository();
            if (repository != null) {
                repository.close();
                return true;
            }
        } catch (IOException e) {
            // failed to open repository
        }
        return false;
    }

    //서브레포지토리 체크 메서드 - .git을 포함하는 상위 폴더까지 올라가서 검사
    public static boolean isSubGitRepository(String path) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = null;
        try {
            repository = builder.readEnvironment().findGitDir(new File(path)).build();
        } catch (RuntimeException | IOException ex) {
            repository = null;
        }
        if (repository != null) {
            repository.close();
            return true;
        }
        return false;
    }

    // 레포 경로 반환 메서드
    public static String findRepoPath(File file) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder
                .findGitDir(file.getParentFile())
                .setMustExist(true)
                .build();

        return repo.getDirectory().getParent();
    }

    public static void getStatusForParentFolder(String path) throws IOException, GitAPIException {
        File file = new File(path);
        File parent = file.getParentFile();

        // find the parent folder including .git
        while (parent != null && !new File(parent, ".git").exists()) {
            parent = parent.getParentFile();
        }

        // check if the parent folder is under version control
        if (parent == null) {
            throw new IllegalArgumentException(path + " is not under version control");
        }

        Repository repository = Git.open(parent).getRepository();
        Git git = new Git(repository);
        Status status = git.status().call();

        // loop through all subfolders and obtain status
        for (File subfolder : parent.listFiles()) {
            if (subfolder.isDirectory()) {
                Status subfolderStatus = git.status().addPath(subfolder.getName()).call();
                System.out.println("Status for " + subfolder.getAbsolutePath() + ":");
                System.out.println("Added: " + subfolderStatus.getAdded());
                System.out.println("Changed: " + subfolderStatus.getChanged());
                System.out.println("Conflicting: " + subfolderStatus.getConflicting());
                System.out.println("Missing: " + subfolderStatus.getMissing());
                System.out.println("Modified: " + subfolderStatus.getModified());
            }
        }
    }

    public static Repository createNewRepository(String path) throws IOException {
        Repository repository = FileRepositoryBuilder.create(new File(path, ".git"));
        repository.create();

        return repository;
    }

    public static boolean isUntracked(String path, String fileName) throws IOException, GitAPIException {
        fileName = extractText(fileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = null;
        repository = builder.readEnvironment().findGitDir(new File(path)).build();
        try (Git git = new Git(repository)) {
            Status status = git.status().call();
            Set<String> untrackedSet = new HashSet<>();
            Set<String> untrackedFileSet = status.getUntracked();
            Set<String> untrackedFolderSet = status.getUntrackedFolders();
            untrackedSet.addAll(untrackedFileSet);
            untrackedSet.addAll(untrackedFolderSet);
            Iterator<String> untrackedSetIterator = untrackedSet.iterator();

            String replacedString = repository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            fileName = replacedPath + fileName;

            while (untrackedSetIterator.hasNext()) {
                if (untrackedSetIterator.next().equals(fileName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isModified(String path, String fileName) throws IOException, GitAPIException {
        fileName = extractText(fileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = null;
        repository = builder.readEnvironment().findGitDir(new File(path)).build();
        try (Git git = new Git(repository)) {
            Status status = git.status().call();
            Set<String> modifiedSet = status.getModified();
            Iterator<String> modifiedSetIterator = modifiedSet.iterator();

            String replacedString = repository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            fileName = replacedPath + fileName;

            while (modifiedSetIterator.hasNext()) {
                if (modifiedSetIterator.next().equals(fileName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isMissing(String path, String fileName) throws IOException, GitAPIException {
        fileName = extractText(fileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = null;
        repository = builder.readEnvironment().findGitDir(new File(path)).build();
        try (Git git = new Git(repository)) {
            Status status = git.status().call();
            Set<String> missingSet = status.getMissing();
            Iterator<String> missingSetIterator = missingSet.iterator();

            String replacedString = repository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            fileName = replacedPath + fileName;

            while (missingSetIterator.hasNext()) {
                if (missingSetIterator.next().equals(fileName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isStaged(String path, String fileName) throws IOException, GitAPIException {
        fileName = extractText(fileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = null;
        repository = builder.readEnvironment().findGitDir(new File(path)).build();
        try (Git git = new Git(repository)) {
            Status status = git.status().call();
            Set<String> stagedSet = new HashSet<>();
            Set<String> addedSet = status.getAdded();
            Set<String> changedSet = status.getChanged();
            Set<String> removedSet = status.getRemoved();
            stagedSet.addAll(addedSet);
            stagedSet.addAll(changedSet);
            stagedSet.addAll(removedSet);
            Iterator<String> addedSetIterator = stagedSet.iterator();

            String replacedString = repository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            fileName = replacedPath + fileName;

            while (addedSetIterator.hasNext()) {
                if (addedSetIterator.next().equals(fileName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isCommitted(String path, String fileName) throws IOException, GitAPIException {
        fileName = extractText(fileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = null;
        repository = builder.readEnvironment().findGitDir(new File(path)).build();
        try (Git git = new Git(repository)) {
            Status status = git.status().call();

            Set<String> notInCommittedOrUnmodifiedSet = new HashSet<>();
            notInCommittedOrUnmodifiedSet.addAll(status.getAdded());
            notInCommittedOrUnmodifiedSet.addAll(status.getChanged());
            notInCommittedOrUnmodifiedSet.addAll(status.getConflicting());
            notInCommittedOrUnmodifiedSet.addAll(status.getMissing());
            notInCommittedOrUnmodifiedSet.addAll(status.getModified());
            notInCommittedOrUnmodifiedSet.addAll(status.getRemoved());
            notInCommittedOrUnmodifiedSet.addAll(status.getUntracked());
            notInCommittedOrUnmodifiedSet.addAll(status.getUntrackedFolders());

            Iterator<String> notInCommittedOrUnmodifiedIterator = notInCommittedOrUnmodifiedSet.iterator();

            String replacedString = repository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            fileName = replacedPath + fileName;

            while (notInCommittedOrUnmodifiedIterator.hasNext()) {
                if (notInCommittedOrUnmodifiedIterator.next().equals(fileName)) {
                    return false;
                }
            }
            return true;
        }
    }

    //git add
    public static void addFile(String path, String fileName) {
        fileName = extractText(fileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository tempGitRepository;
        try {
            tempGitRepository = builder.readEnvironment().findGitDir(new File(path)).build();
            String replacedString = tempGitRepository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            String newFileName = replacedPath + fileName;
            Git git = Git.open(new File(replacedString));
            git.add().addFilepattern(newFileName).call();
        } catch (RuntimeException | IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    //git rm
    public static void rmFile(String path, String fileName) {
        fileName = extractText(fileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository tempGitRepository;
        try {
            tempGitRepository = builder.readEnvironment().findGitDir(new File(path)).build();
            String replacedString = tempGitRepository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            String newFileName = replacedPath + fileName;
            Git git = Git.open(new File(replacedString));
            git.rm().addFilepattern(newFileName).call();
        } catch (RuntimeException | IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    public static void commitFile(String path, String commitMsg) {
        try (Git git = Git.open(new File(path))) {
            git.commit()
                    .setMessage(commitMsg)
                    .call();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    //git restore
    public static void restoreModifiedFile(String path, String fileName) {
        fileName = extractText(fileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository tempGitRepository;
        try {
            tempGitRepository = builder.readEnvironment().findGitDir(new File(path)).build();
            String replacedString = tempGitRepository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            String newFileName = replacedPath + fileName;
            Git git = Git.open(new File(replacedString));
            git.checkout().addPath(newFileName).call();
        } catch (RuntimeException | IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    // git restore --cached
    public static void restoreStagedFile(String path, String fileName) {
        fileName = extractText(fileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository tempGitRepository;
        try {
            tempGitRepository = builder.readEnvironment().findGitDir(new File(path)).build();
            String replacedString = tempGitRepository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            String newFileName = replacedPath + fileName;
            Git git = Git.open(new File(replacedString));
            git.reset().addPath(newFileName).call();
        } catch (RuntimeException | IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    //git rm --cached
    public static void removeCachedFile(String path, String fileName) {
        fileName = extractText(fileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository tempGitRepository;
        try {
            tempGitRepository = builder.readEnvironment().findGitDir(new File(path)).build();
            String replacedString = tempGitRepository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            String newFileName = replacedPath + fileName;
            Git git = Git.open(new File(replacedString));
            git.rm().setCached(true).addFilepattern(newFileName).call();
        } catch (RuntimeException | IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    //git rm
    public static void removeFile(String path, String fileName) {
        fileName = extractText(fileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository tempGitRepository;
        try {
            tempGitRepository = builder.readEnvironment().findGitDir(new File(path)).build();
            String replacedString = tempGitRepository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            String newFileName = replacedPath + fileName;
            Git git = Git.open(new File(replacedString));
            git.rm().addFilepattern(newFileName).call();
        } catch (RuntimeException | IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    //git mv (renaming)
    public static void mvFile(String path, String oldFileName, String newFileName) {
        oldFileName = extractText(oldFileName);
        newFileName = extractText(newFileName);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository tempGitRepository;
        try {
            tempGitRepository = builder.readEnvironment().findGitDir(new File(path)).build();
            String replacedString = tempGitRepository.toString().
                    replace("\\.git", "").
                    replace("Repository[", "").
                    replace("]", "");
            String replacedPath = path.replace(replacedString, "").replaceAll("^\\\\", "");
            if (!replacedPath.equals("")) {
                replacedPath += "/";
                replacedPath = replacedPath.replace("\\", "/");
            }
            String replacedNewFileName = replacedPath + newFileName;
            String replacedOldFileName = replacedPath + oldFileName;
            Git git = Git.open(new File(replacedString));

            Files.move(Paths.get(replacedString + "/" + replacedOldFileName), Paths.get(replacedString + "/" + replacedNewFileName));
            git.add().addFilepattern(replacedNewFileName).call();
            git.rm().addFilepattern(replacedOldFileName).call();
        } catch (RuntimeException | IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    public static String extractText(String input) {
        String extractedText = "";
        Pattern pattern = Pattern.compile("<p>(.*?)</p>");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            extractedText = matcher.group(1);
        }

        if (extractedText.equals("")) {
            return input;
        }
        return extractedText;
    }

    /*
    메서드 getBranch(), getName() 등이 브랜치 명을 반환하는 형식
    로컬 브랜치 - refs/heads/브랜치명
    리모트 브랜치 - refs/remotes/리모트명/브랜치명
     */
    // 전체 브랜치 명 반환
    public static List<String> getBranchNameList(String path) throws IOException, GitAPIException {
        List<String> branchList = new ArrayList<String>();

        try (Git git = Git.open(new File(path))) {
            List<Ref> call = git
                            .branchList()
                            .setListMode(ListMode.ALL)
                            .call();
            for (Ref ref : call){
                String temp = ref.getName();
                // 브랜치 명 파싱
                if (temp.contains("refs/heads/")){ // local branch
                    temp = temp.replace("refs/heads/","");
                }
                else if(temp.contains("refs/remotes/")){ // remote branch
                    temp = temp.replace("refs/remotes/","");
                }
                if (!temp.contains("HEAD")){
                    branchList.add(temp);
                }
            }
        }
        return branchList;
    }

    // 로컬 브랜치 <브랜치 명 : 체크썸> 반환
    public static Map<String, String> getLocalBranchList(String path) throws IOException, GitAPIException {
        if (!isGitRepository(path) && isSubGitRepository(path)) {
            String root = findRepoPath(new File(path));
            path = root;
        }

        Map<String, String> branchChecksums = new HashMap<>();
        try (Repository repository = FileRepositoryBuilder.create(new File(path, ".git"))) {
            ObjectId headId = repository.resolve("HEAD");
            List<Ref> branches = Git.wrap(repository).branchList()
                    .call();

            if (headId != null){
                for (Ref branch : branches) {
                    String branchName = branch.getName();
                    if (branchName.contains("refs/heads/")){ // local branch
                        branchName = branchName.replace("refs/heads/","");
                    }
                    ObjectId objectId = branch.getObjectId();
                    String checksum = objectId.getName();
                    branchChecksums.put(branchName, checksum); // 브랜치명 : 체크섬
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return branchChecksums;
    }

    // 리모트 브랜치 <브랜치 명 : 체크썸>  반환
    public static Map<String, String> getRemoteBranchList(String path) throws IOException, GitAPIException {
        if (!isGitRepository(path) && isSubGitRepository(path)) {
            String root = findRepoPath(new File(path));
            path = root;
        }
        Map<String, String> branchChecksums = new HashMap<>();
        try (Repository repository = FileRepositoryBuilder.create(new File(path, ".git"))) {
            ObjectId headId = repository.resolve("HEAD");
            List<Ref> branches = Git.wrap(repository).branchList()
                    .setListMode(ListMode.REMOTE)
                    .call();

            if (headId != null ){
                for (Ref branch : branches) {
                    String branchName = branch.getName();
                    if(branchName.contains("refs/remotes/")){ // remote branch
                        branchName = branchName.replace("refs/remotes/","");
                    }
                    ObjectId objectId = branch.getObjectId();
                    String checksum = objectId.getName();
                    branchChecksums.put(branchName, checksum); // 브랜치명 : 체크섬
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return branchChecksums;
    }

    // 현재 브랜치 이름 반환
    public static String getCurrentBranchName(String path) throws GitAPIException, IOException {
        String currentBranch = "";
        // 깃 레포 안의 하위 폴더 처리
        if(!isGitRepository(path) && isSubGitRepository(path)){
            String root = findRepoPath(new File(path));
            path = root;
        }
        try (Repository repository = Git.open(new File(path)).getRepository()) {
            ObjectId headId = repository.resolve("HEAD");
            if (headId != null){
//                System.out.println(">>> head checksum : "+headId.getName());
//                System.out.println(">>> repository.getBranch() : "+ repository.getBranch());
                Map<String, String> localChecksums = getLocalBranchList(path);
                Map<String, String> remoteChecksums = getRemoteBranchList(path);

                for (Map.Entry<String, String> localEntry : localChecksums.entrySet()) {
                    String localKey = localEntry.getKey(); // 브랜치 이름
                    String localValue = localEntry.getValue(); // checksum
                    //System.out.println("" + localKey + "" + " " + localValue);

                    // 로컬 브랜치에 있으면 그대로 반환
                    if (localKey.equals(repository.getBranch())&&!localKey.contains("HEAD")) {
                        currentBranch = localKey;
                        return currentBranch;
                    }
                }
                // 로컬 브랜치가 아니라면 원격 브랜치 탐색
                for (Map.Entry<String, String> entry : remoteChecksums.entrySet()) {
                    String key = entry.getKey(); // 브랜치 이름
                    String value = entry.getValue(); // checksum
                    //System.out.println("" + key + "" + " " + value);

                    if (value.equals(repository.getBranch())&&!key.contains("HEAD")){
                        currentBranch = key;
                        return currentBranch;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentBranch;
    }

    // 브랜치 생성
    public static void createBranch(String path, String existingBranchName , String newBranchName) throws GitAPIException, IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.readEnvironment().findGitDir(new File(path)).build();
        Git git = new Git(repository);
        git.branchCreate().setName(newBranchName).setStartPoint(existingBranchName).call();
    }

    // 브랜치 삭제
    public static void deleteBranch(String path, String branchName) throws GitAPIException, IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.readEnvironment().findGitDir(new File(path)).build();
        Git git = new Git(repository);
        git.branchDelete().setBranchNames(branchName).call();
    }

    // 브랜치 리네임
    public static void renameBranch(String path, String oldBranchName, String newBranchName) throws GitAPIException, IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.readEnvironment().findGitDir(new File(path)).build();
        Git git = new Git(repository);
        git.branchRename().setOldName(oldBranchName).setNewName(newBranchName).call();
    }

    // 브랜치 체크아웃
    public static void checkoutBranch(String path, String branchName) throws GitAPIException, IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.readEnvironment().findGitDir(new File(path)).build();
        Git git = new Git(repository);
        git.checkout().setName(branchName).call();
    }
}
