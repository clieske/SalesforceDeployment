/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package General;

import General.Routines;
import deployment.deprecated_Start;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author clieske
 */
public class Options implements Serializable{
    
    public String actualMetaName;
    public String actualTypeName;
    public String actualTarget;
    public String actualSource;
    public String actualEnding;
    public String actualFolder;
    public String buildPropertiesPath;
    public String chosenType;
    public String inputTextString;
    public String oldDeploymentPackageName;
    public String oldDeploymentPath;
    public String packageName;
    public String projectName;
    public String projectFolder;
    public String retrieveFolderName;
    public String subname;
    public String testLevel;
    public String xmlImportPath;
    public String zielDeployGruppe;
    public String zielGruppe;
    public String zusatzziel;
    
    public HashMap<String,HashSet<String>> fromTargetNameToGroupNameSet;
    public HashMap<String,HashSet<String>> fromTargetNameToChangesetGroupNameSet;
    public HashMap<String,Changeset> fromNameToChangesetMap;
    
    public Integer actualLength;
    
    public Map<String,String> properties;
    
    public Options(){
        properties = new HashMap<String,String>();
        fromTargetNameToGroupNameSet 
                = new HashMap<String,HashSet<String>>();
        fromTargetNameToChangesetGroupNameSet 
                = new HashMap<String,HashSet<String>>();
        fromNameToChangesetMap = new HashMap<String,Changeset>();
    }
    
    private String getPropertyValue(String property){
        String propertyValue = null;
        
        try{
            propertyValue = properties.get(property);
        }catch(NullPointerException npex){
            
        }
        
        return propertyValue;
    }
    
    private Integer getInteger(String property){
        String propertyValue = this.getPropertyValue(property);
        if(propertyValue==null)propertyValue ="0";
        return Integer.valueOf(propertyValue);
    }
    
    private void setPropertyValue(String property,String value){
        properties.put(property, value);
    }
    
    public static File optionsFile = new File(
            deprecated_Start.commandFolder.getPath() 
            + "\\options.options");
    
    public static File targetNamesFile = new File(
            deprecated_Start.commandFolder.getPath() 
            + "\\targetNamesFile.options");
    
    public static File targetChangesetNamesFile = new File(
            deprecated_Start.commandFolder.getPath() 
            + "\\targetChangesetNamesFile.options");
    
    public static File changesetFile = new File(
            deprecated_Start.commandFolder.getPath() 
            + "\\changesetsFile.options");
    
    public static Options loadOptions() 
            throws IOException, FileNotFoundException, ClassNotFoundException{
        
        Map<String,String> loading = null;
        if(optionsFile.exists())loading = 
                (Map<String,String>) Routines.loadObject(optionsFile);
        
        Options opt = new Options();
        
        if(targetNamesFile.exists())opt.fromTargetNameToGroupNameSet = 
                (HashMap<String,HashSet<String>>) 
                Routines.loadObject(targetNamesFile);
        
        if(changesetFile.exists())opt.fromNameToChangesetMap = 
                (HashMap<String,Changeset>) 
                Routines.loadObject(changesetFile);
        
        if(targetChangesetNamesFile.exists())opt.fromTargetNameToChangesetGroupNameSet = 
                (HashMap<String,HashSet<String>>) 
                Routines.loadObject(targetChangesetNamesFile);
        
        opt.properties = loading;
        opt.actualLength = opt.getInteger("actualLength");
        opt.actualTypeName= opt.getPropertyValue("actualMetaName");
        opt.actualTarget= opt.getPropertyValue("actualTarget");
        opt.actualSource= opt.getPropertyValue("actualSource");
        opt.actualEnding= opt.getPropertyValue("actualEnding");
        opt.actualFolder= opt.getPropertyValue("actualFolder");
        opt.buildPropertiesPath = opt.getPropertyValue("buildPropertiesPath");
        opt.chosenType= opt.getPropertyValue("chosenType");
        opt.inputTextString = opt.getPropertyValue("inputTextString");
        opt.oldDeploymentPackageName 
                = opt.getPropertyValue("oldDeploymentPackageName");
        opt.oldDeploymentPath 
                = opt.getPropertyValue("oldDeploymentPath");
        opt.packageName = opt.getPropertyValue("packageName");
        opt.projectName= opt.getPropertyValue("projectName");
        opt.projectFolder= opt.getPropertyValue("projectFolder");
        opt.retrieveFolderName = opt.getPropertyValue("retrieveFolderName");
        opt.subname= opt.getPropertyValue("subname");
        opt.testLevel = opt.getPropertyValue("testLevel");
        opt.xmlImportPath = opt.getPropertyValue("xmlImportPath");
        opt.zielGruppe = opt.getPropertyValue("zielGruppe");
        opt.zielDeployGruppe = opt.getPropertyValue("zielDeployGruppe");
        opt.zusatzziel = opt.getPropertyValue("zusatzziel");
        
        Functions.debugSwitchOn("Functions.loadOptions");
        Functions.debug("Functions.loadOptions", "optionsfile: " 
                + optionsFile.getPath() 
        );
        
        return opt;
    }
    
