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

/**
 *
 * @author jesse
 */
public class Lexicon {

    private static Connection conn = null;
    
    public static Connection conn() throws Exception {
        if (Lexicon.conn == null) {
            Class.forName("org.sqlite.JDBC");            
            Lexicon.conn = DriverManager.getConnection("jdbc:sqlite:wims.sql");
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
    
    public String definition(Sense sense) {
        String def = "";
        
        try {
            Statement stmt = Lexicon.conn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT definition FROM wordnet WHERE sense='" + sense.getId().replaceAll("'", "''") + "';");
            while (rs.next()) {
                def = rs.getString("definition");
            }
            
            stmt.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        
        if (def == null || def.equalsIgnoreCase("null")) {
            def = "";
        }
        
        return def;
    }

    public Token token(String representation) {
        
        Token token = new Token(representation);
        
        try {
            Statement stmt = Lexicon.conn().createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM senses WHERE token='" + representation.replaceAll("'", "''") + "';");
            while (rs.next()) {
                Sense sense = new Sense(rs.getString("id"));
                token.addSense(sense);
            }
            
            for (Sense sense : token.listSenses()) {
                Structure s = null;
                int curSeries = -1;

                HashMap<Integer, Dependency> deps = new HashMap();
                
                String query = "SELECT * FROM structures WHERE sense='" + sense.getId().replaceAll("'", "''") + "';";
                
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    int series = rs.getInt("series");
                    if (curSeries != series) {
                        curSeries = series;
                        s = sense.addStructure();
                    }
                    
                    int id = rs.getInt("id");
                    String dependency = rs.getString("dependency");
                    String governor = rs.getString("governor");
                    String dependent = rs.getString("dependent");
                    
                    Dependency dep = new Dependency(dependency, governor, dependent, new HashMap());
                    s.addDependency(dep);
                    deps.put(id, dep);
                }
                
                for (Integer id : deps.keySet()) {
                    Dependency dep = deps.get(id);
                    
                    query = "SELECT * FROM specifications WHERE struct=" + id + ";";
                    rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        String spec = rs.getString("spec");
                        String expectation = rs.getString("expectation");
                        dep.expectations.put(spec, expectation);
                    }
                }
                
                query = "SELECT * FROM meanings WHERE sense='" + sense.getId().replaceAll("'", "''") + "';";

                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    String target = rs.getString("target");
                    String relation = rs.getString("relation");
                    String wim = rs.getString("wim");
                    
                    sense.addMeaning(target, relation, wim);
                }
            }
            
            stmt.close();
            
        } catch (Exception err) {
            err.printStackTrace();
        }

        return token;

    }
}
