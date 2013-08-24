/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Preprocessor;

import org.openwims.WIMGlobals;

/**
 *
 * @author Ben
 */
public class PPMention {
    protected int index;
    protected String text;
    protected String lemma;
    protected String pos;
    protected PPSentence sentence;

    public PPMention() {
        this.index = -1;
        this.text = "";
        this.lemma = "";
        this.pos = "";
        this.sentence = null;
    }
    
    public void setAnchor(String anchor) {
        String[] parts = anchor.split("-");
        this.text = parts[0];
        this.index = Integer.parseInt(parts[1]);
    }

    public void setSentence(PPSentence sentence) {
        this.sentence = sentence;
    }

    public PPSentence getSentence() {
        return sentence;
    }
    
    public void setLemma(String lemma) {
        this.lemma = lemma;
    }
    
    public void setPOS(String pos) {
        this.pos = pos;
    }
    
    public String anchor() {
        return this.text + "-" + this.index;
    }
    
    public String text() {
        return this.text;
    }

    public int getIndex() {
        return index;
    }
    
    public String lemma() {
        return this.lemma;
    }
    
    public String pos() {
        return this.pos;
    }
    
    public String rootPOS() {
        String[] roots = new String[] { "N", "V", "J", "R" };
        
        for (String root : roots) {
            if (WIMGlobals.tagmaps().doTagsMatch(root, pos())) {
                return root;
            }
        }
        
        return pos();
    }

//    @Override
//    public int compareTo(PPToken t) {
//        return this.index - t.index;
//    }

    @Override
    public String toString() {
        return this.anchor();
    }
}
