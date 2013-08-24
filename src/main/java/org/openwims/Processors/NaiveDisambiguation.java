///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.openwims.Processors;
//
//import edu.stanford.nlp.pipeline.Annotation;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import org.openwims.Objects.Lexicon.*;
//import org.openwims.Objects.Preprocessor.PPDependency;
//import org.openwims.Objects.Preprocessor.PPDocument;
//import org.openwims.Objects.Preprocessor.PPSentence;
//import org.openwims.Objects.Preprocessor.PPToken;
//import org.openwims.Objects.WIMAttribute;
//import org.openwims.Objects.WIMFrame;
//import org.openwims.Objects.WIMRelation;
//import org.openwims.Stanford.StanfordHelper;
//import org.openwims.WIMGlobals;
//
///**
// * @deprecated 
// * @todo REMOVE
// * @author jesse
// */
//public class NaiveDisambiguation {
//        
//    public static void main(String[] args) throws Exception {
//        String testPath = "/Users/jesseenglish/Desktop/test.stn";
//        
//        Annotation a = StanfordHelper.load(testPath);
//        LinkedList<WIMFrame> frames = NaiveDisambiguation.WIMify(StanfordHelper.convert(a));
//
//        System.out.println(a);
//        for (WIMFrame frame : frames) {
//            System.out.println(frame);
//        }
//    }
//    
//    public static LinkedList<WIMFrame> WIMify(PPDocument document) {
//        LinkedList<WIMFrame> frames = new LinkedList();
//        
//        for (PPSentence sentence : document.listSentences()) {
//            frames.addAll(NaiveDisambiguation.WIMify(sentence));
//        }
//        
//        //Now clean up all of the frame instance numbers
//        HashMap<String, Integer> instances = new HashMap();
//        for (WIMFrame frame : frames) {
//            Integer count = instances.get(frame.getConcept());
//            if (count == null) {
//                instances.put(frame.getConcept(), new Integer(1));
//            } else {
//                count++;
//                frame.setInstance(count);
//                instances.put(frame.getConcept(), count);
//            }
//        }
//                
//        return frames;
//    }
//    
//    private static LinkedList<WIMFrame> WIMify(PPSentence sentence) { 
//        //1: initialize all of the WIM frames, anchored to the token
//        HashMap<PPToken, WIMFrame> framesByToken = new HashMap();
//        HashMap<WIMFrame, NaiveDisambiguation.StructureMatch> alignments = new HashMap();
//        
//        for (PPToken token : sentence.listTokens()) {
//            WIMFrame frame = new WIMFrame(token);
//            framesByToken.put(token, frame);
//        }
//        
//        //1.5: we rearrange all of the tokens so that we are processing in the
//        //order of V, N + R, J
//        String[] order = new String[] { "V", "N", "R", "J" };
//        LinkedList<PPToken> orderedTokens = new LinkedList();
//        
//        for (String pos : order) {
//            for (PPToken token : sentence.listTokens()) {
//                if (token.rootPOS().equalsIgnoreCase(pos)) {
//                    orderedTokens.add(token);
//                }
//            }
//        }
//        
//        //2: disambiguate each WIM frame
//        for (PPToken token : sentence.listTokens()) {
//            WIMFrame frame = framesByToken.get(token);
//            Word w = WIMGlobals.lexicon().word(token.lemma());
//            
//            Sense selected = null;
//            NaiveDisambiguation.StructureMatch best = null;
//            
//            //assumption for now: list of senses is in "best" order
//            for (Sense sense : w.listSenses()) {
//                if (!WIMGlobals.tagmaps().doTagsMatch(token.pos(), sense.pos())) {
//                    continue;
//                }
//                
//                NaiveDisambiguation.StructureMatch match = match(sentence, frame.getAnchor(), sense);
//
//                if (best == null && match != null) {
//                    best = match;
//                    selected = sense;
//                } else if (best != null && match != null) {
//                    if (match.structure.listDependencies().size() > best.structure.listDependencies().size()) {
//                        best = match;
//                        selected = sense;
//                    }
//                }
//            }
//            
//            frame.setSense(selected);
//            alignments.put(frame, best);
//            
//        }
//        
//        //3: align dependency variables with frames and fill in relations
//        for (WIMFrame frame : framesByToken.values()) {
//            
//            NaiveDisambiguation.StructureMatch alignment = alignments.get(frame);
//            if (alignment == null) {
//                continue;
//            }
//            
//            for (DependencySet dependencySet : alignment.structure.listDependencies()) {
//                if (!alignment.isDependencySetComplete(dependencySet)) {
//                    continue;
//                }
//                
//                HashMap<String, WIMFrame> variables = new HashMap();
//             
//                //a: align variables
//                for (Dependency dependency : dependencySet.dependencies) {
//                    if (dependency == null) {
//                        continue;
//                    }
//
//                    PPDependency ppDep = alignment.alignments.get(dependency);
//                    if (ppDep == null) { //sanity check; shouldn't be possible
//                        continue;
//                    }
//
//                    variables.put(dependency.dependent, framesByToken.get(ppDep.getDependent()));
//                }
//
//
//                //b: fill in meaning
//                for (Meaning meaning : dependencySet.meanings) {
//                    if (variables.get(meaning.wim) == null) {
//                        continue;
//                    }
//
//                    WIMFrame target = frame;
//                    if (!meaning.target.equalsIgnoreCase("SELF")) {
//                        target = variables.get(meaning.target);
//                    }
//
//                    if (meaning.isAttribute()) {
//                        WIMAttribute attribute = new WIMAttribute(meaning.relation, meaning.wim);
//                        target.addAttribute(attribute);
//                    } else {
//                        WIMRelation relation = new WIMRelation(meaning.relation, variables.get(meaning.wim));
//                        target.addRelation(relation);
//                        variables.get(meaning.wim).addInverse(relation);
//                    }
//                }
//                
//                //c: fill in generic meaning
//                for (Meaning meaning : frame.getSense().listMeanings()) {
//                    //currently assumed to ONLY be attributes on SELF
//                    WIMAttribute attribute = new WIMAttribute(meaning.relation, meaning.wim);
//                    frame.addAttribute(attribute);
//                }
//            }
//            
//        }
//        
//        //4: prune frames with no links
//        HashSet<WIMFrame> output = new HashSet();
//        for (WIMFrame frame : framesByToken.values()) {
//            LinkedList<WIMRelation> relations = frame.listRelations();
//            if (relations.size() > 0) {
//                output.add(frame);
//            }
//            
//            for (WIMRelation relation : relations) {
//                output.add(relation.getFrame());
//            }
//        }
//        
//        return new LinkedList(output);
//    }
//    
//    //return the "best" match for this sense
//    // 1) a complete match
//    // 2) highest structural complexity (read: count of deps) wins
//    // 3) tie: take one closest to top (assume: ranked by frequency of occurrence)
//    private static NaiveDisambiguation.StructureMatch match(PPSentence sentence, PPToken anchor, Sense sense) {
//                
//        //Make all of the structures potential matches
//        LinkedList<NaiveDisambiguation.StructureMatch> matchingStructures = new LinkedList();
//        for (Structure structure : sense.listStructures()) {
//            matchingStructures.add(new NaiveDisambiguation.StructureMatch(structure));
//        }
//        
//        
//        //Go through each structure and kick out those that fail to match
//        for (NaiveDisambiguation.StructureMatch structureMatch : matchingStructures) {
//            
//            
//            //Check each dependency for a match
//            for (DependencySet dependencySet : structureMatch.structure.listDependencies()) {
//                
//                for (Dependency dependency : dependencySet.dependencies) {
//                    
//                    PPDEPLOOP:
//                    for (PPDependency ppDependency : sentence.listDependencies()) {
//                        if (doDependenciesMatch(anchor, dependency, ppDependency, structureMatch)) {
//                            structureMatch.alignments.put(dependency, ppDependency);
//                            break PPDEPLOOP;
//                        }
//                    }
//                    
//                }
//                
//            }
//            
//        }
//                
//        //Now we find the best possible structure for this sense
//        NaiveDisambiguation.StructureMatch match = null;
//        STRUCTURELOOP:
//        for (NaiveDisambiguation.StructureMatch structure : matchingStructures) {
//            //Skip any structure that has a non-optional set that has no match
//            for (DependencySet set : structure.structure.listDependencies()) {
//                if (!set.optional && !structure.isDependencySetComplete(set)) {
//                    continue STRUCTURELOOP;
//                }
//            }
//            
//            if (match == null) {
//                match = structure;
//            } else if (structure.structure.listDependencies().size() > match.structure.listDependencies().size()) {
//                match = structure;
//            }
//        }
//        
//        return match;
//    }
//    
//    private static boolean doDependenciesMatch(PPToken anchor, Dependency wimDep, PPDependency ppDep, NaiveDisambiguation.StructureMatch structureMatch) {
//        
//        
//        //Check to see if the dependency type matches
//        if (!wimDep.type.equalsIgnoreCase(ppDep.getType())) {
//            return false;
//        }
//                
//        //Check to see that the governor matches any existing variable mappings
//        if (wimDep.governor.equalsIgnoreCase("SELF")) {
//            if (anchor != ppDep.getGovernor()) {
//                return false;
//            }
//        } else if (structureMatch.anchorForVariable(wimDep.governor) != null &&
//                   structureMatch.anchorForVariable(wimDep.governor) != ppDep.getGovernor()) {
//            return false;
//        }
//        
//        //Check to see that the dependent matches any existing variable mappings and all expectations
//        if (wimDep.dependent.equalsIgnoreCase("SELF")) {
//            if (anchor != ppDep.getDependent()) {
//                return false;
//            }
//        } else if (structureMatch.anchorForVariable(wimDep.dependent) != null &&
//                   structureMatch.anchorForVariable(wimDep.dependent) != ppDep.getDependent()) {
//            return false;
//        } else {
//            
//            for (Expectation expectation : wimDep.expectations) {
//                if (expectation.getSpecification().equalsIgnoreCase("pos") && 
//                    !WIMGlobals.tagmaps().doTagsMatch(expectation.getExpectation(), ppDep.getDependent().pos())) {
//                    return false;
//                } else if (expectation.getSpecification().equalsIgnoreCase("token") &&
//                           !(expectation.getExpectation().equalsIgnoreCase(ppDep.getDependent().lemma()) ||
//                             expectation.getExpectation().equalsIgnoreCase(ppDep.getDependent().text()))) {
//                    return false;
//                } else if (expectation.getSpecification().equalsIgnoreCase("ont")) {
//                    Word w = WIMGlobals.lexicon().word(ppDep.getDependent().lemma());
//                    if (!w.canBeConcept(expectation.getExpectation())) {
//                        return false;
//                    }
//                } else if (expectation.getSpecification().equalsIgnoreCase("micro")) {
//                    System.out.println("WARNING: testing for microtheories (" + expectation.getExpectation() + ") is not yet implemented!");
//                }
//            }
//            
////            for (String specification : wimDep.expectations.keySet()) {
////                if (specification.equalsIgnoreCase("pos") && 
////                    !WIMGlobals.tagmaps().doTagsMatch(wimDep.expectations.get(specification), ppDep.getDependent().pos())) {
////                    return false;
////                } else if (specification.equalsIgnoreCase("token") &&
////                           !(wimDep.expectations.get(specification).equalsIgnoreCase(ppDep.getDependent().lemma()) ||
////                             wimDep.expectations.get(specification).equalsIgnoreCase(ppDep.getDependent().text()))) {
////                    return false;
////                } else if (specification.equalsIgnoreCase("ont")) {
////                    Word w = WIMGlobals.lexicon().word(ppDep.getDependent().lemma());
////                    if (!w.canBeConcept(wimDep.expectations.get(specification))) {
////                        return false;
////                    }
////                } else if (specification.equalsIgnoreCase("micro")) {
////                    System.out.println("WARNING: testing for microtheories (" + wimDep.expectations.get(specification) + ") is not yet implemented!");
////                }
////            }
//            
//        }
//        
//        
//        return true;
//    }
//    
//    private static class StructureMatch {
//        
//        public Structure structure;
//        public HashMap<Dependency, DependencySet> sets;
//        public HashMap<Dependency, PPDependency> alignments;
//        
//        public StructureMatch(Structure structure) {
//            this.structure = structure;
//            this.alignments = new HashMap();
//            this.sets = new HashMap();
//            
//            for (DependencySet dependencySet : structure.listDependencies()) {
//                for (Dependency dependency : dependencySet.dependencies) {
//                    this.alignments.put(dependency, null);
//                    this.sets.put(dependency, dependencySet);
//                }
//            }
//        }
//        
//        public PPToken anchorForVariable(String variable) {
//            for (Dependency dependency : this.alignments.keySet()) {
//                PPDependency ppDependency = this.alignments.get(dependency);
//                if (ppDependency == null) {
//                    continue;
//                }
//                
//                if (dependency.governor.equals(variable)) {
//                    return ppDependency.getGovernor();
//                }
//                
//                if (dependency.dependent.equals(variable)) {
//                    return ppDependency.getDependent();
//                }
//            }
//            
//            return null;
//        }
//        
//        public boolean isDependencySetComplete(DependencySet set) {
//            for (Dependency dependency : set.dependencies) {
//                if (this.alignments.get(dependency) == null) {
//                    return false;
//                }
//            }
//            
//            return true;
//        }
//        
//    }
//    
//}
