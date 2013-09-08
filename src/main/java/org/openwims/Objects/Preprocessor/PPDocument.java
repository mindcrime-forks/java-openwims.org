/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Preprocessor;

import java.util.LinkedList;

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

    public void transform() {
        //THIS IS SYNTAX TRANSFORMATIONS!!! HRAAAAA!
        
        for (PPSentence sentence : sentences) {
            sentence.transform();
        }
    }
    
}
