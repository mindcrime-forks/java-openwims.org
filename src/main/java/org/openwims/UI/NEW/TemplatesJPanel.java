/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI.NEW;

import com.jesseenglish.swingftfy.extensions.FLabel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openwims.Objects.Lexicon.Dependency;
import org.openwims.Objects.Lexicon.DependencySet;
import org.openwims.Objects.Lexicon.Expectation;
import org.openwims.Objects.Lexicon.Meaning;
import org.openwims.Objects.Lexicon.Sense;

/**
 *
 * @author jesseenglish
 */
public class TemplatesJPanel extends JPanel {
    
    private HashMap<String, LinkedList<DependencySet>> templates;
    private HashMap<DependencySet, String> examples;
    private Sense selected;
    private LinkedList<TemplatesListener> listeners;
    
    public TemplatesJPanel() {
        this.setLayout(new GridBagLayout());
        this.listeners = new LinkedList();
        this.selected = new Sense("", "???", "", 0);
        try {
            parseTemplates();
        } catch (Exception ex) {
            Logger.getLogger(TemplatesJPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        refresh();
    }
    
    private void parseTemplates() throws Exception {
        this.templates = new HashMap();
        this.examples = new HashMap();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(TemplatesJPanel.class.getResourceAsStream("/assets/deptemplates")));
        String line = null;
        
        DependencySet current = null;
        
        while ((line = br.readLine()) != null) {
            if (current == null) {
                current = new DependencySet(new LinkedList(), new LinkedList(), false, line.split("\\(")[0]);
                if (line.startsWith("+")) {
                    current.optional = true;
                }
                String pos = line.split("\\(")[1].split("\\)")[0];
                
                LinkedList<DependencySet> ts = templates.get(pos);
                if (ts == null) {
                    ts = new LinkedList();
                    templates.put(pos, ts);
                }
                
                ts.add(current);
                continue;
            }
            if (line.contains("(")) {
                Dependency dep = new Dependency("", "", "", new LinkedList());
                dep.type = line.split("\\(")[0];
                line = line.replaceFirst(dep.type, "").replaceAll("\\(", "").replaceAll("\\)", "");
                
                dep.governor = line.substring(0, line.indexOf(","));
                line = line.replaceFirst(dep.governor + ",", "");
                
                dep.dependent = line.split("\\[")[0];
                line = line.replaceFirst(dep.dependent, "").replaceAll("\\[", "").replaceAll("\\]", "");
                
                if (line.length() > 0) {
                    for (String exp : line.split(",")) {
                        dep.expectations.add(new Expectation(exp.split("=")[0], exp.split("=")[1]));
                    }
                }
                
                current.dependencies.add(dep);
                continue;
            }
            if (line.startsWith(">")) {
                line = line.replaceAll(">", "");
                
                Meaning meaning = new Meaning("", "", "");
                meaning.target = line.split("\\.")[0];
                line = line.replaceAll(meaning.target, "").replaceAll("\\.", "");
                
                meaning.relation = line.split("=")[0];
                meaning.wim = line.split("=")[1];
                
                current.meanings.add(meaning);
                continue;
            }
            if (line.startsWith("\"")) {
                this.examples.put(current, line);
                continue;
            }
            if (line.trim().length() == 0) {
                current = null;
                continue;
            }
        }
        
        for (LinkedList<DependencySet> ts : templates.values()) {
            Collections.sort(ts, new TemplateComparator());
        }
    }
    
    public void refresh() {
        this.removeAll();
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = -1;
        
        if (templates.get(selected.pos()) == null) {
            return;
        }
        
        for (DependencySet template : templates.get(selected.pos())) {
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
            if (examples.get(template) != null) {
                exampleText = " " + examples.get(template).replaceAll("\"", "").replaceAll("\\[SELF\\]", selected.word());
            }
            
            JLabel example = new JLabel(exampleText);
            example.setFont(example.getFont().deriveFont(Font.ITALIC));
            
            header.add(new AddTemplateJLabel(template));
            header.add(title);
            
            this.add(header);
            this.add(example);
            
        }
        
    }
    
    private class TemplateComparator implements Comparator<DependencySet> {

        public int compare(DependencySet o1, DependencySet o2) {
            return o1.label.compareTo(o2.label);
        }
    
    }
    
}
