/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.Deployment_Classes;

import deployment.deprecated_Start;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author clieske
 */
public class SaveRightsThread extends Thread{
    String metadataName;
    String subname;
    String folderName;
    JProgressBar progressSave;
    String targetFolder;
    
    Set<String> fields;//all fields like <Object>.<Fieldname>
    
    public SaveRightsThread(
            JTable table 
            ,String targetFolder
            ,JProgressBar progressSave
    ){
        this(null,"",progressSave,"objects",targetFolder);
        
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        for(int row=0;row<dtm.getRowCount();row++){
            String typeName = dtm.getValueAt(row, 0) + "";
            String metaName = dtm.getValueAt(row, 1) + "";
            
            if("CustomField".equals(typeName)){
                fields.add(metaName);
                
                //System.out.println(metaName);
            }
        }
    }
    
    public SaveRightsThread(
        String metadataName,
        String subname,
        JProgressBar progressSave,
        String folderName,
        String targetFolder
    ){
        this.metadataName = metadataName;
        this.subname = subname;
        this.progressSave = progressSave;
        this.targetFolder = targetFolder;
        this.folderName = folderName;
        
        this.fields = new HashSet<String>();
        if(metadataName != null)this.fields.add(metadataName + "." + subname);
    }
    
    public void run(){
        //System.out.println("run " + folderName + " " + subname);
        //TODO: Profile und PermSets mit dem aktuellen Feld überschreiben
        if("objects".equals(folderName) && subname != null){
            
            String source = deprecated_Start.backup.getPath() 
                + "\\retrieve\\";
            String target = deprecated_Start.backup.getPath() 
                    + "\\retrieve_QA\\";

            File[] targetFiles = deprecated_Start.getAllFieldFiles(target);
            File[] sourceFiles = deprecated_Start.getAllFieldFiles(source);
            
            int lastFieldPermission = -1;
            
            int progressValue = 0;
            
            progressSave.setStringPainted(true);
            progressSave.setMaximum(sourceFiles.length);
            progressSave.setMinimum(0);
            progressSave.setValue(0);
            
            for(File f:sourceFiles){
                progressSave.setValue(progressValue++);
                progressSave.repaint();
                
                File targetFile = new File(targetFolder
                    + "\\"
                    + f.getParentFile().getName() + "\\"
                    + f.getName());
                
                File deployFile = new File(deprecated_Start.backup.getPath() 
                        + "\\deployment\\" 
                        + f.getParentFile().getName() + "\\"
                        + f.getName()
                );
                
                //System.out.println(deployFile.getPath());
                
                if(f.exists()){
                    try {
                        //System.out.println(f.getName());
                        String sourceText = deprecated_Start.readFile(f.getPath());
                        String sourceVal = "";
                        String targetText = deprecated_Start.readFile(targetFile.getPath());
                        
                        for(String metaName:fields){
                            //System.out.println(f.getName()+" ");
                            
                            sourceVal = SaveRightsThread.getFieldPart("<fieldPermissions>",
                                    "<field>" + metaName + "</field>",
                                    "</fieldPermissions>",
                                    sourceText);
                            if(targetText==null){
                                targetText = sourceText;
                            }else{
                                targetText = SaveRightsThread.insertFieldPart("<fieldPermissions>",
                                    "<field>" + metaName + "</field>",
                                    "</fieldPermissions>",
                                    targetText,sourceVal);
                            }
                            
                            deprecated_Start.createFile(deployFile.getParent(),deployFile.getName(),targetText,true);
                            //System.out.println("Source: " + sourceText);
                            //System.out.println("Target: " + targetText);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(deprecated_Start.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                
                try {
                    this.wait(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SaveRightsThread.class.getName()).log(Level.SEVERE, null, ex);
                }catch(IllegalMonitorStateException ilEx){
                    
                }
            }
            
            progressSave.setValue(sourceFiles.length);
        }
    }
    
    private static String insertFieldPart(
        String begin,
        String contains,
        String ending,
        String text,
        String toInsert
    ){
        String val = SaveRightsThread.getFieldPart(begin,contains,ending,text);
        String retVal = null;
        if(toInsert == null)toInsert = "";
        
        int lastInd = text.lastIndexOf(begin);
        
        if(val == null){
            retVal = text.substring(0,lastInd) + toInsert + text.substring(lastInd);
        }else{
            retVal = text;
        }
        
        
        return retVal;
    }
    
    private static String getFieldPart(
        String begin,
        String contains,
        String ending,
        String text
    ){
        String fieldPart = null;
        
        Point interval = SaveRightsThread.getInterval(
                                    begin,
                                    contains,
                                    ending,
                                    text
                            );
        
        if(interval != null)fieldPart = text.substring(interval.x,interval.y);
        
        if(fieldPart != null){
            String sign = text.substring(interval.x, interval.x + 1);
            
            for(int i=interval.x;sign.equals("\n") && i>0;i--){
                fieldPart = sign + fieldPart;
                sign = text.substring(i-1,i);
            }
        }
        
        return fieldPart;
    }
    
    private static Point getInterval(
        String begin,
        String contains,
        String ending,
        String text
    ){
        Point interval = new Point();
        
        int index = text.indexOf(contains);
        int beginIndex = 0;
        int endIndex = 0;
        
        if(index > -1){
            for(;text.indexOf(begin, beginIndex + 1 ) > -1;){
                if(text.indexOf(begin, beginIndex)>-1){
                    beginIndex = text.indexOf(begin, beginIndex);
                }
            }
            
            endIndex = text.indexOf(ending,beginIndex) + ending.length();
            
            interval.setLocation(beginIndex, endIndex);
        }else{
            return null;
        }
                            
        return interval;
    }
}
