/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.Objects.WIM;
import org.openwims.Objects.WIMFrame;
import org.openwims.Processors.TieredGroupingDisambiguation;
import org.openwims.Serialization.JSONPPDocumentSerializer;

/**
 *
 * @author jesseenglish
 */
public class WIMSJTable extends JTable {
    
    private LinkedList<WIMSelectionListener> wimSelectionListeners;
    
    public static void main(String[] args) throws Exception {
        PPDocument document = JSONPPDocumentSerializer.deserialize("/Users/jesseenglish/Desktop/test.pp");
        TieredGroupingDisambiguation tgd = new TieredGroupingDisambiguation();
        LinkedList<WIM> wims = tgd.wimify(tgd.wse(document));
        
        WIMSJTable table = new WIMSJTable(wims);
        
        final JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setLeftComponent(new JScrollPane(table));
        splitter.setRightComponent(new JScrollPane());
        splitter.setDividerLocation(1000);
        
        table.addSelectionListener(new WIMSelectionListener() {
            public void wimSelected(WIM wim) {
                LinkedList<WIM> listOfOne = new LinkedList();
                listOfOne.add(wim);
                
                JScrollPane treeScroller = new JScrollPane(new WIMSJTree(listOfOne));
                splitter.setRightComponent(treeScroller);
                splitter.setDividerLocation(0.65);
                splitter.validate();
                splitter.repaint();
            }
        });
        
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLayout(new GridLayout(1, 1));
        
        frame.add(splitter);
        
        frame.setVisible(true);
    }
    
    public WIMSJTable(final LinkedList<WIM> wims) {
        this.wimSelectionListeners = new LinkedList();
        
        if (wims.size() == 0) {
            return;
        }
        
        Collections.sort(wims);
        Collections.reverse(wims);
        
        LinkedList<PPToken> tokens = new LinkedList();
        for (WIMFrame frame : wims.getFirst().listFrames()) {
            tokens.add(frame.getAnchor());
        }
        
        Collections.sort(tokens);
        
        String[] colNames = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            colNames[i] = tokens.get(i).anchor();
        }
        
        DefaultTableModel model = new DefaultTableModel(colNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
               return WIMFrame.class;
            }
        };
        
        for (WIM wim : wims) {
            WIMFrame[] frames = new WIMFrame[tokens.size()];
            for (int i = 0; i < tokens.size(); i++) {
                PPToken token = tokens.get(i);
                frames[i] = wim.frame(token);
            }
            model.addRow(frames);
        }

        this.setModel(model);
        this.setDefaultRenderer(WIMFrame.class, new WIMFrameCellRenderer());
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                fireSelectionListeners(wims.get(e.getFirstIndex()));
            }
        });
        
        this.getTableHeader().addMouseListener(new WIMSJTableHeaderMouseListener());
    }
    
    public void addSelectionListener(WIMSelectionListener listener) {
        this.wimSelectionListeners.add(listener);
    }
    
    private void fireSelectionListeners(WIM wim) {
        for (WIMSelectionListener listener : wimSelectionListeners) {
            listener.wimSelected(wim);
        }
    }
    
    private class WIMFrameCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            WIMFrame frame = (WIMFrame)value;
                        
            if (frame == null) {
                label.setText("???");
            } else {
                label.setText(frame.getSense().getId());
            }
            
            return label;
        }
        
    }
    
    private class WIMSJTableHeaderMouseListener implements MouseListener {

        public void mouseClicked(MouseEvent e) {}

        public void mousePressed(MouseEvent e) {}

        public void mouseReleased(MouseEvent e) {
            int index = convertColumnIndexToModel(columnAtPoint(e.getPoint()));
            if (index >= 0) {
                System.out.println("Clicked on column " + index);
            }
        }

        public void mouseEntered(MouseEvent e) {}

        public void mouseExited(MouseEvent e) {}
        
    }
    
    public interface WIMSelectionListener {
        public void wimSelected(WIM wim);
    }
    
}
