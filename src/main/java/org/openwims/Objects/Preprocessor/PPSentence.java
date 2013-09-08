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

//    public LinkedList<PPToken> listTokens(String pos) {
//        LinkedList<PPToken> tokensByPOS = new LinkedList();
//        
//        for (PPToken token : this.tokens) {
//            if (WIMGlobals.tagmaps().doTagsMatch(token.pos, pos)) {
//                tokensByPOS.add(token);
//            }
//        }
//        
//        return tokensByPOS;
//    }
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
            for (PPMention mention : token.getMentions()) {
                if (mention.anchor().equals(anchor) && mention.getSentence() == this) {
                    return token;
                }
            }

        }

        return null;
    }

    public void removeDependency(PPDependency dependency) {
        this.dependencies.remove(dependency);
    }

    void transform() {
        LinkedList<PPDependency> toAdd = new LinkedList<PPDependency>();
        for (PPDependency dep : dependencies) {
            //reambiguate PREP attachment
            if(dep.getType().equalsIgnoreCase("prep")){
                if(WIMGlobals.tagmaps().doTagsMatch(dep.getGovernor().pos(this), "N")){
                    PPToken selector = null;
                    for (PPDependency selDep : dependencies) {
                        if(selDep.getDependent() == dep.getGovernor()){
                            selector = selDep.getGovernor();
                        }
                    }
                    if(selector != null){
                        PPDependency newDep = new PPDependency();
                        newDep.setType("prep");
                        newDep.setGovernor(selector);
                        newDep.setDependent(dep.getDependent());
                        toAdd.add(newDep);
                    }
                }
            }
            
            //NSUBJPASS adds DOBJ
        }
        this.dependencies.addAll(toAdd);
        for (PPDependency dep : dependencies) {
            System.out.println(dep.getType() + "(" + dep.getGovernor().anchor(this)+ "," + dep.getDependent().anchor(this)+ ")");
        }
    }
}
