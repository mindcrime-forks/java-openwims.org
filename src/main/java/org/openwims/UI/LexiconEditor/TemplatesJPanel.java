/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI.LexiconEditor;

import com.jesseenglish.swingftfy.extensions.FLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Sense;
import org.openwims.WIMGlobals;

/**
 *
 * @author jesseenglish
 */
public class TemplatesJPanel extends JPanel implements Scrollable {
    
    private Sense selected;
    private LinkedList<TemplatesListener> listeners;
    
    public TemplatesJPanel() {
        this.setLayout(new GridBagLayout());
        this.listeners = new LinkedList();
        this.selected = new Sense("", "???", "", 0);
        
        refresh();
    }
    
    public void refresh() {
        this.removeAll();
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = -1;
        
        if (WIMGlobals.templates().templates(selected.pos()) == null) {
            return;
        }
        
        for (DependencySet template : WIMGlobals.templates().templates(selected.pos())) {
            c.gridx = 0;
            c.gridy++;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            
            this.add(new TemplateJPanel(template), c);
        }
        
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.1;
        c.weighty = 0.1;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        this.add(new JPanel(), c);
        
        this.validate();
        this.repaint();
    }

    public void setSelected(Sense selected) {
        this.selected = selected;
        refresh();
    }
    
    public void addTemplatesListener(TemplatesListener listener) {
        this.listeners.add(listener);
    }
    
    private void fireListeners(DependencySet template) {
        for (TemplatesListener listener : this.listeners) {
            listener.templateAdded(template);
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
    
    public interface TemplatesListener {
        public void templateAdded(DependencySet template);
    }
    
    private class AddTemplateJLabel extends FLabel implements MouseListener {
        
        private DependencySet template;

        public AddTemplateJLabel(DependencySet template) {
            super("[add]");
            this.template = template;
            this.addMouseListener(this);
            
            for (DependencySet dependencySet : selected.listDependencySets()) {
                if (dependencySet.label.equals(this.template.label)) {
                    this.setForeground(new Color(0.5f, 0.5f, 0.5f));
                    this.removeMouseListener(this);
                }
            }
        }

        public void mouseClicked(MouseEvent e) {}

        public void mousePressed(MouseEvent e) {}

        public void mouseReleased(MouseEvent e) {
            fireListeners(template);
            this.setBold(false);
            this.setForeground(new Color(0.5f, 0.5f, 0.5f));
            this.removeMouseListener(this);
            this.validate();
            this.repaint();
        }

        public void mouseEntered(MouseEvent e) {
            this.setBold(true);
            this.validate();
            this.repaint();
        }

        public void mouseExited(MouseEvent e) {
            this.setBold(false);
            this.validate();
            this.repaint();
        }
        
    }
    
    private class TemplateJPanel extends JPanel {
        
        private DependencySet template;

        public TemplateJPanel(DependencySet template) {
            this.template = template;
            this.setLayout(new GridLayout(2, 1));
            
            JPanel header = new JPanel();
            header.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
            header.setBackground(Color.LIGHT_GRAY);
            
            FLabel title = new FLabel(template.label);
            title.setFont(new java.awt.Font("Arial", 1, 14));
            
            String exampleText = "(no example text available)";
            if (WIMGlobals.templates().example(template) != null) {
                exampleText = " " + WIMGlobals.templates().example(template).replaceAll("\"", "").replaceAll("\\[SELF\\]", selected.word());
            }
            
            JLabel example = new JLabel(exampleText);
            example.setFont(example.getFont().deriveFont(Font.ITALIC));
            
            header.add(new AddTemplateJLabel(template));
            header.add(title);
            
            this.add(header);
            this.add(example);
            
        }
        
    }
    
}
