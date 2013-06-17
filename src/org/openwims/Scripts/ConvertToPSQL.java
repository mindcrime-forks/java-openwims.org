/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import org.openwims.Objects.Ontology.Ontology.Ancestry;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesse
 */
public class ConvertToPSQL {
    
    public static void main(String[] args) throws Exception {
        
        File file = new File("/Users/jesse/Desktop/ontology.dump.sql");
 
        // if file doesnt exists, then create it
        if (!file.exists()) {
                file.createNewFile();
        }
 
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        
        
        
        for (String concept : WIMGlobals.ontology().concepts()) {
            Ancestry a = WIMGlobals.ontology().ancestors(concept);
            for (LinkedList<String> path : a.paths) {
                bw.write("INSERT INTO ontology (concept, parent, definition) VALUES ('" + concept.replaceAll("'", "''") + "', '" + path.toString().replaceAll("'", "''") + "', '');\n");
            }
        }
        
        
        
        
//        for (String root : WIMGlobals.lexicon().roots()) {
//            Word w = WIMGlobals.lexicon().word(root);
//            for (Sense sense : w.listSenses()) {
//                //bw.write("INSERT INTO senses (id, word, definition) VALUES ('" + sense.getId().replaceAll("'", "''") + "', '" + w.getRepresentation().replaceAll("'", "''") + "', '" + sense.getDefinition().replaceAll("'", "''") + "');\n");
//            
//                for (Structure structure : sense.listStructures()) {
//                    for (DependencySet dependencySet : structure.listDependencies()) {
//                        bw.write("INSERT INTO structures (sense, series, label, optional) VALUES ('" + sense.getId().replaceAll("'", "''") + "', 1, '" + dependencySet.label.replaceAll("'", "''") + "', true);\n");
//                        
//                        
//                        for (Dependency dependency : dependencySet.dependencies) {
//                            bw.write("INSERT INTO dependencies (struct, dependency, governor, dependent) VALUES ((SELECT max(id) FROM structures), '" + dependency.type.replaceAll("'", "''") + "', '" + dependency.governor.replaceAll("'", "''") + "', '" + dependency.dependent.replaceAll("'", "''") + "');\n");
//                            
//                            for (String specification : dependency.expectations.keySet()) {
//                                bw.write("INSERT INTO specifications (dependency, spec, expectation) VALUES ((SELECT max(id) FROM dependencies), '" + specification.replaceAll("'", "''") + "', '" + dependency.expectations.get(specification).replaceAll("'", "''") + "');\n");
//                            }
//                        }
//                    }
//                }
//            
//                for (Meaning meaning : sense.listMeanings()) {
//                    bw.write("INSERT INTO meanings (sense, target, relation, wim) VALUES ('" + sense.getId().replaceAll("'", "''") + "', '" + meaning.target.replaceAll("'", "''") + "', '" + meaning.relation.replaceAll("'", "''") + "', '" + meaning.wim.replaceAll("'", "''") + "');\n");
//                }
//            }
//        }
        
        
        
        
        
        
        
        
        bw.close();
        
    }
    
}
