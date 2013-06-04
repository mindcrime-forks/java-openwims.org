/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Ontology;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

/**
 *
 * @author jesse
 */
public class Ontology {
    
    private static Connection conn = null;
    private HashMap<String, String> isa;
    
    public static Connection conn() throws Exception {
        if (Ontology.conn == null) {
            Class.forName("org.sqlite.JDBC");            
            Ontology.conn = DriverManager.getConnection("jdbc:sqlite:wims.sql");
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {                    
                    try {
                        Ontology.conn.close();
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }
            }));
        }
        
        return Ontology.conn;
    }
    
    public Ontology() {
        this.isa = new HashMap();
        
        try {
            String query = "SELECT concept, parent FROM ontology;";
            Statement stmt = Ontology.conn().createStatement();
            
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                isa.put(rs.getString("concept"), rs.getString("parent"));
            }
            
            stmt.close();
            
        } catch (Exception err) {
            err.printStackTrace();
        }
        
    }
    
    public boolean isDescendant(String concept, String ancestor) {
        if (concept == null || ancestor == null) {
            return false;
        }
        
        if (concept.equalsIgnoreCase(ancestor)) {
            return true;
        }
        
        if (isDescendant(this.isa.get(concept), ancestor)) {
            return true;
        }
        
        return false;
    }
    
}
