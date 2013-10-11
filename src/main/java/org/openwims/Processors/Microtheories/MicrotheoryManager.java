/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors.Microtheories;

import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 *
 * @author jesseenglish
 */
public class MicrotheoryManager {
    
    private LinkedList<String> unavailable;
    private HashMap<String, Microtheory> microtheories;

    public MicrotheoryManager() {
        this.unavailable = new LinkedList();
        this.microtheories = new HashMap();
        
        this.microtheories.put("#time", new TimeMicrotheory());
        this.microtheories.put("#date", new DateMicrotheory());
    }
    
    public boolean test(String microtheory, Sense sense) {
        Microtheory m = this.microtheories.get(microtheory);
        if (m != null) {
            return m.test(sense);
        } else {
            if (!this.unavailable.contains(microtheory)) {
                this.unavailable.add(microtheory);
                System.out.println("WARNING: microtheory " + microtheory + " not implemented!");
            }
            return false;
        }
    }
    
    public boolean test(String microtheory, PPToken token) {
        Microtheory m = this.microtheories.get(microtheory);
        if (m != null) {
            return m.test(token);
        } else {
            if (!this.unavailable.contains(microtheory)) {
                this.unavailable.add(microtheory);
                System.out.println("WARNING: microtheory " + microtheory + " not implemented!");
            }
            return false;
        }
    }
    
}
