/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.plaf.metal;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import java.io.Serializable;
import javax.swing.text.View;


/**
 * RadioButtonUI implementation for MetalRadioButtonUI
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.18 02/06/02
 * @author Michael C. Albers (Metal modifications)
 * @author Jeff Dinkins (original BasicRadioButtonCode)
 */
public class MetalRadioButtonUI extends BasicRadioButtonUI {

    private static final MetalRadioButtonUI metalRadioButtonUI = new MetalRadioButtonUI();

    protected Color focusColor;
    protected Color selectColor;
    protected Color disabledTextColor;

    private boolean defaults_initialized = false;

    // ********************************
    //        Create PlAF
    // ********************************
    public static ComponentUI createUI(JComponent c) {
        return metalRadioButtonUI;
    }

    // ********************************
    //        Install Defaults 
    // ********************************
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if(!defaults_initialized) {
            focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
            selectColor = UIManager.getColor(getPropertyPrefix() + "select");
            disabledTextColor = UIManager.getColor(getPropertyPrefix() + "disabledText");
            defaults_initialized = true;
        }
        b.setOpaque(true);
    }

    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        defaults_initialized = false;
    }

    // ********************************
    //         Default Accessors 
    // ********************************
    protected Color getSelectColor() {
        return selectColor;
    }

    protected Color getDisabledTextColor() {
        return disabledTextColor;
    }

    protected Color getFocusColor() {
        return focusColor;
    }


    // ********************************
    //        Paint Methods
    // ********************************
    public synchronized void paint(Graphics g, JComponent c) {

        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        
        Dimension size = c.getSize();

        int w = size.width;
        int h = size.height;

        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();

        Rectangle viewRect = new Rectangle(size);
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();

        Icon altIcon = b.getIcon();
        Icon selectedIcon = null;
        Icon disabledIcon = null;
        
        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), altIcon != null ? altIcon : getDefaultIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect, getDefaultTextIconGap(b)
        );
        
        // fill background
        if(c.isOpaque()) {
            g.setColor(b.getBackground());
            g.fillRect(0,0, size.width, size.height); 
        }

        
        // Paint the radio button
        if(altIcon != null) { 

            if(!model.isEnabled()) {
	        if(model.isSelected()) {
                   altIcon = b.getDisabledSelectedIcon();
		} else {
                   altIcon = b.getDisabledIcon();
		}
            } else if(model.isPressed() && model.isArmed()) {
                altIcon = b.getPressedIcon();
                if(altIcon == null) {
                    // Use selected icon
                    altIcon = b.getSelectedIcon();
                } 
            } else if(model.isSelected()) {
                if(b.isRolloverEnabled() && model.isRollover()) {
                        altIcon = (Icon) b.getRolloverSelectedIcon();
                        if (altIcon == null) {
                                altIcon = (Icon) b.getSelectedIcon();
                        }
                } else {
                        altIcon = (Icon) b.getSelectedIcon();
                }
            } else if(b.isRolloverEnabled() && model.isRollover()) {
                altIcon = (Icon) b.getRolloverIcon();
            } 
              
            if(altIcon == null) {
                altIcon = b.getIcon();
            }
               
            altIcon.paintIcon(c, g, iconRect.x, iconRect.y);

        } else {
            getDefaultIcon().paintIcon(c, g, iconRect.x, iconRect.y);
        }


        // Draw the Text
        if(text != null) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
                v.paint(g, textRect);
            } else {
               if(model.isEnabled()) {
                   // *** paint the text normally
                   g.setColor(b.getForeground());
                   BasicGraphicsUtils.drawString(g,text,model.getMnemonic(),
                                                 textRect.x, textRect.y + fm.getAscent());
               } else {
                   // *** paint the text disabled
                   g.setColor(b.getBackground().darker());
                   BasicGraphicsUtils.drawString(g,text,model.getMnemonic(),
                                                 textRect.x,
                                                 textRect.y + fm.getAscent());
               }
               if(b.hasFocus() && b.isFocusPainted() &&
                  textRect.width > 0 && textRect.height > 0 ) {
                   paintFocus(g,textRect,size);
               }
	   }
        }
    }

    protected void paintFocus(Graphics g, Rectangle t, Dimension d){
        g.setColor(getFocusColor());
        g.drawRect(t.x,t.y-1,t.width+1,t.height+1);
    } 
}
