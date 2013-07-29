/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors;

import edu.stanford.nlp.pipeline.Annotation;
import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Expectation;
import org.openwims.Objects.Lexicon.Meaning;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Lexicon.Structure;
import org.openwims.Objects.Lexicon.Word;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPSentence;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.Objects.WIMAttribute;
import org.openwims.Objects.WIMFrame;
import org.openwims.Objects.WIMRelation;
import org.openwims.Stanford.StanfordHelper;
import org.openwims.WIMGlobals;

/**
 * @deprecated 
 * @todo REMOVE
 * @author jesseenglish
 */
public class TieredDisambiguation {
    
    public static void main(String[] args) throws Exception {
        String testPath = "/Users/jesseenglish/Desktop/test.stn";
        
        Annotation a = StanfordHelper.load(testPath);
        LinkedList<WIMFrame> frames = TieredDisambiguation.WIMify(StanfordHelper.convert(a));

        System.out.println(a);
        for (WIMFrame frame : frames) {
            System.out.println(frame);
        }
    }
    
    public static LinkedList<WIMFrame> WIMify(PPDocument document) {
        LinkedList<WIMFrame> frames = new LinkedList();
        
        for (PPSentence sentence : document.listSentences()) {
            frames.addAll(TieredDisambiguation.WIMify(sentence));
        }
        
        //Now clean up all of the frame instance numbers
        HashMap<String, Integer> instances = new HashMap();
        for (WIMFrame frame : frames) {
            Integer count = instances.get(frame.getConcept());
            if (count == null) {
                instances.put(frame.getConcept(), new Integer(1));
            } else {
                count++;
                frame.setInstance(count);
                instances.put(frame.getConcept(), count);
            }
        }
                
        return frames;
    }
    
    public static LinkedList<WIMFrame> WIMify(PPSentence sentence) {
        LinkedList<WIMFrame> frames = new LinkedList();
        
        //These are for the first pass (WSE)
        HashMap<PPToken, LinkedList<Sense>> candidates = new HashMap();
        HashMap<PPToken, LinkedList<StructureMatch>> mappings = new HashMap();
        
        //These are for the second pass (WSD)
        HashMap<PPToken, Sense> selected = new HashMap();
        HashMap<PPToken, StructureMatch> selectedMapping = new HashMap();
        
        //These are for the WIM creation step
        HashMap<PPToken, WIMFrame> framesByToken = new HashMap();
        
        //Load the list of candidates from the lexicon and initialize
        //the list of potential mappings
        for (PPToken token : sentence.listTokens()) {
            candidates.put(token, WIMGlobals.lexicon().word(token.lemma()).listSenses());
            mappings.put(token, new LinkedList());
            framesByToken.put(token, new WIMFrame(token));
        }
        
        //Create the tiers
        LinkedList<PPToken>[] tiers = new LinkedList[] {
            sentence.listTokens("V"),
            sentence.listTokens("N"),
            sentence.listTokens("R"),
            sentence.listTokens("J")
        };
        
        //WSE
        //Process each tier, filtering out invalid senses and recording
        //any valid mappings
        for (LinkedList<PPToken> tier : tiers) {
            for (PPToken token : tier) {
                LinkedList<Sense> toRemove = new LinkedList();

                //Filter senses that CANNOT be a match
                for (Sense sense : candidates.get(token)) {
                    //First, remove any that are not the same POS
                    if (!WIMGlobals.tagmaps().doTagsMatch(token.pos(), sense.pos())) {
                        toRemove.add(sense);
                        continue;
                    }

                    //Look for all mappings; if there are none, this sense is
                    //invalid; if there is at least one, record them all
                    LinkedList<StructureMatch> matches = match(sentence.listDependencies(), token, sense);
                    if (matches.size() == 0) {
                        toRemove.add(sense);
                    } else {
                        mappings.get(token).addAll(matches);
                    }
                }

                candidates.get(token).removeAll(toRemove);
            }
        }
        
        //WSD
        //Now go back through each tier and remove all but (at most) one meaning
        //As a selection is made, any connected tokens that CAN'T work with a selection
        //must be filtered immediately
        for (LinkedList<PPToken> tier : tiers) {
            for (PPToken token : tier) {
                //For now we just take the first sense/mapping; this could be more interesting later
                if (candidates.get(token).size() == 0) {
                    selected.put(token, null);
                    selectedMapping.put(token, null);
                    continue;
                }
                
                Sense sense = candidates.get(token).getFirst();
                selected.put(token, sense);
                
                StructureMatch mapping = null;
                for (StructureMatch structureMatch : mappings.get(token)) {
                    if (sense.listStructures().contains(structureMatch.structure)) {
                        mapping = structureMatch;
                        break;
                    }
                }
                selectedMapping.put(token, mapping);
                
                //Clear out any senses that are connected to the selected mapping that do not
                //satisfy the constraints
                for (Dependency dependency : mapping.alignments.keySet()) {
                    if (mapping.alignments.get(dependency) == null) {
                        continue;
                    }
                    
                    PPToken dependent = mapping.alignments.get(dependency).getDependent();
                    LinkedList<Sense> toRemove = new LinkedList();
                    for (Sense candidate : candidates.get(dependent)) {
                        if (!doesSenseSatisfyDependent(candidate, dependency.expectations)) {
                            toRemove.add(candidate);
                        }
                    }
                    candidates.get(dependent).removeAll(toRemove);
                }
                
            }
        }
        
        //CONVERT
        //We now have our chosen mappings; time to conver to WIM frames
        frames = convert(selected, selectedMapping, framesByToken);
        
        return frames;
    }
    
    
    private static LinkedList<StructureMatch> match(LinkedList<PPDependency> dependencies, PPToken anchor, Sense sense) {        
        //Make all of the structures potential matches
        LinkedList<StructureMatch> matchingStructures = new LinkedList();
        for (Structure structure : sense.listStructures()) {
            matchingStructures.add(new StructureMatch(structure));
        }
        
        LinkedList<StructureMatch> toRemove = new LinkedList();
        
        //Go through each structure and kick out those that fail to match
        for (StructureMatch structureMatch : matchingStructures) {
            
            //Check each dependency for a match
            DEPSETLOOP:
            for (DependencySet dependencySet : structureMatch.structure.listDependencies()) {
                
                for (Dependency dependency : dependencySet.dependencies) {
                    
                    PPDEPLOOP:
                    for (PPDependency ppDependency : dependencies) {
                        if (doDependenciesMatch(anchor, dependency, ppDependency, structureMatch)) {
                            structureMatch.alignments.put(dependency, ppDependency);
                            break PPDEPLOOP;
                        }
                    }
                    
                }
                
                //A non-optional dependency set without a full match invalidates
                //a structure - remove it!
                if (!structureMatch.isDependencySetComplete(dependencySet) && !dependencySet.optional) {
                    toRemove.add(structureMatch);
                    break DEPSETLOOP;
                }
                
            }
            
        }
        
        matchingStructures.removeAll(toRemove);
        
        return matchingStructures;
    }
    
