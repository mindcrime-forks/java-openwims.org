/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import com.jesseenglish.swingftfy.extensions.FDialog;
import com.jesseenglish.swingftfy.extensions.FNode;
import com.jesseenglish.swingftfy.extensions.FTree;
import edu.stanford.nlp.util.ArraySet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.LinkedList;
import java.util.List;
import java.util.TooManyListenersException;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeModel;
import org.openwims.Objects.Preprocessor.PPDependency;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPMention;
import org.openwims.Objects.Preprocessor.PPSentence;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.UI.Editors.PPDependencyEditorJPanel;
import org.openwims.UI.Editors.PPMentionEditorJPanel;
import org.openwims.UI.FileDropJPanel.FilesDraggedListener;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesseenglish
 */
public class PPDocumentJTree extends FTree {
    
    private static ImageIcon ROOT = new ImageIcon(PPDocumentJTree.class.getResource("/images/root.png"));
    private static ImageIcon NODE = new ImageIcon(PPDocumentJTree.class.getResource("/images/node-blue.png"));
    private static ImageIcon LEAF = new ImageIcon(PPDocumentJTree.class.getResource("/images/node-green.png"));
    private static ImageIcon COLLECTION = new ImageIcon(PPDocumentJTree.class.getResource("/images/node-black.png"));
    
    private PPDocument document;
    
    
    private DropTarget dropTarget;
    private DropTargetHandler dropTargetHandler;
    private boolean dragOver = false;
    private BufferedImage screenshot;
    
    private LinkedList<FilesDraggedListener> listeners = new LinkedList();
    private ArraySet<Object> expanded;
    
    

