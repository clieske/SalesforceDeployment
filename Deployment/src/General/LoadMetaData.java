/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package General;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author clieske
 */
public class LoadMetaData implements Runnable{
    
    private String commandFilePath;
    
    public LoadMetaData(
            String commandFilePath
    ){
        this.commandFilePath = commandFilePath;
    }
    
    @Override
    public void run() {
        try {
            Process process = new ProcessBuilder(commandFilePath).start();
        } catch (IOException ex) {
            Logger.getLogger(LoadMetaData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
