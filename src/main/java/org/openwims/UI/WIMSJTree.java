/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import com.jesseenglish.swingftfy.extensions.FNode;
import com.jesseenglish.swingftfy.extensions.FTree;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.WIM;
import org.openwims.Objects.WIMAttribute;
import org.openwims.Objects.WIMFrame;
import org.openwims.Objects.WIMRelation;

/**
 *
 * @author jesse
 */
public class WIMSJTree extends FTree {
    
    private static ImageIcon ROOT = new ImageIcon(WIMSJTree.class.getResource("/images/root.png"));
    private static ImageIcon FRAMES = new ImageIcon(WIMSJTree.class.getResource("/images/frames.png"));
    private static ImageIcon INFERRED = new ImageIcon(WIMSJTree.class.getResource("/images/inferred.png"));
    private static ImageIcon RELATION = new ImageIcon(WIMSJTree.class.getResource("/images/node-blue.png"));
    private static ImageIcon ATTRIBUTE = new ImageIcon(WIMSJTree.class.getResource("/images/node-green.png"));
    private static ImageIcon ANNOTATION = new ImageIcon(WIMSJTree.class.getResource("/images/node-black.png"));
    
    private LinkedList<WIM> wims;
    private LinkedList<SenseSelectionListener> listeners;

    public WIMSJTree(LinkedList<WIM> wims) {
        this.wims = wims;
        this.listeners = new LinkedList();
    
        WIMSNode root = new WIMSNode(wims);
        DefaultTreeModel model = new DefaultTreeModel(root);
        this.setModel(model);
        
        this.expandAll(true);
    }
    
    public void addSenseSelectionListener(SenseSelectionListener listener) {
        this.listeners.add(listener);
    }
    
    
    
    private class WIMSNode extends FNode {
        
        private LinkedList<WIM> wims;

        public WIMSNode(LinkedList<WIM> wims) {
            super("WIMs (" + wims.size() + " interpretations) - showing MAX 100");
            this.wims = wims;
            setIcon(FRAMES);
            
            int max = 100;
            if (wims.size() < 100) {
                max = wims.size();
            }
            
            for (int i = 0; i < max; i++) {
                this.add(new WIMNode(wims.get(i)));
            }
            
        }
        
    }
    
    private class WIMNode extends FNode {
        
        private WIM wim;

        public WIMNode(WIM wim) {
            super("WIM");
            this.wim = wim;
            setIcon(FRAMES);
            for (WIMFrame frame : wim.listFrames()) {
                this.add(new WIMFrameNode(frame));
            }
        }
        
    }
    
    private class WIMFrameNode extends FNode {
        
        private WIMFrame frame;

        public WIMFrameNode(WIMFrame frame) {
            super(frame.getName());
            this.frame = frame;
            setIcon(ROOT);
            
            if (frame.getName().charAt(0) == '*') {
                setIcon(INFERRED);
            }
            
            this.add(new WIMAnchorNode(frame.getAnchor().bestAnchor()));
            this.add(new WIMSenseNode(frame.getSense()));
            
            for (WIMAttribute attribute : frame.listAttributes()) {
                this.add(new WIMAttributeNode(attribute));
            }
            
            for (WIMRelation relation : frame.listRelations()) {
                this.add(new WIMRelationNode(relation));
            }
            
            for (WIMRelation inverse : frame.listInverses()) {
                this.add(new WIMRelationNode(inverse));
            }
        }
        
    }
    
    private class WIMAttributeNode extends FNode {
        
        private WIMAttribute attribute;

        public WIMAttributeNode(WIMAttribute attribute) {
            super(attribute.toString());
            this.attribute = attribute;
            setIcon(ATTRIBUTE);
        }
        
    }
    
    private class WIMRelationNode extends FNode {
        
        private WIMRelation relation;

        public WIMRelationNode(WIMRelation relation) {
            super(relation.toString());
            this.relation = relation;
            setIcon(RELATION);
        }
        
    }
    
    private class WIMAnchorNode extends FNode {
        
        private String anchor;

        public WIMAnchorNode(String anchor) {
            super("anchor: " + anchor);
            this.anchor = anchor;
            setIcon(ANNOTATION);
        }
        
    }
    
    private class WIMSenseNode extends FNode {
        
        private Sense sense;

        public WIMSenseNode(Sense sense) {
            super("sense: " + ((sense == null) ? "unknown" : sense.getId()));
            this.sense = sense;
            setIcon(ANNOTATION);
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (this.sense == null) {
                return;
            }
            
            if (SwingUtilities.isRightMouseButton(me)) {
            
                JPopupMenu menu = new JPopupMenu();
                menu.add(new ShowSenseJMenuItem(sense));

                Rectangle r = WIMSJTree.this.getPathBounds(WIMSJTree.this.getPath(this));
                menu.show(WIMSJTree.this, r.x, r.y + r.height);
            
            }
        }
        
    }
    
    private class ShowSenseJMenuItem extends JMenuItem implements ActionListener {
        
        private Sense sense;

        public ShowSenseJMenuItem(Sense sense) {
            super("Show " + sense.getId());
            this.sense = sense;
            
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            for (SenseSelectionListener listener : WIMSJTree.this.listeners) {
                listener.senseSelected(sense);
            }
        }
        
    }
    
    
    
    
    public interface SenseSelectionListener {
        public void senseSelected(Sense sense);
    }
    
}
