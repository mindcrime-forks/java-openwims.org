/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import org.openwims.UI.Editors.ExpectationEditorJPanel;
import com.jesseenglish.swingftfy.extensions.FDialog;
import com.jesseenglish.swingftfy.extensions.FNode;
import com.jesseenglish.swingftfy.extensions.FTree;
import edu.stanford.nlp.util.ArraySet;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeModel;
import org.openwims.Objects.Lexicon.*;
import org.openwims.UI.Editors.DefinitionEditorJPanel;
import org.openwims.UI.Editors.DependencyEditorJPanel;
import org.openwims.UI.Editors.DependencySetEditorJPanel;
import org.openwims.UI.Editors.ExampleEditorJPanel;
import org.openwims.UI.Editors.FrequencyEditorJPanel;
import org.openwims.UI.Editors.MeaningEditorJPanel;
import org.openwims.UI.Editors.RemapConceptJPanel;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesse
 */
public class SenseJTree extends FTree {
    
    private static ImageIcon ROOT = new ImageIcon(SenseJTree.class.getResource("/images/root.png"));
    private static ImageIcon NODE = new ImageIcon(SenseJTree.class.getResource("/images/node-blue.png"));
    private static ImageIcon MEANING = new ImageIcon(SenseJTree.class.getResource("/images/node-black.png"));
    private static ImageIcon DEFINITION = new ImageIcon(SenseJTree.class.getResource("/images/definition.png"));
    private static ImageIcon EXAMPLE = new ImageIcon(SenseJTree.class.getResource("/images/example.png"));
    private static ImageIcon MANDATORY = new ImageIcon(SenseJTree.class.getResource("/images/mandatory.png"));
    private static ImageIcon OPTIONAL = new ImageIcon(SenseJTree.class.getResource("/images/optional.png"));
    private static ImageIcon FREQUENCY = new ImageIcon(SenseJTree.class.getResource("/images/frequency.png"));
    
    private Sense sense;
    private ArraySet<Object> expanded;
    
