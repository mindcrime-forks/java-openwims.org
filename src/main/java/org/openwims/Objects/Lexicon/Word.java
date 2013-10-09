/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesse
 */
public class Word {
    
    private String representation;
    private HashMap<String, Sense> senses;

    public Word(String representation) {
        this.representation = representation;
        this.senses = new HashMap();
    }

    public String getRepresentation() {
        return representation;
    }
    
    public void addSense(Sense sense) {
        this.senses.put(sense.getId(), sense);
    }
    
    public void removeSense(Sense sense) {
        this.senses.remove(sense.getId());
    }
    
    public Sense getSense(String id) {
        return this.senses.get(id);
    }
    
    public LinkedList<Sense> listSenses() {
        return new LinkedList(this.senses.values());
    }
    
    public LinkedList<Sense> listSenses(String pos) {
        LinkedList<Sense> matches = new LinkedList();
        
        for (Sense sense : this.senses.values()) {
            if (WIMGlobals.tagmaps().doTagsMatch(pos, sense.pos())) {
                matches.add(sense);
            }
        }
        
        return matches;
    }
    
    public boolean canBeConcept(String concept) {
        for (Sense sense : senses.values()) {
            if (WIMGlobals.ontology().isDescendant(sense.concept(), concept)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(this.representation);
        out.append("\n\n");
        
        for (Sense sense : this.listSenses()) {
            out.append(sense);
            out.append("\n");
        }
        
        return out.toString();
    }
    
}
