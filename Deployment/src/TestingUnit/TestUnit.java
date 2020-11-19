/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestingUnit;
import General.Routines;
import static General.Routines.getDashedText;
import deployment.Deployment_Classes.XMLtag;
import deployment.Start_Frame;
import static deployment.Start_Frame.getfromXMLnameToPreviousTagMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Test;
import static junit.framework.Assert.*;

/**
 *
 * @author Christoph Lieske
 */ 
public class TestUnit {
    static Map<String,XMLtag> fromXMLnameToPreviousTagMap;
    static XMLtag linkTag;
    static Matcher regexMatcher;
    static Pattern regexPattern;
    static String sourceCode;
    static Map<String,Integer> fromXmlNameToOpenCloseMap;
    static XMLtag firstGeneralTag;
    
    static String regexXMLelement;
    
    public static void main(String argv[]){
        /*test_Start_Frame_getfromXMLnameToPreviousTagMap();
        test_GetXMLnames();
        test_CleanedXMLnames();
        
        firstGeneralTag = null;
        test_getfromXMLnameToPreviousTagMap();*/
        
        ///*TODO: */test_linkXMLtagsToText();
        //test_lastTag();
        //test_divTag();
        //test_real();
        testBasic();
    }
    
    static final String finalDashedText = "<!---->hallo welt blakes   <html lang=\"de\"><link/><input/><input/><link><link/></link></html>";
    
    private static void testBasic(){
        Start_Frame.switchOffDebug = true;
        Start_Frame.switchOffDashDebug = false;
        
        sourceCode = "fhsdjkfhkjadshjka<!----hallo welt blakes   --><html lang=\"de\">hallo welt<link><input><input><input/><input/><input/><input/></input><link><link/></link></html>";
        fromXMLnameToPreviousTagMap       
                = getfromXMLnameToPreviousTagMap(sourceCode);
        XMLtag lastTag = fromXMLnameToPreviousTagMap.get("input");
        XMLtag nextTag = lastTag.firstReplaceXML;
        
        assertNotNull(lastTag);
        assertNotNull(nextTag);
        nextTag = nextTag.nextReplaceXML;
        
        String dashedText = getDashedText(fromXMLnameToPreviousTagMap,sourceCode);
        //assertEquals(finalDashedText,dashedText);
        System.out.println("###sourceCode:::" + sourceCode);
        System.out.println("###dashedText:::\n" + dashedText);
        XMLtag linkTag = fromXMLnameToPreviousTagMap.get("link");
        nextTag = linkTag.firstReplaceXML;
        assertNotNull(nextTag);
        nextTag = nextTag.nextReplaceXML;
        //assertNull(nextTag);
        
    }
    
    private static void test_real(){
        sourceCode = "<html lang=\"de\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><link><input><input><link></html>";
        fromXMLnameToPreviousTagMap 
                = getfromXMLnameToPreviousTagMap(sourceCode);
        
        XMLtag xmlTag = fromXMLnameToPreviousTagMap.get("html");
        assertNull(xmlTag);
        
        xmlTag = fromXMLnameToPreviousTagMap.get("link");
        assertNotNull(xmlTag);
        
        xmlTag = xmlTag.getPreviousReplaceXML();
        assertNotNull(xmlTag);
        
        xmlTag = xmlTag.getPreviousReplaceXML();
        assertNull(xmlTag);
        
        sourceCode = "<html><link><input><link/><link></link><input><link></link></link></html>";
        fromXMLnameToPreviousTagMap 
                = getfromXMLnameToPreviousTagMap(sourceCode);
        
        xmlTag = fromXMLnameToPreviousTagMap.get("link");
        assertNull(xmlTag);
        
        
        /*String targetCode = Start_Frame.linkXMLtagsToText(fromXMLnameToPreviousTagMap, sourceCode);
        System.out.println("###sourceCode::" + sourceCode);
        System.out.println("targetCode::" + targetCode);*/
    }
    
