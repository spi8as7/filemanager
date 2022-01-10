import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Position;
import javax.swing.tree.*;

public class FileBrowser extends JFrame {
    /* statheres tou programmatos */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    private static final String CONFIG_FILENAME=System.getProperty("user.home")+File.separator+".ce325fb.config";
    private static final String DEFAULT_EDITOR="notepad.exe";
    private static final String IMAGE_DIR="images";
    private static final ImageIcon NULL_ICON=createImageIcon("null.png");
    private static final int ICON_WIDTH=(NULL_ICON==null) ? 65 : NULL_ICON.getIconWidth();
    private static final int ICON_VIEW=0;
    private static final int LIST_VIEW=1;
    private static final String[] LIST_NAMES=new String[]{"File","Filename","Filetype","Length","Last Modified"};
    
    /* arxiko megethos */
    private int width=800;
    private int height=600;    
    
    private String rootDir; /* arxikos fakelos */
    
    /* metavlites pou kratane tis epiloges tou xristi */
    private ArrayList<File> fileSelection=new ArrayList<>();
    private File lastFolder=null;
    
    /* components tou frame */
    private JMenuItem itemNewDir,itemNewFile,itemRename,itemDelete,itemZip;    
    private JPopupMenu popupmenu;
    private JMenuItem popupDir,popupFile,popupRename,popupDelete,popupZip;
    
    private TreeDir treeDir;        /* to dendro me tous fakelous */
    private JTextField searchField; /* keimeno gia to search */
    
    private static final String[] columnNames=new String[] {"Type","Icon","Program"};   /* gia ton pinaka twn parametrwn */
    
    private String[][] fileTypes;       /* pinakas me tous typous twn arxeiwn kai ta eikoneidia */
    private ImageIcon[] icons;          /* pinakas eikoneidiwn */
    
    private int currentView=ICON_VIEW;  /* trexousa emfanisi */


    /* klasi pou xrisimopoieitai gia thn antistoixisi twn typwn twn arxeiwn */
    public class ConfDialog extends JDialog implements MouseListener {                    
        private JButton addButton=new JButton("Add");
        private JButton delButton=new JButton("Delete");
        private JButton saveButton=new JButton("Save");
        private JButton cancelButton=new JButton("Cancel");
        private Table table;
        private JScrollPane scroll;
        private String[][] data;
        private boolean inputCanceled;

        /* allazei tin kataxwrisi enos typou */
        public class InputDialog extends JDialog  {                           
            private JButton okButton=new JButton("OK");
            private JButton cancelButton=new JButton("Cancel");
            
            private JTextField typeField=new JTextField(20);
            private JButton iconField=new JButton();
            private JButton progField=new JButton();

            private int row, rowIndex;

            /* kaleitai gia prosthiki neou typou */
            public InputDialog() {
                super(FileBrowser.this, "Files association", true);            
                rowIndex=-1;
                row=-1;
                setLocationRelativeTo(FileBrowser.this);
                addComponents();
                addListeners();
                pack();
                setVisible(true);
            }
            
            public InputDialog(int rowIndex) {
                super(FileBrowser.this, "Files association", true);            
                this.rowIndex=rowIndex;
                row=table.convertRowIndexToModel(rowIndex);
                Object obj;
                obj=table.getModel().getValueAt(row, 0);
                String value="";
                if (obj!=null) value=obj.toString();
                typeField.setText(value);
                value="";
                obj=table.getModel().getValueAt(row, 1);
                if (obj!=null) value=obj.toString();
                iconField.setText(value);
                value="";
                obj=table.getModel().getValueAt(row, 2);
                if (obj!=null) value=obj.toString();
                progField.setText(value);
                setLocationRelativeTo(FileBrowser.this);
                addComponents();
                addListeners();
                pack();
                setVisible(true);
            }
            
            private void addListeners() {
                /* an to icon den uparxei ston default fakelo, to antigrafei sto fakelo */
                iconField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) { 
                        String imagePath=FileBrowser.class.getResource(IMAGE_DIR).getPath();
                        final JFileChooser fc = new JFileChooser(imagePath);
                        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);               
                        FileNameExtensionFilter filter = new FileNameExtensionFilter("Icon images", "gif", "png", "jpg", "bmp");                       
                        fc.setFileFilter(filter);
                        int returnVal = fc.showOpenDialog(rootPane);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file=fc.getSelectedFile();
                            iconField.setText(file.getName());
                            if (!file.getPath().equals(imagePath)) {
                                Path source=file.toPath();
                                File f=new File(imagePath+File.separator+file.getName());
                                Path target=f.toPath();
                                try {                                        
                                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(rootPane, "Error copying icon.\n"+ex.getMessage(), "Copying icon...", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }            
                });
                
