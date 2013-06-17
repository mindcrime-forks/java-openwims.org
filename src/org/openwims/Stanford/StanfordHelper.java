/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Stanford;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    
}