    private static void test_divTag(){
        sourceCode = "/**/<page prop1=\"val1\"><link prop2=\"val2\"/><link><div><div><link><div></div><ab><ab></ab></ab>";
        fromXMLnameToPreviousTagMap 
                = getfromXMLnameToPreviousTagMap(sourceCode);
        XMLtag divTag = fromXMLnameToPreviousTagMap.get("div");
        assertNotNull(divTag);
        XMLtag nextTag = divTag.firstReplaceXML;
        assertEquals(49,nextTag.startPos);
        nextTag = nextTag.nextReplaceXML;
        assertEquals(54,nextTag.startPos);
        nextTag = nextTag.nextReplaceXML;
        assertEquals(65,nextTag.startPos);
        nextTag = nextTag.nextReplaceXML;
        assertEquals(70,nextTag.startPos);
        nextTag = nextTag.nextReplaceXML;
        assertNull(nextTag);
    }
    
    private static void test_lastTag(){
        sourceCode = "/**/<link prop1=\"val1\"><link prop2=\"val2\"/><link><div><div><link><div></div><ab><ab></ab></ab>";
        fromXMLnameToPreviousTagMap 
                = getfromXMLnameToPreviousTagMap(sourceCode);
        XMLtag lastTag = null;
        XMLtag doTag = firstGeneralTag;
        String cleanedName = Start_Frame.getCleanedName(doTag.xmlName);
        lastTag = fromXMLnameToPreviousTagMap.get(cleanedName);
        
        System.out.println("##firstTag::" + cleanedName);
        assertEquals("link",cleanedName);
        XMLtag nextXMLtag = lastTag.firstReplaceXML;
        assertEquals(4,nextXMLtag.startPos);
        assertEquals(0,nextXMLtag.tabNumber);
        
        
        nextXMLtag = nextXMLtag.nextReplaceXML;
        assertEquals(1,nextXMLtag.tabNumber);
        
        assertEquals(23,nextXMLtag.startPos);
        
        nextXMLtag = nextXMLtag.nextReplaceXML;
        assertEquals(1,nextXMLtag.tabNumber);
        assertEquals(43,nextXMLtag.startPos);
        
        nextXMLtag = nextXMLtag.nextReplaceXML;
        assertEquals(59,nextXMLtag.startPos);
        assertEquals(4,nextXMLtag.tabNumber);
        
        nextXMLtag = nextXMLtag.nextReplaceXML;
        assertEquals(null,nextXMLtag);
        
        
    }
    
    //TODO:
    private static void test_linkXMLtagsToText(){
        firstGeneralTag = null;
        
        sourceCode = "/**/<page prop1=\"val1\"><link prop2=\"val2\"/><link><div><div><link><div></div><ab><ab></ab></ab>";
        fromXMLnameToPreviousTagMap 
                = getfromXMLnameToPreviousTagMap(sourceCode);
        
        assertNotNull(firstGeneralTag);
        assertNotNull(firstGeneralTag.nextGeneralXML);
        
        String targetCode = Start_Frame.linkXMLtagsToText_deprecated(
                fromXMLnameToPreviousTagMap
                ,sourceCode
        );
        
        System.out.println("###sourceCode::" + sourceCode);
        System.out.println("###targetCode::" + targetCode);
        
        //assertEquals("<link prop1=\"val1\"/><link prop2=\"val2\"/><link/><div/><div/><link/><div></div>",targetCode);
    }
    
