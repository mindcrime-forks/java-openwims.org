/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors.Microtheories;

import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 *
 * @author jesseenglish
 */
public abstract class Microtheory {
    
    public abstract boolean test(Sense sense);
    public abstract boolean test(PPToken token);
    
}
