/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;

/**
 * EditableView sets the view it contains to be visible only when the
 * JTextComponent the view is contained in is editable. The min/pref/max
 * size is 0 when not visible.
 *
 * @author  Scott Violet
 * @version 1.6, 02/06/02
 */
class EditableView extends ComponentView {

    EditableView(Element e) {
	super(e);
    }

    public float getMinimumSpan(int axis) {
	if (isVisible) {
	    return super.getMinimumSpan(axis);
	}
	return 0;
    }

    public float getPreferredSpan(int axis) {
	if (isVisible) {
	    return super.getPreferredSpan(axis);
	}
	return 0;
    }

    public float getMaximumSpan(int axis) {
	if (isVisible) {
	    return super.getMaximumSpan(axis);
	}
	return 0;
    }

    public void paint(Graphics g, Shape allocation) {
	Component c = getComponent();
	Container host = getContainer();
	
	if (host != null &&
	    isVisible != ((JTextComponent)host).isEditable()) {
	    isVisible = ((JTextComponent)host).isEditable();
	    preferenceChanged(null, true, true);
	    host.repaint();
	}
	/*
	 * Note: we cannot tweak the visible state of the
	 * component in createComponent() even though it
	 * gets called after the setParent() call where
	 * the value of the boolean is set.  This 
	 * because, the setComponentParent() in the 
	 * superclass, always does a setVisible(false)
	 * after calling createComponent().   We therefore
	 * use this flag in the paint() method to 
	 * setVisible() to true if required.
	 */
	if (isVisible) {
	    super.paint(g, allocation);
	}
	else {
	    setSize(0, 0);
	}
    }

    public void setParent(View parent) {
	if (parent != null) {
	    Container host = parent.getContainer();
	    if (host != null) {
		isVisible = ((JTextComponent)host).isEditable();
	    }
	}
	super.setParent(parent);
    }
	
    /**
     * @return true if the Component is visible.
     */
    public boolean isVisible() {
	return isVisible;
    }

    /** Set to true if the component is visible. This is based off the
     * editability of the container. */
    private boolean isVisible;
} // End of EditableView
