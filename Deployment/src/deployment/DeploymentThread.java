/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;

/**
 *
 * @author Christoph Lieske
 */
public class DeploymentThread extends Thread{
    private String deployZielGruppe;
    private HashMap<String,HashSet<String>> fromTargetNameToGroupNameSet;
    private String targetName;
    private String target2Name;
    private JTextField txtRetrievePackage;
    private JTextField txtProjectFolder;
    private JComboBox<String> comboTestLevel;
    private JTextField txtRetrieveFolderName;
    private JList<String> lstTestKlassen;
    private JTextField txtSource;
    private JTextField txtTarget;
    private JTextField txtTarget2;
    private JTextField txtXmlImport;
    
    public DeploymentThread(
            String deployZielGruppe
            ,HashMap<String,HashSet<String>> fromTargetNameToGroupNameSet
            ,JTextField txtTarget
            ,JTextField txtTarget2
            ,JTextField txtRetrievePackage
            ,JTextField txtProjectFolder
            ,JComboBox<String> comboTestLevel
            ,JTextField txtRetrieveFolderName
            ,JList<String> lstTestKlassen
            ,JTextField txtSource
            ,JTextField txtXmlImport
    ){
        System.out.println("DeploymentThread Constructor");
        this.deployZielGruppe = deployZielGruppe;
        this.fromTargetNameToGroupNameSet = fromTargetNameToGroupNameSet;
        this.targetName = txtTarget.getText();
        this.target2Name = txtTarget2.getText();
        this.txtRetrievePackage = txtRetrievePackage;
        this.txtProjectFolder = txtProjectFolder;
        this.comboTestLevel = comboTestLevel;
        this.txtRetrieveFolderName = txtRetrieveFolderName;
        this.lstTestKlassen = lstTestKlassen;
        this.txtSource = txtSource;
        this.txtTarget = txtTarget;
        this.txtTarget2 = txtTarget2;
        this.txtXmlImport = txtXmlImport;
    }
    
    private void deploy(String targetName) 
            throws IOException, FileNotFoundException, ClassNotFoundException{
        sleep();
        System.out.println("Threading Deploy");
        //targetName = txtTarget.getText();
        target2Name = txtTarget2.getText();
        
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
                + targetName 
                + "\" -f \"" 
                + zipPath 
                + "\" -l \"" 
                + testLevel 
                + "\" ";
        
        System.out.println("###command::" + command);
        
        if("RunSpecifiedTests".equals(testLevel)){
            command += "-r ";
            for(int i = 0; i< lstTestKlassen.getModel().getSize();i++){
                command += lstTestKlassen.getModel().getElementAt(i) + "";
            }
        }
        
        command += " --loglevel \"debug\""
                ;
        
        Start_Frame.createAndExecutePackages(buildFilePath.trim(),
                "retrievePackage",
                true,
                command,
                txtSource.getText(),
                txtRetrievePackage.getText() 
        );
        FileHandling.appendDeploymentInFile(
                "deploy:" 
                + txtRetrieveFolderName.getText()
                ,txtProjectFolder
                ,txtSource
                ,txtTarget
                ,txtRetrievePackage
                ,txtXmlImport
        );
    }
    
    public void run(){
        HashSet<String> set = fromTargetNameToGroupNameSet.get(deployZielGruppe);
        
        System.out.println("run has begun");
        
        if(targetName != null && targetName.length() > 0){
            if(set != null && !set.contains(targetName) || set==null)try {
                deploy(targetName);
                System.out.println("###targetName::" + targetName);
            } catch (IOException ex) {
                Logger.getLogger(DeploymentThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DeploymentThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(target2Name != null && target2Name.length() > 0){
            if(set != null && !set.contains(target2Name) || set == null)try {
                deploy(target2Name);
            } catch (IOException ex) {
                Logger.getLogger(DeploymentThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DeploymentThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(set != null){
            for(String zielName:set){
                try {
                    deploy(zielName);
                    System.out.println("###zielName::" + zielName);
                } catch (IOException ex) {
                    Logger.getLogger(DeploymentThread.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(DeploymentThread.class.getName()).log(Level.SEVERE, null, ex);
                }
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
}
