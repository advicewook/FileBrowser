import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class CustomSwingUtilities {
    public JPanel showCommitMenu(String path, JPanel commitPanel, int height) {

        try (Repository repository = CookbookHelper.openJGitCookbookRepository()) {
            try (Git git = new Git(repository)) {
                Status status = git.status().call();
                System.out.println("Added: " + status.getAdded());
                System.out.println("Changed: " + status.getChanged());
                System.out.println("IgnoredNotInIndex: " + status.getIgnoredNotInIndex());
                System.out.println("Modified: " + status.getModified());
                System.out.println("Removed: " + status.getRemoved());
                System.out.println("Untracked: " + status.getUntracked());
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 커밋 패널 부모 컨테이너
        commitPanel = new JPanel();
        commitPanel.setOpaque(false);
        commitPanel.setSize(new Dimension(300, height));
        commitPanel.setLayout(new BorderLayout());

        // 커밋 패널 - 상단 컨테이너
        JPanel topCommitPanel = new JPanel();
        topCommitPanel.setOpaque(false);
        topCommitPanel.setLayout(new BorderLayout());
        topCommitPanel.setSize(new Dimension(300, 250));
        // 커밋 패널 - 상단 헤더
        JPanel topHeader = new JPanel();
        topHeader.setOpaque(false);
        topHeader.setLayout(new FlowLayout(0));
        topHeader.setSize(new Dimension(300, 35));
        // 커밋 패널 - 중단 텍스트
        JLabel unstagedLabel = new JLabel();
        unstagedLabel.setText("Unstaged");
        unstagedLabel.setPreferredSize(new Dimension(60, 30));
        unstagedLabel.setSize(unstagedLabel.getPreferredSize());
        unstagedLabel.setForeground(Color.black);
        topHeader.add(unstagedLabel);
        // 커밋 패널 - 상단 버튼
        JButton stageButton = new JButton("Stage");
        stageButton.setOpaque(false);
        stageButton.setBackground(new Color(0, 0, 0, 0));
        stageButton.setForeground(Color.black);
        stageButton.setFocusable(false);
        topHeader.add(stageButton);
        // 커밋 패널 - 상단 리스트
        JPanel unstagedPanel = new JPanel();
        JScrollPane unstagedScrollPanel = new JScrollPane(unstagedPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        unstagedScrollPanel.setOpaque(false);
        unstagedScrollPanel.getViewport().add(unstagedPanel);
        unstagedScrollPanel.getViewport().setOpaque(false);
        unstagedScrollPanel.getViewport().validate();
        unstagedScrollPanel.setPreferredSize(new Dimension(300, 215));
        // 커밋 패넝 - 상단 병합
        topCommitPanel.add(topHeader, BorderLayout.NORTH);
        topCommitPanel.add(unstagedScrollPanel, BorderLayout.CENTER);

        // 커밋 패널 - 중단 컨테이너
        JPanel middleCommitPanel = new JPanel();
        middleCommitPanel.setOpaque(false);
        middleCommitPanel.setLayout(new BorderLayout());
        middleCommitPanel.setSize(new Dimension(300, 250));
        // 커밋 패널 - 중단 헤더
        JPanel middleHeader = new JPanel();
        middleHeader.setOpaque(false);
        middleHeader.setLayout(new FlowLayout(0));
        middleHeader.setSize(new Dimension(300, 35));
        // 커밋 패널 - 중단 텍스트
        JLabel stagedLabel = new JLabel();
        stagedLabel.setText("Staged");
        stagedLabel.setPreferredSize(new Dimension(60, 30));
        stagedLabel.setSize(stagedLabel.getPreferredSize());
        stagedLabel.setForeground(Color.black);
        middleHeader.add(stagedLabel);
        // 커밋 패널 - 중단 버튼
        JButton unStageButton = new JButton("Unstage");
        unStageButton.setOpaque(false);
        unStageButton.setBackground(new Color(0, 0, 0, 0));
        unStageButton.setForeground(Color.black);
        unStageButton.setFocusable(false);
        middleHeader.add(unStageButton);
        // 커밋 패널 - 중단 리스트
        JPanel stagedPanel = new JPanel();
        JScrollPane stagedScrollPanel = new JScrollPane(stagedPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        stagedScrollPanel.setOpaque(false);
        stagedScrollPanel.getViewport().add(stagedPanel);
        stagedScrollPanel.getViewport().setOpaque(false);
        stagedScrollPanel.getViewport().validate();
        stagedScrollPanel.setSize(new Dimension(300, 215));
        // 커밋 패넝 - 중단 병합
        middleCommitPanel.add(middleHeader, BorderLayout.NORTH);
        middleCommitPanel.add(stagedScrollPanel, BorderLayout.CENTER);

        // 커밋 패널 - 하단 컨테이너
        JPanel bottomCommitPanel = new JPanel();
        bottomCommitPanel.setOpaque(false);
        bottomCommitPanel.setLayout(new BorderLayout());
        bottomCommitPanel.setSize(new Dimension(300, 200));
        // 커밋 패널 - 하단 헤더
        JPanel bottomHeader = new JPanel();
        bottomHeader.setOpaque(false);
        bottomHeader.setLayout(new FlowLayout(0));
        bottomHeader.setSize(new Dimension(300, 35));
        // 커밋 패널 - 하단 텍스트
        JLabel commitLabel = new JLabel();
        commitLabel.setText("Commit Message");
        commitLabel.setPreferredSize(new Dimension(100, 30));
        commitLabel.setSize(commitLabel.getPreferredSize());
        commitLabel.setForeground(Color.black);
        bottomHeader.add(commitLabel);
        // 커밋 패널 - 하단 버튼
        JButton commitButton = new JButton("Commit");
        commitButton.setOpaque(false);
        commitButton.setBackground(new Color(0, 0, 0, 0));
        commitButton.setForeground(Color.black);
        commitButton.setFocusable(false);
        bottomHeader.add(commitButton);
        // 커밋 패널 - 하단 텍스트 필드
//        JTextField textField = new JTextField();
//        textField.setVisible(true);
//        textField.setSize(300,120);
        // 커밋 패널 - 하단 텍스트 영역
        JTextArea textArea = new JTextArea(3,1);
        textArea.setVisible(true);
        textArea.setSize(300,120);

        JScrollPane scrollTextArea = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTextArea.setOpaque(false);
        scrollTextArea.getViewport().add(textArea);
        scrollTextArea.getViewport().setOpaque(false);
        scrollTextArea.getViewport().validate();
        scrollTextArea.setSize(new Dimension(300, 120));
        // 커밋 패넝 - 하단 병합
        bottomCommitPanel.add(bottomHeader, BorderLayout.NORTH);
        bottomCommitPanel.add(scrollTextArea, BorderLayout.CENTER);
        // 커밋 패널 부모 컨테이너에 추가
        commitPanel.add(topCommitPanel, BorderLayout.NORTH);
        commitPanel.add(middleCommitPanel, BorderLayout.CENTER);
        commitPanel.add(bottomCommitPanel, BorderLayout.SOUTH);

        return commitPanel;
    }
}
