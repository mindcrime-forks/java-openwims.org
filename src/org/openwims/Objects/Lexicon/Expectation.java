/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

/**
 *
 * @author jesseenglish
 */
public class Expectation {
    
    private String specification;
    private String expectation;

    public Expectation(String specification, String expectation) {
        this.specification = specification;
        this.expectation = expectation;
    }

    public String getExpectation() {
        return expectation;
    }

    public String getSpecification() {
        return specification;
    }

    public void setExpectation(String expectation) {
        this.expectation = expectation;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    @Override
    public String toString() {
        return this.specification + " = " + this.expectation;
    }
    
}
