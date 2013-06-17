/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

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
public class Lexicon {

    private static Connection conn = null;
    private HashMap<String, Word> words;
    
    public static void main(String[] args) throws Exception {
        System.out.println(new Lexicon().word("hit"));
    }

    public Lexicon() {
        this.words = new HashMap();
    }
    
    public static Connection conn() throws Exception {
        if (Lexicon.conn == null) {
//            Class.forName("org.sqlite.JDBC");            
//            Lexicon.conn = DriverManager.getConnection("jdbc:sqlite:wims.sql");
            
            String url = "jdbc:postgresql://localhost/OpenWIMs";
            String user = "jesse";
            String pass = "";

            Class.forName("org.postgresql.Driver");
            
            Lexicon.conn = DriverManager.getConnection(url, user, pass);
            
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {                    
                    try {
                        Lexicon.conn.close();
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }
            }));
        }
        
        return Lexicon.conn;
    }
    
    public LinkedList<String> roots() throws Exception {
        LinkedList<String> roots = new LinkedList();
        
        Statement stmt = Lexicon.conn().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT representation FROM words ORDER BY representation ASC;");
        while (rs.next()) {
            roots.add(rs.getString("representation"));
        }
        
        stmt.close();
        
        return roots;
    }
    
    public LinkedList<String> senses() throws Exception {
        LinkedList<String> senses = new LinkedList();
        
        Statement stmt = Lexicon.conn().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM senses ORDER BY id ASC;");
        while (rs.next()) {
            senses.add(rs.getString("id"));
        }
        
        stmt.close();
        
        return senses;
    }
    
    public LinkedList<String> verbs() throws Exception {
        LinkedList<String> verbs = new LinkedList();
        
        Statement stmt = Lexicon.conn().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM senses WHERE id LIKE '%-v-%' ORDER BY id ASC;");
        while (rs.next()) {
            verbs.add(rs.getString("id"));
        }
        
        stmt.close();
        
        return verbs;
    }

    public Word word(String representation) {
        
        if (this.words.get(representation) != null) {
            return this.words.get(representation);
        }
        
        Word word = new Word(representation);
        
        try {
            Statement stmt = Lexicon.conn().createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM senses WHERE word='" + representation.replaceAll("'", "''") + "';");
            while (rs.next()) {
                Sense sense = new Sense(rs.getString("id"));
                sense.setDefinition(rs.getString("definition"));
                word.addSense(sense);
            }
            
            for (Sense sense : word.listSenses()) {
                
                HashMap<Integer, DependencySet> dependencySets = new HashMap();
                HashMap<Integer, Structure> structures = new HashMap();
                HashMap<Integer, Dependency> dependencies = new HashMap();
                
                String query = "SELECT * FROM structures WHERE sense='" + sense.getId().replaceAll("'", "''") + "';";
                
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int series = rs.getInt("series");
                    String set = rs.getString("label");
                    boolean optional = Boolean.parseBoolean(rs.getString("optional"));
                                        
                    Structure structure = structures.get(series);
                    if (structure == null) {
                        structure = sense.addStructure();
                        structures.put(series, structure);
                    }
                    
                    DependencySet dependencySet = structure.addDependencySet();
                    dependencySet.label = set;
                    dependencySet.optional = optional;
                    
                    dependencySets.put(id, dependencySet);
                }
                
                for (Integer id : dependencySets.keySet()) {
                    DependencySet set = dependencySets.get(id);
                    
                    query = "SELECT * FROM dependencies WHERE struct=" + id + ";";
                    
                    rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        int depid = rs.getInt("id");
                        String dependency = rs.getString("dependency");
                        String governor = rs.getString("governor");
                        String dependent = rs.getString("dependent");
                        
                        Dependency dep = new Dependency(dependency, governor, dependent, new HashMap());
                        set.dependencies.add(dep);
                        dependencies.put(depid, dep);
                    }
                }
                
                for (Integer id : dependencies.keySet()) {
                    Dependency dep = dependencies.get(id);
                    
                    query = "SELECT * FROM specifications WHERE dependency=" + id + ";";
                    
                    rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        String specification = rs.getString("spec");
                        String expectation = rs.getString("expectation");
                        
                        dep.expectations.put(specification, expectation);
                    }
                }
                
                query = "SELECT * FROM meanings WHERE sense='" + sense.getId().replaceAll("'", "''") + "';";

                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    String target = rs.getString("target");
                    String relation = rs.getString("relation");
                    String wim = rs.getString("wim");
                    String struct = rs.getString("structure");
                    
                    try {
                        int structID = Integer.parseInt(struct);
                        dependencySets.get(structID).meanings.add(new Meaning(target, relation, wim));
                    } catch (Exception err) {
                        sense.addMeaning(target, relation, wim);
                    }
                    
                }
                
            }
            
            stmt.close();
            
        } catch (Exception err) {
            err.printStackTrace();
        }

        this.words.put(representation, word);
        
        return word;

    }
}
