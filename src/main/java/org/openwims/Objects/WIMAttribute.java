/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Objects;

/**
 *
 * @author jesse
 */
public class WIMAttribute {
    
    private String attribute;
    private String value;

    public WIMAttribute(String attribute, String value) {
        this.attribute = attribute;
        this.value = value;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }
    
    public String json() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("{\"attribute\": \"");
        builder.append(this.attribute);
        builder.append("\", \"value\": \"");
        builder.append(this.value);
        builder.append("\"}");
        
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        
        out.append(this.attribute);
        out.append(": ");
        out.append(this.value);
        
        return out.toString().trim();
    }
    
}
