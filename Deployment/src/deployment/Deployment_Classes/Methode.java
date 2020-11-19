/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.Deployment_Classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author clieske
 */
public class Methode {
    
    List<Parameter> params;
    String methName;
    public String returnVal;

    public int vonZeile;
    public int bisZeile;



    public Methode(
        String methName,
        int vonZeile,
        int bisZeile,
        String returnVal
    ){
        this.methName = methName;
        this.params = new ArrayList<Parameter>();
        this.vonZeile = vonZeile;
        this.bisZeile = bisZeile;
        this.returnVal = returnVal;
    }

    public void addParam(
        String typeName,
        String parName
    ){
        Parameter p = new Parameter(typeName,parName);
        this.params.add(p);
    }

    public String getMethName(){
        return this.methName;
    }

    public List<Parameter> getParams(){
        List<Parameter> params = new ArrayList();

        for(Parameter p:this.params){
            Parameter newPar = new Parameter(
                    p.getTypeName()+"",
                    p.getParName()+"");
            params.add(newPar);
        }

        return params;
    }
        
    public static Methode createMethod(
            String bereichstext,
            int vonZeile,
            int bisZeile
    ){
        String methodName = "";
        String retVal = "Constructor";
        
        boolean nameIsDefined = false;
        
        boolean openComment = false;
        boolean openLineComment = false;
        
        int zeile = 0;
        
        for(int i = 0;i<bereichstext.length();i++){
            String sign = bereichstext.substring(i,i+1);
            String nextSign = "";
            if(i<bereichstext.length() - 1)nextSign = bereichstext.substring(i+1, i+2);
            
            if("/*".equals(sign+nextSign)
                ){
                openComment = true;
            }else if("*/".equals(sign+nextSign)){
                openComment = false;
            }else if("//".equals(sign+nextSign)){
                openLineComment = true;
            }else if("\n".equals(sign)){
                openLineComment = false;
                zeile++;
            }else if("(".equals(sign)){
                while(!nameIsDefined && !openComment){
                    if(!openComment && !openLineComment){
                        for(int j=i-1;j > -1;j--){
                            sign = bereichstext.substring(j,j+1);
                            methodName = sign.trim() + methodName;
                            if(methodName.length() > 0 && sign.equals(" ")){
                                nameIsDefined = true;
                                break;
                            }    
                        }
                    }
                }
            }
        }
        
        Methode m = new Methode(methodName,vonZeile,bisZeile,retVal);
        
        return m;
    }
    
    @Override
    public String toString(){
        return methName + " " + vonZeile + " " + bisZeile; 
    }
    
    public static Map<Integer,Map<Integer,Methode>> getAllMethods(//from row to (from index to Methode)
        String text
    ){
        Map<Integer,Map<Integer,Methode>> methodMap = new HashMap<Integer,Map<Integer,Methode>>();
        
        List<Integer> commentOpenings = new ArrayList<Integer>();
        List<Integer> commentClosings = new ArrayList<Integer>();
        
        List<Integer> openBlocks = new ArrayList<Integer>();
        List<Integer> closedBlocks = new ArrayList<Integer>();
        
        List<Integer> openMethods = new ArrayList<Integer>();
        List<Integer> closedMethods = new ArrayList<Integer>();
        
        List<Integer> lineEndings = new ArrayList<Integer>();
        
        int zeile = 0;
        
        int vonZeile = 0;
        int bisZeile = -1;
        String bereichsText = "";
        
        for(int i = 0;i < text.length();i++){
            String sign = text.substring(i,i+1);
            String nextSign = "";
            if(i+1 < text.length()){
                nextSign = text.substring(i+1,i+2);
            }
            
            if(openBlocks.size() - 1 == closedBlocks.size()){
                bereichsText += sign;
            }
            
            if(commentOpenings.size() == commentClosings.size()){
                if("{".equals(sign)){
                    openBlocks.add(zeile);
                }else if("}".equals(sign)){
                    closedBlocks.add(zeile);
                    if(openMethods.size() - 1 == closedMethods.size()){
                        closedMethods.add(zeile);
                        bisZeile = zeile;
                        
                        Methode m = Methode.createMethod(bereichsText, vonZeile, bisZeile);
                        
                        Map<Integer,Methode> fromIndToMeth = methodMap.get(vonZeile);
                        if(fromIndToMeth==null){
                            fromIndToMeth = new HashMap<Integer,Methode>();
                            methodMap.put(vonZeile,fromIndToMeth);
                        }
                        fromIndToMeth.put(i, m);
                        bereichsText = "";
                    }else{
                        vonZeile = zeile+1;
                    }
                }else if(";".equals(sign)){
                    lineEndings.add(zeile);
                    if(openMethods.size() - 1 == closedMethods.size()){
                        vonZeile = zeile+1;
                    }
                    bereichsText = "";
                }
                else if("//".equals(sign+nextSign)){
                    commentOpenings.add(zeile);
                    commentClosings.add(zeile);
                }else if("/*".equals(sign+nextSign)){
                    commentOpenings.add(zeile);
                }else if("(".equals(sign) 
                        && openBlocks.size() - 1 == closedBlocks.size()){
                    openMethods.add(zeile);
                }else if(")".equals(sign) 
                        && openBlocks.size() - 1 == closedBlocks.size()){
                }
            }else{
                if("*/".equals(sign+nextSign)){
                    commentClosings.add(zeile);
                }
            }
            if("\n".equals(sign)){
                zeile++;
            }
        }
        
        /*System.out.println("open comments: " + commentOpenings);
        System.out.println("closed comments: " + commentClosings);
        System.out.println("open blocks: " + openBlocks);
        System.out.println("closed blocks: " + closedBlocks);
        System.out.println("methoden: " + methodMap);*/
        
        return methodMap;
    }
    
    public static class Parameter{
        String typeName;
        String parName;
        
        public Parameter(
            String typeName,
            String parName
        ){
            this.typeName = typeName;
            this.parName = parName;
        }
        
        public String getParName(){
            return this.parName;
        }
        
        public String getTypeName(){
            return this.typeName;
        }
    }
}
