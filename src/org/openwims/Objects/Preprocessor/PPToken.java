/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Preprocessor;

/**
 *
 * @author jesseenglish
 */
public class PPToken implements Comparable<PPToken> {
    
    protected int index;
    protected String text;
    protected String lemma;
    protected String pos;

    public PPToken() {
        this.index = -1;
        this.text = "";
        this.lemma = "";
        this.pos = "";
    }
    
    public String anchor() {
        return this.text + "-" + this.index;
    }
    
    public String text() {
        return this.text;
    }
    
    public String lemma() {
        return this.lemma;
    }
    
    public String pos() {
        return this.pos;
    }

    @Override
    public int compareTo(PPToken t) {
        return this.index - t.index;
    }
    
}
