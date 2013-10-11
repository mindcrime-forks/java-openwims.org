/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Serialization;

import org.openwims.Objects.Lexicon.Sense;

/**
 *
 * @author jesseenglish
 */
public abstract class LexiconSerializer {
    
    public abstract void saveSense(Sense sense) throws Exception;
    
}
