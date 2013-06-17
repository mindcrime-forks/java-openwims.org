/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author jesse
 */
public class Sense {
    
    private String id;
    private LinkedList<Structure> structures;
    private LinkedList<Meaning> meanings;
    private String definition;
    
    public Sense(String id) {
        this.id = id;
        this.structures = new LinkedList();
        this.meanings = new LinkedList();
        this.definition = "";
    }

    public String getId() {
        return id;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinition() {
        return definition;
    }
    
    public Structure addStructure() {
        Structure s = new Structure();
        this.structures.add(s);
        return s;
    }
    
    public String concept() {
        if (this.id.contains(":")) {
            return this.id.split(":")[0].trim();
        }
        
        return "unknown";
    }
    
    public String pos() {
        if (this.id.contains("-")) {
            return this.id.split("-")[1].trim();
        }
        
        return "unknown";
    }
    
    public void addMeaning(String target, String relation, String wim) {
        this.meanings.add(new Meaning(target, relation, wim));
    }
    
    public LinkedList<Structure> listStructures() {
        return new LinkedList(this.structures);
    }
    
    public LinkedList<Meaning> listMeanings() {
        return new LinkedList(this.meanings);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        
        out.append(this.id);
        out.append("\n");
        out.append(this.definition);
        
        out.append("\n Structures:\n");
        for (Structure structure : structures) {
            out.append("  ");
            out.append(structure);
        }
        
        out.append("\n Meaning:\n");
        for (Meaning meaning : this.meanings) {
            out.append("  ");
            out.append(meaning);
        }
        
        return out.toString();
    }
    
}
