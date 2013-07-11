/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import com.jesseenglish.swingftfy.extensions.FNode;
import com.jesseenglish.swingftfy.extensions.FTree;
import java.awt.event.MouseEvent;
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
        load("@all");
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
            
            if (WIMGlobals.ontology().children(concept) == null) {
                return;
            }
            
            for (String child : WIMGlobals.ontology().children(concept)) {
                this.add(new OntologyConceptNode(child));
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            WIMGlobals.frame.setOntologyDefinition(WIMGlobals.ontology().definition(this.concept));
        }
        
    }
    
}
