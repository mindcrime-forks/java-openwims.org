/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors.Iterators;

import java.util.HashMap;
import java.util.Iterator;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPSentence;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 *
 * @author jesseenglish
 */
public abstract class PossibilityIterator implements Iterator<HashMap<PPToken, Sense>> {
    
    protected PPSentence sentence;

    public PossibilityIterator(PPSentence sentence) {
        this.sentence = sentence;
    }

    public PPSentence getSentence() {
        return sentence;
    }
    
    public abstract void doNotUseSense(PPToken token, Sense sense);
    
    public abstract void doNotUseSenses(PPToken token1, Sense sense1, PPToken token2, Sense sense2);
    
}
