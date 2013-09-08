/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.ArraySet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
import org.openwims.Objects.Preprocessor.PPMention;
import org.openwims.Objects.WIM;
import org.openwims.Processors.WIMProcessor.WSDProcessor;
import org.openwims.Processors.WIMProcessor.WSEProcessor;
import org.openwims.Stanford.StanfordHelper;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesseenglish
 */
public class TieredGroupingDisambiguationRefHack extends WIMProcessor implements WSEProcessor, WSDProcessor {

    public static void main(String[] args) throws Exception {
        String testPath = "/Users/jesseenglish/Desktop/test.stn";

        Annotation a = StanfordHelper.load(testPath);
        System.out.println(a);

        TieredGroupingDisambiguationRefHack processor = new TieredGroupingDisambiguationRefHack();
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

        document.transform();
        
        SenseGraph graph = new SenseGraph();

        //THIS IS WHERE THE HACK LIVES!
        PPSentence documentSentence = new PPSentence();
        for (PPSentence sentence : document.listSentences()) {
            for (PPToken token : sentence.listTokens()) {
                for (PPMention mention : token.getMentions()) {
                    mention.setSentence(documentSentence);
                }
                documentSentence.addToken(token);
            }
            for (PPDependency dep : sentence.listDependencies()) {
                documentSentence.addDependency(dep);
            }
        }

        //First we build a collection of possibly valid sense mappings for each verb

        for (PPToken verb : documentSentence.listTokens()) {
            for (PPMention mention : verb.listMentions("V", documentSentence)) {
                for (Sense sense : WIMGlobals.lexicon().getSenses(mention)) {
                    if (!WIMGlobals.tagmaps().doTagsMatch(mention.pos(), sense.pos())) {
                        continue;
                    }

                    for (Structure structure : sense.listStructures()) {
                        LinkedList<SenseMapping> mappings = map(verb, sense, structure, documentSentence);
                        if (mappings != null) {
                            for (SenseMapping mapping : mappings) {
                                graph.map(verb, mapping);
                            }
                        }
                    }
                }
            }
        }

        //Do the noun trimming here
        //for (PPSentence sentence : document.listSentences()) {
        for (PPToken noun : documentSentence.listTokens()) {
            for (PPMention mention : noun.listMentions("N", documentSentence)) {
                HashMap<Sense, Integer> nounSenseCounts = new HashMap();

                for (Sense sense : WIMGlobals.lexicon().getSenses(mention)) {
                    if (!WIMGlobals.tagmaps().doTagsMatch(mention.pos(), sense.pos())) {
                        continue;
                    }

                    nounSenseCounts.put(sense, 0);

                    for (PPToken verb : graph.verbs.keySet()) {
                        VERBLOOP:
                        for (SenseMapping senseMapping : graph.verbs.get(verb)) {
                            if (senseMapping.senses.get(noun) == sense) {
                                nounSenseCounts.put(sense, nounSenseCounts.get(sense) + 1);
                                break VERBLOOP;
                            }
                        }
                    }
                }

                int max = 0;
                LinkedList<Sense> bestSenses = new LinkedList();
                for (Sense sense : nounSenseCounts.keySet()) {
                    if (nounSenseCounts.get(sense) > max) {
                        max = nounSenseCounts.get(sense);
                        bestSenses.clear();
                        bestSenses.add(sense);
                    } else if (nounSenseCounts.get(sense) == max) {
                        bestSenses.add(sense);
                    }
                }

                for (PPToken verb : graph.verbs.keySet()) {
                    LinkedList<SenseMapping> toRemove = new LinkedList();
                    for (SenseMapping senseMapping : graph.verbs.get(verb)) {
                        if (senseMapping.senses.get(noun) != null && !bestSenses.contains(senseMapping.senses.get(noun))) {
                            //toRemove.add(senseMapping);
                            //was too aggressive
                        }
                    }

                    for (SenseMapping senseMapping : toRemove) {
                        graph.verbs.get(verb).remove(senseMapping);
                    }
                }
            }
        }
        //}

        //Second, we build interpretation sets from mappings that are possibly
        //valid entire sets for the text; each of these can be wimified

        ArraySet<PPToken> verbs = new ArraySet<PPToken>();
        for (PPToken verb : documentSentence.listTokens()) {
            if (verb.listMentions("V", documentSentence).size() > 0) {
                verbs.add(verb);
            }
        }
        InterpretationSet set = new InterpretationSet();
        buildInterpretation_GREEDY(new LinkedList(verbs), set, graph, documentSentence);


        //Trim all interpretations sets that don't cover at least all N and V


//        LinkedList<PPToken> nounsAndVerbs = new LinkedList();
//        for (PPToken token : documentSentence.listTokens()) {
//            if (token.listMentions("V", documentSentence).size() > 0 || token.listMentions("N", documentSentence).size() > 0) {
//                if(!isTokenNonheadOfNN(token, documentSentence)){ //stop gap until we have robust NN processing - ignore nouns that are not heads of NNs
//                    nounsAndVerbs.add(token);
//                }
//            }
//        }
//
//        for (Iterator<InterpretationSet> it = graph.interpretations.iterator(); it.hasNext();) {
//            InterpretationSet interpretationSet = it.next();
//            if (interpretationSet.isForSentence(documentSentence) && !interpretationSet.doesMappingCoverTokens(nounsAndVerbs)) {
//                it.remove();
//            }
//        }


        return graph;
    }
    
