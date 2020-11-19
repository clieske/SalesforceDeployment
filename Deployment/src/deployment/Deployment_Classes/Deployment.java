/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.Deployment_Classes;
 
import General.Routines;
//import deployment.Deployment_Classes.Deployment_Master;
import General.Routines.SortedWrappers;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author clieske
 */
public class Deployment  implements Serializable{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
    public String textAlt;
    public String textNeu;
    
    public List<LineWrapper> zeilenAlt;
    public List<LineWrapper> zeilenNeu;
    
    private Map<String,LineWrapper> zeilenMapAlt;
    private Map<String,LineWrapper> zeilenMapNeu;
    
    public List<LineWrapper> zeilenEntstanden;
    
    private Map<String,Map<Integer,Integer>> fromTextKindAndRowToTargetRow;
    
    public Map<Integer,List<LineWrapper>> removedOld;
    
    public List<Block> blocks;
    public Map<Integer,Block> blocksMap;
    public Map<Integer,Block> toggleBlocksMap;
    
    public String metaDataType;
    
    public static LineRenderer renderer;
    
    public Deployment(){
        this("","",true,true,true,true,2,null);
    }
    
    public Deployment(
            String textAlt,
            String textNeu,
            Boolean addRemoved,
            Boolean addChangedOld,
            Boolean addExistingOld,
            Boolean addMoved,
            Integer minLenChanged,
            String metaDataType
    ){
        this.metaDataType = metaDataType;
        this.textAlt = textAlt;
        this.textNeu = textNeu;
        this.removedOld = new HashMap<Integer,List<LineWrapper>>();
        
        this.fromTextKindAndRowToTargetRow = new HashMap<String,Map<Integer,Integer>>();
        this.blocks = new ArrayList<Block>();
        this.blocksMap = new HashMap<Integer,Block>();
        this.toggleBlocksMap = new HashMap<Integer,Block>();
        
        compare(
                addRemoved,
                addChangedOld,
                addExistingOld,
                addMoved,
                minLenChanged
        );
        
        this.createLineRenderer();
    }
    
    public void createLineRenderer(){
        if(Deployment.renderer == null){
            Deployment.renderer = new LineRenderer();
        }
    }
    
    public static Block createBlock(Integer zeileVon){
        return new Block(zeileVon);
    }
    
    public static List<String> getListFromArray(String[] array){
        List<String> objectList = new ArrayList<String>();
        
        for(String obj:array){
            objectList.add(obj);
        }
        
        return objectList;
    }
    
