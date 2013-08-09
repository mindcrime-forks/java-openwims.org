/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Serialization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPSentence;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 *
 * @author jesseenglish
 */
public class JSONPPDocumentSerializer {
    
    public static PPDocument deserialize(String file) throws Exception {
        String contents = read(file);
        JSONObject o = (JSONObject)JSONValue.parse(contents);
        PPDocument document = document(o);
        
        return document;
    }
    
    private static String read(String file) throws Exception {
        FileInputStream fstream = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        
        String line;
        StringBuilder builder = new StringBuilder();

        while ((line = br.readLine()) != null)   {
            builder.append(line);
            builder.append("\n");
        }

        in.close();
        
        return builder.toString();
    }
    
    private static PPDocument document(JSONObject o) {
        PPDocument document = new PPDocument();
        
        JSONArray sentences = (JSONArray)o.get("sentences");
        for (Object sentence : sentences) {
            document.addSentence(sentence((JSONObject)sentence));
        }
        
        return document;
    }
    
    private static PPSentence sentence(JSONObject o) {
        PPSentence sentence = new PPSentence();
        
        sentence.setText((String)o.get("text"));
        
        JSONArray tokens = (JSONArray)o.get("tokens");
        for (Object token : tokens) {
            sentence.addToken(token((JSONObject)token));
        }
        
        JSONArray dependencies = (JSONArray)o.get("dependencies");
        for (Object dependency : dependencies) {
            sentence.addDependency(dependency((JSONObject)dependency, sentence));
        }
        
        return sentence;
    }
    
    private static PPToken token(JSONObject o) {
        PPToken token = new PPToken();
        
        token.setAnchor((String)o.get("anchor"));
        token.setLemma((String)o.get("lemma"));
        token.setPOS((String)o.get("pos"));
        
        return token;
    }
    
    private static PPDependency dependency(JSONObject o, PPSentence sentence) {
        PPDependency dependency = new PPDependency();
        
        dependency.setType((String)o.get("type"));
        dependency.setGovernor(sentence.tokenWithAnchor((String)o.get("governor")));
        dependency.setDependent(sentence.tokenWithAnchor((String)o.get("dependent")));
        
        return dependency;
    }
    
    public static void serialize(PPDocument document, String file) throws Exception {
        String json = json(document);
        write(json, file);
    }
    
    private static void write(String contents, String file) throws Exception {
        File f = new File(file);
        if (!f.exists()) {
            f.createNewFile();
        }

        FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(contents);
        bw.close();
    }
    
    private static String json(PPDocument document) {
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        json.append("\"sentences\":[");
        
        for (PPSentence sentence : document.listSentences()) {
            json.append(json(sentence));
            
            if (sentence != document.listSentences().getLast()) {
                json.append(",");
            }
        }
        
        json.append("]");
        json.append("}");
        
        return json.toString();
    }
    
    private static String json(PPSentence sentence) {
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        
        json.append("\"text\":\"");
        json.append(sentence.text());
        json.append("\",");
        
        json.append("\"tokens\": [");
        for (PPToken token : sentence.listTokens()) {
            json.append(json(token));
            if (token != sentence.listTokens().getLast()) {
                json.append(",");
            }
        }
        json.append("],");
        
        json.append("\"dependencies\": [");
        for (PPDependency dependency : sentence.listDependencies()) {
            json.append(json(dependency));
            if (dependency != sentence.listDependencies().getLast()) {
                json.append(",");
            }
        }
        json.append("]");
        
        json.append("}");
        
        return json.toString();
    }
    
    private static String json(PPToken token) {
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        
        json.append("\"anchor\": \"");
        json.append(token.anchor());
        json.append("\",");
        
        json.append("\"lemma\": \"");
        json.append(token.lemma());
        json.append("\",");
        
        json.append("\"pos\": \"");
        json.append(token.pos());
        json.append("\"");
        
        json.append("}");
        
        return json.toString();
    }
    
    private static String json(PPDependency dependency) {
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        
        json.append("\"type\": \"");
        json.append(dependency.getType());
        json.append("\",");
        
        json.append("\"governor\": \"");
        json.append(dependency.getGovernor().anchor());
        json.append("\",");
        
        json.append("\"dependent\": \"");
        json.append(dependency.getDependent().anchor());
        json.append("\"");
        
        json.append("}");
        
        return json.toString();
    }
    
    
}
