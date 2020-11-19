/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment;

import deployment.Deployment_Classes.Deployment;
import General.Functions;
import General.Options;
import General.Routines;
import deployment.Deployment_Classes.SaveRightsThread;
import deployment.Deployment_Classes.SaveDeployment;
import deployment.Deployment_Classes.Package;
import deployment.Deployment_Classes.Deployment.Block;
import deployment.Deployment_Classes.Deployment.LineRenderer;
import deployment.Deployment_Classes.Deployment.LineWrapper;
import deployment.Deployment_Classes.Deployment.LineWrapperItem;
import static General.Options.optionsFile;
import deployment.Deployment_Classes.SaveDeployment.DeploymentMeta;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;



/**
 *
 * @author clieske
 * 
 * @id:1, @date: 2019-01-18-JAN, @author: Christoph Lieske, @description: extend creating buildfile to retrieve a specific package
 */
public class deprecated_Start extends javax.swing.JFrame {
    
    public static String buildProperties;
    
    public static final String typeClass = "Class";
    public static final String typeObject = "Object";
    public static final String typePage = "Page";
    
    public static final File sourceDir = new File("H:\\2018-06-23-June-googleDrive\\Alle Dateien\\persoenlich\\geschaeftlich\\Eigenes Unternehmen\\Aktenschrank\\0-I\\E-I\\I,i\\interne Projekte_Code\\2018-11-06-Code-Projekte\\Deployment\\Target");
    public static File backup = new File("H:\\2018-06-23-June-googleDrive\\Alle Dateien\\persoenlich\\geschaeftlich\\Eigenes Unternehmen\\Backup\\");
    public static File saveFolder = new File("H:\\2018-06-23-June-googleDrive\\Alle Dateien\\persoenlich\\geschaeftlich\\Eigenes Unternehmen\\Backup\\save\\");
    
    
    public static File commandFolder = new File(System.getProperty("user.dir") + "\\Deployments-preparation\\");
    
    public static final Integer tableOldNumber = 0;
    public static final Integer tableNewNumber = 1;
    public static final Integer tableCreatedNumber = 2;
    
    private Boolean didInit = false;
    
    public Deployment dep;
    
    private File buildFileRetrieveQA;
    private File packageFile;
    private File commandFileRetrieveQA;
    
    public static Options opts;
    private SaveDeployment saveDep;
    
    public static File antFile;
    public static String projectName;
     
    /**
     * Creates new form Start
     */
    public deprecated_Start() throws IOException, FileNotFoundException, ClassNotFoundException {
        initComponents();
        this.init();
    }
    
    static GraphicsDevice device = GraphicsEnvironment
        .getLocalGraphicsEnvironment().getScreenDevices()[0];
    
    private void init() throws IOException, FileNotFoundException, ClassNotFoundException{
        
        Functions func = new Functions();
        Functions.debugSwitchOn("Functions.findFile");
        Functions.debugSwitchOff("Functions.findFile");
        
        antFile = func.findFile("ant-salesforce.jar");
        
        Functions.debugSwitchOn("FileFound");
        Functions.debug("FileFound", "file: " + antFile);
        
        //System.out.println("##commandFolder before loading: " + Start.commandFolder);
        this.initTable(tableOld);
        this.initTable(tableNew);
        this.initTable(tableCreated);
        
        //System.out.println("##commandFolder before loading: " + Start.commandFolder);
        this.opts = Options.loadOptions();
        
        if(this.opts != null){
            txtMetadataName.setText(this.opts.actualMetaName);
            txtTypeName.setText(this.opts.actualTypeName);
            txtTarget.setText(this.opts.actualTarget);
            txtSource.setText(this.opts.actualSource);
            txtDataEnding.setText(this.opts.actualEnding);
            txtDirectoryOld.setText(this.opts.actualFolder);
            txtSubname.setText(this.opts.subname);
            txtProjectFolder.setText(this.opts.projectFolder);
            txtProjectName.setText(this.opts.projectName);
            txtRetrievePackage.setText(this.opts.packageName);
            deprecated_Start.projectName = txtProjectName.getText();
            cmbMetaData.setSelectedItem(this.opts.chosenType);
            txtBuildProperties.setText(this.opts.buildPropertiesPath);
            txtXmlImport.setText(this.opts.xmlImportPath);
            
            txtRetrieveFolderName.setText(this.opts.retrieveFolderName);
            comboTestLevel.setSelectedItem(this.opts.testLevel);
        }else{
            this.opts = new Options();
            this.saveOptions();
        }
        
        this.setFolders();
        
        try {
            this.saveDep = SaveDeployment.load(lstTestKlassen, tableDeployment);
        } catch (IOException ex) {
        } catch (ClassNotFoundException ex) {
        }
        
        if(this.saveDep == null){
            this.saveDep = new SaveDeployment(lstTestKlassen, tableDeployment);
        }
        
        cmdMetaDataName.removeAllItems();
        
        for( DeploymentMeta dm:this.saveDep.deploymentMeta){
            cmdMetaDataName.addItem(dm.metaName);
        }
        
        tableOld.setName("old");
        tableNew.setName("new");
        tableCreated.setName("created");
        
        device.setFullScreenWindow(this);
        
        didInit = true;
    }
    
    private void saveOptions() throws IOException{
        if(this.opts == null)this.opts = new Options();
             
        this.opts.actualMetaName = txtMetadataName.getText();
        this.opts.actualTypeName = txtTypeName.getText();
        this.opts.actualTarget = txtTarget.getText();
        this.opts.actualSource = txtSource.getText();
        this.opts.actualEnding = txtDataEnding.getText();
        this.opts.actualFolder = txtDirectoryOld.getText();
        this.opts.buildPropertiesPath = txtBuildProperties.getText();
        this.opts.packageName = txtRetrievePackage.getText();
        this.opts.projectFolder = txtProjectFolder.getText();
        this.opts.projectName = txtProjectName.getText();
        this.opts.retrieveFolderName = txtRetrieveFolderName.getText();
        this.opts.subname = txtSubname.getText();
        this.opts.testLevel = comboTestLevel.getSelectedItem() + "";
        this.opts.xmlImportPath = txtXmlImport.getText();
        
        this.opts.saveOptions();
    }
    
