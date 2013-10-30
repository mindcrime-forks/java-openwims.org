/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Ontology;

import java.util.LinkedList;

/**
 *
 * @author jesseenglish
 */
public class Concept {
    
    private String name;
    private Concept parent;
    private LinkedList<Concept> children;
    private String definition;
    private String gloss;
    
    public Concept(String name) {
        this.name = name;
        this.parent = null;
        this.children = new LinkedList();
        this.definition = "";
        this.gloss = "";
    }

    public String getName() {
        return name;
    }

    public String getDefinition() {
        return definition;
    }

    public Concept getParent() {
        return parent;
    }

    public String getGloss() {
        return gloss;
    }

    public LinkedList<Concept> listChildren() {
        return new LinkedList(this.children);
    }
    
    public void setParent(Concept parent) {
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        
        this.parent = parent;
        
        this.parent.addChild(this);
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }
    
    public void addChild(Concept child) {
        this.children.add(child);
    }
    
    public void removeChild(Concept child) {
        this.children.remove(child);
    }
    
    public boolean isDescendant(Concept concept) {
        if (concept == this) {
            return true;
        }
        
        if (concept == parent) {
            return true;
        }
        
        if (parent == null) {
            return false;
        }
        
        if (parent.isDescendant(concept)) {
            return true;
        }
        
        return false;
    }
    
    public int depth() {
        if (this.parent == null) {
            return 0;
        } else {
            return this.parent.depth() + 1;
        }
    }
    
}
