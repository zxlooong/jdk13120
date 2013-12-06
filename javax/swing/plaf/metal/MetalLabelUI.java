/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.plaf.metal;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;


import java.awt.*;


/**
 * A Windows L&F implementation of LabelUI.  This implementation 
 * is completely static, i.e. there's only one UIView implementation 
 * that's shared by all JLabel objects.
 *
 * @version 1.10 06/06/06
 * @author Hans Muller
 */

public class MetalLabelUI extends BasicLabelUI
{
    protected static MetalLabelUI metalLabelUI = new MetalLabelUI();
    private final static MetalLabelUI SAFE_METAL_LABEL_UI = new MetalLabelUI();

    public static ComponentUI createUI(JComponent c) {
        if (System.getSecurityManager() != null) {
            return SAFE_METAL_LABEL_UI;
        } else {
	return metalLabelUI;
        }
    }

    /**
     * Just paint the text gray (Label.disabledForeground) rather than 
     * in the labels foreground color.
     *
     * @see #paint
     * @see #paintEnabledText
     */
    protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY)
    {
	int accChar = l.getDisplayedMnemonic();
	g.setColor(UIManager.getColor("Label.disabledForeground"));
	BasicGraphicsUtils.drawString(g, s, accChar, textX, textY);
    }
}

