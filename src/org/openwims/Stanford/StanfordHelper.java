/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Stanford;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author jesse
 */
public class StanfordHelper {

    private static LexicalizedParser parser = null;

    public static LexicalizedParser parser() {
        if (StanfordHelper.parser == null) {
            StanfordHelper.parser = LexicalizedParser.getParserFromSerializedFile("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        }
        return StanfordHelper.parser;
    }

    public static Annotation annotate(String text) {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        return document;
    }
    
    public static void save(Annotation annotation, String file) throws Exception {
        FileOutputStream fileOut = new FileOutputStream(file);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(annotation);
        out.close();
        fileOut.close();
    }
    
    public static Annotation load(String file) throws Exception {
        FileInputStream fileIn = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Annotation a = (Annotation) in.readObject();
        in.close();
        fileIn.close();

        return a;
    }
    
    @Deprecated
    public static Tree parse(String text) {
        StringReader reader = new StringReader(text);
                
        Tokenizer tokenizer = PTBTokenizer.newPTBTokenizer(reader);
        List tokens = tokenizer.tokenize();
        
        Tree parse = (Tree) StanfordHelper.parser().apply(tokens);
        
        return parse;
    }

    @Deprecated
    public static LinkedList<TypedDependency> dependencies(Tree parse) {
        TreebankLanguagePack languagePack = new PennTreebankLanguagePack();
        GrammaticalStructureFactory factory = languagePack.grammaticalStructureFactory();
        GrammaticalStructure gs = factory.newGrammaticalStructure(parse);
        Collection dependencies = gs.typedDependencies();
        
        return new LinkedList(dependencies);
    }
    
    @Deprecated
    public static String getPOSForConstituent(TreeGraphNode constituent, Tree parse) {
        if (parse == null) {
            return "UNKNOWN";
        }
        
        String representation = constituent.toOneLineString();
        String constituentText = representation.split("-")[0];
        int constituentIndex = Integer.parseInt(representation.split("-")[1]);
        
        TaggedWord word = parse.taggedYield().get(constituentIndex - 1);
        List<CoreLabel> labels = parse.taggedLabeledYield();
        if (constituentText.equals(word.word())) {
            return word.tag();
        }
        
        return "UNKNOWN";
    }
    
}
