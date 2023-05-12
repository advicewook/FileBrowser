
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;


public class CustomSwingUtilities {

    // 커밋 패널 - 상단 리스트 컨테이너
    JPanel unstagedPanel = new JPanel();
    // 커밋 패널 - 중단 리스트 컨테이너
    JPanel stagedPanel = new JPanel();
    private static CustomSwingUtilities instance;

    private FileBrowser fileBrowser;
    private CustomSwingUtilities(FileBrowser fileBrowser){
        this.fileBrowser=fileBrowser;
    }

    public static synchronized CustomSwingUtilities getInstance(FileBrowser fileBrowser){
        if(instance==null){
            instance=new CustomSwingUtilities(fileBrowser);
        }
        return instance;
    }


    public Status getStatus(String path){
        try {
            Status status;
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository tempGitRepository;
            tempGitRepository = builder.readEnvironment().findGitDir(new File(path)).build();
            String replacedString = tempGitRepository.toString().
                    replace("\\.git","").
                    replace("Repository[","").
                    replace("]","");
            Git git = Git.open(new File(replacedString));
            status = git.status().call();
            return status;
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public JPanel showCommitMenu(String path, int height)  {
        
        removeCheckbox(unstagedPanel);
        removeCheckbox(stagedPanel);

        Status status = getStatus(path);
        Set<String> missingSet = status.getMissing();
        Set<String> untrackedSet = status.getUntracked();
        Set<String> modifiedSet = status.getModified();
        Set<String> addedSet = status.getAdded();
        Set<String> changedSet = status.getChanged();
        Set<String> removedSet = status.getRemoved();

        // 커밋 패널 부모 컨테이너
        JPanel commitPanel = new JPanel();
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
        // 커밋 패널 - 상단 텍스트
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
        // 커밋 패널 - 상단 리스트 컨테이너
        unstagedPanel.setLayout(new BoxLayout(unstagedPanel, BoxLayout.Y_AXIS));
        unstagedPanel.setBackground(Color.white);
        // 커밋 패널 - 상단 리스트 추가
        getJCheckBoxList(missingSet, unstagedPanel, "removed");
        getJCheckBoxList(untrackedSet, unstagedPanel, "untracked");
        getJCheckBoxList(modifiedSet, unstagedPanel, "modified");
        // 커밋 패널 - 상단 리스트 컨테이너를 스크롤 컨테이너에 삽입
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

        // 커밋 패널 - 중단 리스트 컨테이너
        stagedPanel.setLayout(new BoxLayout(stagedPanel, BoxLayout.Y_AXIS));
        stagedPanel.setBackground(Color.white);
        // 커밋 패널 - 중단 리스트 추가
        getJCheckBoxList(addedSet, stagedPanel, "added");
        getJCheckBoxList(changedSet, stagedPanel, "changed");
        getJCheckBoxList(removedSet, stagedPanel, "removed");


        // 커밋 패널 - 중단 리스트 컨테이너를 스크롤 컨테이너에 삽입
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
        JTextArea textArea = new JTextArea(3, 1);
        textArea.setVisible(true);
        textArea.setSize(300, 120);

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

        //체크박스에 선택된 아이템들을 staging
        stageButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Set<String> selectedItems1 = new HashSet<>();
                addSelectedItems(unstagedPanel, selectedItems1);
                try {
                    Iterator<String> iter1 = selectedItems1.iterator();
                    while(iter1.hasNext()){
                        CustomJgitUtilities.addFile(path,iter1.next());
                    }
                } catch (Exception ex) {
                    // Handle the exception
                    ex.printStackTrace();
                }
                //1. remove all checkboxes
                //2. update Status set
                //3. get checkboxes

                //1
                removeCheckbox(unstagedPanel);
                removeCheckbox(stagedPanel);

                //2
                Status temp = getStatus(path);

                Set<String> tempmissingSet = temp.getMissing();
                Set<String> tempuntrackedSet = temp.getUntracked();
                Set<String> tempmodifiedSet = temp.getModified();
                Set<String> tempaddedSet = temp.getAdded();
                Set<String> tempchangedSet = temp.getChanged();
                Set<String> tempremovedSet = temp.getRemoved();

                //3
                getJCheckBoxList(tempmissingSet, unstagedPanel, "removed");
                getJCheckBoxList(tempuntrackedSet, unstagedPanel, "untracked");
                getJCheckBoxList(tempmodifiedSet, unstagedPanel, "modified");
                getJCheckBoxList(tempaddedSet, stagedPanel, "added");
                getJCheckBoxList(tempchangedSet, stagedPanel, "changed");
                getJCheckBoxList(tempremovedSet, stagedPanel, "removed");

                unstagedPanel.revalidate();
                stagedPanel.revalidate();
                fileBrowser.updateShowPanel();

            }
        });
        //체크박스에 선택된 아이템들을 unstaging
        unStageButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Set<String> selectedItems2 = new HashSet<>();
                addSelectedItems(stagedPanel, selectedItems2);
                try {
                    Iterator<String> iter1 = selectedItems2.iterator();
                    while(iter1.hasNext()){
                        CustomJgitUtilities.restoreStagedFile(path,iter1.next());
                    }
                } catch (Exception e) {
                    // Handle the exception
                    e.printStackTrace();
                }