    public PPDocumentJTree(PPDocument document) {
        this.document = document;
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
    
    public void refresh() {
        PPDocumentNode root = new PPDocumentNode(this.document);
        this.expanded.add(root.recall());
        
        DefaultTreeModel model = new DefaultTreeModel(root);
        this.setModel(model);
        
        //expand all sentence nodes
        for (int i = 0; i < root.getChildCount(); i++) {
            this.expandNode(root.getChildAt(i));
            this.expanded.add(((ExpansionMemoryNode)root.getChildAt(i)).recall());
        }
        
        reExpand(root);
        
        this.validate();
        this.repaint();
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
    
    private class PPDocumentNode extends ExpansionMemoryNode {
        
        private PPDocument document;

        public PPDocumentNode(PPDocument document) {
            super("Document");
            this.document = document;
            setIcon(ROOT);
            
            for (PPSentence sentence : document.listSentences()) {
                this.add(new PPSentenceNode(sentence));
            }
        }

        @Override
        public Object recall() {
            return this.document;
        }
        
    }
    
    private class PPSentenceNode extends ExpansionMemoryNode {
        
        private PPSentence sentence;

        public PPSentenceNode(PPSentence sentence) {
            super(sentence.text());
            this.sentence = sentence;
            setIcon(ROOT);
            
            for (PPDependency dependency : sentence.listDependencies()) {
                this.add(new PPDependencyNode(dependency, sentence));
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (SwingUtilities.isRightMouseButton(me)) {
            
                JPopupMenu menu = new JPopupMenu();
                menu.add(new AddDependencyJMenuItem(this.sentence));

                Rectangle r = PPDocumentJTree.this.getPathBounds(PPDocumentJTree.this.getPath(this));
                menu.show(PPDocumentJTree.this, r.x + getIcon().getIconWidth(), r.y + r.height);
            }
        }

        @Override
        public Object recall() {
            return this.sentence;
        }
        
    } 
    
    private class PPDependencyNode extends ExpansionMemoryNode {
        
        private PPSentence sentence;
        private PPDependency dependency;

        public PPDependencyNode(PPDependency dependency, PPSentence sentence) {
            super(dependency.getType() + "(" + dependency.getGovernor().anchor(sentence) + "," + dependency.getDependent().anchor(sentence) + ")");
            this.dependency = dependency;
            this.sentence = sentence;
            setIcon(NODE);
            
            this.add(new PPGovernorNode(this.dependency.getGovernor(), sentence));
            this.add(new PPDependentNode(this.dependency.getDependent(), sentence));
        }
        
        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (SwingUtilities.isRightMouseButton(me)) {
            
                JPopupMenu menu = new JPopupMenu();
                menu.add(new EditDependencyJMenuItem(this.dependency, this.sentence));
                menu.add(new DeleteDependencyJMenuItem(this.dependency, this.sentence));

                Rectangle r = PPDocumentJTree.this.getPathBounds(PPDocumentJTree.this.getPath(this));
                menu.show(PPDocumentJTree.this, r.x + getIcon().getIconWidth(), r.y + r.height);
            }
        }

        @Override
        public Object recall() {
            return this.dependency;
        }
        
    }
    
    private class PPGovernorNode extends ExpansionMemoryNode {
        
        private PPSentence sentence;
        private PPToken governor;

        public PPGovernorNode(PPToken governor, PPSentence sentence) {
            super("GOV: " + governor.anchor(sentence));
            this.governor = governor;
            this.sentence = sentence;
            setIcon(NODE);
            
            for (PPMention mention : governor.getMentions()) {
                this.add(new PPMentionNode(mention));
            }
        }

        @Override
        public Object recall() {
            return this.governor;
        }
        
    }
    
    private class PPDependentNode extends ExpansionMemoryNode {
        
        private PPSentence sentence;
        private PPToken dependent;

        public PPDependentNode(PPToken dependent, PPSentence sentence) {
            super("DEP: " + dependent.anchor(sentence));
            this.dependent = dependent;
            this.sentence = sentence;
            setIcon(NODE);
            
            for (PPMention mention : dependent.getMentions()) {
                this.add(new PPMentionNode(mention));
            }
        }

        @Override
        public Object recall() {
            return this.dependent;
        }
        
    }
    
    private class PPPOSNode extends ExpansionMemoryNode {
        
        private PPMention mention;

        public PPPOSNode(PPMention mention) {
            super(mention.pos() + " (" + mention.rootPOS() + ")");
            this.mention = mention;
            setIcon(LEAF);
        }

        @Override
        public Object recall() {
            return null;
        }
        
    }

    private class PPMentionNode extends ExpansionMemoryNode {
        
        private PPMention mention;

        public PPMentionNode(PPMention mention) {
            super("Sentence " + (document.listSentences().indexOf(mention.getSentence()) + 1) + ": " + mention.anchor());
            this.mention = mention;

            setIcon(COLLECTION);
            
            this.add(new PPPOSNode(mention));
            this.add(new PPLemmaNode(mention));
        }

        @Override
        public Object recall() {
            return mention;
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
            
            if (SwingUtilities.isRightMouseButton(me)) {
            
                JPopupMenu menu = new JPopupMenu();
                menu.add(new EditMentionJMenuItem(this.mention));

                Rectangle r = PPDocumentJTree.this.getPathBounds(PPDocumentJTree.this.getPath(this));
                menu.show(PPDocumentJTree.this, r.x + getIcon().getIconWidth(), r.y + r.height);
            }
        }
        
    }
    
    private class PPLemmaNode extends ExpansionMemoryNode {
        
        private PPMention mention;

        public PPLemmaNode(PPMention mention) {
            super(mention.lemma());
            this.mention = mention;
            setIcon(LEAF);
        }

        @Override
        public Object recall() {
            return null;
        }
        
    }
    
    private class AddDependencyJMenuItem extends JMenuItem implements ActionListener {
        
        private PPSentence sentence;

        public AddDependencyJMenuItem(PPSentence sentence) {
            super("Add Dependency");
            this.sentence = sentence;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            PPDependency dependency = new PPDependency();
            dependency.setType("???");
            dependency.setGovernor(sentence.listTokens().getFirst());
            dependency.setDependent(sentence.listTokens().getFirst());
            
            sentence.addDependency(dependency);
            PPDocumentJTree.this.refresh();
        }
        
    }
    
    private class EditDependencyJMenuItem extends JMenuItem implements ActionListener {
        
        private PPSentence sentence;
        private PPDependency dependency;

        public EditDependencyJMenuItem(PPDependency dependency, PPSentence sentence) {
            super("Edit Dependency");
            this.dependency = dependency;
            this.sentence = sentence;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            PPDependencyEditorJPanel e = new PPDependencyEditorJPanel(this.sentence, this.dependency, this.sentence.listTokens());
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 110);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            PPDocumentJTree.this.refresh();
        }
        
    }
    
    private class DeleteDependencyJMenuItem extends JMenuItem implements ActionListener {
        
        private PPDependency dependency;
        private PPSentence sentence;

        public DeleteDependencyJMenuItem(PPDependency dependency, PPSentence sentence) {
            super("Delete Dependency");
            this.dependency = dependency;
            this.sentence = sentence;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            this.sentence.removeDependency(this.dependency);
            PPDocumentJTree.this.refresh();
        }
        
    }
    
    private class EditMentionJMenuItem extends JMenuItem implements ActionListener {
        
        private PPMention mention;

        public EditMentionJMenuItem(PPMention mention) {
            super("Edit Mention");
            this.mention = mention;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            PPMentionEditorJPanel e = new PPMentionEditorJPanel(this.mention);
            
            FDialog d =  new FDialog(WIMGlobals.frame);
            d.setModal(true);
            d.setSize(250, 110);
            d.add(e);
            d.center();
            d.setVisible(true);
            
            PPDocumentJTree.this.refresh();
        }
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void addFilesDraggedListener(FilesDraggedListener listener) {
        this.listeners.add(listener);
    }

    protected DropTarget getMyDropTarget() {
        if (dropTarget == null) {
            dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null);
        }
        return dropTarget;
    }

    protected DropTargetHandler getDropTargetHandler() {
        if (dropTargetHandler == null) {
            dropTargetHandler = new DropTargetHandler();
        }
        return dropTargetHandler;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        try {
            getMyDropTarget().addDropTargetListener(getDropTargetHandler());
        } catch (TooManyListenersException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        getMyDropTarget().removeDropTargetListener(getDropTargetHandler());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    
        if (dragOver) {
            
            g2d.drawImage(blur(blur(screenshot)), 0, 0, this);
            
            g2d.setColor(new Color(0.8f, 0.8f, 0.8f, 0.5f));
            g2d.fill(this.getBounds());
            
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {2.0f}, 0.0f));
            g2d.draw(new RoundRectangle2D.Double(10, 10, getWidth() - 20, getHeight() - 20, 10, 10));
            
            String message = "Release to Load and WIMify";
            float messageWidth = g2d.getFontMetrics().stringWidth(message);
            g2d.drawString(message, (getWidth() / 2) - (messageWidth / 2), getHeight() / 2);
        } 
        
        g2d.dispose();
    }
    
