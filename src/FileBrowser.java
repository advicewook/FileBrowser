import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

public class FileBrowser extends JPanel implements ComponentListener {
    private static final long serialVersionUID = 1L;
    private JFrame frame;
    private JPanel displayPanel = new JPanel();
    public JPanel showPanel, commitPanel;
    private JButton renameFileButton, addFileButton, addFolderButton,
            retButton, cloneButton, saveFileButton, commitMenuButton, createRepoButton;
    private JLabel footerInfoLabel; //파일 주소 출력
    private DefaultMutableTreeNode computer, root;
    private DefaultTreeModel treeModel;
    private JTree tree;
    private JTextField treeTextField;
    private JScrollPane treeScroll, showScrollPane;
    private String currentFolder = "", selectedFolder = null;
    private int width = 1000;
    private int height = 700;
    private FileSystemView fileSystemView;
    private JTextArea textArea;
    private JPanel bttonsFooterPanel = new JPanel();

    private JPanel barPanel = new JPanel();

    private boolean isCommitMenuOpened = false;
    
    Repository currentGitRepository = null;

    CustomSwingUtilities customSwingUtilities = CustomSwingUtilities.getInstance(this);
    private void build() {
//		width = 950;
//		height = 600;
//		try {
//			// Significantly improves the look of the output in
//			// terms of the file names returned by FileSystemView!
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception e) {
//		}
        //GUI 전체 Frame
        frame = new JFrame("File Browser");
        frame.setPreferredSize(new Dimension(width, height));
        frame.setSize(frame.getPreferredSize());
        frame.setLocationRelativeTo(null);
        frame.pack();
//		frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setFocusable(true);
        frame.setBackground(Color.white);
        frame.setIconImage(getImg("img/file4.jpg"));
        frame.add(this, BorderLayout.CENTER);

        setPreferredSize(new Dimension(width, height));
        setSize(getPreferredSize());
        setBackground(Color.white);
        setFocusable(true);
        setBorder(BorderFactory.createBevelBorder(0, Color.white, Color.black));
        setLayout(new BorderLayout(0, 0));

        //이전 위치로 return
        retButton = new JButton(" ");
        retButton.setIcon(new ImageIcon(getImg("img/back.png", 40, 30).getImage()));
        retButton.setBackground(new Color(0, 0, 0, 0));
        retButton.setForeground(Color.black);
        retButton.setOpaque(false);
        retButton.setFocusable(false);

        //git clone
        cloneButton = new JButton("clone");
        cloneButton.setBackground(new Color(0, 0, 0, 0));
        cloneButton.setForeground(Color.black);
        cloneButton.setOpaque(false);
        cloneButton.setFocusable(false);



        treeTextField = new JTextField("=> Computer");
        treeTextField.setEditable(false);
        treeTextField.setOpaque(false);
        treeTextField.setForeground(Color.black);
        treeTextField.setFont(new Font("Tahoma", Font.BOLD, 14));

        showPanel = new JPanel();
        showPanel.setOpaque(false);
        showPanel.setLayout(new FlowLayout(0));

        // 하단 기능 버튼 addFolder, addFile, rename, saveFile
        addFolderButton = new JButton("new Folder");
        addFolderButton.setOpaque(false);
        addFolderButton.setBackground(new Color(0, 0, 0, 0));
        addFolderButton.setForeground(Color.black);
        addFolderButton.setFocusable(false);

        addFileButton = new JButton("new File");
        addFileButton.setOpaque(false);
        addFileButton.setBackground(new Color(0, 0, 0, 0));
        addFileButton.setForeground(Color.black);
        addFileButton.setFocusable(false);

        renameFileButton = new JButton("Rename");
        renameFileButton.setOpaque(false);
        renameFileButton.setBackground(new Color(0, 0, 0, 0));
        renameFileButton.setForeground(Color.black);
        renameFileButton.setFocusable(false);

        saveFileButton = new JButton("Save");
        saveFileButton.setOpaque(false);
        saveFileButton.setBackground(new Color(0, 0, 0, 0));
        saveFileButton.setForeground(Color.black);
        saveFileButton.setFocusable(false);

        //footerPanel - 패널을 묶는 패널인듯
        bttonsFooterPanel.setOpaque(false);
        bttonsFooterPanel.setLayout(new FlowLayout(0));
        bttonsFooterPanel.add(saveFileButton);
        bttonsFooterPanel.add(renameFileButton);
        bttonsFooterPanel.add(addFileButton);
        bttonsFooterPanel.add(addFolderButton);

        //현재 폴더의 파일 정보를 얻음
        File file = new File(currentFolder);
        footerInfoLabel = new JLabel();
        footerInfoLabel.setIcon(fileSystemView.getSystemIcon(file));
        footerInfoLabel.setText(" 2 element(s)");
        footerInfoLabel.setToolTipText(file.getPath());
        footerInfoLabel.setPreferredSize(new Dimension(120, 60));
        footerInfoLabel.setSize(footerInfoLabel.getPreferredSize());
        footerInfoLabel.setForeground(Color.black);

        // 최하단 기능 버튼 + 파일 개수
        JPanel footerPanel = new JPanel();
        footerPanel.setPreferredSize(new Dimension(750, getHeight() / 20));
        footerPanel.setSize(footerPanel.getPreferredSize());
        footerPanel.setBackground(Color.white);
        footerPanel.setLayout(new BorderLayout(0, 0));
        footerPanel.add(footerInfoLabel, BorderLayout.WEST);
        footerPanel.add(bttonsFooterPanel, BorderLayout.EAST);


        // 레포 생성 버튼
        createRepoButton = new JButton("Create Repository");
        createRepoButton.setOpaque(false);
        createRepoButton.setBackground(new Color(0, 0, 0, 0));
        createRepoButton.setForeground(Color.black);
        createRepoButton.setFocusable(false);

        // 커밋 메뉴 버튼
        commitMenuButton = new JButton("Commit Menu");
        commitMenuButton.setOpaque(false);
        commitMenuButton.setBackground(new Color(0, 0, 0, 0));
        commitMenuButton.setForeground(Color.black);
        commitMenuButton.setFocusable(false);

        //ret 버튼 + git 버튼 통합
        JPanel innerBarPanel =new JPanel();
        innerBarPanel.setOpaque(false);
        innerBarPanel.setLayout(new BorderLayout(0, 0));
        innerBarPanel.add(retButton, BorderLayout.WEST);
        innerBarPanel.add(cloneButton,BorderLayout.CENTER);
        barPanel.setOpaque(false);
        barPanel.setLayout(new BorderLayout(0, 0));
        barPanel.add(innerBarPanel, BorderLayout.WEST); // 리턴 버튼 + 파일 위치 + git clone 버튼
        barPanel.add(treeTextField, BorderLayout.CENTER);

        JPanel gitMenuPanel = new JPanel();
        gitMenuPanel.setOpaque(false);
        gitMenuPanel.setLayout(new BorderLayout(0, 0));
        gitMenuPanel.add(createRepoButton, BorderLayout.WEST);
        gitMenuPanel.add(commitMenuButton, BorderLayout.EAST);
        barPanel.add(gitMenuPanel, BorderLayout.EAST); // 커밋 메뉴 + 레포 생성 버튼



        displayPanel.setPreferredSize(new Dimension(getWidth() * 7 / 10, getHeight()));
        displayPanel.setOpaque(false);
        displayPanel.setBorder(BorderFactory.createBevelBorder(0, Color.white, Color.black));
        displayPanel.setLayout(new BorderLayout(0, 0));
        displayPanel.add(barPanel, BorderLayout.NORTH);
        displayPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    @SuppressWarnings("deprecation")
    public FileBrowser() {
        // TODO Bismi allah ^_^
        fileSystemView = FileSystemView.getFileSystemView();
        build();

        computer = new DefaultMutableTreeNode("");
        DefaultMutableTreeNode node1 = null;
        File[] files = File.listRoots();
        for (File file : files) {
            node1 = new DefaultMutableTreeNode(file);
            computer.add(node1);
            node1.add(new DefaultMutableTreeNode(true));
            //파일을 보여주는 pane을 채움
            fillShowPane(file, 0);
        }

        // 레포 생성 버튼 리스너 추가
        createRepoButton.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent arg0) {
               //System.out.println(currentFolder);
               if (CustomJgitUtilities.isGitRepository(currentFolder)) {
                   // 이미 레포가 생성된 폴더의 경우 경고창 띄우기
                   JOptionPane.showMessageDialog(null, "The repository already exists.");

               } else {
                   try {
                       Repository r = CustomJgitUtilities.createNewRepository(currentFolder);
                       System.out.println("Having repository: " + r.getDirectory());
                   } catch (IOException e) {
                       throw new RuntimeException(e);
                   }
               }
               updateShowPanel();
               updateBarPanel();
           }
           });

        // 커밋 메뉴 버튼 리스너 추가
        commitMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(isCommitMenuOpened){
                    removeCommitMenuPanel();
                    return;
                }
                if (!CustomJgitUtilities.isGitRepository(currentFolder)) {
                    JOptionPane.showMessageDialog(null, "This is not under git repository. Please make it as a git repository first.");
                    return;
                }
                // 깃 레포 체크
                if (CustomJgitUtilities.isGitRepository(currentFolder) && !isCommitMenuOpened) {
                    commitPanel = customSwingUtilities.showCommitMenu(currentFolder, getHeight());
                    displayPanel.add(commitPanel, BorderLayout.EAST);
                    displayPanel.setSize(new Dimension((getWidth() * 7 / 10) + commitPanel.getWidth(), getHeight()));
                    frame.setSize(new Dimension(width + commitPanel.getWidth(), height));
                    revalidate();
                    isCommitMenuOpened=true;
                }else{
                    removeCommitMenuPanel();
                }
            }
        });