    public void compare(
            Boolean addRemoved,
            Boolean addChangedOld,
            Boolean addExistingOld,
            Boolean addMoved,
            Integer minLenChanged
    ){
        Boolean isInComment = false;
        int openCommentInLine = -1;
        int openCommentInColumn = -1;
        int openBlocks = 0;
        Block actualBlock = null;
        
        if(textAlt == null)textAlt = "";
        if(textNeu == null)textNeu = "";
        
        zeilenAlt = new ArrayList<LineWrapper>();
        zeilenNeu = new ArrayList<LineWrapper>();
        zeilenEntstanden = new ArrayList<LineWrapper>();
            
        zeilenMapAlt = new HashMap<String,LineWrapper>();
        zeilenMapNeu = new HashMap<String,LineWrapper>();
        
        Integer iAlt = -1;
        Integer iNeu = -1;
        
        Integer zeichenPosAlt = 0;
        Integer zeichenPosNeu = 0;
        
        String zeichenStringNeu = textNeu;
        String zeichenStringAlt = textAlt;
        
        List<String> textListAlt = Deployment.getListFromArray(textAlt.split("\n"));
        List<String> textListNeu = Deployment.getListFromArray(textNeu.split("\n")); 
        
        if(textListAlt.size()==1)textListAlt.remove(0);
        if(textListNeu.size()==1)textListNeu.remove(0);
        
        for(Integer i=0;i<textAlt.length();){
            String lineStr = "";
            Integer lineInt = -1;
            if(zeichenStringAlt.contains("\n")){
                lineInt = zeichenStringAlt.indexOf("\n");
                if(lineInt > 0){
                    lineStr = zeichenStringAlt.substring(0,lineInt);
                    i += lineInt+1;
                    zeichenStringAlt = zeichenStringAlt.substring(lineInt+1);
                }else{
                    lineStr = "";
                    zeichenStringAlt = zeichenStringAlt.substring(1);
                    i++;
                }
                
                iAlt++;
            }else{
                lineStr = zeichenStringAlt.substring(0);
                i=textAlt.length();
                iAlt++;
            }
            
            String lineAlt =lineStr.trim();
            
            LineWrapper exLw = zeilenMapAlt.get(lineAlt);
            Integer firstLine;
            
            if(exLw != null){
            	firstLine = exLw.firstLine;
            }else{
            	firstLine = iAlt;    
            }
            LineWrapper lw = new LineWrapper(
                iAlt,
                firstLine,
                lineStr,
                lineAlt,
            	"existing",
                exLw,
                this
            );
            lw.setSource("alt");
            zeilenAlt.add(lw);
            lw.isOld = true;
            zeilenMapAlt.put(lineAlt,lw);
            
        }
        
        for(Integer i=0;i<textNeu.length();){
            String lineStr = "";
            Integer lineInt = -1;
            if(zeichenStringNeu.contains("\n")){
                lineInt = zeichenStringNeu.indexOf("\n");
                if(lineInt > 0){
                    lineStr = zeichenStringNeu.substring(0,lineInt);
                    i += lineInt+1;
                    zeichenStringNeu = zeichenStringNeu.substring(lineInt+1);
                }else{
                    lineStr = "";
                    zeichenStringNeu = zeichenStringNeu.substring(1);
                    i++;
                }
                
                iNeu++;
            }else{
                lineStr = zeichenStringNeu.substring(0);
                i=textNeu.length();
                iNeu++;
            }
            
            String lineAlt = lineStr.trim();
            
            LineWrapper exLw = zeilenMapNeu.get(lineAlt);
            Integer firstLine;
            
            if(exLw != null){
            	firstLine = exLw.firstLine;
            }else{
            	firstLine = iAlt;    
            }
            LineWrapper lw = new LineWrapper(
                iNeu,
                firstLine,
                lineStr,
                lineAlt,
            	"existing",
                exLw,
                this
            );
            lw.setSource("neu");
            zeilenNeu.add(lw);
            lw.isOld = false;
            
            String addLine = "";
            if(i<textNeu.length())addLine="\n";
            
            LineWrapper lwE = new LineWrapper(
                iNeu,
                firstLine,
                lineStr + addLine,
                lineAlt,
            	"",
                exLw,
                this
            );
            zeilenEntstanden.add(lwE);
            lwE.setSource("neu");
            lwE.isOld = false;
            Deployment.putRows(fromTextKindAndRowToTargetRow, "neu", iNeu, iNeu);
            zeilenMapNeu.put(lineAlt,lw);
        }
        
        Integer zeileAlt = 0;
        Integer zeileNeu = 0;
        
        Integer zeichenPosZeileAlt = 0;
        Integer zeichenPosZeileNeu = 0;
                 
        String bereichTextAlt = "";
        String bereichTextNeu = "";
        
        String bereichTextzeileAlt = "";
        String bereichTextzeileNeu = "";
        
        Boolean gleicherBereich = true;
        
        Map<Integer,LineWrapper> wrapperMap = 
                new HashMap<Integer,LineWrapper>();
        int highestRow = -1;
        
        do{
            LineWrapper lwAlt = null;
            if(zeileAlt < zeilenAlt.size()) lwAlt = zeilenAlt.get(zeileAlt);
            else{
            	lwAlt = new LineWrapper(this);
            }
            
            LineWrapper lwNeu = null;
            if(zeileNeu < zeilenNeu.size()) lwNeu = zeilenNeu.get(zeileNeu);
            else{
            	lwNeu = new LineWrapper(this);    
            }
            
            String zeilenStringAlt = lwAlt.value;
            String zeilenStringNeu = lwNeu.value;
            
            Boolean hasNachfolger = false;
            Boolean hasNachfolgerNeu = false;
            
            LineWrapper altLineWrap = zeilenMapAlt.get(zeilenStringNeu);
            LineWrapper neuLineWrap = zeilenMapNeu.get(zeilenStringAlt);
            
            Integer zeileWohin = 0;
            Integer zeileWoher = 0;
            
            if(altLineWrap != null){
                if(altLineWrap.firstNamenWrapper.aktuelleWahl != null)
                    zeileWoher = altLineWrap.firstNamenWrapper.aktuelleWahl.zeile;
                hasNachfolger = altLineWrap.firstNamenWrapper.hasNachfolgerGoForward();
            }
            
            if(neuLineWrap != null){
                if(neuLineWrap.firstNamenWrapper.aktuelleWahl != null)
                    zeileWohin = neuLineWrap.firstNamenWrapper.aktuelleWahl.zeile;
                hasNachfolgerNeu = neuLineWrap.firstNamenWrapper.hasNachfolgerGoForward();
            }
            
            if(zeilenStringAlt.equals(zeilenStringNeu) && (hasNachfolgerNeu || hasNachfolger)){
            	zeichenPosAlt += zeilenStringAlt.length();
                zeichenPosNeu += zeilenStringNeu.length();
                
                bereichTextAlt += '\n' + zeilenStringAlt;
                bereichTextNeu += '\n' + zeilenStringNeu;
            }else{
                bereichTextAlt = "";
                bereichTextNeu = "";
                
                if(hasNachfolgerNeu){
                    lwAlt.setStyleClass("moved");
                    lwAlt.setZeileMove(zeileWohin);
                }else{
                    lwAlt.setStyleClass("removed");
                    
                    for(LineWrapper zNeu:zeilenNeu){
                        if(lwAlt.value==zNeu.value){
                            lwAlt.setStyleClass("moved");
                            lwAlt.setZeileMove(zNeu.zeile);
                            zNeu.setZeileMove(lwAlt.zeile);
                            break;
                        }
                        else{
                            lwAlt.setAcceptInResultingText(false);
                            
                            if(lwAlt.value.replaceAll(" ", "").contains(zNeu.value.replaceAll(" ", "")) && (zNeu.value.length() >= minLenChanged)){
                               if(zNeu.zeile > zeileAlt){
                                   lwAlt.setStyleClass("changed");
                                   lwAlt.setZeileMove(zNeu.zeile);
                                   zNeu.setStyleClass("changed");
                                   
                                   break;    
                               }
                           }
                            if(zNeu.value.contains(lwAlt.value) && (lwAlt.value.length() >= minLenChanged) ){
                                if(zNeu.zeile > zeileNeu){
                                    
                                    lwAlt.setStyleClass("changed");
                                    lwAlt.setZeileMove(zNeu.zeile);
                                    
                                    zNeu.setStyleClass("changed");
                                    zNeu.setZeileMove(lwAlt.zeile);
                                    
                                    break;    
                                }    
                            }
                        }   
                    }
                }
                
                if(hasNachfolger){
                    lwNeu.setStyleClass("moved");
                    lwNeu.setZeileMove(zeileWoher);
                }else{
                    if("existing".equals(lwNeu.styleClass)){
                        lwNeu.setStyleClass("added");
                    }
                }
                
            	do{
                    zeichenStringAlt = "";
                    if(zeichenPosAlt < textAlt.length()) zeichenStringAlt = 
                            textAlt.substring(zeichenPosAlt,zeichenPosAlt+1);
                    zeichenStringNeu = "";
                    if(zeichenPosNeu < textNeu.length()) zeichenStringNeu = 
                            textNeu.substring(zeichenPosNeu,zeichenPosNeu+1);
                    
                    zeichenPosAlt++;
                    zeichenPosNeu++;
                }while(zeichenPosAlt < textAlt.length() || zeichenPosNeu < textNeu.length());
            }
            
            zeileAlt++;
            zeileNeu++;
            
            if("removed".equals(lwAlt.styleClass)){
                Integer inserValue = Integer.valueOf(lwAlt.insertOnLine);
                    
                List<LineWrapper> lwList = this.removedOld.get(inserValue);
                if(lwList==null){
                    lwList = new ArrayList<LineWrapper>();
                    this.removedOld.put(inserValue,lwList);
                    lwList.add(lwAlt); 
                }
                
                if(addRemoved){
                    wrapperMap.put(lwAlt.zeile, lwAlt);
                }
                
                lwAlt.foreGroundColor = Color.red;
            }else if("existing".equals(lwAlt.styleClass)){
                if(addExistingOld){
                    wrapperMap.put(lwAlt.zeile, lwAlt);
                }
                lwAlt.foreGroundColor = Color.green;
            }else if("changed".equals(lwAlt.styleClass)){
                if(addChangedOld){
                    wrapperMap.put(lwAlt.zeile, lwAlt);
                }
                lwAlt.foreGroundColor = Color.gray;
            }else if("moved".equals(lwAlt.styleClass)){
                if(addMoved){
                    wrapperMap.put(lwAlt.zeile, lwAlt);
                }
                lwAlt.foreGroundColor = Color.orange;
            }
            
            if("removed".equals(lwNeu.styleClass)){
                lwNeu.foreGroundColor = Color.red;
            }else if("existing".equals(lwNeu.styleClass)){
                lwNeu.foreGroundColor = Color.green;
            }else if("changed".equals(lwNeu.styleClass)){
                lwNeu.foreGroundColor = Color.gray;
            }else if("moved".equals(lwNeu.styleClass)){
                lwNeu.foreGroundColor = Color.orange;
            }else{
                lwNeu.foreGroundColor = Color.BLUE;
            }
            
            if(lwAlt.zeile > highestRow){
                highestRow = lwAlt.zeile;
            }
            
            int indLineComment = lwAlt.getValue().indexOf("//");
            int indCommentOpen = lwAlt.getValue().indexOf("/*");
            int indCommentClose = lwAlt.getValue().indexOf("*/");
            int indBlockOpen = -1;
            int indBlockClose = -1;
            
            if("ApexClass".equals(this.metaDataType)){
                
            }else /*if("ApexPage".equals(this.metaDataType))*/{
                if(indCommentOpen < 0){
                    indCommentOpen = lwAlt.getValue().indexOf("<!--*");
                }

                if(indCommentClose < 0){
                    indCommentClose = lwAlt.getValue().indexOf("-->");
                }
            }
            
            if(indCommentOpen > -1){
                if(!isInComment && (indLineComment > -1 && indLineComment > indCommentOpen 
                        || indLineComment <= -1)){
                        openCommentInLine = lwAlt.zeile;
                        openCommentInColumn = indCommentOpen;
                }
                isInComment = isInComment 
                        || indLineComment > -1 && indLineComment > indCommentOpen 
                        || indLineComment <= -1;
            }
            
            if(indCommentClose > -1){
                isInComment = false;
            }
            
            if(!isInComment){
                do{
                    if("ApexClass".equals(this.metaDataType)){
                        indBlockOpen = lwAlt.getValue().indexOf("{",indBlockOpen);
                    }else /*if("ApexPage".equals(this.metaDataType) && lwAlt.getValue().indexOf("</",indBlockOpen) < 0 )*/{
                        indBlockOpen = lwAlt.getValue().indexOf("<",indBlockOpen);
                    }
                    
                    if(indBlockOpen > -1){
                        if(openBlocks == 1){
                            actualBlock = new Block(lwAlt.zeile);
                        }
                        openBlocks++;
                        indBlockOpen++;
                    }
                }while(indBlockOpen > -1);
                 
                do{
                    if("ApexClass".equals(this.metaDataType)){
                        indBlockClose = lwAlt.getValue().indexOf("}",indBlockClose);
                    }else /*if("ApexPage".equals(this.metaDataType))*/{
                        if(indBlockClose < 0){
                            indBlockClose = lwAlt.getValue().indexOf("/>",indBlockClose);
                        }

                        if(indBlockClose < 0){
                            indBlockClose = lwAlt.getValue().indexOf("</",indBlockClose);
                        }    
                    }
                    
                    if("ApexClass".equals(this.metaDataType) 
                            && indBlockClose > -1){
                        if(openBlocks == 2 && actualBlock.zeileVon < lwAlt.zeile){
                            actualBlock.zeileBis = lwAlt.zeile;
                            blocks.add(actualBlock);
                            blocksMap.put(actualBlock.zeileVon,actualBlock);
                            actualBlock = null;
                        }
                        
                        indBlockClose++;
                        openBlocks--;
                    }
                }while(indBlockClose > -1 && "ApexClass".equals(this.metaDataType));
            }
            lwAlt.setAcceptInResultingText(false);
            //System.out.println("neu: " + zeileNeu + " alt: " + zeileAlt);
        }while(zeileAlt < zeilenAlt.size() || zeileNeu < zeilenNeu.size());
        
        /*
        List<LineWrapper> bufferList = new ArrayList<LineWrapper>();
        
        for(Integer i=0;i<highestRow;i++){
            LineWrapper lw = wrapperMap.get(i);
            
            if(lw!=null)bufferList.add(lw);
        }
        zeilenAlt = bufferList;*/
    }
    
