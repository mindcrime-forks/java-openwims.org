/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.openwims.Objects.Disambiguation.Interpretation;
import org.openwims.Objects.Lexicon.Expectation;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.Processors.Iterators.PossibilityIterator;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesseenglish
 */
public abstract class WIMProcessor {
    
    public static boolean LOGGING = false;
    public static LinkedList<WSELogger> wseLoggers = new LinkedList();
    
    public interface WSEProcessor {
        public LinkedList<Interpretation> wse(PossibilityIterator iter);
    }
    
    public interface WSDProcessor {
        public Interpretation wsd(LinkedList<Interpretation> interpretations);
    }
    
    public interface WSELogger {
        public void logPossibilityElimination(HashMap<PPToken, Sense> possibility, Exception reason);
    }
    
    public static void logPossibilityElimination(HashMap<PPToken, Sense> possibility, Exception reason) {
        if (!LOGGING) {
            return;
        }
        
        for (WSELogger logger : wseLoggers) {
            logger.logPossibilityElimination(possibility, reason);
        }
    }
    
    protected LinkedList<Sense> listSatisfyingSenses(PPToken token, LinkedList<Expectation> expectations) {
        LinkedList<Sense> satisfying = new LinkedList();
        
        //Add the token's POS to the expectations (as a copy, so as not to modify the knowledge)
        //The senses that come out must match this; if the token pos is in contradiction with the
        //expectation's pos, then no matches will occur (which is good)
        LinkedList<Expectation> expectationsCopy = new LinkedList(expectations);
        expectationsCopy.add(new Expectation("pos", token.pos()));
        
        for (Sense sense : WIMGlobals.lexicon().listSenses(token)) {
            if (doesSenseSatisfyExpectations(sense, expectationsCopy)) {
                satisfying.add(sense);
            }
        }
        
        return satisfying;
    }
    
    protected boolean doesPossibilitySatisfyExpectations(PPToken token, HashMap<PPToken, Sense> possibility, LinkedList<Expectation> expectations) {
        EXPLOOP:
        for (Expectation expectation : expectations) {
            if (expectation.getSpecification().equalsIgnoreCase("pos") &&
                !WIMGlobals.tagmaps().doTagsMatch(expectation.getExpectation(), possibility.get(token).pos())) {
                return false;
            } else if (expectation.getSpecification().equalsIgnoreCase("token") &&
                       !(expectation.getExpectation().equalsIgnoreCase(possibility.get(token).word()))) {
                return false;
            } else if (expectation.getSpecification().equalsIgnoreCase("ont")) {
                LinkedList<PPToken> corefers = new LinkedList(token.getCorefers());
                corefers.add(token);
                for (PPToken corefer : corefers) {
                    if (WIMGlobals.ontology().isDescendant(possibility.get(corefer).concept(), expectation.getExpectation())) {
                        continue EXPLOOP;
                    }
                }
                return false;
            } else if (expectation.getSpecification().equalsIgnoreCase("micro")) {
                LinkedList<PPToken> corefers = new LinkedList(token.getCorefers());
                corefers.add(token);
                for (PPToken corefer : corefers) {
                    if (WIMGlobals.microtheories().test(expectation.getExpectation(), possibility.get(corefer))) {
                        continue EXPLOOP;
                    }
                }
                return false;
            }
        }
        
        return true;
    }
    
    protected boolean doesTokenSatisfyExpectations(PPToken token, LinkedList<Expectation> expectations) {
        for (Expectation expectation : expectations) {
            if (expectation.getSpecification().equalsIgnoreCase("pos") && 
                !WIMGlobals.tagmaps().doTagsMatch(expectation.getExpectation(), token.pos())) {
                return false;
            } else if (expectation.getSpecification().equalsIgnoreCase("token") &&
                       !(expectation.getExpectation().equalsIgnoreCase(token.lemma()))) {
                return false;
            } else if (expectation.getSpecification().equalsIgnoreCase("ont")) {
                //A token cannot satisfy ONT; this must be false
                return false;
            } else if (expectation.getSpecification().equalsIgnoreCase("micro")) {
                //A token cannot satisfy MICRO; this must be false
            }
        }
        
        return true;
    }

    protected boolean doesSenseSatisfyExpectations(Sense sense, LinkedList<Expectation> expectations) {
        for (Expectation expectation : expectations) {
            if (expectation.getSpecification().equalsIgnoreCase("pos") && 
                !WIMGlobals.tagmaps().doTagsMatch(expectation.getExpectation(), sense.pos())) {
                return false;
            } else if (expectation.getSpecification().equalsIgnoreCase("token") &&
                       !(expectation.getExpectation().equalsIgnoreCase(sense.word()))) {
                return false;
            } else if (expectation.getSpecification().equalsIgnoreCase("ont")) {
                if (!WIMGlobals.ontology().isDescendant(sense.concept(), expectation.getExpectation())) {
                    return false;
                }
            } else if (expectation.getSpecification().equalsIgnoreCase("micro")) {
                return WIMGlobals.microtheories().test(expectation.getExpectation(), sense);
            }
        }
        
        return true;
    }
    
}
