/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package General;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christoph Lieske
 */
public class Functions {
    
    //determines if debugs will be thrown or not
    private static Map<String,Boolean> debugMap = new HashMap<String,Boolean>();
    
    private static Boolean toDebug = true;
    
    private File foundFile = null;
    
    public File findFile(String fileName) {
        Functions fu = new Functions();
        String basePath = null;
        try {
            basePath = new java.io.File( "." ).getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        File f = new File(basePath).getParentFile().getParentFile();
        
        Functions.debugSwitchOn("Functions.findFile");
        Functions.debug("Functions.findFile", "url: " + f);
        Functions.debug("Functions.findFile", "fileName: " + fileName);
        
        foundFile = findFile(f,fileName);
        
        return foundFile; 
    }
    
    private File findFile(File folder,String fileName){
        File[] fileList = folder.listFiles();

        Functions.debug("Functions.findFile", "folder: " + folder);

        for(File fl:fileList){
            Functions.debug("Functions.findFile", "file: " + fl);
            Functions.debug("Functions.findFile", "name: " + fl.getName());
            Functions.debug("Functions.findFile", "isFile: " + fl.isFile());
            
            if(fl.isFile() && fl.getName().equals(fileName)){
                foundFile = fl;
                Functions.debug("Functions.findFile", "file finally FOUND: " + fl.getPath());
                return fl;
            }else if(fl.isDirectory()){
                if(foundFile != null)return foundFile;
                foundFile = findFile(fl,fileName);
            }
        }
        
        return foundFile;
    }
    
    private static void debug(String debugStr){
        if(Functions.toDebug)System.out.println(debugStr +  "----DEBUG ENDE!!!");
    }
    
    public static void debug(String debugFunction,String debugString){
        Boolean toDebug = debugMap.get(debugFunction);
        if(toDebug == null){
            debug("Function needs to be initialized first!!! debugSwitchOn    " 
                    + debugFunction);
        }
        
        if(toDebug){
            debug("## " + debugFunction + " : " + debugString);
        }
    }
    
    public static void debugSwitchOff(){
        Functions.toDebug = false;
    }
    
    public static void debugSwitchOff(String debugFunction){
        Boolean toDebug = debugMap.get(debugFunction);
        if(toDebug == null){
            debug("Function needs to be initialized first!!! debugSwitchOn");
        }
        debugMap.put(debugFunction,false);
    }
    
    public static void debugSwitchOn(){
        Functions.toDebug = true;
    }
    
    public static void debugSwitchOn(String debugFunction){
        Boolean toDebug = debugMap.get(debugFunction);
        if(toDebug != null && !toDebug){
            debug("function " + debugFunction + " has not to be switched off!!!");
            return;
        }
        debugMap.put(debugFunction,true);
    }
}
