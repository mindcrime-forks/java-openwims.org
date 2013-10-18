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
    
}
