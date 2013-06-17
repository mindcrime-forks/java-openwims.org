/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import com.jesseenglish.swingftfy.extensions.FNode;
import com.jesseenglish.swingftfy.extensions.FTree;
import java.util.LinkedList;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesse
 */
public class OntologyJTree extends FTree {
    
    private static ImageIcon ROOT = new ImageIcon(OntologyJTree.class.getResource("/images/root.png"));
    private static ImageIcon PATH = new ImageIcon(WIMSJTree.class.getResource("/images/node-blue.png"));
    
    public OntologyJTree() {
        load("");
    }
    
    public void load(String concept) {
        OntologyConceptNode root = new OntologyConceptNode(concept);
        
        DefaultTreeModel model = new DefaultTreeModel(root);
        this.setModel(model);
        
        this.validate();
        this.repaint();
    }
    
    private class OntologyConceptNode extends FNode {
        
        private String concept;

        public OntologyConceptNode(String concept) {
            super(concept);
            this.concept = concept;
            setIcon(ROOT);
            
            if (WIMGlobals.ontology().ancestors(concept) == null) {
                return;
            }
            
            for (LinkedList<String> path : WIMGlobals.ontology().ancestors(concept).paths) {
                this.add(new OntologyPathNode(path));
            }
        }
        
    }
    
    private class OntologyPathNode extends FNode {
        
        private LinkedList<String> path;

        public OntologyPathNode(LinkedList<String> path) {
            super(path.toString());
            this.path = path;
            setIcon(PATH);
        }
        
    }
    
}
