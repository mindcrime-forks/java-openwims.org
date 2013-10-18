/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.Objects.Preprocessor.PPDocument;
import org.openwims.Objects.Preprocessor.PPSentence;
import org.openwims.Objects.Preprocessor.PPToken;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesseenglish
 */
public class DebugJPanel extends JPanel {
    
    private PPDocument document;
    private HashMap<PPToken, Sense> mapping;

    public DebugJPanel(PPDocument document) {
        this.document = document;
    
        refresh();
    }

    public HashMap<PPToken, Sense> getMapping() {
        return mapping;
    }
    
    public void refresh() {
        this.mapping = new HashMap();
        
        this.removeAll();
        this.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridy = -1;
        c.anchor = GridBagConstraints.WEST;
        
        for (PPSentence sentence : document.listSentences()) {
            for (PPToken token : sentence.listTokens()) {
                c.gridx = 0;
                c.gridy++;
                c.weightx = 0.0;
                c.weighty = 0.0;
                c.fill = GridBagConstraints.HORIZONTAL;
                
                this.add(new JLabel(token.anchor()), c);
                
                c.gridx = 1;
                c.weightx = 0.1;
                
                this.add(new SensesJComboBox(token), c);
            }
        }
        
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.1;
        c.weighty = 0.1;
        c.gridwidth = 2;
        c.fill  = GridBagConstraints.BOTH;
        
        this.add(new JPanel(), c);
        
        this.validate();
        this.repaint();
    }
    
    private class SensesJComboBox extends JComboBox {
        
        private PPToken token;

        public SensesJComboBox(PPToken token) {
            this.token = token;
        
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            
            for (Sense sense : WIMGlobals.lexicon().listSenses(token)) {
                model.addElement(sense);
            }
            
            this.setModel(model);
            
            this.setRenderer(new SensesListCellRenderer());
            
            mapping.put(token, (Sense)model.getElementAt(0));
            
            this.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    mapping.put(SensesJComboBox.this.token, (Sense)e.getItem());
                }
            });
        }
        
    }
    
    private class SensesListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Sense) {
                Sense sense = (Sense) value;
                label.setText(sense.getId());
            }

            return label;
        }
        
    }
    
}
