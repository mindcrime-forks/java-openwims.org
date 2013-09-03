/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Preprocessor;

import java.util.LinkedList;
import java.util.List;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesseenglish
 */
public class PPToken {
    protected LinkedList<PPMention> mentions;
    
    public PPToken(){
        mentions = new LinkedList<PPMention>();
    }

    public LinkedList<PPMention> getMentions() {
        return mentions;
    }

    public LinkedList<PPMention> listMentions(String pos, PPSentence sentence) {
        LinkedList<PPMention> mentionsByPOS = new LinkedList();
        
        for (PPMention mention : this.mentions) {
            if (WIMGlobals.tagmaps().doTagsMatch(mention.pos, pos) && sentence == mention.getSentence()) {
                mentionsByPOS.add(mention);
            }
        }
        
        return mentionsByPOS;
    }
    
    public String bestAnchor() {
        //HACK: need to return "BEST" anchor
        return mentions.getFirst().anchor();
    }
    
    public String anchor(PPSentence sentence) {
        for (PPMention mention : mentions) {
            if (mention.getSentence() == sentence) {
                return mention.anchor();
            }
        }
        
        return null;
    }
    
    public String pos(PPSentence sentence) {
        for (PPMention mention : mentions) {
            if (mention.getSentence() == sentence) {
                return mention.pos();
            }
        }
        
        return null;
    }
    
    public String lemma(PPSentence sentence) {
        for (PPMention mention : mentions) {
            if (mention.getSentence() == sentence) {
                return mention.lemma();
            }
        }
        
        return null;
    }
    
    public LinkedList<String> anchors(){
        LinkedList<String> anchors = new LinkedList<String>();
        
        for (PPMention mention : mentions) {
            anchors.add(mention.anchor());
        }
        return anchors;
    }
    
}
