/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims;

import org.openwims.Objects.Lexicon.Lexicon;
import org.openwims.Objects.Lexicon.TagMaps;
import org.openwims.Objects.Lexicon.Templates;
import org.openwims.Objects.Ontology.Ontology;
import org.openwims.Processors.Microtheories.MicrotheoryManager;
import org.openwims.UI.MainJFrame;

/**
 *
 * @author jesse
 */
public class WIMGlobals {
    
    public static MainJFrame frame = null;
    private static Lexicon lexicon = null;
    private static Ontology ontology = null;
    private static TagMaps tagmaps = null;
    private static MicrotheoryManager microtheories = null;
    private static Templates templates = null;
    public static String graphdbpath = "/Users/ben/neo4j/data/graph.db/";
    public static String graphdbsettings = "/Users/ben/neo4j/conf/neo4j.properties";

    public static Templates templates() {
        if (WIMGlobals.templates == null) {
            WIMGlobals.templates = new Templates();
        }
        
        return WIMGlobals.templates;
    }
    
    public static MicrotheoryManager microtheories() {
        if (WIMGlobals.microtheories == null) {
            WIMGlobals.microtheories = new MicrotheoryManager();
        }
        
        return WIMGlobals.microtheories;
    }
    
    public static TagMaps tagmaps() {
        if (WIMGlobals.tagmaps == null) {
            WIMGlobals.tagmaps = new TagMaps();
        }
        
        return WIMGlobals.tagmaps;
    }
    
    public static Lexicon lexicon() {
        if (WIMGlobals.lexicon == null) {
            WIMGlobals.lexicon = new Lexicon();
        }
        
        return WIMGlobals.lexicon;
    }
    
    public static Ontology ontology() {
        if (WIMGlobals.ontology == null) {
            WIMGlobals.ontology = new Ontology();
        }
        
        return WIMGlobals.ontology;
    }
    
}
