/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Scripts;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.Meaning;

/**
 *
 * @author jesse
 */
public class ImportWordNet {
    
    private static HashMap<String, Integer> indexes;
    private static HashMap<String, VerbTemplate> templates;
    private static int depID = 1;
    private static int specID = 1;
    private static int meaningID = 1;
    private static int wnetID = 1;
    
    public static void main(String[] args) throws Exception {
        
        loadTemplates();
        
        indexes = new HashMap();
        
        Class.forName("org.sqlite.JDBC");            
        Connection conn = DriverManager.getConnection("jdbc:sqlite:wims.sql");
        Statement stmt = conn.createStatement();
        
        FileInputStream fstream = new FileInputStream("/Users/jesse/Desktop/wordnet.out");
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        
        
        File file = new File("/Users/jesse/Desktop/wimssql.dump");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        
        String line;
        int i = 0;
        while ((line = br.readLine()) != null)   {
            String sql = parse(line); 
            
            i++;
            
            if (i % 500 == 0) {
                System.out.println("LINE: " + i);
            }
            
            if (sql.trim().equalsIgnoreCase("")) {
                continue;
            }
            
            bw.write(sql);
            
//            String[] queries = sql.split("\n");
//            for (String query : queries) {
//                stmt.addBatch(query);
//            }
//                
//            stmt.executeBatch();
        }

        bw.close();
        in.close();
        
        stmt.close();
        conn.close();
    }
    
    public static String parse(String line) throws Exception { 
        StringBuilder out = new StringBuilder();
        
        int divide = line.indexOf(")");
        String wnlemma = line.substring(0, divide).replaceAll("Lemma\\(", "").trim();
        if (wnlemma.charAt(0) == '\'') {
            wnlemma = wnlemma.replaceFirst("'", "");
        }

        if (wnlemma.charAt(wnlemma.length() - 1) == '\'') {
            wnlemma = wnlemma.substring(0, wnlemma.length() - 1);
        }
        
        String frames = line.substring(divide + 1, line.length()).trim();
        
        String[] wnlemmaParts = wnlemma.split("\\.");
        String lemma = "";
        for (int i = 3; i < wnlemmaParts.length; i++) {
            lemma = lemma + wnlemmaParts[i];
            if (i < wnlemmaParts.length - 1) {
                lemma = lemma + ".";
            }
        }
        if (wnlemma.endsWith(".")) {
            lemma += ".";
        }
        
        String concept = wnlemmaParts[0];
        String pos = posMap(wnlemmaParts[1]);
        
        
        
        
        
        
        out.append("DELETE FROM tokens WHERE representation='" + lemma.replaceAll("'", "''") + "';\n");
        out.append("INSERT INTO tokens (representation) VALUES ('" + lemma.replaceAll("'", "''") + "');\n");
        
        
        
        frames = frames.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(", ", " ").replaceAll("\"", "'").trim();
        String[] frameParts = frames.split("' '");
        for (String framePart : frameParts) {
            
            int index = nextIndex(lemma, pos);
            String sense = "@" + concept + ":" + lemma + "-" + pos + "-" + index;
            out.append("INSERT INTO senses (id, token) VALUES ('" + sense.replaceAll("'", "''") + "', '" + lemma.replaceAll("'", "''") + "');\n");
            out.append("INSERT INTO wordnet (id, wordnet_sense, sense) VALUES (" + wnetID + ", '" + wnlemma.replaceAll("'", "''") + "', '" + sense.replaceAll("'", "''") + "');\n");
            wnetID++;
            
            if (framePart.length() == 0) {
                continue;
            }
            
            if (framePart.charAt(0) == '\'') {
                framePart = framePart.replaceFirst("'", "");
            }
            
            if (framePart.charAt(framePart.length() - 1) == '\'') {
                framePart = framePart.substring(0, framePart.length() - 1);
            }
            
            
            VerbTemplate template = templates.get(framePart.replaceAll(lemma, "----s"));
            if (template == null) {
                template = templates.get(framePart.replaceAll(lemma, "----ing"));
            }
            if (template == null) {
                template = templates.get(framePart.replaceAll(lemma, "----"));
            }
            
            if (template == null) {
                System.out.println(sense + "\n");
            } else {
                out.append(template.sql(sense));
            }
        }
        
//        if (lemma.equalsIgnoreCase("man") || lemma.equalsIgnoreCase("hit") || lemma.equalsIgnoreCase("building")) {
//            return out.toString();
//        } else {
//            return "";
//        }
        
        return out.toString();
    }
    
    public static int nextIndex(String lemma, String pos) {
        Integer index = indexes.get(lemma + "-" + pos);
        if (index == null) {
            index = 0;
        }
        
        index++;
        indexes.put(lemma + "-" + pos, index);
        return index;
    }
    
