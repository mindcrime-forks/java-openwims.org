/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Preprocessor;

import java.util.Collections;
import java.util.LinkedList;
import org.openwims.WIMGlobals;

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
    
    public LinkedList<PPToken> listRoots() {
        LinkedList<PPToken> roots = new LinkedList(this.tokens);
        for (PPDependency dependency : dependencies) {
            roots.remove(dependency.getDependent());
        }
        
        Collections.sort(roots);
        
        return roots;
    }
    
    public LinkedList<PPToken> listDependents(PPToken governor) {
        LinkedList<PPToken> dependents = new LinkedList();
        for (PPDependency dependency : dependencies) {
            if (dependency.getGovernor() == governor) {
                dependents.add(dependency.getDependent());
            }
        }
        
        Collections.sort(dependents);
        
        return dependents;
    }
    
    public LinkedList<PPToken> listTokens() {
        return new LinkedList(this.tokens);
    }
    
    public LinkedList<PPToken> listTokens(String pos) {
        LinkedList<PPToken> tokensByPOS = new LinkedList();
        
        for (PPToken token : this.tokens) {
            if (WIMGlobals.tagmaps().doTagsMatch(token.pos, pos)) {
                tokensByPOS.add(token);
            }
        }
        
        return tokensByPOS;
    }
    
    public LinkedList<PPToken> listFlattenedTokens() {
        LinkedList<PPToken> tokens = new LinkedList();
        
        for (PPToken token : listRoots()) {
            flattenTokens_RECURSIVE(tokens, token);
        }
        
        return tokens;
    }
    
    private void flattenTokens_RECURSIVE(LinkedList<PPToken> tokens, PPToken token) {
        tokens.add(token);
        for (PPToken dependent : listDependents(token)) {
            flattenTokens_RECURSIVE(tokens, dependent);
        }
    }
    
    public LinkedList<PPDependency> listDependencies() {
        return new LinkedList(this.dependencies);
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public void addToken(PPToken token) {
        this.tokens.add(token);
    }
    
    public void addDependency(PPDependency dependency) {
        this.dependencies.add(dependency);
    }
    
    public PPToken tokenWithAnchor(String anchor) {
        for (PPToken token : tokens) {
            if (token.anchor().equals(anchor)) {
                return token;
            }
        }
        
        return null;
    }
    
    public PPToken tokenWithIndex(int index) {
        for (PPToken token : tokens) {
            if (token.index == index) {
                return token;
            }
        }
        
        return null;
    }
    
    public void removeDependency(PPDependency dependency) {
        this.dependencies.remove(dependency);
    }
    
}
