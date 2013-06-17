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
public class Structure {
    private LinkedList<DependencySet> dependencies;
        
    public Structure() {
        this.dependencies = new LinkedList();
    }

    public DependencySet addDependencySet() {
        DependencySet dependencySet = new DependencySet(new LinkedList(), new LinkedList(), true, "");
        this.dependencies.add(dependencySet);
        
        return dependencySet;
    }

    public LinkedList<DependencySet> listDependencies() {
        return new LinkedList(this.dependencies);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        for (DependencySet dependencySet : dependencies) {
            out.append(dependencySet);
            out.append(" ");
        }

        return out.toString();
    }
    
}
