/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.plaf.UIResource;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Component;
import java.awt.Polygon;
import java.io.Serializable;

/**
 * Factory object that can vend Icons appropriate for the basic L & F.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with 
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.23 02/06/02
 * @author David Kloba
 * @author Georges Saab
 */
public class BasicIconFactory implements Serializable
{
    private static Icon frame_icon;
    private static Icon checkBoxIcon;
    private static Icon radioButtonIcon;
    private static Icon checkBoxMenuItemIcon;
    private static Icon radioButtonMenuItemIcon;
    private static Icon menuItemCheckIcon;
    private static Icon menuItemArrowIcon;
    private static Icon menuArrowIcon;

    public static Icon getMenuItemCheckIcon() {
	if (menuItemCheckIcon == null) {
	    menuItemCheckIcon = new MenuItemCheckIcon();
	}
	return menuItemCheckIcon;
    }

    public static Icon getMenuItemArrowIcon() {
	if (menuItemArrowIcon == null) {
	    menuItemArrowIcon = new MenuItemArrowIcon();
	}
	return menuItemArrowIcon;
    }

    public static Icon getMenuArrowIcon() {
	if (menuArrowIcon == null) {
	    menuArrowIcon = new MenuArrowIcon();
	}
	return menuArrowIcon;
    }

    public static Icon getCheckBoxIcon() {
	if (checkBoxIcon == null) {
	    checkBoxIcon = new CheckBoxIcon();
	}
	return checkBoxIcon;
    }

    public static Icon getRadioButtonIcon() {
	if (radioButtonIcon == null) {
	    radioButtonIcon = new RadioButtonIcon();
	}
	return radioButtonIcon;
    }

    public static Icon getCheckBoxMenuItemIcon() {
	if (checkBoxMenuItemIcon == null) {
	    checkBoxMenuItemIcon = new CheckBoxMenuItemIcon();
	}
	return checkBoxMenuItemIcon;
    }

    public static Icon getRadioButtonMenuItemIcon() {
	if (radioButtonMenuItemIcon == null) {
	    radioButtonMenuItemIcon = new RadioButtonMenuItemIcon();
	}
	return radioButtonMenuItemIcon;
    }

    public static Icon createEmptyFrameIcon() {
	if(frame_icon == null)
	    frame_icon = new EmptyFrameIcon();
	return frame_icon;
    }

    private static class EmptyFrameIcon implements Icon, Serializable {
        int height = 16;
        int width = 14;
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }
        public int getIconWidth() { return width; }
        public int getIconHeight() { return height; }
    };

    private static class CheckBoxIcon implements Icon, Serializable
    {
	final static int csize = 13;
	public void paintIcon(Component c, Graphics g, int x, int y) {
	}

	public int getIconWidth() {
	    return csize;
	}
		
	public int getIconHeight() {
	    return csize;
	}
    }

    private static class RadioButtonIcon implements Icon, UIResource, Serializable
    {
	public void paintIcon(Component c, Graphics g, int x, int y) {
	}

	public int getIconWidth() {
	    return 13;
	}
		
	public int getIconHeight() {
	    return 13;
	}
    } // end class RadioButtonIcon


    private static class CheckBoxMenuItemIcon implements Icon, UIResource, Serializable 
    {
	public void paintIcon(Component c, Graphics g, int x, int y) {
	    AbstractButton b = (AbstractButton) c;
	    ButtonModel model = b.getModel();
	    boolean isSelected = model.isSelected();
	    if (isSelected) {
                g.drawLine(x+7, y+1, x+7, y+3);
                g.drawLine(x+6, y+2, x+6, y+4);
                g.drawLine(x+5, y+3, x+5, y+5);
                g.drawLine(x+4, y+4, x+4, y+6);
                g.drawLine(x+3, y+5, x+3, y+7);
                g.drawLine(x+2, y+4, x+2, y+6);
                g.drawLine(x+1, y+3, x+1, y+5);
	    }
	}
	public int getIconWidth() { return 9; }
	public int getIconHeight() { return 9; }

    } // End class CheckBoxMenuItemIcon

    
    private static class RadioButtonMenuItemIcon implements Icon, UIResource, Serializable 
    {
	public void paintIcon(Component c, Graphics g, int x, int y) {
	    AbstractButton b = (AbstractButton) c;
	    ButtonModel model = b.getModel();
	    if (b.isSelected() == true) {
		g.fillArc(x+1,y+1,getIconWidth()-2, getIconHeight()-2, 0, 360);
	    }            
	}
        public int getIconWidth() { return 9; }  // was 12
	public int getIconHeight() { return 9; }

    } // End class RadioButtonMenuItemIcon


    private static class MenuItemCheckIcon implements Icon, UIResource, Serializable{
	public void paintIcon(Component c, Graphics g, int x, int y) {
	}
	public int getIconWidth() { return 9; }
	public int getIconHeight() { return 9; }

    } // End class MenuItemCheckIcon

    private static class MenuItemArrowIcon implements Icon, UIResource, Serializable {
	public void paintIcon(Component c, Graphics g, int x, int y) {
	}
	public int getIconWidth() { return 4; }
	public int getIconHeight() { return 8; }

    } // End class MenuItemArrowIcon

    private static class MenuArrowIcon implements Icon, UIResource, Serializable {
	public void paintIcon(Component c, Graphics g, int x, int y) {
	    Polygon p = new Polygon();
	    p.addPoint(x, y);
	    p.addPoint(x+getIconWidth(), y+getIconHeight()/2);
	    p.addPoint(x, y+getIconHeight());
	    g.fillPolygon(p);

	}
	public int getIconWidth() { return 4; }
	public int getIconHeight() { return 8; }
    } // End class MenuArrowIcon
}

