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
public class WIM {
    
    private LinkedList<WIMFrame> frames;

    public WIM() {
        this.frames = new LinkedList();
    }
    
    public LinkedList<WIMFrame> listFrames() {
        return new LinkedList(frames);
    }
    
    public WIMFrame frame(PPToken anchor) {
        for (WIMFrame frame : frames) {
            if (frame.getAnchor() == anchor) {
                return frame;
            }
        }
        
        WIMFrame frame = new WIMFrame(anchor);
        this.frames.add(frame);
        return frame;
    }

    @Override
    public String toString() {
        String out = "";
        for (WIMFrame frame : frames) {
            out += frame + "\n\n";
        }
        return out;
    }
    
}
