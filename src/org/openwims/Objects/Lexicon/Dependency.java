/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

import java.util.HashMap;

/**
 *
 * @author jesse
 */
public class Dependency {
    public String type;
    public String governor;
    public String dependent;

    public HashMap<String, String> expectations;

    public Dependency(String type, String governor, String dependent, HashMap<String, String> expectations) {
        this.type = type;
        this.governor = governor;
        this.dependent = dependent;
        this.expectations = expectations;
    }

    @Override
    public String toString() {
        return this.type + "(" + this.governor + ", " + this.dependent + ")";
    }
}
