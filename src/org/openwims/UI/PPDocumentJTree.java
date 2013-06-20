/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import com.jesseenglish.swingftfy.extensions.FNode;
import com.jesseenglish.swingftfy.extensions.FTree;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPSentence;
import org.openwims.Objects.Preprocessor.PPToken;

/**
 *
 * @author jesseenglish
 */
public class PPDocumentJTree extends FTree {
    
    private static ImageIcon ROOT = new ImageIcon(PPDocumentJTree.class.getResource("/images/root.png"));
    private static ImageIcon NODE = new ImageIcon(PPDocumentJTree.class.getResource("/images/node-blue.png"));
    private static ImageIcon LEAF = new ImageIcon(PPDocumentJTree.class.getResource("/images/node-green.png"));
    
    private PPDocument document;

    public PPDocumentJTree(PPDocument document) {
        this.document = document;
    
        refresh();
    }
    
    public void refresh() {
        PPDocumentNode root = new PPDocumentNode(this.document);
        DefaultTreeModel model = new DefaultTreeModel(root);
        this.setModel(model);
        
        //expand all sentence nodes
        for (int i = 0; i < root.getChildCount(); i++) {
            this.expandNode(root.getChildAt(i));
        }
        
        this.validate();
        this.repaint();
    }
    
    private class PPDocumentNode extends FNode {
        
        private PPDocument document;

        public PPDocumentNode(PPDocument document) {
            super("Document");
            this.document = document;
            setIcon(ROOT);
            
            for (PPSentence sentence : document.listSentences()) {
                this.add(new PPSentenceNode(sentence));
            }
        }
        
    }
    
    private class PPSentenceNode extends FNode {
        
        private PPSentence sentence;

        public PPSentenceNode(PPSentence sentence) {
            super(sentence.text());
            this.sentence = sentence;
            setIcon(ROOT);
            
            for (PPDependency dependency : sentence.listDependencies()) {
                this.add(new PPDependencyNode(dependency));
            }
        }
        
    } 
    
    private class PPDependencyNode extends FNode {
        
        private PPDependency dependency;

        public PPDependencyNode(PPDependency dependency) {
            super(dependency);
            this.dependency = dependency;
            setIcon(NODE);
            
            this.add(new PPGovernorNode(this.dependency.getGovernor()));
            this.add(new PPDependentNode(this.dependency.getDependent()));
        }
        
    }
    
    private class PPGovernorNode extends FNode {
        
        private PPToken governor;

        public PPGovernorNode(PPToken governor) {
            super("GOV: " + governor.anchor());
            this.governor = governor;
            setIcon(NODE);
            
            this.add(new PPPOSNode(this.governor));
            this.add(new PPLemmaNode(this.governor));
        }
        
    }
    
    private class PPDependentNode extends FNode {
        
        private PPToken dependent;

        public PPDependentNode(PPToken dependent) {
            super("DEP: " + dependent.anchor());
            this.dependent = dependent;
            setIcon(NODE);
            
            this.add(new PPPOSNode(this.dependent));
            this.add(new PPLemmaNode(this.dependent));
        }
        
    }
    
    private class PPPOSNode extends FNode {
        
        private PPToken token;

        public PPPOSNode(PPToken token) {
            super(token.pos());
            this.token = token;
            setIcon(LEAF);
        }
        
    }
    
    private class PPLemmaNode extends FNode {
        
        private PPToken token;

        public PPLemmaNode(PPToken token) {
            super(token.lemma());
            this.token = token;
            setIcon(LEAF);
        }
        
    }
    
}
