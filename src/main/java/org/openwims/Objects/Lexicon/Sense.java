/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesse
 */
public class Sense {
    
    private String concept;
    private String word;
    private String pos;
    private int instance;
    private LinkedList<DependencySet> dependencySets;
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
        this.dependencySets = new LinkedList();
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
    
    public void addDependencySet(DependencySet dependencySet) {
        this.dependencySets.add(dependencySet);
    }
    
    public void removeDependencySet(DependencySet dependencySet) {
        this.dependencySets.remove(dependencySet);
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
    
    public LinkedList<DependencySet> listDependencySets() {
        return new LinkedList(this.dependencySets);
    }
    
    public LinkedList<Meaning> listMeanings() {
        return new LinkedList(this.meanings);
    }
    
    //calculate a rough approximation of how manually edited this sense is
    public double editorScore() {
        double score = 1.0;
        
        //max 10% contribution for complex concept mapping
            //depth == 0, 1 = 0%
            //depth == 2 = 20%
            //depth == 3 = 50%
            //depth == 4 = 100%
        double mappingContribution = 0.0;
        int depth = WIMGlobals.ontology().concept(this.concept).depth();
        if (depth == 2) {
            mappingContribution = 0.2;
        } else if (depth == 3) {
            mappingContribution = 0.5;
        } else if (depth >= 4) {
            mappingContribution = 1.0;
        }
        mappingContribution *= 0.1; //max 10% contribution
        
        //max 10% contribution for special meanings
            //any special meanings = 100%
        double senseLevelMeaningContribution = 0.0;
        if (this.meanings.size() > 0) {
            senseLevelMeaningContribution = 1.0;
        }
        senseLevelMeaningContribution *= 0.1; //max 10% contribution
        
        //max 80% contribution for dependency set structures
            //cross reference each template (by name) to find total template count (between this sense and the pos template)
            //each template in the total count will contribute 1/n to the total 80%
            //if template is in sense and not pos template == full contribution
            //if template is in pos template and not sense == full contribution
            //else weight contribution by scoring templates against each other
        double dependencySetContribution = 0.0;
        HashSet<String> uniqueTemplateNames = new HashSet();
        HashMap<String, DependencySet> defined = new HashMap();
        HashMap<String, DependencySet> templated = new HashMap();
        for (DependencySet dependencySet : dependencySets) {
            uniqueTemplateNames.add(dependencySet.label);
            defined.put(dependencySet.label, dependencySet);
        }
        for (DependencySet dependencySet : WIMGlobals.templates().templates(pos)) {
            uniqueTemplateNames.add(dependencySet.label);
            templated.put(dependencySet.label, dependencySet);
        }
        double independentContribution = 1.0 / (double)uniqueTemplateNames.size();
        for (DependencySet dependencySet : dependencySets) {
            if (templated.containsKey(dependencySet.label)) {
                dependencySetContribution += independentContribution * dependencySet.editorScore(templated.get(dependencySet.label));
            } else {
                dependencySetContribution += independentContribution; //full contribution
            }
        }
        for (DependencySet dependencySet : WIMGlobals.templates().templates(pos)) {
            if (!defined.containsKey(dependencySet.label)) {
                dependencySetContribution += independentContribution; //full contribution
            }
        }
        
        dependencySetContribution *= 0.8; //max 80% contribution
        
        score = mappingContribution + senseLevelMeaningContribution + dependencySetContribution;
        return score;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        
        out.append(this.getId());
        out.append("\n");
        out.append(this.definition);
        
        out.append("\n Dependency Sets:\n");
        for (DependencySet dependencySet : this.dependencySets) {
            out.append("  ");
            out.append(dependencySet);
        }
        
        out.append("\n Meaning:\n");
        for (Meaning meaning : this.meanings) {
            out.append("  ");
            out.append(meaning);
        }
        
        return out.toString();
    }
    
}
