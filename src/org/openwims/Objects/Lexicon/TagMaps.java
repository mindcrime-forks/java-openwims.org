/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import org.openwims.Processors.NaiveDisambiguation;

/**
 *
 * @author jesseenglish
 */
public class TagMaps {
    
    private HashMap<String, LinkedList<String>> maps;
    
    public TagMaps() {
        this.maps = new HashMap();
        
        InputStream in = NaiveDisambiguation.class.getResourceAsStream("/org/openwims/Assets/tagmaps");
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        String line = null;
        LinkedList<String> map = null;

        try {
            while ((line = input.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("")) {
                    map = null;
                    continue;
                }

                String tag = line.split("-")[0].trim();

                if (map == null) {
                    map = new LinkedList();
                    this.maps.put(tag.toUpperCase(), map);
                }

                map.add(tag.toUpperCase());
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
    
    public boolean doTagsMatch(String tag1, String tag2) {
        if (tag1.equalsIgnoreCase(tag2)) {
            return true;
        }
        
        tag1 = tag1.toUpperCase();
        tag2 = tag2.toUpperCase();
        
        if (this.maps.containsKey(tag1) && this.maps.get(tag1).contains(tag2)) {
            return true;
        }
        
        if (this.maps.containsKey(tag2) && this.maps.get(tag2).contains(tag1)) {
            return true;
        }
        
        if (this.maps.containsKey(tag1)) {
            for (String child : this.maps.get(tag1)) {
                if (child.equals(tag1)) {
                    continue;
                }

                if (doTagsMatch(child, tag2)) {
                    return true;
                }
            }
        } else {
            System.out.println(tag1 + " not found in tagmaps");
        }
        
        if (this.maps.containsKey(tag2)) {
            for (String child : this.maps.get(tag2)) {
                if (child.equals(tag2)) {
                    continue;
                }

                if (doTagsMatch(child, tag1)) {
                    return true;
                }
            }
        } else {
            System.out.println(tag2 + " not found in tagmaps");
        }
        
        return false;
    }
    
}