    public static void putRows(
    	Map<String,Map<Integer,Integer>> fromTextKindAndRowToTargetRow,
        String source,
        Integer fromRow,
        Integer targetRow
    ){
    	Map<Integer,Integer> fromOldLineToOutputLine = fromTextKindAndRowToTargetRow.get(source);
        if(fromOldLineToOutputLine==null){
            fromOldLineToOutputLine = new HashMap<Integer,Integer>();
            fromTextKindAndRowToTargetRow.put(source,fromOldLineToOutputLine);    
        }
        fromOldLineToOutputLine.put(fromRow,targetRow);
    }
    
    public void save(){
        String result = zeilenEntstanden.get(0).value;
           
        for(Integer i = 1;i<zeilenEntstanden.size();i++){
            LineWrapper wrap = zeilenEntstanden.get(i);
            if(wrap.sourceValue!=null)result += '\n' + wrap.sourceValue;
        }
        
        //upsert this.compare;
    }
    
    public static void putLinesViaBlock(
            Integer i,
            List<LineWrapper> zeilen,
            Map<Integer,Boolean> writtenLines,
            List<LineWrapper> zeilenEntstanden,
            Collection<Block> blocks
    ){
        Block blk = null;
        for(Block b:blocks){
            if(b.zeileVon <= i){
                blk = b;
                break;
            }
        }

        if(blk != null && blk.isActive){
            for(Integer j=blk.zeileVon;j<zeilen.size() && j<=blk.zeileBis;j++){
                if(!writtenLines.containsKey(j)){
                    LineWrapper lw = zeilen.get(j);
                    if(lw != null && lw.getAcceptInResultingText()){
                        zeilenEntstanden.add(lw);
                        writtenLines.put(j,true);
                    }
                }
            }
        }
    }
    