//		File fileRoot = fileSystemView.getHomeDirectory();// new File("C:\\Users\\The Mh\\Desktop\\");

//		File[] roots = fileSystemView.getRoots();

//		root = new DefaultMutableTreeNode(fileRoot);

        treeModel = new DefaultTreeModel(computer);
//		treeModel = new DefaultTreeModel(root);
//		createChildren(root);
        tree = new JTree(treeModel);
        tree.setOpaque(false);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.addMouseListener(new Mouse());
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            // TODO for Detect iny File or Directory selected in JTree
            @SuppressWarnings("unused")
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                File file = new File(node.getUserObject().toString());
                if (file != null) {
                    currentFolder = file.getAbsolutePath();
                    treeTextField.setText(currentFolder);
                } else {
                    treeTextField.setText("Computer");
                    currentFolder = "";
                }
                updateBarPanel();
                OpenFile(file);
                selectedFolder = null;
            }
        });

        tree.addTreeExpansionListener(new TreeExpansionListener() {
            // Make sure expansion is threaded and updating the tree model
            // only occurs within the event dispatching thread.
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

                Thread runner = new Thread() {
                    public void run() {
                        node.removeAllChildren();
                        createChildren(node);
                        Runnable runnable = new Runnable() {
                            public void run() {
                                treeModel.reload(node);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                };
                runner.start();
            }

            public void treeCollapsed(TreeExpansionEvent event) {
            }
        });
        tree.expandRow(1);

        ///// >>>>>>>>>>>>>>>>>>
        treeScroll = new JScrollPane(tree);
        treeScroll.setPreferredSize(new Dimension(250, (int) getHeight()));
        treeScroll.setOpaque(false);
        treeScroll.setBorder(BorderFactory.createBevelBorder(0, Color.black, Color.black));
        treeScroll.getViewport().setOpaque(false);

        retButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String parentFolder = new File(currentFolder).getParent();
                FileRepositoryBuilder builder = new FileRepositoryBuilder();
                Repository tempGitRepository = null;
                try {
                    tempGitRepository = builder.readEnvironment().findGitDir(new File(parentFolder)).build();
                    if(tempGitRepository==null || !currentGitRepository.toString().equals(tempGitRepository.toString())){
                        removeCommitMenuPanel();

                    }
                } catch (RuntimeException | IOException ex) {
                    if(tempGitRepository==null){
                        removeCommitMenuPanel();

                    }
                }
                currentGitRepository = tempGitRepository;

                if (currentFolder != null && !currentFolder.equals("")) {
                    currentFolder = parentFolder;
                    if (currentFolder != null && !currentFolder.equals("")) {
                        treeTextField.setText(currentFolder);
                        updateBarPanel();
                        OpenFile(new File(currentFolder));
                    }
                }
                selectedFolder = null;
            }
        });

        // TODO for clone button
        cloneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = showURLInputDialog();
                //check url is not null or empty
                if (url != null && !url.equals("")) {
                    try {
                        Git result = CustomJgitUtilities.cloneRepo(url, currentFolder);
                        // Check if the clone was successful
                        if (result != null) {
                            System.out.println("Git clone was successful.");

                            // Additional logic or operations can be performed on the cloned repository here
                        } else {
                            System.out.println("Git clone failed.");
                        }
                    } catch (GitAPIException ex) {
                        System.out.println("An error occurred during Git clone: " + ex.getMessage());
                    }
                    updateShowPanel();
                    updateBarPanel();
                }
            }
        });

        addFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String nameFolder = JOptionPane.showInputDialog(frame, " Enter the Name of new Folder : ");
                if (nameFolder != null && !nameFolder.equals("")) {
                    new File(currentFolder + "\\" + nameFolder).mkdirs();
                    OpenFile(new File(currentFolder));
                }
                selectedFolder = null;
            }
        });

        addFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String nameFile = JOptionPane.showInputDialog(frame, " Enter the Name of new File.txt : ");
                if (nameFile != null && !nameFile.equals("")) {
                    if (selectedFolder == null) {

                        currentFolder = currentFolder + "\\" + nameFile + ".txt";
                        File f = new File(currentFolder);
                        writeFile(f.toPath(), "");
                        OpenFile(f);
                    } else {
                        selectedFolder = selectedFolder + "\\" + nameFile + ".txt";
                        currentFolder = selectedFolder;
                        File f = new File(selectedFolder);
                        writeFile(f.toPath(), "");
                        OpenFile(f);
                    }
                }
                selectedFolder = null;
            }
        });

        renameFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (selectedFolder != null) {
                    String nameFile = JOptionPane.showInputDialog(frame, " Enter the new Name : ");
                    if (nameFile != null && !nameFile.equals("")) {
                        File file = new File(selectedFolder);
                        selectedFolder = file.getParent() + "\\" + nameFile;
                        if (file.isDirectory())
                            file.renameTo(new File(selectedFolder));
                        else
                            file.renameTo(new File(selectedFolder + ".txt"));
                        OpenFile(new File(currentFolder));
                        updateBarPanel();
                        treeTextField.setText(currentFolder);
                    }
                    selectedFolder = null;
                }
            }
        });

        saveFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                File f = new File(currentFolder);
                if (f.exists())
                    try {
                        String fileType = Files.probeContentType(f.toPath());
                        if (fileType != null)
                            if (fileType.equals("text/plain"))
                                writeFile(f.toPath(), textArea.getText());
                    } catch (IOException e) {
                        System.out.println("!! Ereur : " + e);
                    }
            }
        });

        //주 패널 정보 출력 - 파일 트리 정보 + 파일들
        JScrollPane displayScroll = new JScrollPane(showPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        displayScroll.setOpaque(false);
        displayScroll.getViewport().add(showPanel);
        displayScroll.getViewport().setOpaque(false);
        displayScroll.getViewport().validate();
        displayScroll.setPreferredSize(new Dimension(750, (int) displayScroll.getPreferredSize().getHeight()));
        displayPanel.add(displayScroll, BorderLayout.CENTER);
//		displayPanel.add(showPanel, BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, displayPanel);
        splitPane.setOpaque(false);
        add(splitPane, BorderLayout.CENTER);
        validate();
        // --------------------------------------------------------------------------------

        frame.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                    retButton.doClick();
                else if ((event.getKeyCode() == KeyEvent.VK_N) && ((event.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    addFolderButton.doClick();
                else if ((event.getKeyCode() == KeyEvent.VK_R) && ((event.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    renameFileButton.doClick();
            }
        });
        frame.validate();
        frame.setVisible(true);
    }

//    @Override
//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);// clear and repaint
//        g.drawImage(getImg("img/background7.jpg"), 0, 0, getWidth(), getHeight(), this);
//        g.drawImage(getImg("img/Hacker-silhouette.jpg"), (int) treeScroll.getWidth(), 0,
//                (int) getWidth() - treeScroll.getWidth(), getHeight(), this);
//    }

    // TODO OpenFile for open selected file selected from JTree and showit in Show
    // is Panel
    public void OpenFile(File file) {
        if (file.exists()) {
            showPanel.removeAll();
            showPanel.setLayout(new FlowLayout(0));
            if (!file.isDirectory()) {
                footerInfoLabel.setText(" File zise : " + file.length() + " bytes");
                footerInfoLabel.setIcon(fileSystemView.getSystemIcon(file));
                String fileType = "Undetermined";
                try {
                    fileType = Files.probeContentType(file.toPath());
                } catch (IOException ioException) {
                    fileType = "Undetermined";
                    System.out.println("!! ERROR: " + ioException);
                }
                if (fileType == null)
                    fileType = "Undetermined";

                if (fileType.toString().equals("text/plain")) {// || fileType.toString().equals("application/pdf")) {
                    if (fileType.toString().equals("text/plain"))
                        // TEXTE
                        textArea = new JTextArea(readFile(file.toString()));
                    else
                        textArea = new JTextArea(readPdf(currentFolder));
                    textArea.setBackground(Color.LIGHT_GRAY);
                    textArea.setForeground(Color.black);
                    textArea.setFont(new Font("Tahoma", Font.BOLD, 13));

                    showScrollPane = new JScrollPane();
                    showScrollPane.getViewport().add(textArea, BorderLayout.CENTER);
                    showScrollPane.setPreferredSize(showPanel.getSize());
                    showScrollPane.validate();
                    showPanel.add(showScrollPane, BorderLayout.CENTER);
                    showPanel.validate();

//				} else if (fileType.toString().equals("application/pdf")) {

//			} else if (fileType.toString().equals("video/mp4") || fileType.toString().equals("video/webm")) {
//
//			} else if (fileType.toString().equals("audio/mpeg")) {
//
//			} else if (fileType.toString().equals("application/x-zip-compressed")) {

                } else {
                    Image img;
                    if (fileType.toString().equals("image/jpeg") || fileType.toString().equals("image/png")
                            || fileType.toString().equals("image/jpg") || fileType.equals("image/gif")) {

                        img = new ImageIcon(file.toString()).getImage();

                    } else {
//						System.out.println(fileType);
                        img = getImg("img/unsupported2.png");
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().open(file);
                            } catch (IOException ex) {
                                System.err.println("!! Ereur : " + ex);
                            }
                        }
                    }
                    img = img.getScaledInstance(showPanel.getWidth(), showPanel.getHeight(), Image.SCALE_SMOOTH);

                    JLabel label = new JLabel(new ImageIcon(img));
                    label.setPreferredSize(showPanel.getSize());
                    showPanel.add(label, BorderLayout.CENTER);
                }

            } else {
                showPanel.setLayout(new GridLayout(0, 6, 10, 10));
                File[] files = file.listFiles();
                if (files != null) {
                    footerInfoLabel.setText(" " + files.length + " element(s)");
                    footerInfoLabel.setIcon(fileSystemView.getSystemIcon(file));
                    for (File f : files) {
                        fillShowPane(f, 1);
                    }
                }
            }
            revalidate();
            repaint();
        }
    }

    public String getStatusImage(String rootPath, String filePath) throws GitAPIException, IOException {
        String status = "";
        System.out.println("filePath - " + filePath);
        System.out.println("rootPath - " + rootPath);
        String fileInRepo = filePath.replace(rootPath+"\\","");
        fileInRepo = fileInRepo.replace("\\","/");
        System.out.println("file name : " + fileInRepo);

        Status allFileStatus = customSwingUtilities.getStatus(rootPath);
        Set<String> untracked = allFileStatus.getUntracked();
        Set<String> modified = allFileStatus.getModified();
        Set<String> added = allFileStatus.getAdded();
        Set<String> changed = allFileStatus.getChanged();
        Set<String> removed = allFileStatus.getRemoved();

        if (untracked.contains(fileInRepo)){
            status = "<html><div style='text-align:center;'>" +
                    "<img src=\"https://i.esdrop.com/d/f/1GbLJlgm9n/BLKAXtnbdf.png\" width=\"15\" height=\"15\"/>"+
                    "</div>";
        }
        else if (modified.contains(fileInRepo)){
            status = "<html><div style='text-align:center;'>" +
                    "<img src=\"https://i.esdrop.com/d/f/1GbLJlgm9n/gnM3JMEk24.png\" width=\"15\" height=\"15\"/>"+
                    "</div>";
        }
        else if (added.contains(fileInRepo)){
            status = "<html><div style='text-align:center;'>" +
                    "<img src=\"https://i.esdrop.com/d/f/1GbLJlgm9n/GG2Ju2uJfm.png\" width=\"15\" height=\"15\"/>"+
                    "</div>";
        }
        else if(changed.contains(fileInRepo)){
            status = "<html><div style='text-align:center;'>" +
                    "<img src=\"https://i.esdrop.com/d/f/1GbLJlgm9n/Mpbg9fpQSD.png\" width=\"15\" height=\"15\"/>"+
                    "</div>";
        }
        else if(removed.contains(fileInRepo)){
            status = "<html><div style='text-align:center;'>" +
                    "<img src=\"https://i.esdrop.com/d/f/1GbLJlgm9n/38q8kGJzqP.png\" width=\"15\" height=\"15\"/>"+
                    "</div>";
        }
        else{
            status = " ";
        }
        return status;
    }
    
    //파일 정보를 보여주는 pane을 채움
    private void fillShowPane(File f, int choice) {
        JButton fileButton = new JButton();
        try {
            ImageIcon imgIcon = (ImageIcon) fileSystemView.getSystemIcon(f);
            Image img = imgIcon.getImage();
            img = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
            fileButton.setIcon(new ImageIcon(img));
            String filename = fileSystemView.getSystemDisplayName(f);
            String filePath = f.getPath();
            String temp = filename;

            // 레포 안의 파일
            if (CustomJgitUtilities.isGitRepository(currentFolder)){
                String status = getStatusImage(currentFolder, filePath);
                if (!status.equals(" ")){
                    temp = status+"<p>"+filename+"</p></html>";}
//                temp = "<html><div style='text-align:center'>"+filename
//                        +"<br><font color=\"#A9A9A9\">"+status+"</font></div></html>";
            }

            // 레포 안의 하위 폴더 처리
            else if (CustomJgitUtilities.isSubGitRepository(currentFolder)){
                String rootRepoPath = CustomJgitUtilities.findRepoPath(new File(currentFolder));
                String status = getStatusImage(rootRepoPath, filePath);
                if (!status.equals(" ")){
                    temp = status+"<p>"+filename+"</p></html>";}
//                temp = "<html><div style='text-align:center'>"+filename
//                        +"<br><font color=\"#A9A9A9\">"+status+"</font></div></html>";
            }
            fileButton.setText(temp);
            fileButton.setHorizontalTextPosition(SwingConstants.CENTER);
            fileButton.setVerticalTextPosition(JButton.BOTTOM);
            fileButton.setToolTipText(f.getPath());

            if (choice == 0)
                fileButton.setPreferredSize(new Dimension(140, 150));
            else
                fileButton.setPreferredSize(new Dimension(100, 120));
            fileButton.setSize(fileButton.getPreferredSize());
            fileButton.setBackground(Color.white);
            fileButton.setForeground(Color.black);
            fileButton.setForeground(Color.black);
            fileButton.setOpaque(false);
            fileButton.addMouseListener(new Mouse(fileButton.getToolTipText()));
            fileButton.setBorder(new LineBorder(Color.BLACK, 0));
            showPanel.add(fileButton);
        } catch (Exception e) {
            System.out.println("!! Ereur : " + e);
//			e.getStackTrace();
        }
    }
    public void updateShowPanel(){
        // 새로고침
        showPanel.removeAll();
        showPanel.revalidate();
        showPanel.repaint();

        File file = new File(currentFolder);
        File[] files = file.listFiles();
        for (File f : files){
            fillShowPane(f, 1);
        }
    }

    //파일 읽는 기능
    private String readFile(String file) {
        // LIS LE FICHIER
        String lines = "";
        String line;

        try {
            // CREE LE FLUX
            BufferedReader reader = new BufferedReader(new FileReader(file));
            // LIS LIGNE A LIGNE
            while ((line = reader.readLine()) != null)
                lines += line + "\n";
            reader.close();
        } catch (Exception e) {
            lines = "Une erreur s'est produite durant la lecture du flux : " + e.getMessage();
        }
        return lines;
    }

    private void writeFile(Path pt, String contenu) {
        try (BufferedWriter bw = Files.newBufferedWriter(pt, Charset.forName("UTF-8"), StandardOpenOption.CREATE)) {
            bw.write(contenu);
        } catch (Exception e) {
            System.out.println("!! Ereur in function writeFile(Path pt, String contenu) :\n=>  " + e.getMessage());
        }
    }

    private String readPdf(String pdfFile) {
//		try {
//			PdfReader reader = new PdfReader(pdfFile);
//			StringBuffer sb = new StringBuffer();
//			PdfReaderContentParser parser = new PdfReaderContentParser(reader);
//			TextExtractionStrategy strategy;
//			for (int cmp = 1; cmp <= reader.getNumberOfPages(); cmp++) {
//				strategy = parser.processContent(cmp, new SimpleTextExtractionStrategy());
//				sb.append(strategy.getResultantText());
//			}
//			reader.close();
//			return sb.toString();
//		} catch (IOException e) {
//			throw new IllegalArgumentException("Not able to read file " + pdfFile, e);
//		}
        return "";
    }

    @SuppressWarnings({"deprecation"})
    private void createChildren(DefaultMutableTreeNode node) {
        File fileRoot = new File(node.getUserObject().toString());
        File[] files = fileRoot.listFiles();
        if (files != null) {
            for (File file : files) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
                node.add(childNode);
                if (file.isDirectory())
                    childNode.add(new DefaultMutableTreeNode(true));
//					createChildren(childNode);
            }
        } else
            node.add(new DefaultMutableTreeNode(true));
    }

    public Image getImg(String sh) {
        try {
//			return new ImageIcon(getClass().getResource(sh)).getImage();
            return new ImageIcon("src\\" + sh).getImage();
        } catch (Exception e) {
            System.out.println("!! Ereur : image {\"" + sh + "\"} not found :: " + e.getMessage());
        }
        return null;
    }

    public ImageIcon getImg(String sh, int width, int height) {
        try {
            Image img = getImg(sh);
            img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);// getImg(sh).getScaledInstance(width,
            // height, Image.SCALE_SMOOTH)
            return new ImageIcon(img);
        } catch (Exception e) {
            System.out.println("!! Ereur : image {\"" + sh + "\"} not found :: " + e.getMessage());
            return null;
        }
    }

    private void showMessageDialog(String sh) {
        JOptionPane.showMessageDialog(this, sh, "Not Enabled", JOptionPane.ERROR_MESSAGE);
    }

    public class Mouse extends MouseAdapter {
        private String sh;

        public Mouse(String sh) {
            this.sh = sh;
        }

        public Mouse() {
            super();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger())
                doPop(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
//			super.mousePressed(e);
            if (e.isPopupTrigger())
                doPop(e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository tempGitRepository = null;
            try {
                tempGitRepository = builder.readEnvironment().findGitDir(new File(currentFolder)).build();
                if(tempGitRepository==null || !currentGitRepository.toString().equals(tempGitRepository.toString())){
                    removeCommitMenuPanel();

                }
            } catch (RuntimeException | IOException ex) {
                if(tempGitRepository==null){
                    removeCommitMenuPanel();

                }
            }
            currentGitRepository = tempGitRepository;
            
            if (e.getSource() != tree) {
                if (e.getClickCount() == 1) {
                    selectedFolder = sh;
                } else {
                    currentFolder = sh;
                    OpenFile(new File(currentFolder));
                    treeTextField.setText(currentFolder);
                    updateBarPanel();
                    selectedFolder = null;
                }
            }
        }

        private void doPop(MouseEvent e) {
            PopUpDemo menu = new PopUpDemo(e);
            menu.show(e.getComponent(), e.getX(), e.getY());
        }

        // TODO PopMenu
        //우클릭 했을 때 팝업이 나오면 그 때 사용할 기능
        class PopUpDemo extends JPopupMenu {
            private static final long serialVersionUID = 1L;
            JMenuItem rename, addFolder, addFile, openInDesktop;
            JMenuItem gitAddFile, gitRestoreModifiedFile, gitRestoreStaged, gitUntracking, gitDeleteFile, gitMvFile;
            public PopUpDemo(MouseEvent e)  {
                JButton tempButton = (JButton) e.getSource();
                String fileName = tempButton.getText();
                System.out.println(fileName);
                System.out.println(currentFolder);

                gitAddFile = new JMenuItem("Add to git");
                gitAddFile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            CustomJgitUtilities.addFile(currentFolder,fileName);
                            if (isCommitMenuOpened) {
                                customSwingUtilities.revalidateCommitMenu(currentFolder);
                            }
                            updateShowPanel();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                gitRestoreModifiedFile = new JMenuItem("Unmodifying");
                gitRestoreModifiedFile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            CustomJgitUtilities.restoreModifiedFile(currentFolder, fileName);
                            if (isCommitMenuOpened) {
                                customSwingUtilities.revalidateCommitMenu(currentFolder);
                            }
                            updateShowPanel();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                gitRestoreStaged = new JMenuItem("Unstage changes");
                gitRestoreStaged.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            CustomJgitUtilities.restoreStagedFile(currentFolder, fileName);
                            if (isCommitMenuOpened) {
                                customSwingUtilities.revalidateCommitMenu(currentFolder);
                            }
                            updateShowPanel();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                gitUntracking = new JMenuItem("Untracking");
                gitUntracking.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            CustomJgitUtilities.removeCachedFile(currentFolder, fileName);
                            if (isCommitMenuOpened) {
                                customSwingUtilities.revalidateCommitMenu(currentFolder);
                            }
                            updateShowPanel();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                gitDeleteFile = new JMenuItem("Delete file");
                gitDeleteFile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            CustomJgitUtilities.removeFile(currentFolder, fileName);
                            if (isCommitMenuOpened) {
                                customSwingUtilities.revalidateCommitMenu(currentFolder);
                            }
                            updateShowPanel();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                gitMvFile = new JMenuItem("Rename tracked file");
                gitMvFile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String newName = JOptionPane.showInputDialog(frame, " Enter the new Name : ");
                        if (newName != null && !newName.equals("")){
                            try {
                                CustomJgitUtilities.mvFile(currentFolder, fileName, newName);
                                if (isCommitMenuOpened) {
                                    customSwingUtilities.revalidateCommitMenu(currentFolder);
                                }
                                updateShowPanel();
                                updateBarPanel();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                    }
                    }
                });


                rename = new JMenuItem("-> Rename ");
                rename.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent event) {
//						selectedFolder=e.getComponent().getParent().getName();
//						System.out.println(selectedFolder);
                        renameFileButton.doClick();
                    }
                });
                addFolder = new JMenuItem("-> new Folder ");
                addFolder.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        addFolderButton.doClick();
                    }
                });
                addFile = new JMenuItem("-> new File ");
                addFile.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        addFileButton.doClick();
                    }
                });
                openInDesktop = new JMenuItem("-> Open in Desktop ");
                openInDesktop.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        String sh = "";
                        if (selectedFolder == null || selectedFolder.equals(""))
                            sh = currentFolder;
                        else
                            sh = selectedFolder;
                        if (sh != null && !sh.equals("")) {
                            File file = new File(sh);
                            if (Desktop.isDesktopSupported()) {
                                try {
                                    Desktop.getDesktop().open(file);
                                } catch (IOException ex) {
                                    System.err.println("!! Ereur : " + ex);
                                }
                            }
                        } else
                            showMessageDialog("You chode select File");
                    }
                });

                try {
                    if (CustomJgitUtilities.isSubGitRepository(currentFolder)&&CustomJgitUtilities.isUntracked(currentFolder,fileName)) {
                        add(gitAddFile);
                        addSeparator();

                    }
                    else if (CustomJgitUtilities.isSubGitRepository(currentFolder)&&CustomJgitUtilities.isModified(currentFolder, fileName)) {
                        add(gitAddFile);
                        add(gitRestoreModifiedFile);
                        addSeparator();

                    }
                    else if (CustomJgitUtilities.isSubGitRepository(currentFolder)&&CustomJgitUtilities.isStaged(currentFolder, fileName)) {
                        add(gitRestoreStaged);
                        addSeparator();

                    }
                    else if (CustomJgitUtilities.isSubGitRepository(currentFolder)&&CustomJgitUtilities.isCommitted(currentFolder,fileName)) {
                        add(gitUntracking);
                        add(gitDeleteFile);
                        add(gitMvFile);
                        addSeparator();

                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (GitAPIException ex) {
                    throw new RuntimeException(ex);
                }

                add(rename);
                add(addFolder);
                add(addFile);
                add(openInDesktop);
            }
        }
    }

    /**
     * A TreeCellRenderer for a File.
     */
    class FileTreeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 1L;

        private JLabel label;

        FileTreeCellRenderer() {
            label = new JLabel();
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            File file = new File(node.getUserObject().toString());
            label.setIcon(fileSystemView.getSystemIcon(file));
            label.setText(fileSystemView.getSystemDisplayName(file));
            label.setToolTipText(file.getPath());
            label.setOpaque(false);
            return label;
        }
    }

    @Override
    public void componentHidden(ComponentEvent arg0) {

    }

    @Override
    public void componentMoved(ComponentEvent arg0) {

    }

    @Override
    public void componentResized(ComponentEvent arg0) {
        // TODO wath cmp can dow if the taille of JFrame has changed
        width = getWidth();
        height = getHeight();
        revalidate();
        repaint();
    }

    @Override
    public void componentShown(ComponentEvent arg0) {

    }

    public void removeCommitMenuPanel(){
        if(commitPanel!=null){
            displayPanel.remove(commitPanel);
            displayPanel.setSize(new Dimension((getWidth() * 7 / 10), getHeight()));
            frame.setSize(new Dimension(width, height));
            revalidate();
            isCommitMenuOpened=false;
        }
    }

    // dialog for input URL
    //make dialog with multiple panel for flexibility and extensibility
    public String  showURLInputDialog(){
        JDialog dialog = new JDialog(frame, "Input URL for clone", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel URLPanel = new JPanel();
        URLPanel.setOpaque(false);
        URLPanel.setLayout(new BoxLayout(URLPanel, BoxLayout.Y_AXIS));

        JPanel textFieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JTextField textField = new JTextField(30);
        textField.setText("Enter URL");
        textField.setOpaque(true);
        textField.setBackground(Color.white);
        textField.setForeground(Color.black);
        textFieldPanel.add(textField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("OK");

        okButton.setForeground(Color.black);
        okButton.setOpaque(false);
        okButton.setFocusable(false);

        buttonPanel.add(okButton);

        okButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               String URLString = textField.getText();
               if(!URLString.isEmpty()){
                   System.out.println("Enterd URL is : "+ URLString);
               }else{
                   System.out.println("NO URL enterd");
               }
               dialog.dispose();
           }
        });

        URLPanel.add(textFieldPanel);
        URLPanel.add(Box.createVerticalStrut(10));
        URLPanel.add(buttonPanel);

        dialog.getContentPane().add(URLPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);

        return textField.getText();
    }

    //update barpanel
    //if the current folder is a git repository, then make invisible clone button
    //if the current folder is not a git repository, then make visible clone button
    public void updateBarPanel(){
        if(CustomJgitUtilities.isSubGitRepository(currentFolder)){
            cloneButton.setVisible(false);
        }else{
            cloneButton.setVisible(true);
        }

        barPanel.revalidate();
        barPanel.repaint();
    }

    // --------------------- test
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        new FileBrowser();
    }


}