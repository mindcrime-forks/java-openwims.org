/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import java.awt.Component;
import java.awt.GridLayout;
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
    
    public static void main(String[] args) throws Exception {
        PPDocument document = JSONPPDocumentSerializer.deserialize("/Users/jesseenglish/Desktop/test.pp");
        TieredGroupingDisambiguation tgd = new TieredGroupingDisambiguation();
        LinkedList<WIM> wims = tgd.wimify(tgd.wse(document));
        
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLayout(new GridLayout(1, 1));
        
        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setLeftComponent(new JScrollPane(new WIMSJTable(wims)));
        splitter.setRightComponent(new JLabel("here"));
        splitter.setDividerLocation(650);
        
        frame.add(splitter);
        
        frame.setVisible(true);
    }
    
    public WIMSJTable(final LinkedList<WIM> wims) {
        if (wims.size() == 0) {
            return;
        }
        
        LinkedList<PPToken> tokens = new LinkedList();
        for (WIMFrame frame : wims.getFirst().listFrames()) {
            tokens.add(frame.getAnchor());
        }
        
        Collections.sort(tokens);
        
        String[] colNames = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            colNames[i] = tokens.get(i).anchor();
        }
        
        DefaultTableModel model = new DefaultTableModel(colNames, tokens.size()) {
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
                System.out.println(wims.get(e.getFirstIndex()));
            }
        });
        
        
    }
    
    private class WIMFrameCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.
            WIMFrame frame = (WIMFrame)value;
            
            if (frame == null) {
                label.setText("???");
            } else {
                label.setText(frame.getSense().getId());
            }
            
            return label;
        }
        
    }
    
    public interface WIMFrameSelectionListener {
        public void frameSelected(WIMFrame frame);
    }
    
}
