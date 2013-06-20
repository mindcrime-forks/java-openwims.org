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
import org.openwims.Objects.Lexicon.*;

/**
 *
 * @author jesse
 */
public class SenseJTree extends FTree {
    
    private static ImageIcon ROOT = new ImageIcon(SenseJTree.class.getResource("/images/root.png"));
    private static ImageIcon NODE = new ImageIcon(SenseJTree.class.getResource("/images/node-blue.png"));
    
    private Sense sense;

    public SenseJTree(Sense sense) {
        this.sense = sense;
        refresh();
    }
    
    private void refresh() {
        SenseNode root = new SenseNode(sense);
        
        DefaultTreeModel model = new DefaultTreeModel(root);
        this.setModel(model);
        
        this.validate();
        this.repaint();;
    }
    
    private class SenseNode extends FNode {
        
        private Sense sense;

        public SenseNode(Sense sense) {
            super(sense.getId());
            this.sense = sense;
            setIcon(ROOT);
            
            this.add(new SenseJTree.SenseMeaningNode(sense));
            for (Structure structure : sense.listStructures()) {
                this.add(new SenseJTree.SenseStructureNode(structure));
            }
        }
        
    }
    
    private class SenseMeaningNode extends FNode {
        
        private Sense sense;

        public SenseMeaningNode(Sense sense) {
            super("Meaning");
            this.sense = sense;
            setIcon(ROOT);
            
            for (Meaning meaning : sense.listMeanings()) {
                this.add(new SenseJTree.MeaningRelationNode(meaning));
            }
        }
        
    }
    
    private class MeaningRelationNode extends FNode {
        
        private Meaning meaning;

        public MeaningRelationNode(Meaning meaning) {
            super(meaning);
            this.meaning = meaning;
            setIcon(NODE);
        }
        
        
        
    }
    
    private class SenseStructureNode extends FNode {
        
        private Structure structure;

        public SenseStructureNode(Structure structure) {
            super("Structure");
            this.structure = structure;
            setIcon(ROOT);
            
            for (DependencySet set : structure.listDependencies()) {
                this.add(new SenseJTree.DependencySetNode(set));
            }
        }
        
    }
    
    private class DependencySetNode extends FNode {
        
        private DependencySet set;

        public DependencySetNode(DependencySet set) {
            super(set.label);
            this.set = set;
            setIcon(ROOT);
            
            for (Dependency dependency : set.dependencies) {
                this.add(new SenseJTree.DependencyNode(dependency));
            }
        }
        
    }
    
    private class DependencyNode extends FNode {
        
        private Dependency dependency;

        public DependencyNode(Dependency dependency) {
            super(dependency);
            this.dependency = dependency;
            setIcon(NODE);
            
            for (String specification : dependency.expectations.keySet()) {
                this.add(new SenseJTree.DepenendencyExpectationNode(specification, dependency.expectations.get(specification)));
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (this.dependency == null) {
                return;
            }
            
            if (SwingUtilities.isRightMouseButton(me)) {
            
                JPopupMenu menu = new JPopupMenu();
                menu.add(new AddExpectationJMenuItem(dependency));

                Rectangle r = SenseJTree.this.getPathBounds(SenseJTree.this.getPath(this));
                menu.show(SenseJTree.this, r.x, r.y + r.height);
            
            }
        }
        
    }
    
    private class DepenendencyExpectationNode extends FNode {
        
        private String specification;
        private String expectation;

        public DepenendencyExpectationNode(String specification, String expectation) {
            super(specification + ": " + expectation);
            this.specification = specification;
            this.expectation = expectation;
            setIcon(NODE);
        }
        
    }
    
    private class AddExpectationJMenuItem extends JMenuItem implements ActionListener {
        
        private Dependency dependency;

        public AddExpectationJMenuItem(Dependency dependency) {
            super("Add Expectation");
            this.dependency = dependency;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            ExpectationEditorJPanel e = new ExpectationEditorJPanel();
            
            FDialog d =  new FDialog();
            d.setModal(true);
            d.setSize(250, 100);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            dependency.expectations.put(e.getSpecification(), e.getExpectation());
            
            SenseJTree.this.refresh();
        }
        
    }
    
}
