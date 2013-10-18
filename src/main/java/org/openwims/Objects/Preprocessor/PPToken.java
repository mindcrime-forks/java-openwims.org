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
public class PPToken implements Comparable<PPToken> {
    
    protected int index;
    protected String text;
    protected String lemma;
    protected String pos;
    protected String nerType;
    protected LinkedList<PPToken> corefers;

    public PPToken() {
        this.index = -1;
        this.text = "";
        this.lemma = "";
        this.pos = "";
        this.nerType = "";
        this.corefers = new LinkedList();
    }

    public LinkedList<PPToken> getCorefers() {
        return corefers;
    }

    public void setNERtype(String NERtype) {
        this.nerType = NERtype;
    }
    
    public void setAnchor(String anchor) {
        String[] parts = anchor.split("-");
        this.text = parts[0];
        this.index = Integer.parseInt(parts[1]);
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
    
    public String lemma() {
        return this.lemma;
    }
    
    public String pos() {
        return this.pos;
    }
    
    public String nerType() {
        return this.nerType;
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

    @Override
    public String toString() {
        return this.anchor();
    }
    
}
