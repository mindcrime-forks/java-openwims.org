/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

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
    private String example;
    private double frequency;
    
    public Sense(String id) {
        this.id = id;
        this.structures = new LinkedList();
        this.meanings = new LinkedList();
        this.definition = "";
        this.example = "";
        this.frequency = 0.5;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getDefinition() {
        return definition;
    }

    public String getExample() {
        return example;
    }
    
    public void addStructure(Structure structure) {
        this.structures.add(structure);
    }
    
    public void removeStructure(Structure structure) {
        this.structures.remove(structure);
    }
    
    public void removeMeaning(Meaning meaning) {
        this.meanings.remove(meaning);
    }
    
    public String concept() {
        if (this.id.contains(":")) {
            return this.id.split(":")[0].trim();
        }
        
        return "unknown";
    }
    
    public String pos() {
        if (!this.id.contains(":")) {
            return "unknown";
        }
        
        if (this.id.contains("-")) {
            return this.id.split(":")[1].split("-")[1].trim();
        }
        
        return "unknown";
    }
    
    public String word() {
        if (!this.id.contains(":")) {
            return "unknown";
        }
        
        if (this.id.contains("-")) {
            return this.id.split(":")[1].split("-")[0].trim();
        }
        
        return "unknown";
    }
    
    public int instance() {
        if (!this.id.contains(":")) {
            return -1;
        }
        
        if (this.id.contains("-")) {
            String rhs = this.id.split(":")[1];
            String[] parts = rhs.split("-");
            String instance = parts[parts.length - 1].trim();
            
            return Integer.parseInt(instance);
        }
        
        return -1;
    }
    
    public void addMeaning(Meaning meaning) {
        this.meanings.add(meaning);
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
