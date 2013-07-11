/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Preprocessor;

import org.openwims.WIMGlobals;

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
    
    public String rootPOS() {
        String[] roots = new String[] { "N", "V", "J", "R" };
        
        for (String root : roots) {
            if (WIMGlobals.tagmaps().doTagsMatch(root, pos())) {
                return root;
            }
        }
        
        return pos();
    }

    @Override
    public int compareTo(PPToken t) {
        return this.index - t.index;
    }
    
}