    public void toggleOutput(){
        this.zeilenEntstanden.clear();
        
        Map<Integer,Boolean> writtenLines = new HashMap<Integer,Boolean>();
        
        Map<Integer,Boolean> neueWrittenLines = new HashMap<Integer,Boolean>();
        Map<Integer,Boolean> alteWrittenLines = new HashMap<Integer,Boolean>();
        
        for(int i=0;i<this.zeilenAlt.size();i++){
            alteWrittenLines.put(i,false);
        }
        
        for(int i=0;i<this.zeilenNeu.size();i++){
            neueWrittenLines.put(i,false);
        }
        
        Map<Integer,General.Block> blocksMap = General.Block.getAllBlocks(
                this.textNeu,
                this.zeilenNeu
        );
        
        Map<Integer,General.Block> oldBlocksMap = General.Block.getAllBlocks(
                this.textAlt,
                this.zeilenAlt
        );
        
        SortedWrappers sws = Routines.getSortedWrappers(zeilenAlt);
        
        int j = 0;
        
        for(int i = 0;j < this.zeilenAlt.size() || i<this.zeilenNeu.size();i++){
            //System.out.println("Zeile " + i+":" + j + "--" + this.zeilenAlt.size() + ":" + this.zeilenNeu.size());
            
            for(int k = sws.smallestValue;k < i;k++){
                //System.out.println("Zeile before everything: " + k);
                List<LineWrapper> lwList = sws.wrappers.get(k);
                if(lwList != null){
                    for(LineWrapper lw:lwList){
                        if(lw == null)continue;
                               
                        if(!writtenLines.containsKey(lw.getZeile()) 
                                && lw.acceptInResultingText){
                            writtenLines.put(lw.getZeile(), true);
                            this.zeilenEntstanden.add(lw);
                            alteWrittenLines.put(lw.getZeile(),true);
                        }    
                    }    
                }
            }
            
            if(i < this.zeilenNeu.size()){
                //System.out.println("Zeile Neu: " + i);
                LineWrapper lw = this.zeilenNeu.get(i);
                
                General.Block b = blocksMap.get(i);
                
                if(b != null){
                    Boolean variableRaised = false;
                    //System.out.println("Block: " + b.zeileVon + ":" + b.zeileBis);
                    
                    for(int k = b.zeileVon;k <= b.zeileBis && k < this.zeilenNeu.size();k++){
                        //System.out.println("Zeile Block: " + k);
                        lw = this.zeilenNeu.get(k);
                        if(lw.getAcceptInResultingText()){
                            this.zeilenEntstanden.add(lw);
                            neueWrittenLines.put(lw.getZeile(),true);
                        }
                        
                        
                        LineWrapper lwOld = null;
                        
                        if(k < this.zeilenAlt.size()){
                            lwOld = this.zeilenAlt.get(k);
                        }
                        
                        
                        List<LineWrapper> wrappers = sws.wrappers.get(k);
                        
                        if(wrappers != null){
                            for(LineWrapper lw2:wrappers){
                                if(lw2 == null)continue;
                                Integer insert = Integer.valueOf(lw2.getInsertOnLine());

                                if(!writtenLines.containsKey(lw2.getZeile()) 
                                        && lw2.acceptInResultingText){
                                    writtenLines.put(lw2.getZeile(), true);
                                    this.zeilenEntstanden.add(lw2);
                                    alteWrittenLines.put(lw2.getZeile(),true);
                                }    
                            }
                        }else{
                            if(lwOld != null && lwOld.acceptInResultingText 
                                    && !writtenLines.containsKey(lwOld.getZeile())){
                                if(lwOld.ebene == lw.ebene && lwOld.getInsertOnLine() == null){
                                    this.zeilenEntstanden.add(lwOld);
                                    writtenLines.put(lwOld.getZeile(),true);
                                    alteWrittenLines.put(lwOld.getZeile(),true);
                                }
                            }    
                        }
                        
                        i++;
                        variableRaised = true;
                    }
                    if(variableRaised)i--;
                }else{
                    if(lw.getAcceptInResultingText()){
                        this.zeilenEntstanden.add(lw);
                        neueWrittenLines.put(lw.getZeile(),true);
                    }
                }
            }
            
            
            for(int k = i;k <= j;k++){
                //System.out.println("Zeile nachgefasst: " + k);
                List<LineWrapper> lwList = sws.wrappers.get(k);
                if(lwList != null){
                    for(LineWrapper lw:lwList){
                        if(lw == null)continue;
                        Integer insert = Integer.valueOf(lw.getInsertOnLine());
                        if(k < insert)continue;

                        if(!writtenLines.containsKey(lw.getZeile()) 
                                && lw.acceptInResultingText){
                            writtenLines.put(lw.getZeile(), true);
                            this.zeilenEntstanden.add(lw);
                            alteWrittenLines.put(lw.getZeile(),true);
                        }    
                    }    
                }
            }
            
            for(;j<this.zeilenAlt.size() && j <= i;j++){
                //System.out.println("Zeile after nachgefasst: " + j);
                LineWrapper lwAlt = sws.lineSortedMap.get(j);
                if(lwAlt != null){
                    Integer insert = Integer.valueOf(lwAlt.getInsertOnLine());
                    if(j < insert){
                        continue;
                    }
                }
                LineWrapper lw = this.zeilenAlt.get(j);
                General.Block b = oldBlocksMap.get(j);
                if(b != null){
                    for(int k = j;
                        k <= b.zeileBis;
                        k++
                    ){
                        this.putLinesViaBlock(
                                k,
                                this.zeilenAlt,
                                writtenLines,
                                this.zeilenEntstanden,
                                this.blocks
                        );

                        if(!writtenLines.containsKey(lw.getZeile())){
                            if(lw.acceptInResultingText){
                                Integer insert = Integer.valueOf(lw.getInsertOnLine());
                                if(insert != null && insert >= k || insert == null){
                                    this.zeilenEntstanden.add(lw);
                                    writtenLines.put(lw.getZeile(),true);
                                    alteWrittenLines.put(lw.getZeile(),true);
                                }
                            }
                        }
                        if(j >= this.zeilenAlt.size())break;
                    }
                }else{
                    if(!writtenLines.containsKey(lw.getZeile()) && lw.acceptInResultingText){
                        writtenLines.put(lw.getZeile(), true);
                        this.zeilenEntstanden.add(lw);
                        alteWrittenLines.put(lw.getZeile(),true);
                    }
                }
                
                if(lwAlt != null && !writtenLines.containsKey(lwAlt.getZeile()) && lwAlt.acceptInResultingText){
                    writtenLines.put(lwAlt.getZeile(), true);
                    this.zeilenEntstanden.add(lwAlt);
                    alteWrittenLines.put(lwAlt.getZeile(),true);
                }
            }
        }
        
        for(int i=0;i<alteWrittenLines.size();i++){
            Boolean val = alteWrittenLines.get(i);
            if(!val){
                //System.out.println("not written old line: " + i);
            }
        }
        
        for(int i=0;i<neueWrittenLines.size();i++){
            Boolean val = neueWrittenLines.get(i);
            if(!val){
                //System.out.println("not written old line: " + i);
            }
        }
    }
    