                //1. remove all checkboxes
                //2. update Status set
                //3. get checkboxes

                //1
                removeCheckbox(unstagedPanel);
                removeCheckbox(stagedPanel);

                //2
                Status temp = getStatus(path);

                Set<String> tempmissingSet = temp.getMissing();
                Set<String> tempuntrackedSet = temp.getUntracked();
                Set<String> tempmodifiedSet = temp.getModified();
                Set<String> tempaddedSet = temp.getAdded();
                Set<String> tempchangedSet = temp.getChanged();
                Set<String> tempremovedSet = temp.getRemoved();

                //3
                getJCheckBoxList(tempmissingSet, unstagedPanel, "removed");
                getJCheckBoxList(tempuntrackedSet, unstagedPanel, "untracked");
                getJCheckBoxList(tempmodifiedSet, unstagedPanel, "modified");
                getJCheckBoxList(tempaddedSet, stagedPanel, "added");
                getJCheckBoxList(tempchangedSet, stagedPanel, "changed");
                getJCheckBoxList(tempremovedSet, stagedPanel, "removed");

                unstagedPanel.revalidate();
                stagedPanel.revalidate();
                fileBrowser.updateShowPanel();
            }
        });


        commitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CustomJgitUtilities.commitFile(path, textArea.getText());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                textArea.setText("");
                //1
                removeCheckbox(unstagedPanel);
                removeCheckbox(stagedPanel);

                //2
                Status temp = getStatus(path);

                Set<String> tempuntrackedSet = temp.getUntracked();
                Set<String> tempmodifiedSet = temp.getModified();
                Set<String> tempaddedSet = temp.getAdded();
                Set<String> tempchangedSet = temp.getChanged();
                Set<String> tempremovedSet = temp.getRemoved();

                //3
                getJCheckBoxList(tempuntrackedSet, unstagedPanel, "untracked");
                getJCheckBoxList(tempmodifiedSet, unstagedPanel, "modified");
                getJCheckBoxList(tempaddedSet, stagedPanel, "added");
                getJCheckBoxList(tempchangedSet, stagedPanel, "changed");
                getJCheckBoxList(tempremovedSet, stagedPanel, "removed");

                unstagedPanel.revalidate();
                stagedPanel.revalidate();
                fileBrowser.updateShowPanel();
            }
        });

        // 커밋 패널 부모 컨테이너에 추가
        commitPanel.add(topCommitPanel, BorderLayout.NORTH);
        commitPanel.add(middleCommitPanel, BorderLayout.CENTER);
        commitPanel.add(bottomCommitPanel, BorderLayout.SOUTH);

        return commitPanel;
    }

    public void getJCheckBoxList(Set<String> statusSet, JPanel container, String status) {
        Iterator<String> statusIterator = statusSet.iterator();
        while (statusIterator.hasNext()) {
            JPanel layout = new JPanel();
            layout.setBackground(Color.white);
            layout.setLayout(new BoxLayout(layout, BoxLayout.X_AXIS));
            JCheckBox statusFile = new JCheckBox(statusIterator.next());
            statusFile.setBackground(Color.white);
            layout.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel label=new JLabel();
            Image img;
            ImageIcon imageIcon;
            // status에 따른 icon 생성
            switch (status) {
                case "missing":
                    img = new ImageIcon("src\\" + "/img/removed.png").getImage();
                    img = img.getScaledInstance(13, 13, Image.SCALE_SMOOTH);
                    imageIcon = new ImageIcon(img);
                    label = new JLabel(imageIcon);
                    label.setPreferredSize(new Dimension(13, 13));
                    break;
                case "untracked":
                    img = new ImageIcon("src\\" + "/img/untracked.png").getImage();
                    img = img.getScaledInstance(13, 13, Image.SCALE_SMOOTH);
                    imageIcon = new ImageIcon(img);
                    label = new JLabel(imageIcon);
                    label.setPreferredSize(new Dimension(13, 13));
                    break;
                case "modified":
                    img = new ImageIcon("src\\" + "/img/modified.png").getImage();
                    img = img.getScaledInstance(13, 13, Image.SCALE_SMOOTH);
                    imageIcon = new ImageIcon(img);
                    label = new JLabel(imageIcon);
                    label.setPreferredSize(new Dimension(13, 13));
                    break;
                case "added":
                    img = new ImageIcon("src\\" + "/img/added.png").getImage();
                    img = img.getScaledInstance(13, 13, Image.SCALE_SMOOTH);
                    imageIcon = new ImageIcon(img);
                    label = new JLabel(imageIcon);
                    label.setPreferredSize(new Dimension(13, 13));
                    break;
                case "changed":
                    img = new ImageIcon("src\\" + "/img/changed.png").getImage();
                    img = img.getScaledInstance(13, 13, Image.SCALE_SMOOTH);
                    imageIcon = new ImageIcon(img);
                    label = new JLabel(imageIcon);
                    label.setPreferredSize(new Dimension(13, 13));
                    break;
                case "removed":
                    img = new ImageIcon("src\\" + "/img/removed.png").getImage();
                    img = img.getScaledInstance(13, 13, Image.SCALE_SMOOTH);
                    imageIcon = new ImageIcon(img);
                    label = new JLabel(imageIcon);
                    label.setPreferredSize(new Dimension(13, 13));
                    break;
                default: break;
            }
            label.setBorder(BorderFactory.createEmptyBorder(0 , 5, 0 , 0));
            layout.add(label);
            layout.add(statusFile);

            container.add(layout);
        }
    }
    public void removeCheckbox(Container container) {
        // get all the components in the panel
        for (Component c : container.getComponents()){
            if (c instanceof JCheckBox)
                container.remove(c);
            if (c instanceof JLabel)
                container.remove(c);
            else if(c instanceof Container)
                removeCheckbox((Container) c);
        }

    }
    public void addSelectedItems(Container container, Set<String> selectedItems) {
        for (Component c : container.getComponents()) {
            if (c instanceof JCheckBox) {
                JCheckBox checkbox = (JCheckBox) c;
                if (checkbox.isSelected()) {
                    selectedItems.add(checkbox.getText());
                    System.out.println(checkbox.getText());
                }
            } else if (c instanceof Container) {
                addSelectedItems((Container) c, selectedItems);
            }
        }
    }

    public void revalidateCommitMenu(String path){

        //1
        removeCheckbox(unstagedPanel);
        removeCheckbox(stagedPanel);

        //2
        Status temp = getStatus(path);

        Set<String> tempmissingSet = temp.getMissing();
        Set<String> tempuntrackedSet = temp.getUntracked();
        Set<String> tempmodifiedSet = temp.getModified();
        Set<String> tempaddedSet = temp.getAdded();
        Set<String> tempchangedSet = temp.getChanged();
        Set<String> tempremovedSet = temp.getRemoved();

        //3
        getJCheckBoxList(tempmissingSet, unstagedPanel, "removed");
        getJCheckBoxList(tempuntrackedSet, unstagedPanel, "untracked");
        getJCheckBoxList(tempmodifiedSet, unstagedPanel, "modified");
        getJCheckBoxList(tempaddedSet, stagedPanel, "added");
        getJCheckBoxList(tempchangedSet, stagedPanel, "changed");
        getJCheckBoxList(tempremovedSet, stagedPanel, "removed");

        unstagedPanel.revalidate();
        unstagedPanel.repaint();
        stagedPanel.revalidate();
        stagedPanel.repaint();
    }


}
