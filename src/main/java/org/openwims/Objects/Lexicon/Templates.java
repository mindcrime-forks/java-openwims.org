/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jesseenglish
 */
public class Templates {
    
    private HashMap<String, LinkedList<DependencySet>> templates;
    private HashMap<DependencySet, String> examples;

    public Templates() {
        this.templates = new HashMap();
        this.examples = new HashMap();
        
        try {
            parseTemplates();
        } catch (Exception ex) {
            Logger.getLogger(Templates.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public LinkedList<DependencySet> templates(String pos) {
        return this.templates.get(pos);
    }
    
    public String example(DependencySet dependencySet) {
        return this.examples.get(dependencySet);
    }
    
    public DependencySet template(String pos, String name) {
        for (DependencySet dependencySet : this.templates.get(pos)) {
            if (dependencySet.label.equals(name)) {
                return dependencySet;
            }
        }
        
        return null;
    }
    
    private void parseTemplates() throws Exception {
        this.templates = new HashMap();
        this.examples = new HashMap();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(Templates.class.getResourceAsStream("/assets/deptemplates")));
        String line = null;
        
        DependencySet current = null;
        
        while ((line = br.readLine()) != null) {
            if (current == null) {
                current = new DependencySet(new LinkedList(), new LinkedList(), false, line.split("\\(")[0].replaceAll("\\+", ""));
                if (line.startsWith("+")) {
                    current.optional = true;
                }
                String pos = line.split("\\(")[1].split("\\)")[0];
                
                LinkedList<DependencySet> ts = templates.get(pos);
                if (ts == null) {
                    ts = new LinkedList();
                    templates.put(pos, ts);
                }
                
                ts.add(current);
                continue;
            }
            if (line.contains("(")) {
                Dependency dep = new Dependency("", "", "", new LinkedList());
                dep.type = line.split("\\(")[0];
                line = line.replaceFirst(dep.type, "").replaceAll("\\(", "").replaceAll("\\)", "");
                
                dep.governor = line.substring(0, line.indexOf(","));
                line = line.replaceFirst(dep.governor + ",", "");
                
                dep.dependent = line.split("\\[")[0];
                line = line.replaceFirst(dep.dependent, "").replaceAll("\\[", "").replaceAll("\\]", "");
                
                if (line.length() > 0) {
                    for (String exp : line.split(",")) {
                        dep.expectations.add(new Expectation(exp.split("=")[0], exp.split("=")[1]));
                    }
                }
                
                current.dependencies.add(dep);
                continue;
            }
            if (line.startsWith(">")) {
                line = line.replaceAll(">", "");
                
                Meaning meaning = new Meaning("", "", "");
                meaning.target = line.split("\\.")[0];
                line = line.replaceAll(meaning.target, "").replaceAll("\\.", "");
                
                meaning.relation = line.split("=")[0];
                meaning.wim = line.split("=")[1];
                
                current.meanings.add(meaning);
                continue;
            }
            if (line.startsWith("\"")) {
                this.examples.put(current, line);
                continue;
            }
            if (line.trim().length() == 0) {
                current = null;
                continue;
            }
        }
        
        for (LinkedList<DependencySet> ts : templates.values()) {
            Collections.sort(ts, new TemplateComparator());
        }
    }
    
    private class TemplateComparator implements Comparator<DependencySet> {

        public int compare(DependencySet o1, DependencySet o2) {
            return o1.label.compareTo(o2.label);
        }
    
    }
    
}