    public void transmitResult(
            JTextArea textArea
    ){
        String text = "";
        if(zeilenEntstanden.size()>0) text = zeilenEntstanden.get(0).value + "\n";
        
        for(Integer i = 1;i<zeilenEntstanden.size();i++){
            LineWrapper wrap = zeilenEntstanden.get(i);
            if(wrap.sourceValue != null)text += wrap.sourceValue;
            if(!wrap.sourceValue.contains("\n")) text += "\n";
        }
        
        textArea.setText(text);
    }
    
    public class LineRenderer extends DefaultTableCellRenderer{
         
        public LineRenderer(){
        }
        
        public void setValue(Object value) {
            if(value instanceof LineWrapperItem){
                LineWrapperItem lwi = (LineWrapperItem)value;
                
                setText(lwi.toString());
                String styleClass = lwi.lw.getStyleClass();
                
                if(lwi.renderFromStyleClass){
                    this.setBackground(Color.white);
                    if("added".equals(styleClass)){
                        this.setForeground(Color.BLUE);
                    }else if("removed".equals(styleClass)){
                        this.setForeground(Color.red);
                    }else if("moved".equals(styleClass)){
                        this.setForeground(Color.orange);
                    }else if("existing".equals(styleClass)){
                        this.setForeground(Color.magenta);
                    }if("changed".equals(styleClass)){
                        this.setForeground(Color.gray);
                    }
                }else{
                    this.setForeground(lwi.lw.foreGroundColor);
                    this.setBackground(lwi.lw.backGroundColor);
                }
            }else{
                this.setForeground(Color.black);
                this.setBackground(Color.white);
                setText(value + "");
            }
        }
    }
    