    private static Map<String,XMLtag> test_getfromXMLnameToPreviousTagMap(){
        String sourceCode = "<link prop=\"val1\"><link prop2=\"val2\"></link>";
        String regexXMLelement = Routines.regexXMLelement;
        
        Map<String,XMLtag> fromXMLnameToPreviousTagMap 
                = new HashMap<String,XMLtag>();
        Pattern regexPattern = Pattern.compile(regexXMLelement);
        Matcher regexMatcher = regexPattern.matcher(sourceCode);
        
        Map<String,Integer> fromXmlNameToOpenCloseMap 
                = new HashMap<String,Integer>();
        Integer iterations = 0;
        
        while(regexMatcher.find()){
            String xmlName = regexMatcher.group(2);
            String xmlRegex =  "</" + xmlName;
            String xmlBeforeRegex = "<" + xmlName;
            
            Integer regionStart = regexMatcher.start();
            Integer xmlRegexIndex = sourceCode.indexOf(xmlRegex,regionStart + 1);
            Integer xmlBeforeRegexIndex = 
                    sourceCode.indexOf(xmlBeforeRegex, 
                    regionStart + 1);
            
            String xmlTag = regexMatcher.group(1);
            String dashesText = regexMatcher.group(4);
            String cleanedName = xmlName;
            if(xmlName.startsWith("/")){
                cleanedName = xmlName.substring(1);
            }
            
            assertEquals("link",cleanedName);
            
            Integer openCloseNumber = fromXmlNameToOpenCloseMap.get(cleanedName);
            if(openCloseNumber == null)openCloseNumber = 0;
            long verifyNumber = openCloseNumber;
            
            if(iterations == 0){
                assertEquals(0,verifyNumber);
            }else if(iterations == 1){
                assertEquals(1,verifyNumber);
            }else if(iterations == 2){
                assertEquals(2,verifyNumber);
            }
            
            XMLtag thisXMLtag = new XMLtag(
                    regionStart
                    ,xmlTag.length() + dashesText.length() + 1
                    ,xmlName
            );
            
            if(iterations == 0){
                assertNull(firstGeneralTag);
            }else if(iterations == 1){
                assertNotNull(firstGeneralTag);
            }else if(iterations == 2){
                assertNotNull(firstGeneralTag);
            }
            
            if(firstGeneralTag == null){
                firstGeneralTag = thisXMLtag;
            }
            thisXMLtag.nextGeneralXML = null;
            
            XMLtag previousXMLtag = fromXMLnameToPreviousTagMap.get(cleanedName);
            if(previousXMLtag != null){
                previousXMLtag.nextReplaceXML = thisXMLtag;
                thisXMLtag.firstReplaceXML = previousXMLtag.firstReplaceXML;
                thisXMLtag.setPreviousReplaceXML(previousXMLtag);
            }else{
                thisXMLtag.firstReplaceXML = thisXMLtag;
            }
                
            if(xmlName.startsWith("/")){
                openCloseNumber--;
                thisXMLtag.openCloseNumber--;
                if(previousXMLtag != null){
                    previousXMLtag = previousXMLtag.getPreviousReplaceXML();
                    
                    if(previousXMLtag == null){
                        fromXMLnameToPreviousTagMap.remove(cleanedName);
                    }else{
                        fromXMLnameToPreviousTagMap.put(
                            cleanedName
                            ,previousXMLtag
                        );
                        previousXMLtag.destroyReplaceXMLtag();
                        previousXMLtag = null;
                    }
                }
                
                if(openCloseNumber == 0){
                    fromXMLnameToPreviousTagMap.remove(cleanedName);
                }
            }else{
                openCloseNumber++;
                thisXMLtag.openCloseNumber++;
                if(openCloseNumber > 1){
                    thisXMLtag.setPreviousReplaceXML(previousXMLtag);
                    if(previousXMLtag != null){
                        thisXMLtag.firstReplaceXML = previousXMLtag.firstReplaceXML;
                    }else{
                        thisXMLtag.firstReplaceXML = thisXMLtag;
                    }
               }
               fromXMLnameToPreviousTagMap.put(cleanedName,thisXMLtag);
            }
            
            fromXmlNameToOpenCloseMap.put(cleanedName,openCloseNumber);
            iterations++;
        }
        
        return fromXMLnameToPreviousTagMap;
    }
    
    private static void resetVariables(){
        fromXMLnameToPreviousTagMap = new HashMap<String,XMLtag>();
        linkTag = null;
        regexPattern = Pattern.compile(regexXMLelement);
        regexMatcher = regexPattern.matcher(sourceCode);
        fromXmlNameToOpenCloseMap 
                = new HashMap<String,Integer>();
    }
    
