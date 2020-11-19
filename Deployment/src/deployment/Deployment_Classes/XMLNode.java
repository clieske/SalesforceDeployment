/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.Deployment_Classes;

import java.util.LinkedList;

/**
 *
 * @author Christoph Lieske
 */
public class XMLNode {
    private String typeName;
    public String nodeName;
    public String content;
    public String lastSign;
            
    LinkedList<XMLNode> nodesList;
    LinkedList<XMLParameter> paramtersList;
    
    public XMLNode nextNode;
    public XMLNode previousNode;
    
    public XMLNode(String typeName){
        this(typeName,">");
    }
    
    public XMLNode(String typeName,String lastSign){
        this.typeName = typeName.trim();
        nodesList = new LinkedList<XMLNode>();
        paramtersList = new LinkedList<XMLParameter>();
        this.lastSign = lastSign.trim();
    }
    
    public XMLNode addNode(String typeName){
        XMLNode newNode = new XMLNode(typeName);
        nodesList.add(newNode);
        return newNode;
    }
    
    public void addParamter(String parameterName, String parameterValue){
        if(parameterName != null){
            if(parameterValue != null){
                XMLParameter param 
                        = new XMLParameter(parameterName,parameterValue);
                paramtersList.add(param);
                //System.out.println("XMLParameter::" + param);
            }
        }
    }
    
    public String toString(){
        return toString(false);
    }
    
    public String toString(Boolean withNodesList){
        String retStr = "<" + typeName; 
        for(XMLParameter param:paramtersList){
            retStr += " " + param;
        }
        retStr += lastSign;
        
        if(content != null){
            retStr += content;
            retStr += "</" + typeName + ">";
        }
        
        if(withNodesList){
            for(XMLNode node:nodesList){
                retStr += "\n" + node;
            }
        }
        
        return retStr;
    }
}
