/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.plaf.metal;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.DefaultTextUI;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.plaf.*;
import javax.swing.tree.*;

import javax.swing.plaf.basic.*;

/**
 * MetalTreeUI supports the client property "value-add" system of customization
 * It uses it to determine what style of line to draw.  There are three choices.
 * The default choice is to draw no lines.
 * Also available is a more variant with angled legs running from parent to child.
 * Lastly you can choose an option with horizonl lines be lines at all.  
 * Here is some code to turn on angled legs.
 * 
 * 	tree.putClientProperty("JTree.lineStyle", "Angled");
 *
 * Here is some code to turn on horizontal lines between root nodes.
 * 
 * 	tree.putClientProperty("JTree.lineStyle", "Horizontal");
 *
 *
 * Here is some code to turn off lines all together (which is the default).
 * 
 * 	tree.putClientProperty("JTree.lineStyle", "None");
 *
 *
 * @version 1.17 02/06/02
 * @author Tom Santos
 * @author Steve Wilson (value add stuff)
 */
public class MetalTreeUI extends BasicTreeUI {

    private static Color lineColor;
  
    private static final String LINE_STYLE = "JTree.lineStyle";

    private static final String LEG_LINE_STYLE_STRING = "Angled";
    private static final String HORIZ_STYLE_STRING = "Horizontal";
    private static final String NO_STYLE_STRING = "None";

    private static final int LEG_LINE_STYLE = 2; 
    private static final int HORIZ_LINE_STYLE = 1;
    private static final int NO_LINE_STYLE = 0;

    private int lineStyle = HORIZ_LINE_STYLE;
    private PropertyChangeListener lineStyleListener = new LineListener();

    // Boilerplate
    public static ComponentUI createUI(JComponent x) {
	return new MetalTreeUI();
    }

    public MetalTreeUI()
    {
	super();
    }

    protected int getHorizontalLegBuffer()
      {
	return 4;
      } 

    public void installUI( JComponent c ) {
        super.installUI( c );
	lineColor = UIManager.getColor( "Tree.line" );

	Object lineStyleFlag = c.getClientProperty( LINE_STYLE );
	decodeLineStyle(lineStyleFlag);
	c.addPropertyChangeListener(lineStyleListener);

    }

    public void uninstallUI( JComponent c) {
         c.removePropertyChangeListener(lineStyleListener);
	 super.uninstallUI(c);
    }

    /** this function converts between the string passed into the client property
      * and the internal representation (currently and int)
      *
      */
    protected void decodeLineStyle(Object lineStyleFlag) {
      if ( lineStyleFlag == null || lineStyleFlag.equals(NO_STYLE_STRING) ){
	lineStyle = NO_LINE_STYLE; // default case
      } else {
	  if ( lineStyleFlag.equals(LEG_LINE_STYLE_STRING) ) {
	      lineStyle = LEG_LINE_STYLE;
	  } else if ( lineStyleFlag.equals(HORIZ_STYLE_STRING) ) {
	      lineStyle = HORIZ_LINE_STYLE;
	  }
      }

    }

    protected boolean isLocationInExpandControl(int row, int rowLevel,
						int mouseX, int mouseY) {
	if(tree != null && !isLeaf(row)) {
	    int                     boxWidth;

	    if(getExpandedIcon() != null)
		boxWidth = getExpandedIcon().getIconWidth() + 6;
	    else
		boxWidth = 8;

	    Insets i = tree.getInsets();
	    int    boxLeftX = (i != null) ? i.left : 0;


	    boxLeftX += (((rowLevel + depthOffset - 1) * totalChildIndent) +
			getLeftChildIndent()) - boxWidth/2;

	    int boxRightX = boxLeftX + boxWidth;
	
	    return mouseX >= boxLeftX && mouseX <= boxRightX;
	}
	return false;
    }

    public void paint(Graphics g, JComponent c) {
        super.paint( g, c );
 

	// Paint the lines
	if (lineStyle == HORIZ_LINE_STYLE && !largeModel) {
	    paintHorizontalSeparators(g,c);
	}
    }

    protected void paintHorizontalSeparators(Graphics g, JComponent c) {
        g.setColor( lineColor );

	Rectangle clipBounds = g.getClipBounds();

	int beginRow = getRowForPath(tree, getClosestPathForLocation
				     (tree, 0, clipBounds.y));
	int endRow = getRowForPath(tree, getClosestPathForLocation
			     (tree, 0, clipBounds.y + clipBounds.height - 1));

	if ( beginRow <= -1 || endRow <= -1 ) {
	    return;
	}

	for ( int i = beginRow; i <= endRow; ++i ) {
	    TreePath        path = getPathForRow(tree, i);

	    if(path != null && path.getPathCount() == 2) {
		Rectangle       rowBounds = getPathBounds(tree,getPathForRow
							  (tree, i));

	        // Draw a line at the top
		if(rowBounds != null)
		    g.drawLine(clipBounds.x, rowBounds.y,
			       clipBounds.x + clipBounds.width, rowBounds.y);
	    }
	}

    }

    protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds,
					  Insets insets, TreePath path) {
	if (lineStyle == LEG_LINE_STYLE) {
	    super.paintVerticalPartOfLeg(g, clipBounds, insets, path);
	}
    }

    protected void paintHorizontalPartOfLeg(Graphics g, Rectangle clipBounds,
					    Insets insets, Rectangle bounds,
					    TreePath path, int row,
					    boolean isExpanded,
					    boolean hasBeenExpanded, boolean
					    isLeaf) {
	if (lineStyle == LEG_LINE_STYLE) {
	    super.paintHorizontalPartOfLeg(g, clipBounds, insets, bounds,
					   path, row, isExpanded,
					   hasBeenExpanded, isLeaf);
	}
    }

    /** This class listens for changes in line style */
    class LineListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
	    String name = e.getPropertyName();
	    if ( name.equals( LINE_STYLE ) ) {
	        decodeLineStyle(e.getNewValue());
	    }
	}
    } // end class PaletteListener

}
