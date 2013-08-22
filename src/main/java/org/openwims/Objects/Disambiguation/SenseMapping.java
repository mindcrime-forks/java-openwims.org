/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Disambiguation;

import java.util.HashMap;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 *
 * @author jesseenglish
 */
public class SenseMapping {

    public PPToken anchor;
    public Sense root;
    public HashMap<PPToken, Sense> senses;
    public HashMap<Dependency, PPDependency> mappings;

    public SenseMapping(PPToken anchor, Sense root) {
        this.anchor = anchor;
        this.root = root;
        this.senses = new HashMap();
        this.mappings = new HashMap();
    }
    
    public SenseMapping(SenseMapping copy) {
        this.anchor = copy.anchor;
        this.root = copy.root;
        this.senses = new HashMap(copy.senses);
        this.mappings = new HashMap(copy.mappings);
    }

    public boolean isDependencySetComplete(DependencySet set) {
        for (Dependency dependency : set.dependencies) {
            if (this.mappings.get(dependency) == null) {
                return false;
            }
        }

        return true;
    }

    public void removeDependencySet(DependencySet set) {
        for (Dependency dependency : set.dependencies) {
            this.mappings.remove(dependency);
        }
    }

    public PPToken anchorForVariable(String variable) {
        if (variable.equalsIgnoreCase("SELF")) {
            return anchor;
        }
        
        for (Dependency dependency : this.mappings.keySet()) {
            PPDependency ppDependency = this.mappings.get(dependency);
            if (dependency.governor.equals(variable)) {
                return ppDependency.getGovernor();
            } else if (dependency.dependent.equals(variable)) {
                return ppDependency.getDependent();
            }
        }

        return null;
    }
}