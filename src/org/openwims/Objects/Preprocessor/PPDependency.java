/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Preprocessor;

/**
 *
 * @author jesseenglish
 */
public class PPDependency {
    
    protected String type;
    protected PPToken governor;
    protected PPToken dependent;

    public PPDependency() {
        this.type = "";
        this.governor = null;
        this.dependent = null;
    }
    
    public String getType() {
        return this.type;
    }
    
    public PPToken getGovernor() {
        return this.governor;
    }
    
    public PPToken getDependent() {
        return this.dependent;
    }

    @Override
    public String toString() {
        return this.type + "(" + this.governor.anchor() + ", " + this.dependent.anchor() + ")";
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setGovernor(PPToken governor) {
        this.governor = governor;
    }
    
    public void setDependent(PPToken dependent) {
        this.dependent = dependent;
    }
    
}
