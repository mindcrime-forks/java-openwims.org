/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import com.jesseenglish.swingftfy.extensions.FDialog;
import com.jesseenglish.swingftfy.extensions.FNode;
import com.jesseenglish.swingftfy.extensions.FTree;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import org.openwims.Objects.Ontology.Concept;
import org.openwims.UI.Editors.ParentEditorJPanel;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesse
 */
public class OntologyJTree extends FTree {
    
    private static ImageIcon ROOT = new ImageIcon(OntologyJTree.class.getResource("/images/root.png"));
    private static ImageIcon PATH = new ImageIcon(WIMSJTree.class.getResource("/images/node-blue.png"));
    
    public OntologyJTree() {
        load(WIMGlobals.ontology().concept("@all"));
    }
    
    public void load(Concept concept) {
        OntologyConceptNode root = new OntologyConceptNode(concept);
        
        DefaultTreeModel model = new DefaultTreeModel(root);
        this.setModel(model);
        
        this.validate();
        this.repaint();
    }
    
    private class OntologyConceptNode extends FNode {
        
        private Concept concept;

        public OntologyConceptNode(Concept concept) {
            super(concept.getName());
            this.concept = concept;
            setIcon(ROOT);
            
            for (Concept child : concept.listChildren()) {
                this.add(new OntologyConceptNode(child));
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            WIMGlobals.frame.setOntologyDefinition(concept.getDefinition());
            
            if (SwingUtilities.isRightMouseButton(me)) {
                JPopupMenu menu = new JPopupMenu();
                menu.add(new ChangeParentJMenuItem(concept));
                
                Rectangle r = OntologyJTree.this.getPathBounds(OntologyJTree.this.getPath(this));
                menu.show(OntologyJTree.this, r.x, r.y + r.height);
            }
        }
        
    }
    
    private class ChangeParentJMenuItem extends JMenuItem implements ActionListener {
        
        private Concept concept;

        public ChangeParentJMenuItem(Concept concept) {
            super("Change Parent");
            this.concept = concept;
            this.addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            ParentEditorJPanel editor = new ParentEditorJPanel(concept);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(300, 80);
            d.add(editor);
            d.center();
            d.setVisible(true);
            
            OntologyJTree.this.load(WIMGlobals.ontology().concept("@all"));
        }
        
    }
    
}
