/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI.NEW;

import com.jesseenglish.swingftfy.extensions.FLabel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openwims.Objects.Lexicon.Sense;

/**
 *
 * @author jesseenglish
 */
public class SensesJList extends JList {
    
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
            
            panel.setLayout(new GridLayout(2, 1));
            
            if (o instanceof Sense) {
                Sense sense = (Sense) o;
                
                FLabel idLabel = new FLabel(sense.getId());
                idLabel.setBold(true);
                
                FLabel defLabel = new FLabel(sense.getDefinition());
                defLabel.setFont(defLabel.getFont().deriveFont(Font.ITALIC));
                defLabel.setForeground(Color.DARK_GRAY);
                
                panel.add(idLabel);
                panel.add(defLabel);
            }
            
            return panel;
        }
        
    }
    
}