    public static String posMap(String pos) {
        if (pos.equalsIgnoreCase("n")) {
            return "n";
        }
        if (pos.equalsIgnoreCase("v")) {
            return "v";
        }
        if (pos.equalsIgnoreCase("a")) {
            return "adj";
        }
        if (pos.equalsIgnoreCase("s")) {
            return "adj";
        }
        if (pos.equalsIgnoreCase("r")) {
            return "adv";
        }
        return "unknown";
    }
    
    public static void loadTemplates() throws Exception {
        templates = new HashMap();
        
        InputStream in = ImportWordNet.class.getResourceAsStream("/org/openwims/Assets/verbtemplates");
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        String line = null;
        VerbTemplate template = null;
        
        try {
            while ((line = input.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("")) {
                    template = null;
                    continue;
                }
                
                if (template == null) {
                    template = new VerbTemplate();
                    template.frame = line.trim();
                    templates.put(template.frame, template);
                    continue;
                }
                
                if (line.contains("(")) {
                    //nsubj ( SELF , agent[pos=NN,ont=@somebody] )
                    line = line.replaceAll(" \\( ", " ").replaceAll(" , ", " ").replaceAll(" \\)", "").trim();
                    String[] parts = line.split(" ");
                    
                    String type = parts[0];
                    String gov = parts[1];
                    String fullDep = parts[2];
                    
                    fullDep = fullDep.replaceAll("\\[", " ").replaceAll(",", " ").replaceAll("\\]", "").trim();
                    String[] depParts = fullDep.split(" ");
                    
                    String dep = depParts[0];
                    
                    Dependency dependency = new Dependency(type, gov, dep, new HashMap());
                    
                    for (int i = 1; i < depParts.length; i++) {
                        String spec = depParts[i];
                        String[] specParts = spec.split("=");
                        String specification = specParts[0];
                        String expectation = specParts[1];
                        
                        dependency.expectations.put(specification, expectation);
                    }
                    
                    template.dependencies.add(dependency);
                    
                } else if (line.charAt(0) == '[') {
                    //[SELF.beneficiary beneficiary]
                    line = line.replaceAll("\\[", "").replaceAll("\\]", "").trim();
                    String[] parts = line.trim().split(" ");
                    String[] leftParts = parts[0].split("\\.");
                    String target = leftParts[0];
                    String relation = leftParts[1];
                    String wim = parts[1];
                    
                    template.meanings.add(new Meaning(target, relation, wim));
                } else {
                    continue;
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
            System.out.println(line);
        }
    }
    
    private static class VerbTemplate {
        public String frame;
        public LinkedList<Dependency> dependencies;
        public LinkedList<Meaning> meanings;

        public VerbTemplate() {
            this.frame = "";
            this.dependencies = new LinkedList();
            this.meanings = new LinkedList();
        }
        
        public String sql(String sense) {
            //INSERT INTO structures (id, sense, series, dependency, governor, dependent) VALUES (1, '@hit:hit-v-1', 1, 'nsubj', 'SELF', 'agent');
            //INSERT INTO specifications (id, struct, spec, expectation) VALUES (1, 1, 'pos', 'NN');
            //INSERT INTO meanings (sense, target, relation, wim) VALUES ('@hit:hit-v-1', 'SELF', 'agent', 'agent');
            
            String out = "";
            
            for (Dependency dependency : dependencies) {
                out += "INSERT INTO structures (id, sense, series, dependency, governor, dependent) VALUES (" 
                        + depID + ", '" 
                        + sense.replaceAll("'", "''") + "', 1, '"
                        + dependency.type.replaceAll("'", "''") + "', '"
                        + dependency.governor.replaceAll("'", "''") + "', '"
                        + dependency.dependent.replaceAll("'", "''") + "');\n";
                for (String specification : dependency.expectations.keySet()) {
                    out += "INSERT INTO specifications (id, struct, spec, expectation) VALUES (" + specID + ", " + depID + ", '" + specification.replaceAll("'", "''") + "', '" + dependency.expectations.get(specification).replaceAll("'", "''") + "');\n";
                    specID++;
                }
                depID++;
            }
            
            for (Meaning meaning : meanings) {
                out += "INSERT INTO meanings (id, sense, target, relation, wim) VALUES (" + meaningID + ", '"
                        + sense.replaceAll("'", "''") + "', '"
                        + meaning.target.replaceAll("'", "''") + "', '"
                        + meaning.relation.replaceAll("'", "''") + "', '"
                        + meaning.wim.replaceAll("'", "''") + "');\n";
                meaningID++;
            }
            
            return out;
        }
        
    }
    
}
