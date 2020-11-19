/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.Deployment_Classes;

import deployment.FileHandling;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author Christoph Lieske
 */
public class XMLDatenbank {
    public LinkedList<XMLNode> nodesList;
    public XMLNode actualNode;
    private XMLNode lastNode;
    private XMLNode firstNode;
    
    public HashMap<String,HashMap<String,XMLNode>> 
                fromTypeNameToFromFullNameToNodeMap;
    
    public XMLDatenbank(File readFile) throws IOException{
        nodesList = new LinkedList<XMLNode>();
        fromTypeNameToFromFullNameToNodeMap 
                = new HashMap<String,HashMap<String,XMLNode>>();
        readXMLFile(readFile);
    }
    
    public void firstNode(){
        actualNode = firstNode;
    }
    
    public void lastNode(){
        actualNode = lastNode;
    }
    
    public void nextNode(){
        if(actualNode.nextNode != null)actualNode = actualNode.nextNode;
    }
    
    public void previousNode(){
        if(actualNode.previousNode != null)actualNode = actualNode.previousNode;
    }
    
    public XMLNode newNode(String typeName){
        return newNode(typeName,">");
    }
    
    public XMLNode newNode(String typeName, String lastSign){
        XMLNode newNode = new XMLNode(typeName);
        newNode.previousNode = actualNode;
        
        if(actualNode != null){
            actualNode.nextNode = newNode;
            
        }else{
            firstNode = newNode;
        }
        actualNode = newNode;
        lastNode = newNode;
        //System.out.println("newNode:" + newNode);
        return newNode;
    }
    
    private void readParameters(String line){
        String paramName = null;
        String paramValue = null;
        String[] parameters = line.split("=");
        
        //System.out.println("parameters:" + parameters.length);
        //System.out.println("readParameters:" + line);
        
        for(Integer i = 0;i<parameters.length;i++){
            String lineAfterSplit = parameters[i];
            String[] paramNamesOrValues = lineAfterSplit.split(" ");
            //System.out.println("paramNamesOrValues:" + paramNamesOrValues.length);
            for(Integer j=0;j<paramNamesOrValues.length;j++){
                String value = paramNamesOrValues[j];
                if(value.contains("<")){
                    continue;
                }
                
                if(paramName == null){
                    paramName = value;
                }else{
                    paramValue = value.replace("\\?>", "").trim();
                }

                if(paramValue != null){
                    actualNode.addParamter(paramName, paramValue);
                    paramName = null;
                    paramValue = null;
                }
            }
        }
    }
    
    private String getTypeName(String line){
        String typeName = line.split(">")[0]
                        .replace("<", "")
                        .replace("\\?>","")
                        .replaceAll("\\?","").trim();
        return typeName;
    }
    
    private void readXMLFile(String fileContent){
        String[] lines = fileContent.split("\n");
        Boolean isFirstNode = true;
        
        for(Integer i = 0; i<lines.length; i++){
            String line = lines[i];
            if(line.startsWith("<?xml") || isFirstNode){
                String typeName = line.split(" ")[0]
                        .replace("<", "")
                        .replace("?>","")
                        .replaceAll(">","")
                        .replace(" ","").trim();
                XMLNode newNode = newNode(typeName,"");
                if(line.startsWith("<?xml")){
                    newNode.lastSign = "?>";
                }else {
                    newNode.lastSign = "";
                }
                readParameters(line);
            }
            if(!line.startsWith("<?xml") && line.startsWith("<") 
                    && !line.startsWith("</")){
                System.out.println("read typeLine:: " + line);
                isFirstNode = false;
                continue;
            }
            
            if(line.startsWith("<?xml")){
                continue;
            }
            
            if(line.trim().startsWith("<")){
                String typeName = getTypeName(line);
                
                XMLNode newNode = newNode(typeName);
                System.out.println("newNode:: " + newNode);
                System.out.println("typeName:: " + typeName);
                
                XMLNode newInnerNode = null;
                
                do{
                    //System.out.println("line:: " + line);
                    
                    if(line.trim().startsWith("<")){
                        String firstPartOfContent = "";
                        String[] lineSplit = line
                                .split(">");
                        if(lineSplit.length > 1){
                            firstPartOfContent = lineSplit[1];
                        }
                        if(firstPartOfContent == null){
                            firstPartOfContent = line;
                        }
                        firstPartOfContent = firstPartOfContent.split("</")[0];
                        
                        String lastPartOfContent = "";
                        
                        String innerTypeName = getTypeName(line);
                        if(innerTypeName.equals("fullName") 
                                || innerTypeName.equals("actionName")
                        ){
                            String fullName = null;
                            if(innerTypeName.equals("fullName")){
                                fullName = line
                                    .split("<fullName>")[1]
                                    .split("</fullName>")[0];
                            }else{
                                fullName = line
                                    .split("<actionName>")[1]
                                    .split("</actionName>")[0];
                            }
                            
                            newNode.nodeName = fullName;
                            HashMap<String,XMLNode> fromNameToNodeMap = 
                            fromTypeNameToFromFullNameToNodeMap.get(typeName);
                            if(fromNameToNodeMap == null){
                                fromNameToNodeMap = new HashMap<String,XMLNode>();
                                fromTypeNameToFromFullNameToNodeMap
                                        .put(typeName,fromNameToNodeMap);
                            }
                            fromNameToNodeMap.put(fullName,newNode);
                            System.out.println("added Node:: " 
                                    + fullName + " ;; " + newNode);
                        }
                        
                        i++;
                        if(i < lines.length){
                            line = lines[i];
                        }else{
                            break;
                        }
                        //System.out.println("line:: " + line);
                        while(!line.trim().startsWith("<")){
                            lastPartOfContent += line.split("</")[0];
                            i++;
                            if(i < lines.length){
                                line = lines[i];
                            }else{
                                break;
                            }
                            //System.out.println("line:: " + line);
                        }
                        /*System.out.println("firstPartOfContent:: " 
                                + firstPartOfContent);
                        System.out.println("lastPartOfContent:: " 
                                + lastPartOfContent);*/
                        
                        newInnerNode = newNode.addNode(innerTypeName);
                        newInnerNode.content 
                                = firstPartOfContent + lastPartOfContent;
                        //System.out.println("newInnerNode:: " + newInnerNode);
                    }
                }while(!line.trim().contains("</" + typeName));
            }
        }
    }
    
    private void readXMLFile(File readFile) throws IOException{
        System.out.println("readXMLFile:: " + readFile.getPath());
        
        String content = FileHandling.readFile(readFile);
        readXMLFile(content);
    }
    
    public void writeXMLFile(File writeFile) throws IOException{
        String content = firstNode + "";
        content += "\n" + firstNode.nextNode;
        
        for(String typeName : fromTypeNameToFromFullNameToNodeMap.keySet()){
            HashMap<String,XMLNode> fromFullNameToNodeMap = 
                    fromTypeNameToFromFullNameToNodeMap
                            .get(typeName);
            for(String fullName : fromFullNameToNodeMap.keySet()){
                XMLNode node = fromFullNameToNodeMap.get(fullName);
                content += "\n" + node.toString(true);
            }
        }
        content += "\n" + lastNode;
        //System.out.println("content::" + content);
        FileHandling.writeFile(content,writeFile);
    }
}
