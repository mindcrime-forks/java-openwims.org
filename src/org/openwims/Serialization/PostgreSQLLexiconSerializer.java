/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Serialization;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
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
public class PostgreSQLLexiconSerializer extends LexiconSerializer {
    
    private static Connection conn;
    
    public static Connection conn() throws Exception {
        if (PostgreSQLLexiconSerializer.conn == null) {
            String url = "jdbc:postgresql://localhost/OpenWIMs";
            String user = "jesse";
            String pass = "";

            Class.forName("org.postgresql.Driver");
            
            PostgreSQLLexiconSerializer.conn = DriverManager.getConnection(url, user, pass);
            
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {                    
                    try {
                        PostgreSQLLexiconSerializer.conn.close();
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }
            }));
        }
        
        return PostgreSQLLexiconSerializer.conn;
    }

    @Override
    public void saveSense(Sense sense) throws Exception {
        
        StringBuilder builder = new StringBuilder("");
        
        //BEGIN
        builder.append("BEGIN;\n");
        
        //DELETE OLD SENSE (IF IT EXISTS); CASCADE / FKEYS HANDLE THE REST
        builder.append("DELETE FROM senses WHERE id='");
        builder.append(sense.getId().replaceAll("'", "''"));
        builder.append("';\n");
        
        //ADD WORD (IF IT DOES NOT EXIST)
        builder.append("INSERT INTO words (representation) SELECT '");
        builder.append(sense.word().replaceAll("'", "''"));
        builder.append("' WHERE NOT EXISTS (SELECT representation FROM words WHERE representation='");
        builder.append(sense.word().replaceAll("'", "''"));
        builder.append("');\n");
        
        //INSERT SENSE
        builder.append("INSERT INTO senses (id, word, definition) VALUES ('");
        builder.append(sense.getId().replaceAll("'", "''"));
        builder.append("', '");
        builder.append(sense.word().replaceAll("'", "''"));
        builder.append("', '");
        builder.append(sense.getDefinition().replaceAll("'", "''"));
        builder.append("');\n");
        
        //INSERT MEANINGS
        for (Meaning meaning : sense.listMeanings()) {
            builder.append("INSERT INTO meanings (sense, target, relation, wim, structure) VALUES ('");
            builder.append(sense.getId().replaceAll("'", "''"));
            builder.append("', '");
            builder.append(meaning.target.replaceAll("'", "''"));
            builder.append("', '");
            builder.append(meaning.relation.replaceAll("'", "''"));
            builder.append("', '");
            builder.append(meaning.wim.replaceAll("'", "''"));
            builder.append("', -1);\n");
        }
        
        //INSERT STRUCTURES / DEPENDENCY SETS (CURRENTLY ONE TABLE)
        int series = 1;
        for (Structure structure : sense.listStructures()) {
            for (DependencySet set : structure.listDependencies()) {
                builder.append("INSERT INTO structures (sense, series, label, optional) VALUES ('");
                builder.append(sense.getId().replaceAll("'", "''"));
                builder.append("', ");
                builder.append(series);
                builder.append(", '");
                builder.append(set.label.replaceAll("'", "''"));
                builder.append("', ");
                builder.append(set.optional);
                builder.append(");\n");
                
                //INSERT DEPENDENCIES
                for (Dependency dependency : set.dependencies) {
                    builder.append("INSERT INTO dependencies (struct, dependency, governor, dependent) VALUES (");
                    builder.append("(SELECT max(id) FROM structures), '");
                    builder.append(dependency.type.replaceAll("'", "''"));
                    builder.append("', '");
                    builder.append(dependency.governor.replaceAll("'", "''"));
                    builder.append("', '");
                    builder.append(dependency.dependent.replaceAll("'", "''"));
                    builder.append("');\n");
                    
                    //INSERT SPECIFICATIONS
                    for (Expectation expectation : dependency.expectations) {
                        builder.append("INSERT INTO specifications (dependency, spec, expectation) VALUES (");
                        builder.append("(SELECT max(id) FROM dependencies), '");
                        builder.append(expectation.getSpecification().replaceAll("'", "''"));
                        builder.append("', '");
                        builder.append(expectation.getExpectation().replaceAll("'", "''"));
                        builder.append("');\n");
                    }
                }
                
                //INSERT MEANINGS
                for (Meaning meaning : set.meanings) {
                    builder.append("INSERT INTO meanings (sense, target, relation, wim, structure) VALUES ('");
                    builder.append(sense.getId().replaceAll("'", "''"));
                    builder.append("', '");
                    builder.append(meaning.target.replaceAll("'", "''"));
                    builder.append("', '");
                    builder.append(meaning.relation.replaceAll("'", "''"));
                    builder.append("', '");
                    builder.append(meaning.wim.replaceAll("'", "''"));
                    builder.append("', (SELECT max(id) FROM structures));\n");
                }
            }
            
            series += 1;
        }
        
        //COMMIT
        builder.append("COMMIT;\n");
        
        String insert = builder.toString();
        System.out.println(insert);
        
        Statement stmt = PostgreSQLLexiconSerializer.conn().createStatement();
        stmt.execute(insert);
        stmt.close();
    }

    @Override
    public void renameSense(Sense sense, String newID) throws Exception {
        String update = "UPDATE senses SET id='" + newID.replaceAll("'", "''") + "' WHERE id='" + sense.getId().replaceAll("'", "''") + "';";
        Statement stmt = PostgreSQLLexiconSerializer.conn().createStatement();
        stmt.execute(update);
        stmt.close();
    }
    
}
