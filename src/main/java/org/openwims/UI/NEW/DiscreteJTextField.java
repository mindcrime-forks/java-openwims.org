/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI.NEW;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

/**
 *
 * @author jesseenglish
 */
public class DiscreteJTextField extends JTextField {
        
    private Color ERROR = Color.RED;
    private Color WARNING = Color.ORANGE;

    public DiscreteJTextField(String text) {
        super(text);
        this.setBorder(new LineBorder(new Color(0.9f, 0.9f, 0.9f), 1));
    }

    protected boolean error() {
        return false;
    }

    protected boolean warning() {
        return false;
    }

    @Override
    public boolean isValidateRoot() {
        return false;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (error()) {
            g.setColor(ERROR);
            paintWigglyLine(g);
        }

        if (warning()) {
            g.setColor(WARNING);
            paintWigglyLine(g);
        }
    }

    private void paintWigglyLine(Graphics g) {
        for (int i = 0; i < this.getWidth(); i+=2) {
            g.drawLine(i, this.getHeight() - 2, i, this.getHeight() - 2);
        }
        for (int i = 1; i < this.getWidth(); i+=2) {
            g.drawLine(i, this.getHeight() - 1, i, this.getHeight() - 1);
        }
    }

}
