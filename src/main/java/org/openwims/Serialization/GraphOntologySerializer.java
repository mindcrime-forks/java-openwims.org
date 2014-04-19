package org.openwims.Serialization;

import org.openwims.Objects.Ontology.Concept;
import org.openwims.Utils.GraphUtils;
import org.openwims.WIMGlobals;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by jesse on 4/19/14.
 */
public class GraphOntologySerializer implements OntologySerializer {

    @Override
    public void saveConcept(Concept concept) throws Exception {
        throw new RuntimeException("I totes didn't implement this function.  Please see code, birch.");
    }

    public static void main(String args[]){
        LinkedList<Concept> concepts = WIMGlobals.ontology().concepts();
        for (Concept concept : concepts) {
            createConceptNode(concept);
        }

        for (Concept concept : concepts) {
            connectToParent(concept);
        }
    }

    private static void connectToParent(Concept concept) {
        if (concept.getParent() == null){
            return;
        }

        HashMap<String, Object> params = new HashMap<String, Object>();

        params.put("name", concept.getName());
        params.put("parentName", concept.getParent().getName());

        String query = "match (c:Concept {name:{name}}), (parent:Concept {name:{parentName}}) create (c)-[:ISA]->(parent)";

        GraphUtils.runCypher(query, params);
    }

    private static void createConceptNode(Concept concept) {
        HashMap<String, Object> params = new HashMap<String, Object>();

        HashMap<String, String> properties = new HashMap<String, String>();

        properties.put("name", concept.getName());
        properties.put("gloss", concept.getGloss());
        properties.put("definition", concept.getDefinition());

        params.put("properties", properties);

        String query = "create (:Concept {properties})";

        GraphUtils.runCypher(query, params);
    }

}
