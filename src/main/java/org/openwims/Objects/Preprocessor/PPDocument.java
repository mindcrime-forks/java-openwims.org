/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Preprocessor;

import java.util.LinkedList;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesseenglish
 */
public class PPDocument {
    
    protected LinkedList<PPSentence> sentences;

    public PPDocument() {
        this.sentences = new LinkedList();
    }
    
    public LinkedList<PPSentence> listSentences() {
        return new LinkedList(this.sentences);
    }
    
    public String text() {
        String out = "";
        for (PPSentence sentence : sentences) {
            out += sentence.text + " ";
        }
        return out.trim();
    }
    
    public void addSentence(PPSentence sentence) {
        this.sentences.add(sentence);
    }
    
    public int countPossibleSenseInterpretations() {
        int possibilities = 1;
        for (PPSentence sentence : listSentences()) {
            for (PPToken token : sentence.listTokens()) {
                int senses = WIMGlobals.lexicon().listSenses(token).size();
                possibilities *= senses;
            }
        }
        return possibilities;
    }
    
}
