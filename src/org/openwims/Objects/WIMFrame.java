/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects;

import java.util.LinkedList;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 *
 * @author jesse
 */
public class WIMFrame {
    
    private PPToken anchor;
    private Sense sense;
    private LinkedList<WIMRelation> relations;
    private LinkedList<WIMRelation> inverses;
    private LinkedList<WIMAttribute> attributes;
    private int instance;
    
    public WIMFrame(PPToken anchor) {
        this.anchor = anchor;
        this.sense = null;
        this.relations = new LinkedList();
        this.inverses = new LinkedList();
        this.attributes = new LinkedList();
        this.instance = 1;
    }

    public PPToken getAnchor() {
        return anchor;
    }
    
//    public String getToken() {
//        String pattern = "-[0-9]*$";
//        return this.anchor.replaceAll(pattern, "");
//    }

    public int getInstance() {
        return instance;
    }

    public void setInstance(int instance) {
        this.instance = instance;
    }

    public void setSense(Sense sense) {
        this.sense = sense;
    }

    public Sense getSense() {
        return sense;
    }
    
    public void addRelation(WIMRelation relation) {
        this.relations.add(relation);
    }
    
    public void addInverse(WIMRelation inverses) {
        this.inverses.add(inverses);
    }
    
    public void addAttribute(WIMAttribute attribute) {
        this.attributes.add(attribute);
    }
    
    public LinkedList<WIMRelation> listRelations() {
        return new LinkedList(this.relations);
    }
    
    public LinkedList<WIMRelation> listInverses() {
        return new LinkedList(this.inverses);
    }
    
    public LinkedList<WIMAttribute> listAttributes() {
        return new LinkedList(this.attributes);
    }
    
    private boolean doInversesContainRelation(String relation) {
        for (WIMRelation inverse : this.inverses) {
            if (inverse.getRelation().equalsIgnoreCase(relation)) {
                return true;
            }
        }
        
        return false;
    }
    
    public String getConcept() {
        String concept = "unknown";
        
        if (this.sense != null) {
            concept = this.sense.concept();
        }
        
        if (concept.equalsIgnoreCase("unknown")) {
            concept = "";
            
            //if there are properties (but it is not an agent of something else), it is an event
            if (this.relations.size() > 0 && !doInversesContainRelation("agent")) {
                concept = "event";
            }
            //if [agent, x, y, z] points to it, then it is an actor
            if (doInversesContainRelation("agent")) {
                concept += " actor";
            }
            
            //if there are no properties, but [theme, instrument, location a, b, c] point to, then it is a prop
            if (this.relations.size() == 0 && (doInversesContainRelation("theme") || doInversesContainRelation("instrument") || doInversesContainRelation("location"))) {
                concept += " prop";
            }
            
            concept = concept.trim().replaceAll(" ", "+");
            
            if (concept.length() == 0) {
                concept = "unknown";
            }
            
            concept = "*" + concept;
        }
        
        return concept;
    }
    
    public String getName() {
        return getConcept() + "-" + this.instance;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        
        out.append(getName());
        out.append("\n  ");
        out.append("anchor: ");
        out.append(this.anchor.anchor());
        out.append("\n  ");
        out.append("sense: ");
        out.append(this.sense == null ? "unknown" : this.sense.getId());
        out.append("\n  ");
        
        for (WIMRelation relation : relations) {
            out.append(relation);
            out.append("\n  ");
        }
        
        for (WIMAttribute attribute : attributes) {
            out.append(attribute);
            out.append("\n  ");
        }
        
        return out.toString().trim();
    }
    
}
