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
public class DependencySet {
    public LinkedList<Dependency> dependencies;
    public LinkedList<Meaning> meanings;
    public boolean optional;
    public String label;

    public DependencySet(DependencySet toCopy) {
        this.dependencies = new LinkedList();
        this.meanings = new LinkedList();
        this.optional = toCopy.optional;
        this.label = toCopy.label;
        
        for (Dependency dependency : toCopy.dependencies) {
            this.dependencies.add(new Dependency(dependency));
        }
        for (Meaning meaning : toCopy.meanings) {
            this.meanings.add(new Meaning(meaning));
        }
    }
    
    public DependencySet(LinkedList<Dependency> dependencies, LinkedList<Meaning> meanings, boolean optional, String label) {
        this.dependencies = dependencies;
        this.meanings = meanings;
        this.optional = optional;
        this.label = label;
    }
    
    public double editorScore(DependencySet scoreAgainst) {
        double score = 0.0;
        
        //10% contribution from optional
            //if same = 0
            //else = 1
        double optionalScore = 0.0;
        if (scoreAgainst.optional != optional) {
            optionalScore = 1.0;
        }
        optionalScore *= 0.1; //max 10% contribution
        
        //45% contribution from dependencies
        double dependencyScore = 0.0;
        double dependencyContribution = 1.0 / (double)(this.dependencies.size() + scoreAgainst.dependencies.size());
        OUTER:
        for (Dependency dependency : dependencies) {
            INNER:
            for (Dependency depCandidate : scoreAgainst.dependencies) {
                if (!dependency.type.equals(depCandidate.type)) {
                    continue;
                }
                if (!dependency.governor.equals(depCandidate.governor)) {
                    continue;
                }
                if (!dependency.dependent.equals(depCandidate.dependent)) {
                    continue;
                }
                
                EXPLOOP:
                for (Expectation expectation : dependency.expectations) {
                    for (Expectation expCandidate : depCandidate.expectations) {
                        if (!expectation.getSpecification().equals(expCandidate.getSpecification())) {
                            continue;
                        }
                        if (!expectation.getExpectation().equals(expCandidate.getExpectation())) {
                            continue;
                        }
                        //complete match
                        continue EXPLOOP;
                    }
                    //no matches
                    continue INNER;
                }
                
                //it is a complete match
                //add nothing to the score
                //skip all others
                continue OUTER;
            }
            //there were no matches, add full contribution
            dependencyScore += dependencyContribution;
        }
            
        //45% contribution from meanings
        double meaniningScore = 0.0;
        double meaningContribution = 1.0 / (double)(this.meanings.size() + scoreAgainst.meanings.size());
        OUTER:
        for (Meaning meaning : meanings) {
            for (Meaning meaningCandidate : scoreAgainst.meanings) {
                if (!meaning.target.equals(meaningCandidate.target)) {
                    continue;
                }
                if (!meaning.relation.equals(meaningCandidate.relation)) {
                    continue;
                }
                if (!meaning.wim.equals(meaningCandidate.wim)) {
                    continue;
                }
                //it is a complete match
                //add nothing to the score
                //skip all others
                continue OUTER;
            }
            
            //there were no matches, add full contribution
            meaniningScore += meaningContribution;
        }
        
        
        score += optionalScore + dependencyScore + meaniningScore;
        return score;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        if (!optional) {
            out.append("*");
        }
        out.append(this.label);
        out.append(":");
        for (Dependency dependency : dependencies) {
            out.append("\n   ");
            out.append(dependency);
        }

        return out.toString();
    }
}
