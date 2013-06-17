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
    private HashMap<String, Ancestry> ancestry;
    
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
        this.ancestry = new HashMap();
        
        try {
            String query = "SELECT concept, parent FROM ontology;";
            Statement stmt = Ontology.conn().createStatement();
            
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String concept = rs.getString("concept");
                String path = rs.getString("parent");
                
                Ancestry a = this.ancestry.get(concept);
                if (a == null) {
                    a = new Ancestry(concept);
                    this.ancestry.put(concept, a);
                }
                
                //trim brackets
                path = path.substring(1, path.length() - 1);
                
                //split on the concept - this will give x (usually 1) distinct path
                String[] paths = path.split(concept);
                for (String p : paths) {
                    LinkedList<String> ancestors = new LinkedList();
                    
                    //split on commas
                    String[] elements = p.split(", ");
                    
                    for (String e : elements) {
                        e = e.trim();
                        if (e.length() > 0) {
                            ancestors.add(e);
                        }
                    }
                    
                    ancestors.add(concept);
                    a.paths.add(ancestors);
                }
                
//                LinkedList<String> parents = isa.get(rs.getString("concept"));
//                if (parents == null) {
//                    parents = new LinkedList();
//                    isa.put(rs.getString("concept"), parents);
//                }
//                
//                parents.add(rs.getString("parent"));
            }
            
            stmt.close();
            
        } catch (Exception err) {
            err.printStackTrace();
        }
        
    }
    
    public LinkedList<String> concepts() {
        return new LinkedList(this.ancestry.keySet());
    }
    
    public Ancestry ancestors(String concept) {
        return this.ancestry.get(concept);
    }
    
    public boolean isDescendant(String concept, String ancestor) {
        if (concept == null || ancestor == null) {
            return false;
        }
        
        if (concept.equalsIgnoreCase(ancestor)) {
            return true;
        }
        
        Ancestry a = this.ancestry.get(concept);
        if (a == null) {
            return false;
        }
        
        for (LinkedList<String> path : a.paths) {
            if (path.contains(ancestor)) {
                return true;
            }
        }
        
//        for (String parent : this.isa.get(concept)) {
//            if (isDescendant(parent, ancestor)) {
//                return true;
//            }
//        }
        
        return false;
    }
    
    
    
    
    
    public class Ancestry {
        
        public String concept;
        public LinkedList<LinkedList<String>> paths;

        public Ancestry(String concept) {
            this.concept = concept;
            this.paths = new LinkedList();
        }
        
    }
    
}
