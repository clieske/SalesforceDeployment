/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package General;

import java.io.Serializable;

/**
 *
 * @author Christoph Lieske
 */
public class Changeset implements Serializable{
    private String changesetName = null;
    private String xmlPackagePath = null;
    private String folderPath = null;
    private String sourceName = null;
    
    public Changeset(
            String changesetName
            ,String xmlPackagePath
            ,String folderPath
            ,String sourceName
    ){
        this.changesetName = changesetName;
        this.xmlPackagePath = xmlPackagePath;
        this.folderPath = folderPath;
        this.sourceName = sourceName;
    }
    
    public String get_changesetName(){
        return changesetName;
    }
    
    public String get_folderPath(){
        return folderPath;
    }
    
    public String get_sourceName(){
        return sourceName;
    }
    
    public String get_xmlPackagePath(){
        return xmlPackagePath;
    }
}
