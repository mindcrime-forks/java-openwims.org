/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesse
 */
public class WIMFrame {
    
    private HashMap<PPToken, Sense> anchors;
    private LinkedList<WIMRelation> relations;
    private LinkedList<WIMRelation> inverses;
    private LinkedList<WIMAttribute> attributes;
    private int instance;
    
    public WIMFrame(PPToken anchor, Sense sense) {
        this.anchors = new HashMap();
        this.anchors.put(anchor, sense);
        this.relations = new LinkedList();
        this.inverses = new LinkedList();
        this.attributes = new LinkedList();
        this.instance = 1;
    }

    public HashMap<PPToken, Sense> getAnchors() {
        return anchors;
    }

    public int getInstance() {
        return instance;
    }

    public void setInstance(int instance) {
        this.instance = instance;
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
    
    public double score() {
        double score = 0.0;
        for (Sense sense : this.anchors.values()) {
            score += sense.getFrequency();
        }
        score = score / (double)this.anchors.size();
        
        return score + 0.5;
    }
    
    public LinkedList<String> getConcepts() {
        HashSet<String> concepts = new HashSet();
        
        for (Sense sense : this.anchors.values()) {
            String concept = "unknown";
        
            if (sense != null) {
                concept = sense.concept();
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
            
            concepts.add(concept);
        }
        
        //prune redundant concepts
        OUTER:
        for (Iterator<String> it = concepts.iterator(); it.hasNext();) {
            String concept = it.next();
            for (String child : concepts) {
                if (WIMGlobals.ontology().concept(child).isDescendant(WIMGlobals.ontology().concept(concept)) && !child.equalsIgnoreCase(concept)) {
                    it.remove();
                    continue OUTER;
                }
            }
        }
        
        LinkedList<String> out = new LinkedList(concepts);
        Collections.sort(out);
        
        return out;
    }
    
    public String getName() {
        String name = "";
        for (String concept : getConcepts()) {
            name += concept;
        }
        
        return name + "-" + this.instance;
    }
    
    public String json() {
        StringBuilder out = new StringBuilder();
        
//        out.append("{");
//        out.append("\"frame\": \"");
//        out.append(getName());
//        out.append("\", \"anchor\":\"");
//        out.append(this.anchor.anchor());
//        out.append("\", \"sense\":\"");
//        out.append(this.sense == null ? "unknown" : this.sense.getId());
//        out.append("\", \"relations\":[");
//        
//        for (WIMRelation relation : relations) {
//            out.append(relation.json());
//            
//            if (relation != relations.getLast()) {
//                out.append(",");
//            }
//        }
//        
//        out.append("], \"attributes\":[");
//        for (WIMAttribute attribute : attributes) {
//            out.append(attribute.json());
//            
//            if (attribute != attributes.getLast()) {
//                out.append(",");
//            }
//        }
//        
//        out.append("]}");
        
        return out.toString();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        
//        out.append(getName());
//        out.append("\n  ");
//        out.append("anchor: ");
//        out.append(this.anchor.anchor());
//        out.append("\n  ");
//        out.append("sense: ");
//        out.append(this.sense == null ? "unknown" : this.sense.getId());
//        out.append("\n  ");
//        
//        for (WIMRelation relation : relations) {
//            out.append(relation);
//            out.append("\n  ");
//        }
//        
//        for (WIMAttribute attribute : attributes) {
//            out.append(attribute);
//            out.append("\n  ");
//        }
        
        return out.toString().trim();
    }
    
}
