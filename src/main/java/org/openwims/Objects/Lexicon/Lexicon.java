/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.Serialization.LexiconSerializer;
import org.openwims.Serialization.PostgreSQLLexiconSerializer;

/**
 *
 * @author jesse
 */
public class Lexicon {

    private static Connection conn = null;
    private HashMap<String, Word> words;
    private LexiconSerializer serializer;
    
    public static void main(String[] args) throws Exception {
        System.out.println(new Lexicon().word("hit"));
    }

    public Lexicon() {
        this.words = new HashMap();
        this.serializer = new PostgreSQLLexiconSerializer();
    }
    
    public LexiconSerializer serializer() {
        return this.serializer;
    }
    
    public static Connection conn() throws Exception {
        if (Lexicon.conn == null) {
            String url = "jdbc:postgresql://localhost/OpenWIMs";
            String user = "jesse";
            String pass = "";

            Class.forName("org.postgresql.Driver");
            
            Lexicon.conn = DriverManager.getConnection(url, user, pass);
            
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {                    
                    try {
                        Lexicon.conn.close();
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }
            }));
        }
        
        return Lexicon.conn;
    }
    
    public Sense sense(String id) throws Exception {
        Statement stmt = Lexicon.conn().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT word FROM senses WHERE id='" + id.replaceAll("'", "''") + "';");
        if (rs.next()) {
            String word = rs.getString("word");
            Word w = word(word);
            return w.getSense(id);
        }
        
        return null;
    }
    
    public LinkedList<String> roots() throws Exception {
        LinkedList<String> roots = new LinkedList();
        
        Statement stmt = Lexicon.conn().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT representation FROM words ORDER BY representation ASC;");
        while (rs.next()) {
            roots.add(rs.getString("representation"));
        }
        
        stmt.close();
        
        return roots;
    }
    
    public LinkedList<String> senses() throws Exception {
        LinkedList<String> senses = new LinkedList();
        
        Statement stmt = Lexicon.conn().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM senses ORDER BY id ASC;");
        while (rs.next()) {
            senses.add(rs.getString("id"));
        }
        
        stmt.close();
        
        return senses;
    }
    
    public LinkedList<String> verbs() throws Exception {
        LinkedList<String> verbs = new LinkedList();
        
        Statement stmt = Lexicon.conn().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM senses WHERE id LIKE '%-v-%' ORDER BY id ASC;");
        while (rs.next()) {
            verbs.add(rs.getString("id"));
        }
        
        stmt.close();
        
        return verbs;
    }
    
    public LinkedList<String> nouns() throws Exception {
        LinkedList<String> verbs = new LinkedList();
        
        Statement stmt = Lexicon.conn().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM senses WHERE id LIKE '%-n-%' ORDER BY id ASC;");
        while (rs.next()) {
            verbs.add(rs.getString("id"));
        }
        
        stmt.close();
        
        return verbs;
    }
    
    public LinkedList<Sense> listSenses(PPToken token){
        LinkedList<Sense> list = new LinkedList<Sense>();
        Word word = word(token.lemma());
        list.addAll(word.listSenses(token.rootPOS()));
        
        //handle NER
        if(list.size() == 0 && !token.nerType().equalsIgnoreCase("")){
            if(token.nerType().equalsIgnoreCase("date")){
                Sense dateSense = new Sense("@temporal-object", token.lemma(), "n", nextInstanceNumber("@temporal-object", token.lemma(), "n"));
                list.add(dateSense);
                word.addSense(dateSense);
            }
            if(token.nerType().equalsIgnoreCase("person")){
                Sense personSense = new Sense("@human", token.lemma(), "n", nextInstanceNumber("@human", token.lemma(), "n"));
                list.add(personSense);
                word.addSense(personSense);
            }
            if (token.nerType().equalsIgnoreCase("time")) {
                Sense timeSense = new Sense("@temporal-object", token.lemma(), "n", nextInstanceNumber("@temporal-object", token.lemma(), "n"));
                list.add(timeSense);
                word.addSense(timeSense);
            }
            if (token.nerType().equalsIgnoreCase("location")) {
                Sense locationSense = new Sense("@location-object", token.lemma(), "n", nextInstanceNumber("@location-object", token.lemma(), "n"));
                list.add(locationSense);
                word.addSense(locationSense);
            }
        }
        
        //handle unknowns
        if (list.size() == 0) {
            Sense noneSense = new Sense("@none", token.lemma(), token.rootPOS().toLowerCase(), 1);
            list.add(noneSense);
            word.addSense(noneSense);
        }
        
        return list;
    }

    public Word word(String representation) {
                
        if (this.words.get(representation) != null) {
            return this.words.get(representation);
        }
        
        Word word = new Word(representation);
        
        try {
            Statement stmt = Lexicon.conn().createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM senses WHERE word='" + representation.toLowerCase().replaceAll("'", "''") + "';");
            while (rs.next()) {
                String example = rs.getString("example");
                if (example == null) {
                    example = "";
                }
                
                Sense sense = new Sense(rs.getString("meaning"), rs.getString("word"), rs.getString("pos"), rs.getInt("instance"));
                sense.setDefinition(rs.getString("definition"));
                sense.setExample(example);
                sense.setFrequency(rs.getDouble("frequency"));
                sense.setUid(rs.getInt("uid"));
                word.addSense(sense);
            }
            
            for (Sense sense : word.listSenses()) {
                
                HashMap<Integer, DependencySet> dependencySets = new HashMap();
                HashMap<Integer, Structure> structures = new HashMap();
                HashMap<Integer, Dependency> dependencies = new HashMap();
                
                String query = "SELECT * FROM structures WHERE sense_uid=" + sense.getUid() + ";";
                
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int series = rs.getInt("series");
                    String set = rs.getString("label");
                    boolean optional = rs.getBoolean("optional");
                                        
                    Structure structure = structures.get(series);
                    if (structure == null) {
                        structure = new Structure();
                        sense.addStructure(structure);
                        structures.put(series, structure);
                    }
                    
                    DependencySet dependencySet = new DependencySet(new LinkedList(), new LinkedList(), optional, set);
                    structure.addDependencySet(dependencySet);
                    
                    dependencySets.put(id, dependencySet);
                }
                
                for (Integer id : dependencySets.keySet()) {
                    DependencySet set = dependencySets.get(id);
                    
                    query = "SELECT * FROM dependencies WHERE struct=" + id + ";";
                    
                    rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        int depid = rs.getInt("id");
                        String dependency = rs.getString("dependency");
                        String governor = rs.getString("governor");
                        String dependent = rs.getString("dependent");
                        
                        Dependency dep = new Dependency(dependency, governor, dependent, new LinkedList());
                        set.dependencies.add(dep);
                        dependencies.put(depid, dep);
                    }
                }
                
                for (Integer id : dependencies.keySet()) {
                    Dependency dep = dependencies.get(id);
                    
                    query = "SELECT * FROM specifications WHERE dependency=" + id + ";";
                    
                    rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        String specification = rs.getString("spec");
                        String expectation = rs.getString("expectation");
                        
                        dep.expectations.add(new Expectation(specification, expectation));
                    }
                }
                
                query = "SELECT * FROM dependency_meanings WHERE sense_uid=" + sense.getUid() + ";";

                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    String target = rs.getString("target");
                    String relation = rs.getString("relation");
                    String wim = rs.getString("wim");
                    String struct = rs.getString("structure");
                    
                    int structID = Integer.parseInt(struct);
                    dependencySets.get(structID).meanings.add(new Meaning(target, relation, wim));
                }
                
                query = "SELECT * FROM sense_meanings WHERE sense_uid=" + sense.getUid() + ";";
                
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    String target = rs.getString("target");
                    String relation = rs.getString("relation");
                    String wim = rs.getString("wim");
                    
                    sense.addMeaning(new Meaning(target, relation, wim));
                }
            }
            
            stmt.close();
            
        } catch (Exception err) {
            err.printStackTrace();
        }

        this.words.put(representation, word);
        
        return word;

    }
    
    public void addSense(Sense sense) {
        Word w = word(sense.word());
        w.addSense(sense);
    }
    
    public void removeSense(Sense sense) {
        Word w = word(sense.word());
        w.removeSense(sense);
    }
    
    public int nextInstanceNumber(String concept, String word, String pos) {
        int max = 0;
        
        Word w = word(word);
        for (Sense sense : w.listSenses()) {
            if (sense.concept().equalsIgnoreCase(concept)) {
                if (sense.pos().equalsIgnoreCase(pos)) {
                    if (sense.instance() > max) {
                        max = sense.instance();
                    }
                }
            }
        }
        
        return max + 1;
    }
}
