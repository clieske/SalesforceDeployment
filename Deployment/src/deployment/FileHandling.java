package deployment;


import General.Routines;
import deployment.deprecated_Start;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JTextField;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Christoph Lieske
 */
public class FileHandling {
    
    public static void appendDeploymentInFile(
            String appendix
            ,JTextField txtProjectFolder
            ,JTextField txtSource
            ,JTextField txtTarget
            ,JTextField txtRetrievePackage
            ,JTextField txtXmlImport
    ) 
            throws IOException, 
                FileNotFoundException, 
                ClassNotFoundException{
        FileHandling.appendDeploymentInFile(appendix,null,txtProjectFolder,txtSource,txtTarget,txtRetrievePackage,txtXmlImport);
    }
    
    public static void appendDeploymentInFile(
            String appendix, 
            String additionalFilePath
            ,JTextField txtProjectFolder
            ,JTextField txtSource
            ,JTextField txtTarget
            ,JTextField txtRetrievePackage
            ,JTextField txtXmlImport
    ) throws IOException, FileNotFoundException, ClassNotFoundException {
        String fileName = txtProjectFolder.getText() 
                    + "\\deployments.csv";
        String mapFileName = txtProjectFolder.getText() 
                + "\\deployments_map.txt";
        String sourceText = txtSource.getText();
        String targetText = txtTarget.getText();
        String retrievePackageText = txtRetrievePackage.getText();
        String xmlImportText = txtXmlImport.getText();
        String projectFolderText = txtProjectFolder.getText();
        
        File deploymentFile = new File(fileName);
        File mapFile = new File(mapFileName);
        
        FileHandling.appendDeploymentInFile(
                deploymentFile,
                mapFile,
                appendix,
                sourceText,
                targetText,
                retrievePackageText,
                xmlImportText,
                projectFolderText
        );
        
        File ALL_deploymentFile = new File("\\ALL_deployments.csv");
        File ALL_mapFile = new File("\\ALL_deployments_map.txt");
        
        FileHandling.appendDeploymentInFile(
                ALL_deploymentFile,
                ALL_mapFile,
                appendix,
                sourceText,
                targetText,
                retrievePackageText,
                xmlImportText,
                projectFolderText
        );
    }
    
