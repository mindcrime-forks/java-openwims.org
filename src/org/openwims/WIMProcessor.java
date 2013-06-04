/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.Lexicon;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Lexicon.Meaning;
import org.openwims.Objects.Lexicon.Structure;
import org.openwims.Objects.Lexicon.Token;
import org.openwims.Objects.WIMAttribute;
import org.openwims.Objects.WIMFrame;
import org.openwims.Objects.WIMRelation;

/**
 *
 * @author jesse
 */
public class WIMProcessor {
    
    private static HashMap<String, LinkedList<String>> tagmaps = null;
    
    public static HashMap<String, LinkedList<String>> tagmaps() {
        if (WIMProcessor.tagmaps == null) {
            WIMProcessor.tagmaps = new HashMap();
            
            InputStream in = WIMProcessor.class.getResourceAsStream("/org/openwims/Assets/tagmaps");
            BufferedReader input = new BufferedReader(new InputStreamReader(in));
            
            String line = null;
            LinkedList<String> map = null;
            
            try {
                while ((line = input.readLine()) != null) {
                    if (line.trim().equalsIgnoreCase("")) {
                        map = null;
                        continue;
                    }
                    
                    String tag = line.split("-")[0].trim();
                    
                    if (map == null) {
                        map = new LinkedList();
                        WIMProcessor.tagmaps.put(tag, map);
                    }
                    
                    map.add(tag);
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
        
        return WIMProcessor.tagmaps;
    }
    
    public static LinkedList<WIMFrame> WIMify(Annotation document) {
        LinkedList<WIMFrame> frames = new LinkedList();
        
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            frames.addAll(WIMProcessor.WIMify(tokens, graph));
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
    
    private static LinkedList<WIMFrame> WIMify(List<CoreLabel> tokens, SemanticGraph graph) { 
        //1: initialize all of the WIM frames, anchored to the token
        HashMap<String, WIMFrame> framesByAnchor = new HashMap();
        HashMap<CoreLabel, WIMFrame> framesByCoreLabel = new HashMap();
        HashMap<WIMFrame, StructureMatch> alignments = new HashMap();
        
        for (CoreLabel token : tokens) {
            WIMFrame frame = new WIMFrame(token.toString());
            framesByAnchor.put(token.toString(), frame);
            framesByCoreLabel.put(token, frame);           
        }
        
        //2: disambiguate each WIM frame
        for (CoreLabel token : tokens) {
            WIMFrame frame = framesByCoreLabel.get(token);
            Token t = WIMGlobals.lexicon().token(token.lemma());
            
            Sense selected = null;
            StructureMatch best = null;
            
            //assumption for now: list of senses is in "best" order
            for (Sense sense : t.listSenses()) {
                StructureMatch match = match(graph, frame.getAnchor(), sense);

                if (best == null && match != null) {
                    best = match;
                    selected = sense;
                } else if (best != null && match != null) {
                    if (match.structure.listDependencies().size() > best.structure.listDependencies().size()) {
                        best = match;
                        selected = sense;
                    }
                }
            }
            
            frame.setSense(selected);
            alignments.put(frame, best);
            
        }
        
        //3: align dependency variables with frames and fill in relations
        for (WIMFrame frame : framesByAnchor.values()) {
            HashMap<String, WIMFrame> variables = new HashMap();
             
            //a: align variables
            StructureMatch alignment = alignments.get(frame);
            if (alignment == null) {
                continue;
            }
            
            for (Dependency dependency : alignment.alignments.keySet()) {
                if (dependency == null) {
                    continue;
                }
                
                SemanticGraphEdge stan = alignment.alignments.get(dependency);
                if (stan == null) { //sanity check; shouldn't be possible
                    continue;
                }
                
                variables.put(dependency.dependent, framesByAnchor.get(anchorNameForIndexWord(stan.getDependent())));
            }
            
            
            //b: fill in meaning
            for (Meaning meaning : frame.getSense().listMeanings()) {
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
        }
        
        //4: prune frames with no links
        HashSet<WIMFrame> output = new HashSet();
        for (WIMFrame frame : framesByAnchor.values()) {
            LinkedList<WIMRelation> relations = frame.listRelations();
            if (relations.size() > 0) {
                output.add(frame);
            }
            
            for (WIMRelation relation : relations) {
                output.add(relation.getFrame());
            }
        }
        
        return new LinkedList(output);
    }
    
    //return the "best" match for this sense
    // 1) a complete match
    // 2) highest structural complexity (read: count of deps) wins
    // 3) tie: take one closest to top (assume: ranked by frequency of occurrence)
    private static StructureMatch match(SemanticGraph graph, String anchor, Sense sense) {
        
        //Make all of the structures potential matches
        LinkedList<StructureMatch> matchingStructures = new LinkedList();
        for (Structure structure : sense.listStructures()) {
            matchingStructures.add(new StructureMatch(structure));
        }
        
        //Go through each structure and kick out those that fail to match
        for (StructureMatch structureMatch : matchingStructures) {
            
            //Check each dependency for a match
            for (Dependency dependency : structureMatch.structure.listDependencies()) {
                
                STANDEPLOOP:
                for (SemanticGraphEdge edge : graph.getEdgeSet()) {
                    if (doDependenciesMatch(anchor, dependency, edge, structureMatch)) {
                        structureMatch.alignments.put(dependency, edge);
                        break STANDEPLOOP;
                    }
                }
            }
        }
        
        //Now find the structure with the most dependencies and return it
        // (assume the list is ranked)
        StructureMatch match = null;
        for (StructureMatch structure : matchingStructures) {
            if (!structure.isComplete()) {
                continue;
            }
            
            if (match == null) {
                match = structure;
            } else if (structure.structure.listDependencies().size() > match.structure.listDependencies().size()) {
                match = structure;
            }
        }
        
        return match;
    }
    
    private static boolean doDependenciesMatch(String anchor, Dependency wimDep, SemanticGraphEdge edge, StructureMatch structureMatch) {
        
        //Check to see if the dependency type matches
        if (!wimDep.type.equalsIgnoreCase(edge.getRelation().getShortName())) {
            return false;
        }
        
        String governorAnchor = anchorNameForIndexWord(edge.getGovernor());
        String dependentAnchor = anchorNameForIndexWord(edge.getDependent());
                
        //Check to see that the governor matches any existing variable mappings
        if (wimDep.governor.equalsIgnoreCase("SELF")) {
            if (!anchor.equals(governorAnchor)) {
                return false;
            }
        } else if (structureMatch.anchorForVariable(wimDep.governor) != null &&
                   !structureMatch.anchorForVariable(wimDep.governor).equals(governorAnchor)) {
            return false;
        }
        
        //Check to see that the dependent matches any existing variable mappings and all expectations
        if (wimDep.dependent.equalsIgnoreCase("SELF")) {
            if (!anchor.equals(dependentAnchor)) {
                return false;
            }
        } else if (structureMatch.anchorForVariable(wimDep.dependent) != null &&
                   !structureMatch.anchorForVariable(wimDep.dependent).equals(dependentAnchor)) {
            return false;
        } else {
            for (String specification : wimDep.expectations.keySet()) {
                if (specification.equalsIgnoreCase("pos") && 
                    !doTagsMatch(wimDep.expectations.get(specification), edge.getDependent().tag())) {
                    return false;
                } else if (specification.equalsIgnoreCase("token") &&
                           !(wimDep.expectations.get(specification).equalsIgnoreCase(edge.getDependent().lemma()) ||
                             wimDep.expectations.get(specification).equalsIgnoreCase(edge.getDependent().originalText()))) {
                    return false;
                } else if (specification.equalsIgnoreCase("ont")) {
                    Token t = WIMGlobals.lexicon().token(edge.getDependent().lemma());
                    if (!t.canBeConcept(wimDep.expectations.get(specification))) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private static String anchorNameForIndexWord(IndexedWord word) {
        return word.originalText() + "-" + word.index();
    }
    
    private static boolean doTagsMatch(String tag1, String tag2) {
        if (tag1.equalsIgnoreCase(tag2)) {
            return true;
        }
        
        if (WIMProcessor.tagmaps().containsKey(tag1) && WIMProcessor.tagmaps().get(tag1).contains(tag2)) {
            return true;
        }
        
        if (WIMProcessor.tagmaps().containsKey(tag2) && WIMProcessor.tagmaps().get(tag2).contains(tag1)) {
            return true;
        }
        
        return false;
    }
    
    private static class StructureMatch {
        
        public Structure structure;
        public HashMap<Dependency, SemanticGraphEdge> alignments;
        
        public StructureMatch(Structure structure) {
            this.structure = structure;
            this.alignments = new HashMap();
            
            for (Dependency dependency : structure.listDependencies()) {
                this.alignments.put(dependency, null);
            }
        }
        
        public String anchorForVariable(String variable) {
            for (Dependency dependency : this.alignments.keySet()) {
                SemanticGraphEdge edge = this.alignments.get(dependency);
                if (edge == null) {
                    continue;
                }
                
                if (dependency.governor.equals(variable)) {
                    return anchorNameForIndexWord(edge.getGovernor());
                }
                
                if (dependency.dependent.equals(variable)) {
                    return anchorNameForIndexWord(edge.getDependent());
                }
            }
            
            return null;
        }
        
        public boolean isComplete() {
            return !this.alignments.values().contains(null);
        }
        
    }
    
}
