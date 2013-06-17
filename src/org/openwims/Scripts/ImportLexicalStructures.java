/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Meaning;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesse
 */
public class ImportLexicalStructures {
    
    public static void main(String[] args) throws Exception {
        
        InputStream in = ImportLexicalStructures.class.getResourceAsStream("/org/openwims/Assets/prepositions");
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        String line = null;
        
        LinkedList<DependencySet> sets = new LinkedList();
        LinkedList<Meaning> meanings = new LinkedList();

        try {
            
            DependencySet set = null;
            
            while ((line = input.readLine()) != null) {
                if (set == null) {
                    set = new DependencySet(new LinkedList(), new LinkedList(), true, "");
                    sets.add(set);
                }
                
                if (line.trim().equalsIgnoreCase("")) {
                    set = new DependencySet(new LinkedList(), new LinkedList(), true, "");
                    sets.add(set);
                    continue;
                }
                
                if (line.charAt(0) == '+') {
                    set.label = line.trim();
                } else if (line.charAt(0) == '[') {
                    line = line.substring(1, line.length() - 1);
                    String[] parts = line.split("\\.");
                    String domain = parts[0];
                    parts = parts[1].split("=");
                    String property = parts[0];
                    String range = parts[1];
                    
                    set.meanings.add(new Meaning(domain, property, range));
                } else {
                    String[] parts = line.split("\\(");
                    String type = parts[0];
                    line = parts[1].replaceAll("\\)", "");
                    
                    String gov = line.substring(0, line.indexOf(", "));
                    line = line.replaceFirst(gov + ", ", "");
                    
                    parts = line.split("\\{");
                    String dep = parts[0];
                    line = parts[1].replaceAll("\\}", "");
                    
                    Dependency dependency = new Dependency(type, gov, dep, new HashMap());
                    
                    parts = line.split(", ");
                    for (String spec : parts) {
                        String[] innerparts = spec.split("=");
                        String specification = innerparts[0];
                        String expectation = innerparts[1].replaceAll("'", "");
                        
                        dependency.expectations.put(specification, expectation);
                    }
                    
                    set.dependencies.add(dependency);
                }
            }
            
        } catch (Exception err) {
            err.printStackTrace();
        }
        
        
        File file = new File("/Users/jesse/Desktop/prepositions.dump.sql");
 
        // if file doesnt exists, then create it
        if (!file.exists()) {
                file.createNewFile();
        }
 
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        
        for (String verb : WIMGlobals.lexicon().verbs()) {
                
            System.out.println(verb);
            
            for (DependencySet set : sets) {
                String query = "INSERT INTO structures (sense, series, label, optional) VALUES ('" + verb + "', 1, '" + set.label + "', '" + set.optional + "');\n";
                bw.write(query);
                
                for (Dependency dependency : set.dependencies) {
                    AddSingleDependency(dependency, bw);
                }
                
                for (Meaning meaning : set.meanings) {
                    query = "INSERT INTO meanings (sense, target, relation, wim, structure) VALUES ('" + verb + "', '" + meaning.target + "', '" + meaning.relation + "', '" + meaning.wim + "', (SELECT max(id) FROM structures));\n";
                    bw.write(query);
                }
               
            }
            
        }
        
        bw.close();
    }
    
    private static void AddSingleDependency(Dependency dependency, BufferedWriter bw) throws Exception {
        String structID = "(SELECT max(id) FROM structures)";
        String query = "INSERT INTO dependencies (struct, dependency, governor, dependent) VALUES (" + structID + ", '" + dependency.type + "', '" + dependency.governor + "', '" + dependency.dependent + "');\n";
        bw.write(query);
        
        for (String specification : dependency.expectations.keySet()) {
            String expectation = dependency.expectations.get(specification);
            String depID = "(SELECT max(id) FROM dependencies)";
            query = "INSERT INTO specifications (dependency, spec, expectation) VALUES (" + depID + ", '" + specification + "', '" + expectation + "');\n";
            bw.write(query);
        }
    }
    
}
