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
    
    public Dependency(Dependency toCopy) {
        this.type = toCopy.type;
        this.governor = toCopy.governor;
        this.dependent = toCopy.dependent;
        this.expectations = new LinkedList();
        
        for (Expectation expectation : toCopy.expectations) {
            this.expectations.add(new Expectation(expectation));
        }
    }

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

    public HashMap<String, String> getExpectationsAsMap() {
        HashMap<String, String> output = new HashMap();
        for (Expectation expectation : expectations) {
            output.put(expectation.getSpecification(), expectation.getExpectation());
        }
        return output;
    }
}
