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
import org.openwims.Stanford.StanfordHelper;
import org.openwims.Stanford.StanfordPPDocument;
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
    private LinkedList<DoNotUseSenses> doNotUseSenses;
    
    public NaivePossibilityIterator(PPDocument document) {
        this(HACK_COMBINE_SENTENCES(document));
    }
        
    public NaivePossibilityIterator(PPSentence sentence) {
        super(sentence);
        this.tokens = sentence.listTokens();

        this.senses = new HashMap();
        this.pointers = new HashMap();
        this.finished = false;
        
        this.doNotUseSenses = new LinkedList();
        
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
        
        if (!validate(next) && hasNext()) {
            return next();
        }

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
    
    private boolean validate(HashMap<PPToken, Sense> possibility) {
        for (DoNotUseSenses dnus : this.doNotUseSenses) {
            if (!dnus.validate(possibility)) {
                return false;
            }
        }
        
        return true;
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

    @Override
    public void doNotUseSense(PPToken token, Sense sense) {
        if (senses.get(token).indexOf(sense) < pointers.get(token)) {
            pointers.put(token, pointers.get(token) - 1);
        }
        
        this.senses.get(token).remove(sense);
    }

    @Override
    public void doNotUseSenses(PPToken token1, Sense sense1, PPToken token2, Sense sense2) {
        this.doNotUseSenses.add(new DoNotUseSenses(token1, token2, sense1, sense2));
    }
    
    private class DoNotUseSenses {
        
        private PPToken token1;
        private PPToken token2;
        private Sense sense1;
        private Sense sense2;

        public DoNotUseSenses(PPToken token1, PPToken token2, Sense sense1, Sense sense2) {
            this.token1 = token1;
            this.token2 = token2;
            this.sense1 = sense1;
            this.sense2 = sense2;
            System.out.println(this);
        }
        
        public boolean validate(HashMap<PPToken, Sense> possibility) {
            if (possibility.get(token1) == sense1 && possibility.get(token2) == sense2) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return token1.text() + " -> " + sense1.getId() + " && " + token2.text() + " -> " + sense2.getId();
        }
        
        
        
    }
    
}
