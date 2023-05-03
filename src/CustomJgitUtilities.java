import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RevertCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CustomJgitUtilities {
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

    public static Repository createNewRepository() throws IOException {
        // prepare a new folder
        File localPath = File.createTempFile("TestGitRepository", "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        // create the directory
        Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"));
        repository.create();

        return repository;
    }

    //git add
//    public class AddFile {
//
//        public static void main(String[] args) throws IOException, GitAPIException {
//            final File localPath;
//            // prepare a new test-repository
//            try (Repository repository = CookbookHelper.createNewRepository()) {
//                localPath = repository.getWorkTree();
//
//                try (Git git = new Git(repository)) {
//                    // create the file
//                    File myFile = new File(repository.getDirectory().getParent(), "testfile");
//                    if(!myFile.createNewFile()) {
//                        throw new IOException("Could not create file " + myFile);
//                    }
//
//                    // run the add-call
//                    git.add()
//                            .addFilepattern("testfile")
//                            .call();
//
//                    System.out.println("Added file " + myFile + " to repository at " + repository.getDirectory());
//                }
//            }
//
//            // clean up here to not keep using more and more disk-space for these samples
//            FileUtils.deleteDirectory(localPath);
//        }
//    }
//    //git restore
//    public class RevertCommit {
//
//        public static void main(String[] args) throws IOException, GitAPIException {
//            final File path;
//            try (Repository repository = CookbookHelper.createNewRepository()) {
//                try (Git git = new Git(repository)) {
//                    path = repository.getWorkTree();
//                    System.out.println("Repository at " + path);
//
//                    // Create a new file and add it to the index
//                    File newFile = new File(path, "file1.txt");
//                    FileUtils.writeStringToFile(newFile, "Line 1\r\n", "UTF-8", true);
//                    git.add().addFilepattern("file1.txt").call();
//                    RevCommit rev1 = git.commit().setAuthor("test", "test@test.com").setMessage("Commit Log 1").call();
//                    System.out.println("Rev1: " + rev1);
//
//                    // commit some changes
//                    FileUtils.writeStringToFile(newFile, "Line 2\r\n", "UTF-8", true);
//                    git.add().addFilepattern("file1.txt").call();
//                    RevCommit rev2 = git.commit().setAll(true).setAuthor("test", "test@test.com").setMessage("Commit Log 2").call();
//                    System.out.println("Rev2: " + rev2);
//
//                    // commit some changes
//                    FileUtils.writeStringToFile(newFile, "Line 3\r\n", "UTF-8", true);
//                    git.add().addFilepattern("file1.txt").call();
//                    RevCommit rev3 = git.commit().setAll(true).setAuthor("test", "test@test.com").setMessage("Commit Log 3").call();
//                    System.out.println("Rev3: " + rev3);
//
//                    // print logs
//                    Iterable<RevCommit> gitLog = git.log().call();
//                    for (RevCommit logMessage : gitLog) {
//                        System.out.println("Before revert: " + logMessage.getName() + " - " + logMessage.getFullMessage());
//                    }
//
//                    RevertCommand revertCommand = git.revert();
//                    // revert to revision 2
//                    revertCommand.include(rev3);
//                    RevCommit revCommit = revertCommand.call();
//                    System.out.println("Reverted: " + revCommit);
//                    System.out.println("Reverted refs: " + revertCommand.getRevertedRefs());
//                    System.out.println("Unmerged paths: " + revertCommand.getUnmergedPaths());
//                    System.out.println("Failing results: " + revertCommand.getFailingResult());
//
//                    // print logs
//                    gitLog = git.log().call();
//                    for (RevCommit logMessage : gitLog) {
//                        System.out.println("After revert: " + logMessage.getName() + " - " + logMessage.getFullMessage());
//                    }
//
//                    System.out.println("File contents: " + FileUtils.readFileToString(newFile, "UTF-8"));
//                }
//            }
//
//            // clean up here to not keep using more and more disk-space for these samples
//            FileUtils.deleteDirectory(path);
//        }
//    }
//    //git restore --staged
//    public class RevertChanges {
//
//        public static void main(String[] args) throws IOException, GitAPIException {
//            final File localPath;
//            try (Repository repository = CookbookHelper.createNewRepository()) {
//                localPath = repository.getWorkTree();
//
//                System.out.println("Listing local branches:");
//                try (Git git = new Git(repository)) {
//                    // set up a file
//                    String fileName = "temptFile.txt";
//                    File tempFile = new File(repository.getDirectory().getParentFile(), fileName);
//                    if(!tempFile.createNewFile()) {
//                        throw new IOException("Could not create temporary file " + tempFile);
//                    }
//                    Path tempFilePath = tempFile.toPath();
//
//                    // write some initial text to it
//                    String initialText = "Initial Text";
//                    System.out.println("Writing text [" + initialText + "] to file [" + tempFile.toString() + "]");
//                    Files.write(tempFilePath, initialText.getBytes());
//
//                    // add the file and commit it
//                    git.add().addFilepattern(fileName).call();
//                    git.commit().setMessage("Added untracked file " + fileName + "to repo").call();
//
//                    // modify the file
//                    Files.write(tempFilePath, "Some modifications".getBytes(), StandardOpenOption.APPEND);
//
//                    // assert that file's text does not equal initialText
//                    if (initialText.equals(getTextFromFilePath(tempFilePath))) {
//                        throw new IllegalStateException("Modified file's text should not equal " +
//                                "its original state after modification");
//                    }
//
//                    System.out.println("File now has text [" + getTextFromFilePath(tempFilePath) + "]");
//
//                    // revert the changes
//                    git.checkout().addPath(fileName).call();
//
//                    // text should no longer have modifications
//                    if (!initialText.equals(getTextFromFilePath(tempFilePath))) {
//                        throw new IllegalStateException("Reverted file's text should equal its initial text");
//                    }
//
//                    System.out.println("File modifications were reverted. " +
//                            "File now has text [" + getTextFromFilePath(tempFilePath) + "]");
//                }
//            }
//
//            // clean up here to not keep using more and more disk-space for these samples
//            FileUtils.deleteDirectory(localPath);
//        }
//
//        private static String getTextFromFilePath(Path file) throws IOException {
//            byte[] bytes = Files.readAllBytes(file);
//            CharBuffer chars = Charset.defaultCharset().decode(ByteBuffer.wrap(bytes));
//            return chars.toString();
//        }
//    }
//    //git rm --cached
//    public class GitRemove {
//        public static void removeFile(String filePath) throws IOException, GitAPIException {
//            FileRepositoryBuilder builder = new FileRepositoryBuilder();
//            try (Repository repository = builder.setGitDir(new File(".git"))
//                    .readEnvironment() // scan environment GIT_* variables
//                    .findGitDir() // scan up the file system tree
//                    .build()) {
//                try (Git git = new Git(repository)) {
//                    RmCommand rm = git.rm().setCached(true);
//                    rm.addFilepattern(filePath);
//                    rm.call();
//                    System.out.println("Removed file " + filePath + " from index.");
//                }
//            }
//        }
//    }
//    //git rm
//    public class GitRemove {
//        public static void removeFile(String filePath) throws IOException, GitAPIException {
//            FileRepositoryBuilder builder = new FileRepositoryBuilder();
//            try (Repository repository = builder.setGitDir(new File(".git"))
//                    .readEnvironment() // scan environment GIT_* variables
//                    .findGitDir() // scan up the file system tree
//                    .build()) {
//                try (Git git = new Git(repository)) {
//                    RmCommand rm = git.rm();
//                    rm.addFilepattern(filePath);
//                    rm.call();
//                    System.out.println("Removed file " + filePath + " from repository.");
//                }
//            }
//        }
//    }
//    //git mv
//    public class GitMove {
//        public static void moveFile(String oldPath, String newPath) throws IOException, GitAPIException {
//            FileRepositoryBuilder builder = new FileRepositoryBuilder();
//            try (Repository repository = builder.setGitDir(new File(".git"))
//                    .readEnvironment() // scan environment GIT_* variables
//                    .findGitDir() // scan up the file system tree
//                    .build()) {
//                try (Git git = new Git(repository)) {
//                    git.mv().setForce(true).addFilepattern(oldPath).addFilepattern(newPath).call();
//                    System.out.println("Moved file " + oldPath + " to " + newPath + ".");
//                }
//            }
//        }
//    }
}
