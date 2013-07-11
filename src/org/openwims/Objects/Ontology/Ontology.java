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
import java.util.LinkedList;

/**
 *
 * @author jesse
 */
public class Ontology {
    
    private static Connection conn = null;
    private HashMap<String, String> isa;
    private HashMap<String, LinkedList<String>> subclasses;
    private HashMap<String, String> definitions;
    
    public static Connection conn() throws Exception {
        if (Ontology.conn == null) {
            String url = "jdbc:postgresql://localhost/OpenWIMs";
            String user = "jesse";
            String pass = "";

            Class.forName("org.postgresql.Driver");
            
            Ontology.conn = DriverManager.getConnection(url, user, pass);
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
        this.subclasses = new HashMap();
        this.definitions = new HashMap();
        
        try {
            String query = "SELECT concept, parent, definition FROM ontology;";
            Statement stmt = Ontology.conn().createStatement();
            
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String concept = rs.getString("concept");
                String parent = rs.getString("parent");
                String definition = rs.getString("definition");
                
                this.isa.put(concept, parent);
                this.definitions.put(concept, definition);
                
                LinkedList<String> children = this.subclasses.get(parent);
                if (children == null) {
                    children = new LinkedList();
                    this.subclasses.put(parent, children);
                }
                
                children.add(concept);
            }
            
            stmt.close();
            
        } catch (Exception err) {
            err.printStackTrace();
        }
        
    }
    
    public String definition(String concept) {
        return this.definitions.get(concept);
    }
    
    public LinkedList<String> concepts() {
        return new LinkedList(this.isa.keySet());
    }
    
    public LinkedList<String> children(String concept) {
        return this.subclasses.get(concept);
    }
    
    public boolean isDescendant(String concept, String ancestor) {
        if (concept == null || ancestor == null) {
            return false;
        }
        
        if (concept.equalsIgnoreCase(ancestor)) {
            return true;
        }

        if (this.isa.get(concept) != null) {
            if (isDescendant(this.isa.get(concept), ancestor)) {
                return true;
            }
        }
        
        return false;
    }
    
}
