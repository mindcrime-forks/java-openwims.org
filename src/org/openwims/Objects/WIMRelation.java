/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects;

/**
 *
 * @author jesse
 */
public class WIMRelation {
    
    private String relation;
    private WIMFrame frame;

    public WIMRelation(String relation, WIMFrame frame) {
        this.relation = relation;
        this.frame = frame;
    }

    public WIMFrame getFrame() {
        return frame;
    }

    public String getRelation() {
        return relation;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        
        out.append(this.relation);
        out.append(": ");
        out.append(this.frame.getName());
        
        return out.toString().trim();
    }
    
}
