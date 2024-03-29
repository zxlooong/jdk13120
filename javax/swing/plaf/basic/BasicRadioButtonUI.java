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
 * RadioButtonUI implementation for BasicRadioButtonUI
 *
 * @version 1.62 02/06/02
 * @author Jeff Dinkins
 */
public class BasicRadioButtonUI extends BasicToggleButtonUI 
{
    private final static BasicRadioButtonUI radioButtonUI = new BasicRadioButtonUI();

    protected Icon icon;

    private boolean defaults_initialized = false;

    private final static String propertyPrefix = "RadioButton" + "."; 

    // ********************************
    //        Create PLAF 
    // ********************************
    public static ComponentUI createUI(JComponent b) {
        return radioButtonUI;
    }

    protected String getPropertyPrefix() {
        return propertyPrefix; 
    }

    // ********************************
    //        Install PLAF 
    // ********************************
    protected void installDefaults(AbstractButton b){
        super.installDefaults(b);
        if(!defaults_initialized) {
            icon = UIManager.getIcon(getPropertyPrefix() + "icon");
            defaults_initialized = true;
        }
    }

    // ********************************
    //        Uninstall PLAF 
    // ********************************
    protected void uninstallDefaults(AbstractButton b){
        super.uninstallDefaults(b);
        defaults_initialized = false;
    }

    public Icon getDefaultIcon() {
        return icon;
    }


    /* These Dimensions/Rectangles are allocated once for all 
     * RadioButtonUI.paint() calls.  Re-using rectangles 
     * rather than allocating them in each paint call substantially 
     * reduced the time it took paint to run.  Obviously, this 
     * method can't be re-entered.
     */
    private static Dimension size = new Dimension();
    private static Rectangle viewRect = new Rectangle();
    private static Rectangle iconRect = new Rectangle();
    private static Rectangle textRect = new Rectangle();

    /**
     * paint the radio button
     */
    public synchronized void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();

        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();

        size = b.getSize(size);
        viewRect.x = viewRect.y = 0;
        viewRect.width = size.width;
        viewRect.height = size.height;
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
        textRect.x = textRect.y = textRect.width = textRect.height = 0;

        Icon altIcon = b.getIcon();
        Icon selectedIcon = null;
        Icon disabledIcon = null;

        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), altIcon != null ? altIcon : getDefaultIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect, getDefaultTextIconGap(b));

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
                                                 textRect.x, 
                                                 textRect.y + fm.getAscent());
               } else {
                   // *** paint the text disabled
                   g.setColor(b.getBackground().brighter());
                   BasicGraphicsUtils.drawString(g,text,model.getMnemonic(),
                                                 textRect.x + 1, 
                                                 textRect.y + fm.getAscent() + 1);
                   g.setColor(b.getBackground().darker());
                   BasicGraphicsUtils.drawString(g,text,model.getMnemonic(),
                                                 textRect.x, 
                                                 textRect.y + fm.getAscent());
               }
               if(b.hasFocus() && b.isFocusPainted() && 
                  textRect.width > 0 && textRect.height > 0 ) {
                   paintFocus(g, textRect, size);
               }
           }
        }
    }

    protected void paintFocus(Graphics g, Rectangle textRect, Dimension size){
    }


    /* These Insets/Rectangles are allocated once for all 
     * RadioButtonUI.getPreferredSize() calls.  Re-using rectangles 
     * rather than allocating them in each call substantially 
     * reduced the time it took getPreferredSize() to run.  Obviously, 
     * this method can't be re-entered.
     */
    private static Rectangle prefViewRect = new Rectangle();
    private static Rectangle prefIconRect = new Rectangle();
    private static Rectangle prefTextRect = new Rectangle();
    private static Insets prefInsets = new Insets(0, 0, 0, 0);

    /**
     * The preferred size of the radio button
     */
    public Dimension getPreferredSize(JComponent c) {
        if(c.getComponentCount() > 0) {
            return null;
        }

        AbstractButton b = (AbstractButton) c;

        String text = b.getText();

        Icon buttonIcon = (Icon) b.getIcon();
        if(buttonIcon == null) {
            buttonIcon = getDefaultIcon();
        }

        Font font = b.getFont();
        FontMetrics fm = b.getToolkit().getFontMetrics(font);

        prefViewRect.x = prefViewRect.y = 0;
        prefViewRect.width = Short.MAX_VALUE;
        prefViewRect.height = Short.MAX_VALUE;
        prefIconRect.x = prefIconRect.y = prefIconRect.width = prefIconRect.height = 0;
        prefTextRect.x = prefTextRect.y = prefTextRect.width = prefTextRect.height = 0;

        SwingUtilities.layoutCompoundLabel(
            c, fm, text, buttonIcon,
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            prefViewRect, prefIconRect, prefTextRect, 
            text == null ? 0 : getDefaultTextIconGap(b));

        // find the union of the icon and text rects (from Rectangle.java)
        int x1 = Math.min(prefIconRect.x, prefTextRect.x);
        int x2 = Math.max(prefIconRect.x + prefIconRect.width, 
                          prefTextRect.x + prefTextRect.width);
        int y1 = Math.min(prefIconRect.y, prefTextRect.y);
        int y2 = Math.max(prefIconRect.y + prefIconRect.height, 
                          prefTextRect.y + prefTextRect.height);
        int width = x2 - x1;
        int height = y2 - y1;

        prefInsets = b.getInsets(prefInsets);
        width += prefInsets.left + prefInsets.right;
        height += prefInsets.top + prefInsets.bottom;
        return new Dimension(width, height);
    }
}