    public void saveOptions() throws IOException{
        if(properties==null)properties = new HashMap<String,String>();
        if(fromTargetNameToGroupNameSet==null) fromTargetNameToGroupNameSet 
                = new HashMap<String,HashSet<String>>();
        if(fromTargetNameToChangesetGroupNameSet==null) fromTargetNameToChangesetGroupNameSet 
                = new HashMap<String,HashSet<String>>();
        if(fromNameToChangesetMap==null){
            fromNameToChangesetMap = new HashMap<String,Changeset>();
        }
        setPropertyValue("actualLength",""+actualLength);
        setPropertyValue("actualMetaName",actualTypeName);
        setPropertyValue("actualTarget",actualTarget);
        setPropertyValue("actualSource",actualSource);
        setPropertyValue("actualEnding",actualEnding);
        setPropertyValue("actualFolder",actualFolder);
        setPropertyValue("buildPropertiesPath",buildPropertiesPath);
        setPropertyValue("chosenType",chosenType);
        setPropertyValue("inputTextString",inputTextString);
        setPropertyValue("oldDeploymentPackageName",oldDeploymentPackageName);
        setPropertyValue("oldDeploymentPath",oldDeploymentPath);
        setPropertyValue("packageName",packageName);
        setPropertyValue("projectName",projectName);
        setPropertyValue("projectFolder",projectFolder);
        setPropertyValue("retrieveFolderName",retrieveFolderName);
        setPropertyValue("subname",subname);
        setPropertyValue("testLevel",testLevel);
        setPropertyValue("xmlImportPath",xmlImportPath);
        setPropertyValue("zielDeployGruppe",zielDeployGruppe);
        setPropertyValue("zielGruppe",zielGruppe);
        setPropertyValue("zusatzziel",zusatzziel);
        
        optionsFile = new File(
            deprecated_Start.commandFolder.getPath() 
            + "\\options.options");
        targetNamesFile = new File(
            deprecated_Start.commandFolder.getPath() 
            + "\\targetNamesFile.options");
        Functions.debugSwitchOn("Functions.saveOptions");
        Functions.debug("Functions.saveOptions"
                , "optionsfile: " + optionsFile.getPath() 
        );
        
        if(optionsFile.exists())optionsFile.delete();
        optionsFile.getParentFile().mkdirs();
        Options.optionsFile.createNewFile();
        Routines.saveObject(optionsFile, (Serializable) properties);
        
        if(targetNamesFile.exists())targetNamesFile.delete();
        targetNamesFile.getParentFile().mkdirs();
        Options.targetNamesFile.createNewFile();
        Routines.saveObject(targetNamesFile
                , (Serializable) fromTargetNameToGroupNameSet
        );
        Routines.saveObject(targetChangesetNamesFile
                , (Serializable) fromTargetNameToChangesetGroupNameSet
        );
        Routines.saveObject(changesetFile
                , (Serializable) fromNameToChangesetMap
        );
        
        
    }
}