    public SenseJTree(Sense sense) {
        this.sense = sense;
        this.expanded = new ArraySet();
        
        this.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent tee) {
                if (tee.getPath().getLastPathComponent() instanceof ExpansionMemoryNode) {
                    ExpansionMemoryNode memory = (ExpansionMemoryNode) tee.getPath().getLastPathComponent();
                    expanded.add(memory.recall());
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent tee) {
                if (tee.getPath().getLastPathComponent() instanceof ExpansionMemoryNode) {
                    ExpansionMemoryNode memory = (ExpansionMemoryNode) tee.getPath().getLastPathComponent();
                    expanded.remove(memory.recall());
                }
            }
        });
        
        refresh();
    }
    
    private void refresh() {
        this.expanded.add(this.sense);
        
        SenseNode root = new SenseNode(sense);
        
        DefaultTreeModel model = new DefaultTreeModel(root);
        this.setModel(model);
        
        reExpand(root);
        
        this.validate();
        this.repaint();;
    }
    
    private void reExpand(ExpansionMemoryNode node) {
        if (this.expanded.contains(node.recall())) {
            this.expandNode(node);
            
            for (int i = 0; i < node.getChildCount(); i++) {
                reExpand((ExpansionMemoryNode)node.getChildAt(i));
            }
        }
    }
    
    private abstract class ExpansionMemoryNode extends FNode {
        
        public ExpansionMemoryNode(Object userObject) {
            super(userObject);
        }
        
        public abstract Object recall();
    }

    private class SenseNode extends ExpansionMemoryNode {
        
        private Sense sense;

        public SenseNode(Sense sense) {
            super(sense.getId());
            this.sense = sense;
            setIcon(ROOT);
            
            this.add(new SenseJTree.DefinitionNode(this.sense));
            this.add(new SenseJTree.ExampleNode(this.sense));
            this.add(new SenseJTree.FrequencyNode(this.sense));
            
            for (Meaning meaning : sense.listMeanings()) {
                this.add(new SenseJTree.MeaningRelationNode(this.sense, meaning));
            }
            
            for (DependencySet dependencySet : sense.listDependencySets()) {
                this.add(new SenseJTree.DependencySetNode(dependencySet));
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (SwingUtilities.isRightMouseButton(me)) {
            
                JPopupMenu menu = new JPopupMenu();
                menu.add(new AddMeaningJMenuItem(this.sense));
                menu.add(new AddDependencySetJMenuItem());
                menu.addSeparator();
                menu.add(new RemapJMenuItem(this.sense));
                menu.add(new SaveJMenuItem(this.sense));

                Rectangle r = SenseJTree.this.getPathBounds(SenseJTree.this.getPath(this));
                menu.show(SenseJTree.this, r.x + getIcon().getIconWidth(), r.y + r.height);
            }
        }

        @Override
        public Object recall() {
            return this.sense;
        }
        
    }
    
    private class DefinitionNode extends ExpansionMemoryNode {
        
        private Sense sense;
        
        public DefinitionNode(Sense sense) {
            super(sense.getDefinition());
            this.sense = sense;
            setIcon(DEFINITION);
        }
        
        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (SwingUtilities.isRightMouseButton(me)) {
            
                JPopupMenu menu = new JPopupMenu();
                menu.add(new EditDefinitionJMenuItem(this.sense));
                
                Rectangle r = SenseJTree.this.getPathBounds(SenseJTree.this.getPath(this));
                menu.show(SenseJTree.this, r.x + getIcon().getIconWidth(), r.y + r.height);
            }
        }

        @Override
        public Object recall() {
            return this.sense.getDefinition();
        }
    }
    
    private class ExampleNode extends ExpansionMemoryNode {
        
        private Sense sense;
        
        public ExampleNode(Sense sense) {
            super(sense.getExample());
            this.sense = sense;
            setIcon(EXAMPLE);
        }
        
        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (SwingUtilities.isRightMouseButton(me)) {
            
                JPopupMenu menu = new JPopupMenu();
                menu.add(new EditExampleJMenuItem(this.sense));
                
                Rectangle r = SenseJTree.this.getPathBounds(SenseJTree.this.getPath(this));
                menu.show(SenseJTree.this, r.x + getIcon().getIconWidth(), r.y + r.height);
            }
        }

        @Override
        public Object recall() {
            return this.sense.getExample();
        }
    }
    
    private class FrequencyNode extends ExpansionMemoryNode {
        
        private Sense sense;
        
        public FrequencyNode(Sense sense) {
            super(sense.getFrequency());
            this.sense = sense;
            setIcon(FREQUENCY);
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (SwingUtilities.isRightMouseButton(me)) {
                JPopupMenu menu = new JPopupMenu();
                menu.add(new EditFrequencyJMenuItem(this.sense));
                
                Rectangle r = SenseJTree.this.getPathBounds(SenseJTree.this.getPath(this));
                menu.show(SenseJTree.this, r.x + getIcon().getIconWidth(), r.y + r.height);
            }
        }

        @Override
        public Object recall() {
            return null;
        }
        
    }
    
    private class MeaningRelationNode extends ExpansionMemoryNode {
        
        private Sense sense = null;
        private DependencySet set = null;
        private Meaning meaning;

        public MeaningRelationNode(Sense sense, Meaning meaning) {
            super(meaning);
            this.sense = sense;
            this.meaning = meaning;
            setIcon(MEANING);
        }
        
        public MeaningRelationNode(DependencySet set, Meaning meaning) {
            super(meaning);
            this.set = set;
            this.meaning = meaning;
            setIcon(MEANING);
        }
        
        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (SwingUtilities.isRightMouseButton(me)) {
            
                JPopupMenu menu = new JPopupMenu();
                menu.add(new EditMeaningJMenuItem(this.meaning));
                
                if (this.sense != null) {
                    menu.add(new DeleteMeaningJMenuItem(this.sense, this.meaning));
                } else if (this.set != null) {
                    menu.add(new DeleteMeaningJMenuItem(this.set, this.meaning));
                }

                Rectangle r = SenseJTree.this.getPathBounds(SenseJTree.this.getPath(this));
                menu.show(SenseJTree.this, r.x + getIcon().getIconWidth(), r.y + r.height);
            }
        }

        @Override
        public Object recall() {
            return this.meaning;
        }
        
    }
    
    private class DependencySetNode extends ExpansionMemoryNode {
        
        private DependencySet set;

        public DependencySetNode(DependencySet set) {
            super((set.optional ? "+" : "") + set.label);
            this.set = set;
            
            if (set.optional) {
                setIcon(OPTIONAL);
            } else {
                setIcon(MANDATORY);
            }
            
            for (Dependency dependency : set.dependencies) {
                this.add(new SenseJTree.DependencyNode(this.set, dependency));
            }
            
            for (Meaning meaning : set.meanings) {
                this.add(new SenseJTree.MeaningRelationNode(this.set, meaning));
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (SwingUtilities.isRightMouseButton(me)) {
            
                JPopupMenu menu = new JPopupMenu();
                menu.add(new AddDependencyJMenuItem(this.set));
                menu.add(new AddMeaningJMenuItem(this.set));
                menu.addSeparator();
                menu.add(new EditDependencySetJMenuItem(this.set));
                menu.add(new DeleteDependencySetJMenuItem(this.set));
                
                Rectangle r = SenseJTree.this.getPathBounds(SenseJTree.this.getPath(this));
                menu.show(SenseJTree.this, r.x + getIcon().getIconWidth(), r.y + r.height);
            }
        }

        @Override
        public Object recall() {
            return this.set;
        }
        
    }
    
    private class DependencyNode extends ExpansionMemoryNode {
        
        private DependencySet set;
        private Dependency dependency;

        public DependencyNode(DependencySet set, Dependency dependency) {
            super(dependency);
            this.set = set;
            this.dependency = dependency;
            setIcon(NODE);
            
            for (Expectation expectation : dependency.expectations) {
                this.add(new SenseJTree.ExpectationNode(this.dependency, expectation));
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
                menu.add(new AddExpectationJMenuItem(this.dependency));
                menu.addSeparator();
                menu.add(new EditDependencyJMenuItem(this.dependency));
                menu.add(new DeleteDependencyJMenuItem(this.set, this.dependency));

                Rectangle r = SenseJTree.this.getPathBounds(SenseJTree.this.getPath(this));
                menu.show(SenseJTree.this, r.x + getIcon().getIconWidth(), r.y + r.height);
            }
        }

        @Override
        public Object recall() {
            return this.dependency;
        }
        
    }
    
    private class ExpectationNode extends ExpansionMemoryNode {
        
        private Dependency dependency;
        private Expectation expectation;

        public ExpectationNode(Dependency dependency, Expectation expectation) {
            super(expectation.toString());
            this.dependency = dependency;
            this.expectation = expectation;
            setIcon(NODE);
        }
        
        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (SwingUtilities.isRightMouseButton(me)) {
            
                JPopupMenu menu = new JPopupMenu();
                menu.add(new EditExpectationJMenuItem(this.expectation));
                menu.add(new DeleteExpectationJMenuItem(this.dependency, this.expectation));
                
                Rectangle r = SenseJTree.this.getPathBounds(SenseJTree.this.getPath(this));
                menu.show(SenseJTree.this, r.x + getIcon().getIconWidth(), r.y + r.height);
            }
        }

        @Override
        public Object recall() {
            return this.expectation;
        }
        
    }
    
    private class SaveJMenuItem extends JMenuItem implements ActionListener {
        
        private Sense sense;

        public SaveJMenuItem(Sense sense) {
            super("Save Changes");
            this.sense = sense;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                WIMGlobals.lexicon().serializer().saveSense(this.sense);
            } catch (Exception ex) {
                Logger.getLogger(SenseJTree.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    private class RemapJMenuItem extends JMenuItem implements ActionListener {
        
        private Sense sense;

        public RemapJMenuItem(Sense sense) {
            super("Remap Concept");
            this.sense = sense;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            RemapConceptJPanel e = new RemapConceptJPanel(this.sense);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(300, 80);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            SenseJTree.this.refresh();
        }
        
    }
    
    private class DeleteExpectationJMenuItem extends JMenuItem implements ActionListener {
        
        private Dependency dependency;
        private Expectation expectation;

        public DeleteExpectationJMenuItem(Dependency dependency, Expectation expectation) {
            super("Delete Expectation");
            this.dependency = dependency;
            this.expectation = expectation;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            this.dependency.expectations.remove(this.expectation);
            SenseJTree.this.refresh();
        }
        
    }
    
    private class EditExpectationJMenuItem extends JMenuItem implements ActionListener {
        
        private Expectation expectation;

        public EditExpectationJMenuItem(Expectation expectation) {
            super("Edit Expectation");
            this.expectation = expectation;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            ExpectationEditorJPanel e = new ExpectationEditorJPanel(this.expectation);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 110);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            SenseJTree.this.refresh();
        }
        
    }
    
    private class EditDefinitionJMenuItem extends JMenuItem implements ActionListener {
        
        private Sense sense;

        public EditDefinitionJMenuItem(Sense sense) {
            super("Edit Definition");
            this.sense = sense;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            DefinitionEditorJPanel e = new DefinitionEditorJPanel(this.sense);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 50);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            SenseJTree.this.refresh();
        }
        
    }
    
    private class EditExampleJMenuItem extends JMenuItem implements ActionListener {
        
        private Sense sense;

        public EditExampleJMenuItem(Sense sense) {
            super("Edit Example");
            this.sense = sense;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            ExampleEditorJPanel e = new ExampleEditorJPanel(this.sense);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 50);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            SenseJTree.this.refresh();
        }
        
    }
    
    private class EditFrequencyJMenuItem extends JMenuItem implements ActionListener {
        
        private Sense sense;
        
        public EditFrequencyJMenuItem(Sense sense) {
            super("Edit Frequency");
            this.sense = sense;
            this.addActionListener(this);
        }

        public void actionPerformed(ActionEvent ae) {
            FrequencyEditorJPanel e = new FrequencyEditorJPanel(this.sense);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 50);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            SenseJTree.this.refresh();
        }
        
    }
    
    private class DeleteDependencyJMenuItem extends JMenuItem implements ActionListener {
        
        private DependencySet set;
        private Dependency dependency;

        public DeleteDependencyJMenuItem(DependencySet set, Dependency dependency) {
            super("Delete Dependency");
            this.set = set;
            this.dependency = dependency;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            this.set.dependencies.remove(this.dependency);
            SenseJTree.this.refresh();
        }
        
    }
    
    private class EditDependencyJMenuItem extends JMenuItem implements ActionListener {
        
        private Dependency dependency;

        public EditDependencyJMenuItem(Dependency dependency) {
            super("Edit Dependency");
            this.dependency = dependency;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            DependencyEditorJPanel e = new DependencyEditorJPanel(this.dependency);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 110);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            SenseJTree.this.refresh();
        }
        
    }
    
    private class EditDependencySetJMenuItem extends JMenuItem implements ActionListener {
        
        private DependencySet set;

        public EditDependencySetJMenuItem(DependencySet set) {
            super("Edit Dependency Set");
            this.set = set;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            DependencySetEditorJPanel e = new DependencySetEditorJPanel(this.set);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 100);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            SenseJTree.this.refresh();
        }
        
    }
    
    private class DeleteDependencySetJMenuItem extends JMenuItem implements ActionListener {
        
        private DependencySet set;

        public DeleteDependencySetJMenuItem(DependencySet set) {
            super("Delete Dependency Set");
            this.set = set;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            SenseJTree.this.sense.removeDependencySet(this.set);
            SenseJTree.this.refresh();
        }
        
    }
    
    private class AddDependencyJMenuItem extends JMenuItem implements ActionListener {
        
        private DependencySet set;

        public AddDependencyJMenuItem(DependencySet set) {
            super("Add Dependency");
            this.set = set;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            Dependency dependency = new Dependency("", "", "", new LinkedList());
            
            DependencyEditorJPanel e = new DependencyEditorJPanel(dependency);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 110);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            this.set.dependencies.add(dependency);
            
            SenseJTree.this.refresh();
        }
        
    }
    
    private class AddMeaningJMenuItem extends JMenuItem implements ActionListener {
        
        private Sense sense = null;
        private DependencySet set = null;

        public AddMeaningJMenuItem(Sense sense) {
            super("Add Meaning");
            this.sense = sense;
            this.addActionListener(this);
        }
        
        public AddMeaningJMenuItem(DependencySet set) {
            super("Add Meaning");
            this.set = set;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            Meaning m = new Meaning("", "", "");
            
            if (this.sense != null && this.set == null) {
                m.target = "SELF";
            }
            
            MeaningEditorJPanel e = new MeaningEditorJPanel(m);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 110);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            if (this.sense != null) {
                this.sense.addMeaning(m);
            } else if (set != null) {
                this.set.meanings.add(m);
            }
            
            SenseJTree.this.refresh();
        }
        
    }
    
    private class EditMeaningJMenuItem extends JMenuItem implements ActionListener {
        
        private Meaning meaning;

        public EditMeaningJMenuItem(Meaning meaning) {
            super("Edit Meaning");
            this.meaning = meaning;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            MeaningEditorJPanel e = new MeaningEditorJPanel(this.meaning);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 110);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            SenseJTree.this.refresh();
        }
        
    }
    
    private class DeleteMeaningJMenuItem extends JMenuItem implements ActionListener {
        
        private Sense sense = null;
        private DependencySet set = null;
        private Meaning meaning;

        public DeleteMeaningJMenuItem(Sense sense, Meaning meaning) {
            super("Delete Meaning");
            this.sense = sense;
            this.meaning = meaning;
            this.addActionListener(this);
        }
        
        public DeleteMeaningJMenuItem(DependencySet set, Meaning meaning) {
            super("Delete Meaning");
            this.set = set;
            this.meaning = meaning;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (this.sense != null) {
                this.sense.removeMeaning(this.meaning);
            } else if (this.set != null) {
                this.set.meanings.remove(this.meaning);
            }
            
            SenseJTree.this.refresh();
        }
        
    }
    
    private class AddDependencySetJMenuItem extends JMenuItem implements ActionListener {
        
        public AddDependencySetJMenuItem() {
            super("Add Dependency Set");
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            DependencySet set = new DependencySet(new LinkedList(), new LinkedList(), true, "");
            
            DependencySetEditorJPanel e = new DependencySetEditorJPanel(set);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 100);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            SenseJTree.this.sense.addDependencySet(set);
            
            SenseJTree.this.refresh();
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
            Expectation expectation = new Expectation("", "");
            
            ExpectationEditorJPanel e = new ExpectationEditorJPanel(expectation);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 100);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            this.dependency.expectations.add(expectation);
            
            SenseJTree.this.refresh();
        }
        
    }
    
}
