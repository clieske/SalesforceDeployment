/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package General;

import deployment.Deployment_Classes.Deployment.LineWrapper;
import deployment.Deployment_Classes.XMLtag;
import static deployment.Start_Frame.getCleanedName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author clieske
 */
public class Routines {
    public static XMLtag firstGeneralTag;
    public static String regexXMLelement 
            = "(<([/]{0,1}[^\\^\"^>^\\s]+)[\\s]{0,1}([^\"^/^>]*\"[^\"]*\")*)([/]{0,1})>";
    
    public static Object loadObject(File loadFile) 
            throws FileNotFoundException, IOException, ClassNotFoundException{
        if(loadFile == null || !loadFile.exists())return null;
        FileInputStream fis = new FileInputStream(loadFile.getPath());
        ObjectInputStream ois = new ObjectInputStream(fis);
        System.out.println("Routines.loadObject--loadFile::" + loadFile);
        if(!loadFile.exists())return null;
        Object obj = ois.readObject();
        ois.close();
        fis.close();
        
        return obj;
    }
    
    public static void saveObject(File saveFile,Serializable ser) 
            throws IOException,FileNotFoundException{
        saveFile.getParentFile().mkdirs();
        if(!saveFile.exists())saveFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(saveFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        
        oos.writeObject(ser);
        oos.close();
        fos.close();
    }
    
    public static SortedWrappers getSortedWrappers(List<LineWrapper> wrappersList){
        SortedWrappers sws = new SortedWrappers();
        
        sws.sortAfterMoveTo(wrappersList);
        
        return sws;
    }
    
    public static String getDashedText(
            Map<String,XMLtag> fromXMLnameToPreviousTagMap,
            String sourceCode
    ){
        return getDashedText(fromXMLnameToPreviousTagMap,sourceCode,false);
    }
    
    public static String getDashedText(
            Map<String,XMLtag> fromXMLnameToPreviousTagMap,
            String sourceCode,
            Boolean isOnlyLined
    ){
        System.out.println();
        System.out.println();
        System.out.println("METHOD:: getDashedText");
        System.out.println();
        
        String dashedText = "";
        int lastPos = 0;
        int correctionTabNumber = 0;
        
        for(XMLtag doTag=firstGeneralTag
                ;doTag != null
                ; doTag=doTag.nextGeneralXML
                ){
            String tagText = sourceCode.substring(lastPos, doTag.startPos).trim();
            tagText += sourceCode.substring(doTag.startPos, doTag.startPos + doTag.length).trim();
            if(lastPos > 0)dashedText += "\n";
            doTag.tabNumber -= correctionTabNumber;
            String cleanedName = getCleanedName(doTag.xmlName);
            XMLtag lastTag = fromXMLnameToPreviousTagMap.get(cleanedName);
            if(lastTag != null){
                XMLtag nextTag = null;
                if(lastTag.thisReplaceXML==null && !lastTag.hasAlreadyInit 
                        && lastTag.thisReplaceXML != lastTag.firstReplaceXML
                        ){
                    lastTag.thisReplaceXML = lastTag.firstReplaceXML;
                    lastTag.hasAlreadyInit = true;
                }
                nextTag = lastTag.thisReplaceXML;
                
                if(nextTag != null){
                    lastTag.thisReplaceXML = nextTag.nextReplaceXML;
                    String xmlName = nextTag.xmlName;
                    if(!(xmlName.startsWith("/") || xmlName.endsWith("/"))){
                        if(!isOnlyLined)tagText = tagText.substring(0,tagText.length()-1) + "/>";
                        correctionTabNumber++;
                    }
                }
            }
            dashedText += deleteEmptyLines(getTabbedString(tagText, doTag.tabNumber));
            lastPos = doTag.startPos + doTag.length;
        }
        return dashedText;
    }
    
    public static String deleteEmptyLines(String linedText){
        String shrinkedText = "";
        
        String[] lines = linedText.split("\n");
        
        Boolean hadSignsAlready = false;
                
        for(String lineText:lines){
            if(hadSignsAlready)shrinkedText += "\n";
            String nextText = lineText.trim();
            if(nextText.length() > 0){
                shrinkedText += nextText;
                hadSignsAlready = true;
            }
        }
        
        return shrinkedText;
    }
    
     public static String getLinedText(
            Map<String,XMLtag> fromXMLnameToPreviousTagMap,
            String sourceCode
    ){
        return getDashedText(fromXMLnameToPreviousTagMap,sourceCode,true);
    }
     
    public static String getTabbedString(String lineStr, Integer tabNumber){
        String tabbedString = "";
        
        for(Integer i=0;i<tabNumber;i++){
            tabbedString += "    ";
        }
        tabbedString += lineStr;
        
        return tabbedString;
    }
    
    public static class SortedWrappers{
        public HashMap<Integer,LineWrapper> lineSortedMap;
        public HashMap<Integer,List<LineWrapper>> wrappers;
        public Integer smallestValue;
        
        public SortedWrappers(){
            this.lineSortedMap = new HashMap<Integer,LineWrapper>();
            this.wrappers = new HashMap<Integer,List<LineWrapper>>();
        }
        
        public void sortAfterMoveTo(List<LineWrapper> wrappersList){
            this.wrappers.clear();
            for(LineWrapper lw:wrappersList){
                int insert = Integer.valueOf(lw.getInsertOnLine());
                
                if(lw == null)continue;
                if(this.smallestValue == null || this.smallestValue != null 
                        && insert < 
                        this.smallestValue){
                    this.smallestValue = insert;
                }
                List<LineWrapper> wrapperList = this.wrappers.get(insert);
                if(wrapperList == null){
                    wrapperList = new ArrayList<LineWrapper>();
                    this.wrappers.put(insert,wrapperList);
                }
                wrapperList.add(lw);
                this.lineSortedMap.put(lw.getZeile(),lw);
                
            }
        }
    }
}