    public static void test_GetXMLnames(){
        sourceCode = "<link isfkisdhfkjdsjkfsdjk=\"sdfsdfsd\"><link skjbfkjsbfjsdb=\"akjdklajklas\">";
        regexXMLelement = "(<([^\\^\"^>^\\s]+)[\\s]{0,1}([^\"^/^>]*\"[^\"]*\")*)([/]{0,1})>";
        resetVariables();
        if(regexMatcher.find()){
            String xmlName = regexMatcher.group(2);
            System.out.println("###xmlName::" + xmlName);
            assertEquals("link",xmlName);
        }else{
            assertEquals(false,true);
        }
    }
    
    public static void test_CleanedXMLnames(){
        sourceCode = "<link prop1=\"val1\"><link prop2=\"val2\"/></link>";
        regexXMLelement = "(<([/]{0,1}[^\\^\"^>^\\s]+)[\\s]{0,1}([^\"^/^>]*\"[^\"]*\")*)([/]{0,1})>";
        resetVariables();
        
        Integer iterations = 0;
        
        if(iterations == 0){
                
        }else if(iterations == 1){

        }else if(iterations == 2){

        }
        
        while(regexMatcher.find()){
            String xmlName = regexMatcher.group(2);
            String xmlRegex =  "</" + xmlName;
            String xmlBeforeRegex = "<" + xmlName;
            System.out.println("iterations::" 
                    + iterations
                    + "\nxmlName:::" + xmlName + "--"
            );
            
            if(iterations == 2){
                assertEquals("/link",xmlName);
            }else{
                assertEquals(4,xmlName.length());
                assertEquals("link",xmlName);
            }
            
            Integer regionStart = regexMatcher.start();
            long verifyNumber = regionStart;
            if(iterations == 0){
                assertEquals(0,verifyNumber);
                System.out.println("0--sourceCode::" 
                        + sourceCode.substring(regionStart)
                );
            }else if(iterations == 1){
                System.out.println("1--regionStart::" + regionStart);
                System.out.println("1--sourceCode::" 
                        + sourceCode.substring(regionStart)
                );
                assertEquals(19,verifyNumber);
            }else if(iterations == 2){
                System.out.println("2--regionStart::" + regionStart);
                System.out.println("2--sourceCode::" 
                        + sourceCode.substring(regionStart)
                );
                assertEquals(39,verifyNumber);
            }
            
            Integer xmlRegexIndex = sourceCode.indexOf(xmlRegex,regionStart + 1);
            Integer xmlBeforeRegexIndex = 
                    sourceCode.indexOf(xmlBeforeRegex, 
                    regionStart + 1);
            
            if(iterations == 0){
                assertEquals(true,xmlRegexIndex > xmlBeforeRegexIndex);
                assertEquals(true,xmlBeforeRegexIndex > 5);
            }else if(iterations == 0){
                assertEquals(true,xmlRegexIndex < xmlBeforeRegexIndex);
            }
            
            String xmlTag = regexMatcher.group(1);
            
            if(iterations == 0){
                System.out.println("xmlTag::" + xmlTag);
                assertEquals("<link prop1=\"val1\"",xmlTag);
            }else if(iterations == 1){
                assertEquals("<link prop2=\"val2\"",xmlTag);
            }else if(iterations == 2){
                assertEquals("</link",xmlTag);
            }
            
            String dashesText = regexMatcher.group(4);
            if(iterations == 0){
                assertEquals("",dashesText);
            }else if(iterations == 1){
                assertEquals("/",dashesText);
            }else if(iterations == 2){
                assertEquals("",dashesText);
            }
            
            String cleanedName = xmlName;
            if(xmlName.startsWith("/")){
                cleanedName = xmlName.substring(1);
            }
            assertEquals("link",cleanedName);
            
            Integer openCloseNumber = fromXmlNameToOpenCloseMap.get(cleanedName);
            if(openCloseNumber == null)openCloseNumber = 0;
            verifyNumber = openCloseNumber;
            
            if(iterations == 0){
                assertEquals(0,verifyNumber);
            }else if(iterations == 1){
                assertEquals(0,verifyNumber);
            }else if(iterations == 2){
                assertEquals(0,verifyNumber);
            }
            
            XMLtag thisXMLtag = new XMLtag(
                    regionStart
                    ,xmlTag.length() + dashesText.length() + 1
                    ,xmlName
            );
            
            if(iterations == 0){
                assertEquals(null,firstGeneralTag);
            }else{
                assertNotSame(null,firstGeneralTag);
            }
            
            if(firstGeneralTag == null){
                firstGeneralTag = thisXMLtag;
            }
            thisXMLtag.nextGeneralXML = null;
            
            XMLtag previousXMLtag = fromXMLnameToPreviousTagMap.get(cleanedName);
            
            
            if(previousXMLtag != null){
                previousXMLtag.nextReplaceXML = thisXMLtag;
                thisXMLtag.firstReplaceXML = previousXMLtag.firstReplaceXML;
                thisXMLtag.setPreviousReplaceXML(previousXMLtag);
            }else{
                thisXMLtag.firstReplaceXML = thisXMLtag;
            }
            iterations ++;
        }
    }
    
