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
public class XMLParameter {
    String parameterName;
    String parameterValue;
    
    public XMLParameter(String parameterName,String parameterValue){
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;        
    }
    
    public String toString(){
        return parameterName + "=" + parameterValue;
    }
}
