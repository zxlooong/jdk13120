/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */



package javax.swing.plaf.basic;



import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.border.Border;
import java.beans.*;
import java.io.*;



/**
 * Divider used by BasicSplitPaneUI. Subclassers may wish to override
 * paint to do something more interesting.
 * The border effect is drawn in BasicSplitPaneUI, so if you don't like
 * that border, reset it there.
 * To conditionally drag from certain areas subclass mousePressed and
 * call super when you wish the dragging to begin.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.39 02/06/02
 * @author Scott Violet
 */
public class BasicSplitPaneDivider extends Container
    implements PropertyChangeListener
{
    /**
     * Width or height of the divider based on orientation
     * BasicSplitPaneUI adds two to this.
     */
    protected static final int ONE_TOUCH_SIZE = 6;
    protected static final int ONE_TOUCH_OFFSET = 2;

    /**
     * Handles mouse dragging message to do the actual dragging.
     */
    protected DragController dragger;

    /**
     * UI this instance was created from.
     */
    protected BasicSplitPaneUI splitPaneUI;

    /**
     * Size of the divider.
     */
    protected int dividerSize = 0; // default - SET TO 0???

    /**
     * Divider that is used for noncontinuous layout mode.
     */
    protected Component hiddenDivider;

    /**
     * JSplitPane the receiver is contained in.
     */
    protected JSplitPane splitPane;

    /**
     * Handles mouse events from both this class, and the split pane.
     * Mouse events are handled for the splitpane since you want to be able
     * to drag when clicking on the border of the divider, which is not 
     * drawn by the divider.
     */
    protected MouseHandler mouseHandler;

    /**
     * Orientation of the JSplitPane.
     */
    protected int orientation;

    /**
     * Button for quickly toggling the left component.
     */
    protected JButton leftButton;

    /**
     * Button for quickly toggling the right component.
     */
    protected JButton rightButton;

    /**
     * Cursor used for HORIZONTAL_SPLIT splitpanes.
     */
    static final Cursor horizontalCursor =
                            Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

    /**
     * Cursor used for VERTICAL_SPLIT splitpanes.
     */
    static final Cursor verticalCursor =
                            Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);

    /**
     * Default cursor.
     */
    static final Cursor defaultCursor =
                            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    /** Border. */
    private Border border;


    /**
     * Creates an instance of BasicSplitPaneDivider. Registers this
     * instance for mouse events and mouse dragged events.
     */
    public BasicSplitPaneDivider(BasicSplitPaneUI ui) {
        setLayout(new DividerLayout());
        setBasicSplitPaneUI(ui);
        orientation = splitPane.getOrientation();
        setBackground(UIManager.getColor("SplitPane.background"));
    }


    /**
     * Sets the SplitPaneUI that is using the receiver.
     */
    public void setBasicSplitPaneUI(BasicSplitPaneUI newUI) {
        if (splitPane != null) {
            splitPane.removePropertyChangeListener(this);
           if (mouseHandler != null) {
               splitPane.removeMouseListener(mouseHandler);
               splitPane.removeMouseMotionListener(mouseHandler);
	       removeMouseListener(mouseHandler);
	       removeMouseMotionListener(mouseHandler);
               mouseHandler = null;
           }
        }
        splitPaneUI = newUI;
        if (newUI != null) {
            splitPane = newUI.getSplitPane();
            if (splitPane != null) {
                if (mouseHandler == null) mouseHandler = new MouseHandler();
                splitPane.addMouseListener(mouseHandler);
                splitPane.addMouseMotionListener(mouseHandler);
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
                splitPane.addPropertyChangeListener(this);
                if (splitPane.isOneTouchExpandable()) {
                    oneTouchExpandableChanged();
                }
            }
        }
        else {
            splitPane = null;
        }
    }


    /**
     * Returns the <code>SplitPaneUI</code> the receiver is currently
     * in.
     */
    public BasicSplitPaneUI getBasicSplitPaneUI() {
        return splitPaneUI;
    }


    /**
     * Sets the size of the divider to <code>newSize</code>. That is
     * the width if the splitpane is <code>HORIZONTAL_SPLIT</code>, or
     * the height of <code>VERTICAL_SPLIT</code>.
     */
    public void setDividerSize(int newSize) {
        dividerSize = newSize;
    }


    /**
     * Returns the size of the divider, that is the width if the splitpane
     * is HORIZONTAL_SPLIT, or the height of VERTICAL_SPLIT.
     */
    public int getDividerSize() {
        return dividerSize;
    }


    /**
     * Sets the border of this component.
     * @since 1.3
     */
    public void setBorder(Border border) {
        Border         oldBorder = this.border;

        this.border = border;
    }

    /**
     * Returns the border of this component or null if no border is
     * currently set.
     *
     * @return the border object for this component
     * @see #setBorder
     * @since 1.3
     */
    public Border getBorder() {
        return border;
    }

    /**
     * If a border has been set on this component, returns the
     * border's insets, else calls super.getInsets.
     *
     * @return the value of the insets property.
     * @see #setBorder
     */
    public Insets getInsets() {
	Border    border = getBorder();

        if (border != null) {
            return border.getBorderInsets(this);
        }
    	return super.getInsets();
    }

    /**
     * Returns dividerSize x dividerSize
     */
    public Dimension getPreferredSize() {
        return new Dimension(getDividerSize(), getDividerSize());
    }

    /**
     * Returns dividerSize x dividerSize
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }


    /**
     * Property change event, presumably from the JSplitPane, will message
     * updateOrientation if necessary.
     */
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getSource() == splitPane) {
            if (e.getPropertyName().equals(JSplitPane.ORIENTATION_PROPERTY)) {
                orientation = splitPane.getOrientation();
                invalidate();
                validate();
            }
            else if (e.getPropertyName().equals(JSplitPane.
                      ONE_TOUCH_EXPANDABLE_PROPERTY)) {
                oneTouchExpandableChanged();
            }
        }
    }


    /**
     * Paints the divider.
     */
    public void paint(Graphics g) {
      super.paint(g);

      // Paint the border.
      Border   border = getBorder();

      if (border != null) {
	  Dimension     size = getSize();

	  border.paintBorder(this, g, 0, 0, size.width, size.height);
      }
    }


    /**
     * Messaged when the oneTouchExpandable value of the JSplitPane the
     * receiver is contained in changes. Will create the
     * <code>leftButton</code> and <code>rightButton</code> if they
     * are null. invalidates the receiver as well.
     */
    protected void oneTouchExpandableChanged() {
        if (splitPane.isOneTouchExpandable() &&
            leftButton == null &&
            rightButton == null) {
            /* Create the left button and add an action listener to
               expand/collapse it. */
            leftButton = createLeftOneTouchButton();
            if (leftButton != null)
                leftButton.addActionListener(new OneTouchActionHandler(true));


            /* Create the right button and add an action listener to
               expand/collapse it. */
            rightButton = createRightOneTouchButton();
            if (rightButton != null)
                rightButton.addActionListener(new OneTouchActionHandler
		    (false));

            if (leftButton != null && rightButton != null) {
                add(leftButton);
                add(rightButton);
            }
        }
        invalidate();
        validate();
    }


    /**
     * Creates and return an instance of JButton that can be used to
     * collapse the left component in the split pane.
     */
    protected JButton createLeftOneTouchButton() {
        JButton b = new JButton() {
            public void setBorder(Border b) {
            }
            public void paint(Graphics g) {
                if (splitPane != null) {
                    int[]   xs = new int[3];
                    int[]   ys = new int[3];
                    int     blockSize = Math.min(getDividerSize(),
                                                 ONE_TOUCH_SIZE);

                    // Fill the background first ...
                    g.setColor(this.getBackground());
                    g.fillRect(0, 0, this.getWidth(),
                               this.getHeight());

                    // ... then draw the arrow.
                    g.setColor(Color.black);
                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
                        xs[0] = blockSize;
                        xs[1] = 0;
                        xs[2] = blockSize << 1;
                        ys[0] = 0;
                        ys[1] = ys[2] = blockSize;
                        g.drawPolygon(xs, ys, 3); // Little trick to make the
                                                  // arrows of equal size
                    }
                    else {
                        xs[0] = xs[2] = blockSize;
                        xs[1] = 0;
                        ys[0] = 0;
                        ys[1] = blockSize;
                        ys[2] = blockSize << 1;
                    }
                    g.fillPolygon(xs, ys, 3);
                }
            }
	    // Don't want the button to participate in focus traversable.
	    public boolean isFocusTraversable() {
		return false;
	    }
        };
	b.setCursor(defaultCursor);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        return b;
    }


    /**
     * Creates and return an instance of JButton that can be used to
     * collapse the right component in the split pane.
     */
    protected JButton createRightOneTouchButton() {
        JButton b = new JButton() {
            public void setBorder(Border border) {
            }
            public void paint(Graphics g) {
                if (splitPane != null) {
                    int[]          xs = new int[3];
                    int[]          ys = new int[3];
                    int            blockSize = Math.min(getDividerSize(),
                                                        ONE_TOUCH_SIZE);

                    // Fill the background first ...
                    g.setColor(this.getBackground());
                    g.fillRect(0, 0, this.getWidth(),
                               this.getHeight());

                    // ... then draw the arrow.
                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
                        xs[0] = blockSize;
                        xs[1] = blockSize << 1;
                        xs[2] = 0;
                        ys[0] = blockSize;
                        ys[1] = ys[2] = 0;
                    }
                    else {
                        xs[0] = xs[2] = 0;
                        xs[1] = blockSize;
                        ys[0] = 0;
                        ys[1] = blockSize;
                        ys[2] = blockSize << 1;
                    }
                    g.setColor(Color.black);
                    g.fillPolygon(xs, ys, 3);
                }
            }
	    // Don't want the button to participate in focus traversable.
	    public boolean isFocusTraversable() {
		return false;
	    }
        };
	b.setCursor(defaultCursor);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        return b;
    }


    /**
     * Message to prepare for dragging. This messages the BasicSplitPaneUI
     * with startDragging.
     */
    protected void prepareForDragging() {
        splitPaneUI.startDragging();
    }


    /**
     * Messages the BasicSplitPaneUI with dragDividerTo that this instance
     * is contained in.
     */
    protected void dragDividerTo(int location) {
        splitPaneUI.dragDividerTo(location);
    }


    /**
     * Messages the BasicSplitPaneUI with finishDraggingTo that this instance
     * is contained in.
     */
    protected void finishDraggingTo(int location) {
        splitPaneUI.finishDraggingTo(location);
    }


    /**
     * MouseHandler is responsible for converting mouse events
     * (released, dragged...) into the appropriate DragController 
     * methods.
     * <p>
     */
    protected class MouseHandler extends MouseAdapter
            implements MouseMotionListener
    {
        /**
         * Starts the dragging session by creating the appropriate instance
         * of DragController.
         */
        public void mousePressed(MouseEvent e) {
            if ((e.getSource() == BasicSplitPaneDivider.this ||
		 e.getSource() == splitPane) &&
		dragger == null &&splitPane.isEnabled()) {
                Component            newHiddenDivider = splitPaneUI.
                                     getNonContinuousLayoutDivider();

                if (hiddenDivider != newHiddenDivider) {
                    if (hiddenDivider != null) {
                        hiddenDivider.removeMouseListener(this);
                        hiddenDivider.removeMouseMotionListener(this);
                    }
                    hiddenDivider = newHiddenDivider;
                    if (hiddenDivider != null) {
                        hiddenDivider.addMouseMotionListener(this);
                        hiddenDivider.addMouseListener(this);
                    }
                }
                if (splitPane.getLeftComponent() != null &&
                    splitPane.getRightComponent() != null) {
                    if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
                        dragger = new DragController(e);
                    }
                    else {
                        dragger = new VerticalDragController(e);
                    }
                    if (!dragger.isValid()) {
                        dragger = null;
                    }
                    else {
                        prepareForDragging();
                        dragger.continueDrag(e);
                    }
                }
                e.consume();
            }
        }


        /**
         * If dragger is not null it is messaged with completeDrag.
         */
        public void mouseReleased(MouseEvent e) {
            if (dragger != null) {
                if (e.getSource() == splitPane) {
                    dragger.completeDrag(e.getX(), e.getY());
                }
		else if (e.getSource() == BasicSplitPaneDivider.this) {
                    Point   ourLoc = getLocation();

		    dragger.completeDrag(e.getX() + ourLoc.x,
					 e.getY() + ourLoc.y);
		}
                else if (e.getSource() == hiddenDivider) {
                    Point   hDividerLoc = hiddenDivider.getLocation();
                    int     ourX = e.getX() + hDividerLoc.x;
                    int     ourY = e.getY() + hDividerLoc.y;
                    
                    dragger.completeDrag(ourX, ourY);
                }
                dragger = null;
                e.consume();
            }
        }


        //
        // MouseMotionListener
        //

        /**
         * If dragger is not null it is messaged with continueDrag.
         */
        public void mouseDragged(MouseEvent e) {
            if (dragger != null) {
                if (e.getSource() == splitPane) {
                    dragger.continueDrag(e.getX(), e.getY());
                }
		else if (e.getSource() == BasicSplitPaneDivider.this) {
                    Point   ourLoc = getLocation();
                    
                    dragger.continueDrag(e.getX() + ourLoc.x,
					 e.getY() + ourLoc.y);
		}
                else if (e.getSource() == hiddenDivider) {
                    Point   hDividerLoc = hiddenDivider.getLocation();
                    int     ourX = e.getX() + hDividerLoc.x;
                    int     ourY = e.getY() + hDividerLoc.y;
                    
                    dragger.continueDrag(ourX, ourY);
                }
                e.consume();
            }
        }


        /**
         *  Resets the cursor based on the orientation.
         */
        public void mouseMoved(MouseEvent e) {
            if (dragger != null) return;

            int         eventX = e.getX();
            int         eventY = e.getY();
            Rectangle   bounds = getBounds();
	    Cursor      newCursor;

	    if (e.getSource() == BasicSplitPaneDivider.this) {
		if (eventX >= 0 && eventX < bounds.width &&
		    eventY >= 0 && eventY < bounds.height) {
		    newCursor = (orientation == JSplitPane.HORIZONTAL_SPLIT) ?
			        horizontalCursor : verticalCursor;
		}
		else {
		    newCursor = defaultCursor;
		}
	    }
	    else {
		if (eventX >= bounds.x &&
		    eventX < (bounds.x + bounds.width) &&
		    eventY >= bounds.y &&
		    eventY < (bounds.y + bounds.height)) {
		    newCursor = (orientation == JSplitPane.HORIZONTAL_SPLIT) ?
			        horizontalCursor : verticalCursor;
		}
		else {
		    newCursor = defaultCursor;
		}
	    }
	    if (getCursor() != newCursor) {
		setCursor(newCursor);
            }
        }
    }


    /**
     * Handles the events during a dragging session for a
     * HORIZONTAL_SPLIT orientated split pane. This continually
     * messages dragDividerTo and then when done messages
     * finishDraggingTo. When an instance is created it should be
     * messaged with isValid() to insure that dragging can happen
     * (dragging won't be allowed if the two views can not be resized).
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    protected class DragController
    {
        /**
         * Initial location of the divider.
         */
        int initialX;

        /**
         * Maximum and minimum positions to drag to.
         */
        int maxX, minX;

        /**
         * Initial location the mouse down happened at.
         */
        int offset;


        protected DragController(MouseEvent e) {
            JSplitPane  splitPane = splitPaneUI.getSplitPane();
            Component   leftC = splitPane.getLeftComponent();
	    Component   rightC = splitPane.getRightComponent();

            initialX = getLocation().x;
	    if (e.getSource() == BasicSplitPaneDivider.this) {
		offset = e.getX();
	    }
	    else { // splitPane
		offset = e.getX() - initialX;
	    }
	    if (leftC == null || rightC == null || offset < -1 ||
		offset > getSize().width) {
		// Don't allow dragging.
		maxX = -1;
	    }
	    else {
		Insets      insets = splitPane.getInsets();

                if (leftC.isVisible()) {
                    minX = leftC.getMinimumSize().width;
		    if (insets != null) {
			minX += insets.left;
		    }
                }
                else {
                    minX = 0;
                }
                if (rightC.isVisible()) {
		    int right = (insets != null) ? insets.right : 0;
                    maxX = Math.max(0, splitPane.getSize().width -
                                    (getSize().width + right) -
                                    rightC.getMinimumSize().width);
                }
                else {
		    int right = (insets != null) ? insets.right : 0;
                    maxX = Math.max(0, splitPane.getSize().width -
                                    (getSize().width + right));
                }
                if (maxX < minX) minX = maxX = 0;
            }
        }


        /**
         * Returns true if the dragging session is valid.
         */
        protected boolean isValid() {
            return (maxX > 0);
        }


        /**
         * Returns the new position to put the divider at based on
         * the passed in MouseEvent.
         */
        protected int positionForMouseEvent(MouseEvent e) {
	    int newX = (e.getSource() == BasicSplitPaneDivider.this) ?
		        (e.getX() + getLocation().x) : e.getX();

            newX = Math.min(maxX, Math.max(minX, newX - offset));
            return newX;
        }


        /**
         * Returns the x argument, since this is used for horizontal
         * splits.
         */
        protected int getNeededLocation(int x, int y) {
            int newX;

            newX = Math.min(maxX, Math.max(minX, x - offset));
            return newX;
        }


        protected void continueDrag(int newX, int newY) {
            dragDividerTo(getNeededLocation(newX, newY));
        }


        /**
         * Messages dragDividerTo with the new location for the mouse
         * event.
         */
        protected void continueDrag(MouseEvent e) {
            dragDividerTo(positionForMouseEvent(e));
        }


        protected void completeDrag(int x, int y) {
            finishDraggingTo(getNeededLocation(x, y));
        }


        /**
         * Messages finishDraggingTo with the new location for the mouse
         * event.
         */
        protected void completeDrag(MouseEvent e) {
            finishDraggingTo(positionForMouseEvent(e));
        }
    } // End of BasicSplitPaneDivider.DragController


    /**
     * Handles the events during a dragging session for a
     * VERTICAL_SPLIT orientated split pane. This continually
     * messages dragDividerTo and then when done messages
     * finishDraggingTo. When an instance is created it should be
     * messaged with isValid() to insure that dragging can happen
     * (dragging won't be allowed if the two views can not be resized).
     */
    protected class VerticalDragController extends DragController
    {
        /* DragControllers ivars are now in terms of y, not x. */
        protected VerticalDragController(MouseEvent e) {
            super(e);
            JSplitPane splitPane = splitPaneUI.getSplitPane();
            Component  leftC = splitPane.getLeftComponent();
	    Component  rightC = splitPane.getRightComponent();

            initialX = getLocation().y;
	    if (e.getSource() == BasicSplitPaneDivider.this) {
		offset = e.getY();
	    }
	    else {
		offset = e.getY() - initialX;
	    }
	    if (leftC == null || rightC == null || offset < -1 ||
		offset > getSize().height) {
		// Don't allow dragging.
		maxX = -1;
	    }
	    else {
		Insets     insets = splitPane.getInsets();

                if (leftC.isVisible()) {
                    minX = leftC.getMinimumSize().height;
		    if (insets != null) {
			minX += insets.top;
		    }
                }
                else {
                    minX = 0;
                }
                if (rightC.isVisible()) {
		    int    bottom = (insets != null) ? insets.bottom : 0;

                    maxX = Math.max(0, splitPane.getSize().height -
                                    (getSize().height + bottom) -
                                    rightC.getMinimumSize().height);
                }
                else {
		    int    bottom = (insets != null) ? insets.bottom : 0;

                    maxX = Math.max(0, splitPane.getSize().height -
                                    (getSize().height + bottom));
                }
                if (maxX < minX) minX = maxX = 0;
            }
        }


        /**
         * Returns the y argument, since this is used for vertical
         * splits.
         */
        protected int getNeededLocation(int x, int y) {
            int newY;

            newY = Math.min(maxX, Math.max(minX, y - offset));
            return newY;
        }


        /**
         * Returns the new position to put the divider at based on
         * the passed in MouseEvent.
         */
        protected int positionForMouseEvent(MouseEvent e) {
	    int newY = (e.getSource() == BasicSplitPaneDivider.this) ?
		        (e.getY() + getLocation().y) : e.getY();


            newY = Math.min(maxX, Math.max(minX, newY - offset));
            return newY;
        }
    } // End of BasicSplitPaneDividier.VerticalDragController


    /**
     * Used to layout a BasicSplitPaneDivider. Layout for the divider
     * involves appropraitely moving the left/right buttons around.
     * <p>
     */
    protected class DividerLayout implements LayoutManager
    {
        public void layoutContainer(Container c) {
            if (leftButton != null && rightButton != null &&
                c == BasicSplitPaneDivider.this) {
                if (splitPane.isOneTouchExpandable()) {
		    Insets insets = getInsets();

                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
			int extraX = (insets != null) ? insets.left : 0;
			int blockSize = getDividerSize();

			if (insets != null) {
			    blockSize -= (insets.top + insets.bottom);
			}
			blockSize = Math.min(blockSize, ONE_TOUCH_SIZE);

                        int y = (c.getSize().height - blockSize) / 2;

                        leftButton.setBounds(extraX + ONE_TOUCH_OFFSET, y,
                                             blockSize * 2, blockSize);
                        rightButton.setBounds(extraX + ONE_TOUCH_OFFSET +
                                              ONE_TOUCH_SIZE * 2, y,
                                              blockSize * 2, blockSize);
                    }
                    else {
			int extraY = (insets != null) ? insets.top : 0;
			int blockSize = getDividerSize();

			if (insets != null) {
			    blockSize -= (insets.left + insets.right);
			}
			blockSize = Math.min(blockSize, ONE_TOUCH_SIZE);

                        int x = (c.getSize().width - blockSize) / 2;

                        leftButton.setBounds(x, extraY + ONE_TOUCH_OFFSET,
                                             blockSize, blockSize * 2);
                        rightButton.setBounds(x, extraY + ONE_TOUCH_OFFSET +
                                              ONE_TOUCH_SIZE * 2, blockSize,
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
    } // End of class BasicSplitPaneDivider.DividerLayout


    /**
     * Listeners installed on the one touch expandable buttons.
     */
    private class OneTouchActionHandler implements ActionListener {
	/** True indicates the resize should go the minimum (top or left)
	 * vs false which indicates the resize should go to the maximum.
	 */
	private boolean toMinimum;

	OneTouchActionHandler(boolean toMinimum) {
	    this.toMinimum = toMinimum;
	}

        public void actionPerformed(ActionEvent e) {
            Insets  insets = splitPane.getInsets();
	    int     lastLoc = splitPane.getLastDividerLocation();
            int     currentLoc = splitPaneUI.getDividerLocation(splitPane);
	    int     newLoc;

	    // We use the location from the UI directly, as the location the
	    // JSplitPane itself maintains is not necessarly correct.
	    if (toMinimum) {
		if (orientation == JSplitPane.VERTICAL_SPLIT) {
		    if (currentLoc >= (splitPane.getHeight() -
				       insets.bottom - getDividerSize()))
			newLoc = lastLoc;
		    else
			newLoc = insets.top;
		}
		else {
		    if (currentLoc >= (splitPane.getWidth() -
				       insets.right - getDividerSize()))
			newLoc = lastLoc;
		    else
			newLoc = insets.left;
		}
	    }
	    else {
		if (orientation == JSplitPane.VERTICAL_SPLIT) {
		    if (currentLoc == insets.top)
			newLoc = lastLoc;
		    else
			newLoc = splitPane.getHeight() - getHeight() -
			         insets.top;
		}
		else {
		    if (currentLoc == insets.left)
			newLoc = lastLoc;
		    else
			newLoc = splitPane.getWidth() - getWidth() - 
			         insets.left;
		}
	    }
	    if (currentLoc != newLoc) {
		splitPane.setDividerLocation(newLoc);
		// We do this in case the dividers notion of the location
		// differs from the real location.
		splitPane.setLastDividerLocation(currentLoc);
	    }
        }
    } // End of class BasicSplitPaneDivider.LeftActionListener
}