    private void initTable(
            JTable table
    ){
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel tColMod = table.getColumnModel();
        
        for(Integer i=0;i<tColMod.getColumnCount();i++){
            TableColumn col = tColMod.getColumn(i);
            String headerVal = col.getHeaderValue() + ""; 
            col.setPreferredWidth(headerVal.length()*8);
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

        jDialog1 = new javax.swing.JDialog();
        saveButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jPanel9 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelSystemInformation = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel11 = new javax.swing.JPanel();
        spinMinValChanged = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jChkAusblenden = new javax.swing.JCheckBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        chkRemoved = new javax.swing.JCheckBox();
        chkChanged = new javax.swing.JCheckBox();
        chkExisting = new javax.swing.JCheckBox();
        chkMoved = new javax.swing.JCheckBox();
        jLabel14 = new javax.swing.JLabel();
        cmbMetaData = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        cmdMetaDataName = new javax.swing.JComboBox<>();
        jTabbedPane5 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        btnXmlImport = new javax.swing.JToggleButton();
        btnBackupXML = new javax.swing.JToggleButton();
        cmdRetrievePackage = new javax.swing.JButton();
        btnBackup = new javax.swing.JButton();
        btnRetrieve = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnRetSFDX = new javax.swing.JButton();
        btnRetBackupSFDX = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        btnRetrieveSFDX = new javax.swing.JButton();
        jLabel28 = new javax.swing.JLabel();
        jButton15 = new javax.swing.JButton();
        jTabbedPane7 = new javax.swing.JTabbedPane();
        jPanel15 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        txtDataEnding = new javax.swing.JTextField();
        btnUserRegister = new javax.swing.JButton();
        chkPROD = new javax.swing.JCheckBox();
        jLabel27 = new javax.swing.JLabel();
        txtLoginURL = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtDirectoryOld = new javax.swing.JTextField();
        btnUserNamesList = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtSource = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtTarget = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtTypeName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtMetadataName = new javax.swing.JTextField();
        txtSubname = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        textFieldZiel_2 = new javax.swing.JTextField();
        jPanel16 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        txtTargetData = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        txtProjectFolder = new javax.swing.JTextField();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel21 = new javax.swing.JLabel();
        txtProjectName = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        txtBuildProperties = new javax.swing.JTextField();
        lblXmlImport = new javax.swing.JLabel();
        txtXmlImport = new javax.swing.JTextField();
        lblPackage = new javax.swing.JLabel();
        txtRetrievePackage = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtTestklasse = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        lstTestKlassen = new javax.swing.JList<>();
        jButton9 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jButton11 = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        tableDeployment = new javax.swing.JTable();
        txtRecentVal = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        comboTestLevel = new javax.swing.JComboBox<>();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        txtRetrieveFolderName = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        txtDeployTo = new javax.swing.JTextField();
        jTabbedPane6 = new javax.swing.JTabbedPane();
        jPanel12 = new javax.swing.JPanel();
        jButton10 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        btnDeploy = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        btnValDepSFDX = new javax.swing.JButton();
        jLabel29 = new javax.swing.JLabel();
        btnValDepSFDX1 = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        btn_DEPLOY = new javax.swing.JButton();
        btnBackupANDDeploy = new javax.swing.JButton();
        jLabel34 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        txtUserName = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        txtObjName = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        txtAreaFieldNames = new javax.swing.JTextArea();
        jLabel32 = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        txtAreaOutputFIeldsPackage = new javax.swing.JTextArea();
        btnFnamesPackage = new javax.swing.JButton();
        chkZwischenspeicher = new javax.swing.JCheckBox();
        jPanelDokumente = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableNew = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblBlocks = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableOld = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        spinVon = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        spinBis = new javax.swing.JSpinner();
        btnEinbeziehen = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        spinMoveToLine = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        jPanelResultat = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableCreated = new javax.swing.JTable();
        loadDocuments = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtOutput = new javax.swing.JTextArea();
        loadButton = new javax.swing.JButton();
        progressSave = new javax.swing.JProgressBar();
        jButton1 = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Salesforce Paketverwaltung");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        saveButton.setText("Speichern");
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jButton2.setText("Output zusammensetzen");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTabbedPane1.setAutoscrolls(true);

        spinMinValChanged.setValue(2);

        jLabel9.setText("Mindestlänge");

        jChkAusblenden.setSelected(true);
        jChkAusblenden.setText("andere Blöcke ausblenden (removed, changed)");

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("Code-Blöcke ordnen (Variablen,Methoden)");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jChkAusblenden, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spinMinValChanged, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinMinValChanged, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jChkAusblenden)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addContainerGap(331, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Changed-Optionen", jPanel11);

        chkRemoved.setText("removed entfernen");
        chkRemoved.setEnabled(false);

        chkChanged.setText("changed entfernen");
        chkChanged.setEnabled(false);

        chkExisting.setText("existing entfernen");
        chkExisting.setEnabled(false);

        chkMoved.setText("moved entfernen");
        chkMoved.setEnabled(false);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(chkRemoved)
                        .addGap(18, 18, 18)
                        .addComponent(chkMoved))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(chkChanged)
                        .addGap(18, 18, 18)
                        .addComponent(chkExisting)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkRemoved)
                    .addComponent(chkMoved))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkChanged)
                    .addComponent(chkExisting))
                .addContainerGap(343, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Source-Optionen", jPanel8);

        jLabel14.setText("Meta Data:");

        cmbMetaData.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AuraBundle-app", "AuraBundle-controller", "AuraBundle-cmp", "AuraBundle-css", "AuraBundle-doc", "AuraBundle-helper", "AuraBundle-renderer", "AuraBundle-svg", "Class", "Custom-Metadata-Type", "Object", "Page" }));
        cmbMetaData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbMetaDataActionPerformed(evt);
            }
        });

        jLabel15.setText("Meta Data Type:");

        cmdMetaDataName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdMetaDataNameActionPerformed(evt);
            }
        });

        btnXmlImport.setText("Import XML");
        btnXmlImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXmlImportActionPerformed(evt);
            }
        });

        btnBackupXML.setText("Backup XML");
        btnBackupXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackupXMLActionPerformed(evt);
            }
        });

        cmdRetrievePackage.setText("Retrieve Package");
        cmdRetrievePackage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRetrievePackageActionPerformed(evt);
            }
        });

        btnBackup.setText("Backup Package");
        btnBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackupActionPerformed(evt);
            }
        });

        btnRetrieve.setText("Retrieve XML");
        btnRetrieve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRetrieveActionPerformed(evt);
            }
        });

        jButton14.setText("Retrieve Changeset/Package");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmdRetrievePackage)
                    .addComponent(btnXmlImport))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBackup)
                    .addComponent(btnBackupXML))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 149, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton14)
                    .addComponent(btnRetrieve))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnXmlImport)
                    .addComponent(btnBackupXML)
                    .addComponent(btnRetrieve))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmdRetrievePackage)
                    .addComponent(btnBackup)
                    .addComponent(jButton14)))
        );

        jTabbedPane5.addTab("ant-Tools", jPanel2);

        btnRetSFDX.setText("1. Retrieve Package (sfdx)");
        btnRetSFDX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRetSFDXActionPerformed(evt);
            }
        });

        btnRetBackupSFDX.setText("3. Backup Package (sfdx)");
        btnRetBackupSFDX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRetBackupSFDXActionPerformed(evt);
            }
        });

        jButton16.setText("2. unzip Package (sfdx)");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });

        btnRetrieveSFDX.setText("Retrieve XML (sfdx)");
        btnRetrieveSFDX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRetrieveSFDXActionPerformed(evt);
            }
        });

        jLabel28.setText("4. Deploy Package (siehe oben)");

        jButton15.setText("4. Delete User Permissions");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton16)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnRetSFDX)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel28)))
                .addGap(132, 132, 132)
                .addComponent(btnRetrieveSFDX)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(btnRetBackupSFDX)
                .addGap(169, 169, 169)
                .addComponent(jButton15)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRetSFDX)
                    .addComponent(btnRetrieveSFDX)
                    .addComponent(jLabel28))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRetBackupSFDX)
                    .addComponent(jButton15))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jTabbedPane5.addTab("sfdx-Tools", jPanel3);

        jLabel7.setText("Datei-Endung:");

        txtDataEnding.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txtDataEndingPropertyChange(evt);
            }
        });
        txtDataEnding.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtDataEndingKeyPressed(evt);
            }
        });

        btnUserRegister.setText("User registrieren");
        btnUserRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUserRegisterActionPerformed(evt);
            }
        });

        chkPROD.setText("register in LIVE");

        jLabel27.setText("Login URL");

        jLabel17.setText("aktuelle vollständige Dateien des Zielsystems:");

        txtDirectoryOld.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txtDirectoryOldPropertyChange(evt);
            }
        });
        txtDirectoryOld.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtDirectoryOldKeyPressed(evt);
            }
        });

        btnUserNamesList.setText("see All User Names");
        btnUserNamesList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUserNamesListActionPerformed(evt);
            }
        });

        jLabel1.setText("Name Quelle:");

        txtSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSourceActionPerformed(evt);
            }
        });
        txtSource.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txtSourcePropertyChange(evt);
            }
        });
        txtSource.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtSourceKeyPressed(evt);
            }
        });

        jLabel2.setText("Name Ziel:");

        txtTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTargetActionPerformed(evt);
            }
        });
        txtTarget.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txtTargetPropertyChange(evt);
            }
        });
        txtTarget.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtTargetKeyPressed(evt);
            }
        });

        jLabel3.setText("Typname:");

        txtTypeName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTypeNameActionPerformed(evt);
            }
        });
        txtTypeName.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txtTypeNamePropertyChange(evt);
            }
        });
        txtTypeName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtTypeNameKeyPressed(evt);
            }
        });

        jLabel4.setText("metaName:");

        txtMetadataName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMetadataNameActionPerformed(evt);
            }
        });
        txtMetadataName.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txtMetadataNamePropertyChange(evt);
            }
        });
        txtMetadataName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtMetadataNameKeyPressed(evt);
            }
        });

        txtSubname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSubnameActionPerformed(evt);
            }
        });

        jLabel16.setText("sub Name:");

        jLabel33.setText("Name Ziel 2:");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtDirectoryOld, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel15Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel15Layout.createSequentialGroup()
                                        .addComponent(btnUserRegister)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(chkPROD))
                                    .addGroup(jPanel15Layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabel27)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtLoginURL, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel15Layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDataEnding, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnUserNamesList)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel15Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(txtTarget, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel33)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldZiel_2, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE))
                            .addGroup(jPanel15Layout.createSequentialGroup()
                                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel15Layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtSource, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(jPanel15Layout.createSequentialGroup()
                                            .addComponent(jLabel3)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(txtTypeName, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel15Layout.createSequentialGroup()
                                            .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                                                    .addComponent(jLabel4)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                                .addGroup(jPanel15Layout.createSequentialGroup()
                                                    .addComponent(jLabel16)
                                                    .addGap(14, 14, 14)))
                                            .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtMetadataName)
                                                .addComponent(txtSubname, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGap(4, 4, 4))))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtDataEnding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnUserNamesList))
                        .addGap(4, 4, 4)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnUserRegister)
                            .addComponent(chkPROD))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel27)
                            .addComponent(txtLoginURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(32, 32, 32))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel33)
                            .addComponent(textFieldZiel_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtTypeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtMetadataName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(txtSubname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDirectoryOld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addGap(32, 32, 32))
        );

        jTabbedPane7.addTab("Basisfunktionen", jPanel15);

        jLabel6.setText("Ordnername: ");

        jLabel20.setText("Projekt Ordner");

        txtProjectFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtProjectFolderActionPerformed(evt);
            }
        });

        jCheckBox2.setText("use current date for retrieve");

        jLabel21.setText("Projekt Name");

        txtProjectName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtProjectNameActionPerformed(evt);
            }
        });
        txtProjectName.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txtProjectNamePropertyChange(evt);
            }
        });

        jLabel22.setText("build.properties (nur für ant-Tool)");

        txtBuildProperties.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txtBuildPropertiesPropertyChange(evt);
            }
        });

        lblXmlImport.setText("Package Xml Import");

        txtXmlImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtXmlImportActionPerformed(evt);
            }
        });

        lblPackage.setText("Package Name (Change Set):");

        jButton5.setText("Herunterladen");
        jButton5.setEnabled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton5)
                        .addGap(17, 17, 17))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel16Layout.createSequentialGroup()
                        .addComponent(lblPackage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtRetrievePackage))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel16Layout.createSequentialGroup()
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(lblXmlImport)
                            .addComponent(jLabel20))
                        .addGap(13, 13, 13)
                        .addComponent(txtXmlImport, javax.swing.GroupLayout.PREFERRED_SIZE, 781, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel16Layout.createSequentialGroup()
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel21)
                                .addGap(160, 160, 160))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)))
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTargetData, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtProjectName, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtBuildProperties, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addComponent(txtProjectFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBox2)))))
                .addGap(82, 82, 82))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(txtTargetData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(txtProjectFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox2))
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtProjectName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel21))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtBuildProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblXmlImport)
                            .addComponent(txtXmlImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtRetrievePackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPackage))))
                .addComponent(jButton5)
                .addContainerGap())
        );

        jTabbedPane7.addTab("Projektdaten", jPanel16);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cmbMetaData, 0, 263, Short.MAX_VALUE)
                            .addComponent(cmdMetaDataName, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jTabbedPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(944, 944, 944)
                                .addComponent(jTabbedPane3))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jTabbedPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 952, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbMetaData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmdMetaDataName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(208, 208, 208)
                        .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTabbedPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTabbedPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(152, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Retrieve", jPanel5);

        jLabel5.setText("Testklassen:");

        jButton6.setText("Hinzufügen");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        lstTestKlassen.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane5.setViewportView(lstTestKlassen);

        jButton9.setText("Löschen");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jLabel8.setText("Deploy");

        jButton11.setText("Löschen");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        tableDeployment.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Typname", "Meta Name", "Datei-Endung", "Ordner Name"
            }
        ));
        tableDeployment.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane6.setViewportView(tableDeployment);

        jLabel19.setText("recent validation id");

        comboTestLevel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NoTestRun", "RunSpecifiedTests", "RunLocalTests", "RunAllTestsInOrg" }));

        jLabel23.setText("TestLevel:");

        jLabel24.setText("Retrieve Folder Name:::");

        jLabel25.setText("deploy to");

        txtDeployTo.setEnabled(false);

        jButton10.setText("Deploy Vorbereitung aktuell");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton13.setText("Validate Deployment");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        btnDeploy.setText("DEPLOY");
        btnDeploy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeployActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton10)
                            .addComponent(jButton13))
                        .addGap(0, 590, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnDeploy)))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addComponent(jButton13)
                .addGap(18, 18, 18)
                .addComponent(btnDeploy)
                .addContainerGap())
        );

        jTabbedPane6.addTab("ant-Tools", jPanel12);

        btnValDepSFDX.setText("Validate Deployment (sfdx)");
        btnValDepSFDX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnValDepSFDXActionPerformed(evt);
            }
        });

        jLabel29.setText("Go to corresponding environment and check deployment!!!");

        btnValDepSFDX1.setText("Validate BACKUP Deployment (sfdx)");
        btnValDepSFDX1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnValDepSFDX1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnValDepSFDX)
                    .addComponent(jLabel29)
                    .addComponent(btnValDepSFDX1))
                .addContainerGap(449, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnValDepSFDX)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnValDepSFDX1)
                .addContainerGap(59, Short.MAX_VALUE))
        );

        jTabbedPane6.addTab("sfdx-Tools", jPanel13);

        btn_DEPLOY.setText("DEPLOY (sfdx)");
        btn_DEPLOY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_DEPLOYActionPerformed(evt);
            }
        });

        btnBackupANDDeploy.setText("Backup AND Deploy");
        btnBackupANDDeploy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackupANDDeployActionPerformed(evt);
            }
        });

        jLabel34.setText("MUSS vorbereitet sein");

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addContainerGap(670, Short.MAX_VALUE)
                .addComponent(btn_DEPLOY)
                .addGap(20, 20, 20))
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addComponent(btnBackupANDDeploy)
                .addGap(18, 18, 18)
                .addComponent(jLabel34)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn_DEPLOY)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 88, Short.MAX_VALUE)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBackupANDDeploy)
                    .addComponent(jLabel34))
                .addContainerGap())
        );

        jTabbedPane6.addTab("sfdx-Deployment", jPanel17);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(txtTestklasse, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(32, 32, 32)
                                .addComponent(jButton6)))
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton9))
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addGap(26, 26, 26)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel23)
                                    .addComponent(jLabel24))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(comboTestLevel, 0, 187, Short.MAX_VALUE)
                                    .addComponent(txtRetrieveFolderName))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel19)
                                    .addComponent(txtRecentVal, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                        .addGap(82, 82, 82)
                                        .addComponent(jLabel25)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtDeployTo, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE))
                                    .addGroup(jPanel6Layout.createSequentialGroup()
                                        .addGap(0, 968, Short.MAX_VALUE)
                                        .addComponent(jButton11)))
                                .addGap(134, 134, 134))))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jTabbedPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 808, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton9))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(22, 22, 22)
                        .addComponent(txtTestklasse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jButton11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel25)
                            .addComponent(txtDeployTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRecentVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboTestLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(txtRetrieveFolderName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(393, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Deployment-Vorbereitung", jPanel6);

        jLabel26.setText("Username::");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("sfdx - Tools", jPanel4);

        jLabel30.setText("package-Tool");

        jLabel31.setText("Object Name");

        txtAreaFieldNames.setColumns(20);
        txtAreaFieldNames.setRows(5);
        jScrollPane10.setViewportView(txtAreaFieldNames);

        jLabel32.setText("Fieldnames of List in Lightning::");

        txtAreaOutputFIeldsPackage.setColumns(20);
        txtAreaOutputFIeldsPackage.setRows(5);
        txtAreaOutputFIeldsPackage.setEnabled(false);
        jScrollPane11.setViewportView(txtAreaOutputFIeldsPackage);

        btnFnamesPackage.setText("get Fieldnames for package");
        btnFnamesPackage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFnamesPackageActionPerformed(evt);
            }
        });

        chkZwischenspeicher.setText("in den Zwischenspeicher schreiben");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel31)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtObjName, javax.swing.GroupLayout.PREFERRED_SIZE, 548, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnFnamesPackage)
                        .addGap(110, 110, 110)
                        .addComponent(chkZwischenspeicher))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 716, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 582, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel32))
                .addContainerGap(506, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(txtObjName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31)
                    .addComponent(btnFnamesPackage)
                    .addComponent(chkZwischenspeicher))
                .addGap(7, 7, 7)
                .addComponent(jLabel32)
                .addGap(18, 18, 18)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 582, Short.MAX_VALUE)
                    .addComponent(jScrollPane10))
                .addContainerGap(218, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Tools", jPanel14);

        javax.swing.GroupLayout jPanelSystemInformationLayout = new javax.swing.GroupLayout(jPanelSystemInformation);
        jPanelSystemInformation.setLayout(jPanelSystemInformationLayout);
        jPanelSystemInformationLayout.setHorizontalGroup(
            jPanelSystemInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSystemInformationLayout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1828, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(141, Short.MAX_VALUE))
        );
        jPanelSystemInformationLayout.setVerticalGroup(
            jPanelSystemInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSystemInformationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2))
        );

        jTabbedPane1.addTab("System Informationen", jPanelSystemInformation);

        jPanelDokumente.setAutoscrolls(true);

        jScrollPane8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tableNew.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Zeile", "Status", "Akzeptanz", "String", "alte Zeile"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, true, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableNew.getTableHeader().setReorderingAllowed(false);
        tableNew.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                tableNewMouseWheelMoved(evt);
            }
        });
        tableNew.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tableNewPropertyChange(evt);
            }
        });
        tableNew.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                tableNewVetoableChange(evt);
            }
        });
        jScrollPane3.setViewportView(tableNew);

        tblBlocks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Aktiv", "Zeile von", "Zeile bis"
            }
        ));
        tblBlocks.setEnabled(false);
        tblBlocks.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tblBlocksPropertyChange(evt);
            }
        });
        jScrollPane7.setViewportView(tblBlocks);

        jButton4.setText("neues Dokument retrieve (Target)");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton8.setText("neues Dokument");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        tableOld.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Zeile", "Status", "Akzeptanz", "String", "neue Zeile", "Wohin verschieben"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, true, true, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableOld.getTableHeader().setReorderingAllowed(false);
        tableOld.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tableOldPropertyChange(evt);
            }
        });
        jScrollPane1.setViewportView(tableOld);

        jButton3.setText("altes Dokument retrieve (Source)");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel11.setText("Zeile von");

        jLabel12.setText("Zeile bis");

        btnEinbeziehen.setText("toggle");
        btnEinbeziehen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEinbeziehenActionPerformed(evt);
            }
        });

        jLabel10.setText("Blöcke");
        jLabel10.setEnabled(false);

        jButton7.setText("altesDokument");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jToggleButton1.setText("toggle new Lines");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jLabel13.setText("Move to Line");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 762, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8))
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinVon, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinBis, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnEinbeziehen, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinMoveToLine, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel10))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 770, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(227, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel10))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton4)
                            .addComponent(jButton8)
                            .addComponent(jButton3)
                            .addComponent(jButton7)
                            .addComponent(spinMoveToLine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel12)
                                .addComponent(spinBis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnEinbeziehen))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(spinVon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel11)
                                .addComponent(jToggleButton1)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addGap(265, 265, 265))
        );

        jScrollPane8.setViewportView(jPanel1);

        javax.swing.GroupLayout jPanelDokumenteLayout = new javax.swing.GroupLayout(jPanelDokumente);
        jPanelDokumente.setLayout(jPanelDokumenteLayout);
        jPanelDokumenteLayout.setHorizontalGroup(
            jPanelDokumenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDokumenteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 2031, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelDokumenteLayout.setVerticalGroup(
            jPanelDokumenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDokumenteLayout.createSequentialGroup()
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 670, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 254, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Dokumente", jPanelDokumente);

        tableCreated.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Quelle", "Zeile", "String"
            }
        ));
        tableCreated.setColumnSelectionAllowed(true);
        tableCreated.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPane2.setViewportView(tableCreated);
        tableCreated.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        loadDocuments.setText("Load Documents");
        loadDocuments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDocumentsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelResultatLayout = new javax.swing.GroupLayout(jPanelResultat);
        jPanelResultat.setLayout(jPanelResultatLayout);
        jPanelResultatLayout.setHorizontalGroup(
            jPanelResultatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelResultatLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1472, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(loadDocuments)
                .addGap(0, 422, Short.MAX_VALUE))
        );
        jPanelResultatLayout.setVerticalGroup(
            jPanelResultatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelResultatLayout.createSequentialGroup()
                .addGroup(jPanelResultatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelResultatLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(loadDocuments))
                    .addGroup(jPanelResultatLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 762, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(130, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Resultat", jPanelResultat);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 2020, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 952, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane4.addTab("Optionen", jPanel9);

        jScrollPane9.setPreferredSize(new java.awt.Dimension(800, 600));

        txtOutput.setColumns(20);
        txtOutput.setRows(5);
        jScrollPane4.setViewportView(txtOutput);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 756, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(570, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 575, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 103, Short.MAX_VALUE))
        );

        jScrollPane9.setViewportView(jPanel7);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(1238, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(363, Short.MAX_VALUE))
        );

        jTabbedPane4.addTab("Resultat", jPanel10);

        loadButton.setText("Laden");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        jButton1.setText("Profile laden");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel18.setText("Profile laden:");

        jButton12.setText("Save Options");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(loadButton)
                        .addGap(82, 82, 82)
                        .addComponent(saveButton)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)
                        .addGap(141, 141, 141)
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressSave, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1)
                        .addGap(102, 102, 102)
                        .addComponent(jButton12))
                    .addComponent(jTabbedPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 2058, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(loadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(saveButton)
                        .addComponent(jButton2))
                    .addComponent(progressSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1)
                        .addComponent(jButton12))
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane4))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // TODO add your handling code here:
        String path = getPath("deployment","");
        String retrievePath = getPath("retrieve","");
        
        File deployFile = new File(path);
        
        File deployFileMeta = new File(path + "-meta.xml");
        
        File retrieveFileMeta = new File(retrievePath + "-meta.xml");
        
        try {
            String text = null;
            if(retrieveFileMeta.exists()){
                text = deprecated_Start.readFile(retrieveFileMeta.getPath());
                deprecated_Start.createFile(deployFileMeta.getParent(),deployFileMeta.getName(),text,true);
            }

            deprecated_Start.createFile(deployFile.getParent(),deployFile.getName(),txtOutput.getText(),true);
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        deprecated_Start.saveFolder.mkdirs();
        
        try {
            deployFile = this.getSaveFile();
            FileOutputStream fos = new FileOutputStream(deployFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.dep);
            oos.close();
            
            this.saveDep.save();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_saveButtonActionPerformed
    
    public static File[] getAllFieldFiles(
            String path
    ){
        File [] fieldFiles = null;
        
        File profileFolder = new File(path + "\\profiles\\");
        File permissionFolder = new File(path + "\\permissionsets\\");
        
        File[] profiles = profileFolder.listFiles();
        File[] permissions = permissionFolder.listFiles();
        
        int fileCount = profiles.length + permissions.length;
        
        fieldFiles = new File[fileCount];
        
        int i = 0;
        for(File f:profiles){
            fieldFiles[i] = f;
            i++;
        }
        
        for(File f:permissions){
            fieldFiles[i] = f;
            i++;
        }
        
        return fieldFiles;
    }
    
    private void putDocuments(
            File sourceDir,
            String title,
            Boolean isOldText
    ) throws FileNotFoundException, IOException{
        putDocuments(sourceDir,title,isOldText,true,null);
    }
    
    public static String readFile(
            String path
    ) throws FileNotFoundException, IOException{
        
        File f = new File(path);
        if(!f.exists())return null;
        
        FileReader fr = new FileReader(path);
        
        BufferedReader br = new BufferedReader(fr);
        String text = "";

        if(br.ready()){
            text = br.readLine();
        }

        while(br.ready()){
            text += "\n" + br.readLine();
        }
        
        return text;
    }
    
    private void putDocuments(
            File sourceDir,
            String title,
            Boolean isOldText,
            Boolean withFileChooser,
            String path
    ) throws FileNotFoundException, IOException{
        if(this.dep == null)this.dep = new Deployment();
        JFileChooser chooser = null;
        int rueck = 0;
        if(withFileChooser){
            chooser = new JFileChooser();
            chooser.setCurrentDirectory(sourceDir);
            rueck = chooser.showDialog(null,title);
        }
        
        // Dialog zum Oeffnen von Dateien anzeigen
        
        File selected = null;
        
        if(!withFileChooser || withFileChooser && rueck==chooser.APPROVE_OPTION){
            FileReader fr;
            if(withFileChooser){
                selected = chooser.getSelectedFile();
                fr = new FileReader(selected.getPath());
            }else{
                fr = new FileReader(path);
            }
             
            BufferedReader br = new BufferedReader(fr);
            String text = "";
            
            if(br.ready()){
                text = br.readLine();
            }
            
            while(br.ready()){
                text += "\n" + br.readLine();
            }
            
            if(isOldText){
                this.dep = new Deployment(
                        text,
                        this.dep.textNeu,
                        !chkRemoved.isSelected(),
                        !chkChanged.isSelected(),
                        !chkExisting.isSelected(),
                        !chkMoved.isSelected(), 
                        (Integer) spinMinValChanged.getValue(),
                        txtTypeName.getText()
                );
                
            }else{
                this.dep = new Deployment(
                        this.dep.textAlt,
                        text,
                        !chkRemoved.isSelected(),
                        !chkChanged.isSelected(),
                        !chkExisting.isSelected(),
                        !chkMoved.isSelected(),
                        (Integer) spinMinValChanged.getValue(),
                        txtTypeName.getText()
                );
            }

            br.close();
        } 
    }
   
    public static void putRows(
            JTable table,
            List rows,
            Integer whichTable
    ){
        table.setDefaultRenderer(Object.class, Deployment.renderer);
        
        DefaultTableModel model;
        
        model = (DefaultTableModel) table.getModel();
        
        while(model.getRowCount()>0){
            model.removeRow(0);
        }
        
        for (int i = 0; i < rows.size(); i++) {
            Object obj = rows.get(i);
            LineWrapper lw = null;
            Block blk = null;
            
            if(obj instanceof LineWrapper)lw = (LineWrapper) obj;
            TableColumnModel tColMod = table.getColumnModel();
            TableColumn tc;
            Integer mult = 5;
            
            if(lw != null){
                if(whichTable==deprecated_Start.tableCreatedNumber){

                    model.addRow(new Object[]{
                        lw.itemMap.get("source"),
                        lw.itemMap.get("zeile"),
                        lw.itemMap.get("sourceValue")}
                    );
                    tc = tColMod.getColumn(0);
                    if(lw.getSource().length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(lw.getSource().length()*mult);
                    }

                    tc = tColMod.getColumn(1);
                    String zeile = lw.getZeile()+"";
                    if(zeile.length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(zeile.length()*mult);
                    }

                    tc = tColMod.getColumn(2);
                    if(lw.getValue().length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(lw.getValue().length()*mult);
                    }
                }else if(whichTable==deprecated_Start.tableOldNumber){
                    
                    LineWrapperItem lwi 
                            = lw.itemMap.get("acceptInResultingText");
                    if(lw.getAcceptInResultingText()){
                        lwi = lw.itemMap.get("insertOnLine");
                    }
                    
                    model.addRow(new Object[]{
                        lw.itemMap.get("zeile"), 
                        lw.itemMap.get("styleClass"), 
                        lwi,
                        lw.itemMap.get("sourceValue"),
                        lw.itemMap.get("zeileMove"),
                        lw.itemMap.get("insertOnLine")}
                    );

                    String zeile = lw.getZeile()+"";
                    tc = tColMod.getColumn(0);
                    if(zeile.length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(zeile.length()*mult);
                    }

                    tc = tColMod.getColumn(1);
                    if(lw.getStyleClass().length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(lw.getStyleClass().length()*mult);
                    }

                    tc = tColMod.getColumn(2);
                    String acceptInResultingText = lw.getAcceptInResultingText()+"";
                    if(acceptInResultingText.length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(acceptInResultingText.length()*mult);
                    }

                    tc = tColMod.getColumn(3);
                    if(lw.getValue().length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(lw.getValue().length()*mult);
                    }

                    tc = tColMod.getColumn(4);
                    String zeileMove = lw.getZeileMove() +"";
                    if(zeileMove.length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(zeileMove.length()*mult);
                    }

                    tc = tColMod.getColumn(5);
                    String insertOnLine = lw.getInsertOnLine() +"";
                    if(zeileMove.length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(insertOnLine.length()*mult);
                    }
                }else if(whichTable==deprecated_Start.tableNewNumber){
                    model.addRow(new Object[]{
                        lw.itemMap.get("zeile"),
                        lw.itemMap.get("styleClass"),
                        lw.itemMap.get("acceptInResultingText"),
                        lw.itemMap.get("sourceValue"),
                        lw.itemMap.get("zeileMove")
                        }
                    );

                    String zeile = lw.getZeile()+"";
                    tc = tColMod.getColumn(0);
                    if(zeile.length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(zeile.length()*mult);
                    }

                    tc = tColMod.getColumn(1);
                    if(lw.getStyleClass().length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(lw.getStyleClass().length()*mult);
                    }

                    tc = tColMod.getColumn(2);
                    String acceptInResultingText = lw.getAcceptInResultingText()+"";
                    if(acceptInResultingText.length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(acceptInResultingText.length()*mult);
                    }

                    tc = tColMod.getColumn(3);
                    if(lw.getValue().length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(lw.getValue().length()*mult);
                    }

                    tc = tColMod.getColumn(4);
                    String zeileMove = lw.getZeileMove() +"";
                    if(zeileMove.length()*mult > tc.getPreferredWidth()){
                        tc.setPreferredWidth(zeileMove.length()*mult);
                    }
                }

            }else if(obj instanceof  Block){
                blk = (Block)obj;
                model.addRow(new Object[]{
                    blk.isActive,
                    blk.zeileVon,
                    blk.zeileBis}
                );
                tc = tColMod.getColumn(0);
                if(4*mult > tc.getPreferredWidth()){
                    tc.setPreferredWidth(lw.getSource().length()*mult);
                }

                tc = tColMod.getColumn(1);
                String zeile = 4+"";
                if(zeile.length()*mult > tc.getPreferredWidth()){
                    tc.setPreferredWidth(zeile.length()*mult);
                }

                tc = tColMod.getColumn(2);
                if(4*mult > tc.getPreferredWidth()){
                    tc.setPreferredWidth(lw.getValue().length()*mult);
                }
            }
        }
    }
    
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        
        try {
           
            File buildFile= createBuildFile(deprecated_Start.commandFolder.getPath() + "\\" + deprecated_Start.projectName,
                    txtSource.getText(),"retrieve",
                    "retrieve","retrieve");
            
            File packageFile = this.createPackageFile(deprecated_Start.commandFolder.getPath()+ "\\" + deprecated_Start.projectName,
                    txtMetadataName.getText(),
                    txtSubname.getText(),
                    txtTypeName.getText()
            );
            File commandFileRetrieve = createCommandFile(deprecated_Start.commandFolder.getPath()+ "\\" + deprecated_Start.projectName,"retrieve",false);
            
            Process process = new ProcessBuilder(commandFileRetrieve.getPath()).start();
            
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton3ActionPerformed
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        if(this.dep != null){
            this.dep.toggleOutput();
            this.putRows(tableCreated,this.dep.zeilenEntstanden,deprecated_Start.tableCreatedNumber);
            this.dep.transmitResult(txtOutput);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        try {
            buildFileRetrieveQA = createBuildFile(deprecated_Start.commandFolder.getPath() + "\\" + deprecated_Start.projectName
                    ,txtTarget.getText(),"retrieve","\\retrieve_QA","retrieve_QA");
            packageFile = this.createPackageFile(deprecated_Start.commandFolder.getPath()+ "\\" + deprecated_Start.projectName,
                txtMetadataName.getText(),
                txtSubname.getText(),
                txtTypeName.getText()
            );
            commandFileRetrieveQA = createCommandFile(deprecated_Start.commandFolder.getPath()+ "\\" + deprecated_Start.projectName,
                    "retrieve_QA",false);
            Process process = new ProcessBuilder(commandFileRetrieveQA.getPath()).start();
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void tableOldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tableOldPropertyChange
        // TODO add your handling code here:
        if(this.dep==null)return;
        
        deprecated_Start.changeAcceptInResultingText(tableOld,evt,this.dep.zeilenAlt,0);
    }//GEN-LAST:event_tableOldPropertyChange

    private void txtTypeNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTypeNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTypeNameActionPerformed
    
    private static void writeProperty(
            BufferedWriter bw,
            String property,
            String value
    ) throws IOException{
        
        bw.write(property + "=\"${" + value + "." + property + "}\"\n");
    }
    
    private static void writeFixedProperty(
            StringBuilder sb,
            String property,
            String value
    ) throws IOException{
        sb.append(property + "=\"" + value + "\"\n");
    }
    
    private static void writeProperty(
            StringBuilder sb,
            String property,
            String value
    ) throws IOException{
        
        sb.append(property + "=\"${" + value + "." + property + "}\"\n");
    }
    
    private static File getActualValidFile(
            String path,
            String name,
            String ending
    ){
        File nextValid = getNextValidFile(deprecated_Start.backup.getPath(), name, ending);
        String str = nextValid.getName()
                .replaceAll(name, "")
                .replaceAll("." + ending, "");
        if(str.length()<=0)return null;
        int number = Integer.valueOf(str);
        number--;
        
        String fileName = nextValid.getName()
                .replaceAll("." + ending, "").replaceAll(str, "");
        
        File actualValid = new File(deprecated_Start.backup.getPath() + "\\" + fileName + number + "." + ending);
        
        if(number < 0)return null;
        return actualValid;
    }
    
    private static File getNextValidFile(
            String path,
            String name,
            String ending
    ){
        File f = new File(path + "\\" + name + "." + ending);
        
        for(Integer i = 0;f.exists();i++){
            f = new File(path + "\\" + name + i + "." + ending);
        }
        
        return f;
    }
    
    /**
     * path -- 
     * name --
     * targetName --
     * directoryName -- determines the name of the directory where data will be saved
     * methodName -- determines the name in a build xml file
     * <target name= methodName>
</project>
     * packageName -- Name from the package to retrieve
    */
    private static File createBuildFile(
            String path,
            String name,
            String targetName,
            String directoryName,
            String methodName,
            String packageName
   ) throws IOException{
        Long millis = System.currentTimeMillis();
        StringBuilder sb = initBuildFile(methodName,
                directoryName,
                targetName,
                millis
        );
        writeProperties(sb,name,packageName,directoryName,millis);
        closeBuildFile(sb,methodName,millis,directoryName);
        
        return createXmlFile(path,"build",sb.toString());
    }
    
    private static void closeBuildFile(
            StringBuilder sb,
            String targetName,
            Long millis,
            String directoryName
    ){
        sb.append("/>\n");
        sb.append("<record name=\"" + directoryName + "_" + millis + "\\log_" 
                + millis + ".txt\" action=\"stop\"/>");
        sb.append("</target>\n");
        appendMoveTaskToAntFile(sb,targetName,directoryName,millis);
        sb.append("</project>");
    }
    
    private static StringBuilder initBuildFile(
            String methodName,
            String directoryName,
            String targetName,
            Long millis
    ){
        StringBuilder sb = new StringBuilder();
        setBuildFileInit(sb,methodName);
        sb.append("<mkdir dir=\"" + directoryName + "_" + millis + "\"/>\n");
        
        sb.append("<record name=\"" + directoryName + "_" + millis + "\\log_" 
                + millis + ".txt\" action=\"start\"/>");
        sb.append("<sf:" + targetName +" \n");
        
        return sb;
    }
    
    private static void writeProperties(
            StringBuilder sb,
            String name,
            String packageName,
            String directoryName,
            Long millis
    ) throws IOException{
        
        writeAccessProperties(sb,name);
        
        //@id: 1 BEGIN
        if(packageName != null){
            deprecated_Start.writeFixedProperty(sb,"packageNames",packageName);
        }else{
            deprecated_Start.writeFixedProperty(sb,"unpackaged","package.xml");
        }
        //@id: 1 END
        
        deprecated_Start.writeFixedProperty(sb,"unzip","false");
        deprecated_Start.writeFixedProperty(sb,"retrieveTarget",directoryName + "_" + millis);
    }
    
    private static void writeAccessProperties(
            StringBuilder sb,
            String name
    ) throws IOException{
        deprecated_Start.writeProperty(sb,"username",name);
        deprecated_Start.writeProperty(sb,"password",name);
        deprecated_Start.writeProperty(sb,"serverurl",name);
        deprecated_Start.writeProperty(sb,"maxPoll",name);
    }
    
    private static void appendMoveTaskToAntFile(
            StringBuilder sb,
            String targetName,
            String directoryName,
            Long millis
    ){
        appendMoveTaskToAntFile(sb,targetName,"",directoryName,millis);
    }
    
    private static void appendMoveTaskToAntFile(
            StringBuilder sb,
            String targetName,
            String appendix,
            String directoryName,
            Long millis
    ){
        sb.append("<move file=\"build.xml\" tofile=\"" 
                + directoryName + "_" + millis + "\\build_" + appendix
                + millis + ".xml\"/>");
        sb.append("<move file=\"" + targetName + ".cmd\" tofile=\"" 
                + directoryName + "_" + millis + "\\build_" + appendix
                + millis + ".cmd\"/>");
    }
            
    private static File createBuildFile(
            String path,
            String name,
            String targetName,
            String directoryName,
            String methodName
    ) throws IOException{
        return createBuildFile(
                path,
                name,
                targetName,
                directoryName,
                methodName,
                null
        );
    }
    
    private static File createBuildFile(
            String path,
            String name,
            String targetName,
            String directoryName
   ) throws IOException{
        return createBuildFile(
                path,
                name,
                targetName,
                directoryName,
                directoryName
        );
    }
    
    private static File createBuildFile(
            String path,
            String name
    ) throws IOException{
        return createBuildFile(path,name,"retrieve","retrieve");
    }
    
    private static void setBuildFileInit(
            StringBuilder sb,
            String directoryName
    ){
        sb.append("<project name=\"Sample usage of Salesforce Ant tasks\" default=\"test\" basedir=\".\" xmlns:sf=\"antlib:com.salesforce\">\n");
        sb.append("<property file=\"" + deprecated_Start.opts.buildPropertiesPath + "\"/>\n");
        sb.append("<property environment=\"env\"/>\n");
        sb.append("<taskdef resource=\"com/salesforce/antlib.xml\" uri=\"antlib:com.salesforce\">\n");
        sb.append("<classpath>\n");
        sb.append("<pathelement location=\"" + deprecated_Start.antFile.getPath() + "\" />\n");
        sb.append("</classpath>\n");
        sb.append("</taskdef>\n");
        sb.append("<target name=\"" + directoryName +"\">\n");
        
    }
    private static File createDeployBuildFile(
            String path,
            String targetServer,
            String targetName,
            List<String> testKlassen,
            Boolean onlyValidate,
            String recentValId
    ) throws IOException{
        return createDeployBuildFile(
                path,
                targetServer,
                targetName,
                testKlassen,
                onlyValidate,
                recentValId
        );
    }
    private static File createDeployBuildFile(
            String path,
            String targetServer,
            String targetName,
            List<String> testKlassen,
            Boolean onlyValidate,
            String recentValId,
            String deployRoot, // @date: 2019-03-21-MAR, @deprecated
            String folder,
            String testLevel,
            String retrieveFolderName
    ) throws IOException{
        Long millis = System.currentTimeMillis();
        
        StringBuilder sb = new StringBuilder();
        setBuildFileInit(sb,targetName);
        Boolean isRecentValConform = recentValId != null && recentValId.length() > 0;
        
        sb.append("<record name=\"deploy_" + millis + "\\log_" 
                + millis + ".txt\" action=\"start\"/>");
        if(isRecentValConform){
            sb.append("<sf:deployRecentValidation \n");
        }else{
            sb.append("<sf:deploy \n");
        }
        
        deprecated_Start.writeProperty(sb,"username",targetServer);
        deprecated_Start.writeProperty(sb,"password",targetServer);
        deprecated_Start.writeProperty(sb,"serverurl",targetServer);
        
        if(isRecentValConform){
            deprecated_Start.writeFixedProperty(sb, "recentValidationId", recentValId);
            sb.append(">\n");
        }else{
            //sb.append("deployroot=\"" + deployRoot + "\\" + folder + "\"");
            
            //deploy the last created ZipFile
            File allFiles = new File(deployRoot);
            System.out.println("###deployRoot: " + deployRoot);
            
            String retrieveZipFilePath = null;
            
            if(retrieveFolderName==null){
                for(String fileName : allFiles.list()){
                    System.out.println("###fileName: " + fileName);
                    if(fileName.startsWith("retrieve") && !fileName.contains("old")){
                        if(!fileName.equals(retrieveFolderName)){
                            retrieveFolderName = fileName;
                        }
                    }
                }
            }
            
            System.out.println("###retrieveFolder: " + deployRoot + "\\" 
                    + retrieveFolderName);
            File zipFolder = new File(deployRoot + "\\" + retrieveFolderName);
            for(String fileName : zipFolder.list()){
                System.out.println("###zipFolder files: " + fileName);
                if(fileName.endsWith(".zip")){
                    retrieveZipFilePath = deployRoot 
                            + "\\" + retrieveFolderName 
                            + "\\" + fileName;
                    break;
                }
            }
            sb.append("zipFile=\"" + retrieveZipFilePath +"\"");
            
            if(testKlassen == null || testKlassen.size() == 0){
                sb.append("\ntestLevel=\"" + testLevel + "\"");
                
            }else{
                sb.append("\ntestLevel=\"RunSpecifiedTests\"");
            }

            sb.append(" checkOnly=\"" + onlyValidate +"\"");

            sb.append(">\n");

            if(testKlassen == null || testKlassen.size() == 0){

            }else{
                for(String testClass:testKlassen){
                    sb.append("\n<runTest>" + testClass + "</runTest>");
                }
            }
        }
        
        if(isRecentValConform){
            sb.append("\n</sf:deployRecentValidation>\n");
        }else{
            sb.append("\n</sf:deploy>\n");
        }
        sb.append("<record name=\"log.txt\" action=\"stop\"/>");
        sb.append("</target>\n");
        //look for directory of last zip-file
        
        appendMoveTaskToAntFile(sb,targetName,"deploy",millis);
        
        sb.append("</project>");
        
        return createXmlFile(path,"build",sb.toString());
    }
    
    private static File createCommandFile(
            String path,
            String commandType,
            Boolean toDelete
    ) throws IOException{
        return createCommandFile(path,commandType,toDelete,false,null);
    }
    
    private static File createCommandFile(
            String path,
            String commandType,
            Boolean toDelete,
            Boolean isSFDXTool,
            String sfdxCommand
    ) throws IOException{
        StringBuilder sb = new StringBuilder();
        if(path != null)sb.append(path.substring(0, path.indexOf("\\")) + "\\\n");
        sb.append("cd " + path + "\n");
         
        if(!isSFDXTool){
            sb.append("start ant " + commandType);

            System.out.println("pfad: " + sb.toString());
        }else{
            if(sfdxCommand == null){
                throw new IOException("The sfdx command must not be null!!!");
            }else{
                sb.append(sfdxCommand);
            }
        }
        
        return createFile(path,commandType,sb.toString(),"cmd",toDelete);
    }
    
    private static File createCommandFileRetrieve(
            String path
    ) throws IOException{
        
        return createCommandFile(path,"retrieve",true);
    }
    
    private static Map<String,File> createDeploymentFiles(
            String path,
            String targetServer,
            Package pckg,
            List<String> testClasses,
            Boolean runCommandFile,
            Boolean onlyValidate,
            String recentValId,
            String deployRoot,
            String testLevel,
            String retrieveFolderName
    ) throws IOException{
        HashMap<String,File> depMap = new HashMap<String,File>();
        
        File buildFile = createDeployBuildFile(
                path,
                targetServer, 
                "deployment", 
                testClasses,
                onlyValidate,
                recentValId,
                deployRoot,
                "deployment",
                testLevel,
                retrieveFolderName
        );
        pckg.writePackage();
        File packageFile = pckg.packageFile;
        
        File commandFile = createCommandFile(path,"deployment",true);
        if(runCommandFile){
            Process process = new ProcessBuilder(commandFile.getPath()).start();
        } 
        
        depMap.put("build",buildFile);
        depMap.put("package",packageFile);
        depMap.put("command",commandFile);
        
        return depMap;
    }
    
    private static Map<String,File> createDeploymentFiles(
            String path,
            String targetServer,
            String xmlImportPath,
            List<String> testClasses,
            Boolean runCommandFile,
            Boolean onlyValidate,
            String recentValId,
            String deployRoot,
            String testLevel,
            String retrieveFolderName
    ) throws IOException{
        HashMap<String,File> depMap = new HashMap<String,File>();
        
        File buildFile = createDeployBuildFile(path,targetServer, 
                "deployment", testClasses,onlyValidate,recentValId,
                deployRoot,"retrieve",testLevel,retrieveFolderName);
        
        File packageFile = new File(xmlImportPath);
        
        File commandFile = createCommandFile(path,"deployment",true);
        
        Functions.debugSwitchOn("createDeploymentFiles");
        Functions.debugSwitchOff("createDeploymentFiles");
        Functions.debug("createDeploymentFiles","package-content: " 
                + readFile(xmlImportPath));
        Functions.debug("createDeploymentFiles","command-content: " 
                + readFile(path + "\\deploy.cmd"));
        Functions.debug("createDeploymentFiles","build-content: " 
                + readFile(path + "\\build.xml"));
        Functions.debug("createDeploymentFiles","path: " + path);
        
        if(runCommandFile){
            Process process = new ProcessBuilder(commandFile.getPath()).start();
        } 
        
        depMap.put("build",buildFile);
        depMap.put("package",packageFile);
        depMap.put("command",commandFile);
        
        return depMap;
    }
    
    private static File createPackageFile(
            String targetPath,
            String sourcePath
    ) throws IOException{
        return createXmlFile(targetPath,"package",readFile(sourcePath));
    }
    
    private static File createPackageFile(
            String path,
            String metaDataName,
            String subname,
            String type
    ) throws IOException{
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n");
        sb.append("<types>\n");
        sb.append("<members>" + metaDataName);
        if(subname != null && subname.length() > 0)sb.append("." + subname);
        sb.append("</members>\n");
        if(subname != null && subname.length() > 0){
            if("CustomObject".equals(type)){
                type = "CustomField";
            }
        }
        sb.append("<name>" + type + "</name>\n");
        sb.append("</types>\n");
        
        if(subname != null && subname.length() > 0){
            if("CustomField".equals(type)){
                deprecated_Start.appendPermissions(sb);
            }
        }
        
        sb.append("</Package>");
        
        return createXmlFile(path,"package",sb.toString());
    }
    
    public static void appendPermissions(
        StringBuilder sb
    ){
        sb.append("<types>\n");
        sb.append("<members>");
        sb.append("*");
        sb.append("</members>\n");
        sb.append("<name>Profile</name>\n");
        sb.append("</types>\n");

        sb.append("<types>\n");
        sb.append("<members>");
        sb.append("*");
        sb.append("</members>\n");
        sb.append("<name>PermissionSet</name>\n");
        sb.append("</types>\n");
    }
    
    public static File createFile(
            String path,
            String fileName,
            String fileContent,
            Boolean deleteAfterCreate
    ) throws IOException{
        return createFile(path,fileName,fileContent,"",deleteAfterCreate);
    }
    
    private static File createFile(
            String path,
            String fileName,
            String fileContent,
            String ending,
            Boolean deleteAfterCreate
    ) throws IOException{
        String filePath = "";
        String buildFilePath = "";
        
        if(ending.length()>0){
            filePath = path+"\\" + fileName + "." + ending;
            buildFilePath = path+"\\"+fileName+"." + ending;
        }else{
            filePath = path+"\\" + fileName;
            buildFilePath = path+"\\"+fileName;
        }
         
        File f = new File(filePath);
        File buildFile = new File(buildFilePath);
        
        f = getNextValidFile(path,fileName,ending);
       
        if(buildFile.exists()){
            if(deleteAfterCreate) buildFile.delete();
            else buildFile.renameTo(f);
        }
        else buildFile = f;
        
        String directoryStr = buildFile.getParent() + "";
        
        File folders = new File(directoryStr);
        folders.mkdirs();
        
        Functions.debugSwitchOn("createFile");
        Functions.debug("createFile", "dir: " + buildFile.getPath());
        
        buildFile.createNewFile();
        FileWriter fw = new FileWriter(buildFile);
        BufferedWriter bw = new BufferedWriter(fw);

        bw.write(fileContent);
        
        bw.close();
        
        return buildFile;
    }
    
    private static File createXmlFile(
            String path,
            String fileName,
            String fileContent
    ) throws IOException{
        return createFile(path,fileName,fileContent,"xml",false);
    }
    
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        
        try {
            File buildFile= this.createBuildFile(deprecated_Start.backup.getPath(),txtSource.getText());
            
            File packageFile = this.createPackageFile(deprecated_Start.backup.getPath(),
                    txtMetadataName.getText(),
                    txtSubname.getText(),
                    txtTypeName.getText()
            );
            File commandFileRetrieve = createCommandFileRetrieve(deprecated_Start.backup.getPath());
            Process process = new ProcessBuilder(commandFileRetrieve.getPath()).start();
            
            /*
            File buildFileRetrieveQA = createBuildFile(Start.backup.getPath(),txtTarget.getText(),"retrieve","retrieve_QA");
            File commandFileRetrieveQA = createCommandFile(Start.backup.getPath(),"retrieve_QA");
            new ProcessBuilder(commandFileRetrieveQA.getPath()).start();
            
            //System.out.println(commandFileRetrieve.getPath());*/
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }//GEN-LAST:event_jButton5ActionPerformed
    
    private void loadOldDocuments(){
        
        deprecated_Start.renameFile(deprecated_Start.backup.getPath(),"build","xml");
        deprecated_Start.renameFile(deprecated_Start.backup.getPath(),"package","xml");
        deprecated_Start.renameFile(deprecated_Start.backup.getPath(),"retrieve","cmd");
        
        if(packageFile != null)packageFile.delete();
        if(commandFileRetrieveQA != null)commandFileRetrieveQA.delete();
        
        try {
            String path = getPath("retrieve","");
            
            // TODO add your handling code here:
            this.putDocuments(deprecated_Start.sourceDir,"altes Dokument wählen",true,false,
                    path);
            
            this.putRows(tableOld,this.dep.zeilenAlt,deprecated_Start.tableOldNumber);
            this.putRows(tableNew,this.dep.zeilenNeu,deprecated_Start.tableNewNumber);
            this.putRows(tableCreated,this.dep.zeilenEntstanden,deprecated_Start.tableCreatedNumber);
            this.putRows(tblBlocks,this.dep.blocks,null);
            this.dep.transmitResult(txtOutput);
          } catch (IOException ex) {
              Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
          }
    }
    
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        loadOldDocuments();
        saveButton.setEnabled(true);
    }//GEN-LAST:event_jButton7ActionPerformed
    
    private static void renameFile(
            String path,
            String name,
            String ending
    ){
        File f  = new File(path+"\\" + name + "." + ending);
        
        File actualValidBuild = getActualValidFile(deprecated_Start.backup.getPath(), name, ending);
        if(actualValidBuild != null){
            f.delete();
            actualValidBuild.renameTo(f);
        }
    }
    
    private String getPathAsSaving(){
        return this.getPath("save",null,true);
    }
    
    private String getPath(String folder, String append){
        return this.getPath(folder,append,false);
    }
    
    private String getPath(String folder, String append, Boolean asSaving){
        String path = deprecated_Start.commandFolder.getPath() + "\\" + txtProjectName.getText()
                        +  "\\" + folder;
        if(!asSaving){
            path += "\\" + txtDirectoryOld.getText();
        }
        path += "\\" + txtMetadataName.getText();
        
        String metadataName = txtTypeName.getText();
        if("AuraDefinitionBundle".equals(metadataName)){
            path += "\\" + txtMetadataName.getText();
            if("AuraBundle-controller".equals(cmbMetaData.getSelectedItem()+"")){
                path += "Controller";
            }else if("AuraBundle-helper".equals(cmbMetaData.getSelectedItem()+"")){
                path += "Helper";
            }else if("AuraBundle-renderer".equals(cmbMetaData.getSelectedItem()+"")){
                path += "Renderer";
            }
        }
        path += "." + txtDataEnding.getText();
        if(!asSaving)path += append;
            
        return path;
    }
    
    private void loadNewDocuments(){
        deprecated_Start.renameFile(deprecated_Start.backup.getPath(),"build","xml");
        deprecated_Start.renameFile(deprecated_Start.backup.getPath(),"package","xml");
        deprecated_Start.renameFile(deprecated_Start.backup.getPath(),"retrieve_QA","cmd");
          
        if(packageFile != null)packageFile.delete();
        if(commandFileRetrieveQA != null)commandFileRetrieveQA.delete();
        
        try {
            String path = getPath("retrieve_QA","");
            
            this.putDocuments(deprecated_Start.sourceDir,"neues Dokument wählen",false,false,
                    path);
            this.putRows(tableOld,this.dep.zeilenAlt,deprecated_Start.tableOldNumber);
            this.putRows(tableNew,this.dep.zeilenNeu,deprecated_Start.tableNewNumber);
            this.putRows(tableCreated,this.dep.zeilenEntstanden,deprecated_Start.tableCreatedNumber);
            this.putRows(tblBlocks,this.dep.blocks,null);
            this.dep.transmitResult(txtOutput);
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        loadNewDocuments();
        saveButton.setEnabled(true);
    }//GEN-LAST:event_jButton8ActionPerformed

    private static void deleteListenElement(
            JList lst
    ){
        int selInd = lst.getSelectedIndex();
        
        if(selInd>-1){
            DefaultListModel dlm = (DefaultListModel) lst.getModel();
            dlm.remove(lst.getSelectedIndex());
        }
    }
    
    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
        deprecated_Start.deleteListenElement(lstTestKlassen);
        try {
            this.saveDep.save();
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton9ActionPerformed
    
    public static void addListElement(
            JList lst,
            String txt
    ){
        DefaultListModel dlm = new DefaultListModel();
        
        ListModel lm = lst.getModel();
        
        for(int i =0;i<lm.getSize();i++){
            String str = (String) lm.getElementAt(i);
            dlm.addElement(str);
        }
        
        if(txt != null && txt.length()>0) dlm.addElement(txt);
        
        lst.setModel(dlm);
    }
    
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        deprecated_Start.addListElement(lstTestKlassen,txtTestklasse.getText());
        
        try {
            this.saveDep.save();
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
        int row = tableDeployment.getSelectedRow();
        
        if(row > -1){
            DefaultTableModel model;

            model = (DefaultTableModel) tableDeployment.getModel();

            model.removeRow(row);    
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model;
        
        model = (DefaultTableModel) tableDeployment.getModel();
        
        String typeName = txtTypeName.getText();
        String metadataName = txtMetadataName.getText();
        String subName = txtSubname.getText();
        
        if(subName != null && subName.length() > 0){
            if("CustomObject".equals(typeName)){
                typeName = "CustomField";
            }
            metadataName += "." + subName;
        }
        
        model.addRow(new Object[]{
                typeName,
                metadataName, 
                txtDataEnding.getText(),
                txtDirectoryOld.getText()
            }
        );
        
        try {
            this.saveDep.save();
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton10ActionPerformed
    
    private void deploy(
        Boolean runTests
    ) throws IOException{
        List<String> testClasses = new ArrayList<String>();
        DefaultListModel dlm = null;
        
        String xmlImportPath = txtXmlImport.getText().trim();
        
        try{
            dlm = (DefaultListModel) lstTestKlassen.getModel();
        }catch(ClassCastException cce){
            dlm = new DefaultListModel();
            lstTestKlassen.setModel(dlm);
        }
        
        for(int i = 0;i < dlm.getSize();i++){
            String val = dlm.getElementAt(i) + "";
            testClasses.add(val);
        }
        
        Package pck = null;
        Functions.debugSwitchOn("xmlImportPath");
        Functions.debug("xmlImportPath", xmlImportPath);
        
        String projectPath = txtProjectFolder.getText().trim();
        String packageName = txtRetrievePackage.getText().trim();
        String buildFilePath = deprecated_Start.commandFolder.getPath().trim() + "\\" + 
                    projectPath; 
                
        if(packageName.length() > 0){
            buildFilePath +=  "\\" + packageName.trim();
        }
                
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        
        
        if(xmlImportPath == null || "".equals(xmlImportPath)){
            pck = Package.createDeploymentPackage(
                buildFilePath + "\\deployment\\package.xml",
                tableDeployment
            );
        }
        
        String testLevel = comboTestLevel.getSelectedItem()+"";
        if(pck != null){
            deprecated_Start.createDeploymentFiles(buildFilePath
                ,txtTarget.getText(),pck,
                testClasses,true,runTests,txtRecentVal.getText(),buildFilePath,
                testLevel,
                txtRetrieveFolderName.getText()
            );
        }else{
            deprecated_Start.createDeploymentFiles(buildFilePath,txtTarget.getText(),
                    xmlImportPath,
                testClasses,true,runTests,txtRecentVal.getText(),"",
                testLevel,
                txtRetrieveFolderName.getText()
            );
        
        }
        
    }
    
    private void btnDeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeployActionPerformed
        try {
            // TODO add your handling code here:
            deploy(false);
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnDeployActionPerformed

    private void loadDocumentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadDocumentsActionPerformed
        // TODO add your handling code here:
        loadNewDocuments();
        loadOldDocuments();
    }//GEN-LAST:event_loadDocumentsActionPerformed

    private void tblBlocksPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tblBlocksPropertyChange
        // TODO add your handling code here:
        
        Map<String,String> propMap = getPropertyMapFromSource(evt.getSource()+"");
        int column = Integer.valueOf(propMap.get("editingColumn"));
        
        if(column != 0)return;
        
        DefaultTableModel model = (DefaultTableModel) tblBlocks.getModel();
        DefaultTableModel modelOld = (DefaultTableModel) tableOld.getModel();
        
        if(propMap.containsKey("editingRow")){
            int row = Integer.valueOf(propMap.get("editingRow"));
            if(row>=0){
                int zeileVon = Integer.valueOf(model.getValueAt(row, 1)+"");
                int zeileBis = Integer.valueOf(model.getValueAt(row, 2)+"");
                
                Boolean isActive = Boolean.valueOf(model.getValueAt(row, 0)+"");
                
                Block blk = this.dep.blocksMap.get(zeileVon);
                blk.isActive = isActive;
                
                for(int i=zeileVon;i<=zeileBis;i++){
                    modelOld.setValueAt(isActive+"", i, 2);
                    changeAcceptInResultingText(model,row,column,this.dep.zeilenAlt,1,i);
                }
            }
        }   
    }//GEN-LAST:event_tblBlocksPropertyChange

    private void btnEinbeziehenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEinbeziehenActionPerformed
        // TODO add your handling code here:
        DefaultTableModel modelOld = (DefaultTableModel) tableOld.getModel();
        int zeileVon = Integer.valueOf(spinVon.getValue()+"");
        int zeileBis = Integer.valueOf(spinBis.getValue()+"");
        
        Integer moveToLine = null;
        String activeVal = modelOld.getValueAt(zeileVon, 2)+"";
        try{
            moveToLine = Integer.valueOf(activeVal);
        }catch(NumberFormatException nex){
            
        }
        
        Boolean isActive = null;
        if(moveToLine == null){
            isActive = !Boolean.valueOf(activeVal);
        }else{
            isActive = false;
        }
        
        Block b= new Block(zeileVon);
        b.zeileBis = zeileBis;
        b.isActive = true;
        
        if(isActive){
            this.dep.toggleBlocksMap.put(zeileVon,b);
        }else{
            this.dep.toggleBlocksMap.remove(zeileVon);
        }
        
        moveToLine = Integer.valueOf(spinMoveToLine.getValue() + "");
        
        for(int i=zeileVon;i<=zeileBis;i++){
            
            if(isActive){
                modelOld.setValueAt(moveToLine, i, 2);
            }else{
                modelOld.setValueAt(isActive+"", i, 2);
            }
            
            
            changeAcceptInResultingText(modelOld,i,2,this.dep.zeilenAlt,2,i);
        }
    }//GEN-LAST:event_btnEinbeziehenActionPerformed
    
    private File getSaveFile() throws IOException{
        File deployFile = new File(getPathAsSaving());
        //System.out.println(deployFile.getPath() + "");
        
        if(!deployFile.exists()){
            deployFile.getParentFile().mkdirs();
            deployFile.createNewFile();
        }
        
        return deployFile;
    }  
    
    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        try {
            
            // TODO add your handling code here:
            File deployFile = this.getSaveFile();
            FileInputStream fis = new FileInputStream(deployFile.getPath());
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.dep = (Deployment) ois.readObject();
            this.dep.createLineRenderer();
            
            this.putRows(tableOld,this.dep.zeilenAlt,deprecated_Start.tableOldNumber);
            this.putRows(tableNew,this.dep.zeilenNeu,deprecated_Start.tableNewNumber);
            this.putRows(tableCreated,this.dep.zeilenEntstanden,deprecated_Start.tableCreatedNumber);
            this.putRows(tblBlocks,this.dep.blocks,null);
            
            this.dep.transmitResult(txtOutput);
            
            saveButton.setEnabled(true);
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }//GEN-LAST:event_loadButtonActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        // TODO add your handling code here:
        DefaultTableModel modelOld = (DefaultTableModel) tableNew.getModel();
        int zeileVon = Integer.valueOf(spinVon.getValue()+"");
        int zeileBis = Integer.valueOf(spinBis.getValue()+"");
        
        Integer moveToLine = null;
        String activeVal = modelOld.getValueAt(zeileVon, 2)+"";
        try{
            moveToLine = Integer.valueOf(activeVal);
        }catch(NumberFormatException nex){
            
        }
        
        Boolean isActive = null;
        if(moveToLine == null){
            isActive = !Boolean.valueOf(activeVal);
        }else{
            isActive = false;
        }
        Block b= new Block(zeileVon);
        b.zeileBis = zeileBis;
        b.isActive = true;
        
        moveToLine = Integer.valueOf(spinMoveToLine.getValue() + "");
        
        for(int i=zeileVon;i<=zeileBis;i++){
            
            modelOld.setValueAt(isActive+"", i, 2);
            /*
            if(isActive){
                modelOld.setValueAt(moveToLine, i, 2);
            }else{
                
            }*/
            
            changeAcceptInResultingText(modelOld,i,2,this.dep.zeilenNeu,2,i);
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void txtSourcePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtSourcePropertyChange
        // TODO add your handling code here:
        
    }//GEN-LAST:event_txtSourcePropertyChange

    private void txtTargetPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtTargetPropertyChange
        // TODO add your handling code here:
        txtDeployTo.setText(txtTarget.getText());
    }//GEN-LAST:event_txtTargetPropertyChange

    private void txtTypeNamePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtTypeNamePropertyChange
        // TODO add your handling code here:
        
    }//GEN-LAST:event_txtTypeNamePropertyChange

    private void txtMetadataNamePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtMetadataNamePropertyChange
        // TODO add your handling code here:
        saveButton.setEnabled(false);
    }//GEN-LAST:event_txtMetadataNamePropertyChange

    private void txtDataEndingPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtDataEndingPropertyChange
        // TODO add your handling code here:
        
    }//GEN-LAST:event_txtDataEndingPropertyChange

    private void txtDirectoryOldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtDirectoryOldPropertyChange
        // TODO add your handling code here:
        
    }//GEN-LAST:event_txtDirectoryOldPropertyChange

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        try {
            // TODO add your handling code here:
            this.saveOptions();
            Functions.debugSwitchOn("jButton12ActionPerformed");
            Functions.debug("jButton12ActionPerformed", System.getProperty("user.dir"));
            txtDeployTo.setText(txtTarget.getText());
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton12ActionPerformed

    private void txtMetadataNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMetadataNameActionPerformed
        // TODO add your handling code here:
        saveButton.setEnabled(false);
    }//GEN-LAST:event_txtMetadataNameActionPerformed

    private void txtMetadataNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMetadataNameKeyPressed
        // TODO add your handling code here:
        saveButton.setEnabled(false);
    }//GEN-LAST:event_txtMetadataNameKeyPressed

    private void txtTypeNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTypeNameKeyPressed
        // TODO add your handling code here:
        saveButton.setEnabled(false);
    }//GEN-LAST:event_txtTypeNameKeyPressed

    private void txtTargetKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTargetKeyPressed
        // TODO add your handling code here:
        saveButton.setEnabled(false);
    }//GEN-LAST:event_txtTargetKeyPressed

    private void txtSourceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSourceKeyPressed
        // TODO add your handling code here:
        saveButton.setEnabled(false);
    }//GEN-LAST:event_txtSourceKeyPressed

    private void txtDataEndingKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDataEndingKeyPressed
        // TODO add your handling code here:
        saveButton.setEnabled(false);
    }//GEN-LAST:event_txtDataEndingKeyPressed

    private void txtDirectoryOldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDirectoryOldKeyPressed
        // TODO add your handling code here:
        saveButton.setEnabled(false);
    }//GEN-LAST:event_txtDirectoryOldKeyPressed

    private void cmbMetaDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbMetaDataActionPerformed
        // TODO add your handling code here:
        this.setOptions(cmbMetaData.getSelectedItem()+"", txtDataEnding, txtDirectoryOld, txtTypeName);
    }//GEN-LAST:event_cmbMetaDataActionPerformed

    private void cmdMetaDataNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMetaDataNameActionPerformed
        // TODO add your handling code here:
        if(didInit){
            txtMetadataName.setText(cmdMetaDataName.getSelectedItem() + "");
        }
    }//GEN-LAST:event_cmdMetaDataNameActionPerformed

    private void txtSubnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSubnameActionPerformed
        // TODO add your handling code here:
        saveButton.setEnabled(false);
    }//GEN-LAST:event_txtSubnameActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        SaveRightsThread st = new SaveRightsThread(
                tableDeployment,
                txtTargetData.getText(),
                progressSave
        );
        
        st.start();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
         // TODO add your handling code here:
        try {
            // TODO add your handling code here:
            deploy(true);
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton13ActionPerformed

    private void txtProjectFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProjectFolderActionPerformed
        // TODO add your handling code here:
        this.setFolders();
    }//GEN-LAST:event_txtProjectFolderActionPerformed
    
    private String getProjectName(){
        String projectName = txtProjectName.getText();
        if(projectName == null || projectName.length()==0){
            projectName = "\\" + projectName;
        }
        
        return projectName;
    }
    
    private void setFolders(){
        String projectName = getProjectName();
        deprecated_Start.saveFolder = new File(txtProjectFolder.getText() +  "\\" + projectName + "\\save\\");
        deprecated_Start.backup = new File(txtProjectFolder.getText() + "\\" + projectName);
    }
    
    private void txtProjectNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProjectNameActionPerformed
        // TODO add your handling code here:
        deprecated_Start.projectName = txtProjectName.getText();
        this.setFolders();
    }//GEN-LAST:event_txtProjectNameActionPerformed

    private void txtSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSourceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSourceActionPerformed

    private void tableNewVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_tableNewVetoableChange
        // TODO add your handling code here:

    }//GEN-LAST:event_tableNewVetoableChange

    private void tableNewPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tableNewPropertyChange
        // TODO add your handling code here:
        if(this.dep==null)return;
        deprecated_Start.changeAcceptInResultingText(tableNew,evt,this.dep.zeilenNeu,0);
    }//GEN-LAST:event_tableNewPropertyChange

    private void tableNewMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_tableNewMouseWheelMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_tableNewMouseWheelMoved

    private void txtBuildPropertiesPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtBuildPropertiesPropertyChange
        // TODO add your handling code here:
        buildProperties = txtBuildProperties.getText();
        
    }//GEN-LAST:event_txtBuildPropertiesPropertyChange

    private void txtProjectNamePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtProjectNamePropertyChange
        deprecated_Start.projectName = txtProjectName.getText();    // TODO add your handling code here:
    }//GEN-LAST:event_txtProjectNamePropertyChange
    
    private Process getImportProcess(){
        
        try {
            Functions.debugSwitchOn("btnXmlImportActionPerformed");
            Functions.debug("btnXmlImportActionPerformed", "buildFile: " + 
                    deprecated_Start.commandFolder.getPath() + "\\" + 
                    deprecated_Start.commandFolder.getPath()+ "\\" + 
                    txtProjectFolder.getText() +  "\\" + deprecated_Start.projectName);
            Functions.debug("btnXmlImportActionPerformed", "source: " + 
                    txtSource.getText());
            String projectPath = txtProjectFolder.getText().trim();
            String buildFilePath = deprecated_Start.commandFolder.getPath() +  "\\" + 
                    projectPath +  "\\" + deprecated_Start.projectName;
            if(projectPath.contains(":")){
                buildFilePath = projectPath;
            }
            File buildFile= createBuildFile(buildFilePath,
                    txtSource.getText(),"retrieve",
                    "retrieve","retrieve");
            Functions.debug("btnXmlImportActionPerformed", "xml Iimport file: " + 
                    txtXmlImport.getText());
            File packageFile = this.createPackageFile(
                    buildFilePath,
                    txtXmlImport.getText()
            );
            
            File commandFileRetrieve = createCommandFile(
                    buildFilePath,"retrieve",false);
            
            Functions.debug("btnXmlImportActionPerformed", 
                    buildFilePath);
            
            Process process = new ProcessBuilder(
                    commandFileRetrieve.getPath()).start();
            return process;
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    

    private void createAndExecutePackages(
            String buildFilePath,
            String methodName
    ){
        createAndExecutePackages(buildFilePath,methodName,false,null);
    }
    private void createAndExecutePackages(
            String buildFilePath,
            String methodName,
            Boolean isSFDXTool,
            String sfdxCommand
    ){
        // TODO add your handling code here:
        System.out.println("###createAndExecutePackages:::" + buildFilePath);
        
        try {
            String debugStr = "cmdRetrievePackageActionPerformed";
            Functions.debugSwitchOn(debugStr);
            Functions.debug(debugStr, "source: " + txtSource.getText());
            
            File buildFile = null;
            if(!isSFDXTool){
                buildFile = createBuildFile(
                    buildFilePath,
                    txtSource.getText(),"retrieve",
                    "retrieve",methodName,txtRetrievePackage.getText()
                );
            }
               
            File commandFileRetrieve = createCommandFile(
                    buildFilePath,
                    methodName,
                    false,
                    isSFDXTool,
                    sfdxCommand
            );
            
            Process process = new ProcessBuilder(
                    commandFileRetrieve.getPath())
                    .start();
            ///////
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    private Process getBackupProcess(){
        try {
            Functions.debugSwitchOn("btnBackupXMLActionPerformed");
            Functions.debug("btnBackupXMLActionPerformed", "buildFile: " + 
                    deprecated_Start.commandFolder.getPath() + "\\" + 
                    deprecated_Start.commandFolder.getPath()+ "\\" + 
                    txtProjectFolder.getText() +  "\\" + deprecated_Start.projectName);
            Functions.debug("btnBackupXMLActionPerformed", "source: " + 
                    txtTarget.getText());
            
            String projectPath = txtProjectFolder.getText().trim();
            String buildFilePath = deprecated_Start.commandFolder.getPath() +  "\\" + 
                    projectPath +  "\\" + deprecated_Start.projectName;
            if(projectPath.contains(":")){
                buildFilePath = projectPath;
            }
            File buildFile= createBuildFile(buildFilePath,
                    txtTarget.getText(),"retrieve",
                    "backup","backup");
            
            File packageFile = this.createPackageFile(
                    buildFilePath,
                    txtXmlImport.getText()
            );
            
            File commandFileRetrieve = createCommandFile(buildFilePath,"backup",false);
            
            Functions.debug("btnBackupXMLActionPerformed", 
                    buildFilePath);
            
            Process process = new 
                ProcessBuilder(commandFileRetrieve.getPath()).start();
            return process;
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    private void txtTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTargetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTargetActionPerformed

    private void txtXmlImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtXmlImportActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtXmlImportActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        // TODO add your handling code here:
        cmdRetrievePackageActionPerformed(null);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
        btnBackupActionPerformed(null);
    }//GEN-LAST:event_jButton14ActionPerformed

    private void btnBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackupActionPerformed

        try {
            // TODO add your handling code here:
            String packageName = txtRetrievePackage.getText().trim();
            String buildFilePath = deprecated_Start.commandFolder.getPath().trim() + "\\" +
            txtProjectFolder.getText() +  "\\" + packageName;

            String projectPath = txtProjectFolder.getText().trim();
            if(projectPath.contains(":")){
                buildFilePath = projectPath;
            }

            File buildFile= createBuildFile(
                buildFilePath,
                txtTarget.getText(),
                "retrieve",
                "backup",
                "backup"
            );
            File commandFileRetrieve = createCommandFile(
                buildFilePath ,
                "backup",
                false
            );

            String debugStr = "btnBackupActionPerformed";
            Functions.debugSwitchOn(debugStr);
            Functions.debug(debugStr, "target: " + txtTarget.getText());
            Functions.debug(debugStr, "buildFilePath: " + buildFilePath);

            File packageFile = this.createPackageFile(
                buildFilePath,
                buildFilePath + "\\retrieve\\package.xml"
            );
            Process process = new ProcessBuilder(commandFileRetrieve.getPath()).start();
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_btnBackupActionPerformed

    private void cmdRetrievePackageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRetrievePackageActionPerformed
        // TODO add your handling code here:
        String packageName = txtRetrievePackage.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = deprecated_Start.commandFolder.getPath() + "\\" + 
        projectPath +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        createAndExecutePackages(buildFilePath.trim(),"retrievePackage");
    }//GEN-LAST:event_cmdRetrievePackageActionPerformed

    private void btnBackupXMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackupXMLActionPerformed
        // TODO add your handling code here:
        getBackupProcess();
    }//GEN-LAST:event_btnBackupXMLActionPerformed

    private void btnXmlImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXmlImportActionPerformed
        // TODO add your handling code here:
        getImportProcess();
    }//GEN-LAST:event_btnXmlImportActionPerformed

    private void btnRetSFDXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRetSFDXActionPerformed
        // TODO add your handling code here:
        String packageName = txtRetrievePackage.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = deprecated_Start.commandFolder.getPath() + "\\" + 
        projectPath +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        String sourceName = txtSource.getText();
        Long millis = System.currentTimeMillis();
        
        String zipPath = buildFilePath + "\\" + millis + "\\unpackaged.zip";
        //buildFilePath = buildFilePath.trim();
        
        String command = "start sfdx force:mdapi:retrieve -u " 
                + sourceName 
                + " -r \"" + buildFilePath + "\"/" + millis + " -p \"" 
                + packageName + "\"";
        command += "\njar xf \"" + zipPath + "\"";
        System.out.println("command:::" + command);
        
        createAndExecutePackages(buildFilePath.trim(),
                "retrievePackage",
                true,
                command 
        );
        
        appendDeploymentInFile("retrieve:" + buildFilePath);
    }//GEN-LAST:event_btnRetSFDXActionPerformed

    private void btnUserRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUserRegisterActionPerformed
        // TODO add your handling code here:
        String sourceName = txtSource.getText();
        String loginURL = txtLoginURL.getText();
        
        if(loginURL.length()==0)loginURL = "https://test.salesforce.com";
        
        if(chkPROD.isSelected()){
            loginURL = "https://login.salesforce.com";
        }
        
        createAndExecutePackages(null,"retrievePackage",true,
                "start sfdx force:auth:web:login -a " + sourceName 
                        + " -r " + "\"" + loginURL +"\""
        );
    }//GEN-LAST:event_btnUserRegisterActionPerformed

    private void btnUserNamesListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUserNamesListActionPerformed
        // TODO add your handling code here:
        String packageName = txtRetrievePackage.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = deprecated_Start.commandFolder.getPath() + "\\" + 
        projectPath +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        String sourceName = txtSource.getText();
        Long millis = System.currentTimeMillis();
        
        String command = "start sfdx force:alias:list";
        
        createAndExecutePackages(buildFilePath.trim(),
                "retrievePackage",
                true,
                command 
        );
    }//GEN-LAST:event_btnUserNamesListActionPerformed

    private void backup(String targetName){
        /*String packageName = txtRetrievePackage.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = Start.commandFolder.getPath() + "\\" + 
        projectPath +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        
        System.out.println("Path::" + buildFilePath);
        
        File f = new File(buildFilePath);
        File[] fs = f.listFiles();
        String folderName = null;
        for(Integer i = fs.length - 1;i>-1;i--){
            File fi = fs[i];
            //System.out.println("fileName:::" + fi.getName());
            folderName = fi.getName();
            if(fi.isDirectory()){
                if(!folderName.contains("deploy")){
                    break;
                }
            }
        }
        
        System.out.println("folderName:::" + folderName);
        buildFilePath = buildFilePath + "\\" + folderName;
        
        String packagePath = buildFilePath + "\\" + packageName + "\\package.xml";
        
        Long millis = System.currentTimeMillis();
        
        String command = "start sfdx force:mdapi:retrieve -u " + targetName 
                        + " -r \"" + buildFilePath + "\\backup_" 
                + millis + "_" + targetName + "\" " 
                + "-k \"" + packagePath + "\"";
        
        System.out.println("command:::" + command);
        
        createAndExecutePackages(buildFilePath.trim(),
                "retrievePackage",
                true,
                command 
        );
        
        appendDeploymentInFile("backup:\"" + buildFilePath + "\"");*/
    }
    
    public void backup(){
        String targetName = txtTarget.getText();
        if(targetName != null && targetName.length() > 0)backup(targetName);
        
        String target2Name = textFieldZiel_2.getText();
        if(target2Name != null && target2Name.length() > 0)backup(target2Name);
    }
    
    private void btnRetBackupSFDXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRetBackupSFDXActionPerformed
        // TODO add your handling code here:
        //backup();
    }//GEN-LAST:event_btnRetBackupSFDXActionPerformed

    private void btnRetrieveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRetrieveActionPerformed
        // TODO add your handling code here:
        Process importProcess = getImportProcess();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
        Process backupProcess = getBackupProcess();
    }//GEN-LAST:event_btnRetrieveActionPerformed

    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        // TODO add your handling code here:
        String packageName = txtRetrievePackage.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = deprecated_Start.commandFolder.getPath() + "\\" + 
        projectPath +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        String sourceName = txtSource.getText();
        System.out.println("Path::" + buildFilePath);
        
        File f = new File(buildFilePath);
        File[] fs = f.listFiles();
        String folderName = null;
        for(Integer i = fs.length - 1;i>-1;i--){
            File fi = fs[i];
            //System.out.println("fileName:::" + fi.getName());
            
            if(fi.isDirectory()){
                folderName = fi.getName();
                    
                if(!folderName.contains("deploy")){
                    break;
                }
            }
        }
        
        System.out.println("folderName:::" + folderName);
        buildFilePath = buildFilePath + "\\" + folderName;
        
        String zipPath = buildFilePath + "\\unpackaged.zip";
        
        String command = "start jar xf \"" + zipPath + "\"";
        System.out.println("command:::" + command);
        
        createAndExecutePackages(buildFilePath.trim(),
                "retrievePackage",
                true,
                command 
        );
    }//GEN-LAST:event_jButton16ActionPerformed

    private void validate(String targetName){
        String packageName = txtRetrievePackage.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = deprecated_Start.commandFolder.getPath() + "\\" + 
        projectPath +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        
        Long millis = System.currentTimeMillis();
        
        String testLevel = comboTestLevel.getSelectedItem() + "";
        String retrieveFolder = txtRetrieveFolderName.getText();
        String zipPath = buildFilePath + "\\" + retrieveFolder 
                + "\\unpackaged.zip";
        String command = "start sfdx force:mdapi:deploy -u \"" 
                + targetName + "\" -c -f \"" + zipPath + "\" -l \"" + testLevel + "\""
                + " --loglevel \"debug\""
                ;
        
        System.out.println("command:::" + command);
        
        createAndExecutePackages(buildFilePath.trim(),
                "retrievePackage",
                true,
                command 
        );
        
        appendDeploymentInFile("validate:" + txtRetrieveFolderName.getText());
    }
    
    private void btnValDepSFDXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnValDepSFDXActionPerformed
        // TODO add your handling code here:
        String targetName = txtTarget.getText();
        if(targetName != null)validate(targetName);
        
        String target2Name = textFieldZiel_2.getText();
        if(target2Name != null)validate(target2Name);
    }//GEN-LAST:event_btnValDepSFDXActionPerformed
    
    public void appendDeploymentInFile(String appendix) {
        appendDeploymentInFile(appendix,null);
    }
    
    private void appendDeploymentInFile(
            File deploymentFile,
            File mapFile,
            String appendix
    ) 
            throws IOException, 
            FileNotFoundException, 
            ClassNotFoundException
    {
        String fieldNames = "Datum, Zeit, Nummer, Quelle von, Ziel nach, " 
                + "Changset_Name, File Path, Projekt_Ordner, Deployment Id";
        String firstField = "Datum";
        
        if(!deploymentFile.exists())deploymentFile.createNewFile();
            FileWriter fw = new FileWriter(deploymentFile);
            BufferedWriter bw = new BufferedWriter(fw);
            
            FileReader fr = new FileReader(deploymentFile);
            BufferedReader br = new BufferedReader(fr);
            
            Map<String,String>loading = null;
            String newContent = fieldNames;
            if(mapFile.exists())loading = 
                (Map<String,String>) Routines.loadObject(mapFile);
            if(loading != null){
                for(String str:loading.values()){
                    if(str.startsWith(firstField))continue;
                    newContent += "\n";
                    newContent += str;
                }
            }else{
                loading = new HashMap<String,String>();
                newContent += fieldNames + "\n";
                loading.put("0",fieldNames);
            }
            String newLoadingVal = LocalDate.now() 
                    + ", " 
                    + LocalTime.now() 
                    + ", " 
                    + appendix
                    + ","
                    + txtSource.getText()
                    + ","
                    + txtTarget.getText()
                    + ",\""
                    + txtRetrievePackage.getText() + "\""
                    + ",\""
                    + txtXmlImport.getText() + "\""
                    + ",\""
                    + txtProjectFolder.getText() + "\""
                    + ","
                    ;
            
            loading.put(loading.size() + "",newLoadingVal);
            newContent += "\n";
            newContent += newLoadingVal;
            System.out.println("END appendDeploymentInFile::" + newContent);
            bw.write(newContent);
            bw.close();
            if(mapFile.exists())mapFile.delete();
            mapFile.createNewFile();
            Routines.saveObject(mapFile, (Serializable) loading);
    }
    
    public void appendDeploymentInFile(
            String appendix, 
            String additionalFilePath
    ) {
        try {
            String fileName = txtProjectFolder.getText() 
                    + "\\deployments.txt";
            String mapFileName = txtProjectFolder.getText() 
                    + "\\deployments_map.txt";
            
            File deploymentFile = new File(fileName);
            File mapFile = new File(mapFileName);
            
            appendDeploymentInFile(deploymentFile,mapFile,appendix);
            
            File ALL_deploymentFile = new File("\\ALL_deployments.txt");
            File ALL_mapFile = new File("\\ALL_deployments_map.txt");
            
            appendDeploymentInFile(
                    ALL_deploymentFile
                    ,ALL_mapFile
                    ,appendix);
            System.out.println("All_Files Path::" 
                    + ALL_deploymentFile.getAbsolutePath());
        } catch (IOException ex){
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void btnRetrieveSFDXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRetrieveSFDXActionPerformed
        // TODO add your handling code here:
        String xmlPath = txtXmlImport.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = deprecated_Start.commandFolder.getPath() + "\\" + 
        projectPath +  "\\";
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        String sourceName = txtSource.getText();
        Long millis = System.currentTimeMillis();
        
        String zipPath = buildFilePath + "\\" + millis + "\\unpackaged.zip";
        String command = "start sfdx force:mdapi:retrieve -u " + sourceName 
                        + " -r ./" + millis + " -k \"" + xmlPath + "\"";
        System.out.println("command:::" + command);
        
        createAndExecutePackages(buildFilePath.trim(),
                "retrievePackage",
                true,
                command 
        );
    }//GEN-LAST:event_btnRetrieveSFDXActionPerformed

    private void deploy(String targetName){
        String packageName = txtRetrievePackage.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = deprecated_Start.commandFolder.getPath() + "\\" + 
        projectPath +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        
        Long millis = System.currentTimeMillis();
        
        String testLevel = comboTestLevel.getSelectedItem() + "";
        String retrieveFolder = txtRetrieveFolderName.getText();
        String zipPath = buildFilePath + "\\" + retrieveFolder 
                + "\\unpackaged.zip";
        String command = "start sfdx force:mdapi:deploy -u \"" 
                + targetName + "\" -f \"" + zipPath + "\" -l \"" + testLevel + "\" "
                + " --loglevel \"debug\""
                ;
        
        
        System.out.println("command:::" + command);
        
        createAndExecutePackages(buildFilePath.trim(),
                "retrievePackage",
                true,
                command 
        );
        appendDeploymentInFile("deploy:" + txtRetrieveFolderName.getText());
    }
    
    public void deploy(){
        String targetName = txtTarget.getText();
        if(targetName != null && targetName.length() > 0)deploy(targetName);
        
        String target2Name = textFieldZiel_2.getText();
        if(target2Name != null && target2Name.length() > 0)deploy(target2Name);
    }
    
    private void btn_DEPLOYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_DEPLOYActionPerformed
        // TODO add your handling code here:
        deploy();
    }//GEN-LAST:event_btn_DEPLOYActionPerformed

    private void btnFnamesPackageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFnamesPackageActionPerformed
        // TODO add your handling code here:
        String objName = txtObjName.getText();
        String sourceFieldNames = txtAreaFieldNames.getText();
        String outputFieldNamesText = "";
        
        String [] sourceLines = sourceFieldNames.split("\n");
        
        for(String sourceL:sourceLines){
            if(!sourceL.contains("__c")){
                continue;
            }
            String [] customFields = sourceL.split("__c");
            String [] fieldNames = customFields[0].split("\\t");
            String fieldName = fieldNames[1] + "__c";
            if(outputFieldNamesText.length() > 0){
                outputFieldNamesText += "\n";
            }
            outputFieldNamesText += 
                    "<members>" + objName + "." + fieldName  
                    + "</members>";
        }
        
        if(chkZwischenspeicher.isSelected()){
            try
	{
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(outputFieldNamesText);
		cb.setContents(stringSelection, stringSelection);
            }
            catch(Exception exc){}
        }
        txtAreaOutputFIeldsPackage.setText(outputFieldNamesText);
    }//GEN-LAST:event_btnFnamesPackageActionPerformed

    private void btnValDepSFDX1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnValDepSFDX1ActionPerformed
        // TODO add your handling code here:
        String packageName = txtSource.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = deprecated_Start.commandFolder.getPath() + "\\" + 
        projectPath +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        String targetName = txtSource.getText();
        Long millis = System.currentTimeMillis();
        
        String testLevel = comboTestLevel.getSelectedItem() + "";
        String retrieveFolder = txtRetrieveFolderName.getText();
        
        File retrieveFile = new File(buildFilePath + "\\" + retrieveFolder);
        File[] filesList = retrieveFile.listFiles();
        File backupFile = null;
        
        String backupName = null;
        for(Integer i=filesList.length - 1; i>=0;i--){
            File f = filesList[i];
            backupName = f.getName();
            if(backupName.toLowerCase().startsWith("backup")){
                break;
            }
        }
        
        System.out.println("backupName::" + backupName);
        
        String zipPath = buildFilePath + "\\" + retrieveFolder  
                + "\\" + backupName
                + "\\unpackaged.zip";
        String command = "start sfdx force:mdapi:deploy -u \"" 
                + targetName + "\" -c -f \"" + zipPath + "\" -l \"" + testLevel + "\""
                + " --loglevel \"debug\""
                ;
        
        createAndExecutePackages(buildFilePath.trim(),
                "retrievePackage",
                true,
                command 
        );
    }//GEN-LAST:event_btnValDepSFDX1ActionPerformed

    private void btnBackupANDDeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackupANDDeployActionPerformed
        // TODO add your handling code here:
        backup();
        deploy();
    }//GEN-LAST:event_btnBackupANDDeployActionPerformed

    public void setOptions(
            String type,
            JTextField txtDataEnding,
            JTextField txtDirectoryOld,
            JTextField txtTypeName
    ){
        if(typeClass.equals(type)){
            txtDataEnding.setText("cls");
            txtDirectoryOld.setText("classes");
            txtTypeName.setText("ApexClass");
        }else if(typeObject.equals(type)){
            txtDataEnding.setText("object");
            txtDirectoryOld.setText("objects");
            txtTypeName.setText("CustomObject");
        }else if(typePage.equals(type)){
            txtDataEnding.setText("page");
            txtDirectoryOld.setText("pages");
            txtTypeName.setText("ApexPage");
        }else if("AuraBundle-app".equals(type)){
            txtDataEnding.setText("app");
            txtDirectoryOld.setText("aura");
            txtTypeName.setText("AuraDefinitionBundle");
        }else if("AuraBundle-css".equals(type)){
            txtDataEnding.setText("css");
            txtDirectoryOld.setText("aura");
            txtTypeName.setText("AuraDefinitionBundle");
        }else if("AuraBundle-controller".equals(type)){
            txtDataEnding.setText("js");
            txtDirectoryOld.setText("aura");
            txtTypeName.setText("AuraDefinitionBundle");
        }else if("AuraBundle-helper".equals(type)){
            txtDataEnding.setText("js");
            txtDirectoryOld.setText("aura");
            txtTypeName.setText("AuraDefinitionBundle");
        }else if("AuraBundle-doc".equals(type)){
            txtDataEnding.setText("auradoc");
            txtDirectoryOld.setText("aura");
            txtTypeName.setText("AuraDefinitionBundle");
        }else if("AuraBundle-cmp".equals(type)){
            txtDataEnding.setText("cmp");
            txtDirectoryOld.setText("aura");
            txtTypeName.setText("AuraDefinitionBundle");
        }else if("AuraBundle-svg".equals(type)){
            txtDataEnding.setText("svg");
            txtDirectoryOld.setText("aura");
            txtTypeName.setText("AuraDefinitionBundle");
        }else if("AuraBundle-renderer".equals(type)){
            txtDataEnding.setText("js");
            txtDirectoryOld.setText("aura");
            txtTypeName.setText("AuraDefinitionBundle");
        }else if("Custom-Metadata-Type".equals(type)){
            txtDataEnding.setText("object");
            txtDirectoryOld.setText("objects");
            txtTypeName.setText("CustomObject");
        }
        
        this.opts.chosenType = type;
    }                                        
    
    public static void changeAcceptInResultingText(
            DefaultTableModel model,
            int row,
            int column,
            List<Deployment.LineWrapper> wrapperList,
            int wrapRowColumn,
            Integer wrapRow
    ){
        if(row>=0){
            if(wrapRow == null){
                wrapRow = Integer.valueOf(model.getValueAt(row, wrapRowColumn)+"");
            }
            if(column>=0){
                Deployment.LineWrapper lw = null;
                for(Deployment.LineWrapper liWrap:wrapperList){
                    String wrapZeile = liWrap.getZeile()+"";
                    String rowWrap = wrapRow+"";
                    
                    if(rowWrap.equals(wrapZeile)){
                        lw = liWrap;
                        break;
                    }
                }
                String val = model.getValueAt(row, column)+"";
                
                if(lw!=null){
                    if("true".equals(val)){
                        lw.setAcceptInResultingText(true);
                    }else{
                        Integer intVal = null;
                        try{
                            intVal = Integer.valueOf(val);
                        }catch(Exception ex){
                            
                        }
                        
                        if(intVal != null){
                            lw.setAcceptInResultingText(true);
                            lw.setInsertOnLine(val);
                        }else{
                            lw.setAcceptInResultingText(false);
                        }
                    }
                }
            }
        }
    }
    
    public static void changeAcceptInResultingText(
            JTable table,
            java.beans.PropertyChangeEvent evt,
            List<Deployment.LineWrapper> wrapperList,
            int wrapRowColumn
            
    ){
       
        Map<String,String> propMap = getPropertyMapFromSource(evt.getSource()+"");
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        
        int column = Integer.valueOf(propMap.get("editingColumn"));
        
        if(propMap.containsKey("editingRow")){
            int row = Integer.valueOf(propMap.get("editingRow"));
            changeAcceptInResultingText(model,row,column,wrapperList,wrapRowColumn,null);
        }
    }
    
    public static Map<String,String> getPropertyMapFromSource(
            String sourceString
    ){
        Map<String,String> result = new HashMap<String,String>();
        
        for(String str:sourceString.split(",")){
            String[] propVal = str.split("=");
            if(propVal.length>1)result.put(propVal[0], propVal[1]);
        }
        
        return result;
    }
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
            java.util.logging.Logger.getLogger(deprecated_Start.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(deprecated_Start.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(deprecated_Start.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(deprecated_Start.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
           
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    deprecated_Start st = new deprecated_Start();
                    st.setVisible(true);
                    st.txtDeployTo.setText(st.txtTarget.getText());
                } catch (IOException ex) {
                    Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        });
        
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackup;
    private javax.swing.JButton btnBackupANDDeploy;
    private javax.swing.JToggleButton btnBackupXML;
    private javax.swing.JButton btnDeploy;
    private javax.swing.JButton btnEinbeziehen;
    private javax.swing.JButton btnFnamesPackage;
    private javax.swing.JButton btnRetBackupSFDX;
    private javax.swing.JButton btnRetSFDX;
    private javax.swing.JButton btnRetrieve;
    private javax.swing.JButton btnRetrieveSFDX;
    private javax.swing.JButton btnUserNamesList;
    private javax.swing.JButton btnUserRegister;
    private javax.swing.JButton btnValDepSFDX;
    private javax.swing.JButton btnValDepSFDX1;
    private javax.swing.JToggleButton btnXmlImport;
    private javax.swing.JButton btn_DEPLOY;
    private javax.swing.JCheckBox chkChanged;
    private javax.swing.JCheckBox chkExisting;
    private javax.swing.JCheckBox chkMoved;
    private javax.swing.JCheckBox chkPROD;
    private javax.swing.JCheckBox chkRemoved;
    private javax.swing.JCheckBox chkZwischenspeicher;
    private javax.swing.JComboBox<String> cmbMetaData;
    private javax.swing.JComboBox<String> cmdMetaDataName;
    private javax.swing.JButton cmdRetrievePackage;
    private javax.swing.JComboBox<String> comboTestLevel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jChkAusblenden;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelDokumente;
    private javax.swing.JPanel jPanelResultat;
    private javax.swing.JPanel jPanelSystemInformation;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JTabbedPane jTabbedPane5;
    private javax.swing.JTabbedPane jTabbedPane6;
    private javax.swing.JTabbedPane jTabbedPane7;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JLabel lblPackage;
    private javax.swing.JLabel lblXmlImport;
    private javax.swing.JButton loadButton;
    private javax.swing.JButton loadDocuments;
    private javax.swing.JList<String> lstTestKlassen;
    private javax.swing.JProgressBar progressSave;
    private javax.swing.JButton saveButton;
    private javax.swing.JSpinner spinBis;
    private javax.swing.JSpinner spinMinValChanged;
    private javax.swing.JSpinner spinMoveToLine;
    private javax.swing.JSpinner spinVon;
    private javax.swing.JTable tableCreated;
    private javax.swing.JTable tableDeployment;
    private javax.swing.JTable tableNew;
    private javax.swing.JTable tableOld;
    private javax.swing.JTable tblBlocks;
    private javax.swing.JTextField textFieldZiel_2;
    private javax.swing.JTextArea txtAreaFieldNames;
    private javax.swing.JTextArea txtAreaOutputFIeldsPackage;
    private javax.swing.JTextField txtBuildProperties;
    private javax.swing.JTextField txtDataEnding;
    private javax.swing.JTextField txtDeployTo;
    private javax.swing.JTextField txtDirectoryOld;
    private javax.swing.JTextField txtLoginURL;
    private javax.swing.JTextField txtMetadataName;
    private javax.swing.JTextField txtObjName;
    private javax.swing.JTextArea txtOutput;
    private javax.swing.JTextField txtProjectFolder;
    private javax.swing.JTextField txtProjectName;
    private javax.swing.JTextField txtRecentVal;
    private javax.swing.JTextField txtRetrieveFolderName;
    private javax.swing.JTextField txtRetrievePackage;
    private javax.swing.JTextField txtSource;
    private javax.swing.JTextField txtSubname;
    private javax.swing.JTextField txtTarget;
    private javax.swing.JTextField txtTargetData;
    private javax.swing.JTextField txtTestklasse;
    private javax.swing.JTextField txtTypeName;
    private javax.swing.JTextField txtUserName;
    private javax.swing.JTextField txtXmlImport;
    // End of variables declaration//GEN-END:variables
}