    public static void test_Start_Frame_getfromXMLnameToPreviousTagMap(){
        regexXMLelement = Routines.regexXMLelement;
        sourceCode = "<link>";
        fromXMLnameToPreviousTagMap 
                = getfromXMLnameToPreviousTagMap(sourceCode,regexXMLelement);
        
        linkTag = fromXMLnameToPreviousTagMap.get("link");
        assertNotNull(linkTag);
        
        assertNull(linkTag.getPreviousReplaceXML());
        
        sourceCode = "<link isfkisdhfkjdsjkfsdjk><link skjbfkjsbfjsdb>";
        regexXMLelement = "<([^\\\"^>^\\\\s]+)";
        
        resetVariables();
        
        if(regexMatcher.find()){
            
        }else{
            assertEquals(false,true);
        }
        if(true)return;
        
        if(regexMatcher.find()){
            
        }else{
            assertEquals(false,true);
        } 
        
        if(regexMatcher.find()){
            assertEquals(false,true);
        }else{
            
        }
        
        resetVariables();
        
        if(regexMatcher.find()){
            String xmlName = regexMatcher.group(2);
            String xmlRegex =  "</" + xmlName;
            String xmlBeforeRegex = "<" + xmlName;

            Integer regionStart = regexMatcher.start();
            Integer xmlRegexIndex = sourceCode.indexOf(xmlRegex,regionStart + 1);
            Integer xmlBeforeRegexIndex = 
                    sourceCode.indexOf(xmlBeforeRegex, 
                    regionStart + 1);

            String xmlTag = regexMatcher.group(1);
            String dashesText = regexMatcher.group(4);
            String cleanedName = xmlName;
            if(xmlName.startsWith("/")){
                cleanedName = xmlName.substring(1);
                assertEquals(false,true);
            }
            
            Integer openCloseNumber = fromXmlNameToOpenCloseMap.get(cleanedName);
            if(openCloseNumber == null)openCloseNumber = 0;
            else assertEquals(false,true);
        }else{
            assertEquals(false,true);
        }
        
        if(regexMatcher.find()){
            String xmlName = regexMatcher.group(2);
            String xmlRegex =  "</" + xmlName;
            String xmlBeforeRegex = "<" + xmlName;

            Integer regionStart = regexMatcher.start();
            Integer xmlRegexIndex = sourceCode.indexOf(xmlRegex,regionStart + 1);
            Integer xmlBeforeRegexIndex = 
                    sourceCode.indexOf(xmlBeforeRegex, 
                    regionStart + 1);

            String xmlTag = regexMatcher.group(1);
            String dashesText = regexMatcher.group(4);
            String cleanedName = xmlName;
            if(xmlName.startsWith("/")){
                assertEquals(false,true);
            }
            
            Integer openCloseNumber = fromXmlNameToOpenCloseMap.get(cleanedName);
            if(openCloseNumber == null)assertEquals(false,true);
        }else{
            assertEquals(false,true);        
        }
        
        resetVariables();
        assertEquals(false,true);
        testFind("link",1,-1,true);
        
        sourceCode = "<link prop1=\"val1\"><link prop2=\"val2\"/></link>";
        fromXMLnameToPreviousTagMap 
                = getfromXMLnameToPreviousTagMap(sourceCode);
        linkTag = fromXMLnameToPreviousTagMap.get("link");
        assertNotNull(linkTag);
        
        assertNull(linkTag.getPreviousReplaceXML());
        
        sourceCode = "<link prop1=\"val1\"><link prop2=\"val2\"/><link><div><div><link><div>";
        fromXMLnameToPreviousTagMap 
                = getfromXMLnameToPreviousTagMap(sourceCode);
        linkTag = fromXMLnameToPreviousTagMap.get("link");
        assertNotNull(linkTag);
        linkTag = linkTag.getPreviousReplaceXML();
        assertNotNull(linkTag);
        linkTag = linkTag.getPreviousReplaceXML();
        assertNotNull(linkTag);
        linkTag = linkTag.getPreviousReplaceXML();
        assertNotNull(linkTag);
        linkTag = linkTag.getPreviousReplaceXML();
        assertNull(linkTag);
        
        
        
        XMLtag divTag = fromXMLnameToPreviousTagMap.get("div");
        assertNotNull(divTag);
        divTag = divTag.getPreviousReplaceXML();
        assertNotNull(divTag);
        divTag = divTag.getPreviousReplaceXML();
        assertNotNull(divTag);
        divTag = divTag.getPreviousReplaceXML();
        assertNull(divTag);
        
        sourceCode = "<link prop1=\"val1\"><link prop2=\"val2\"/><link><div><div><link><div></div>";
        fromXMLnameToPreviousTagMap 
                = getfromXMLnameToPreviousTagMap(sourceCode);
        divTag = fromXMLnameToPreviousTagMap.get("div");
        assertNotNull(divTag);
        divTag = divTag.getPreviousReplaceXML();
        assertNull(divTag);
    }
    
