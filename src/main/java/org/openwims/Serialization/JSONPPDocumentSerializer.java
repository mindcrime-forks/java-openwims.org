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
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPMention;
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
        private HashMap<PPSentence, Integer> sentenceIDs = new HashMap();
        
        public String json(PPDocument document) {
            //First, generate IDs for each sentence and token
            int tokenID = 0;
            int sentenceID = 0;
            
            for (PPSentence sentence : document.listSentences()) {
                sentenceIDs.put(sentence, sentenceID);
                sentenceID++;
                
                for (PPToken token : sentence.listTokens()) {
                    if (!tokenIDs.containsKey(token)) {
                        tokenIDs.put(token, tokenID);
                        tokenID++;
                    }
                }
            }
            
            //Now we write out the tokens
            StringBuilder json = new StringBuilder();
        
            json.append("{");
            json.append("\"tokens\":[");
            for (Iterator<PPToken> it = tokenIDs.keySet().iterator(); it.hasNext();) {
                PPToken token = it.next();
                json.append(json(token));
                if (it.hasNext()) {
                    json.append(",");
                }
            }
            
            json.append("],");
            
            //Now we write out the sentences
            json.append("\"sentences\":[");
            for (Iterator<PPSentence> it = sentenceIDs.keySet().iterator(); it.hasNext();) {
                PPSentence sentence = it.next();
                json.append(json(sentence));
                if (it.hasNext()) {
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
            
            json.append("\"id\": \"");
            json.append(sentenceIDs.get(sentence));
            json.append("\",");

            json.append("\"text\":\"");
            json.append(sentence.text());
            json.append("\",");

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

            json.append("\"governor\": \"");
            json.append(tokenIDs.get(dependency.getGovernor()));
            json.append("\",");

            json.append("\"dependent\": \"");
            json.append(tokenIDs.get(dependency.getDependent()));
            json.append("\"");

            json.append("}");

            return json.toString();
        }
        
        private String json(PPToken token) {
            StringBuilder json = new StringBuilder();

            json.append("{");
            
            json.append("\"id\": \"");
            json.append(tokenIDs.get(token));
            json.append("\",");

            json.append("\"mentions\": [");
            for (PPMention mention : token.getMentions()) {
                json.append(json(mention));
                if (mention != token.getMentions().getLast()) {
                    json.append(", ");
                }
            }

            json.append("]");

            json.append("}");

            return json.toString();
        }
        
        private String json(PPMention mention) {
            StringBuilder json = new StringBuilder();

            json.append("{");

            json.append("\"anchor\": \"");
            json.append(mention.anchor());
            json.append("\",");

            json.append("\"lemma\": \"");
            json.append(mention.lemma());
            json.append("\",");

            json.append("\"pos\": \"");
            json.append(mention.pos());
            json.append("\",");
            
            json.append("\"sentence\": \"");
            json.append(sentenceIDs.get(mention.getSentence()));
            json.append("\"");

            json.append("}");

            return json.toString();
        }
        
    }
    
    private class Deserializer {
        
        private HashMap<Integer, PPToken> tokenIDs = new HashMap();
        private HashMap<Integer, PPSentence> sentenceIDs = new HashMap();
        
        public PPDocument document(JSONObject o) {
            PPDocument document = new PPDocument();
        
            JSONArray tokens = (JSONArray)o.get("tokens");
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
            PPToken token = new PPToken();
            
            //First, pull out the id and associate the object with it
            int id = Integer.parseInt((String)o.get("id"));
            tokenIDs.put(id, token);
            
            //Now parse each mention
            JSONArray mentions = (JSONArray)o.get("mentions");
            for (Object mention : mentions) {
                PPMention m = mention((JSONObject)mention);
                token.getMentions().add(m);
                
                PPSentence sentence = m.getSentence();
                if (!sentence.listTokens().contains(token)) {
                    sentence.addToken(token);
                }
            }
            
            return token;
        }
        
        private PPMention mention(JSONObject o) {
            PPMention mention = new PPMention();
            
            mention.setAnchor((String)o.get("anchor"));
            mention.setLemma((String)o.get("lemma"));
            mention.setPOS((String)o.get("pos"));
            
            int sentenceID = Integer.parseInt((String)o.get("sentence"));
            PPSentence sentence = getOrAddSentence(sentenceID);
            mention.setSentence(sentence);
            
            return mention;
        }
        
        private PPSentence sentence(JSONObject o) {
            int id = Integer.parseInt((String)o.get("id"));
            PPSentence sentence = getOrAddSentence(id);
            
            sentence.setText((String)o.get("text"));

            JSONArray dependencies = (JSONArray)o.get("dependencies");
            for (Object dependency : dependencies) {
                sentence.addDependency(dependency((JSONObject)dependency));
            }

            return sentence;
        }
        
        private PPDependency dependency(JSONObject o) {
            PPDependency dependency = new PPDependency();
        
            dependency.setType((String)o.get("type"));
            dependency.setGovernor(tokenIDs.get(Integer.parseInt((String)o.get("governor"))));
            dependency.setDependent(tokenIDs.get(Integer.parseInt((String)o.get("dependent"))));

            return dependency;
        }
        
       
        
        
        private PPSentence getOrAddSentence(int id) {
            PPSentence sentence = sentenceIDs.get(id);
            if (sentence == null) {
                sentence = new PPSentence();
                sentenceIDs.put(id, sentence);
            }
            
            return sentence;
        }
        
    }
    
}
