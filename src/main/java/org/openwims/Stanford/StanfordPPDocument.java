/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Stanford;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPMention;
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

        //set sentence pointers in all the mentions
        for (PPSentence sentence : this.sentences) {
            for (PPToken token : sentence.listTokens()) {
                for (PPMention mention : token.getMentions()) {
                    mention.setSentence(sentence);
                }
            }
        }

        //reference resolution
        Map<Integer, CorefChain> entities = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
        for (CorefChain chain : entities.values()) {
            PPToken entityToken = new PPToken();
            LinkedList<PPToken> toBeReplaced = new LinkedList();
            //for each entity, compile a list of all of the tokens that are mentions
            for (CorefChain.CorefMention mention : chain.getMentionsInTextualOrder()) {
                int wordNum = mention.headIndex;
                int sentenceNum = mention.sentNum;
                PPToken wordToReplace = sentences.get(sentenceNum - 1).listTokens().get(wordNum - 1);
                toBeReplaced.add(wordToReplace);
                entityToken.getMentions().addAll(wordToReplace.getMentions());
            }

            //replace each mention with the entity token 
            for (PPSentence sentence : this.sentences) {
                for (PPToken token : toBeReplaced) {
                    if (sentence instanceof StanfordPPSentence) {
                        StanfordPPSentence stanfordPPSentence = (StanfordPPSentence) sentence;
                        //replace the mention with the entity in the sentence token list
                        if (sentence.listTokens().contains(token)) {
                            int index = stanfordPPSentence.getTokens().indexOf(token);
                            stanfordPPSentence.getTokens().remove(token);
                            stanfordPPSentence.getTokens().add(index, entityToken);
                        }
                        //replace the mention with the entity in the deps
                        for (PPDependency dep : stanfordPPSentence.getDependencies()) {
                            if(toBeReplaced.contains(dep.getDependent())){
                                dep.setDependent(entityToken);
                            }
                            if(toBeReplaced.contains(dep.getGovernor())){
                                dep.setGovernor(entityToken);
                            }
                        }
                    }
                }
            }
            //replace each mention with the entity token in the syntax

        }
        //DO REF HEREs

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

            Collections.sort(this.tokens, new Comparator<PPToken>() {
                public int compare(PPToken o1, PPToken o2) {
                    return o1.getMentions().getFirst().getIndex() - o2.getMentions().getFirst().getIndex();
                }
            });
        }

        private LinkedList<PPToken> getTokens() {
            return tokens;
        }

        private LinkedList<PPDependency> getDependencies() {
            return dependencies;
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
            this.mentions.add(new StanfordPPMention(token));
        }
    }

    private class StanfordPPMention extends PPMention {

        public StanfordPPMention(IndexedWord token) {
            super();

            this.index = token.index();
            this.text = token.originalText();
            this.lemma = token.lemma();
            this.pos = token.tag();
            this.NERtype = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
        }
    }
}