    private static void testFind(
            String testXmlName
            ,long testRegionStart
            ,long testCloseXMLtagRegionStart
            ,boolean testNextXMLtagExisting
    ){
        if(regexMatcher.find()){
            String xmlName = regexMatcher.group(2);
            assertEquals(testXmlName,xmlName);//
            
            String xmlRegex =  "</" + xmlName;
            String xmlBeforeRegex = "<" + xmlName;
            
            Integer regionStart = regexMatcher.start();
            long thisRegionStartLong = regionStart;
            
            assertEquals(testRegionStart,thisRegionStartLong);//
            
            Integer xmlRegexIndex = sourceCode.indexOf(xmlRegex,regionStart + 1);
            thisRegionStartLong = xmlRegexIndex;
            assertEquals(testCloseXMLtagRegionStart,thisRegionStartLong);//
            
            Integer xmlBeforeRegexIndex = 
                    sourceCode.indexOf(xmlBeforeRegex, 
                    regionStart + 1);
            assertEquals(testNextXMLtagExisting,xmlBeforeRegexIndex > xmlRegexIndex);
            
            String xmlTag = regexMatcher.group(1);
            assertEquals("<link isfkisdhfkjdsjkfsdjk>",xmlTag);
            
            String dashesText = regexMatcher.group(4);
            assertEquals("",dashesText);
            
            String cleanedName = xmlName;
            if(xmlName.startsWith("/")){
                cleanedName = xmlName.substring(1);
            }
            assertEquals("link",xmlTag);
            
            Integer openCloseNumber = fromXmlNameToOpenCloseMap.get(cleanedName);
            assertEquals(null,openCloseNumber);
            
            if(openCloseNumber == null)openCloseNumber = 0;
            
            XMLtag thisXMLtag = new XMLtag(
                    regionStart
                    ,xmlTag.length() + dashesText.length() + 1
                    ,xmlName
            );
            assertEquals(null,firstGeneralTag);
            
            if(firstGeneralTag == null){
                firstGeneralTag = thisXMLtag;
            }
            
            
        }
    }
}
