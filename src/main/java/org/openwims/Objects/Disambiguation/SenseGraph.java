/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Disambiguation;

import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 * @deprecated 
 * @author jesseenglish
 */
public class SenseGraph {

    public HashMap<PPToken, LinkedList<SenseMapping>> verbs;
    public LinkedList<InterpretationSet> interpretations;

    public SenseGraph() {
        this.verbs = new HashMap();
        this.interpretations = new LinkedList();
    }

    public void map(PPToken token, SenseMapping map) {
        LinkedList<SenseMapping> maps = this.verbs.get(token);
        if (maps == null) {
            maps = new LinkedList();
            this.verbs.put(token, maps);
        }
        maps.add(map);
    }
    
}
