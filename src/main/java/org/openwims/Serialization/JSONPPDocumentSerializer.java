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
import java.util.HashMap;
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
    
    private Serializer serializer = new Serializer();
    private Deserializer deserializer = new Deserializer();
    
    
    public static PPDocument deserialize(String file) throws Exception {
        JSONPPDocumentSerializer s = new JSONPPDocumentSerializer();
        String contents = read(file);
        JSONObject o = (JSONObject)JSONValue.parse(contents);
        
        PPDocument document = s.deserializer.document(o);
        
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

    public static void serialize(PPDocument document, String file) throws Exception {        
        JSONPPDocumentSerializer s = new JSONPPDocumentSerializer();
        
        String json = s.serializer.json(document);
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
    
    private class Serializer {
        
        private HashMap<PPToken, Integer> tokenIDs = new HashMap();
        
        public String json(PPDocument document) {
            StringBuilder json = new StringBuilder();

            //First, generate token IDs
            int tokenID = 1;
            for (PPSentence sentence : document.listSentences()) {
                for (PPToken token : sentence.listTokens()) {
                    tokenIDs.put(token, tokenID);
                    tokenID++;
                }
            }
            
            json.append("{");
            json.append("\"tokens\":[");
            
            for (PPSentence sentence : document.listSentences()) {
                for (PPToken token : sentence.listTokens()) {
                    json.append(json(token));
                    
                    if (token != sentence.listTokens().getLast()) {
                        json.append(",");
                    }
                }
            }
            
            json.append("],");
            
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
        
        private  String json(PPToken token) {
            StringBuilder json = new StringBuilder();

            json.append("{");
            
            json.append("\"id\": ");
            json.append(tokenIDs.get(token));
            json.append(",");

            json.append("\"anchor\": \"");
            json.append(token.anchor());
            json.append("\",");

            json.append("\"lemma\": \"");
            json.append(token.lemma());
            json.append("\",");

            json.append("\"pos\": \"");
            json.append(token.pos());
            json.append("\",");

            json.append("\"ner\": \"");
            json.append(token.nerType());
            json.append("\",");
            
            json.append("\"corefers\": [");
            
            for (PPToken corefer : token.getCorefers()) {
                json.append(tokenIDs.get(corefer));
                
                if (corefer != token.getCorefers().getLast()) {
                    json.append(",");
                }
            }
            
            json.append("]");

            json.append("}");

            return json.toString();
        }
        
        private String json(PPSentence sentence) {
            StringBuilder json = new StringBuilder();

            json.append("{");

            json.append("\"text\":\"");
            json.append(sentence.text());
            json.append("\",");

            json.append("\"tokens\": [");
            for (PPToken token : sentence.listTokens()) {
                json.append(tokenIDs.get(token));
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
        
        private String json(PPDependency dependency) {
            StringBuilder json = new StringBuilder();

            json.append("{");

            json.append("\"type\": \"");
            json.append(dependency.getType());
            json.append("\",");

            json.append("\"governor\": ");
            json.append(tokenIDs.get(dependency.getGovernor()));
            json.append(",");

            json.append("\"dependent\": ");
            json.append(tokenIDs.get(dependency.getDependent()));
            json.append("");

            json.append("}");

            return json.toString();
        }
        
    }
    
    private class Deserializer {
        
        private HashMap<Integer, PPToken> tokenIDs = new HashMap();
        
        public PPDocument document(JSONObject o) {
            PPDocument document = new PPDocument();
            
            //First, declare a PPToken for each ID
            JSONArray tokens = (JSONArray)o.get("tokens");
            for (Object token : tokens) {
                int id = ((Long)((JSONObject)token).get("id")).intValue();
                tokenIDs.put(id, new PPToken());
            }
            
            for (Object token : tokens) {
                token((JSONObject)token);
            }

            JSONArray sentences = (JSONArray)o.get("sentences");
            for (Object sentence : sentences) {
                document.addSentence(sentence((JSONObject)sentence));
            }

            return document;
        }
        
        private PPToken token(JSONObject o) {
            int id = ((Long)o.get("id")).intValue();
            
            PPToken token = tokenIDs.get(id);

            token.setAnchor((String)o.get("anchor"));
            token.setLemma((String)o.get("lemma"));
            token.setPOS((String)o.get("pos"));
            token.setNERtype((String)o.get("ner"));
            
            JSONArray corefers = (JSONArray)o.get("corefers");
            for (Object corefer : corefers) {
                token.getCorefers().add(tokenIDs.get(((Long)corefer).intValue()));
            }

            return token;
        }
        
        private PPSentence sentence(JSONObject o) {
            PPSentence sentence = new PPSentence();

            sentence.setText((String)o.get("text"));

            JSONArray tokens = (JSONArray)o.get("tokens");
            for (Object token : tokens) {
                sentence.addToken(tokenIDs.get(((Long)token).intValue()));
            }

            JSONArray dependencies = (JSONArray)o.get("dependencies");
            for (Object dependency : dependencies) {
                sentence.addDependency(dependency((JSONObject)dependency, sentence));
            }

            return sentence;
        }
        
        private PPDependency dependency(JSONObject o, PPSentence sentence) {
            PPDependency dependency = new PPDependency();

            dependency.setType((String)o.get("type"));
            dependency.setGovernor(tokenIDs.get(((Long)o.get("governor")).intValue()));
            dependency.setDependent(tokenIDs.get(((Long)o.get("dependent")).intValue()));

            return dependency;
        }
        
    }

}
