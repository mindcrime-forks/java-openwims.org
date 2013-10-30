/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Serialization;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.openwims.Objects.Ontology.Concept;

/**
 *
 * @author jesseenglish
 */
public class PostgreSQLOntologySerializer implements OntologySerializer {
    
    private static Connection conn;
    
    public static Connection conn() throws Exception {
        if (PostgreSQLOntologySerializer.conn == null) {
            String url = "jdbc:postgresql://localhost/OpenWIMs";
            String user = "jesse";
            String pass = "";

            Class.forName("org.postgresql.Driver");
            
            PostgreSQLOntologySerializer.conn = DriverManager.getConnection(url, user, pass);
            
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {                    
                    try {
                        PostgreSQLOntologySerializer.conn.close();
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }
            }));
        }
        
        return PostgreSQLOntologySerializer.conn;
    }

    public void saveConcept(Concept concept) throws Exception {
        StringBuilder builder = new StringBuilder("");
        
        //BEGIN
        builder.append("BEGIN;\n");
        
        //DELETE OLD CONCEPT (IF IT EXISTS)
        builder.append("DELETE FROM ontology WHERE concept='");
        builder.append(concept.getName().replaceAll("'", "''"));
        builder.append("';\n");
        
        //INSERT RELEVANT PARTS
        builder.append("INSERT INTO ontology (concept, parent, definition, gloss) VALUES ('");
        builder.append(concept.getName().replaceAll("'", "''"));
        builder.append("', '");
        builder.append(concept.getParent().getName().replaceAll("'", "''"));
        builder.append("', '");
        builder.append(concept.getDefinition().replaceAll("'", "''"));
        builder.append("', '");
        builder.append(concept.getGloss().replaceAll("'", "''"));
        builder.append("');\n");
        
        //COMMIT
        builder.append("COMMIT;");
        
        String insert = builder.toString();
        System.out.println(insert);
        
        Statement stmt = PostgreSQLLexiconSerializer.conn().createStatement();
        stmt.execute(insert);
        stmt.close();
    }
    
}
