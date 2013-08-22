/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors;

import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.Objects.Disambiguation.InterpretationSet;
import org.openwims.Objects.Disambiguation.SenseGraph;
import org.openwims.Objects.Disambiguation.SenseMapping;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Expectation;
import org.openwims.Objects.Lexicon.Meaning;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Lexicon.Structure;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.Objects.WIM;
import org.openwims.Objects.WIMFrame;
import org.openwims.Objects.WIMRelation;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesseenglish
 */
public abstract class WIMProcessor {
    
    public interface WSEProcessor {
        public SenseGraph wse(PPDocument document);
    }
    
    public interface WSDProcessor {
        public SenseGraph wsd(SenseGraph graph);
    }
    
    protected boolean doDependenciesMatch(PPToken anchor, Dependency wimDep, PPDependency ppDep, SenseMapping mapping) {
        
        
        //Check to see if the dependency type matches
        if (!wimDep.type.equalsIgnoreCase(ppDep.getType())) {
            return false;
        }
                
        //Check to see that the governor matches any existing variable mappings
        if (wimDep.governor.equalsIgnoreCase("SELF")) {
            if (anchor != ppDep.getGovernor()) {
                return false;
            }
        } else if (mapping.anchorForVariable(wimDep.governor) != null &&
                   mapping.anchorForVariable(wimDep.governor) != ppDep.getGovernor()) {
            return false;
        }
        
        //Check to see that the dependent matches any existing variable mappings and all expectations
        if (wimDep.dependent.equalsIgnoreCase("SELF")) {
            if (anchor != ppDep.getDependent()) {
                return false;
            }
        } else if (mapping.anchorForVariable(wimDep.dependent) != null &&
                   mapping.anchorForVariable(wimDep.dependent) != ppDep.getDependent()) {
            return false;
        } else {
            if (listSatisfyingSenses(ppDep.getDependent(), wimDep.expectations).size() == 0 &&
                !doesTokenSatisfyExpectations(ppDep.getDependent(), wimDep.expectations)) {
                return false;
            }
        }
        
        
        return true;
    }
    
    protected LinkedList<Sense> listSatisfyingSenses(PPToken token, LinkedList<Expectation> expectations) {
        LinkedList<Sense> satisfying = new LinkedList();
        
        //Add the token's POS to the expectations (as a copy, so as not to modify the knowledge)
        //The senses that come out must match this; if the token pos is in contradiction with the
        //expectation's pos, then no matches will occur (which is good)
        LinkedList<Expectation> expectationsCopy = new LinkedList(expectations);
        expectationsCopy.add(new Expectation("pos", token.pos()));
        
        for (Sense sense : WIMGlobals.lexicon().word(token.lemma()).listSenses()) {
            if (doesSenseSatisfyExpectations(sense, expectationsCopy)) {
                satisfying.add(sense);
            }
        }
        
        return satisfying;
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
    
    public LinkedList<WIM> wimify(SenseGraph senseGraph) {
        LinkedList<WIM> wims = new LinkedList();
        
        for (InterpretationSet interpretation : senseGraph.interpretations) {
            WIM wim = new WIM();
            
            for (SenseMapping mapping : interpretation.mappings) {
                WIMFrame verbFrame = wim.frame(mapping.anchor);
                verbFrame.setSense(mapping.root);
                
                //Find all of the completed dependency sets in from the sense
                //and map their meanings
                for (Structure structure : mapping.root.listStructures()) {
                    for (DependencySet dependencySet : structure.listDependencies()) {
                        if (mapping.isDependencySetComplete(dependencySet)) {
                            for (Meaning meaning : dependencySet.meanings) {
                                WIMFrame source = verbFrame;
                                if (!meaning.target.equalsIgnoreCase("SELF")) {
                                    source = wim.frame(mapping.anchorForVariable(meaning.target));
                                }
                                
                                WIMFrame destination = wim.frame(mapping.anchorForVariable(meaning.wim));
                                destination.setSense(mapping.senses.get(mapping.anchorForVariable(meaning.wim)));
                                
                                WIMRelation relation = new WIMRelation(meaning.relation, destination);
                                source.addRelation(relation);
                                destination.addInverse(relation);
                            }
                        }
                    }
                }
            }
            
            //Now clean up all of the frame instance numbers
            HashMap<String, Integer> instances = new HashMap();
            for (WIMFrame frame : wim.listFrames()) {
                Integer count = instances.get(frame.getConcept());
                if (count == null) {
                    instances.put(frame.getConcept(), new Integer(1));
                } else {
                    count++;
                    frame.setInstance(count);
                    instances.put(frame.getConcept(), count);
                }
            }
            
            wims.add(wim);
        }
        
        return wims;
    }
    
}
