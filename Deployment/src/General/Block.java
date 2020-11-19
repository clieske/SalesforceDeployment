/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package General;

import deployment.Deployment_Classes.Deployment.LineWrapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author clieske
 */
public class Block{
        public Boolean isActive;
        public int zeileVon;
        public int zeileBis;

        public Block(
                int zeileVon
        ){
            isActive = false;
            this.zeileVon = zeileVon;
            
            //System.out.println("addBlock " + this);
        }
        
        public String toString(){
            return "isActive: " + isActive + " von: " + zeileVon + " bis: " + zeileBis;
        }
        
        public static Map<Integer,Block> getAllBlocks(//from row to (from index to Block)
                String text,
                List<LineWrapper> wrapperList
        ){
            Map<Integer,Block> blocksMap 
                    = new HashMap<Integer,Block>();
            
            int zeile = 0;
            
            int vonZeile = 0;
            int bisZeile = 0;
            
            int ebene = 0;
            
            List<Integer> commentOpenings = new ArrayList<Integer>();
            List<Integer> commentClosings = new ArrayList<Integer>();

            List<Integer> openBlocks = new ArrayList<Integer>();
            List<Integer> closedBlocks = new ArrayList<Integer>();

            List<Integer> openMethods = new ArrayList<Integer>();
            List<Integer> closedMethods = new ArrayList<Integer>();

            List<Integer> lineEndings = new ArrayList<Integer>();
            
            Block b = null;
            LineWrapper lw = null;
            
            for(int i = 0;i < text.length();i++){
                String sign = text.substring(i,i+1);
                String nextSign = "";
                if(i+1 < text.length()){
                    nextSign = text.substring(i+1,i+2);
                }
                
                if("\n".equals(sign)){
                    zeile++;
                }else if(commentOpenings.size() == commentClosings.size()){
                    if("{".equals(sign)){
                        ebene ++;
                        openBlocks.add(zeile);
                        //System.out.println("\nplus Block: " + zeile);
                        if(openBlocks.size() - 2 == closedBlocks.size()){
                            lw = wrapperList.get(zeile);
                            if(lw== null || lw.getAcceptInResultingText()){
                                b = new Block(vonZeile);
                                //System.out.println("new Block: " + zeile);
                            }
                        }
                    }else if("}".equals(sign)){
                        ebene --;
                        closedBlocks.add(zeile);
                        if(openBlocks.size() - 1 == closedBlocks.size() 
                                && lw!= null 
                                && lw.getAcceptInResultingText()
                            ){
                            closedMethods.add(zeile);
                            bisZeile = zeile;

                            b.zeileBis = bisZeile;
                            
                            blocksMap.put(b.zeileVon, b);
                            
                            //System.out.println("add Block: " + b);
                            vonZeile = zeile+1;
                            //System.out.println("add new von : " + vonZeile);
                        }
                    }else if(";".equals(sign) && openBlocks.size() - 1 == closedBlocks.size()){
                        lineEndings.add(zeile);
                        if(openBlocks.size() - 1 == closedBlocks.size()){
                            vonZeile = zeile+1;
                            //System.out.println("add new von : " + vonZeile);
                        }
                    }
                    else if("//".equals(sign+nextSign)){
                        commentOpenings.add(zeile);
                        commentClosings.add(zeile);
                    }else if("/*".equals(sign+nextSign)){
                        ebene ++;
                        if(openBlocks.size() - 1 <= closedBlocks.size()){
                            b = new Block(zeile);
                        }
                        commentOpenings.add(zeile);
                        //System.out.println(zeile + "-> comment open:" + commentOpenings.size());
                        
                    }else if("(".equals(sign) 
                            && openBlocks.size() - 1 == closedBlocks.size()){
                        openMethods.add(zeile);
                    }else if(")".equals(sign) 
                            && openBlocks.size() - 1 == closedBlocks.size()){
                    } 
                }else{
                    if("*/".equals(sign+nextSign)){
                        ebene --;
                        commentClosings.add(zeile);
                        //System.out.println(zeile + "-> comment close:" + commentClosings.size());
                        if(openBlocks.size() - 1 <= closedBlocks.size()){
                            b.zeileBis = zeile;
                            blocksMap.put(b.zeileVon, b);
                        }
                    }
                }
                
                LineWrapper lw2 = wrapperList.get(zeile);
                if(lw2 != null){
                    lw2.ebene = ebene;
                }
            }
            
            return blocksMap;
        } 
        
    }
