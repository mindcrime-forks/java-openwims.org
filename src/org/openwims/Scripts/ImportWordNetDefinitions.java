/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Scripts;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 *
 * @author jesse
 */
public class ImportWordNetDefinitions {
    
    public static void main(String[] args) throws Exception {

        FileInputStream fstream = new FileInputStream("/Users/jesse/Desktop/wndefinitions.dump");
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        
        File file = new File("/Users/jesse/Desktop/wimsdefssql.dump");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        
        String line;
        int i = 0;
        while ((line = br.readLine()) != null)   {
            
            String sql = parse(line); 
            
            i++;
            
            if (i % 500 == 0) {
                System.out.println("LINE: " + i);
            }
            
            if (sql.trim().equalsIgnoreCase("")) {
                continue;
            }
            
            bw.write(sql);
            
        }
        
        bw.close();
        in.close();
    }
    
    public static String parse(String line) throws Exception { 
        //haze.v.01:::become hazy, dull, or cloudy
        String out = "";
        
        String[] parts = line.split(":::");
        
        out = "UPDATE wordnet SET definition='" + parts[1].replaceAll("'", "''") + "' where wordnet_sense like '" + parts[0].replaceAll("'", "''") + "%';\n";
        
        return out;
    }
    
}
