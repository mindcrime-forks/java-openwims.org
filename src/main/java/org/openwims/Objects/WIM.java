/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects;

import java.util.LinkedList;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 *
 * @author jesseenglish
 */
public class WIM implements Comparable<WIM> {
    
    private LinkedList<WIMFrame> frames;

    public WIM() {
        this.frames = new LinkedList();
    }
    
    public LinkedList<WIMFrame> listFrames() {
        return new LinkedList(frames);
    }
    
    public WIMFrame frame(PPToken anchor) {
        for (WIMFrame frame : frames) {
            if (frame.getAnchors().containsKey(anchor)) {
                return frame;
            }
        }
        
        WIMFrame frame = new WIMFrame(anchor, null);
        this.frames.add(frame);
        return frame;
    }
    
    //adds all content from f1 to f2, cleans up all pointers to f2
    public void merge(WIMFrame f1, WIMFrame f2) {
        for (PPToken anchor : f2.getAnchors().keySet()) {
            f1.getAnchors().put(anchor, f2.getAnchors().get(anchor));
        }
        
        for (WIMAttribute attribute : f2.listAttributes()) {
            f1.addAttribute(attribute);
        }
        
        for (WIMRelation relation : f2.listRelations()) {
            f1.addRelation(relation);
        }
        
        for (WIMRelation inverse : f2.listInverses()) {
            f1.addInverse(inverse);
        }
        
        for (WIMFrame frame : frames) {
            for (WIMRelation relation : frame.listRelations()) {
                if (relation.getFrame() == f2) {
                    relation.setFrame(f1);
                }
            }
            for (WIMRelation inverse : frame.listInverses()) {
                if (inverse.getFrame() == f2) {
                    inverse.setFrame(f1);
                }
            }
        }
        
        
        this.frames.remove(f2);
    }
    
    public double score() {
        double score = 1.0;
        
        for (WIMFrame frame : frames) {
            score = score * (frame.score());
        }
        
        return score;
    }

    @Override
    public String toString() {
        String out = "";
        for (WIMFrame frame : frames) {
            out += frame + "\n\n";
        }
        return out;
    }

    public int compareTo(WIM o) {
        return (int)(this.score() * 100) - (int)(o.score() * 100);
    }
    
}