    public class LineWrapperItem implements Serializable{
        Boolean renderFromStyleClass;
        public LineWrapper lw;
        public String label;
        
        public LineWrapperItem(
                LineWrapper lw,
                String label
        ){
            this.label = label;
            this.lw = lw;
            this.renderFromStyleClass = true;
        }
        
        @Override
        public String toString(){
            return this.label;
        }
    }
    
    public static class Block implements Serializable{
            public Boolean isActive;
            public int zeileVon;
            public int zeileBis;
            
            public Block(
                    int zeileVon
            ){
                isActive = false;
                this.zeileVon = zeileVon;
            }
            
    }
    
    public class LineWrapper implements Serializable{
        public Map<String,LineWrapperItem> itemMap;
        
        private String value;
        private String sourceValue;
        private String styleClass;
        private String source;
        
        public Deployment dep;
        
        public Color foreGroundColor;
        public Color backGroundColor;
        
        public int ebene;
        
        public boolean isOld;
        
        private String insertOnLine;
        private Boolean acceptInResultingText;
        
        public Boolean getIsAcceptable(){
            return "removed".equals(this.styleClass) || "changed".equals(this.styleClass);
        }
        
        public Boolean getIsRemoved(){
            return "removed".equals(this.styleClass);
        }
        
        public LineWrapper stringNachfolger;
        public LineWrapper firstNamenWrapper;
        
