/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.plaf.metal;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;


/**
 * Metal's split pane divider
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.15 02/06/02
 * @author Steve Wilson
 * @author Ralph kar
 */
class MetalSplitPaneDivider extends BasicSplitPaneDivider
{
    private MetalBumps bumps = new MetalBumps(10, 10,
                                MetalLookAndFeel.getControlHighlight(),
                                MetalLookAndFeel.getControlDarkShadow(),
                                MetalLookAndFeel.getControl() );

    private MetalBumps focusBumps = new MetalBumps(10, 10,
                                MetalLookAndFeel.getPrimaryControlHighlight(),
                                MetalLookAndFeel.getPrimaryControlDarkShadow(),
                                MetalLookAndFeel.getPrimaryControl() );

    private int inset = 2;

    private Color controlColor = MetalLookAndFeel.getControl();
    private Color primaryControlColor = MetalLookAndFeel.getPrimaryControl();

    public MetalSplitPaneDivider(BasicSplitPaneUI ui) {
        super(ui);
        setLayout(new MetalDividerLayout());
    }

    public void paint(Graphics g) {
	MetalBumps usedBumps;
	if (splitPane.hasFocus()) {
	    usedBumps = focusBumps;
	    g.setColor(primaryControlColor);
	}
	else {
	    usedBumps = bumps;
	    g.setColor(controlColor);
	}
	Rectangle clip = g.getClipBounds();
	Insets insets = getInsets();
	g.fillRect(clip.x, clip.y, clip.width, clip.height);
        Dimension  size = getSize();
        size.width -= inset * 2;
        size.height -= inset * 2;
	int drawX = inset;
	int drawY = inset;
	if (insets != null) {
	    size.width -= (insets.left + insets.right);
	    size.height -= (insets.top + insets.bottom);
	    drawX += insets.left;
	    drawY += insets.top;
	}
        usedBumps.setBumpArea(size);
        usedBumps.paintIcon(this, g, drawX, drawY);
        super.paint(g);
    }

    /**
     * Creates and return an instance of JButton that can be used to
     * collapse the left component in the metal split pane.
     */
    protected JButton createLeftOneTouchButton() {
        JButton b = new JButton() {
            // Sprite buffer for the arrow image of the left button
            int[][]     buffer = {{0, 0, 0, 2, 2, 0, 0, 0, 0},
                                  {0, 0, 2, 1, 1, 1, 0, 0, 0},
                                  {0, 2, 1, 1, 1, 1, 1, 0, 0},
                                  {2, 1, 1, 1, 1, 1, 1, 1, 0},
                                  {0, 3, 3, 3, 3, 3, 3, 3, 3}};

            public void setBorder(Border b) {
            }

            public void paint(Graphics g) {
                JSplitPane splitPane = getSplitPaneFromSuper();
                if(splitPane != null) {
                    int         oneTouchSize = getOneTouchSizeFromSuper();
                    int         orientation = getOrientationFromSuper();
                    int         blockSize = Math.min(getDividerSize(),
                                                     oneTouchSize);

                    // Initialize the color array
                    Color[]     colors = {
                            this.getBackground(),
                            MetalLookAndFeel.getPrimaryControlDarkShadow(),
                            MetalLookAndFeel.getPrimaryControlInfo(),
                            MetalLookAndFeel.getPrimaryControlHighlight()};

                    // Fill the background first ...
                    g.setColor(this.getBackground());
                    g.fillRect(0, 0, this.getWidth(),
                               this.getHeight());

                    // ... then draw the arrow.
                    if (getModel().isPressed()) {
                            // Adjust color mapping for pressed button state
                            colors[1] = colors[2];
                    }
                    if(orientation == JSplitPane.VERTICAL_SPLIT) {
                            // Draw the image for a vertical split
                            for (int i=1; i<=buffer[0].length; i++) {
                                    for (int j=1; j<blockSize; j++) {
                                            if (buffer[j-1][i-1] == 0) {
                                                    continue;
                                            }
                                            else {
                                                g.setColor(
                                                    colors[buffer[j-1][i-1]]);
                                            }
                                            g.drawLine(i, j, i, j);
                                    }
                            }
                    }
                    else {
                            // Draw the image for a horizontal split
                            // by simply swaping the i and j axis.
                            // Except the drawLine() call this code is
                            // identical to the code block above. This was done
                            // in order to remove the additional orientation
                            // check for each pixel.
                            for (int i=1; i<=buffer[0].length; i++) {
                                    for (int j=1; j<blockSize; j++) {
                                            if (buffer[j-1][i-1] == 0) {
                                                    // Nothing needs
                                                    // to be drawn
                                                    continue;
                                            }
                                            else {
                                                    // Set the color from the
                                                    // color map
                                                    g.setColor(
                                                    colors[buffer[j-1][i-1]]);
                                            }
                                            // Draw a pixel
                                            g.drawLine(j, i, j, i);
                                    }
                            }
                    }
                }
            }

	    // Don't want the button to participate in focus traversable.
	    public boolean isFocusTraversable() {
		return false;
	    }
        };
	b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        return b;
    }

