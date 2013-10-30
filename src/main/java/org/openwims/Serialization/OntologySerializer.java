/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.Serialization;

import org.openwims.Objects.Ontology.Concept;

/**
 *
 * @author jesseenglish
 */
public interface OntologySerializer {
    
    public void saveConcept(Concept concept) throws Exception;
    
}
