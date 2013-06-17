/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Scripts;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.Lexicon;
import org.openwims.Objects.Lexicon.Meaning;

/**
 *
 * @author jesse
 */
public class BatchLexiconModification {
    
    public static void main(String[] args) throws Exception {
        Lexicon.conn();
        
        HashMap expectations = new HashMap();
        Dependency dep = new Dependency("dobj", "SELF", "theme", expectations);
        
        LinkedList<Dependency> deps = new LinkedList();
        deps.add(dep);
        
        AddStructure(1, "DIRECT-OBJECT", true, new Lexicon().senses(), deps);
        
        
//        AddMeaning(new Lexicon().senses(), new Meaning("SELF", "theme", "theme"));
        
        Lexicon.conn().close();
    }
    
    
    public static void AddMeaning(LinkedList<String> senses, Meaning meaning) throws Exception {
        Connection conn = Lexicon.conn();
        Statement stmt = conn.createStatement();
        
        for (String sense : senses) {
            System.out.println(sense);
            String query = "INSERT INTO meanings (sense, target, relation, wim) VALUES ('" + sense + "', '" + meaning.target + "', '" + meaning.relation + "', '" + meaning.wim + "');";
            stmt.execute(query);
        }
        
        stmt.close();
    }
    
    public static void AddStructure(int series, String label, boolean optional, LinkedList<String> senses, LinkedList<Dependency> dependencies) throws Exception {
        Connection conn = Lexicon.conn();
        Statement stmt = conn.createStatement();
        
        for (String sense : senses) {
            System.out.println(sense);
            String query = "INSERT INTO structures (sense, series, label, optional) VALUES ('" + sense + "', " + series + ", '" + label + "', '" + optional + "');";
            
            //System.out.println(query);
            stmt.execute(query);
            
            for (Dependency dependency : dependencies) {
                AddSingleDependency(dependency);
            }
        }
        
        stmt.close();
    }
    
    private static void AddSingleDependency(Dependency dependency) throws Exception {
        Connection conn = Lexicon.conn();
        Statement stmt = conn.createStatement();
        
        String structID = "(SELECT max(id) FROM structures)";
        String query = "INSERT INTO dependencies (struct, dependency, governor, dependent) VALUES (" + structID + ", '" + dependency.type + "', '" + dependency.governor + "', '" + dependency.dependent + "');";
        
        //System.out.println(query);
        stmt.execute(query);
        
        for (String specification : dependency.expectations.keySet()) {
            String expectation = dependency.expectations.get(specification);
            String depID = "(SELECT max(id) FROM dependencies)";
            query = "INSERT INTO specifications (dependency, spec, expectation) VALUES (" + depID + ", '" + specification + "', '" + expectation + "');";
            
            //System.out.println(query);
            stmt.execute(query);
        }
        
        stmt.close();
    }
    
}
