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
        // create the directory
        Repository repository = FileRepositoryBuilder.create(new File(path, ".git"));
        repository.create();

        return repository;
    }
    //git add
    public static void addFile(String path, String fileName) throws IOException, GitAPIException {

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Repository repository = builder.setGitDir(new File(path + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build()) {
            try (Git git = new Git(repository)) {
                git.add().addFilepattern(fileName).call();
                System.out.println("Added file " + fileName + " to repository.");
            }
        }

    }

    //git restore
    public static void restoreModifiedFile(String path, String fileName) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Repository repository = builder.setGitDir(new File(path + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build()) {
            try (Git git = new Git(repository)) {
                CheckoutCommand checkout = git.checkout();
                checkout.setAllPaths(true);
                checkout.setStartPoint("HEAD");
                checkout.addPath(fileName);
                checkout.call();
                System.out.println("Unmodified file " + fileName);
            }
        }
    }



    // git restore --cached
    public static void restoreStagedFile(String path, String fileName) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Repository repository = builder.setGitDir(new File(path + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build()) {
            try (Git git = new Git(repository)) {
                ResetCommand resetCommand = git.reset();
                resetCommand.setRef("HEAD");
                resetCommand.addPath(fileName);
                resetCommand.call();
                System.out.println("Unstaged changes for file " + fileName);
            }
        }
    }
    //git rm --cached
    public static void removeCachedFile(String path, File file) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Repository repository = builder.setGitDir(new File(path + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build()) {
            try (Git git = new Git(repository)) {
                RmCommand rm = git.rm();
                rm.setCached(true);
                rm.addFilepattern(file.getPath());
                rm.call();
                System.out.println("Removed cached file " + file.getPath());
            }
        }
    }
    //git rm
    public static void removeFile(String path, File file) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Repository repository = builder.setGitDir(new File(path + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build()) {
            try (Git git = new Git(repository)) {
                RmCommand rm = git.rm();
                rm.addFilepattern(file.getPath());
                rm.call();
                System.out.println("Removed file " + file.getPath());
            }
        }
    }

    //git mv (renaming)
    public static void mvFile(String path, File file, String newName) throws IOException, GitAPIException {
        try (Repository repository = Git.open(new File(path, ".git")).getRepository()) {
            Git git = new Git(repository);
            String oldFilePath = file.getPath();
            String newFilePath = path + File.separator + newName;

            // Add the file to the index with the old path
            git.add().addFilepattern(oldFilePath).call();

            // Use Files.move to rename the file
            Files.move(Paths.get(oldFilePath), Paths.get(newFilePath));

            // Add the new file to the index with the new path
            git.add().addFilepattern(newFilePath).call();

            git.commit().setMessage("Renamed file " + oldFilePath + " to " + newFilePath).call();
        } catch (JGitInternalException e) {
            throw new JGitInternalException("Error occurred while renaming file.", e);
        }
    }
    }



