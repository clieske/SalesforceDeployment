/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.Deployment_Classes;

import General.Routines;
import deployment.deprecated_Start;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author clieske
 */
public class SaveDeployment implements Serializable{
    public transient JList lstTestKlassen;
    public transient JTable tableDeployment;
    
    public List<String> testClasses;
    public List<DeploymentMeta> deploymentMeta;
    
    public static File optionsFile;
    
    public SaveDeployment(
        JList lstTestKlassen,
        JTable tableDeployment
    ){
        this.lstTestKlassen = lstTestKlassen;
        this.tableDeployment = tableDeployment;
        
        this.loadData();
    }
    
    public void loadData(){
        this.loadMetaData();
        this.loadTestClasses();
    }
    
    private void loadMetaData(){
        
        DefaultTableModel model;
        
        model = (DefaultTableModel) tableDeployment.getModel();
        
        if(this.deploymentMeta == null){
            this.deploymentMeta = new ArrayList<DeploymentMeta>();
            
            for(int i=0;i < model.getRowCount();i++){
                DeploymentMeta dpm = new DeploymentMeta(
                        model.getValueAt(i, 0) + "",
                        model.getValueAt(i, 1) + "",
                        model.getValueAt(i, 2) + "",
                        model.getValueAt(i, 3) + ""
                );
                
                this.deploymentMeta.add(dpm);
            }
        }else{
            for(DeploymentMeta dpm:this.deploymentMeta){
                model.addRow(new Object[]{
                    dpm.typeName,
                    dpm.metaName,
                    dpm.dataEnding,
                    dpm.folderName
                });
            }
        }
    }
    
    private void loadTestClasses(){
        
        ListModel model = this.lstTestKlassen.getModel();
        
        if(this.testClasses == null){
            this.testClasses = new ArrayList<String>();
            
            for(int i = 0;i < model.getSize();i++){
                String val = model.getElementAt(i) + "";
                this.testClasses.add(val);
            }
            
            
        }else{
            for(String testClass:this.testClasses){
                deprecated_Start.addListElement(lstTestKlassen, testClass);
            }
        }
    }
    
    public static SaveDeployment load(
        JList lstTestKlassen,
        JTable tableDeployment
    ) throws IOException, FileNotFoundException, ClassNotFoundException{
        optionsFile = new File(
            deprecated_Start.commandFolder.getPath() + deprecated_Start.projectName
            + "\\deployment.options");
        SaveDeployment sd = (SaveDeployment) Routines.loadObject(optionsFile);
        
        if(sd != null){
            sd.setComponents(lstTestKlassen, tableDeployment);
            sd.loadData();
        }
        
        return sd;
    }
    
    public void save() throws IOException{
        optionsFile = new File(
            deprecated_Start.commandFolder.getPath() + deprecated_Start.projectName
            + "\\deployment.options");
        this.testClasses = null;
        this.deploymentMeta = null;
        this.loadData();
        Routines.saveObject(optionsFile, this);
    }
    
    public void setComponents(
        JList lstTestKlassen,
        JTable tableDeployment
    ){
        this.lstTestKlassen = lstTestKlassen;
        this.tableDeployment = tableDeployment;
    }
    
    public class DeploymentMeta implements Serializable{
        public String typeName;
        public String metaName;
        public String dataEnding;
        public String folderName;
        
        public DeploymentMeta(
            String typeName,
            String metaName,
            String dataEnding,
            String folderName
        ){
            this.typeName = typeName;
            this.metaName = metaName;
            this.dataEnding = dataEnding;
            this.folderName = folderName;
        }
    }
}
