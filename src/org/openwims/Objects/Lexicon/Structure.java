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
    private LinkedList<Dependency> dependencies;
        
    public Structure() {
        this.dependencies = new LinkedList();
    }

    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);
    }

    public LinkedList<Dependency> listDependencies() {
        return new LinkedList(this.dependencies);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        for (Dependency dependency : dependencies) {
            out.append(dependency);
            out.append(" ");
        }

        return out.toString();
    }
}
