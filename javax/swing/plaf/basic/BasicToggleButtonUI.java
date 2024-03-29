/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package javax.swing.plaf.basic;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.text.View;



/**
 * BasicToggleButton implementation
 * <p>
 *
 * @version 1.54 02/06/02
 * @author Jeff Dinkins
 */
public class BasicToggleButtonUI extends BasicButtonUI {

    private final static BasicToggleButtonUI toggleButtonUI = new BasicToggleButtonUI();

    private final static String propertyPrefix = "ToggleButton" + ".";

    // ********************************
    //          Create PLAF
    // ********************************
    public static ComponentUI createUI(JComponent b) {
        return toggleButtonUI;
    }

    protected String getPropertyPrefix() {
        return propertyPrefix;
    }
    

    // ********************************
    //          Paint Methods
    // ********************************
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
	
        Dimension size = b.getSize();
        FontMetrics fm = g.getFontMetrics();

        Insets i = c.getInsets();

        Rectangle viewRect = new Rectangle(size);

        viewRect.x += i.left;
        viewRect.y += i.top;
        viewRect.width -= (i.right + viewRect.x);
        viewRect.height -= (i.bottom + viewRect.y);

        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();

        Font f = c.getFont();
        g.setFont(f);

        // layout the text and icon
        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), b.getIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect, b.getText() == null ? 0 : getDefaultTextIconGap(b)
        );


        g.setColor(b.getBackground());

        if (model.isArmed() && model.isPressed() || model.isSelected()) {
            paintButtonPressed(g,b);
	} else if (b.isOpaque()) {
	    Insets insets = b.getInsets();
	    Insets margin = b.getMargin();
	    
	    g.fillRect(insets.left - margin.left,
		       insets.top - margin.top, 
		       size.width - (insets.left-margin.left) - (insets.right - margin.right),
		       size.height - (insets.top-margin.top) - (insets.bottom - margin.bottom));
	}
	
        // Paint the Icon
        if(b.getIcon() != null) { 
            paintIcon(g, b, iconRect);
        }
	
        // Draw the Text
        if(text != null && !text.equals("")) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
               v.paint(g, textRect);
            } else {
               paintText(g, b, textRect, text);
            }
        }
	
        // draw the dashed focus line.
        if (b.isFocusPainted() && b.hasFocus()) {
	    paintFocus(g, b, viewRect, textRect, iconRect);
        }
    }

    protected void paintIcon(Graphics g, AbstractButton b, Rectangle iconRect) {
        ButtonModel model = b.getModel();
        Icon icon = null;
        
        if(!model.isEnabled()) {
	    if(model.isSelected()) {
               icon = (Icon) b.getDisabledSelectedIcon();
	    } else {
               icon = (Icon) b.getDisabledIcon();
	    }
        } else if(model.isPressed() && model.isArmed()) {
            icon = (Icon) b.getPressedIcon();
            if(icon == null) {
                // Use selected icon
		icon = (Icon) b.getSelectedIcon();
            } 
        } else if(model.isSelected()) {
            if(b.isRolloverEnabled() && model.isRollover()) {
		icon = (Icon) b.getRolloverSelectedIcon();
		if (icon == null) {
		    icon = (Icon) b.getSelectedIcon();
		}
            } else {
		icon = (Icon) b.getSelectedIcon();
            }
        } else if(b.isRolloverEnabled() && model.isRollover()) {
            icon = (Icon) b.getRolloverIcon();
        } 
        
        if(icon == null) {
            icon = (Icon) b.getIcon();
        }
        
        icon.paintIcon(b, g, iconRect.x, iconRect.y);
    }
    
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text){
        ButtonModel model = b.getModel();
	
        FontMetrics fm = g.getFontMetrics();
	
        if(model.isEnabled()) {
            // *** paint the text normally
	    g.setColor(b.getForeground());
            BasicGraphicsUtils.drawString(g,text, model.getMnemonic(),
                                          textRect.x,
                                          textRect.y + fm.getAscent());
        } else {
            // *** paint the text disabled
	    g.setColor(b.getBackground().brighter());
            BasicGraphicsUtils.drawString(g,text,model.getMnemonic(),
                                          textRect.x, textRect.y + fm.getAscent());
            g.setColor(b.getBackground().darker());
            BasicGraphicsUtils.drawString(g,text,model.getMnemonic(),
                                          textRect.x - 1, textRect.y + fm.getAscent() - 1);
        }
    }
    
    // Basic does nothing - L&F like Motif paint the button's focus
    protected void paintFocus(Graphics g, AbstractButton b,
                              Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
    }
    
    // Basic does nothing - L&F like Motif shade the button
    protected void paintButtonPressed(Graphics g, AbstractButton b){
    }

}
