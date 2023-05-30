import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.awtui.CommitGraphPane;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 *
 * @author Chris
 */
public class LogCreator {

    private final List<RevCommit> commits = new ArrayList<>();
    private Repository repo;
    public CommitGraphPane commitGraphPane;

    private void createCommitList(RevWalk walk, RevWalk argWalk) throws IOException {
        commits.removeAll(commits);
        for (Ref a : repo.getRefDatabase().getRefs()) {
            ObjectId oid = a.getPeeledObjectId();

            if (oid == null) {
                oid = a.getObjectId();
            }
            try {
                commits.add(walk.parseCommit(oid));
            } catch (IncorrectObjectTypeException e) {
                // Ignore all refs which are not commits
            }
        }

        if (commits.isEmpty()) {
            final ObjectId head = repo.resolve(Constants.HEAD);
            if (head == null) {
                return;
            }
            commits.add(walk.parseCommit(head));
        }

        for (RevCommit c : commits) {
            final RevCommit real = argWalk == walk ? c : walk.parseCommit(c);

            if (c.has(RevFlag.UNINTERESTING)) {
                walk.markUninteresting(real);
            } else {
                walk.markStart(real);
            }
        }
    }

    public String repoName() {
        final File gitDir = repo.getDirectory();
        if (gitDir == null) {
            return repo.toString();
        }

        String n = gitDir.getName();
        if (Constants.DOT_GIT.equals(n)) {
            n = gitDir.getParentFile().getName();
        }

        return n;
    }

    public CommitGraphPane createCommitGraphPane(String path) throws IOException {
        commitGraphPane = new CommitGraphPane();
        RevWalk walk = createWalk(path);
        if (walk != null) {
            commitGraphPane.getCommitList().source(walk);
            commitGraphPane.getCommitList().fillTo(Integer.MAX_VALUE);
        }
        return commitGraphPane;
    }

    private RevWalk createWalk(String path) {
        RevWalk walk;
        RevWalk argWalk;
        try {
            repo = new FileRepositoryBuilder().setWorkTree(new File(path)).build();
            walk = argWalk = new PlotWalk(repo);
            createCommitList(walk, argWalk);
            return walk;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}