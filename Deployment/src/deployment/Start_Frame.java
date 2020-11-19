/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment;

import General.Changeset;
import General.Functions;
import General.Options;
import General.Routines;
import static General.Routines.firstGeneralTag;
import static General.Routines.regexXMLelement;
import deployment.Deployment_Classes.SaveDeployment;
import deployment.Deployment_Classes.Package;
import deployment.Deployment_Classes.XMLDatenbank;
import deployment.Deployment_Classes.XMLNode;
import deployment.Deployment_Classes.XMLtag;
import static deployment.deprecated_Start.antFile;
import static deployment.deprecated_Start.commandFolder;
import static deployment.deprecated_Start.device;
import static deployment.deprecated_Start.readFile;
import static deployment.deprecated_Start.typeClass;
import static deployment.deprecated_Start.typeObject;
import static deployment.deprecated_Start.typePage;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import static junit.framework.Assert.assertNotNull;

/**
 *
 * @author Christoph Lieske
 */
public class Start_Frame extends javax.swing.JFrame {
    
    /*VARIABLES*/
    public static String buildProperties;
    private Boolean didInit = false;
    public static Options opts;
    private SaveDeployment saveDep;
    
    /*METHODS*/
    
    private void appendDeploymentInFile(String appendix) 
            throws IOException, 
                FileNotFoundException, 
                ClassNotFoundException{
        FileHandling.appendDeploymentInFile(appendix,txtProjectFolder,txtSource,txtTarget,txtRetrievePackage,txtXmlImport);
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
    
    public void backup() 
            throws IOException, FileNotFoundException, ClassNotFoundException{
        String deployZielGruppe = txtDeployZielGruppe.getText();
        HashSet<String> set = this.opts.fromTargetNameToGroupNameSet.get(deployZielGruppe);
        String targetName = txtTarget.getText();
        Long fileNumber = System.currentTimeMillis();
        if(targetName != null && targetName.length() > 0){
            if(set != null && !set.contains(targetName) || set == null)backup(targetName,fileNumber);
        }
        
        String target2Name = textFieldZiel_2.getText();
        if(target2Name != null && target2Name.length() > 0){
            if(set != null && !set.contains(target2Name) || set == null)backup(target2Name,fileNumber);
        }
        
        Integer i = 0;
        if(set != null){
            System.out.println("###deployZielGruppe::" + deployZielGruppe);
            for(String zielName:set){
                i++;
                System.out.println("###backup out of set----zielName::" + zielName + "--" + i);
                sleep();
                backup(zielName,fileNumber);
            }
        }
    }
    
    private void sleep(){
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void backup(String targetName,Long fileNumber) 
            throws IOException, FileNotFoundException, ClassNotFoundException{
        String packageName = txtRetrievePackage.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = commandFolder.getPath() 
                + "\\" 
                + projectPath 
                +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        
        File f = new File(buildFilePath);
        File[] fs = f.listFiles();
        String folderName = null;
        System.out.println("###buildFilePath:" + buildFilePath);
        System.out.println("###fs:" + fs);
        for(Integer i = fs.length - 1;i>-1;i--){
            File fi = fs[i];
            folderName = fi.getName();
            if(fi.isDirectory()){
                if(!folderName.contains("deploy")){
                    break;
                }
            }
        }
        
        buildFilePath = buildFilePath 
                + "\\" + folderName;
        
        String packagePath = buildFilePath + "\\" + packageName + "\\package.xml";
        
        buildFilePath += "\\" + fileNumber;
        f = new File(buildFilePath);
        f.mkdirs();
        Long millis = System.currentTimeMillis();
        
        String command = "start sfdx force:mdapi:retrieve -u " + targetName 
                        + " -r \"" + buildFilePath + "\\backup_" 
                + millis + "_" + targetName + "\" " 
                + "-k \"" + packagePath + "\"";
        
        createAndExecutePackages(buildFilePath.trim(),
                "retrievePackage",
                true,
                command,
                txtSource.getText(),
                txtRetrievePackage.getText()
        );
        
        appendDeploymentInFile("backup:" + buildFilePath + "");
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
    
    public static void createAndExecutePackages(
            String buildFilePath,
            String methodName,
            String txtSource,
            String txtRetrievePackage
    ){
        createAndExecutePackages(buildFilePath,methodName,
                false,null,txtSource,txtRetrievePackage
        );
    }
    public static void createAndExecutePackages(
            String buildFilePath,
            String methodName,
            Boolean isSFDXTool,
            String sfdxCommand,
            String txtSource,
            String txtRetrievePackage
    ){
        // TODO add your handling code here:
        //System.out.println("###createAndExecutePackages:::" + buildFilePath);
        
        try {
            String debugStr = "cmdRetrievePackageActionPerformed";
            Functions.debugSwitchOn(debugStr);
            //Functions.debug(debugStr, "source: " + txtSource.getText());
            
            File buildFile = null;
            if(!isSFDXTool){
                buildFile = createBuildFile(
                    buildFilePath,
                    txtSource,"retrieve",
                    "retrieve",methodName,txtRetrievePackage
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
    
    private static File createBuildFile(
            String path,
            String name
    ) throws IOException{
        return createBuildFile(path,name,"retrieve","retrieve");
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

            //System.out.println("pfad: " + sb.toString());
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
        
        writeProperty(sb,"username",targetServer);
        writeProperty(sb,"password",targetServer);
        writeProperty(sb,"serverurl",targetServer);
        
        if(isRecentValConform){
            writeFixedProperty(sb, "recentValidationId", recentValId);
            sb.append(">\n");
        }else{
            //sb.append("deployroot=\"" + deployRoot + "\\" + folder + "\"");
            
            //deploy the last created ZipFile
            File allFiles = new File(deployRoot);
            //System.out.println("###deployRoot: " + deployRoot);
            
            String retrieveZipFilePath = null;
            
            if(retrieveFolderName==null){
                for(String fileName : allFiles.list()){
                    //System.out.println("###fileName: " + fileName);
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
                //System.out.println("###zipFolder files: " + fileName);
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
        
        buildFile = getNextValidFile(path,fileName,ending);
       
        /*if(buildFile.exists()){
            if(deleteAfterCreate) buildFile.delete();
            else buildFile.renameTo(f);
        }
        else buildFile = f;*/
        
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
        fw.close();
        
        return buildFile;
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
    
    private static File createXmlFile(
            String path,
            String fileName,
            String fileContent
    ) throws IOException{
        return createFile(path,fileName,fileContent,"xml",false);
    }
    
    private static void deleteListenElement(
            JList lst
    ){
        int selInd = lst.getSelectedIndex();
        
        if(selInd>-1){
            DefaultListModel dlm = (DefaultListModel) lst.getModel();
            dlm.remove(lst.getSelectedIndex());
        }
    }
    
    public void deploy() throws IOException, FileNotFoundException, ClassNotFoundException{
        DeploymentThread deployThread = new DeploymentThread(
                txtDeployZielGruppe.getText()
                , this.opts.fromTargetNameToGroupNameSet
                ,txtTarget
                ,textFieldZiel_2
                ,txtRetrievePackage
                ,txtProjectFolder
                ,comboTestLevel
                ,txtRetrieveFolderName
                ,lstTestKlassen
                ,txtSource
                ,txtXmlImport
        );
        deployThread.start();
    }
    
    
    
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
            createDeploymentFiles(buildFilePath
                ,txtTarget.getText(),pck,
                testClasses,true,runTests,txtRecentVal.getText(),buildFilePath,
                testLevel,
                txtRetrieveFolderName.getText()
            );
        }else{
            createDeploymentFiles(buildFilePath,txtTarget.getText(),
                    xmlImportPath,
                testClasses,true,runTests,txtRecentVal.getText(),"",
                testLevel,
                txtRetrieveFolderName.getText()
            );
        
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
    
    private String getProjectName(){
        String projectName = txtProjectName.getText();
        if(projectName == null || projectName.length()==0){
            projectName = "\\" + projectName;
        }
        
        return projectName;
    }
    
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
    
    private void init() throws IOException, FileNotFoundException, ClassNotFoundException{
        
        Functions func = new Functions();
        Functions.debugSwitchOn("Functions.findFile");
        Functions.debugSwitchOff("Functions.findFile");
        
        antFile = func.findFile("ant-salesforce.jar");
        
        Functions.debugSwitchOn("FileFound");
        Functions.debug("FileFound", "file: " + antFile);
        this.opts = Options.loadOptions();
        
        if(this.opts != null){
            comboTestLevel.setSelectedItem(this.opts.testLevel);
            txtBuildProperties.setText(this.opts.buildPropertiesPath);
            txtMetadataName.setText(this.opts.actualMetaName);
            txtTypeName.setText(this.opts.actualTypeName);
            txtTarget.setText(this.opts.actualTarget);
            txtSource.setText(this.opts.actualSource);
            txtDataEnding.setText(this.opts.actualEnding);
            txtDeployZielGruppe.setText(this.opts.zielDeployGruppe);
            txtDirectoryOld.setText(this.opts.actualFolder);
            txtAreaFieldNames.setText(this.opts.inputTextString);
            txtSubname.setText(this.opts.subname);
            txtDeploymentsVereinen.setText(this.opts.oldDeploymentPath);
            txtPackageName.setText(this.opts.oldDeploymentPackageName);
            txtProjectFolder.setText(this.opts.projectFolder);
            txtProjectName.setText(this.opts.projectName);
            txtRetrievePackage.setText(this.opts.packageName);
            deprecated_Start.projectName = txtProjectName.getText();
            cmbMetaData.setSelectedItem(this.opts.chosenType);
            txtXmlImport.setText(this.opts.xmlImportPath);
            txtZielGruppe.setText(this.opts.zielGruppe);
            txtZusatzZiel.setText(this.opts.zusatzziel);
            txtRetrieveFolderName.setText(this.opts.retrieveFolderName);
            
            DefaultTableModel tModel = (DefaultTableModel)tblZiel.getModel();
            cmbBoxZielgruppe.removeAllItems();
            List<String> sortedGruppenList = new ArrayList();
            
            for(String zielGruppe:this.opts.fromTargetNameToGroupNameSet.keySet()){
                sortedGruppenList.add(zielGruppe);
                HashSet<String> groupNameSet = this.opts.fromTargetNameToGroupNameSet.get(zielGruppe);
                for(String zielName:groupNameSet){
                    tModel.addRow(new Object[0]);
                    tModel.setValueAt(zielGruppe, tModel.getRowCount()-1, 0);
                    tModel.setValueAt(zielName, tModel.getRowCount()-1, 1);
                }
            }
            Collections.sort(sortedGruppenList);
            for(String zielGruppe:sortedGruppenList){
                cmbBoxZielgruppe.addItem(zielGruppe);
            }
            
            DefaultTableModel tModelChangeset = (DefaultTableModel)tblChangesets.getModel();
            for(String zielGruppe:this.opts.fromTargetNameToChangesetGroupNameSet.keySet()){
                sortedGruppenList.add(zielGruppe);
                HashSet<String> groupNameSet = this.opts.fromTargetNameToChangesetGroupNameSet.get(zielGruppe);
                for(String zielName:groupNameSet){
                    tModelChangeset.addRow(new Object[0]);
                    tModelChangeset.setValueAt(zielGruppe, tModelChangeset.getRowCount()-1, 0);
                    tModelChangeset.setValueAt(zielName, tModelChangeset.getRowCount()-1, 1);
                }
            }
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
        
        for( SaveDeployment.DeploymentMeta dm:this.saveDep.deploymentMeta){
            cmdMetaDataName.addItem(dm.metaName);
        }
        
        device.setFullScreenWindow(this);
        btnProjektTakeOverActionPerformed(null);
        didInit = true;
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
    
    private void saveOptions() throws IOException{
        if(this.opts == null)this.opts = new Options();
             
        this.opts.actualMetaName = txtMetadataName.getText();
        this.opts.actualTypeName = txtTypeName.getText();
        this.opts.actualTarget = txtTarget.getText();
        this.opts.actualSource = txtSource.getText();
        this.opts.actualEnding = txtDataEnding.getText();
        this.opts.actualFolder = txtDirectoryOld.getText();
        this.opts.buildPropertiesPath = txtBuildProperties.getText();
        this.opts.inputTextString = txtAreaFieldNames.getText();
        this.opts.packageName = txtRetrievePackage.getText();
        this.opts.projectFolder = txtProjectFolder.getText();
        this.opts.projectName = txtProjectName.getText();
        this.opts.retrieveFolderName = txtRetrieveFolderName.getText();
        this.opts.subname = txtSubname.getText();
        this.opts.testLevel = comboTestLevel.getSelectedItem() + "";
        this.opts.xmlImportPath = txtXmlImport.getText();
        this.opts.zielGruppe = txtZielGruppe.getText();
        this.opts.zielDeployGruppe = txtDeployZielGruppe.getText();
        this.opts.zusatzziel = txtZusatzZiel.getText();
        
        this.opts.oldDeploymentPackageName = txtPackageName.getText();
        this.opts.oldDeploymentPath = txtDeploymentsVereinen.getText();
    
        this.opts.saveOptions();
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
    
    private void setFolders(){
        String projectName = getProjectName();
        deprecated_Start.saveFolder = new File(txtProjectFolder.getText() +  "\\" + projectName + "\\save\\");
        deprecated_Start.backup = new File(txtProjectFolder.getText() + "\\" + projectName);
    }
    
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
   
    
    private void validate(String targetName) 
            throws IOException, FileNotFoundException, ClassNotFoundException{
        sleep();
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
                command,
                txtSource.getText(),
                txtRetrievePackage.getText() 
        );
        
        appendDeploymentInFile("validate:" + txtRetrieveFolderName.getText());
    }
    
    private static void writeAccessProperties(
            StringBuilder sb,
            String name
    ) throws IOException{
        Start_Frame.writeProperty(sb,"username",name);
        Start_Frame.writeProperty(sb,"password",name);
        Start_Frame.writeProperty(sb,"serverurl",name);
        Start_Frame.writeProperty(sb,"maxPoll",name);
    }
    
    private static void writeProperty(
            StringBuilder sb,
            String property,
            String value
    ) throws IOException{
        
        sb.append(property + "=\"${" + value + "." + property + "}\"\n");
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
            Start_Frame.writeFixedProperty(sb,"packageNames",packageName);
        }else{
            Start_Frame.writeFixedProperty(sb,"unpackaged","package.xml");
        }
        //@id: 1 END
        
        Start_Frame.writeFixedProperty(sb,"unzip","false");
        Start_Frame.writeFixedProperty(sb,"retrieveTarget",directoryName + "_" + millis);
    }
    
    private static void writeFixedProperty(
            StringBuilder sb,
            String property,
            String value
    ) throws IOException{
        sb.append(property + "=\"" + value + "\"\n");
    }
    
    private static void writeProperty(
            BufferedWriter bw,
            String property,
            String value
    ) throws IOException{
        
        bw.write(property + "=\"${" + value + "." + property + "}\"\n");
    }
    
    /**
     * Creates new form Start_Frame
     */
    public Start_Frame() throws IOException, FileNotFoundException, ClassNotFoundException {
        System.out.println("###Start_Frame");
        initComponents();
        this.init();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelSystemInformation = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
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
        jScrollPane7 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jPanel17 = new javax.swing.JPanel();
        btn_DEPLOY = new javax.swing.JButton();
        btnBackupANDDeploy = new javax.swing.JButton();
        jLabel34 = new javax.swing.JLabel();
        chkZielgruppe = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        txtUserName = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        txtObjName = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        txtAreaOutputFIeldsPackage = new javax.swing.JTextArea();
        btnFnamesPackage = new javax.swing.JButton();
        chkZwischenspeicher = new javax.swing.JCheckBox();
        btnClosingXMLelement = new javax.swing.JButton();
        txtClosingXMLelement = new javax.swing.JTextField();
        lblLineOfCode = new javax.swing.JLabel();
        btnShowLines = new javax.swing.JButton();
        jTabPnDeployment = new javax.swing.JTabbedPane();
        jScrollPane10 = new javax.swing.JScrollPane();
        txtAreaFieldNames = new javax.swing.JTextArea();
        jPanel9 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel10 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        btmWellFormedVFP = new javax.swing.JButton();
        btnPutNewLinesText = new javax.swing.JButton();
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
        jTabRetrieve = new javax.swing.JTabbedPane();
        jPanelBasisfunktionen = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        txtDataEnding = new javax.swing.JTextField();
        btnUserRegister = new javax.swing.JButton();
        chkPROD = new javax.swing.JCheckBox();
        jLabel27 = new javax.swing.JLabel();
        txtLoginURL = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtDirectoryOld = new javax.swing.JTextField();
        btnUserNamesList = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtTypeName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtMetadataName = new javax.swing.JTextField();
        txtSubname = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        textFieldZiel_2 = new javax.swing.JTextField();
        txtUsernameRegister = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        jPanelProjektdaten = new javax.swing.JPanel();
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
        btnCloneObj = new javax.swing.JButton();
        txtNewObjectName = new javax.swing.JTextField();
        chkBoxBuildSourceIntoFolder = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        btnUnzipOLD = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        txtDeploymentsVereinen = new javax.swing.JTextField();
        txtPackageName = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        btnDeploymentsVereinen = new javax.swing.JButton();
        jPanelChangesets = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblChangesets = new javax.swing.JTable();
        btnRetrieveChangeset = new javax.swing.JButton();
        jTextFieldRetrieveChangesetQuelle = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        btnRetrieveGroups = new javax.swing.JButton();
        jLabel38 = new javax.swing.JLabel();
        jTextFieldRetrieveGroup = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        jTextFieldXmlImportChangeset = new javax.swing.JTextField();
        jLabel40 = new javax.swing.JLabel();
        jTextFieldPackageChangeset = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        jTextFieldOrdnerChangeset = new javax.swing.JTextField();
        jLabel42 = new javax.swing.JLabel();
        jTextFieldQuelleChangeset = new javax.swing.JTextField();
        jBtnChangesetGruppeHinzu = new javax.swing.JButton();
        jTextFieldGruppenName = new javax.swing.JTextField();
        jBtnChangesetGruppeLoeschen = new javax.swing.JButton();
        btnProjektTakeOver = new javax.swing.JButton();
        btnProjektdatenSchreiben = new javax.swing.JButton();
        jPanelSourceTarget = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtSource = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtTarget = new javax.swing.JTextField();
        btnToggleSrcTarget = new javax.swing.JButton();
        jPanelZielsysteme = new javax.swing.JPanel();
        txtZusatzZiel = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblZiel = new javax.swing.JTable();
        txtZielGruppe = new javax.swing.JTextField();
        btnZielGruppe = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtZielLabelArea = new javax.swing.JTextArea();
        btnDeployGroupDelete = new javax.swing.JButton();
        txtTakeOverTarget = new javax.swing.JTextField();
        btnTakeOverTarget = new javax.swing.JButton();
        btnTakeOverGroup = new javax.swing.JButton();
        cmbBoxZielgruppe = new javax.swing.JComboBox<>();
        jLabel36 = new javax.swing.JLabel();
        chkTakeOverGroup = new javax.swing.JCheckBox();
        jButton12 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        txtDeployZielGruppe = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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

        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jTextArea3.setText("- validates target name 1 and 2\n- validates ALL targets in written group\n- ALSO validates source, if it is contained in group\n\n");
        jTextArea3.setEnabled(false);
        jScrollPane7.setViewportView(jTextArea3);

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(278, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(btnValDepSFDX)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnValDepSFDX1)))
                .addContainerGap(34, Short.MAX_VALUE))
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

        chkZielgruppe.setText("mit Zielgruppe deployen");
        chkZielgruppe.setEnabled(false);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("- deploys target name 1 and 2\n- deploys ALL targets in written group\n- ALSO deploys source, if it is contained in group\n\n");
        jTextArea1.setEnabled(false);
        jScrollPane3.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(btnBackupANDDeploy)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel34)
                        .addGap(0, 318, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 428, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkZielgruppe)
                    .addComponent(btn_DEPLOY))
                .addGap(20, 20, 20))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(btn_DEPLOY)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkZielgruppe))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                                        .addGap(0, 0, Short.MAX_VALUE)
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
                .addContainerGap(581, Short.MAX_VALUE))
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

        jLabel32.setText("Fieldnames of List in Lightning::");

        txtAreaOutputFIeldsPackage.setColumns(20);
        txtAreaOutputFIeldsPackage.setRows(5);
        jScrollPane11.setViewportView(txtAreaOutputFIeldsPackage);

        btnFnamesPackage.setText("get Fieldnames for package");
        btnFnamesPackage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFnamesPackageActionPerformed(evt);
            }
        });

        chkZwischenspeicher.setText("in den Zwischenspeicher schreiben");

        btnClosingXMLelement.setText("getTheClosingXMLelement");

        lblLineOfCode.setText("Line of code:");

        btnShowLines.setText("btnShowLines");
        btnShowLines.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowLinesActionPerformed(evt);
            }
        });

        txtAreaFieldNames.setColumns(20);
        txtAreaFieldNames.setRows(5);
        jScrollPane10.setViewportView(txtAreaFieldNames);

        jTabPnDeployment.addTab("Input", jScrollPane10);

        jLabel18.setText("jLabel18");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("first Tab", jPanel10);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 668, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 43, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        jTabPnDeployment.addTab("Output", jPanel9);

        btmWellFormedVFP.setText("Make well formed VFP");
        btmWellFormedVFP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btmWellFormedVFPActionPerformed(evt);
            }
        });

        btnPutNewLinesText.setText("Make readible lines VFP");
        btnPutNewLinesText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPutNewLinesTextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel14Layout.createSequentialGroup()
                                .addComponent(jLabel32)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnShowLines)
                                .addGap(18, 18, 18)
                                .addComponent(txtClosingXMLelement, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblLineOfCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel14Layout.createSequentialGroup()
                                .addComponent(jLabel30)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel31)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtObjName, javax.swing.GroupLayout.PREFERRED_SIZE, 548, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addComponent(btnFnamesPackage)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btmWellFormedVFP)
                                .addGap(19, 19, 19)
                                .addComponent(chkZwischenspeicher))
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addComponent(btnClosingXMLelement)
                                .addGap(18, 18, 18)
                                .addComponent(btnPutNewLinesText))))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jTabPnDeployment, javax.swing.GroupLayout.PREFERRED_SIZE, 716, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 582, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(467, Short.MAX_VALUE))
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
                    .addComponent(chkZwischenspeicher)
                    .addComponent(btmWellFormedVFP))
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel32)
                            .addComponent(txtClosingXMLelement, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblLineOfCode)
                            .addComponent(btnShowLines)))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnClosingXMLelement)
                            .addComponent(btnPutNewLinesText))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane11)
                    .addComponent(jTabPnDeployment))
                .addContainerGap(408, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Tools", jPanel14);

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
                    .addComponent(jChkAusblenden, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
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
                .addContainerGap(361, Short.MAX_VALUE))
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

        jLabel35.setText("Username:");

        javax.swing.GroupLayout jPanelBasisfunktionenLayout = new javax.swing.GroupLayout(jPanelBasisfunktionen);
        jPanelBasisfunktionen.setLayout(jPanelBasisfunktionenLayout);
        jPanelBasisfunktionenLayout.setHorizontalGroup(
            jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtDirectoryOld, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDataEnding, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnUserNamesList))
                            .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                                        .addComponent(jLabel35)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtUsernameRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabel27)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtLoginURL, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                                        .addComponent(btnUserRegister)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(chkPROD)))))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                                .addGap(267, 267, 267)
                                .addComponent(jLabel33)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldZiel_2, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE))
                            .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                                .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtTypeName, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelBasisfunktionenLayout.createSequentialGroup()
                                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBasisfunktionenLayout.createSequentialGroup()
                                                .addComponent(jLabel4)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                            .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                                                .addComponent(jLabel16)
                                                .addGap(14, 14, 14)))
                                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtMetadataName)
                                            .addComponent(txtSubname, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(4, 4, 4)))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanelBasisfunktionenLayout.setVerticalGroup(
            jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtDataEnding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnUserNamesList))
                        .addGap(4, 4, 4)
                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnUserRegister)
                            .addComponent(chkPROD))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtUsernameRegister, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel35))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel27)
                            .addComponent(txtLoginURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(32, 32, 32))
                    .addGroup(jPanelBasisfunktionenLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel33)
                            .addComponent(textFieldZiel_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtTypeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtMetadataName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(txtSubname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 468, Short.MAX_VALUE)))
                .addGroup(jPanelBasisfunktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDirectoryOld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addGap(32, 32, 32))
        );

        jTabRetrieve.addTab("Basisfunktionen", jPanelBasisfunktionen);

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 441, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 405, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmdRetrievePackage)
                    .addComponent(btnBackup)
                    .addComponent(jButton14))
                .addGap(101, 101, 101))
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

        btnCloneObj.setText("Objekt klonen");
        btnCloneObj.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloneObjActionPerformed(evt);
            }
        });

        chkBoxBuildSourceIntoFolder.setText("Build Source into Folder");
        chkBoxBuildSourceIntoFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkBoxBuildSourceIntoFolderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnCloneObj)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNewObjectName)
                        .addGap(197, 197, 197))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(btnRetSFDX)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkBoxBuildSourceIntoFolder)
                                .addGap(26, 26, 26)
                                .addComponent(jLabel28))
                            .addComponent(jButton15))
                        .addGap(28, 28, 28)
                        .addComponent(btnRetrieveSFDX)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton16)
                    .addComponent(btnRetBackupSFDX))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRetSFDX)
                    .addComponent(btnRetrieveSFDX)
                    .addComponent(jLabel28)
                    .addComponent(chkBoxBuildSourceIntoFolder))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton15)
                .addGap(4, 4, 4)
                .addComponent(jButton16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRetBackupSFDX)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCloneObj)
                    .addComponent(txtNewObjectName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(400, Short.MAX_VALUE))
        );

        jTabbedPane5.addTab("sfdx-Tools", jPanel3);

        btnUnzipOLD.setText("sfdx - unzip OLD Deployment package");
        btnUnzipOLD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUnzipOLDActionPerformed(evt);
            }
        });

        jLabel10.setText("altes Deployment-Paket (Pfad-zipDatei)");

        jLabel11.setText("altes Deployment-Paket (Name)");

        btnDeploymentsVereinen.setText("sfdx - deployments vereinen");
        btnDeploymentsVereinen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeploymentsVereinenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnUnzipOLD)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtDeploymentsVereinen, javax.swing.GroupLayout.PREFERRED_SIZE, 354, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(btnDeploymentsVereinen, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPackageName, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDeploymentsVereinen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUnzipOLD))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtPackageName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 323, Short.MAX_VALUE)
                .addComponent(btnDeploymentsVereinen)
                .addGap(111, 111, 111))
        );

        jTabbedPane5.addTab("sfdx -- Deployments vereinen", jPanel1);

        tblChangesets.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Changeset-Gruppe", "Changeset-Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblChangesets.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblChangesetsMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tblChangesets);

        btnRetrieveChangeset.setText("Retrieve Changeset");
        btnRetrieveChangeset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRetrieveChangesetActionPerformed(evt);
            }
        });

        jTextFieldRetrieveChangesetQuelle.setEnabled(false);

        jLabel37.setText("Name Quelle (take over from left):");

        btnRetrieveGroups.setText("Retrieve group of Changesets");

        jLabel38.setText("Name Gruppe (taken over from left):");

        jTextFieldRetrieveGroup.setEnabled(false);

        jLabel39.setText("Package Xml Import");

        jTextFieldXmlImportChangeset.setEditable(false);

        jLabel40.setText("Package Name (Change Set):");

        jTextFieldPackageChangeset.setEditable(false);

        jLabel41.setText("Projektordner:");

        jTextFieldOrdnerChangeset.setEditable(false);

        jLabel42.setText("Name Quelle:");

        jTextFieldQuelleChangeset.setEditable(false);

        jBtnChangesetGruppeHinzu.setText("Changeset in die Gruppe übernehmen");
        jBtnChangesetGruppeHinzu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnChangesetGruppeHinzuActionPerformed(evt);
            }
        });

        jBtnChangesetGruppeLoeschen.setText("Changeset aus der Gruppe entfernen");
        jBtnChangesetGruppeLoeschen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnChangesetGruppeLoeschenActionPerformed(evt);
            }
        });

        btnProjektTakeOver.setText("Projektdaten übernehmen  (oben)");
        btnProjektTakeOver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProjektTakeOverActionPerformed(evt);
            }
        });

        btnProjektdatenSchreiben.setText("Projektdaten zurückschreiben");
        btnProjektdatenSchreiben.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProjektdatenSchreibenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelChangesetsLayout = new javax.swing.GroupLayout(jPanelChangesets);
        jPanelChangesets.setLayout(jPanelChangesetsLayout);
        jPanelChangesetsLayout.setHorizontalGroup(
            jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelChangesetsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelChangesetsLayout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelChangesetsLayout.createSequentialGroup()
                                .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnRetrieveGroups, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextFieldRetrieveGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelChangesetsLayout.createSequentialGroup()
                                .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnRetrieveChangeset, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextFieldRetrieveChangesetQuelle, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanelChangesetsLayout.createSequentialGroup()
                        .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel40)
                            .addComponent(jLabel39)
                            .addComponent(jLabel41)
                            .addComponent(jLabel42))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldQuelleChangeset)
                            .addComponent(jTextFieldOrdnerChangeset)
                            .addComponent(jTextFieldXmlImportChangeset)
                            .addComponent(jTextFieldPackageChangeset)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelChangesetsLayout.createSequentialGroup()
                        .addComponent(jBtnChangesetGruppeHinzu)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldGruppenName, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnChangesetGruppeLoeschen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnProjektTakeOver)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelChangesetsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnProjektdatenSchreiben, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(83, 83, 83))
        );
        jPanelChangesetsLayout.setVerticalGroup(
            jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelChangesetsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelChangesetsLayout.createSequentialGroup()
                        .addComponent(btnRetrieveChangeset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextFieldRetrieveChangesetQuelle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel37))
                        .addGap(32, 32, 32)
                        .addComponent(btnRetrieveGroups)
                        .addGap(18, 18, 18)
                        .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel38)
                            .addComponent(jTextFieldRetrieveGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel39)
                    .addComponent(jTextFieldXmlImportChangeset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel40)
                    .addComponent(jTextFieldPackageChangeset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel41)
                    .addComponent(jTextFieldOrdnerChangeset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel42)
                    .addComponent(jTextFieldQuelleChangeset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelChangesetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnChangesetGruppeHinzu)
                    .addComponent(jTextFieldGruppenName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnChangesetGruppeLoeschen)
                    .addComponent(btnProjektTakeOver))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnProjektdatenSchreiben)
                .addContainerGap(158, Short.MAX_VALUE))
        );

        jTabbedPane5.addTab("Changesets", jPanelChangesets);

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

        btnToggleSrcTarget.setText("Toggle Source and Target");
        btnToggleSrcTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnToggleSrcTargetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelSourceTargetLayout = new javax.swing.GroupLayout(jPanelSourceTarget);
        jPanelSourceTarget.setLayout(jPanelSourceTargetLayout);
        jPanelSourceTargetLayout.setHorizontalGroup(
            jPanelSourceTargetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSourceTargetLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSourceTargetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelSourceTargetLayout.createSequentialGroup()
                        .addGap(0, 20, Short.MAX_VALUE)
                        .addGroup(jPanelSourceTargetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnToggleSrcTarget)
                            .addGroup(jPanelSourceTargetLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtTarget, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanelSourceTargetLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSource, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(4, 4, 4))
        );
        jPanelSourceTargetLayout.setVerticalGroup(
            jPanelSourceTargetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSourceTargetLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSourceTargetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelSourceTargetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnToggleSrcTarget)
                .addContainerGap(156, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelProjektdatenLayout = new javax.swing.GroupLayout(jPanelProjektdaten);
        jPanelProjektdaten.setLayout(jPanelProjektdatenLayout);
        jPanelProjektdatenLayout.setHorizontalGroup(
            jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProjektdatenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel20)
                    .addGroup(jPanelProjektdatenLayout.createSequentialGroup()
                        .addGroup(jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelProjektdatenLayout.createSequentialGroup()
                                .addComponent(jLabel21)
                                .addGap(160, 160, 160))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelProjektdatenLayout.createSequentialGroup()
                                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)))
                        .addGroup(jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTargetData, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtProjectName, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtBuildProperties, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelProjektdatenLayout.createSequentialGroup()
                                .addComponent(txtProjectFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBox2))))
                    .addGroup(jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelProjektdatenLayout.createSequentialGroup()
                            .addComponent(lblPackage)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtRetrievePackage))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelProjektdatenLayout.createSequentialGroup()
                            .addComponent(lblXmlImport)
                            .addGap(13, 13, 13)
                            .addComponent(txtXmlImport, javax.swing.GroupLayout.PREFERRED_SIZE, 781, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jTabbedPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelProjektdatenLayout.createSequentialGroup()
                        .addComponent(jPanelSourceTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelProjektdatenLayout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addGap(99, 99, 99))))
        );
        jPanelProjektdatenLayout.setVerticalGroup(
            jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProjektdatenLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanelSourceTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton5)
                .addGap(300, 300, 300))
            .addGroup(jPanelProjektdatenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addGroup(jPanelProjektdatenLayout.createSequentialGroup()
                        .addComponent(txtTargetData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addGroup(jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(txtProjectFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox2))
                        .addGroup(jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtProjectName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel21))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtBuildProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22))))
                .addGap(18, 18, 18)
                .addGroup(jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblXmlImport)
                    .addComponent(txtXmlImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanelProjektdatenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRetrievePackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPackage))
                .addGap(18, 18, 18)
                .addComponent(jTabbedPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabRetrieve.addTab("Projektdaten", jPanelProjektdaten);

        jLabel12.setText("Name des zusätzlichen Ziels");

        tblZiel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Zielgruppe", "Zielname"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblZiel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblZielMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblZiel);

        btnZielGruppe.setText("Ziel mit Gruppe hinzufügen");
        btnZielGruppe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZielGruppeActionPerformed(evt);
            }
        });

        txtZielLabelArea.setEditable(false);
        txtZielLabelArea.setColumns(20);
        txtZielLabelArea.setRows(5);
        txtZielLabelArea.setText("Hier können Salesforce Organisationen\nnamentlich erfasst und auch in Gruppen\nzusammengefasst werden. \nMan kann dann ein Deployment in eine\nganze Gruppe von Salesforce-Umgebungen\nvornehmen!");
        jScrollPane2.setViewportView(txtZielLabelArea);

        btnDeployGroupDelete.setText("Delete Ziel");
        btnDeployGroupDelete.setSelected(true);
        btnDeployGroupDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeployGroupDeleteActionPerformed(evt);
            }
        });

        txtTakeOverTarget.setEditable(false);

        btnTakeOverTarget.setText("Take over target");
        btnTakeOverTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTakeOverTargetActionPerformed(evt);
            }
        });

        btnTakeOverGroup.setText("Take over group");
        btnTakeOverGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTakeOverGroupActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelZielsystemeLayout = new javax.swing.GroupLayout(jPanelZielsysteme);
        jPanelZielsysteme.setLayout(jPanelZielsystemeLayout);
        jPanelZielsystemeLayout.setHorizontalGroup(
            jPanelZielsystemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelZielsystemeLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelZielsystemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelZielsystemeLayout.createSequentialGroup()
                        .addComponent(txtZielGruppe, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnZielGruppe)
                        .addContainerGap())
                    .addGroup(jPanelZielsystemeLayout.createSequentialGroup()
                        .addGroup(jPanelZielsystemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTakeOverTarget)
                            .addGroup(jPanelZielsystemeLayout.createSequentialGroup()
                                .addComponent(btnTakeOverTarget)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(btnTakeOverGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addGroup(jPanelZielsystemeLayout.createSequentialGroup()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtZusatzZiel, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnDeployGroupDelete)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelZielsystemeLayout.setVerticalGroup(
            jPanelZielsystemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelZielsystemeLayout.createSequentialGroup()
                .addGroup(jPanelZielsystemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtZusatzZiel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(btnDeployGroupDelete))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelZielsystemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelZielsystemeLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanelZielsystemeLayout.createSequentialGroup()
                        .addGroup(jPanelZielsystemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtZielGruppe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnZielGruppe))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelZielsystemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addGroup(jPanelZielsystemeLayout.createSequentialGroup()
                                .addComponent(txtTakeOverTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnTakeOverTarget)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnTakeOverGroup)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );

        jTabRetrieve.addTab("Zielsysteme", jPanelZielsysteme);

        cmbBoxZielgruppe.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbBoxZielgruppe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbBoxZielgruppeActionPerformed(evt);
            }
        });

        jLabel36.setText("Zielgruppe");

        chkTakeOverGroup.setText("Take over group");

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
                        .addGap(56, 56, 56)
                        .addComponent(jLabel36)
                        .addGap(18, 18, 18)
                        .addComponent(cmbBoxZielgruppe, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkTakeOverGroup)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jTabRetrieve)
                        .addGap(18, 18, 18)
                        .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 537, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbMetaData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(cmbBoxZielgruppe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36)
                    .addComponent(chkTakeOverGroup))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmdMetaDataName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTabRetrieve, javax.swing.GroupLayout.PREFERRED_SIZE, 720, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(270, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Retrieve", jPanel5);

        jButton12.setText("Save Options");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jLabel13.setText("Zielgruppe");

        javax.swing.GroupLayout jPanelSystemInformationLayout = new javax.swing.GroupLayout(jPanelSystemInformation);
        jPanelSystemInformation.setLayout(jPanelSystemInformationLayout);
        jPanelSystemInformationLayout.setHorizontalGroup(
            jPanelSystemInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSystemInformationLayout.createSequentialGroup()
                .addGroup(jPanelSystemInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelSystemInformationLayout.createSequentialGroup()
                        .addGap(744, 744, 744)
                        .addComponent(jButton12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDeployZielGruppe, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelSystemInformationLayout.createSequentialGroup()
                        .addContainerGap(154, Short.MAX_VALUE)
                        .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1828, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(1509, 1509, 1509))
        );
        jPanelSystemInformationLayout.setVerticalGroup(
            jPanelSystemInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSystemInformationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSystemInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton12)
                    .addComponent(txtDeployZielGruppe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addGap(52, 52, 52)
                .addComponent(jTabbedPane2)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelSystemInformation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelSystemInformation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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

    private void btnXmlImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXmlImportActionPerformed
        // TODO add your handling code here:
        getImportProcess();
    }//GEN-LAST:event_btnXmlImportActionPerformed

    private void btnBackupXMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackupXMLActionPerformed
        // TODO add your handling code here:
        getBackupProcess();
    }//GEN-LAST:event_btnBackupXMLActionPerformed

    private void cmdRetrievePackageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRetrievePackageActionPerformed
        // TODO add your handling code here:
        String packageName = txtRetrievePackage.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = deprecated_Start.commandFolder.getPath() + "\\" + 
        projectPath +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        createAndExecutePackages(buildFilePath.trim(),"retrievePackage",
                txtSource.getText(),
                txtRetrievePackage.getText());
    }//GEN-LAST:event_cmdRetrievePackageActionPerformed

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
                command,
                txtSource.getText(),
                txtRetrievePackage.getText() 
        );
        
        try {
            appendDeploymentInFile("retrieve:" + buildFilePath);
        } catch (IOException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnRetSFDXActionPerformed

    private void btnRetBackupSFDXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRetBackupSFDXActionPerformed
        try {
            // TODO add your handling code here:
            backup();
        } catch (IOException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnRetBackupSFDXActionPerformed

    private static String getLastFolderName(String buildFilePath){
        File f = new File(buildFilePath);
        File[] fs = f.listFiles();
        String folderName = null;
        for(Integer i = fs.length - 1;i>-1;i--){
            File fi = fs[i];

            if(fi.isDirectory()){
                folderName = fi.getName();

                if(!folderName.contains("deploy")){
                    break;
                }
            }
        }
        
        return folderName;
    }
    
    private String getBuildFilePath(){
        String packageName = txtRetrievePackage.getText().trim();
        String projectPath = txtProjectFolder.getText().trim();
        String buildFilePath = commandFolder.getPath() + "\\" + 
        projectPath +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        System.out.println("Path::" + buildFilePath);
        String folderName = getLastFolderName(buildFilePath);
        System.out.println("folderName:::" + folderName);
        buildFilePath = buildFilePath + "\\" + folderName;
        
        return buildFilePath;
    }
    
    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        // TODO add your handling code here:
        String buildFilePath = getBuildFilePath();
        unzip(buildFilePath,txtSource.getText(),txtRetrievePackage.getText(),
                "unpackaged.zip");
    }//GEN-LAST:event_jButton16ActionPerformed

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
            command,
                txtSource.getText(),
                txtRetrievePackage.getText()
        );
    }//GEN-LAST:event_btnRetrieveSFDXActionPerformed

    private void txtDataEndingPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtDataEndingPropertyChange
        // TODO add your handling code here:

    }//GEN-LAST:event_txtDataEndingPropertyChange

    private void txtDataEndingKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDataEndingKeyPressed
        // TODO add your handling code here:
        //saveButton.setEnabled(false);
    }//GEN-LAST:event_txtDataEndingKeyPressed

    private void btnUserRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUserRegisterActionPerformed
        // TODO add your handling code here:
        String sourceName = txtUsernameRegister.getText();
        if(sourceName == null || sourceName.trim().length()==0){
            sourceName = txtSource.getText();
        }
        String loginURL = txtLoginURL.getText();

        if(loginURL.length()==0)loginURL = "https://test.salesforce.com";

        if(chkPROD.isSelected()){
            loginURL = "https://login.salesforce.com";
        }

        createAndExecutePackages(null,"retrievePackage",true,
            "start sfdx force:auth:web:login -a " + sourceName
            + " -r " + "\"" + loginURL +"\"",
                txtSource.getText(),
                txtRetrievePackage.getText()
        );
    }//GEN-LAST:event_btnUserRegisterActionPerformed

    private void txtDirectoryOldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtDirectoryOldPropertyChange
        // TODO add your handling code here:

    }//GEN-LAST:event_txtDirectoryOldPropertyChange

    private void txtDirectoryOldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDirectoryOldKeyPressed
        // TODO add your handling code here:
        //saveButton.setEnabled(false);
    }//GEN-LAST:event_txtDirectoryOldKeyPressed

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
            command,
                txtSource.getText(),
                txtRetrievePackage.getText()
        );
    }//GEN-LAST:event_btnUserNamesListActionPerformed

    private void txtSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSourceActionPerformed
        // TODO add your handling code here:
        String txtSourceStr = txtSource.getText();
        btnUserRegister.setText(txtSourceStr);
    }//GEN-LAST:event_txtSourceActionPerformed

    private void txtSourcePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtSourcePropertyChange
        // TODO add your handling code here:

    }//GEN-LAST:event_txtSourcePropertyChange

    private void txtSourceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSourceKeyPressed
        // TODO add your handling code here:
        //saveButton.setEnabled(false);
    }//GEN-LAST:event_txtSourceKeyPressed

    private void txtTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTargetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTargetActionPerformed

    private void txtTargetPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtTargetPropertyChange
        // TODO add your handling code here:
        txtDeployTo.setText(txtTarget.getText());
    }//GEN-LAST:event_txtTargetPropertyChange

    private void txtTargetKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTargetKeyPressed
        // TODO add your handling code here:
        //saveButton.setEnabled(false);
    }//GEN-LAST:event_txtTargetKeyPressed

    private void txtTypeNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTypeNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTypeNameActionPerformed

    private void txtTypeNamePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtTypeNamePropertyChange
        // TODO add your handling code here:

    }//GEN-LAST:event_txtTypeNamePropertyChange

    private void txtTypeNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTypeNameKeyPressed
        // TODO add your handling code here:
        //saveButton.setEnabled(false);
    }//GEN-LAST:event_txtTypeNameKeyPressed

    private void txtMetadataNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMetadataNameActionPerformed
        // TODO add your handling code here:
        //saveButton.setEnabled(false);
    }//GEN-LAST:event_txtMetadataNameActionPerformed

    private void txtMetadataNamePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtMetadataNamePropertyChange
        // TODO add your handling code here:
        //saveButton.setEnabled(false);
    }//GEN-LAST:event_txtMetadataNamePropertyChange

    private void txtMetadataNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMetadataNameKeyPressed
        // TODO add your handling code here:
        //saveButton.setEnabled(false);
    }//GEN-LAST:event_txtMetadataNameKeyPressed

    private void txtSubnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSubnameActionPerformed
        // TODO add your handling code here:
        //saveButton.setEnabled(false);
    }//GEN-LAST:event_txtSubnameActionPerformed

    private void txtProjectFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProjectFolderActionPerformed
        // TODO add your handling code here:
        this.setFolders();
    }//GEN-LAST:event_txtProjectFolderActionPerformed

    private void txtProjectNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProjectNameActionPerformed
        // TODO add your handling code here:
        deprecated_Start.projectName = txtProjectName.getText();
        this.setFolders();
    }//GEN-LAST:event_txtProjectNameActionPerformed

    private void txtProjectNamePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtProjectNamePropertyChange
        deprecated_Start.projectName = txtProjectName.getText();    // TODO add your handling code here:
    }//GEN-LAST:event_txtProjectNamePropertyChange

    private void txtBuildPropertiesPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txtBuildPropertiesPropertyChange
        // TODO add your handling code here:
        buildProperties = txtBuildProperties.getText();

    }//GEN-LAST:event_txtBuildPropertiesPropertyChange

    private void txtXmlImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtXmlImportActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtXmlImportActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:

        try {
            File buildFile= this.createBuildFile(deprecated_Start.backup.getPath(),txtSource.getText());

            File packageFile = this.createPackageFile(
                deprecated_Start.backup.getPath(),
                txtMetadataName.getText(),
                txtSubname.getText(),
                txtTypeName.getText()
            );
            File commandFileRetrieve = createCommandFileRetrieve(deprecated_Start.backup.getPath());
            Process process = new ProcessBuilder(commandFileRetrieve.getPath()).start();
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        deprecated_Start.addListElement(lstTestKlassen,txtTestklasse.getText());

        try {
            this.saveDep.save();
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
        Start_Frame.deleteListenElement(lstTestKlassen);
        try {
            this.saveDep.save();
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton9ActionPerformed

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

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        // TODO add your handling code here:
        try {
            // TODO add your handling code here:
            deploy(true);
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton13ActionPerformed

    private void btnDeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeployActionPerformed
        try {
            // TODO add your handling code here:
            deploy(false);
        } catch (IOException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnDeployActionPerformed

    private void btnValDepSFDXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnValDepSFDXActionPerformed
        try {
            String deployZielGruppe = txtDeployZielGruppe.getText();
            HashSet<String> set = this.opts.fromTargetNameToGroupNameSet.get(deployZielGruppe);
            // TODO add your handling code here:
            String targetName = txtTarget.getText();
            if(targetName != null && targetName.length() > 0){
                if(set != null && !set.contains(targetName)|| set==null)validate(targetName);
            }
             
            String target2Name = textFieldZiel_2.getText(); 
            if(target2Name != null && target2Name.length() > 0){
                if(set != null && !set.contains(target2Name) || set==null)validate(target2Name);
            }
            
            if(set != null){
                for(String zielName:set){
                    validate(zielName);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnValDepSFDXActionPerformed

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
                command,
                txtSource.getText(),
                txtRetrievePackage.getText()
            );
    }//GEN-LAST:event_btnValDepSFDX1ActionPerformed

    private void btn_DEPLOYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_DEPLOYActionPerformed
        try{
            deploy();
        } catch (IOException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btn_DEPLOYActionPerformed

    private void btnBackupANDDeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackupANDDeployActionPerformed
        try {
            // TODO add your handling code here:
            backup();
            deploy();
        } catch (IOException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnBackupANDDeployActionPerformed

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

    
    private void handleFile(File newFile,File oldFile){
        try {
            //System.out.println("handleFile::newFile::" + newFile);
            //System.out.println("handleFile::oldFile::" + oldFile);
            
            if(oldFile.isDirectory()){
                String newFilePath = 
                        newFile.getPath() 
                        + "\\"
                        + oldFile.getName();
                for(File file:oldFile.listFiles()){
                    newFile = new File(newFilePath
                        + "\\"
                        + file.getName()
                    );
                    System.out.println("handleFile::directory::" + newFile);
                    System.out.println("handleFile::directory::" + file);
                    if(file.getParentFile().isDirectory()){
                        newFile.getParentFile().mkdirs();
                    }
                    handleFile(newFile,file);
                }
            }else if(!newFile.exists()){
                String oldFileContent 
                    = FileHandling.readFile(oldFile);
                FileHandling.writeFile(oldFileContent, newFile);
            }
        } catch (IOException ex) {
            Logger.getLogger(Start_Frame.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    private void btnDeploymentsVereinenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeploymentsVereinenActionPerformed
        // TODO add your handling code here:
        
        String newBuildFilePath = txtProjectFolder.getText() 
                + "\\";
        String newFolderName = getLastFolderName(newBuildFilePath);
        newBuildFilePath += newFolderName;
        newFolderName = getLastFolderName(newBuildFilePath);
        newBuildFilePath += "\\" + newFolderName;
        
        File readNewFolder = new File(newBuildFilePath);
        File[] newFolders = readNewFolder.listFiles();
        
        HashMap<String,File> newFoldersMap = new HashMap<String,File>();
        for(File newFolder:newFolders){
            newFoldersMap.put(newFolder.getName(),newFolder);
        }
        
        ///
        
        String buildFilePath = txtDeploymentsVereinen.getText() 
                + "\\";
        String folderName = getLastFolderName(buildFilePath);
        buildFilePath += folderName;
        
        //System.out.println("buildFilePath::" + buildFilePath);
        
        File readOldFolder = new File(buildFilePath);
        File[] oldFolders = readOldFolder.listFiles();
        
        for(File oldFolder:oldFolders){
            if(oldFolder.isDirectory()){
                String oldFolderName = oldFolder.getName();
                //System.out.println("oldFolderName::" + oldFolderName);
                
                File newFolder = newFoldersMap.get(oldFolder.getName());
                if(newFolder == null){
                     newFolder = new File(readNewFolder.getPath() 
                             + "\\"
                             + oldFolderName
                     );
                    newFolder.mkdirs();
                }
                
                File[] oldFiles = null;
                
                if(oldFolderName.equals("email")){
                    File newEmailFolder = new File(newFolder.getPath());
                    newEmailFolder.mkdirs();
                }
                oldFiles = oldFolder.listFiles();
                for(File oldFile:oldFiles){
                    if(oldFolderName.equals("classes") 
                        || oldFolderName.equals("email")
                        || oldFolderName.equals("flows")
                        || oldFolderName.equals("layouts")
                        || oldFolderName.equals("permissionsets")
                        || oldFolderName.equals("pages")
                        || oldFolderName.equals("tabs")
                        || oldFolderName.equals("triggers")
                        || !new File(newFolder.getPath()
                            + "\\" + oldFile.getName()).exists()
                    ){
                        File newFile = new File(newFolder.getPath());
                        handleFile(newFile,oldFile);
                    }else{
                        try {
                            File newFile = new File(newFolder.getPath()
                            + "\\" + oldFile.getName());
                            XMLDatenbank oldFileDB = new XMLDatenbank(oldFile);
                            XMLDatenbank newFileDB = new XMLDatenbank(newFile);
                            
                            for(String typeName : oldFileDB
                                    .fromTypeNameToFromFullNameToNodeMap
                                    .keySet()
                            ){
                                HashMap<String,XMLNode> oldFromFullNameToNodeMap 
                                        = oldFileDB
                                                .fromTypeNameToFromFullNameToNodeMap
                                                .get(typeName);
                                HashMap<String,XMLNode> newFromFullNameToNodeMap 
                                        = newFileDB
                                                .fromTypeNameToFromFullNameToNodeMap
                                                .get(typeName);
                                if(newFromFullNameToNodeMap == null){
                                    newFromFullNameToNodeMap 
                                            = new HashMap<String,XMLNode>();
                                            oldFileDB
                                                    .fromTypeNameToFromFullNameToNodeMap
                                                    .put(typeName,oldFromFullNameToNodeMap);
                                }
                                for(String fullName 
                                        : newFromFullNameToNodeMap.keySet()
                                ){
                                    XMLNode node = newFromFullNameToNodeMap
                                            .get(fullName);
                                    oldFromFullNameToNodeMap.put(fullName,node);
                                }
                                
                                for(String fullName 
                                        : oldFromFullNameToNodeMap.keySet()
                                ){
                                    XMLNode node = oldFromFullNameToNodeMap
                                            .get(fullName);
                                    newFromFullNameToNodeMap.put(fullName,node);
                                }
                            }
                            
                            newFileDB.writeXMLFile(newFile);
                        } catch (IOException ex) {
                            Logger.getLogger(Start_Frame.class.getName())
                                    .log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
        
        /*try {
            FileHandling.zipFile(readNewFolder
                    , readNewFolder.getParentFile()
            );
        } catch (IOException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }//GEN-LAST:event_btnDeploymentsVereinenActionPerformed

    private void btnUnzipOLDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUnzipOLDActionPerformed
        // TODO add your handling code here:
        unzip(txtDeploymentsVereinen.getText(), 
                txtSource.getText(), 
                txtRetrievePackage.getText(),
                txtPackageName.getText());
    }//GEN-LAST:event_btnUnzipOLDActionPerformed

    private void btnCloneObjActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloneObjActionPerformed
        try {
            // TODO add your handling code here:
            String buildFilePath = getBuildFilePath()
                    + "\\"
                    + txtRetrievePackage.getText();
            File f = new File(buildFilePath);
            String objectName = txtNewObjectName.getText();
            Long millis = System.currentTimeMillis();
            File newFolder = new File(
                f.getParentFile().getPath() + "\\" + millis + "_ClonedObject"
            );
            newFolder.mkdirs();
            System.out.println(newFolder.getPath());
            
            cloneFileForObject(
                f,objectName,newFolder.getPath()
            );
        } catch (IOException ex) {
            Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnCloneObjActionPerformed

    private void btnZielGruppeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZielGruppeActionPerformed
        
        DefaultTableModel tModel = (DefaultTableModel)tblZiel.getModel();
        
        String zielGruppe = txtZielGruppe.getText();
        String zielName = txtZusatzZiel.getText();
        
        System.out.println("zielGruppe::" + zielGruppe);
        System.out.println("zielName::" + zielName);
        
        HashSet<String> set = this.opts.fromTargetNameToGroupNameSet.get(zielGruppe);
        if(set == null){
            set = new HashSet<String>();
            this.opts.fromTargetNameToGroupNameSet.put(zielGruppe,set);
        }
        
        if(!set.contains(zielName)){
            tModel.addRow(new Object[0]);
            tModel.setValueAt(zielGruppe, tModel.getRowCount()-1, 0);
            tModel.setValueAt(zielName, tModel.getRowCount()-1, 1);
            set.add(zielName);
        }
    }//GEN-LAST:event_btnZielGruppeActionPerformed

    private void btnDeployGroupDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeployGroupDeleteActionPerformed
        // TODO add your handling code here:
        DefaultTableModel tModel = (DefaultTableModel)tblZiel.getModel();
        Integer rowIndex = -1;
        
        for(Integer i=0;i<tModel.getRowCount();i++){
            String zielName = tModel.getValueAt(i, 1) + "";
            String zielGruppe = tModel.getValueAt(i, 0) + "";
            
            if(txtZusatzZiel.getText().equals(zielName)){
                if(txtZielGruppe.getText().equals(zielGruppe)){
                    HashSet<String> set = this.opts.fromTargetNameToGroupNameSet.get(zielGruppe);
                    if(set != null){
                        set.remove(zielName);
                    }
                    rowIndex = i;
                    break;
                }
            }
        }
        
        if(rowIndex >= 0){
            tModel.removeRow(rowIndex);
        }
    }//GEN-LAST:event_btnDeployGroupDeleteActionPerformed

    private void btnShowLinesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowLinesActionPerformed
        // TODO add your handling code here:
        //String 
    }//GEN-LAST:event_btnShowLinesActionPerformed
    
    private static String overrideForWellFormedXML(
            String sourceCode,
            List<XMLtag> xmlTagListToOverrideList
    ){
        String overwrittenWellFormedXML = sourceCode;
        for(XMLtag tag:xmlTagListToOverrideList){
            String leftPart = sourceCode.substring(0, tag.startPos-1);
            
        }
        return overwrittenWellFormedXML;
    }
    
    //Tested
    public static Map<String,XMLtag> getfromXMLnameToPreviousTagMap(String sourceCode){
        return getfromXMLnameToPreviousTagMap(sourceCode,regexXMLelement);
    }
    
    public static String getCleanedName(String xmlName){
        String cleanedName;
        cleanedName = xmlName;
        if(cleanedName.startsWith("/"))cleanedName = cleanedName.substring(1);
        if(cleanedName.endsWith("/"))cleanedName = cleanedName.substring(0,cleanedName.length()-1);
        return cleanedName;
    }
    
    //Tested
    public static Map<String,XMLtag> getfromXMLnameToPreviousTagMap(
            String sourceCode
            ,String regexXMLelement
    ){
        Map<String,XMLtag> fromXMLnameToPreviousTagMap 
                = new HashMap<String,XMLtag>();
        Pattern regexPattern = Pattern.compile(regexXMLelement);
        Matcher regexMatcher = regexPattern.matcher(sourceCode);
        
        Map<String,Integer> fromXmlNameToOpenCloseMap 
                = new HashMap<String,Integer>();
        int tabNumber = 0;
        firstGeneralTag = null;
        
        while(regexMatcher.find()){
            String xmlName = regexMatcher.group(2);
 
            Integer regionStart = regexMatcher.start();
            
            String xmlTag = regexMatcher.group(1);
            String dashesText = regexMatcher.group(4);
            String cleanedName = getCleanedName(xmlName);
           
            Integer openCloseNumber = fromXmlNameToOpenCloseMap.get(cleanedName);
            if(openCloseNumber == null)openCloseNumber = 0;
           
            XMLtag thisXMLtag = new XMLtag(
                regionStart
                ,xmlTag.length() + dashesText.length() + 1
                ,xmlName
            );
            
            if(firstGeneralTag == null){
                firstGeneralTag = thisXMLtag;
                firstGeneralTag.firstGeneralXML = firstGeneralTag;
            }else{
                XMLtag theFirstTag = firstGeneralTag.firstGeneralXML;
                XMLtag theLastTag = firstGeneralTag.lastGeneralXML;
                
                theLastTag.nextGeneralXML = thisXMLtag;
                thisXMLtag.previousGeneralXML = theLastTag;
                theFirstTag.previousGeneralXML = thisXMLtag;
            }
            firstGeneralTag.lastGeneralXML = thisXMLtag;
            thisXMLtag.nextGeneralXML = null;
            
            XMLtag lastXMLtag = fromXMLnameToPreviousTagMap.get(cleanedName);
            if(lastXMLtag != null){
                lastXMLtag.nextReplaceXML = thisXMLtag;
                thisXMLtag.firstReplaceXML = lastXMLtag.firstReplaceXML;
                thisXMLtag.setPreviousReplaceXML(lastXMLtag);
            }else{
                thisXMLtag.firstReplaceXML = thisXMLtag;
            }
                
            if(xmlName.startsWith("/")){
                openCloseNumber--;
                tabNumber--;
                thisXMLtag.tabNumber = tabNumber;
                
                thisXMLtag.openCloseNumber = openCloseNumber;
                if(openCloseNumber == 0){
                    fromXMLnameToPreviousTagMap.remove(cleanedName);
                    lastXMLtag.destroyReplaceXMLtag();
                    System.out.println("###remove tag::" + xmlTag 
                            + "--for openCloseNumber for name::"
                            + xmlName + "-- cleaned::-" + cleanedName
                            + "--old startPos::" + thisXMLtag.startPos
                    );
                }
                if(lastXMLtag != null){
                    lastXMLtag = lastXMLtag.getPreviousReplaceXML();
                    
                    if(lastXMLtag == null){
                        fromXMLnameToPreviousTagMap.remove(cleanedName);
                        System.out.println("###remove tag::" + xmlTag 
                                + "--for previousXMLtag::for xmlName::" 
                                + xmlName + "--cleaned::" + cleanedName
                                + "--old startPos::" + thisXMLtag.startPos
                        );
                    }else{
                        fromXMLnameToPreviousTagMap.put(
                            cleanedName
                            ,lastXMLtag
                        );
                        lastXMLtag.nextReplaceXML = null;
                        System.out.println("###replace tag::" + xmlTag 
                                + "--for previousXMLrag::for xmlName::" 
                                + xmlName + "--cleaned::" + cleanedName
                                + "--new startPos::" + thisXMLtag.startPos
                        );
                    }
                    if(openCloseNumber == 0){
                        lastXMLtag = null;
                    }
                }
            }else if(!xmlTag.trim().endsWith("/")){
                thisXMLtag.tabNumber = tabNumber;
                if(!xmlTag.startsWith("<!")){
                    tabNumber++;
                    openCloseNumber++;
                    fromXMLnameToPreviousTagMap.put(cleanedName,thisXMLtag);
                    System.out.println("###put tag::" + xmlTag
                            + "--for NOT ending dash::for xmlName::" 
                                + xmlName + "--cleaned::" + cleanedName + "--"
                                + "--new startPos::" + thisXMLtag.startPos
                    );
                }
            }else{
                thisXMLtag.tabNumber = tabNumber;
                System.out.println("###skip tag::" + xmlTag
                    + "--for xmlName::"
                        + xmlName + "--cleaned::" + cleanedName + "--"
                        + "--equal startPos::" + thisXMLtag.startPos
                );
            }
            
            fromXmlNameToOpenCloseMap.put(cleanedName,openCloseNumber);
            thisXMLtag.openCloseNumber = openCloseNumber;
        }
        return fromXMLnameToPreviousTagMap;
    }
    
    private void btmWellFormedVFPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btmWellFormedVFPActionPerformed
        // TODO add your handling code here:
        
        String sourceCode = txtAreaFieldNames.getText();
        String targetCode = "";
        
        Map<String,XMLtag> fromXMLnameToPreviousTagMap 
                = getfromXMLnameToPreviousTagMap(sourceCode);
        
        targetCode = Routines.getDashedText(
                fromXMLnameToPreviousTagMap
                ,sourceCode
        );
        txtAreaOutputFIeldsPackage.setText(targetCode);
    }//GEN-LAST:event_btmWellFormedVFPActionPerformed

    private void btnPutNewLinesTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPutNewLinesTextActionPerformed
        // TODO add your handling code here:
        String sourceCode = txtAreaFieldNames.getText();
        String targetCode = "";
        
        Map<String,XMLtag> fromXMLnameToPreviousTagMap 
                = getfromXMLnameToPreviousTagMap(sourceCode);
        
        targetCode = Routines.getLinedText(
                fromXMLnameToPreviousTagMap
                ,sourceCode
        );
        txtAreaOutputFIeldsPackage.setText(targetCode);
    }//GEN-LAST:event_btnPutNewLinesTextActionPerformed

    private void tblZielMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblZielMouseClicked
        // TODO add your handling code here:
        
        String username = (String)tblZiel.getValueAt(tblZiel.getSelectedRow(), tblZiel.getSelectedColumn());
        txtTakeOverTarget.setText(username);
    }//GEN-LAST:event_tblZielMouseClicked

    private void btnTakeOverTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTakeOverTargetActionPerformed
        // TODO add your handling code here:
        txtTarget.setText(txtTakeOverTarget.getText());
    }//GEN-LAST:event_btnTakeOverTargetActionPerformed

    private void btnTakeOverGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTakeOverGroupActionPerformed
        // TODO add your handling code here:
        txtDeployZielGruppe.setText(txtTakeOverTarget.getText());
    }//GEN-LAST:event_btnTakeOverGroupActionPerformed

    private void cmbBoxZielgruppeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbBoxZielgruppeActionPerformed
        // TODO add your handling code here:
        if(chkTakeOverGroup.isSelected()){
            txtDeployZielGruppe.setText((String)cmbBoxZielgruppe.getSelectedItem());
        }
    }//GEN-LAST:event_cmbBoxZielgruppeActionPerformed

    private void btnToggleSrcTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnToggleSrcTargetActionPerformed
        // TODO add your handling code here:
        String sourceText = txtSource.getText();
        String targetText = txtTarget.getText();
        
        txtTarget.setText(sourceText);
        txtSource.setText(targetText);
    }//GEN-LAST:event_btnToggleSrcTargetActionPerformed

    private void chkBoxBuildSourceIntoFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkBoxBuildSourceIntoFolderActionPerformed
        // TODO add your handling code here:
        String sourceString = txtSource.getText().trim();
        String pathString = txtProjectFolder.getText();
        String[] splittedPathFragmentsArray = pathString.split("\\\\");
        String targetPathString = "";
        for(Integer i=0;i<splittedPathFragmentsArray.length-2;i++){
            String pathFragment = splittedPathFragmentsArray[i];
            if(targetPathString.length()>0){
                targetPathString += "\\";
            }
            targetPathString += pathFragment;
        }
        if(chkBoxBuildSourceIntoFolder.isSelected()){
            String nextFragment = splittedPathFragmentsArray[splittedPathFragmentsArray.length-2];
            targetPathString += "\\" + nextFragment;
            targetPathString += "\\" + sourceString;
        }
        targetPathString += "\\" + splittedPathFragmentsArray[splittedPathFragmentsArray.length-1];
        
        System.out.println("###targetPathString::" + targetPathString);
        txtProjectFolder.setText(targetPathString);
    }//GEN-LAST:event_chkBoxBuildSourceIntoFolderActionPerformed

    private void tblChangesetsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblChangesetsMouseClicked
        // TODO add your handling code here:
        String groupName = (String)tblChangesets.getValueAt(tblChangesets.getSelectedRow(),0);
        jTextFieldRetrieveGroup.setText(groupName);
        
        String changesetName = (String)tblChangesets.getValueAt(tblChangesets.getSelectedRow(),1);
        jTextFieldRetrieveChangesetQuelle.setText(changesetName);
        Changeset newChangeset = this.opts.fromNameToChangesetMap.get(changesetName);
        jTextFieldXmlImportChangeset.setText(newChangeset.get_xmlPackagePath());
        jTextFieldPackageChangeset.setText(newChangeset.get_changesetName());
        jTextFieldOrdnerChangeset.setText(newChangeset.get_folderPath());
        jTextFieldQuelleChangeset.setText(newChangeset.get_sourceName());
    }//GEN-LAST:event_tblChangesetsMouseClicked

    private void btnProjektTakeOverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProjektTakeOverActionPerformed
        // TODO add your handling code here:
        String projektOrdner = txtProjectFolder.getText();
        String xmlImport = txtXmlImport.getText();
        String changeSet = txtRetrievePackage.getText();
        String source = txtSource.getText();
        
        jTextFieldQuelleChangeset.setText(source);
        jTextFieldOrdnerChangeset.setText(projektOrdner);
        jTextFieldPackageChangeset.setText(changeSet);
        jTextFieldXmlImportChangeset.setText(xmlImport);
    }//GEN-LAST:event_btnProjektTakeOverActionPerformed

    private void jBtnChangesetGruppeHinzuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnChangesetGruppeHinzuActionPerformed
        // TODO add your handling code here:
        DefaultTableModel tModel = (DefaultTableModel)tblChangesets.getModel();
        
        String changesetGruppe = jTextFieldGruppenName.getText();
        String changesetZielName = jTextFieldPackageChangeset.getText();
        
        System.out.println("changesetGruppe::" + changesetGruppe);
        System.out.println("changesetZielName::" + changesetZielName);
        
        HashSet<String> set = this.opts.fromTargetNameToChangesetGroupNameSet.get(changesetGruppe);
        if(set == null){
            set = new HashSet<String>();
            this.opts.fromTargetNameToChangesetGroupNameSet.put(changesetGruppe,set);
        }
        
        Changeset newChangeset = new Changeset(
                changesetZielName
                ,jTextFieldXmlImportChangeset.getText()
                ,jTextFieldOrdnerChangeset.getText()
                ,jTextFieldQuelleChangeset.getText()
        );
        this.opts.fromNameToChangesetMap.put(changesetZielName,newChangeset);
        
        if(!set.contains(changesetZielName)){
            tModel.addRow(new Object[0]);
            tModel.setValueAt(changesetGruppe, tModel.getRowCount()-1, 0);
            tModel.setValueAt(changesetZielName, tModel.getRowCount()-1, 1);
            set.add(changesetZielName);
        }
    }//GEN-LAST:event_jBtnChangesetGruppeHinzuActionPerformed

    private void jBtnChangesetGruppeLoeschenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnChangesetGruppeLoeschenActionPerformed
        // TODO add your handling code here:
        DefaultTableModel tModel = (DefaultTableModel)tblChangesets.getModel();
        Integer rowIndex = -1;
        
        for(Integer i=0;i<tModel.getRowCount();i++){
            String zielName = tModel.getValueAt(i, 1) + "";
            String zielGruppe = tModel.getValueAt(i, 0) + "";
            
            if(jTextFieldPackageChangeset.getText().equals(zielName)){
                if(jTextFieldGruppenName.getText().equals(zielGruppe)){
                    HashSet<String> set = this.opts.fromTargetNameToChangesetGroupNameSet.get(zielGruppe);
                    if(set != null){
                        set.remove(zielName);
                    }
                    rowIndex = i;
                    break;
                }
            }
        }
        
        if(rowIndex >= 0){
            tModel.removeRow(rowIndex);
        }
    }//GEN-LAST:event_jBtnChangesetGruppeLoeschenActionPerformed

    private void btnRetrieveChangesetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRetrieveChangesetActionPerformed
        // TODO add your handling code here:
        String packageName = jTextFieldPackageChangeset.getText().trim();
        String projectPath = jTextFieldOrdnerChangeset.getText().trim();
        String buildFilePath = deprecated_Start.commandFolder.getPath() + "\\" + 
        projectPath +  "\\" + packageName;
        if(projectPath.contains(":")){
            buildFilePath = projectPath;
        }
        String sourceName = jTextFieldQuelleChangeset.getText();
        Long millis = System.currentTimeMillis();
        
        String zipPath = buildFilePath + "\\" + millis + "\\unpackaged.zip";
        
        String command = "start sfdx force:mdapi:retrieve -u " 
                + sourceName 
                + " -r \"" + buildFilePath + "\"/" + millis + " -p \"" 
                + packageName + "\"";
        command += "\njar xf \"" + zipPath + "\"";
        System.out.println("command:::" + command);
        
        createAndExecutePackages(buildFilePath.trim(),
                "retrievePackage",
                true,
                command,
                sourceName,
                packageName 
        );
        
        try {
            appendDeploymentInFile("retrieve:" + buildFilePath);
        } catch (IOException ex) {
            Logger.getLogger(Start_Frame.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Start_Frame.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnRetrieveChangesetActionPerformed

    private void btnProjektdatenSchreibenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProjektdatenSchreibenActionPerformed
        
        String projektOrdner = jTextFieldOrdnerChangeset.getText();
        String xmlImport = jTextFieldXmlImportChangeset.getText();
        String changeSet = jTextFieldPackageChangeset.getText();
        String source = jTextFieldQuelleChangeset.getText();
        
        txtSource.setText(source);
        txtProjectFolder.setText(projektOrdner);
        txtRetrievePackage.setText(changeSet);
        txtXmlImport.setText(xmlImport);
    }//GEN-LAST:event_btnProjektdatenSchreibenActionPerformed
    
    public static boolean switchOffDashDebug = false;
    
    public static boolean switchOffDebug = false;
    
     public static String linkXMLtagsToText_deprecated(
            Map<String,XMLtag> fromXMLnameToPreviousTagMap,
            String sourceCode
    ){
        String resultText = "";
        
        if(firstGeneralTag.startPos > 0){
            resultText += sourceCode.substring(0, firstGeneralTag.startPos);
        }
        
        Integer lastPos = firstGeneralTag.startPos;
        
        for(XMLtag doTag = firstGeneralTag.nextReplaceXML
                ;doTag != null
                ;doTag = doTag.nextGeneralXML
           )
        {
            String cleanedName = doTag.xmlName;
            if(cleanedName.startsWith("/"))cleanedName = cleanedName.substring(1);
            XMLtag lastTag = fromXMLnameToPreviousTagMap.get(cleanedName); 
            
            String tagString = null;
            
            if(lastTag != null){
                if(lastTag.thisReplaceXML  == null){
                    if(lastPos == firstGeneralTag.startPos){
                        lastTag.thisReplaceXML = lastTag.firstReplaceXML.nextReplaceXML;
                    }else{
                        lastTag.thisReplaceXML = lastTag.firstReplaceXML;
                    }
                }
                XMLtag nextTag = null;
                nextTag = lastTag.thisReplaceXML;
                if(nextTag != null){
                    tagString = sourceCode.substring(lastPos, nextTag.startPos);
                    if(!tagString.endsWith("/>") && nextTag == doTag){
                        tagString = tagString.substring(0,tagString.length()-1) + "/>";
                    }
                    tagString = Routines.getTabbedString(tagString
                            ,doTag.previousGeneralXML.tabNumber
                    );
                    lastTag.thisReplaceXML = nextTag.nextReplaceXML;
                }
            }
            
            if(tagString == null){
                tagString = sourceCode.substring(lastPos, doTag.startPos);
                tagString = Routines.getTabbedString(tagString
                        ,doTag.previousGeneralXML.tabNumber
                );
            }
           
            resultText += "\n" 
                    + tagString;
            lastPos = doTag.startPos;
        }
        
        resultText += "\n" + Routines.getTabbedString(
                sourceCode.substring(lastPos, sourceCode.length())
                ,firstGeneralTag.lastGeneralXML.tabNumber
        );
        
        return resultText;
     }
     
    //@deprecated
    public static String linkXMLtagsToText_backup(
            Map<String,XMLtag> fromXMLnameToPreviousTagMap,
            String sourceCode
    ){
        String resultText = "";
        Integer tabNumber = 0;
        Boolean previousHasBeenAdded = false;
        
        if(firstGeneralTag.startPos > 0){
            resultText += sourceCode.substring(0, firstGeneralTag.startPos);
        }
        Integer lastPos = resultText.length();
        Boolean hasAlreadyRun = false;
        
        for(XMLtag doTag = firstGeneralTag
                ;doTag != null
                ;doTag = doTag.nextGeneralXML
           )
        {
            //System.out.println("###doTag.xmlName::" + doTag.xmlName); 
            if(doTag.xmlName.startsWith("/")){
                tabNumber --;
            }
            XMLtag lastTag = fromXMLnameToPreviousTagMap.get(doTag.xmlName);
            
            if(lastTag != null && !lastTag.xmlName.startsWith("/")){
                XMLtag firstReplaceTag = lastTag.firstReplaceXML;
                
                String[] sourceCodeLinesList = sourceCode.substring(lastPos
                    , firstReplaceTag.startPos).split("\n");
                
                for(String lineStr:sourceCodeLinesList){
                    if(hasAlreadyRun){
                        resultText += "\n";
                    }
                            
                    resultText += Routines.getTabbedString(lineStr,tabNumber);
                    hasAlreadyRun = true;
                }
                
                sourceCodeLinesList = sourceCode.substring(firstReplaceTag.startPos
                    ,firstReplaceTag.startPos + firstReplaceTag.length - 1).split("\n");
                
                for(String lineStr:sourceCodeLinesList){
                    resultText += "\n" + Routines.getTabbedString(lineStr,tabNumber);
                }
                
                resultText += "/>";
                tabNumber--;
                lastPos = doTag.startPos;
                doTag = doTag.nextGeneralXML;
                previousHasBeenAdded = true;
                if(lastTag.getPreviousReplaceXML() == null){
                    fromXMLnameToPreviousTagMap.remove(lastTag.xmlName);
                }else{
                    lastTag = lastTag.getPreviousReplaceXML();
                    lastTag.firstReplaceXML = firstReplaceTag.nextReplaceXML;
                    fromXMLnameToPreviousTagMap.put(lastTag.xmlName,lastTag);
                }
            }else{
                String[] sourceCodeLinesList = sourceCode.substring(lastPos
                    , doTag.startPos).split("\n");
                for(String lineStr:sourceCodeLinesList){
                    resultText += "\n" + Routines.getTabbedString(lineStr,tabNumber);
                }
                if(previousHasBeenAdded){
                }
            }
            if(doTag == null)break;
            if(!doTag.xmlName.startsWith("/")){
                tabNumber ++;
            }
            lastPos = doTag.startPos;
        }
        String[] sourceCodeLinesList = sourceCode.substring(lastPos
                    , sourceCode.length()-1).split("\n");
        for(String lineStr:sourceCodeLinesList){
            resultText += "\n" + Routines.getTabbedString(lineStr,tabNumber-1);
        }
        
        return resultText + ">";
    }
    
    private static void cloneFileForObject(
            File f
            ,String objectName
            , String clonedObjFolder
    ) throws IOException{
        if(true)return;
        for(File ff:f.listFiles()){
            if(ff.isDirectory()){
                cloneFileForObject(ff,objectName,clonedObjFolder);
            }else{
                String content = FileHandling.readFile(ff);
            }
        }
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
            java.util.logging.Logger.getLogger(Start_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Start_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Start_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Start_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Start_Frame().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Start_Frame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    private static void unzip(
            String buildFilePath, 
            String txtSource,
            String txtRetrievePackage,
            String zipName
    ){
        String zipPath = buildFilePath + "\\" + zipName;

        String command = "start jar xf \"" + zipPath + "\"";

        createAndExecutePackages(buildFilePath.trim(),
            "retrievePackage",
            true,
            command,
                txtSource,
                txtRetrievePackage
        );
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btmWellFormedVFP;
    private javax.swing.JButton btnBackup;
    private javax.swing.JButton btnBackupANDDeploy;
    private javax.swing.JToggleButton btnBackupXML;
    private javax.swing.JButton btnCloneObj;
    private javax.swing.JButton btnClosingXMLelement;
    private javax.swing.JButton btnDeploy;
    private javax.swing.JButton btnDeployGroupDelete;
    private javax.swing.JButton btnDeploymentsVereinen;
    private javax.swing.JButton btnFnamesPackage;
    private javax.swing.JButton btnProjektTakeOver;
    private javax.swing.JButton btnProjektdatenSchreiben;
    private javax.swing.JButton btnPutNewLinesText;
    private javax.swing.JButton btnRetBackupSFDX;
    private javax.swing.JButton btnRetSFDX;
    private javax.swing.JButton btnRetrieve;
    private javax.swing.JButton btnRetrieveChangeset;
    private javax.swing.JButton btnRetrieveGroups;
    private javax.swing.JButton btnRetrieveSFDX;
    private javax.swing.JButton btnShowLines;
    private javax.swing.JButton btnTakeOverGroup;
    private javax.swing.JButton btnTakeOverTarget;
    private javax.swing.JButton btnToggleSrcTarget;
    private javax.swing.JButton btnUnzipOLD;
    private javax.swing.JButton btnUserNamesList;
    private javax.swing.JButton btnUserRegister;
    private javax.swing.JButton btnValDepSFDX;
    private javax.swing.JButton btnValDepSFDX1;
    private javax.swing.JToggleButton btnXmlImport;
    private javax.swing.JButton btnZielGruppe;
    private javax.swing.JButton btn_DEPLOY;
    private javax.swing.JCheckBox chkBoxBuildSourceIntoFolder;
    private javax.swing.JCheckBox chkChanged;
    private javax.swing.JCheckBox chkExisting;
    private javax.swing.JCheckBox chkMoved;
    private javax.swing.JCheckBox chkPROD;
    private javax.swing.JCheckBox chkRemoved;
    private javax.swing.JCheckBox chkTakeOverGroup;
    private javax.swing.JCheckBox chkZielgruppe;
    private javax.swing.JCheckBox chkZwischenspeicher;
    private javax.swing.JComboBox<String> cmbBoxZielgruppe;
    private javax.swing.JComboBox<String> cmbMetaData;
    private javax.swing.JComboBox<String> cmdMetaDataName;
    private javax.swing.JButton cmdRetrievePackage;
    private javax.swing.JComboBox<String> comboTestLevel;
    private javax.swing.JButton jBtnChangesetGruppeHinzu;
    private javax.swing.JButton jBtnChangesetGruppeLoeschen;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jChkAusblenden;
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
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
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
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelBasisfunktionen;
    private javax.swing.JPanel jPanelChangesets;
    private javax.swing.JPanel jPanelProjektdaten;
    private javax.swing.JPanel jPanelSourceTarget;
    private javax.swing.JPanel jPanelSystemInformation;
    private javax.swing.JPanel jPanelZielsysteme;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTabbedPane jTabPnDeployment;
    private javax.swing.JTabbedPane jTabRetrieve;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPane5;
    private javax.swing.JTabbedPane jTabbedPane6;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextField jTextFieldGruppenName;
    private javax.swing.JTextField jTextFieldOrdnerChangeset;
    private javax.swing.JTextField jTextFieldPackageChangeset;
    private javax.swing.JTextField jTextFieldQuelleChangeset;
    private javax.swing.JTextField jTextFieldRetrieveChangesetQuelle;
    private javax.swing.JTextField jTextFieldRetrieveGroup;
    private javax.swing.JTextField jTextFieldXmlImportChangeset;
    private javax.swing.JLabel lblLineOfCode;
    private javax.swing.JLabel lblPackage;
    private javax.swing.JLabel lblXmlImport;
    private javax.swing.JList<String> lstTestKlassen;
    private javax.swing.JSpinner spinMinValChanged;
    private javax.swing.JTable tableDeployment;
    private javax.swing.JTable tblChangesets;
    private javax.swing.JTable tblZiel;
    private javax.swing.JTextField textFieldZiel_2;
    private javax.swing.JTextArea txtAreaFieldNames;
    private javax.swing.JTextArea txtAreaOutputFIeldsPackage;
    private javax.swing.JTextField txtBuildProperties;
    private javax.swing.JTextField txtClosingXMLelement;
    private javax.swing.JTextField txtDataEnding;
    private javax.swing.JTextField txtDeployTo;
    private javax.swing.JTextField txtDeployZielGruppe;
    private javax.swing.JTextField txtDeploymentsVereinen;
    private javax.swing.JTextField txtDirectoryOld;
    private javax.swing.JTextField txtLoginURL;
    private javax.swing.JTextField txtMetadataName;
    private javax.swing.JTextField txtNewObjectName;
    private javax.swing.JTextField txtObjName;
    private javax.swing.JTextField txtPackageName;
    private javax.swing.JTextField txtProjectFolder;
    private javax.swing.JTextField txtProjectName;
    private javax.swing.JTextField txtRecentVal;
    private javax.swing.JTextField txtRetrieveFolderName;
    private javax.swing.JTextField txtRetrievePackage;
    private javax.swing.JTextField txtSource;
    private javax.swing.JTextField txtSubname;
    private javax.swing.JTextField txtTakeOverTarget;
    private javax.swing.JTextField txtTarget;
    private javax.swing.JTextField txtTargetData;
    private javax.swing.JTextField txtTestklasse;
    private javax.swing.JTextField txtTypeName;
    private javax.swing.JTextField txtUserName;
    private javax.swing.JTextField txtUsernameRegister;
    private javax.swing.JTextField txtXmlImport;
    private javax.swing.JTextField txtZielGruppe;
    private javax.swing.JTextArea txtZielLabelArea;
    private javax.swing.JTextField txtZusatzZiel;
    // End of variables declaration//GEN-END:variables
}