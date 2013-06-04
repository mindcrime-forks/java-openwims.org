/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims;

import edu.stanford.nlp.pipeline.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Properties;
import org.openwims.Objects.WIMFrame;
import org.openwims.Stanford.StanfordHelper;

/**
 *
 * @author jesse
 */
public class WIMs {
    
    public static void main(String[] args) throws Exception {
        
        String testPath = "/Users/jesse/Desktop/test.stn";

//        StanfordHelper.save(StanfordHelper.annotate("The men hit the building with the hammer."), testPath);
        
        Annotation a = StanfordHelper.load(testPath);
        LinkedList<WIMFrame> frames = WIMProcessor.WIMify(a);

        for (WIMFrame frame : frames) {
            System.out.println(frame);
        }
    }

    public static Annotation loadDocument() throws Exception {
        FileInputStream fileIn =
                new FileInputStream("/Users/jesse/Desktop/test.stn");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Annotation a = (Annotation) in.readObject();
        in.close();
        fileIn.close();

        return a;
    }

    public static void samplePipeline(String text) {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        try {
            FileOutputStream fileOut =
                    new FileOutputStream("/Users/jesse/Desktop/test.stn");
            ObjectOutputStream out =
                    new ObjectOutputStream(fileOut);
            out.writeObject(document);
            out.close();
            fileOut.close();

        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