        private LineWrapper aktuelleWahl;
        
        public Integer firstLine;
        private Integer zeile;
        private Integer zeileMove;
        
        public Integer stringCount;
        
        public LineWrapper(
        	Deployment dep
        ){
        	this(0,0,"","","",null,dep);    
        }
        
        public LineWrapper(
            Integer zeile,
            Integer firstLine,
            String sourceValue,
        	String value,
            String styleClass,
            LineWrapper vorgaenger,
            Deployment dep
        ){
            this(zeile,sourceValue,value,styleClass,vorgaenger,dep);
            this.firstLine = firstLine;
        }
        
        public LineWrapper(
            Integer zeile,
            String sourceValue,
            String value,
            String styleClass,
            LineWrapper vorgaenger,
            Deployment dep
        ){
            this.itemMap = new HashMap<String,LineWrapperItem>();
            this.setValue(value);
            this.setStyleClass(styleClass);
            this.setZeile(zeile);
            this.setSourceValue(sourceValue);
            this.setAcceptInResultingText(true);
            this.dep = dep;
            this.setInsertOnLine(zeile + "");
            this.aktuelleWahl = this;
            
            this.backGroundColor = Color.white;
            this.foreGroundColor = Color.black;
            
            if(vorgaenger!=null){
                vorgaenger.stringNachfolger = this;
                this.stringCount = vorgaenger.stringCount + 1;
                this.firstNamenWrapper = vorgaenger.firstNamenWrapper;
                this.firstLine = this.firstNamenWrapper.firstLine;
            }else{
                this.stringCount = 1;
                this.firstNamenWrapper = this;
                this.firstLine = this.zeile;
            }
        }
        
