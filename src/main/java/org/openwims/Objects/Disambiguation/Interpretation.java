/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Disambiguation;

import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Meaning;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.Objects.WIM;
import org.openwims.Objects.WIMAttribute;
import org.openwims.Objects.WIMFrame;
import org.openwims.Objects.WIMRelation;

/**
 *
 * @author jesseenglish
 */
public class Interpretation implements Comparable<Interpretation> {
    
    private HashMap<PPToken, Sense> senses;
    private HashMap<PPDependency, Dependency> dependencies;
    private WIM wim;
    
    private HashMap<DependencySet, HashMap<String, PPToken>> variables;

    public Interpretation(HashMap<PPToken, Sense> senses, HashMap<PPDependency, Dependency> dependencies) {
        this.senses = senses;
        this.dependencies = dependencies;
        this.wim = null;
    }
    
    public WIM wim() {
        if (this.wim == null) {
            wimify();
        }
        
        return this.wim;
    }
    
    private DependencySet dependencySetForDependency(Dependency dependency) {
        for (Sense sense : senses.values()) {
            for (DependencySet dependencySet : sense.listDependencySets()) {
                if (dependencySet.dependencies.contains(dependency)) {
                    return dependencySet;
                }
            }
        }
        return null;
    }
    
    private void mapVariable(DependencySet dependencySet, String variable, PPToken token) {
        HashMap<String, PPToken> mapping = this.variables.get(dependencySet);
        if (mapping == null) {
            mapping = new HashMap();
            this.variables.put(dependencySet, mapping);
        }
        mapping.put(variable, token);
    }
    
    private WIM wimify() {
        this.variables = new HashMap();
        this.wim = new WIM();
        
        for (PPToken token : senses.keySet()) {
            WIMFrame frame = this.wim.frame(token);
            frame.getAnchors().put(token, senses.get(token));
        }
        
        for (PPToken token : senses.keySet()) {
            for (PPDependency ppDependency : dependencies.keySet()) {
                Dependency dependency = dependencies.get(ppDependency);
                if (ppDependency.getGovernor() == token) {
                    mapVariable(dependencySetForDependency(dependency), dependency.governor, token);
                }
                if (ppDependency.getDependent()== token) {
                    mapVariable(dependencySetForDependency(dependency), dependency.dependent, token);
                }
            }
        }
        
        for (DependencySet dependencySet : this.variables.keySet()) {
            for (Meaning meaning : dependencySet.meanings) {
                PPToken domain = this.variables.get(dependencySet).get(meaning.target);
                PPToken range = this.variables.get(dependencySet).get(meaning.wim);
                
                if (meaning.isAttribute()) {
                    WIMAttribute attribute = new WIMAttribute(meaning.relation, meaning.wim);
                    wim.frame(domain).addAttribute(attribute);
                    continue;
                }
                
                WIMRelation relation = new WIMRelation(meaning.relation, wim.frame(range));
                wim.frame(domain).addRelation(relation);
                wim.frame(range).addInverse(relation);
            }
        }
        
        for (PPToken token : senses.keySet()) {
            for (Meaning meaning : senses.get(token).listMeanings()) {
                if (!meaning.isAttribute() || !meaning.target.equalsIgnoreCase("SELF")) {
                    continue; //consider throwing exception here?  this would be invalid knowledge
                }
                
                wim.frame(token).addAttribute(new WIMAttribute(meaning.relation, meaning.wim));
            }
        }
        
        //Now merge all of the corefered frames
        LinkedList<PPToken> alreadyMerged = new LinkedList();
        for (PPToken token : senses.keySet()) {
            if (token.getCorefers().size() > 0 && !alreadyMerged.contains(token)) {
                WIMFrame frame = wim.frame(token);
                for (PPToken coreferred : token.getCorefers()) {
                    wim.merge(frame, wim.frame(coreferred));
                }
                alreadyMerged.add(token);
                alreadyMerged.addAll(token.getCorefers());
            }
        }
        
        //Now clean up all of the frame instance numbers
        HashMap<String, Integer> instances = new HashMap();
        for (WIMFrame frame : wim.listFrames()) {
            Integer count = instances.get(frame.getConcepts().toString());
            if (count == null) {
                instances.put(frame.getConcepts().toString(), new Integer(1));
            } else {
                count++;
                frame.setInstance(count);
                instances.put(frame.getConcepts().toString(), count);
            }
        }
        
        return this.wim;
    }
    
    public double score() {
        return this.wim().score();
    }

    public int compareTo(Interpretation o) {
        return (int)(this.score() * 100) - (int)(o.score() * 100);
    }
    
}
