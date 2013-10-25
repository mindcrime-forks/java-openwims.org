/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI.LexiconEditor;

import com.jesseenglish.swingftfy.extensions.FLabel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.util.LinkedList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openwims.Objects.Lexicon.Sense;

/**
 *
 * @author jesseenglish
 */
public class SensesJList extends JList implements Scrollable {
    
    private LinkedList<SenseSelectionListener> listeners;
    
    public SensesJList() {
        this.listeners = new LinkedList();
        this.setCellRenderer(new SensesListCellRenderer());
        this.addListSelectionListener(new SensesListSelectionListener());
    }
    
    public void addSenseSelectionListener(SenseSelectionListener listener) {
        this.listeners.add(listener);
    }
    
    private void fireSenseSelectionListeners(Sense sense) {
        for (SenseSelectionListener listener : listeners) {
            listener.senseSelected(sense);
        }
    }
    
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
       return 16;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 16;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
    public interface SenseSelectionListener {
        public void senseSelected(Sense sense);
    }
    
    private class SensesListSelectionListener implements ListSelectionListener {
        
        @Override
        public void valueChanged(ListSelectionEvent lse) {
            Sense sense = (Sense)((JList)lse.getSource()).getSelectedValue();
            if (sense == null) {
                return;
            }
            
            SensesJList.this.fireSenseSelectionListeners(sense);
        }
        
    }
    
    private class SensesListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object o, int i, boolean bln, boolean bln1) {
            
            JPanel panel = new JPanel();
            panel.setBackground(Color.WHITE);
            panel.setBorder(new LineBorder(Color.WHITE, 2));
            
            if (i % 2 == 1) {
                panel.setBackground(new Color(0.9f, 0.95f, 1.0f));
                panel.setBorder(new LineBorder(Color.WHITE, 1));
            }
            
            if (bln) {
                panel.setBackground(new Color(0.8f, 0.85f, 1.0f));
            }
            
            panel.setLayout(new GridBagLayout());
            
            if (o instanceof Sense) {
                Sense sense = (Sense) o;
                
                GridBagConstraints c = new GridBagConstraints();
                
                FLabel idLabel = new FLabel(sense.getId());
                idLabel.setBold(true);
                
                FLabel defLabel = new FLabel(sense.getDefinition());
                defLabel.setFont(defLabel.getFont().deriveFont(Font.ITALIC));
                defLabel.setForeground(Color.DARK_GRAY);
                
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 0.0;
                c.weighty = 0.0;
                c.fill = GridBagConstraints.HORIZONTAL;
                panel.add(idLabel, c);
                
                c.gridx = 0;
                c.gridy = 1;
                c.weightx = 0.1;
                c.weighty = 0.1;
                c.fill = GridBagConstraints.BOTH;
                panel.add(defLabel, c);
            }
            
            return panel;
        }
        
    }
    
}
