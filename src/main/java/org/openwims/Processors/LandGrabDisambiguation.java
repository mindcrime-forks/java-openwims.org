/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Processors;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import org.openwims.Objects.Disambiguation.Interpretation;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPSentence;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.Processors.Iterators.NaivePossibilityIterator;
import org.openwims.Processors.Iterators.PossibilityIterator;
import org.openwims.Processors.WIMProcessor.WSDProcessor;
import org.openwims.Processors.WIMProcessor.WSEProcessor;
import org.openwims.Serialization.JSONPPDocumentSerializer;

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
        for (Interpretation interpretation : d.wse(new NaivePossibilityIterator(document))) {
            System.out.println(interpretation.wim());
        }
    }

    public LandGrabDisambiguation() {}
    
    public Interpretation wsd(LinkedList<Interpretation> interpretations) {
        Collections.sort(interpretations);
        Collections.reverse(interpretations);
        return interpretations.getFirst();
    }
    
    public LinkedList<Interpretation> wse(PossibilityIterator iter) {
        this.sentence = iter.getSentence();
        this.flattenedTokens = this.sentence.listFlattenedTokens();
        
        LinkedList<Interpretation> interpretations = new LinkedList();
        
        while (iter.hasNext()) {
            HashMap<PPToken, Sense> possibility = iter.next();
            try {
                HashMap<PPDependency, Dependency> mapping = landgrab(possibility);
                interpretations.add(new Interpretation(possibility, mapping));
            } catch (IncompleteMappingException e) {
                WIMProcessor.logPossibilityElimination(possibility, e);
            } catch (UnusedNonOptionalSetException e) {
                WIMProcessor.logPossibilityElimination(possibility, e);
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
            throw new IncompleteMappingException(this.sentence.listDependencies(), claims.keySet());
        }
        
        //Verify that all senses have all non-optional sets used
        for (PPToken token : possibility.keySet()) {
            Sense sense = possibility.get(token);
            
            for (DependencySet dependencySet : sense.listDependencySets()) {
                if (!dependencySet.optional && !claims.values().containsAll(dependencySet.dependencies)) {
                    throw new UnusedNonOptionalSetException(token, sense, dependencySet);
                }
            }
        }
        
        return claims;
    }
    
    private void claim(PPToken token, HashMap<PPToken, Sense> possibility, HashMap<PPDependency, Dependency> claims) {
        Sense sense = possibility.get(token);
        
        for (DependencySet dependencySet : sense.listDependencySets()) {
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
            if (!doesPossibilitySatisfyExpectations(ppDependency.getGovernor(), possibility, dependency.expectations)) {
                return false;
            }
        }
        if (!variables.containsKey(dependency.dependent)) {
            if (!doesPossibilitySatisfyExpectations(ppDependency.getDependent(), possibility, dependency.expectations)) {
                return false;
            }
        }
        
        return true;
    }
    
    private class IncompleteMappingException extends Exception {
        
        private Collection<PPDependency> inputDependencies;
        private Collection<PPDependency> claimedDependencies;

        public IncompleteMappingException(Collection<PPDependency> inputDependencies, Collection<PPDependency> claimedDependencies) {
            this.inputDependencies = inputDependencies;
            this.claimedDependencies = claimedDependencies;
        }

        @Override
        public String toString() {
            HashSet<PPDependency> inputs = new HashSet(inputDependencies);
            inputs.removeAll(claimedDependencies);
            
            return "no matches found for inputs: " + inputs;
        }
        
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

        @Override
        public String toString() {
            return token.anchor() + " (" + sense.getId() + ") found no match for NON-OPTIONAL dependency set " + dependencySet;
        }
        
    }
    
}
