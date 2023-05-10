import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

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

    public static Repository createNewRepository(String path) throws IOException {
        Repository repository = FileRepositoryBuilder.create(new File(path, ".git"));
        repository.create();

        return repository;
    }

    public static boolean isUntracked(String path, String fileName) throws IOException, GitAPIException {
        try (Repository repository = new FileRepositoryBuilder().setGitDir(new File(path + "/.git")).build()) {
            try (Git git = new Git(repository)) {
                // get the status of all files in the repository
                Status status = git.status().call();

                // check if the fileName is untracked
                return status.getUntracked().contains(fileName);
            }
        }
    }

    public static boolean isModified(String path, String fileName) throws IOException, GitAPIException {
        try (Repository repository = new FileRepositoryBuilder().setGitDir(new File(path + "/.git")).build()) {
            try (Git git = new Git(repository)) {
                // get the status of all files in the repository
                Status status = git.status().call();

                // check if the fileName is modified
                return status.getModified().contains(fileName);
            }
        }
    }

    public static boolean isStaged(String path, String fileName) throws IOException, GitAPIException {
        try (Repository repository = new FileRepositoryBuilder().setGitDir(new File(path + "/.git")).build()) {
            try (Git git = Git.open(new File(path))) {
                // get the status of all files in the repository
                Status status = git.status().call();

                // check if the fileName is staged
                return status.getAdded().contains(fileName);
            }
        }
    }


    public static boolean isCommitted(String path, String fileName) throws IOException, GitAPIException {
        try (Repository repository = new FileRepositoryBuilder().setGitDir(new File(path + "/.git")).build()) {
            try (Git git = new Git(repository)) {
                // get the status of all files in the repository
                Status status = git.status().call();

                // check if the fileName is committed or unmodified
                return !status.getUncommittedChanges().contains(fileName) && !status.getUntracked().contains(fileName);
            }
        }
    }

    //git add
    public static void addFile(String path, String fileName) {
        try (Git git = Git.open(new File(path))) {
            git.add().addFilepattern(fileName).call();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
//    public static void addFile(String path, String fileName) throws IOException, GitAPIException {
//
//        FileRepositoryBuilder builder = new FileRepositoryBuilder();
//        try (Repository repository = builder.setGitDir(new File(path + "/.git"))
//                .readEnvironment() // scan environment GIT_* variables
//                .findGitDir() // scan up the file system tree
//                .build()) {
//            try (Git git = new Git(repository)) {
//                git.add().addFilepattern(fileName).call();
//                System.out.println("Added file " + fileName + " to repository.");
//            }
//        }
//
//    }

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
        try (Git git = Git.open(new File(path))) {
            git.checkout().addPath(fileName).call();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
//    public static void restoreModifiedFile1(String path, String fileName) throws IOException, GitAPIException {
//        FileRepositoryBuilder builder = new FileRepositoryBuilder();
//        try (Repository repository = builder.setGitDir(new File(path + "/.git"))
//                .readEnvironment() // scan environment GIT_* variables
//                .findGitDir() // scan up the file system tree
//                .build()) {
//            try (Git git = new Git(repository)) {
//                CheckoutCommand checkout = git.checkout();
//                checkout.setAllPaths(true);
//                checkout.setStartPoint("HEAD");
//                checkout.addPath(fileName);
//                checkout.call();
//                System.out.println("Unmodified file " + fileName);
//            }
//        }
//    }




    // git restore --cached
    public static void restoreStagedFile(String path, String fileName) {
        try (Git git = Git.open(new File(path))) {
            git.reset().addPath(fileName).call();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
//    public static void restoreStagedFile1(String path, String fileName) throws IOException, GitAPIException {
//        FileRepositoryBuilder builder = new FileRepositoryBuilder();
//        try (Repository repository = builder.setGitDir(new File(path + "/.git"))
//                .readEnvironment() // scan environment GIT_* variables
//                .findGitDir() // scan up the file system tree
//                .build()) {
//            try (Git git = new Git(repository)) {
//                ResetCommand resetCommand = git.reset();
//                resetCommand.setRef("HEAD");
//                resetCommand.addPath(fileName);
//                resetCommand.call();
//                System.out.println("Unstaged changes for file " + fileName);
//            }
//        }
//    }

    //git rm --cached
    public static void removeCachedFile(String path, String fileName) {
        try (Git git = Git.open(new File(path))) {
            git.rm().setCached(true).addFilepattern(fileName).call();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
//    public static void removeCachedFile1(String path, String fileName) throws IOException, GitAPIException {
//        FileRepositoryBuilder builder = new FileRepositoryBuilder();
//        try (Repository repository = builder.setGitDir(new File(path + "/.git"))
//                .readEnvironment() // scan environment GIT_* variables
//                .findGitDir() // scan up the file system tree
//                .build()) {
//            try (Git git = new Git(repository)) {
//                RmCommand rm = git.rm();
//                rm.setCached(true);
//                rm.addFilepattern(fileName);
//                rm.call();
//                System.out.println("Removed cached file " + fileName);
//            }
//        }
//    }


    //git rm
    public static void removeFile(String path, String fileName) {
        try (Git git = Git.open(new File(path))) {
            git.rm().addFilepattern(fileName).call();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
//    public static void removeFile1(String path, String fileName) throws IOException, GitAPIException {
//        FileRepositoryBuilder builder = new FileRepositoryBuilder();
//        try (Repository repository = builder.setGitDir(new File(path + "/.git"))
//                .readEnvironment() // scan environment GIT_* variables
//                .findGitDir() // scan up the file system tree
//                .build()) {
//            try (Git git = new Git(repository)) {
//                RmCommand rm = git.rm();
//                rm.addFilepattern(fileName);
//                rm.call();
//                System.out.println("Removed file " + fileName);
//            }
//        }
//    }


    //git mv (renaming)
    public static void mvFile(String path, String oldFileName, String newFileName) {
        try (Git git = Git.open(new File(path))) {
            Files.move(Paths.get(path + "/" + oldFileName), Paths.get(path + "/" + newFileName));
            git.add().addFilepattern(newFileName).call();
            git.rm().addFilepattern(oldFileName).call();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
//    public static void mvFile1(String path, String fileName, String newName) throws IOException, GitAPIException {
//        try (Repository repository = Git.open(new File(path, ".git")).getRepository()) {
//            Git git = new Git(repository);
//            String oldFilePath = path + File.separator + fileName;
//            String newFilePath = path + File.separator + newName;
//
//            // Add the file to the index with the old path
//            git.add().addFilepattern(oldFilePath).call();
//
//            // Use Files.move to rename the file
//            Files.move(Paths.get(oldFilePath), Paths.get(newFilePath));
//
//            // Add the new file to the index with the new path
//            git.add().addFilepattern(newFilePath).call();
//
//            git.commit().setMessage("Renamed file " + oldFilePath + " to " + newFilePath).call();
//        } catch (JGitInternalException e) {
//            throw new JGitInternalException("Error occurred while renaming file.", e);
//        }
//    }



    }