    private boolean doesProvideCoverage(PPSentence documentSentence, SenseGraph graph){
        
        LinkedList<PPToken> nounsAndVerbs = new LinkedList();
        for (PPToken token : documentSentence.listTokens()) {
            if (token.listMentions("V", documentSentence).size() > 0 || token.listMentions("N", documentSentence).size() > 0) {
                if(!isTokenNonheadOfNN(token, documentSentence)){ //stop gap until we have robust NN processing - ignore nouns that are not heads of NNs
                    nounsAndVerbs.add(token);
                }
            }
        }

        for (Iterator<InterpretationSet> it = graph.interpretations.iterator(); it.hasNext();) {
            InterpretationSet interpretationSet = it.next();
            if (interpretationSet.isForSentence(documentSentence) && !interpretationSet.doesMappingCoverTokens(nounsAndVerbs)) {
                it.remove();
            }
        }
        if(graph.interpretations.size() > 0){
            return true;
        } else {
            return false;
        }
    }
    
    private boolean buildInterpretation_GREEDY(LinkedList<PPToken> remainingVerbs, InterpretationSet set, SenseGraph graph, PPSentence sentence) {
        //Copy the list so as not to impact higher iterations of the recursion
        remainingVerbs = new LinkedList(remainingVerbs);

        //Continue to build on to the valid sets
        if (remainingVerbs.size() > 0) {
            PPToken first = remainingVerbs.removeFirst();
            if (graph.verbs.get(first) == null) {
                graph.interpretations.add(set);
                return false;  //there was no mapping for the verb
            }

            for (SenseMapping mapping : graph.verbs.get(first)) {
                if (!set.doesMappingViolateSet(mapping)) {
                    InterpretationSet innerSet = new InterpretationSet(set);
                    innerSet.mappings.add(mapping);
                    if(buildInterpretation_GREEDY(remainingVerbs, innerSet, graph, sentence)){
                        return true;
                    }
                }
            }
        } else {
            //Only valid sets will have made it this far, and the recursion is complete
            graph.interpretations.add(set);
            if(doesProvideCoverage(sentence, graph)){
                return true;
            } else {
                return false;
            }
        }
        return false;
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
            Collections.sort(remainingDependencies, new TieredGroupingDisambiguationRefHack.PreferMappedGovernorsComparator(mappings));

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

            if (newMappings.size() > 0) {
                mappings.clear();
            } else {
                newMappings = new LinkedList(mappings);
            }

            mappings.addAll(buildMappings_RECURSIVE(newMappings, rootMapping, new LinkedList(remainingDependencies)));
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

    private boolean isTokenNonheadOfNN(PPToken token, PPSentence documentSentence) {
        for (PPDependency dep : documentSentence.listDependencies()) {
            if(dep.getType().equalsIgnoreCase("NN") && dep.getDependent() == token){
                return true;
            }
        }
        return false;
    }

    private class PreferMappedGovernorsComparator implements Comparator<Dependency> {

        private LinkedList<SenseMapping> mappings;

        public PreferMappedGovernorsComparator(LinkedList<SenseMapping> mappings) {
            this.mappings = mappings;
        }

        public int compare(Dependency t, Dependency t1) {
            if (t.governor.equalsIgnoreCase("SELF")) {
                return -1;
            } else if (t1.governor.equalsIgnoreCase("SELF")) {
                return 1;
            } else if (mappings.size() > 0) {
                if (mappings.getFirst().anchorForVariable(t.governor) != null) {
                    return -1;
                } else if (mappings.getFirst().anchorForVariable(t1.governor) != null) {
                    return 1;
                }
            }

            return 0;
        }
    }
}