    public static void appendDeploymentInFile(
            String appendix, 
            String additionalFilePath,
            String fileName,
            String mapFileName,
            String sourceText,
            String targetText,
            String retrievePackageText,
            String xmlImportText,
            String projectFolderText
    ) {
        try {
            File deploymentFile = new File(fileName);
            File mapFile = new File(mapFileName);
            
            appendDeploymentInFile(
                    deploymentFile,
                    mapFile,
                    appendix,
                    sourceText,
                    targetText,
                    retrievePackageText,
                    xmlImportText,
                    projectFolderText
            );
            
            File ALL_deploymentFile = new File("\\ALL_deployments.csv");
            File ALL_mapFile = new File("\\ALL_deployments_map.txt");
            
            appendDeploymentInFile(
                    ALL_deploymentFile
                    ,ALL_mapFile
                    ,appendix
                    ,sourceText
                    ,targetText
                    ,retrievePackageText
                    ,xmlImportText
                    ,projectFolderText
            );
        } catch (IOException ex){
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void appendDeploymentInFile(
            File deploymentFile,
            File mapFile,
            String appendix,
            String sourceText,
            String targetText,
            String retrievePackageText,
            String xmlImportText,
            String projectFolderText
    ) 
            throws IOException, 
            FileNotFoundException, 
            ClassNotFoundException
    {
        String fieldNames = "Datum, Zeit, Nummer, Quelle von, Ziel nach, " 
                + "Changset_Name, File Path, Projekt_Ordner";
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
                for(String keyVal:loading.keySet()){
                    String valStr = loading.get(keyVal);
                    if(valStr.startsWith(firstField))continue;
                    String[] splittedVals = valStr.split(",");
                    String newLine = "";
                    for(String element:splittedVals){
                        if(newLine.length() > 0)newLine += ",";
                        String trimmedElement = element.trim().replaceAll("\"", "");
                        if(!trimmedElement.startsWith("\"")){
                            newLine += "\"";
                        }
                        newLine += trimmedElement;
                        if(!trimmedElement.trim().endsWith("\"")){
                            newLine += "\"";
                        }
                    }
                    loading.put(keyVal,newLine);
                }
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
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy.MM.dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat(
                "HH:mm:ss ");
            
            String newLoadingVal = "\"" 
                    + dateFormat.format(currentDate) + "\""  
                    + "," 
                    + "\"" + timeFormat.format(currentDate) + "\"" 
                    + "," 
                    + "\"" + appendix + "\""
                    + ","
                    + "\"" + sourceText + "\""
                    + ","
                    + "\"" + targetText + "\""
                    + ",\""
                    + retrievePackageText + "\""
                    + ",\""
                    + xmlImportText + "\""
                    + ",\""
                    + projectFolderText + "\""
                    ;
            
            loading.put(loading.size() + "",newLoadingVal);
            newContent += "\n";
            newContent += newLoadingVal;
            
            bw.write(newContent);
            bw.close();
            if(mapFile.exists())mapFile.delete();
            mapFile.createNewFile();
            Routines.saveObject(mapFile, (Serializable) loading);
    }
    
    public static String readFile(File loadFile) throws IOException{
        String content = "";
        
        FileInputStream fis = new FileInputStream(loadFile.getPath());
        BufferedInputStream bis = new BufferedInputStream(fis);
        
        InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
        
        BufferedReader br = new BufferedReader(isr);
        
        while(br.ready()){
            content += (char)br.read();
        }
        
        bis.close();
        fis.close();
        br.close();
        isr.close();
        
        //System.out.println("readFile::" + content);
        
        return content;
    }
    
    public static void writeFile(String fileContent, File buildFile) 
            throws IOException{
        if(!buildFile.exists()){
            buildFile.createNewFile();
        }
        FileWriter fw = new FileWriter(buildFile);
        BufferedWriter bw = new BufferedWriter(fw);

        bw.write(fileContent);
        
        bw.close();
        fw.close();
        
        //System.out.println("writeFile::" + fileContent);
    }
    
    private static void handleFolder(File sourceFolder
            , ZipOutputStream zout
            , File targetFolder
            , File folder
    ) 
            throws IOException{
        byte[] buffer = new byte[1024];
        
        for(File f : sourceFolder.listFiles()){
            if(f.isDirectory()){
                handleFolder(f,zout,targetFolder,new File(folder.getPath() 
                        + "\\" + f.getName()));
            }else{
                String zipPath = folder.getPath() + "\\" + f.getName(); 
                //System.out.println("zipPath::" + zipPath);
                zout.putNextEntry(new ZipEntry(zipPath));
                FileInputStream fis = new FileInputStream(
                        f.getPath()
                );
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zout.write(buffer, 0, len);
                }
            }
        }
    }
    
    public static void zipFile(File sourceFolder, File targetFolder) 
            throws IOException{
        /*System.out.println("zipFile::\nsourceFolder::" + sourceFolder 
                + "\ntargetFolder::" + targetFolder);*/
        if(sourceFolder.isDirectory() && targetFolder.isDirectory()){
            Long millis = System.currentTimeMillis();
            String parentPath = targetFolder
                            + "\\" + millis + "\\";
            
            File parentFolder = new File(parentPath);
            parentFolder.mkdirs();
            
            ZipOutputStream zout = new ZipOutputStream(
                    new FileOutputStream(parentPath + "unpackaged.zip")
            );
            
            handleFolder(sourceFolder,zout,targetFolder,
                    new File(sourceFolder.getName()));
            zout.closeEntry();
            zout.close();
        }
    }
}
