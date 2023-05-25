import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
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

    // 전체 브랜치 리스트 반환
    public static List<String> getBranchList(String path) throws IOException, GitAPIException {
        List<String> branchList = new ArrayList<String>();

        try (Git git = Git.open(new File(path))) {
            List<Ref> call = git
                            .branchList()
                            .setListMode(ListMode.ALL)
                            .call();

            for (Ref ref : call){
                String temp = ref.getName();
                // 브랜치 명 파싱
                if (temp.contains("refs/heads/")){
                    temp = temp.replace("refs/heads/","");
                }
                else if(temp.contains("refs/remotes/")){
                    temp = temp.replace("refs/remotes/","");
                }
                branchList.add(temp);
            }
        }
        return branchList;
    }

    public static String getCurrentBranchName(String path) throws GitAPIException, IOException {
        // 깃 레포 안의 하위 폴더 처리
        if(!isGitRepository(path) && isSubGitRepository(path)){
            String root = findRepoPath(new File(path));
            path = root;
        }
        try (Repository repository = Git.open(new File(path)).getRepository()) {
            return repository.getBranch();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
