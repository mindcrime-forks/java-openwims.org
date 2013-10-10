/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.openwims.Objects.Disambiguation.Interpretation;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPSentence;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.Processors.WIMProcessor.WSDProcessor;
import org.openwims.Processors.WIMProcessor.WSEProcessor;
import org.openwims.Serialization.JSONPPDocumentSerializer;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesseenglish
 */
public class LandGrabDisambiguation extends WIMProcessor implements WSEProcessor, WSDProcessor {
    
    private PPSentence sentence;
    private LinkedList<PPToken> flattenedTokens;
    
    public static void main(String[] args) throws Exception {
        
        PPDocument document = JSONPPDocumentSerializer.deserialize("/Users/jesseenglish/Desktop/test.pp");
        LandGrabDisambiguation d = new LandGrabDisambiguation();
        for (Interpretation interpretation : d.wse(document)) {
            System.out.println(interpretation.wim());
        }
    }

    public LandGrabDisambiguation() {}
    
    public Interpretation wsd(LinkedList<Interpretation> interpretations) {
        Collections.sort(interpretations);
        Collections.reverse(interpretations);
        return interpretations.getFirst();
    }
    
    public LinkedList<Interpretation> wse(PPDocument document) {
        //HACK, turn it all into one sentence for now
        this.sentence = new PPSentence();
        for (PPSentence s : document.listSentences()) {
            for (PPToken token : s.listTokens()) {
                this.sentence.addToken(token);
            }
            for (PPDependency ppDependency : s.listDependencies()) {
                this.sentence.addDependency(ppDependency);
            }
        }
        
        //flatten tokens
        this.flattenedTokens = this.sentence.listFlattenedTokens();
        
        LinkedList<Interpretation> interpretations = new LinkedList();
        
        PossibilityIterator iter = new PossibilityIterator(sentence);
        while (iter.hasNext()) {
            try {
                HashMap<PPToken, Sense> possibility = iter.next();
                HashMap<PPDependency, Dependency> mapping = landgrab(possibility);
                interpretations.add(new Interpretation(possibility, mapping));
            } catch (IncompleteMappingException e) {
                
            } catch (UnusedNonOptionalSetException e) {
                
            }
        }
        
        return interpretations;
    }
    
    private HashMap<PPDependency, Dependency> landgrab(HashMap<PPToken, Sense> possibility) throws IncompleteMappingException, UnusedNonOptionalSetException {
        HashMap<PPDependency, Dependency> claims = new HashMap();
        
        for (PPToken token : this.flattenedTokens) {
            claim(token, possibility, claims);
        }
        
        //Verify that all ppdeps have been mapped
        if (!claims.keySet().containsAll(this.sentence.listDependencies())) {
            throw new IncompleteMappingException();
        }
        
        //Verify that all senses have all non-optional sets used
        for (PPToken token : possibility.keySet()) {
            Sense sense = possibility.get(token);
            
            //HACK: ASSUME STRUCTURE 1 FOR NOW
            if (sense.listStructures().size() == 0) {
                continue; //nothing to claim; e.g., @none:the-det-1
            }
            for (DependencySet dependencySet : sense.listStructures().getFirst().listDependencies()) {
                if (!dependencySet.optional && !claims.values().containsAll(dependencySet.dependencies)) {
                    throw new UnusedNonOptionalSetException(token, sense, dependencySet);
                }
            }
        }
        
        return claims;
    }
    
