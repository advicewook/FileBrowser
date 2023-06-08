
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    // 메인 브랜치 패널
    JPanel branchPanel = new JPanel();

    List<String> branchList = null;
    String currentBranch = null;
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
        commitPanel.setSize(new Dimension(250, height));
        commitPanel.setLayout(new BorderLayout());

        // 커밋 패널 - 상단 컨테이너
        JPanel topCommitPanel = new JPanel();
        topCommitPanel.setOpaque(false);
        topCommitPanel.setLayout(new BorderLayout());
        topCommitPanel.setSize(new Dimension(250, 250));
        // 커밋 패널 - 상단 헤더
        JPanel topHeader = new JPanel();
        topHeader.setOpaque(false);
        topHeader.setLayout(new FlowLayout(0));
        topHeader.setSize(new Dimension(250, 35));
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
        unstagedScrollPanel.setPreferredSize(new Dimension(250, 215));
        // 커밋 패넝 - 상단 병합
        topCommitPanel.add(topHeader, BorderLayout.NORTH);
        topCommitPanel.add(unstagedScrollPanel, BorderLayout.CENTER);

        // 커밋 패널 - 중단 컨테이너
        JPanel middleCommitPanel = new JPanel();
        middleCommitPanel.setOpaque(false);
        middleCommitPanel.setLayout(new BorderLayout());
        middleCommitPanel.setSize(new Dimension(250, 250));
        // 커밋 패널 - 중단 헤더
        JPanel middleHeader = new JPanel();
        middleHeader.setOpaque(false);
        middleHeader.setLayout(new FlowLayout(0));
        middleHeader.setSize(new Dimension(250, 35));
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
        stagedScrollPanel.setSize(new Dimension(250, 215));
        // 커밋 패넝 - 중단 병합
        middleCommitPanel.add(middleHeader, BorderLayout.NORTH);
        middleCommitPanel.add(stagedScrollPanel, BorderLayout.CENTER);

        // 커밋 패널 - 하단 컨테이너
        JPanel bottomCommitPanel = new JPanel();
        bottomCommitPanel.setOpaque(false);
        bottomCommitPanel.setLayout(new BorderLayout());
        bottomCommitPanel.setSize(new Dimension(250, 200));
        // 커밋 패널 - 하단 헤더
        JPanel bottomHeader = new JPanel();
        bottomHeader.setOpaque(false);
        bottomHeader.setLayout(new FlowLayout(0));
        bottomHeader.setSize(new Dimension(250, 35));
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
        scrollTextArea.setSize(new Dimension(250, 120));
        // 커밋 패널 - 하단 병합
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
                        String element = iter1.next();
                        if(CustomJgitUtilities.isMissing(path, element)){
                            CustomJgitUtilities.rmFile(path,element);
                        }else{
                            CustomJgitUtilities.addFile(path,element);
                        }
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
                revalidateBranchMenu(path);
            }
        });

        // 커밋 패널 부모 컨테이너에 추가
        commitPanel.add(topCommitPanel, BorderLayout.NORTH);
        commitPanel.add(middleCommitPanel, BorderLayout.CENTER);
        commitPanel.add(bottomCommitPanel, BorderLayout.SOUTH);

        return commitPanel;
    }

    // 브랜치 패널
    public JPanel showBranchMenu(String path, int height) throws GitAPIException, IOException {
        branchPanel.removeAll();
        // 메인 브랜치 패널
        branchPanel.setOpaque(false);
        branchPanel.setLayout(new BorderLayout());
        branchPanel.setSize(new Dimension(250,250));

        // 패널 헤더
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new FlowLayout(0));
        header.setSize(new Dimension(250, 35));

        // 패널 라벨
        JLabel label = new JLabel();
        label.setText("Branches");
        label.setPreferredSize(new Dimension( 60,30));
        label.setSize(label.getPreferredSize());
        label.setForeground(Color.black);
        header.add(label);

        // 머지 버튼
        JButton mergeButton = new JButton("Merge");
        mergeButton.setBackground(Color.white);
        mergeButton.setForeground(Color.black);
        mergeButton.setFocusable(false);
        header.add(mergeButton);
        
        // 브랜치 리스트 출력
        JPanel branchListPanel = new JPanel();
        branchListPanel.setLayout(new BoxLayout(branchListPanel, BoxLayout.Y_AXIS));
        EmptyBorder paddingBorder = new EmptyBorder(0, 5,0, 5); // 여백 생성
        branchListPanel.setBorder(paddingBorder); // 패널에 여백 적용
        branchListPanel.setBackground(Color.white);
        showBranchListOnPanel(path, branchListPanel);

        // 스크롤
        JScrollPane branchScrollPanel = new JScrollPane(branchListPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        branchScrollPanel.setOpaque(false);
        branchScrollPanel.getViewport().add(branchListPanel);
        branchScrollPanel.getViewport().setOpaque(false);
        branchScrollPanel.getViewport().validate();
        branchScrollPanel.setPreferredSize(new Dimension(250,250));

        // 최종 병합
        branchPanel.add(header, BorderLayout.NORTH);
        branchPanel.add(branchScrollPanel, BorderLayout.CENTER);
        branchPanel.updateUI();

        mergeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // 1. 새로운 merge 창 오픈
                JFrame mergeFrame = new JFrame("Merge Window");
                mergeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                mergeFrame.setSize(300, 250);
                mergeFrame.setLayout(new BorderLayout());

                // 2. 전체 브랜치 리스트 업
                try {
                    branchList = CustomJgitUtilities.getBranchNameList(path);
                    currentBranch = CustomJgitUtilities.getCurrentBranchName(path);

                } catch (IOException | GitAPIException ex) {
                    throw new RuntimeException(ex);
                }

                JPanel radioPanel = new JPanel();
                radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
                ButtonGroup buttonGrp = new ButtonGroup(); // branch 중복 선택 방지
                for (String branch : branchList) {
                    if (branch.equals(currentBranch)){
                        // 현재 브랜치는 머지 대상에서 제외
                        continue;
                    }
                    JRadioButton radioButton = new JRadioButton(branch);
                    radioPanel.add(radioButton);
                    buttonGrp.add(radioButton);
                }
                mergeFrame.add(new JScrollPane(radioPanel), BorderLayout.CENTER);

                // 3. 하단에 merge 버튼 추가
                JButton mergeButton = new JButton("merge");
                mergeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String commitMsg = JOptionPane.showInputDialog(branchPanel, " Enter the commit msg : ");
                        String mergedBranch = getSelectedButtonText(buttonGrp);
                        if(commitMsg != null) {
                            CustomJgitUtilities.mergeBranch(path, mergedBranch, commitMsg);
                        }
                        mergeFrame.dispose();
                    }
                    public String getSelectedButtonText(ButtonGroup buttonGroup) {
                        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
                            AbstractButton button = buttons.nextElement();

                            if (button.isSelected()) {
                                return button.getText();
                            }
                        }
                        return null;
                    }
                });
                mergeFrame.add(mergeButton, BorderLayout.SOUTH);
                mergeFrame.setLocationRelativeTo(branchPanel);
                mergeFrame.setVisible(true);
            }
        });

        return branchPanel;
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

    // 브랜치 패널에 리스트업
    public void showBranchListOnPanel(String path, JPanel container) throws GitAPIException, IOException {
        List<String> branchList = CustomJgitUtilities.getBranchNameList(path);
        String currentBranch = CustomJgitUtilities.getCurrentBranchName(path);

        for (String branch : branchList) {
            JLabel label = new JLabel(branch);
            JPopupMenu popupMenu = new JPopupMenu();
            boolean isCurrentBranch = currentBranch.equals(branch);
            JMenuItem menuItem1 = new JMenuItem("Create Branch");
            JMenuItem menuItem2 = new JMenuItem("Delete Branch");
            JMenuItem menuItem3 = new JMenuItem("Rename Branch");
            JMenuItem menuItem4 = new JMenuItem("Checkout Branch");
            if(isCurrentBranch){
                popupMenu.add(menuItem1);
                popupMenu.add(menuItem3);
                popupMenu.add(menuItem4);
                label.setBackground(new Color(0, 0, 0, 0));
                label.setForeground(Color.green);
            }else{
                popupMenu.add(menuItem1);
                popupMenu.add(menuItem2);
                popupMenu.add(menuItem3);
                popupMenu.add(menuItem4);
            }
            container.add(label);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    showPopupMenu(e);
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    showPopupMenu(e);
                }
                private void showPopupMenu(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        // 마우스 오른쪽 버튼 클릭 시 팝업 메뉴 띄우기
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });

            menuItem1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String branchName = JOptionPane.showInputDialog(label, "Write branch name to create : ");
                        if(branchName==null){
                            return;
                        }
                        CustomJgitUtilities.createBranch(path,branch,branchName);
                        revalidateBranchMenu(path);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "An Error has occurred. Make sure your file path is correct");
                    } catch (GitAPIException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "A Git Error has occurred. Your command has not executed.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "An Error has occurred. Your command may not be executed.");
                    }
                }
            });

            menuItem2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        CustomJgitUtilities.deleteBranch(path,branch);
                        revalidateBranchMenu(path);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "An Error has occurred. Make sure your file path is correct");
                    } catch (GitAPIException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "A Git Error has occurred. Your command has not executed.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "An Error has occurred. Your command may not be executed.");
                    }
                }
            });

            menuItem3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String branchName = JOptionPane.showInputDialog(label, "Write branch name to rename : ");
                        if(branchName==null){
                            return;
                        }
                        CustomJgitUtilities.renameBranch(path,branch,branchName);
                        revalidateBranchMenu(path);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "An Error has occurred. Make sure your file path is correct");
                    } catch (GitAPIException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "A Git Error has occurred. Your command has not executed.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "An Error has occurred. Your command may not be executed.");
                    }
                }
            });

            menuItem4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        CustomJgitUtilities.checkoutBranch(path,branch);
                        revalidateBranchMenu(path);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "An Error has occurred. Make sure your file path is correct");
                    } catch (GitAPIException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "A Git Error has occurred. Your command has not executed.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(label, "An Error has occurred. Your command may not be executed.");
                    }
                }
            });
        }
    }

    public void revalidateBranchMenu(String path){
        try{
            branchPanel.removeAll();
            showBranchMenu(path, 100);
            branchPanel.revalidate();
            fileBrowser.setTextField();
            fileBrowser.updateShowPanel();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
