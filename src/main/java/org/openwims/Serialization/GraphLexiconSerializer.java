package org.openwims.Serialization;

import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Meaning;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Utils.GraphUtils;
import org.openwims.WIMGlobals;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by jesse on 4/19/14.
 */
public class GraphLexiconSerializer {
    public static void main(String args[]) throws Exception {

        Transaction tx = GraphUtils.graphdb.beginTx();
        try {

            for (String root : WIMGlobals.lexicon().roots()) {
                saveRoot(root);
            }

            LinkedList<String> senses = WIMGlobals.lexicon().senses();
            Collections.sort(senses);
            int seen = 0;
            for (String senseName : senses) {
                if(seen % 1000 == 0){
                    System.out.println(seen);
                }
                //System.out.println(senseName);
                Sense sense = WIMGlobals.lexicon().sense(senseName);
                saveSense(sense);
                seen++;
                if (seen == 100){
                    break;
                }
            }
            tx.success();
        } catch (Exception ex){
            ex.printStackTrace();
            tx.failure();
        }
        tx.close();
    }

    private static void saveRoot(String root) {
        HashMap<String, Object> params = new HashMap();

        params.put("word", root);

        String query = "create (:Root {word:{word}})";

        GraphUtils.runCypher(query, params);
    }

    private static void saveSense(Sense sense) {
        if (sense == null){
            return;
        }
        HashMap<String, Object> params = new HashMap();
        params.put("properties", sense.getProperties());
        params.put("rootName", sense.word());
        params.put("conceptName", sense.concept());

        long millis = System.currentTimeMillis();
        String query = "match (r:Root {word:{rootName}}), (c:Concept {name:{conceptName}})\n" +
                "create (s:Sense {properties}) create (s)-[:ISA]->(c), (s)-[:hasRoot]->(r)\n" +
                "return s";

        Node node = (Node) GraphUtils.runCypher(query, params).columnAs("s").next();

        HashMap<String, Node> variables = saveVariablesFromSense(sense);
        for (Meaning meaning : sense.listMeanings()) {
            params.clear();

            saveMeaning(meaning, variables, node);
        }

        for (DependencySet set : sense.listDependencySets()) {
            saveDependencySet(set, variables, node);
        }

//        System.out.println("Sense added in " + (System.currentTimeMillis() - millis) + "ms.");
    }

    private static void saveDependencySet(DependencySet set, HashMap<String, Node> variables, Node parent) {
        HashMap<String, Object> params = new HashMap();
        params.put("parent", parent);
        HashMap<String, Object> properties = new HashMap();
        properties.put("label", set.label);
        properties.put("optional", set.optional);
        params.put("properties", properties);

        String query = "start parent=node({parent})" +
                "\n create (d:DependencySet {properties})<-[:has]-(parent) return d";

        Node setNode = (Node) GraphUtils.runCypher(query, params).columnAs("d").next();

        for (Meaning meaning : set.meanings) {
            saveMeaning(meaning, variables, setNode);
        }

        for (Dependency dependency : set.dependencies) {
            saveDependency(dependency, variables, setNode);
        }
    }

    private static Node saveDependency(Dependency dependency, HashMap<String, Node> variables, Node parent) {
        HashMap<String, String> expectations = dependency.getExpectationsAsMap();
        HashMap<String, Object> params = new HashMap();
        params.put("type", dependency.type);
        params.put("parent", parent);
        params.put("dependent", variables.get(dependency.dependent));
        params.put("governor", variables.get(dependency.governor));
        params.put("expectations", expectations);

        String query = "start parent=node({parent}), dependent=node({dependent}), governor=node({governor})\n" +
                "create (parent)-[:has]->(d:Dependency {type:{type}})\n" +
                "create (dependent)<-[:dependent {expectations}]-(d)-[:governor]->(governor)\n" +
                "return d;";

        return (Node) GraphUtils.runCypher(query, params).columnAs("d").next();
    }

    private static Node saveMeaning(Meaning meaning, HashMap<String, Node> variables, Node parent) {
        HashMap<String, Object> params = new HashMap();
        params.put("type", meaning.relation);
        params.put("parent", parent);
        params.put("domain", variables.get(meaning.wim));
        params.put("range", variables.get(meaning.target));
        String query = "start parent=node({parent}), range=node({range}), domain=node({domain})\n" +
                "create (parent)-[:has]->(m:Meaning {type:{type}})\n" +
                "create (range)<-[:range]-(m)-[:domain]->(domain)\n" +
                "return m";
        return (Node) GraphUtils.runCypher(query, params).columnAs("m").next();
    }

    private static HashMap<String, Node> saveVariablesFromSense(Sense sense) {
        HashMap<String, Node> nodes = new HashMap();

        for (String variable : sense.listVariables()) {
            HashMap<String, Object> params = new HashMap();
            params.put("name", variable);
            params.put("uid", sense.getUid());
            params.put("variable", variable);
            String query = "match (s:Sense {uid:{uid}}) create (r:Variable {name:{variable}})<-[:has]-(s) return r";
            ExecutionResult result = GraphUtils.runCypher(query, params);
            nodes.put(variable, (Node) result.columnAs("r").next());
        }
        return nodes;
    }
}
