/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import com.jesseenglish.swingftfy.ExamplesJFrame;
import com.jesseenglish.swingftfy.extensions.FNode;
import com.jesseenglish.swingftfy.extensions.FTree;
import javax.swing.tree.DefaultTreeModel;
import org.openwims.Objects.Lexicon.*;

/**
 *
 * @author jesse
 */
public class SenseJTree extends FTree {
    
    private Sense sense;

    public SenseJTree(Sense sense) {
        this.sense = sense;
    
        SenseJTree.SenseNode root = new SenseJTree.SenseNode(sense);
        
        DefaultTreeModel model = new DefaultTreeModel(root);
        this.setModel(model);
    }
    
    private class SenseNode extends FNode {
        
        private Sense sense;

        public SenseNode(Sense sense) {
            super(sense.getId());
            this.sense = sense;
            setIcon(ExamplesJFrame.ROOT);
            
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
            setIcon(ExamplesJFrame.ROOT);
            
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
            setIcon(ExamplesJFrame.NODE);
        }
        
        
        
    }
    
    private class SenseStructureNode extends FNode {
        
        private Structure structure;

        public SenseStructureNode(Structure structure) {
            super(structure);
            this.structure = structure;
            setIcon(ExamplesJFrame.ROOT);
            
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
            setIcon(ExamplesJFrame.ROOT);
            
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
            setIcon(ExamplesJFrame.NODE);
            
            for (String specification : dependency.expectations.keySet()) {
                this.add(new SenseJTree.DepenendencyExpectationNode(specification, dependency.expectations.get(specification)));
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
            setIcon(ExamplesJFrame.NODE);
        }
        
    }
    
}