        public Boolean hasNachfolgerGoForward(){
            if(this.aktuelleWahl != null)this.aktuelleWahl = this.aktuelleWahl.stringNachfolger;
            else return false;
            
        	return true;    
        }
        
        public Boolean getAcceptInResultingText(){
            return this.acceptInResultingText;
        }
        
        public void setInsertOnLine(String insertOnLine){
            this.insertOnLine = insertOnLine;
            this.itemMap.put("insertOnLine", new LineWrapperItem(this, insertOnLine));
        }
        
        public String getInsertOnLine(){
            return this.insertOnLine;
        }
        
        public void setAcceptInResultingText(Boolean acceptInResultingText){
            this.acceptInResultingText = acceptInResultingText;
            this.itemMap.put("acceptInResultingText", new LineWrapperItem(this, acceptInResultingText+""));
        }
        
        public String getSource(){
            return this.source;
        }
        
        public void setSource(String source){
            this.source = source;
            this.itemMap.put("source", new LineWrapperItem(this, source));
        }
        
        public String getSourceValue(){
            return this.sourceValue;
        }
        
        public void setSourceValue(String sourceValue){
            this.sourceValue = sourceValue;
            this.itemMap.put("sourceValue", new LineWrapperItem(this, sourceValue));
        }
        
        public String getStyleClass(){
            return this.styleClass;
        }
        
        public void setStyleClass(String styleClass){
            this.styleClass = styleClass;
            this.itemMap.put("styleClass", new LineWrapperItem(this, styleClass));
        }
        
        public String getValue(){
            return this.value;
        }
        
        public void setValue(String value){
            this.value = value;
            this.itemMap.put("value", new LineWrapperItem(this, value));
        }
        
        public Integer getZeile(){
            return this.zeile;
        }
        
        public void setZeile(Integer zeile){
            this.zeile = zeile;
            this.itemMap.put("zeile", new LineWrapperItem(this, zeile+""));
        }
        
        public Integer getZeileMove(){
            return this.zeileMove;
        }
        
        public void setZeileMove(Integer zeileMove){
            this.zeileMove = zeileMove;
            this.itemMap.put("zeileMove", new LineWrapperItem(this, zeileMove+""));
        }
    }
}