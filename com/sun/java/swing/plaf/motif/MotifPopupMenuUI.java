/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.java.swing.plaf.motif;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicPopupMenuUI;


/**
 * A Motif L&F implementation of PopupMenuUI. 
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.22 02/06/02
 * @author Georges Saab
 * @author Rich Schiavi
 */
public class MotifPopupMenuUI extends BasicPopupMenuUI {
    private static Border border = null;
    private Font titleFont = null;

    public static ComponentUI createUI(JComponent x) {
	return new MotifPopupMenuUI();
    }

    /* This has to deal with the fact that the title may be wider than
       the widest child component.
       */
    public Dimension getPreferredSize(JComponent c) {
	LayoutManager layout = c.getLayout();
	Dimension d = layout.preferredLayoutSize(c);
	String title = ((JPopupMenu)c).getLabel();
	if (titleFont == null) {
	    UIDefaults table = UIManager.getLookAndFeelDefaults();
	    titleFont = table.getFont("PopupMenu.font");
	}
	FontMetrics fm = c.getFontMetrics(titleFont);
	int         stringWidth = 0;
        
        if (title!=null) {
            stringWidth +=fm.stringWidth(title);
        }

	if (d.width < stringWidth) {
	    d.width = stringWidth + 8;
	    Insets i = c.getInsets();
	    if (i!=null) {
		d.width += i.left + i.right;
	    }
	    if (border != null) {
		i = border.getBorderInsets(c);
		d.width += i.left + i.right;
	    }

	    return d;
	}
	return null;
    }

    protected ChangeListener createChangeListener(JPopupMenu m) {
	return new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {}
	};
    }

    public boolean isPopupTrigger(MouseEvent e) {
	return ((e.getID()==MouseEvent.MOUSE_PRESSED) 
		&& ((e.getModifiers() & MouseEvent.BUTTON3_MASK)!=0));
    }	    
}




