package org.openwims.Utils;

import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.Visitor;
import org.neo4j.kernel.impl.core.DefaultLabelIdCreator;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.kernel.logging.LogMarker;
import org.openwims.WIMGlobals;

import java.util.HashMap;

/**
 * Created by jesse on 4/19/14.
 */
public class GraphUtils {
    public static GraphDatabaseService graphdb;

    static {
        GraphUtils.graphdb = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(WIMGlobals.graphdbpath)
                .loadPropertiesFromFile(WIMGlobals.graphdbsettings)
                .newGraphDatabase();

        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                System.out.println("shutting dat shit DOWN");
                GraphUtils.graphdb.shutdown();
            }
        } );
    }

    public static ExecutionResult runCypher(String query){
        return runCypher(query, new HashMap<String, Object>());
    }

    public static ExecutionResult runCypher(String query, HashMap<String, Object> params){
        GraphDatabaseService db = GraphUtils.graphdb;

        ExecutionEngine engine = new ExecutionEngine(db, new QueryLogger());

        ExecutionResult result = engine.execute(query, params);


        return result;
    }

    public static Node getNode(String label, String prop, Object value) {
        GraphDatabaseService db = GraphUtils.graphdb;


        Node node = db.findNodesByLabelAndProperty(DynamicLabel.label(label), prop, value).iterator().next();


        return node;
    }

    public static String replaceParams(String query, HashMap<String, Object> params) {
        String output = query + "";
        for (String paramName : params.keySet()) {
            String quotes = "";
            if (params.get(paramName) instanceof String) {
                quotes = "\"";
            }
            output = output.replaceAll("\\{" + paramName + "\\}", quotes + params.get(paramName).toString() + quotes);
        }
        return output;
    }
}
