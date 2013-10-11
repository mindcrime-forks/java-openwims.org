/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors.Microtheories;

import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 * @author jesseenglish
 * @todo   The method for testing Sense objects is inadequate.
 */
public class TimeMicrotheory extends Microtheory {

    @Override
    public boolean test(Sense sense) {
        return sense.concept().equalsIgnoreCase("@temporal-object");
    }

    @Override
    public boolean test(PPToken token) {
        return token.nerType().equalsIgnoreCase("time");
    }
    
}