    private static boolean doesSenseSatisfyDependent(Sense sense, LinkedList<Expectation> expectations) {
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
                System.out.println("WARNING: testing for microtheories (" + expectation.getExpectation() + ") is not yet implemented!");
            }
        }
        
        return true;
    }
    
    private static boolean doDependenciesMatch(PPToken anchor, Dependency wimDep, PPDependency ppDep, StructureMatch structureMatch) {
        
        
        //Check to see if the dependency type matches
        if (!wimDep.type.equalsIgnoreCase(ppDep.getType())) {
            return false;
        }
                
        //Check to see that the governor matches any existing variable mappings
        if (wimDep.governor.equalsIgnoreCase("SELF")) {
            if (anchor != ppDep.getGovernor()) {
                return false;
            }
        } else if (structureMatch.anchorForVariable(wimDep.governor) != null &&
                   structureMatch.anchorForVariable(wimDep.governor) != ppDep.getGovernor()) {
            return false;
        }
        
        //Check to see that the dependent matches any existing variable mappings and all expectations
        if (wimDep.dependent.equalsIgnoreCase("SELF")) {
            if (anchor != ppDep.getDependent()) {
                return false;
            }
        } else if (structureMatch.anchorForVariable(wimDep.dependent) != null &&
                   structureMatch.anchorForVariable(wimDep.dependent) != ppDep.getDependent()) {
            return false;
        } else {
            
            for (Expectation expectation : wimDep.expectations) {
                if (expectation.getSpecification().equalsIgnoreCase("pos") && 
                    !WIMGlobals.tagmaps().doTagsMatch(expectation.getExpectation(), ppDep.getDependent().pos())) {
                    return false;
                } else if (expectation.getSpecification().equalsIgnoreCase("token") &&
                           !(expectation.getExpectation().equalsIgnoreCase(ppDep.getDependent().lemma()) ||
                             expectation.getExpectation().equalsIgnoreCase(ppDep.getDependent().text()))) {
                    return false;
                } else if (expectation.getSpecification().equalsIgnoreCase("ont")) {
                    Word w = WIMGlobals.lexicon().word(ppDep.getDependent().lemma());
                    if (!w.canBeConcept(expectation.getExpectation())) {
                        return false;
                    }
                } else if (expectation.getSpecification().equalsIgnoreCase("micro")) {
                    System.out.println("WARNING: testing for microtheories (" + expectation.getExpectation() + ") is not yet implemented!");
                }
            }
            
        }
        
        
        return true;
    }
    
    private static LinkedList<WIMFrame> convert(HashMap<PPToken, Sense> selected, HashMap<PPToken, StructureMatch> selectedMapping, HashMap<PPToken, WIMFrame> framesByToken) {
        LinkedList<WIMFrame> frames = new LinkedList();
        
        for (PPToken token : selected.keySet()) {
            Sense sense = selected.get(token);
            StructureMatch mapping = selectedMapping.get(token);
            WIMFrame frame = framesByToken.get(token);
            
            frame.setSense(sense);
            
            //Don't generate frames if there is no mapping
            if (sense == null || mapping == null) {
                continue;
            }
            
            for (DependencySet dependencySet : mapping.structure.listDependencies()) {
                //Don't include partial dependency sets
                if (!mapping.isDependencySetComplete(dependencySet)) {
                    continue;
                }
                
                HashMap<String, WIMFrame> variables = new HashMap();
             
                //a: align variables
                for (Dependency dependency : dependencySet.dependencies) {
                    if (dependency == null) {
                        continue;
                    }

                    PPDependency ppDep = mapping.alignments.get(dependency);
                    if (ppDep == null) { //sanity check; shouldn't be possible
                        continue;
                    }

                    variables.put(dependency.dependent, framesByToken.get(ppDep.getDependent()));
                }


                //b: fill in meaning
                for (Meaning meaning : dependencySet.meanings) {
                    if (variables.get(meaning.wim) == null) {
                        continue;
                    }

                    WIMFrame target = frame;
                    if (!meaning.target.equalsIgnoreCase("SELF")) {
                        target = variables.get(meaning.target);
                    }

                    if (meaning.isAttribute()) {
                        WIMAttribute attribute = new WIMAttribute(meaning.relation, meaning.wim);
                        target.addAttribute(attribute);
                    } else {
                        WIMRelation relation = new WIMRelation(meaning.relation, variables.get(meaning.wim));
                        target.addRelation(relation);
                        variables.get(meaning.wim).addInverse(relation);
                    }
                }
                
                //c: fill in generic meaning
                for (Meaning meaning : frame.getSense().listMeanings()) {
                    //currently assumed to ONLY be attributes on SELF
                    WIMAttribute attribute = new WIMAttribute(meaning.relation, meaning.wim);
                    frame.addAttribute(attribute);
                }
            } 
           
            //If the frame has anything in it, add it to the output
//            if (frame.listRelations().size() > 0 || frame.listAttributes().size() > 0 || frame.listInverses().size() > 0) {
//                frames.add(frame);
//            }
            frames.add(frame);
        }
        
        return frames;
    }
    
    private static class StructureMatch {
        
        public Structure structure;
        public HashMap<Dependency, DependencySet> sets;
        public HashMap<Dependency, PPDependency> alignments;
        
        public StructureMatch(Structure structure) {
            this.structure = structure;
            this.alignments = new HashMap();
            this.sets = new HashMap();
            
            for (DependencySet dependencySet : structure.listDependencies()) {
                for (Dependency dependency : dependencySet.dependencies) {
                    this.alignments.put(dependency, null);
                    this.sets.put(dependency, dependencySet);
                }
            }
        }
        
        public PPToken anchorForVariable(String variable) {
            for (Dependency dependency : this.alignments.keySet()) {
                PPDependency ppDependency = this.alignments.get(dependency);
                if (ppDependency == null) {
                    continue;
                }
                
                if (dependency.governor.equals(variable)) {
                    return ppDependency.getGovernor();
                }
                
                if (dependency.dependent.equals(variable)) {
                    return ppDependency.getDependent();
                }
            }
            
            return null;
        }
        
        public boolean isDependencySetComplete(DependencySet set) {
            for (Dependency dependency : set.dependencies) {
                if (this.alignments.get(dependency) == null) {
                    return false;
                }
            }
            
            return true;
        }
        
    }
    
}
