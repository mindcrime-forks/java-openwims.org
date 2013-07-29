/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors;

import edu.stanford.nlp.pipeline.Annotation;
import java.util.LinkedList;
import org.openwims.Objects.Disambiguation.InterpretationSet;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Lexicon.Structure;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPSentence;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.Objects.Disambiguation.SenseGraph;
import org.openwims.Objects.Disambiguation.SenseMapping;
import org.openwims.Objects.WIM;
import org.openwims.Processors.WIMProcessor.WSDProcessor;
import org.openwims.Processors.WIMProcessor.WSEProcessor;
import org.openwims.Stanford.StanfordHelper;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesseenglish
 */
public class TieredGroupingDisambiguation extends WIMProcessor implements WSEProcessor, WSDProcessor {
    
    public static void main(String[] args) throws Exception {
        String testPath = "/Users/jesseenglish/Desktop/test.stn";
        
        Annotation a = StanfordHelper.load(testPath);
        System.out.println(a);
        
        TieredGroupingDisambiguation processor = new TieredGroupingDisambiguation();
        SenseGraph senseGraph = processor.wse(StanfordHelper.convert(a));
        LinkedList<WIM> wims = processor.wimify(senseGraph);
        
        for (WIM wim : wims) {
            System.out.println("==== WIM ====\n");
            System.out.println(wim);
            System.out.println("\n\n");
        }
        
        System.out.println(wims.size());
        
        System.out.println("DONE!");
    }
    
    //In WSE we eliminate all senses of a word that cannot possibly hold in the
    //context, and produce a SenseGraph of all remaining senses for the verbs
    //and their mappings (to particular senses)
    public SenseGraph wse(PPDocument document) {
        
        SenseGraph graph = new SenseGraph();
        
        //First we build a collection of possibly valid sense mappings for each verb
        for (PPSentence sentence : document.listSentences()) {
            for (PPToken verb : sentence.listTokens("V")) {
                for (Sense sense : WIMGlobals.lexicon().word(verb.lemma()).listSenses()) {
                    if (!WIMGlobals.tagmaps().doTagsMatch(verb.pos(), sense.pos())) {
                        continue;
                    }
                    
                    for (Structure structure : sense.listStructures()) {
                        LinkedList<SenseMapping> mappings = map(verb, sense, structure, sentence);
                        if (mappings != null) {
                            for (SenseMapping mapping : mappings) {
                                graph.map(verb, mapping);
                            }
                        }
//                        if (mapping != null) {
//                            graph.map(verb, mapping);
//                        }
                    }
                }
            }
        }
        
        //Second, we build interpretation sets from mappings that are possibly
        //valid entire sets for the text; each of these can be wimified
        for (PPSentence sentence : document.listSentences()) {
            LinkedList<PPToken> verbs = sentence.listTokens("V");
            InterpretationSet set = new InterpretationSet();
            buildInterpretation_RECURSIVE(verbs, set, graph);
        }
        
        return graph;
    }
    
    private void buildInterpretation_RECURSIVE(LinkedList<PPToken> remainingVerbs, InterpretationSet set, SenseGraph graph) {
        //Copy the list so as not to impact higher iterations of the recursion
        remainingVerbs = new LinkedList(remainingVerbs);
        
        //Continue to build on to the valid sets
        if (remainingVerbs.size() > 0) {
            PPToken first = remainingVerbs.removeFirst();
            if (graph.verbs.get(first) == null) {
                graph.interpretations.add(set);
                return;  //there was no mapping for the verb
            }
            
            for (SenseMapping mapping : graph.verbs.get(first)) {
                if (!set.doesMappingViolateSet(mapping)) {
                    InterpretationSet innerSet = new InterpretationSet(set);
                    innerSet.mappings.add(mapping);
                    buildInterpretation_RECURSIVE(remainingVerbs, innerSet, graph);
                }
            }
        } else {
            //Only valid sets will have made it this far, and the recursion is complete
            graph.interpretations.add(set);
        }
    }
    
    private LinkedList<SenseMapping> map(PPToken verb, Sense sense, Structure structure, PPSentence sentence) {
        
        SenseMapping mapping = new SenseMapping(verb, sense);
        
        //First, find matches for the dependency sets
        for (DependencySet dependencySet : structure.listDependencies()) {
            //Align as many dependencies in the set as possible
            for (Dependency dependency : dependencySet.dependencies) {
                for (PPDependency ppDependency : sentence.listDependencies()) {
                    if (doDependenciesMatch(verb, dependency, ppDependency, mapping)) {
                        mapping.mappings.put(dependency, ppDependency);
                    }
                }
            }
            //If the set was not fully matched, remove all partial matches
            if (!mapping.isDependencySetComplete(dependencySet)) {
                mapping.removeDependencySet(dependencySet);
                
                //If the set was non-optional, return null - this mapping cannot be completed
                if (!dependencySet.optional) {
                    return null;
                }
            }
        }
        
        LinkedList<SenseMapping> allMappings = buildMappings_RECURSIVE(new LinkedList(), mapping, new LinkedList(mapping.mappings.keySet()));
        
        return allMappings;
    }
    
    private LinkedList<SenseMapping> buildMappings_RECURSIVE(LinkedList<SenseMapping> mappings, SenseMapping rootMapping, LinkedList<Dependency> remainingDependencies) {
        if (remainingDependencies.size() > 0) {
            Dependency dependency = remainingDependencies.removeFirst();
            PPDependency ppDependency = rootMapping.mappings.get(dependency);
            
            LinkedList<Sense> matches = listSatisfyingSenses(ppDependency.getDependent(), dependency.expectations);
            LinkedList<SenseMapping> newMappings = new LinkedList();
            for (Sense sense : matches) {
                if (mappings.size() == 0) {
                    SenseMapping copy = new SenseMapping(rootMapping);
                    copy.senses.put(ppDependency.getDependent(), sense);
                    newMappings.add(copy);
                } else {
                    for (SenseMapping mapping : mappings) {
                        SenseMapping copy = new SenseMapping(mapping);
                        copy.senses.put(ppDependency.getDependent(), sense);
                        newMappings.add(copy);
                    }
                }
            }
            mappings.clear();
            mappings.addAll(buildMappings_RECURSIVE(newMappings, rootMapping, remainingDependencies));
        }
        return mappings;
    }
    
    //HACK: for now, just chooseo the first!
    public SenseGraph wsd(SenseGraph senseGraph) {
        InterpretationSet first = senseGraph.interpretations.getFirst();
        senseGraph.interpretations.clear();
        senseGraph.interpretations.add(first);
        return senseGraph;
    }
    
}
