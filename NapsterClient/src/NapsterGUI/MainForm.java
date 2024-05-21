/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package NapsterGUI;

/**
 *
 * @author HP
 */
import NapsterUtilities.*;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import org.apache.solr.client.solrj.SolrServerException;


public class MainForm extends javax.swing.JFrame {
    private ClientManager clientManager;
    private SolrClientManager solrClientManager;
    private FileServer fileServer;
    private FileClient fileClient;
    private String username;
    private List<FileMetaData> searchResults;
    private String[] columnNames = {"Filename", "Filesize", "User", "Download"};
    private Bytes[] downloadingBytes;
    private javax.swing.JProgressBar[] downloadProgressBars;
    private Bytes[] uploadingBytes;
    private FileMetaData[] uploadingFiles;
    private javax.swing.JProgressBar[] uploadProgressBars;
    
    private void manageDownloadsProgress() {
        long prevDownloadedBytes[] = new long[this.fileClient.getMaxParallelDownloads()];
        for (int i = 0; i < this.fileClient.getMaxParallelDownloads(); i++) {
            prevDownloadedBytes[i] = 0;
        }
        while (true) {
            for (int i = 0; i < this.fileClient.getMaxParallelDownloads(); i++) {
                if (this.downloadingBytes[i].transferring) {
                    this.downloadProgressBars[i].setValue((int)this.downloadingBytes[i].getPercentage());
                    this.downloadsTable.getModel().setValueAt(Bytes.getBytesStr(this.downloadingBytes[i].transferringBytes - prevDownloadedBytes[i]) + "/s", i, 3);
                    prevDownloadedBytes[i] = this.downloadingBytes[i].transferringBytes;
                    if (this.downloadProgressBars[i].getValue() == 100) {
                        prevDownloadedBytes[i] = 0;
                        this.downloadingBytes[i].transferring = false;
                        this.downloadProgressBars[i].setValue(0);
                        this.downloadsTable.getModel().setValueAt("", i, 0);
                        this.downloadsTable.getModel().setValueAt("", i, 1);
                        this.downloadsTable.getModel().setValueAt("", i, 2);
                        this.downloadsTable.getModel().setValueAt("", i, 3);
                    }
                }
            }
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    
    private void manageUploadsProgress() {
        boolean prevUploading[] = new boolean[this.fileServer.getMaxParallelUploads()];
        long prevUploadedBytes[] = new long[this.fileServer.getMaxParallelUploads()];
        for (int i = 0; i < this.fileServer.getMaxParallelUploads(); i++) {
            prevUploading[i] = false;
            prevUploadedBytes[i] = 0;
        }
        while (true) {
            for (int i = 0; i < this.fileServer.getMaxParallelUploads(); i++) {
                if (!prevUploading[i] && this.uploadingBytes[i].transferring) {
                    this.uploadsTable.getModel().setValueAt(this.uploadingFiles[i].getFilename(), i, 0);
                    this.uploadsTable.getModel().setValueAt(this.uploadingFiles[i].getFilesizeStr(), i, 1);
                    this.uploadsTable.getModel().setValueAt(this.uploadingFiles[i].getUserIPAddress(), i, 2);
                    prevUploading[i] = true;
                }
                else if (prevUploading[i] && !this.uploadingBytes[i].transferring) {
                    this.uploadProgressBars[i].setValue(0);
                    this.uploadsTable.getModel().setValueAt("", i, 0);
                    this.uploadsTable.getModel().setValueAt("", i, 1);
                    this.uploadsTable.getModel().setValueAt("", i, 2);
                    this.uploadsTable.getModel().setValueAt("", i, 3);
                    prevUploading[i] = false;
                }
                else if (this.uploadingBytes[i].transferring) {
                    this.uploadProgressBars[i].setValue((int)this.uploadingBytes[i].getPercentage());
                    this.uploadsTable.getModel().setValueAt(Bytes.getBytesStr(this.uploadingBytes[i].transferringBytes - prevUploadedBytes[i]) + "/s", i, 3);
                    prevUploadedBytes[i] = this.uploadingBytes[i].transferringBytes;
                }
            }
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
    
    
    /**
     * Creates new form MainForm
     */
    public MainForm() {
        initComponents();
        this.downloadingBytes = new Bytes[10];
        for (int i = 0; i < this.downloadingBytes.length; i++) {
            this.downloadingBytes[i] = new Bytes();
            this.downloadingBytes[i].transferring = false;
        }       
        
        try {
            this.solrClientManager = new SolrClientManager();
            this.fileClient = new FileClient();
            this.clientManager = new ClientManager();
            this.fileServer = new FileServer();
            this.fileServer.start();
            this.username = this.clientManager.readUsername();
            try {
                this.solrClientManager.updateUserStatus(this.username, this.clientManager.getIPAddress(), true);
            }
            catch (Exception e) {
                System.out.println(e.toString());
                this.solrClientManager.changeSolrClient();
                this.solrClientManager.updateUserStatus(this.username, this.clientManager.getIPAddress(), true);
            }
            this.usernameLabel.setText(this.username);
            
            this.downloadProgressBars = new javax.swing.JProgressBar[this.fileClient.getMaxParallelDownloads()];
            this.downloadProgressBars[0] = this.progressBar1;
            this.downloadProgressBars[1] = this.progressBar2;
            this.downloadProgressBars[2] = this.progressBar3;
            this.downloadProgressBars[3] = this.progressBar4; 
            this.downloadProgressBars[4] = this.progressBar5;

            this.uploadProgressBars = new javax.swing.JProgressBar[this.fileServer.getMaxParallelUploads()];
            this.uploadProgressBars[0] = this.progressBar6;
            this.uploadProgressBars[1] = this.progressBar7;
            this.uploadProgressBars[2] = this.progressBar8;
            this.uploadProgressBars[3] = this.progressBar9; 
            this.uploadProgressBars[4] = this.progressBar10;
            
            Thread downloadingProgressThread = new Thread(new Runnable() {
                public void run() {
                    manageDownloadsProgress();
                }
            });
            downloadingProgressThread.setDaemon(true);
            downloadingProgressThread.start();
            
            this.uploadingBytes = this.fileServer.uploadingBytes;
            this.uploadingFiles = this.fileServer.uploadingFiles;
            Thread uploadingProgressThread = new Thread(new Runnable() {
                public void run() {
                    manageUploadsProgress();
                }
            });
            uploadingProgressThread.setDaemon(true);
            uploadingProgressThread.start();
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e.toString());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        leaveButton = new javax.swing.JButton();
        usernameLabel1 = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();
        searchPanel = new javax.swing.JPanel();
        searchTextField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        searchTable = new javax.swing.JTable();
        downloadsPanel = new javax.swing.JPanel();
        progressBar2 = new javax.swing.JProgressBar();
        jScrollPane2 = new javax.swing.JScrollPane();
        downloadsTable = new javax.swing.JTable();
        progressBar1 = new javax.swing.JProgressBar();
        progressBar4 = new javax.swing.JProgressBar();
        progressBar5 = new javax.swing.JProgressBar();
        progressBar3 = new javax.swing.JProgressBar();
        uploadsPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        uploadsTable = new javax.swing.JTable();
        progressBar6 = new javax.swing.JProgressBar();
        progressBar7 = new javax.swing.JProgressBar();
        progressBar8 = new javax.swing.JProgressBar();
        progressBar9 = new javax.swing.JProgressBar();
        progressBar10 = new javax.swing.JProgressBar();
        syncButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Napster");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        leaveButton.setBackground(new java.awt.Color(255, 51, 51));
        leaveButton.setText("Leave");
        leaveButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        leaveButton.setPreferredSize(new java.awt.Dimension(70, 25));
        leaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leaveButtonActionPerformed(evt);
            }
        });

        usernameLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        usernameLabel1.setText("Username:");
        usernameLabel1.setPreferredSize(new java.awt.Dimension(70, 25));

        usernameLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        usernameLabel.setPreferredSize(new java.awt.Dimension(200, 25));

        tabbedPane.setToolTipText("");

        searchTextField.setPreferredSize(new java.awt.Dimension(300, 30));

        searchButton.setText("Search");
        searchButton.setPreferredSize(new java.awt.Dimension(70, 30));
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        searchTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Filename", "Filesize", "User", "Download"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        searchTable.setEnabled(false);
        searchTable.setRowHeight(40);
        searchTable.setShowGrid(true);
        searchTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                searchTableMouseMoved(evt);
            }
        });
        searchTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchTableMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchTableMouseExited(evt);
            }
        });
        jScrollPane1.setViewportView(searchTable);

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 741, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Search", searchPanel);

        progressBar2.setPreferredSize(new java.awt.Dimension(150, 30));

        downloadsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Filename", "Filesize", "User", "Speed"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        downloadsTable.setRowHeight(40);
        downloadsTable.setShowGrid(true);
        jScrollPane2.setViewportView(downloadsTable);

        progressBar1.setPreferredSize(new java.awt.Dimension(150, 30));

        progressBar4.setPreferredSize(new java.awt.Dimension(150, 30));

        progressBar5.setPreferredSize(new java.awt.Dimension(150, 30));

        progressBar3.setPreferredSize(new java.awt.Dimension(150, 30));

        javax.swing.GroupLayout downloadsPanelLayout = new javax.swing.GroupLayout(downloadsPanel);
        downloadsPanel.setLayout(downloadsPanelLayout);
        downloadsPanelLayout.setHorizontalGroup(
            downloadsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, downloadsPanelLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(downloadsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressBar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressBar4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressBar5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );
        downloadsPanelLayout.setVerticalGroup(
            downloadsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(downloadsPanelLayout.createSequentialGroup()
                .addGroup(downloadsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(downloadsPanelLayout.createSequentialGroup()
                        .addGap(91, 91, 91)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(downloadsPanelLayout.createSequentialGroup()
                        .addGap(123, 123, 123)
                        .addComponent(progressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(progressBar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(progressBar4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(197, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Downloads", downloadsPanel);

        uploadsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Filename", "Filesize", "User IP Address", "Speed"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        uploadsTable.setRowHeight(40);
        uploadsTable.setShowGrid(true);
        jScrollPane3.setViewportView(uploadsTable);

        progressBar6.setPreferredSize(new java.awt.Dimension(150, 30));

        progressBar7.setPreferredSize(new java.awt.Dimension(150, 30));

        progressBar8.setPreferredSize(new java.awt.Dimension(150, 30));

        progressBar9.setPreferredSize(new java.awt.Dimension(150, 30));

        progressBar10.setPreferredSize(new java.awt.Dimension(150, 30));

        javax.swing.GroupLayout uploadsPanelLayout = new javax.swing.GroupLayout(uploadsPanel);
        uploadsPanel.setLayout(uploadsPanelLayout);
        uploadsPanelLayout.setHorizontalGroup(
            uploadsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, uploadsPanelLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(uploadsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressBar7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressBar8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressBar9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressBar10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );
        uploadsPanelLayout.setVerticalGroup(
            uploadsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(uploadsPanelLayout.createSequentialGroup()
                .addGroup(uploadsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(uploadsPanelLayout.createSequentialGroup()
                        .addGap(91, 91, 91)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(uploadsPanelLayout.createSequentialGroup()
                        .addGap(123, 123, 123)
                        .addComponent(progressBar6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(progressBar8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(progressBar9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(197, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Uploads", uploadsPanel);

        syncButton.setText("Sync");
        syncButton.setPreferredSize(new java.awt.Dimension(70, 25));
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(usernameLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(usernameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(syncButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(leaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(leaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(syncButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(usernameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(tabbedPane))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void leaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leaveButtonActionPerformed
        int response = JOptionPane.showConfirmDialog(rootPane, "Do you really want to leave?", "Leave Confirmation", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.NO_OPTION) {
            return;
        }
        try {
            this.fileServer.stop();
            try {
                this.solrClientManager.removeUser(this.username);
            }
            catch (SolrServerException e) {
                if (e.getCause().getMessage().contains("Connection refused")) {
                    System.out.println(e.toString());
                    this.solrClientManager.changeSolrClient();
                    this.solrClientManager.removeUser(this.username);
                }
            }
            this.clientManager.deleteFiles();
            this.dispose();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new JoinForm().setVisible(true);
                }
            });
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e.toString());
        }
    }//GEN-LAST:event_leaveButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            try {
                this.solrClientManager.updateUserStatus(this.username, this.clientManager.getIPAddress(), false);
            }
            catch (SolrServerException e) {
                System.out.println(e.toString());
                this.solrClientManager.changeSolrClient();
                this.solrClientManager.updateUserStatus(this.username, this.clientManager.getIPAddress(), false);
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e.toString());
        }
    }//GEN-LAST:event_formWindowClosing

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        try {
            try {
                this.searchResults = this.solrClientManager.searchFiles(this.searchTextField.getText());
            }
            catch (Exception e) {
                System.out.println(e.toString());
                this.solrClientManager.changeSolrClient();
                this.searchResults = this.solrClientManager.searchFiles(this.searchTextField.getText());
            }
            DefaultTableModel tableModel = new DefaultTableModel(this.columnNames, 0);            
            for (int i = 0; i < this.searchResults.size(); i++) {
                String username = this.searchResults.get(i).getUsername();
                String filename = this.searchResults.get(i).getFilename();
                String filesize = this.searchResults.get(i).getFilesizeStr();
                tableModel.insertRow(i, new Object[] {filename, filesize, username, "Download"});
            }
            this.searchTable.setModel(tableModel);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e.toString());
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    private void syncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncButtonActionPerformed
        try {
            this.clientManager.writeFileRecords();
            List<FileMetaData> publishedFiles;
            try {
                publishedFiles = this.solrClientManager.getPublishedFiles(this.username);
            }
            catch (Exception e) {
                System.out.println(e.toString());
                this.solrClientManager.changeSolrClient();
                publishedFiles = this.solrClientManager.getPublishedFiles(this.username);
            }
            List<UpdateOperation> updateOperations = this.clientManager.getUpdateOperations(publishedFiles);

            for (UpdateOperation op : updateOperations) {
                if (op.operation.equals(UpdateOperation.publish)) {
                    try {
                        this.solrClientManager.publishFile(op.fileMetaData.getFilename(), op.fileMetaData.getFilesize(), this.username);
                    }
                    catch (Exception e) {
                        System.out.println(e.toString());
                        this.solrClientManager.changeSolrClient();
                        this.solrClientManager.publishFile(op.fileMetaData.getFilename(), op.fileMetaData.getFilesize(), this.username);
                    }
                }
                else if (op.operation.equals(UpdateOperation.remove)) {
                    try {
                        this.solrClientManager.removeFile(op.fileMetaData.getFilename(), this.username);
                    }
                    catch (Exception e) {
                        System.out.println(e.toString());
                        this.solrClientManager.changeSolrClient();
                        this.solrClientManager.removeFile(op.fileMetaData.getFilename(), this.username);
                    }
                }
            }
            JOptionPane.showMessageDialog(rootPane, "Files are synced successfully.");
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e.toString());
        }
    }//GEN-LAST:event_syncButtonActionPerformed

    private void searchTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchTableMouseClicked
        int row = this.searchTable.rowAtPoint(evt.getPoint());
        int col = this.searchTable.columnAtPoint(evt.getPoint());
        if (col == 3) {            
            String filename = this.searchResults.get(row).getFilename();
            long filesize = this.searchResults.get(row).getFilesize();
            String username = this.searchResults.get(row).getUsername();
            String ipAddr = this.searchResults.get(row).getUserIPAddress();
            try {
                int i;
                for (i = 0; i < this.fileClient.getMaxParallelDownloads(); i++) {
                    if (!this.downloadingBytes[i].transferring) {
                        break;
                    }
                }
                if (i < this.fileClient.getMaxParallelDownloads()) {
                    this.downloadsTable.getModel().setValueAt(filename, i, 0);
                    this.downloadsTable.getModel().setValueAt(Bytes.getBytesStr(filesize), i, 1);
                    this.downloadsTable.getModel().setValueAt(username, i, 2);
                    this.downloadingBytes[i].transferring = true;
                    this.downloadingBytes[i].totalBytes = filesize;
                    this.fileClient.downloadFileAsync(filename, filesize, ipAddr, this.downloadingBytes[i]);
                }
                else {
                    throw new Exception("You cannot download more than " + this.fileClient.getMaxParallelDownloads() + " files simultaneously.");
                }
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e.toString());
            }
        }
    }//GEN-LAST:event_searchTableMouseClicked

    private void searchTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchTableMouseMoved
        java.awt.Point p = evt.getPoint();
        int hoveredRow = searchTable.rowAtPoint(p);
        int hoveredColumn = searchTable.columnAtPoint(p);
        if (hoveredColumn == 3) {
            searchTable.setRowSelectionAllowed(true);
            searchTable.setColumnSelectionAllowed(true);
//            searchTable.setSelectionBackground(Color.BLUE);
            searchTable.setRowSelectionInterval(hoveredRow, hoveredRow);
            searchTable.setColumnSelectionInterval(hoveredColumn, hoveredColumn);
            searchTable.repaint();
        }
        else {
            searchTableMouseExited(evt);
        }
    }//GEN-LAST:event_searchTableMouseMoved

    private void searchTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchTableMouseExited
        searchTable.setRowSelectionAllowed(false);
        searchTable.setColumnSelectionAllowed(false);
        searchTable.repaint();
    }//GEN-LAST:event_searchTableMouseExited

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        try {
            ClientManager clientManager = new ClientManager();
            boolean joined = !clientManager.readUsername().isEmpty();
            if (joined) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        new MainForm().setVisible(true);
                    }
                });
            }
            else {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        new JoinForm().setVisible(true);
                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel downloadsPanel;
    private javax.swing.JTable downloadsTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton leaveButton;
    private javax.swing.JProgressBar progressBar1;
    private javax.swing.JProgressBar progressBar10;
    private javax.swing.JProgressBar progressBar2;
    private javax.swing.JProgressBar progressBar3;
    private javax.swing.JProgressBar progressBar4;
    private javax.swing.JProgressBar progressBar5;
    private javax.swing.JProgressBar progressBar6;
    private javax.swing.JProgressBar progressBar7;
    private javax.swing.JProgressBar progressBar8;
    private javax.swing.JProgressBar progressBar9;
    private javax.swing.JButton searchButton;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTable searchTable;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JButton syncButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel uploadsPanel;
    private javax.swing.JTable uploadsTable;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JLabel usernameLabel1;
    // End of variables declaration//GEN-END:variables
}
