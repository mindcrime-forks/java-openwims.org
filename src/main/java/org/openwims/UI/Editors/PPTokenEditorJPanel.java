/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI.Editors;

import org.openwims.Objects.Preprocessor.PPToken;

/**
 *
 * @author jesseenglish
 */
public class PPTokenEditorJPanel extends javax.swing.JPanel {

    private PPToken token;
    
    /**
     * Creates new form PPTokenEditorJPanel
     */
    public PPTokenEditorJPanel(PPToken token) {
        initComponents();
        this.token = token;
        
//        this.POSJTextField.setText(token.pos());
//        this.LemmaJTextField.setText(token.lemma());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        POSJTextField = new com.jesseenglish.swingftfy.extensions.FTextField();
        LemmaJTextField = new com.jesseenglish.swingftfy.extensions.FTextField();

        setLayout(new java.awt.GridBagLayout());

        POSJTextField.setHintText("pos");
        POSJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                POSJTextFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        add(POSJTextField, gridBagConstraints);

        LemmaJTextField.setHintText("lemma");
        LemmaJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                LemmaJTextFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(LemmaJTextField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void POSJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_POSJTextFieldKeyReleased
//        this.token.setPOS(this.POSJTextField.getText().trim());
    }//GEN-LAST:event_POSJTextFieldKeyReleased

    private void LemmaJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LemmaJTextFieldKeyReleased
//        this.token.setLemma(this.LemmaJTextField.getText().trim());
    }//GEN-LAST:event_LemmaJTextFieldKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.jesseenglish.swingftfy.extensions.FTextField LemmaJTextField;
    private com.jesseenglish.swingftfy.extensions.FTextField POSJTextField;
    // End of variables declaration//GEN-END:variables
}
