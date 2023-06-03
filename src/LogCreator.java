import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.awtui.CommitGraphPane;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Chris
 */
public class LogCreator {

    private final List<RevCommit> commits = new ArrayList<>();
    private Repository repo;
    private Boolean isCommitSelected;
    private JTextArea diffContentArea;
    private JScrollPane scrollPane;
    private JFrame frame;

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

    public void getFrame(JFrame frame) {
        this.frame = frame;
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
        isCommitSelected = false;
        commitGraphPane = new CommitGraphPane();
        RevWalk walk = createWalk(path);
        if (walk != null) {
            commitGraphPane.getCommitList().source(walk);
            commitGraphPane.getCommitList().fillTo(Integer.MAX_VALUE);
            commitGraphPane.setRowSelectionAllowed(true);

            commitGraphPane.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent event) {
                    if (!event.getValueIsAdjusting()) {
                        // 번호 출력
                        int selectedRow = commitGraphPane.getSelectedRow();
                        // 번호 사용
                        RevCommit commit = (RevCommit)commitGraphPane.getValueAt(selectedRow,0);

                        if(!isCommitSelected) {
                            diffContentArea = new JTextArea();
                            scrollPane = new JScrollPane(diffContentArea);
                            diffContentArea.setPreferredSize(new Dimension(frame.getWidth(), 200));

                            getDiffs(commit);
                            frame.add(scrollPane, BorderLayout.SOUTH);
                            frame.setSize(new Dimension(frame.getWidth(), frame.getHeight()+200));
                            isCommitSelected = true;
                        } else {
                            diffContentArea.setText("");
                            getDiffs(commit);
                        }
                        System.out.println("출력");
                    }
                }

                public void getDiffs(RevCommit commit) {
                    List<String> changed = CustomJgitUtilities.listDiff(new Git(repo), repo, commit);
                    for(String str : changed) {
                        diffContentArea.append(str);
                    }
                }
            });
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