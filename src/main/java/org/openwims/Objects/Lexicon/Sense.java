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
    
    private String concept;
    private String word;
    private String pos;
    private int instance;
    private LinkedList<Structure> structures;
    private LinkedList<Meaning> meanings;
    private String definition;
    private String example;
    private double frequency;
    private int uid;
    
    public Sense(String concept, String word, String pos, int instance) {
        this.concept = concept;
        this.word = word;
        this.pos = pos;
        this.instance = instance;
        this.structures = new LinkedList();
        this.meanings = new LinkedList();
        this.definition = "";
        this.example = "";
        this.frequency = 0.5;
        this.uid = -1;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public void setInstance(int instance) {
        this.instance = instance;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }

    public String getId() {
        return this.concept + ":" + this.word + "-" + this.pos + "-" + this.instance;
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
        return this.concept;
    }
    
    public String pos() {
        return this.pos;
    }
    
    public String word() {
        return this.word;
    }
    
    public int instance() {
        return this.instance;
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
        
        out.append(this.getId());
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
