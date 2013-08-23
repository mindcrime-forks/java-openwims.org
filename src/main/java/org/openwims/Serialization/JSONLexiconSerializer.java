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
import java.util.LinkedList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Expectation;
import org.openwims.Objects.Lexicon.Meaning;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Lexicon.Structure;

/**
 *
 * @author jesseenglish
 */
public class JSONLexiconSerializer {

    public static Sense deserialize(String file) throws Exception {
        String contents = read(file);
        JSONObject o = (JSONObject)JSONValue.parse(contents);
        Sense sense = sense(o);
        
        return sense;
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
    
    private static Sense sense(JSONObject o) {
        Sense sense = new Sense((String)o.get("id"));
        
        sense.setDefinition((String)o.get("definition"));
        sense.setExample((String)o.get("example"));
        
        JSONArray meanings = (JSONArray)o.get("meanings");
        for (Object meaning : meanings) {
            sense.addMeaning(meaning((JSONObject)meaning));
        }
        
        JSONArray structures = (JSONArray)o.get("structures");
        for (Object structure : structures) {
            sense.addStructure(structure((JSONObject)structure));
        }
        
        return sense;
    }
    
    private static Meaning meaning(JSONObject o) {
        Meaning meaning = new Meaning((String)o.get("target"), (String)o.get("relation"), (String)o.get("wim"));
        return meaning;
    }
    
    private static Structure structure(JSONObject o) {
        Structure structure = new Structure();
        
        JSONArray sets = (JSONArray)o.get("sets");
        for (Object set : sets) {
            structure.addDependencySet(set((JSONObject)set));
        }
        
        return structure;
    }
    
    private static DependencySet set(JSONObject o) {
        DependencySet set = new DependencySet(new LinkedList(), new LinkedList(), Boolean.parseBoolean((String)o.get("optional")), (String)o.get("label"));
        
        JSONArray meanings = (JSONArray)o.get("meanings");
        for (Object meaning : meanings) {
            set.meanings.add(meaning((JSONObject)meaning));
        }
        
        JSONArray dependencies = (JSONArray)o.get("dependencies");
        for (Object dependency : dependencies) {
            set.dependencies.add(dependency((JSONObject)dependency));
        }
        
        return set;
    }
    
    private static Dependency dependency(JSONObject o) {
        Dependency dependency = new Dependency((String)o.get("type"), (String)o.get("governor"), (String)o.get("dependent"), new LinkedList());
        
        JSONArray expectations = (JSONArray)o.get("expectations");
        for (Object expectation : expectations) {
            dependency.expectations.add(expectation((JSONObject)expectation));
        }
        
        return dependency;
    }
    
    private static Expectation expectation(JSONObject o) {
        Expectation expectation = new Expectation((String)o.get("specification"), (String)o.get("expectation"));
        return expectation;
    }
    
    public static void serialize(Sense sense, String file) throws Exception {
        String json = json(sense);
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
    
    private static String json(Sense sense) {
        StringBuilder json = new StringBuilder();

        json.append("{");
        
        json.append("\"id\": \"");
        json.append(sense.getId());
        json.append("\", ");
        
        json.append("\"concept\": \"");
        json.append(sense.concept());
        json.append("\", ");
        
        json.append("\"word\": \"");
        json.append(sense.word());
        json.append("\", ");
        
        json.append("\"pos\": \"");
        json.append(sense.pos());
        json.append("\", ");
        
        json.append("\"instance\": \"");
        json.append(sense.instance());
        json.append("\", ");
        
        json.append("\"definition\": \"");
        json.append(sense.getDefinition());
        json.append("\", ");
        
        json.append("\"example\": \"");
        json.append(sense.getExample());
        json.append("\", ");
        
        json.append("\"meanings\": [");
        for (Meaning meaning : sense.listMeanings()) {
            json.append(json(meaning));
            if (meaning != sense.listMeanings().getLast()) {
                json.append(", ");
            }
        }
        json.append("],");
        
        json.append("\"structures\": [");
        for (Structure structure : sense.listStructures()) {
            json.append(json(structure));
            if (structure != sense.listStructures().getLast()) {
                json.append(", ");
            }
        }
        json.append("]");
        
        json.append("}");
        
        return json.toString();
    }
    
    private static String json(Meaning meaning) {
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        
        json.append("\"target\": \"");
        json.append(meaning.target);
        json.append("\", ");
        
        json.append("\"relation\": \"");
        json.append(meaning.relation);
        json.append("\", ");
        
        json.append("\"wim\": \"");
        json.append(meaning.wim);
        json.append("\"");
        
        json.append("}");
        
        return json.toString();
    }
    
    private static String json(Structure structure) {
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        
        json.append("\"sets\": [");
        for (DependencySet dependencySet : structure.listDependencies()) {
            json.append(json(dependencySet));
            if (dependencySet != structure.listDependencies().getLast()) {
                json.append(", ");
            }
        }
        json.append("]");
        
        json.append("}");
        
        return json.toString();
    }
    
    private static String json(DependencySet set) {
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        
        json.append("\"label\": \"");
        json.append(set.label);
        json.append("\", ");
        
        json.append("\"optional\": \"");
        json.append(set.optional);
        json.append("\", ");
        
        json.append("\"meanings\": [");
        for (Meaning meaning : set.meanings) {
            json.append(json(meaning));
            if (meaning != set.meanings.getLast()) {
                json.append(", ");
            }
        }
        json.append("], ");
        
        json.append("\"dependencies\": [");
        for (Dependency dependency : set.dependencies) {
            json.append(json(dependency));
            if (dependency != set.dependencies.getLast()) {
                json.append(", ");
            }
        }
        json.append("]");
        
        json.append("}");
        
        return json.toString();
    }
    
    private static String json(Dependency dependency) {
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        
        json.append("\"type\": \"");
        json.append(dependency.type);
        json.append("\", ");
        
        json.append("\"governor\": \"");
        json.append(dependency.governor);
        json.append("\", ");
        
        json.append("\"dependent\": \"");
        json.append(dependency.dependent);
        json.append("\", ");
        
        json.append("\"expectations\": [");
        for (Expectation expectation : dependency.expectations) {
            json.append(json(expectation));
            if (expectation != dependency.expectations.getLast()) {
                json.append(", ");
            }
        }
        json.append("]");
        
        json.append("}");
        
        return json.toString();
    }
    
    private static String json(Expectation expectation) {
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        
        json.append("\"specification\": \"");
        json.append(expectation.getSpecification());
        json.append("\", ");
        
        json.append("\"expectation\": \"");
        json.append(expectation.getExpectation());
        json.append("\"");
        
        json.append("}");
        
        return json.toString();
    }
    
}
