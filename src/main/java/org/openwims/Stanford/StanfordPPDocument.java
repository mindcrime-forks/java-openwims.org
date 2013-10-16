/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Stanford;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPSentence;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 *
 * @author jesseenglish
 */
public class StanfordPPDocument extends PPDocument {
    
//    private HashMap<CoreLabel, PPToken> tokenMap;
    private HashMap<CoreMap, HashMap<IndexedWord, PPToken>> tokenMap;

    public StanfordPPDocument(Annotation document) {
        super();
        
        this.tokenMap = new HashMap();
        
        List<CoreMap> stanfordSentences = document.get(CoreAnnotations.SentencesAnnotation.class);
//        for (CoreMap sentence : stanfordSentences) {
//            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                tokenMap.put(token, new StanfordPPToken(token));
//            }
//        }
        for (CoreMap sentence : stanfordSentences) { 
            HashMap<IndexedWord, PPToken> innerTokenMap = new HashMap();
            tokenMap.put(sentence, innerTokenMap);
            for (SemanticGraphEdge edge : sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class).getEdgeSet()) {
                if (innerTokenMap.get(edge.getGovernor()) == null) {
                    innerTokenMap.put(edge.getGovernor(), new StanfordPPToken(edge.getGovernor()));
                }
                if (innerTokenMap.get(edge.getDependent()) == null) {
                    innerTokenMap.put(edge.getDependent(), new StanfordPPToken(edge.getDependent()));
                }
            }
        }
        
        for (CoreMap sentence : stanfordSentences) {
            this.sentences.add(new StanfordPPSentence(sentence));
        }
        
    }
    
    private class StanfordPPSentence extends PPSentence {
    
        public StanfordPPSentence(CoreMap sentence) {
            super();
            
            this.text = sentence.toString();
            
            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            for (SemanticGraphEdge edge : graph.getEdgeSet()) {
                this.dependencies.add(new StanfordPPDependency(sentence, edge));
            }
            
            for (IndexedWord word : graph.topologicalSort()) {
                PPToken token = StanfordPPDocument.this.tokenMap.get(sentence).get(word);
                if (token == null) {
                    continue;
                }
                
                this.tokens.add(token);
            }
            
            Collections.sort(this.tokens);
        }
        
    }
    
    private class StanfordPPDependency extends PPDependency {

        public StanfordPPDependency(CoreMap sentence, SemanticGraphEdge edge) {
            super();
            
            this.type = edge.getRelation().getShortName();
            this.governor = StanfordPPDocument.this.tokenMap.get(sentence).get(edge.getGovernor());
            this.dependent = StanfordPPDocument.this.tokenMap.get(sentence).get(edge.getDependent());
        }
        
    }
    
    private class StanfordPPToken extends PPToken {
        
        public StanfordPPToken(IndexedWord token) {
            super();
            
            this.index = token.index();
            this.text = token.originalText();
            this.lemma = token.lemma();
            this.pos = token.tag();
            this.nerType = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
            
            //This seems to be wrong coming from stanford (consistently)
            if (this.lemma.equalsIgnoreCase("yourselve")) {
                this.lemma = "yourselves";
            }
        }
        
    }
    
}
