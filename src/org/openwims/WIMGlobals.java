/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims;

import org.openwims.Objects.Lexicon.Lexicon;
import org.openwims.Objects.Lexicon.TagMaps;
import org.openwims.Objects.Ontology.Ontology;

/**
 *
 * @author jesse
 */
public class WIMGlobals {
    
    private static Lexicon lexicon = null;
    private static Ontology ontology = null;
    private static TagMaps tagmaps = null;
    
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
