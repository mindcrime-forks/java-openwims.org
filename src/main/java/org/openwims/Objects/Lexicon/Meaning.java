/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects.Lexicon;

/**
 *
 * @author jesse
 */
public class Meaning {
    public String target;
    public String relation;
    public String wim;

    public Meaning(String target, String relation, String wim) {
        this.target = target;
        this.relation = relation;
        this.wim = wim;
    }

    public boolean isAttribute() {
        return wim.contains("\"");
    }

    @Override
    public String toString() {
        return "[" + this.target + "." + this.relation + " " + this.wim + "]";
    }
}