    private void claim(PPToken token, HashMap<PPToken, Sense> possibility, HashMap<PPDependency, Dependency> claims) {
        Sense sense = possibility.get(token);
        //HACK: ASSUME STRUCTURE 1 FOR NOW
        if (sense.listStructures().size() == 0) {
            return; //nothing to claim; e.g., @none:the-det-1
        }
        for (DependencySet dependencySet : sense.listStructures().getFirst().listDependencies()) {
            HashMap<PPDependency, Dependency> consideredClaims = new HashMap();
            HashMap<String, PPToken> variables = new HashMap();
            variables.put("SELF", token);
            
            for (Dependency dependency : dependencySet.dependencies) {
                for (PPDependency ppDependency : sentence.listDependencies()) {
                    if (claims.containsKey(ppDependency)) {
                        continue;
                    }
                    if (doDependenciesMatch(dependency, ppDependency, possibility, variables)) {
                        consideredClaims.put(ppDependency, dependency);
                        variables.put(dependency.governor, ppDependency.getGovernor());
                        variables.put(dependency.dependent, ppDependency.getDependent());
                    }
                }
            }
            
            //Add to official claims only if the dependency set is fully satisfied
            if (consideredClaims.values().containsAll(dependencySet.dependencies)) {
                claims.putAll(consideredClaims);
            }
        }
    }
    
    private boolean doDependenciesMatch(Dependency dependency, PPDependency ppDependency, HashMap<PPToken, Sense> possibility, HashMap<String, PPToken> variables) {
        //Verify types match
        if (!dependency.type.equalsIgnoreCase(ppDependency.getType())) {
            return false;
        }
        
        //if either variable (gov/dep) is spoken for, then the tokens must match
        if (variables.containsKey(dependency.governor) && variables.get(dependency.governor) != ppDependency.getGovernor()) {
            return false;
        }
        if (variables.containsKey(dependency.dependent) && variables.get(dependency.dependent) != ppDependency.getDependent()) {
            return false;
        }
        
        //Verify expectations match (excluding any variable already mapped)
        if (!variables.containsKey(dependency.governor)) {
            if (!doesSenseSatisfyExpectations(possibility.get(ppDependency.getGovernor()), dependency.expectations)) {
                return false;
            }
        }
        if (!variables.containsKey(dependency.dependent)) {
            if (!doesSenseSatisfyExpectations(possibility.get(ppDependency.getDependent()), dependency.expectations)) {
                return false;
            }
        }
        
        return true;
    }
    
    private class IncompleteMappingException extends Exception {
        
    }
    
    private class UnusedNonOptionalSetException extends Exception {
        
        private PPToken token;
        private Sense sense;
        private DependencySet dependencySet;

        public UnusedNonOptionalSetException(PPToken token, Sense sense, DependencySet dependencySet) {
            this.token = token;
            this.sense = sense;
            this.dependencySet = dependencySet;
        }
        
    }
    
    private class PossibilityIterator implements Iterator<HashMap<PPToken, Sense>> {
        
        private PPSentence sentence;
        private LinkedList<PPToken> tokens;
        private HashMap<PPToken, LinkedList<Sense>> senses;
        private HashMap<PPToken, Integer> pointers;
        private boolean finished;
        
        public PossibilityIterator(PPSentence sentence) {
            this.sentence = sentence;
            this.tokens = sentence.listTokens();
            
            this.senses = new HashMap();
            this.pointers = new HashMap();
            this.finished = false;
            
            for (PPToken token : this.tokens) {
                this.senses.put(token, WIMGlobals.lexicon().listSenses(token));
                this.pointers.put(token, 0);
            }
        }

        public boolean hasNext() {
            return !this.finished;
        }

        public HashMap<PPToken, Sense> next() {
            HashMap<PPToken, Sense> next = new HashMap();
            
            for (PPToken token : this.tokens) {
                next.put(token, this.senses.get(token).get(this.pointers.get(token)));
            }
            
            increment(this.tokens.getLast());
            
            return next;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        private void increment(PPToken token) {
            int pointer = this.pointers.get(token) + 1;
            this.pointers.put(token, pointer);
            
            if (pointer >= this.senses.get(token).size()) {
                this.pointers.put(token, 0);
                
                if (token != this.tokens.getFirst()) {
                    increment(this.tokens.get(this.tokens.indexOf(token) - 1));
                } else {
                    this.finished = true;
                }
            }
        }
        
    }
    
}
