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
public class PPSentence {
    
    protected String text;
    protected LinkedList<PPToken> tokens;
    protected LinkedList<PPDependency> dependencies;

    public PPSentence() {
        this.text = "";
        this.tokens = new LinkedList();
        this.dependencies = new LinkedList();
    }
    
    public String text() {
        return this.text;
    }
    
    public LinkedList<PPToken> listTokens() {
        return new LinkedList(this.tokens);
    }
    
    public LinkedList<PPDependency> listDependencies() {
        return new LinkedList(this.dependencies);
    }
    
}
