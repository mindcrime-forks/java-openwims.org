/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Disambiguation;

import java.util.LinkedList;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 *
 * @author jesseenglish
 */
public class InterpretationSet {

    public LinkedList<SenseMapping> mappings;

    public InterpretationSet() {
        this.mappings = new LinkedList();
    }

    public InterpretationSet(InterpretationSet copy) {
        this.mappings = new LinkedList(copy.mappings);
    }

    public boolean doesMappingViolateSet(SenseMapping mapping) {
        for (PPToken token : mapping.senses.keySet()) {
            Sense sense = mapping.senses.get(token);

            for (SenseMapping m : mappings) {
                if (m.senses.get(token) != null && m.senses.get(token) != sense) {
                    return true;
                }
            }
        }

        return false;
    }
    
    public boolean doesMappingCoverTokens(LinkedList<PPToken> tokens) {
        TOKENS:
        for (PPToken token : tokens) {
            for (SenseMapping mapping : this.mappings) {
                if (mapping.anchor == token || mapping.senses.containsKey(token)) {
                    continue TOKENS;
                }
            }
            
            //no match found
            return false;
        }
        
        return true;
    }
}