    /**
     * Creates and return an instance of JButton that can be used to
     * collapse the right component in the metal split pane.
     */
    protected JButton createRightOneTouchButton() {
        JButton b = new JButton() {
            // Sprite buffer for the arrow image of the right button
            int[][]     buffer = {{2, 2, 2, 2, 2, 2, 2, 2},
                                  {0, 1, 1, 1, 1, 1, 1, 3},
                                  {0, 0, 1, 1, 1, 1, 3, 0},
                                  {0, 0, 0, 1, 1, 3, 0, 0},
                                  {0, 0, 0, 0, 3, 0, 0, 0}};

            public void setBorder(Border border) {
            }

            public void paint(Graphics g) {
                JSplitPane splitPane = getSplitPaneFromSuper();
                if(splitPane != null) {
                    int         oneTouchSize = getOneTouchSizeFromSuper();
                    int         orientation = getOrientationFromSuper();
                    int         blockSize = Math.min(getDividerSize(),
                                                     oneTouchSize);

                    // Initialize the color array
                    Color[]     colors = {
                            this.getBackground(),
                            MetalLookAndFeel.getPrimaryControlDarkShadow(),
                            MetalLookAndFeel.getPrimaryControlInfo(),
                            MetalLookAndFeel.getPrimaryControlHighlight()};

                    // Fill the background first ...
                    g.setColor(this.getBackground());
                    g.fillRect(0, 0, this.getWidth(),
                               this.getHeight());

                    // ... then draw the arrow.
                    if (getModel().isPressed()) {
                            // Adjust color mapping for pressed button state
                            colors[1] = colors[2];
                    }
                    if(orientation == JSplitPane.VERTICAL_SPLIT) {
                            // Draw the image for a vertical split
                            for (int i=1; i<=buffer[0].length; i++) {
                                    for (int j=1; j<blockSize; j++) {
                                            if (buffer[j-1][i-1] == 0) {
                                                    continue;
                                            }
                                            else {
                                                g.setColor(
                                                    colors[buffer[j-1][i-1]]);
                                            }
                                            g.drawLine(i, j, i, j);
                                    }
                            }
                    }
                    else {
                            // Draw the image for a horizontal split
                            // by simply swaping the i and j axis.
                            // Except the drawLine() call this code is
                            // identical to the code block above. This was done
                            // in order to remove the additional orientation
                            // check for each pixel.
                            for (int i=1; i<=buffer[0].length; i++) {
                                    for (int j=1; j<blockSize; j++) {
                                            if (buffer[j-1][i-1] == 0) {
                                                    // Nothing needs
                                                    // to be drawn
                                                    continue;
                                            }
                                            else {
                                                    // Set the color from the
                                                    // color map
                                                    g.setColor(
                                                    colors[buffer[j-1][i-1]]);
                                            }
                                            // Draw a pixel
                                            g.drawLine(j, i, j, i);
                                    }
                            }
                    }
                }
            }

	    // Don't want the button to participate in focus traversable.
	    public boolean isFocusTraversable() {
		return false;
	    }
        };
	b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        return b;
    }

    /**
     * Used to layout a MetalSplitPaneDivider. Layout for the divider
     * involves appropriately moving the left/right buttons around.
     * <p>
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of MetalSplitPaneDivider.
     */
    public class MetalDividerLayout implements LayoutManager {
        public void layoutContainer(Container c) {
            JButton     leftButton = getLeftButtonFromSuper();
            JButton     rightButton = getRightButtonFromSuper();
            JSplitPane  splitPane = getSplitPaneFromSuper();
            int         orientation = getOrientationFromSuper();
            int         oneTouchSize = getOneTouchSizeFromSuper();
            int         oneTouchOffset = getOneTouchOffsetFromSuper();
	    Insets      insets = getInsets();

            // This layout differs from the one used in BasicSplitPaneDivider.
            // It does not center justify the oneTouchExpadable buttons.
            // This was necessary in order to meet the spec of the Metal
            // splitpane divider.
            if (leftButton != null && rightButton != null &&
                c == MetalSplitPaneDivider.this) {
                if (splitPane.isOneTouchExpandable()) {
                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
			int extraY = (insets != null) ? insets.top : 0;
			int blockSize = getDividerSize();

			if (insets != null) {
			    blockSize -= (insets.top + insets.bottom);
			}
			blockSize = Math.min(blockSize, oneTouchSize);
                        leftButton.setBounds(oneTouchOffset, extraY,
                                             blockSize * 2, blockSize);
                        rightButton.setBounds(oneTouchOffset +
                                              oneTouchSize * 2, extraY,
                                              blockSize * 2, blockSize);
                    }
                    else {
			int blockSize = getDividerSize();
			int extraX = (insets != null) ? insets.left : 0;

			if (insets != null) {
			    blockSize -= (insets.left + insets.right);
			}
			blockSize = Math.min(blockSize, oneTouchSize);
                        leftButton.setBounds(extraX, oneTouchOffset,
                                             blockSize, blockSize * 2);
                        rightButton.setBounds(extraX, oneTouchOffset +
                                              oneTouchSize * 2, blockSize,
                                              blockSize * 2);
                    }
                }
                else {
                    leftButton.setBounds(-5, -5, 1, 1);
                    rightButton.setBounds(-5, -5, 1, 1);
                }
            }
        }

        public Dimension minimumLayoutSize(Container c) {
            return new Dimension(0,0);
        }

        public Dimension preferredLayoutSize(Container c) {
            return new Dimension(0, 0);
        }

        public void removeLayoutComponent(Component c) {}

        public void addLayoutComponent(String string, Component c) {}
    }

    /*
     * The following methods only exist in order to be able to access protected
     * members in the superclass, because these are otherwise not available
     * in any inner class.
     */

    int getOneTouchSizeFromSuper() {
        return super.ONE_TOUCH_SIZE;
    }

    int getOneTouchOffsetFromSuper() {
        return super.ONE_TOUCH_OFFSET;
    }

    int getOrientationFromSuper() {
        return super.orientation;
    }

    JSplitPane getSplitPaneFromSuper() {
        return super.splitPane;
    }

    JButton getLeftButtonFromSuper() {
        return super.leftButton;
    }

    JButton getRightButtonFromSuper() {
        return super.rightButton;
    }
}