    public BufferedImage blur(BufferedImage image) {
        BufferedImage blurred = new BufferedImage(image.getWidth(this), image.getHeight(this), BufferedImage.TYPE_INT_RGB);
        
        float data[] = {0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f, 0.0625f, 0.125f, 0.0625f};
        Kernel kernel = new Kernel(3, 3, data);
        ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        convolve.filter(image, blurred);
        
        return blurred;
    }

    protected void importFiles(final List files) {
        Runnable run = new Runnable() {

            @Override
            public void run() {
                for (FilesDraggedListener listener : listeners) {
                    listener.filesDraggedEvent(files);
                }
            }
        };
        SwingUtilities.invokeLater(run);
    }

    protected class DropTargetHandler implements DropTargetListener {

        protected void processDrag(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                dtde.rejectDrag();
            }
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            processDrag(dtde);
            SwingUtilities.invokeLater(new PPDocumentJTree.DragUpdate(true));
            repaint();
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            processDrag(dtde);
            SwingUtilities.invokeLater(new PPDocumentJTree.DragUpdate(true));
            repaint();
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            SwingUtilities.invokeLater(new PPDocumentJTree.DragUpdate(false));
            repaint();
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {

            SwingUtilities.invokeLater(new PPDocumentJTree.DragUpdate(false));

            Transferable transferable = dtde.getTransferable();
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(dtde.getDropAction());
                try {

                    List transferData = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    if (transferData != null && transferData.size() > 0) {
                        importFiles(transferData);
                        dtde.dropComplete(true);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                dtde.rejectDrop();
            }
        }
    }

    public class DragUpdate implements Runnable {

        private boolean dragOver;

        public DragUpdate(boolean dragOver) {
            this.dragOver = dragOver;
        }

        @Override
        public void run() {
            if (PPDocumentJTree.this.dragOver == this.dragOver) {
                return;
            }
            
            //First capture a screenshot of the UI now - when the repaint happens that
            //screen will be used as an image to be blurred
            BufferedImage image = new BufferedImage(PPDocumentJTree.this.getWidth(), PPDocumentJTree.this.getHeight(), BufferedImage.TYPE_INT_RGB);
            PPDocumentJTree.this.paint(image.getGraphics());
            
            PPDocumentJTree.this.dragOver = dragOver;
            PPDocumentJTree.this.screenshot = image;
            PPDocumentJTree.this.repaint();
        }
    }
    
}
