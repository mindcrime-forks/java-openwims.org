/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author jesse
 */
public class Dependency {
    public String type;
    public String governor;
    public String dependent;

    public LinkedList<Expectation> expectations;

    public Dependency(String type, String governor, String dependent, LinkedList<Expectation> expectations) {
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
