/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors.Iterators;

import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPSentence;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesseenglish
 */
public class NaivePossibilityIterator extends PossibilityIterator {
            
    private LinkedList<PPToken> tokens;
    private HashMap<PPToken, LinkedList<Sense>> senses;
    private HashMap<PPToken, Integer> pointers;
    private boolean finished;
    
    public NaivePossibilityIterator(PPDocument document) {
        this(HACK_COMBINE_SENTENCES(document));
    }
        
    public NaivePossibilityIterator(PPSentence sentence) {
        super(sentence);
        this.tokens = sentence.listTokens();

        this.senses = new HashMap();
        this.pointers = new HashMap();
        this.finished = false;

        for (PPToken token : this.tokens) {
            this.senses.put(token, WIMGlobals.lexicon().listSenses(token));
            this.pointers.put(token, 0);
        }
    }

    public boolean hasNext() {
        return !this.finished;
    }

    public HashMap<PPToken, Sense> next() {
        HashMap<PPToken, Sense> next = new HashMap();

        for (PPToken token : this.tokens) {
            next.put(token, this.senses.get(token).get(this.pointers.get(token)));
        }

        increment(this.tokens.getLast());

        return next;
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void increment(PPToken token) {
        int pointer = this.pointers.get(token) + 1;
        this.pointers.put(token, pointer);

        if (pointer >= this.senses.get(token).size()) {
            this.pointers.put(token, 0);

            if (token != this.tokens.getFirst()) {
                increment(this.tokens.get(this.tokens.indexOf(token) - 1));
            } else {
                this.finished = true;
            }
        }
    }
    
    private static PPSentence HACK_COMBINE_SENTENCES(PPDocument document) {
        PPSentence sentence = new PPSentence();
        for (PPSentence s : document.listSentences()) {
            for (PPToken token : s.listTokens()) {
                sentence.addToken(token);
            }
            for (PPDependency ppDependency : s.listDependencies()) {
                sentence.addDependency(ppDependency);
            }
        }
        return sentence;
    }
    
}
