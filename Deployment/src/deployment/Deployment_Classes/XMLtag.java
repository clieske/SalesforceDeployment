/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.Deployment_Classes;

/**
 *
 * @author Christoph Lieske
 */
public class XMLtag{
        public static int numberOfXMLtags;
        
        public boolean hasAlreadyInit;
        public int openCloseNumber;
        public int tabNumber;
        
        public boolean isFirstXMLtag;
        
        public int startPos;
        public int length;
        private int numberOfLastXMLlinks;
        
        public String resultingText;
        public String xmlName;
        
        public XMLtag nextReplaceXML;
        private XMLtag previousReplaceXML;
        public XMLtag firstReplaceXML;
        public XMLtag thisReplaceXML;
        
        public XMLtag nextGeneralXML;
        public XMLtag lastGeneralXML;
        public XMLtag previousGeneralXML;
        public XMLtag firstGeneralXML;
        
        public XMLtag(
                int startPos,
                int length,
                String xmlName
        ){
            if(numberOfXMLtags == 0){
                this.isFirstXMLtag = true;
                numberOfXMLtags = 1;
            }
            numberOfXMLtags++;
            
            this.startPos = startPos;
            this.length = length;
            this.numberOfLastXMLlinks = 0;
            this.xmlName = xmlName;
        }
        
        public void destroyReplaceXMLtag(){
            this.nextReplaceXML = null;
            this.previousReplaceXML = null;
            this.firstReplaceXML = null;
        }
        
        public Integer getNumberOfLastXMLlinks(){
            return this.numberOfLastXMLlinks;
        }
        
        public XMLtag getPreviousReplaceXML(){
            return this.previousReplaceXML;
        }
        
        public void setPreviousReplaceXML(XMLtag previousReplaceXML){
            this.previousReplaceXML = previousReplaceXML;
            if(previousReplaceXML != null){
                this.numberOfLastXMLlinks = previousReplaceXML.getNumberOfLastXMLlinks() + 1;
            }
        }
    }