                /* programma pou tha ekteleitai otan epilegetai o typos */
                progField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {               
                        final JFileChooser fc = new JFileChooser(System.getenv("ProgramFiles"));
                        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);               
                        FileNameExtensionFilter filter = new FileNameExtensionFilter("Exec programms", "exe", "com");                       
                        fc.setFileFilter(filter);
                        int returnVal = fc.showOpenDialog(rootPane);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            progField.setText(fc.getSelectedFile().toString());
                        }
                    }            
                });
                
                
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {                       
                        table.setValueAt(typeField.getText(), rowIndex, 0);
                        table.setValueAt(iconField.getText(), rowIndex, 1);
                        table.setValueAt(progField.getText(), rowIndex, 2);
                        closeDialog();
                    }
                });
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        inputCanceled=true;
                        closeDialog();
                    }
                });
            }

            private void closeDialog() {
                this.dispose();
            }

            /* xtizei to parathyro */
            private void addComponents() {            
                setLayout(new BorderLayout());        

                JPanel centerPanel=new JPanel();
                centerPanel.setLayout(new GridLayout(3,2));
                centerPanel.add(new JLabel("Type:"));
                centerPanel.add(typeField);
                centerPanel.add(new JLabel("Icon:"));
                centerPanel.add(iconField);
                centerPanel.add(new JLabel("Program:"));
                centerPanel.add(progField);
                add(centerPanel, BorderLayout.CENTER);
                
                JPanel buttonPanel=new JPanel();
                buttonPanel.setLayout(new FlowLayout());
                buttonPanel.add(okButton);
                buttonPanel.add(cancelButton);
                add(buttonPanel, BorderLayout.SOUTH);
            }        

        }
                
        public ConfDialog() {
            super(FileBrowser.this, "Files association", true);            
            setLocationRelativeTo(FileBrowser.this);
            setSize(500, 300);
            addComponents();
            addListeners();
            setVisible(true);
        }
    
        private void addListeners() {                        
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {                    
                    /* dhmioyrgoume mia nea eggrafi gia na tin gemisoume me dedomena */
                    int n=0;
                    if (data!=null) n=data.length;
                    String[][] newData=new String[n+1][];
                    for(int i=0; i<n; i++) {
                        newData[i]=data[i];
                    }
                    newData[n]=new String[]{"","",""};
                    table=new Table(newData, columnNames, false);           
                    table.sizeToFit();
                    table.setRowSelectionAllowed(true);
                    table.setColumnSelectionAllowed(false);
                    table.addMouseListener(ConfDialog.this);
                    scroll.setViewportView(table);
                    new InputDialog(n);
                    if (!inputCanceled) data=newData;   // an akyrothei tote epanaferetai o arxikos pinakas
                    
                    /* epanemfanisis tou pinaka */
                    table=new Table(data, columnNames, false);           
                    table.sizeToFit();
                    table.setRowSelectionAllowed(true);
                    table.setColumnSelectionAllowed(false);
                    table.addMouseListener(ConfDialog.this);
                    scroll.setViewportView(table);                                           
                }
            });
            
            delButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int rowIndex = table.getSelectedRow();
                    if (rowIndex<0) return;
                    int row=table.convertRowIndexToModel(rowIndex);
                    int n=0;
                    if (data!=null) n=data.length;
                    else return ;
                    String[][] newData=new String[n-1][];
                    
                    for(int i=0, j=0; i<n; i++) {
                        if (i!=row) {
                            newData[j]=data[i];
                            j++;
                        }
                    }
                    data=newData;   // an akyrothei tote epanaferetai o arxikos pinakas
                    
                    /* epanemfanisis tou pinaka */
                    table=new Table(data, columnNames, false);           
                    table.sizeToFit();
                    table.setRowSelectionAllowed(true);
                    table.setColumnSelectionAllowed(false);
                    table.addMouseListener(ConfDialog.this);
                    scroll.setViewportView(table);                                           
                }
            });
                        
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (fileTypes==null || data.length!=fileTypes.length) {
                        fileTypes=new String[data.length][3];
                    }
                    copyData(data,fileTypes);
                    try {
                        writeConfig();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(rootPane, "Error saving configuration.\n"+ex.getMessage(), "Saving configuration", JOptionPane.ERROR_MESSAGE);
                    }
                    createIcons();
                    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();                    
                    renderer.setBackgroundSelectionColor(Color.yellow);
                    renderer.setBorderSelectionColor(Color.black);
                    renderer.setTextSelectionColor(Color.red);
                    ImageIcon folderIcon=getIcon(null);            
                    if (folderIcon != null) {
                        renderer.setClosedIcon(folderIcon);
                        renderer.setOpenIcon(folderIcon);
                        renderer.setLeafIcon(folderIcon);
                    }
                    treeDir.dirTree.setCellRenderer(renderer);
                    
                    treeDir.display();
                    closeDialog();
                }
            });
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    closeDialog();
                }
            });
        }
        
        private void closeDialog() {
            this.dispose();
        }
    
        private void addComponents() {            
            setLayout(new BorderLayout());
            if (fileTypes!=null) {
                data=new String[fileTypes.length][3];            
                copyData(fileTypes,data);            
                table=new Table(data, columnNames, false);           
                table.sizeToFit();
                table.setRowSelectionAllowed(true);
                table.setColumnSelectionAllowed(false);
                table.addMouseListener(this);
            }
            scroll=new JScrollPane(table);            
            add(scroll, BorderLayout.CENTER);            
            JPanel buttonPanel=new JPanel();
            buttonPanel.setLayout(new FlowLayout());
            buttonPanel.add(addButton);
            buttonPanel.add(delButton);
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }        

        void copyData(String[][] from, String[][] to){
            for(int i=0; i<from.length;i++) {
                for(int j=0; j<from[i].length;j++) {
                    to[i][j]=from[i][j];
                }
            }            
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = table.getSelectedRow();
            if (row >= 0) {
                new InputDialog(row);
            }   
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
    }
       
    public class TreeDir extends JPanel implements TreeSelectionListener, TreeExpansionListener {
        private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        private JTree dirTree;
        private JScrollPane folderView;
        private ArrayList<JLabel> labelSelection=new ArrayList<>();
        private Table listTable;        
        private Vector names;
        
        public TreeDir(String rootDir) {
            super(new GridLayout(1,0));
            names=new Vector();
            this.setSize(FileBrowser.this.getSize());
            this.setPreferredSize(FileBrowser.this.getPreferredSize());
            for(int i=0; i<LIST_NAMES.length; i++) {
                names.add(LIST_NAMES[i]);
            }            
            createTree(rootDir);         
        }

        /* diavazei ta direccories kai dhmioyrgei to dendro gia to aristero panel */
        private void createTree(String rootDir) {
            //Create the nodes.
            DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
            File dir=new File(rootDir);
            top=addNodes(null, dir.getAbsolutePath());        
            //Create a tree that allows one selection at a time.
            dirTree = new JTree(top);
            
            dirTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            /* kathosizei to eikonidio gia to dendro kai ta xrwmata twn epilogwn*/
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setBackgroundSelectionColor(Color.yellow);
            renderer.setBorderSelectionColor(Color.black);
            renderer.setTextSelectionColor(Color.red);
            ImageIcon folderIcon=getIcon(null);            
            if (folderIcon != null) {
                renderer.setClosedIcon(folderIcon);
                renderer.setOpenIcon(folderIcon);
                renderer.setLeafIcon(folderIcon);
            }
            dirTree.setCellRenderer(renderer);

            //Listen for when the selection changes.
            dirTree.addTreeSelectionListener(this);
            dirTree.addTreeExpansionListener(this);
            //Create the scroll pane and add the tree to it. 
            JScrollPane treeView = new JScrollPane(dirTree);        
            //Create the Folder contents viewing pane.
            folderView=new JScrollPane();
            folderView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            folderView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            //Add the scroll panes to a split pane.

            /* sthnoume to listener gia gia to popup menu */
            folderView.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {}

                @Override
                public void mousePressed(MouseEvent e) {}

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        for(JLabel lbl:labelSelection) {
                            lbl.setForeground(Color.black);
                        }
                        labelSelection.clear();
                        fileSelection.clear();
                        popupRename.setEnabled(false);
                        popupDelete.setEnabled(false);
                        popupZip.setEnabled(false);
                        popupmenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            });
            
            /* kathorizei ti tha emfanizetai se kathe panel */            
            splitPane.setTopComponent(treeView);
            splitPane.setBottomComponent(folderView);
            splitPane.setDividerLocation(200); 
            add(splitPane); // topotheti to splitpane sto panel
        }

        /* methodos gia ton listener tou dendrou */
        public void valueChanged(TreeSelectionEvent e) {
//            DefaultMutableTreeNode node = (DefaultMutableTreeNode) dirTree.getLastSelectedPathComponent();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            if (node == null) return;
            FileObject fo=(FileObject) node.getUserObject();
            String folder=fo.getAbsolutePath();  // getPath(node);
            lastFolder= fo;          //new File(folder); // apothikevei tin epilogi sto lastfolder
            display(node); // emfanizei ta periexomena tou fakelou
        }

        
        private  boolean existsNode(DefaultMutableTreeNode root, DefaultMutableTreeNode node) {
            Enumeration children=root.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode curr=(DefaultMutableTreeNode) children.nextElement();
                if (curr.getUserObject().toString().equals(node.getUserObject().toString())) return true;
            }
            return false;
        }

        /* change directory */
        public void changeDir(File newDir) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dirTree.getLastSelectedPathComponent();
            Enumeration children=parent.children();           
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
                if (((File) node.getUserObject()).equals(newDir)) {
                    TreeNode[] nodes=node.getPath();
                    TreePath path=new TreePath(nodes);                   
                    dirTree.getSelectionModel().clearSelection();
                    dirTree.getSelectionModel().addSelectionPath(path);
                    break;
                }
            }
        treeDir.display();                    
        }
        
        public void display() {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) dirTree.getLastSelectedPathComponent();
            if (node == null) return;
            FileObject fo=(FileObject) node.getUserObject();
            lastFolder=fo;  //new File(folder); // apothikevei tin epilogi sto lastfolder
            display(node); // emfanizei ta periexomena tou fakelou            
        }

        private void addFilesToDisplay(String searchStr, String folder, ArrayList<File> directoryFiles, Vector data, JPanel iconPanel) {
                for (File file : directoryFiles) {                                        
                    String filename = file.getName();
                    Vector row=new Vector();
                    row.add(file);
                    if (searchStr!=null) row.add(file);
                    else row.add(filename);
                    
                    /* vriskei ton typoy tou arxeio apo tin katalixi kai daibazei to antistoixo eikoneidio */
                    String filetype;
                    if (file.isDirectory()) filetype=null; 
                    else {
                        int pos=filename.lastIndexOf(".");
                        if (pos!=-1) filetype=filename.substring(pos, file.getName().length()).toLowerCase();
                        else filetype="";
                    }                     
                    if (filetype==null) row.add("dir");
                    else row.add(filetype);
                    data.add(row);
                    
                    if (file.isFile()) row.add(file.length());
                    else row.add("");
                    row.add(dateFormat.format(file.lastModified()));

                    /* an to onoma enos arxeiou-fakelou einai megalytero apo 15 xaraktires,
                       to emfanizei se 2 grammes twn 10 grammatwn alla mexri to poly 20 xaraktires */
                    String str=filename;
                    if (filename.length()>15) {
                        str="<html>";
                        for (int i=0;i<filename.length() && i<20; ) {
                            if (i!=0) str=str+"<br>";
                            for (int j=0; i<filename.length() && j<10; j++) {
                                str=str+filename.charAt(i++);
                            }
                        }
                        str=str+"</html>";
                    }
                    if (searchStr!=null) str=folder + File.separator+str;
                    iconPanel.add(createLabel(str, filetype, file));                    
                }            
        }
        
        /* prosthetei arxeia stin othoni */
        private void addFolderToDisplay(String searchStr, String folder, DefaultMutableTreeNode curNode, Vector data, JPanel iconPanel) throws IOException {
            DirectoryStream<Path> directoryFiles;
            ArrayList<File> dirArray=null;
                if (searchStr==null) { 
                    directoryFiles=Files.newDirectoryStream(Paths.get(folder));                    
                } else {                                        
                    File curDir=new File(folder);
                    File[] dir=curDir.listFiles();
                    
                    for(int i=0; i<dir.length; i++) {
                        if (dir[i].isDirectory()) {
                            if (dirArray==null) dirArray=new ArrayList<>();
                            dirArray.add(dir[i]);
                        }
                    }
                    directoryFiles=Files.newDirectoryStream(Paths.get(folder), searchStr);
                    curNode=null;
                }

                ArrayList<File> dirList=new ArrayList<>();
                ArrayList<File> fileList=new ArrayList<>();                
                for (Path filePath : directoryFiles) {
                    final File file = filePath.toFile();
                    if (file.isDirectory()) {
                        dirList.add(file);
                        /* prosthetei sto dendro tou fakelous poy vriskontai mesa sto trexonta fakelo kai den exoun topothetithei */
                        if (curNode!=null && file.isDirectory()) {                        
                            FileObject fo=new FileObject(file);
                            DefaultMutableTreeNode node = new DefaultMutableTreeNode(fo);
                            if (!existsNode(curNode,node)) {
                                curNode.add(node);
                            }                                                
                        }                        
                    }
                    else fileList.add(file);                    
                }
                
                addFilesToDisplay(searchStr, folder,  dirList, data, iconPanel);  // list directories
                addFilesToDisplay(searchStr, folder,  fileList, data, iconPanel); // list files
                
                if (searchStr!=null && curNode==null && dirArray!=null) {
                    for (File f:dirArray) {
                        addFolderToDisplay(searchStr, f.getAbsolutePath(), null, data, iconPanel);
                    }
                }
        }

        public void resize() {
            int x= (FileBrowser.this.getWidth()-splitPane.getDividerLocation()-200) /(ICON_WIDTH+10);
            JPanel iconPanel=(JPanel) folderView.getViewport().getComponents()[0];
            iconPanel.setLayout(new GridLayout(0,x,0,10));            
        }
        
        /* emfanizei ta perixomena enos fakelou */
        private void display(DefaultMutableTreeNode curNode ) {
            JPanel iconPanel=new JPanel();
            if (! ((FileObject) curNode.getUserObject()).exists()) return;
            String folder=((FileObject) curNode.getUserObject()).getAbsolutePath();
            String searchStr=searchField.getText().trim();
            if (searchStr.isEmpty()) searchStr=null;
            Vector data=new Vector();
            try {
                addFolderToDisplay(searchStr,folder, curNode, data,iconPanel);
            } catch (IOException | DirectoryIteratorException ex) {
                JOptionPane.showMessageDialog(rootPane, "Error reading directory "+ex.getMessage(), "Reading...", JOptionPane.ERROR_MESSAGE);
            }
                    
            /* emfanisi periexomenwn se morfh listas */
            listTable=new Table(data,names,false);
            listTable.setAlignment(2,JLabel.CENTER);
            listTable.setAlignment(3,JLabel.RIGHT);
            listTable.sizeToFit();
                                    
            listTable.removeColumn(listTable.getColumnModel().getColumn(0));
            JScrollPane listPanel=new JScrollPane(listTable);
            
            listPanel.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {}

                @Override
                public void mousePressed(MouseEvent e) {}

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        listTable.clearSelection();
                        popupDir.setEnabled(true);
                        popupFile.setEnabled(true);
                        popupRename.setEnabled(false);
                        popupDelete.setEnabled(false);
                        popupZip.setEnabled(false);
                        popupmenu.show(e.getComponent(), e.getX(), e.getY());
                    }                        
                }

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            });
            
            listTable.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Table table=(Table) e.getSource();
                    if (table.getSelectedRows().length==1) {
                        itemNewDir.setEnabled(true);                        
                        itemNewFile.setEnabled(true);                        
                        itemRename.setEnabled(true);                        
                    } else {
                        itemNewDir.setEnabled(false);                        
                        itemNewFile.setEnabled(false);                        
                        itemRename.setEnabled(false);                        
                    }
                    if (fileSelection.size()==0) {
                        itemZip.setEnabled(false);
                    } else {
                        itemZip.setEnabled(true);                        
                    }
                    
                    if (e.getClickCount()==2) {
                        int[] cols=new int[LIST_NAMES.length];
                        int[] rowIndex=table.getSelectedRows();
                        cols[0]=0;
                        for(int i=1;i <cols.length; i++) {
                            cols[i]=table.convertColumnIndexToView(table.getColumn(LIST_NAMES[i]).getModelIndex());
                        }
                        for(int i=0; i<rowIndex.length; i++) {
                            String command="";
                            try {
                                int row=table.convertRowIndexToModel(rowIndex[i]);
                                File file=(File) table.getModel().getValueAt(row, cols[0]);
                                if (file.isFile()) {
                                    String filetype;
                                    filetype=(String) table.getModel().getValueAt(row, cols[2]);
                                    command=getProgramm(filetype);
                                    if (command==null) command=DEFAULT_EDITOR;
                                    command=command+" "+file.getAbsolutePath();
                                    Runtime.getRuntime().exec(command);
                                } else {
                                      changeDir(file);
                                }
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(rootPane, "Error executing "+command+".\n"+ex.getMessage(), "Opening....", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }                            
                
                @Override
                public void mousePressed(MouseEvent e) {}

                @Override
                public void mouseReleased(MouseEvent e) {
                    Table table=(Table) e.getSource();
                    if (table.getSelectedRows().length==1) {
                        itemNewDir.setEnabled(true);                        
                        itemNewFile.setEnabled(true);                        
                        itemRename.setEnabled(true);
                    } else {
                        itemNewDir.setEnabled(false);                        
                        itemNewFile.setEnabled(false);                        
                        itemRename.setEnabled(false);                        
                    }
                    if (fileSelection.size()==0) {
                        itemZip.setEnabled(false);
                    } else {
                        itemZip.setEnabled(true);                        
                    }
                    
                    /* emfanizei to popup menu */
                    int rowIndex = table.rowAtPoint(e.getPoint());
                    if (rowIndex >= 0) {
                      if (e.isPopupTrigger()) {
                            if (! table.isRowSelected(rowIndex)) {
                                table.clearSelection();
                                table.addRowSelectionInterval(rowIndex, rowIndex);
                            }
                            popupDelete.setEnabled(true);
                            popupZip.setEnabled(true);                            
                            if (table.getSelectedRows().length==1) {
                                popupDir.setEnabled(true);                        
                                popupFile.setEnabled(true);                        
                                popupRename.setEnabled(true);
                            } else {
                                popupDir.setEnabled(false);                        
                                popupFile.setEnabled(false);                        
                                popupRename.setEnabled(false);                        
                            }                            
                                                        
                            popupmenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }                                        
                }

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            });
            
            int x= (FileBrowser.this.getWidth()-splitPane.getDividerLocation()-200) /(ICON_WIDTH+10);
            iconPanel.setLayout(new GridLayout(0,x,0,10));
            
            if (currentView==LIST_VIEW) folderView.setViewportView(listPanel);
            else folderView.setViewportView(iconPanel);            
        }
        
        /* dhmiourgei ena label */
        JLabel createLabel(String str, String filetype, File file) {
            JLabel flabel = new JLabel(str, getIcon(filetype), JLabel.LEFT);
            flabel.setVerticalTextPosition(JLabel.BOTTOM);
            flabel.setHorizontalTextPosition(JLabel.CENTER);
            flabel.setHorizontalAlignment(JLabel.CENTER);
            flabel.setVerticalAlignment(JLabel.TOP);

            /* vazei ton mouse listener sta labels */
            flabel.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    /* an kanei click se ena label, to epilegei */
                    JLabel label=(JLabel) e.getSource();
                    boolean control=e.isControlDown();
                    label.setForeground(Color.red);
                    if (!control) {
                        for(JLabel lbl:labelSelection) {
                            if (lbl!=label) lbl.setForeground(Color.black);
                        }
                        fileSelection.clear();
                        labelSelection.clear();                    
                    }
                    if (!labelSelection.contains(label)) labelSelection.add(label);                    
                    if (!fileSelection.contains(file)) fileSelection.add(file);        
                    
                    if (fileSelection.size()==1) {
                        itemNewDir.setEnabled(true);                        
                        itemNewFile.setEnabled(true);                        
                        itemRename.setEnabled(true);
                    } else {
                        itemNewDir.setEnabled(false);                        
                        itemNewFile.setEnabled(false);                        
                        itemRename.setEnabled(false);                        
                    }
                    if (fileSelection.size()==0) {
                        itemZip.setEnabled(false);
                    } else {
                        itemZip.setEnabled(true);                        
                    }
                    
                    /* diplo klik, anoigei to arxeio h mpainei sto fakelo */
                    if (e.getClickCount()==2) {
                        String command="";
                        try {
                            if (file.isDirectory()) {
                                changeDir(file);
                            } else {                            
                                command=getProgramm(filetype);
                                if (command==null) command=DEFAULT_EDITOR;
                                command=command+" "+file.getAbsolutePath();
                                Runtime.getRuntime().exec(command);
                            }
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(rootPane, "Error executing "+command+".\n"+ex.getMessage(), "Opening....", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                @Override
                    public void mousePressed(MouseEvent e) {}

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        /* emfanizei to popup menu */
                        JLabel label=(JLabel) e.getSource();
                        if (e.isPopupTrigger()) { 
                            boolean control=e.isControlDown();
                            label.setForeground(Color.red);
                            if (!control) {
                                for(JLabel lbl:labelSelection) {
                                    if (lbl!=label) lbl.setForeground(Color.black);
                                }
                                fileSelection.clear();
                                labelSelection.clear();                    
                            }
                            if (!labelSelection.contains(label)) labelSelection.add(label);                    
                            if (!fileSelection.contains(file)) fileSelection.add(file);      
                            popupDelete.setEnabled(true);
                            popupZip.setEnabled(true);                            
                            if (fileSelection.size()==1) {
                                popupDir.setEnabled(true);                        
                                popupFile.setEnabled(true);                        
                                popupRename.setEnabled(true);
                            } else {
                                popupDir.setEnabled(false);                        
                                popupFile.setEnabled(false);                        
                                popupRename.setEnabled(false);                        
                            }                            
                                                        
                            popupmenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {}

                    @Override
                    public void mouseExited(MouseEvent e) {}
                });
            
            return flabel;
        }

        private Vector getDirVector(File dir) {
            /* dhmiourgoume ton vector me ta periexoma tou directory */
            Vector ol = new Vector();
            File[] tmp = dir.listFiles();
            if (tmp!=null) {
                for (int i = 0; i < tmp.length; i++) {                    
                    if (tmp[i].isDirectory()) {
                        ol.addElement(tmp[i]);
                    }
                }
            }                    
            Collections.sort(ol, null /*String.CASE_INSENSITIVE_ORDER*/); //* taxinomei ta periexomena twn fakelwn */            
            return ol;
        }

        private void updateNode(DefaultMutableTreeNode curNode) {
            curNode.removeAllChildren();
           
            FileObject fo=(FileObject) curNode.getUserObject();            
            Vector dirList = getDirVector(fo.getAbsoluteFile());
            for (int i = 0; i < dirList.size(); i++) {                            
                File f = (File) dirList.elementAt(i);
                fo=new FileObject(f);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(fo);
                Vector childList=getDirVector(f);
                for(int j=0; j<childList.size(); j++) {
                    f = (File) childList.elementAt(j);
                    fo=new FileObject(f);
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(fo);
                    node.add(childNode);
                }
                curNode.add(node);                
            }
            
        }
        
        private void addNodes(DefaultMutableTreeNode curDir, File dir) {
            Vector dirList = getDirVector(dir);
            for (int i = 0; i < dirList.size(); i++) {                            
                File f = (File) dirList.elementAt(i);
                FileObject fo=new FileObject(f);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(fo);
                Vector childList=getDirVector(f);
                for(int j=0; j<childList.size(); j++) {
                    f = (File) childList.elementAt(j);
                    fo=new FileObject(f);
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(fo);
                    node.add(childNode);
                }
                curDir.add(node);                
            }            
        }
        
        /* dhmioyrgei to dendro diavazontas tous fakelous kai prosthetwntas komvous*/
        DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, String folder) {
            File dir=new File(folder);
            String curPath = folder;
            String name=dir.getName();
            if (curTop==null) name=curPath;
            FileObject fo=new FileObject(name);
            DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(fo);

            if (curTop != null) { // should only be null at root
                curTop.add(curDir);
            }
            
            /* dhmiourgoume ton vector me ta periexoma tou directory */            
            addNodes(curDir, dir);
            
            return curDir;
        }    

        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            DefaultMutableTreeNode node=(DefaultMutableTreeNode) event.getPath().getLastPathComponent();
            Enumeration<DefaultMutableTreeNode> children=node.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode curDir=children.nextElement();
                if (curDir.isLeaf()) {
                    updateNode(curDir);   
                }
            }
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {}
    }
        
    public FileBrowser(String rootDir) {
        super("CE325 File Browser");        
        this.rootDir=rootDir;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width,height);
        setPreferredSize(new Dimension(width,height));
        this.setBackground(Color.red);
        addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                treeDir.resize();
            }

            @Override
            public void componentMoved(ComponentEvent e) {}

            @Override
            public void componentShown(ComponentEvent e) {}

            @Override
            public void componentHidden(ComponentEvent e) {}
        });        
        try {
            loadConfig();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(rootPane, "Error reading configuration.\n"+ex.getMessage(), "Reading configuration", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(rootPane, "Error reading configuration.\n"+ex.getMessage(), "Reading configuration", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(rootPane, "Error reading configuration.\n"+ex.getMessage(), "Reading configuration", JOptionPane.ERROR_MESSAGE);
        } 
                        
        createMenu();
        createPopupMenu();
        createIcons();
        treeDir=new TreeDir(this.rootDir);
        treeDir.dirTree.setSelectionRow(0);  // epilegei to root fakelo
        add(treeDir);
        setVisible(true);        
    }

    private void loadConfig() throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream in=new ObjectInputStream(new FileInputStream(CONFIG_FILENAME));
        fileTypes=(String[][]) in.readObject();
        in.close();
    }
    
    private void writeConfig() throws FileNotFoundException, IOException {
        ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(CONFIG_FILENAME));
        out.writeObject(fileTypes);
        out.close();
    }
    
    /* diavazei ta eikoneidia apo ton pinaka */
    private void createIcons() {
        if (fileTypes==null) return;
        int n=fileTypes.length;
        icons=new ImageIcon[n];
        for(int i=0; i<n; i++) {
            icons[i] = createImageIcon(fileTypes[i][1]);
        }
    }
    
    /* epistrefei ena eikoneidio apo ton pinaka eikoneidiwn analoga me ton typo
       toy arxeiou. xrhsimopoiei ton pinaka me toys typoys arxeiwn  */
    public ImageIcon getIcon(String filetype) {
        if (icons==null || fileTypes==null) return NULL_ICON;
        for(int i=0; i<fileTypes.length; i++) {
            if (filetype==null) {
                if (fileTypes[i][0]==null || fileTypes[i][0].trim().length()==0 || fileTypes[i][0].equalsIgnoreCase("dir")) return icons[i];
            } else {
                if (fileTypes[i][0].equals(filetype)) return icons[i];
            }
        }
        return NULL_ICON;
    }
    
    /* epistrefei ena eikoneidio apo ton pinaka eikoneidiwn analoga me ton typo
       toy arxeiou. xrhsimopoiei ton pinaka me toys typoys arxeiwn  */
    public String getProgramm(String filetype) {
        if (filetype==null) return null;
        for(int i=1; i<fileTypes.length; i++) {
            if (fileTypes[i][0].equals(filetype)) return fileTypes[i][2];
        }
        return null;
    }
    
    /* dhmioyrgei to orizontio menou */
    private void createMenu() {
        JMenuBar menuBar=new JMenuBar();
        JMenuItem item;
        
        JMenu menu=new JMenu("File");  
        itemNewDir=new JMenuItem("New Dir");
        itemNewDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNew(true);
            }
        });
        menu.add(itemNewDir);        
        
        itemNewFile=new JMenuItem("New File");
        itemNewFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNew(false);
            }
        });
        menu.add(itemNewFile);        
        
        itemRename=new JMenuItem("Rename");
        itemRename.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rename();
            }
        });
        menu.add(itemRename);        
        
        itemDelete=new JMenuItem("Delete");
        itemDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });
        menu.add(itemDelete);        
        
        itemZip = new JMenuItem("Make zip");
        itemZip.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createZip();
            }
        });
        itemZip.setEnabled(false);
        menu.add(itemZip);
        
        
        item=new JMenuItem("Exit");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(item);        
        
        menuBar.add(menu);
        
        menu=new JMenu("View");          
        item=new JMenuItem("Icons");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                currentView=ICON_VIEW;
                treeDir.display();
            }
        });
        menu.add(item);        
        item=new JMenuItem("List");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                currentView=LIST_VIEW;
                treeDir.display();
            }
        });
        menu.add(item);        
        menuBar.add(menu);
        
        menu=new JMenu("Help");  
        item=new JMenuItem("Configuration");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ConfDialog();
            }
        });
        menu.add(item);           
        
        
        item=new JMenuItem("About");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(rootPane, "File Browser created by \nDimitris Greasidis and\nStefanos Papanastasiou", "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(item);                
        menuBar.add(menu);
        
        menuBar.add(Box.createHorizontalStrut(400));
        searchField=new JTextField();
        searchField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                char c=e.getKeyChar();
                if (c=='\n') {
                    FileBrowser.this.treeDir.display();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        menuBar.add(searchField);
        
        setJMenuBar(menuBar);      
    }
    
    /* dhmiourgia popup menu */
    private void createPopupMenu() {
        popupmenu = new JPopupMenu("Popup");            
        
        popupDir = new JMenuItem("New Dir");
        popupDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createNew(true);
            }
        });        
        popupmenu.add(popupDir);
        
        popupFile = new JMenuItem("New File");
        popupFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createNew(false);
            }
        });        
        popupmenu.add(popupFile);
        
        popupRename = new JMenuItem("Rename");
        popupRename.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rename();
            }
        });        
        popupmenu.add(popupRename);

        popupDelete = new JMenuItem("Delete");
        popupDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });        
        popupmenu.add(popupDelete);        

        popupZip = new JMenuItem("Make zip");
        popupZip.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createZip();
            }
        });
        popupmenu.add(popupZip);
    }
        
    /* dhmiourgei neo directory h neo txt arxeio analoga me tin timh ths boolean dir */
    private void createNew(boolean dir) {
        String type1,type2;
        /* an exei eppilexei apo to dexio panel, tote pairnei to lastFile, alliws to lastFolder
           poy einai apo to dendro o fakelos poy exei epilexthei */
        File curDir=lastFolder;
        if (fileSelection.size()==1 && fileSelection.get(0).isDirectory() ) curDir=fileSelection.get(0);
        
        /* kathorismos string gia ta mhnymata */
        if (dir) {
            type1="directory";
            type2="Directory";
        } else {
            type1="file";
            type2="File";
        }
        String newname=JOptionPane.showInputDialog(rootPane, "Enter "+type1+" name", "Create new "+type1, JOptionPane.OK_CANCEL_OPTION);
        if (newname!=null) {
            String path=curDir.getAbsolutePath();
            newname=path + File.separator +newname; /* kathorizei to path */
            if(!dir) newname=newname+".txt";        /* an exei epilexei neo arxeio vazei thn katalixi .txt */
            File newFile=new File(newname);
            try {
                boolean retValue;
                if (dir) retValue=newFile.mkdir();
                else retValue=newFile.createNewFile();               
                if (retValue) updateTree(false); /* xanadiavazei to dendro */
                else JOptionPane.showMessageDialog(rootPane, "Error creating new "+type1+" "+newFile.getName()+". "+type2+" already exists.", "Create new "+type1, JOptionPane.ERROR_MESSAGE);
            } catch(SecurityException ex) { // sfalma dikaiwmatwn
                JOptionPane.showMessageDialog(rootPane, "Error creating new "+type1+" "+newFile.getName()+". Permission denied.", "Create new "+type1, JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) { // sfalma IO
                JOptionPane.showMessageDialog(rootPane, "Error creating new "+type1+" "+newFile.getName()+". "+ex.getMessage(), "Create new "+type1, JOptionPane.ERROR_MESSAGE);
            }
        }        
    }

    /* allzei onoma se arxeio h directory */
    private void rename() {
        File curFile=lastFolder;
        if (currentView==LIST_VIEW) {
            updateSelection();
        }                        
        if (fileSelection.size()==1) curFile=fileSelection.get(0);        
        
        String newname=JOptionPane.showInputDialog(rootPane, "Enter new name", "Renaming...", JOptionPane.OK_CANCEL_OPTION);
        if (newname!=null) {
            String path=curFile.getParent();
            newname=path + File.separator +newname;                
            File newFile=new File(newname);
            try {
                boolean retValue=curFile.renameTo(newFile);
                if (retValue) updateTree(false);
                else JOptionPane.showMessageDialog(rootPane, "Error renaming "+newFile.getName()+". It already exists.", "Renaming...", JOptionPane.ERROR_MESSAGE);
            } catch(SecurityException ex) { // sfalma dikaiwmatwn
                JOptionPane.showMessageDialog(rootPane, "Error renaming "+newFile.getName()+". Permission denied.", "Renaming...", JOptionPane.ERROR_MESSAGE);
            }
        }
    } 
              
    /* diagrafei ena arxeio h directory */
    private void delete() {
        boolean hasFiles=true;
        int retOption=JOptionPane.showConfirmDialog(rootPane, "Do you really want to delete selected files ?", "Deleting...", JOptionPane.YES_NO_OPTION);
        if (retOption==JOptionPane.YES_OPTION){
            if (currentView==LIST_VIEW) {
                updateSelection();
            }                        
            if (fileSelection.size()==0) {
                hasFiles=false;
                fileSelection.add(lastFolder);        
            }
            for(File curFile:fileSelection) {            
                try {
                    boolean retValue;
                    if (curFile.isFile()) retValue=curFile.delete();                
                    else retValue=dirDelete(curFile);
                    if (!retValue) JOptionPane.showMessageDialog(rootPane, "Error deleting "+curFile.getName()+".", "Deleting...", JOptionPane.ERROR_MESSAGE);
                    else updateTree(!hasFiles);
                } catch(SecurityException ex) {
                    JOptionPane.showMessageDialog(rootPane, "Error deleting "+curFile.getName()+". Permission denied.", "Deleting...", JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }
            if (!hasFiles) fileSelection.remove(lastFolder);
        }
    }
            
    /* anadromikh diagrafh enos directory, diagrafei oloys tous fakelous kai ta arxeia toys */
    private boolean dirDelete(File curDir) {
        boolean retValue;
        File[] files=curDir.listFiles();
        if (files!=null) {
            for(File file:files) {
                if (file.isFile()) {
                    retValue=file.delete();
                    if (retValue==false) return false;
                }
                else {
                    retValue=dirDelete(file);
                    if (retValue==false) return false;
                }
            }        
        }
        return curDir.delete();
    }

    private void createZip() {
        String zipFileName=lastFolder.getAbsoluteFile() + File.separator;
        if (fileSelection.size()==1) {
            String str=fileSelection.get(0).getName();
            int pos=str.lastIndexOf(".");
            if (pos!=-1) str=str.substring(0, pos);            
            zipFileName += str+".zip";
        }
        else {
            String newname=JOptionPane.showInputDialog(rootPane, "Enter name for zip file", "Creating zip...", JOptionPane.OK_CANCEL_OPTION);
            if (newname==null) return;            
            zipFileName += newname+".zip";
        }
        ZipFile zip=new ZipFile(fileSelection,zipFileName, lastFolder);    
        try {
            zip.zipFiles();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(rootPane, "Error creating zip "+ex.getMessage(), "Creating zip...", JOptionPane.ERROR_MESSAGE);
        }
        treeDir.display();
    }    
    
    void updateTree(boolean isCurrent) {
        Enumeration<TreePath> prevExpanded=treeDir.dirTree.getExpandedDescendants(treeDir.dirTree.getPathForRow(0));            
        DefaultMutableTreeNode node=(DefaultMutableTreeNode) treeDir.dirTree.getLastSelectedPathComponent();
        TreePath oldPath=treeDir.dirTree.getSelectionModel().getSelectionPath();
        if (isCurrent) node=(DefaultMutableTreeNode) node.getParent();
        treeDir.updateNode(node);        
        ((DefaultTreeModel) treeDir.dirTree.getModel()).reload();
        if (isCurrent) {
           TreeNode[] nodes = ((DefaultTreeModel) treeDir.dirTree.getModel()).getPathToRoot(node);
           oldPath = new TreePath(nodes);                    
        }
        treeDir.dirTree.getSelectionModel().setSelectionPath(oldPath);        
        while(prevExpanded.hasMoreElements()) {
            TreePath t=prevExpanded.nextElement();
            Object[] obj=t.getPath();
            for (Object o:obj) {
                TreePath tp=treeDir.dirTree.getNextMatch(o.toString(), 0, Position.Bias.Forward);
                int row=treeDir.dirTree.getRowForPath(tp);
                treeDir.dirTree.expandRow(row);
            }                
        }        
        
        treeDir.display();        
    }
    
    private void updateSelection() {
        int[] rowIndex=treeDir.listTable.getSelectedRows();
        int[] cols=new int[LIST_NAMES.length];
        cols[0]=0;
        for(int i=1;i <cols.length; i++) {
            cols[i]=treeDir.listTable.convertColumnIndexToView(treeDir.listTable.getColumn(LIST_NAMES[i]).getModelIndex());
        }
        if (rowIndex.length > 0) {
            fileSelection.clear();                   
            for(int i=0; i<rowIndex.length; i++) {
                int row=treeDir.listTable.convertRowIndexToModel(rowIndex[i]);                           
                /* an kanei click se ena label, to epilegei */                                                   
                File file=(File) treeDir.listTable.getModel().getValueAt(row, cols[0]);                        
                fileSelection.add(file);                                
            }    
        }        
    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = FileBrowser.class.getResource(IMAGE_DIR+"/"+path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return NULL_ICON;
        }
    }
    
    public static void main(String[] args) {
        new FileBrowser(System.getProperty("user.home"));
    }

}