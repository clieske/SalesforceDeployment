/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.Deployment_Classes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author clieske
 */
public class Package {
    public Map<String,PackageType> types;
    public String path;
    public File packageFile;
    
    public Package(String path){
        this.path = path;
        this.types = new HashMap<String,PackageType>();
        this.packageFile = new File(path);
    }
    
    @Override
    public String toString(){
        String retStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        retStr += "<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">";
        for(PackageType pt:types.values()){
            retStr += "\n" + pt.toString();
        }
        retStr += "\n<version>43.0</version>";
        retStr += "\n</Package>";
        
        return retStr;
    }
    
    public static class PackageType{
        public String typeName;
        
        public Map<String,PackageMember> members;
        
        public PackageType(String typeName){
            this.typeName = typeName;
            
            this.members = new HashMap<String,PackageMember>();
        }
        
        @Override
        public String toString(){
            String retStr = "<types>";
            
            for(PackageMember pm:members.values()){
                retStr += "\n" + pm.toString();
            }
            
            retStr += "\n<name>" + typeName + "</name>";
            
            retStr += "\n</types>";
            
            return retStr;
        }
    }
    
    public static class PackageMember{
        public String memberName;
        
        public PackageMember(String memberName){
            this.memberName = memberName;
        }
        
        @Override
        public String toString(){
            return "<members>" + this.memberName + "</members>";
        }
                
    }
    
    public void writePackage() throws IOException{
        this.packageFile.getParentFile().mkdirs();
        if(!this.packageFile.exists()){
            this.packageFile.createNewFile();
        }
        
        FileWriter fw = new FileWriter(packageFile);
        BufferedWriter bw = new BufferedWriter(fw);
        
        bw.write(this.toString());
        
        bw.close();
    }
    
    
    public static Package createDeploymentPackage(
            String path,
            JTable table
    ){
        Package pck = new Package(path);
        
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        
        Set<String> fieldSet = new HashSet<String>();
        
        PackageType pType;
        PackageMember pMember;
        
        for(int row=0;row<dtm.getRowCount();row++){
            String typeName = dtm.getValueAt(row, 0) + "";
            String metaName = dtm.getValueAt(row, 1) + "";
            String ending = dtm.getValueAt(row, 2) + "";
            String dirName = dtm.getValueAt(row, 3) + "";
            
            if("CustomField".equals(typeName)){
                fieldSet.add(metaName);
            }
            
            pType = pck.types.get(typeName);
            if(pType == null){
                pType = new Package.PackageType(typeName);
                pck.types.put(typeName, pType);
            }
            
            pMember = new PackageMember(metaName);
            pType.members.put(metaName, pMember);
        }
        
        if(fieldSet.size()>0){
            pType = new Package.PackageType("PermissionSet");
            pck.types.put("PermissionSet", pType);
            pMember = new PackageMember("*");
            pType.members.put("*", pMember);
            
            pType = new Package.PackageType("Profile");
            pck.types.put("Profile", pType);
            pMember = new PackageMember("*");
            pType.members.put("*", pMember);
        }
        
        return pck;
    }
    
}
