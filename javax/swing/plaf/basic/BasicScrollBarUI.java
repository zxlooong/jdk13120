/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;


import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;


/**
 * Implementation of ScrollBarUI for the Basic Look and Feel
 *
 * @version 1.58 02/06/02
 * @author Rich Schiavi
 * @author David Kloba
 * @author Hans Muller
 */
public class BasicScrollBarUI 
    extends ScrollBarUI implements LayoutManager, SwingConstants
{
    private static final int POSITIVE_SCROLL = 1;
    private static final int NEGATIVE_SCROLL = -1;

    private static final int MIN_SCROLL = 2;
    private static final int MAX_SCROLL = 3;

    protected Dimension minimumThumbSize;
    protected Dimension maximumThumbSize;

    protected Color thumbHighlightColor;
    protected Color thumbLightShadowColor;
    protected Color thumbDarkShadowColor;
    protected Color thumbColor;
    protected Color trackColor;
    protected Color trackHighlightColor;

    protected JScrollBar scrollbar;
    protected JButton incrButton;
    protected JButton decrButton;
    protected boolean isDragging;
    protected TrackListener trackListener;
    protected ArrowButtonListener buttonListener;
    protected ModelListener modelListener;

    protected Rectangle thumbRect;
    protected Rectangle trackRect;

    protected int trackHighlight;

    protected static final int NO_HIGHLIGHT = 0;
    protected static final int DECREASE_HIGHLIGHT = 1;
    protected static final int INCREASE_HIGHLIGHT = 2;

    protected ScrollListener scrollListener;
    protected PropertyChangeListener propertyChangeListener; 
    protected Timer scrollTimer;

    private final static int scrollSpeedThrottle = 60; // delay in milli seconds

    /** True indicates a middle click will absolutely position the
     * scrollbar. */
    private boolean supportsAbsolutePositioning;


    public static ComponentUI createUI(JComponent c)    {
        return new BasicScrollBarUI();
    }


    protected void configureScrollBarColors() 
    {
	thumbHighlightColor = UIManager.getColor("ScrollBar.thumbHighlight");
	thumbLightShadowColor = UIManager.getColor("ScrollBar.thumbLightShadow");
	thumbDarkShadowColor = UIManager.getColor("ScrollBar.thumbDarkShadow");
	thumbColor = UIManager.getColor("ScrollBar.thumb");
	trackColor = UIManager.getColor("ScrollBar.track");
	trackHighlightColor = UIManager.getColor("ScrollBar.trackHighlight");
    }


    public void installUI(JComponent c)   {
	scrollbar = (JScrollBar)c;
        thumbRect = new Rectangle(0, 0, 0, 0);
        trackRect = new Rectangle(0, 0, 0, 0);
	installDefaults();
	installComponents();
	installListeners();
	installKeyboardActions();
    }

    public void uninstallUI(JComponent c) {
	scrollbar = (JScrollBar)c;
	uninstallDefaults();
	uninstallComponents();
	uninstallListeners();
	uninstallKeyboardActions();
	c.remove(incrButton);
	c.remove(decrButton);
	c.setLayout(null);
	thumbRect = null;
	scrollbar = null;
	incrButton = null;
	decrButton = null;
    }
    

    protected void installDefaults()
    {
	minimumThumbSize = (Dimension)UIManager.get("ScrollBar.minimumThumbSize");
	maximumThumbSize = (Dimension)UIManager.get("ScrollBar.maximumThumbSize");

	Boolean absB = (Boolean)UIManager.get("ScrollBar.allowsAbsolutePositioning");
	supportsAbsolutePositioning = (absB != null) ? absB.booleanValue() :
	                              false;

	trackHighlight = NO_HIGHLIGHT;
        switch (scrollbar.getOrientation()) {
        case JScrollBar.VERTICAL:
            incrButton = createIncreaseButton(SOUTH);
            decrButton = createDecreaseButton(NORTH);
            break;
            
        case JScrollBar.HORIZONTAL:
            incrButton = createIncreaseButton(EAST);
            decrButton = createDecreaseButton(WEST);
            break;
        }
                
        scrollbar.setLayout(this);
        scrollbar.add(incrButton);
        scrollbar.add(decrButton);
	scrollbar.setEnabled(scrollbar.isEnabled());
	scrollbar.setOpaque(true);
	configureScrollBarColors();
        LookAndFeel.installBorder(scrollbar, "ScrollBar.border");
    }


    protected void installComponents(){
    }

    protected void uninstallComponents(){
    }

  
    protected void installListeners(){
	trackListener = createTrackListener();
    	buttonListener = createArrowButtonListener();
    	modelListener = createModelListener();
	propertyChangeListener = createPropertyChangeListener();

	scrollbar.addMouseListener(trackListener);
	scrollbar.addMouseMotionListener(trackListener);
        scrollbar.getModel().addChangeListener(modelListener);
	scrollbar.addPropertyChangeListener(propertyChangeListener);

        if (incrButton != null) {
            incrButton.addMouseListener(buttonListener);
	}
        if (decrButton != null)	{
            decrButton.addMouseListener(buttonListener);
	}

	scrollListener = createScrollListener();
	scrollTimer = new Timer(scrollSpeedThrottle, scrollListener);
	scrollTimer.setInitialDelay(300);  // default InitialDelay?
    }


    protected void installKeyboardActions(){
	ActionMap map = getActionMap();

	SwingUtilities.replaceUIActionMap(scrollbar, map);
	InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
	SwingUtilities.replaceUIInputMap(scrollbar, JComponent.WHEN_FOCUSED,
					 inputMap);
    }

    protected void uninstallKeyboardActions(){
	SwingUtilities.replaceUIInputMap(scrollbar, JComponent.WHEN_FOCUSED,
					 null);
	SwingUtilities.replaceUIActionMap(scrollbar, null);
    }

    private InputMap getInputMap(int condition) {
	if (condition == JComponent.WHEN_FOCUSED) {
	    return (InputMap)UIManager.get("ScrollBar.focusInputMap");
	}
	return null;
    }

    private ActionMap getActionMap() {
	ActionMap map = (ActionMap)UIManager.get("ScrollBar.actionMap");

	if (map == null) {
	    map = createActionMap();
	    if (map != null) {
		UIManager.put("ScrollBar.actionMap", map);
	    }
	}
	return map;
    }

    private ActionMap createActionMap() {
	ActionMap map = new ActionMapUIResource();
        map.put("positiveUnitIncrement", new SharedActionScroller
		(POSITIVE_SCROLL, false));
        map.put("positiveBlockIncrement", new SharedActionScroller
		(POSITIVE_SCROLL, true));
        map.put("negativeUnitIncrement", new SharedActionScroller
		(NEGATIVE_SCROLL, false));
        map.put("negativeBlockIncrement", new SharedActionScroller
		(NEGATIVE_SCROLL, true));
        map.put("minScroll", new SharedActionScroller(MIN_SCROLL, true));
        map.put("maxScroll", new SharedActionScroller(MAX_SCROLL, true));
	return map;
    }


    protected void uninstallListeners() {
	scrollTimer.stop();
	scrollTimer = null;

	if (decrButton != null){
	    decrButton.removeMouseListener(buttonListener);
	}
	if (incrButton != null){
	    incrButton.removeMouseListener(buttonListener);
	}
    
	scrollbar.getModel().removeChangeListener(modelListener);
	scrollbar.removeMouseListener(trackListener);
	scrollbar.removeMouseMotionListener(trackListener);
	scrollbar.removePropertyChangeListener(propertyChangeListener);
    }


    protected void uninstallDefaults(){
        LookAndFeel.uninstallBorder(scrollbar);
    }


    protected TrackListener createTrackListener(){
	return new TrackListener();
    }

    protected ArrowButtonListener createArrowButtonListener(){
	return new ArrowButtonListener();
    }
    
    protected ModelListener createModelListener(){
	return new ModelListener();
    }

    protected ScrollListener createScrollListener(){
	return new ScrollListener();
    }
    
    protected PropertyChangeListener createPropertyChangeListener() {
	return new PropertyChangeHandler();
    }

    public void paint(Graphics g, JComponent c) {
	paintTrack(g, c, getTrackBounds());		
	paintThumb(g, c, getThumbBounds());
    }

        
    /**
     * A vertical scrollbars preferred width is the maximum of 
     * preferred widths of the (non null) increment/decrement buttons,
     * and the minimum width of the thumb. The preferred height is the 
     * sum of the preferred heights of the same parts.  The basis for 
     * the preferred size of a horizontal scrollbar is similar. 
     * <p>
     * The preferredSize is only computed once, subequent
     * calls to this method just return a cached size.  T
     * 
     * @param c The JScrollBar that's delegating this method to us.
     * @return The preferred size of a Basic JScrollBar.
     * @see #getMaximumSize
     * @see #getMinimumSize
     */
    public Dimension getPreferredSize(JComponent c) {
	return (scrollbar.getOrientation() == JScrollBar.VERTICAL)
	    ? new Dimension(16, 48)
	    : new Dimension(48, 16);
    }


    /**
     * A vertical scrollbars minimum width is the largest 
     * minimum width of the (non null) increment/decrement buttons,
     * and the minimum width of the thumb. The minimum height is the 
     * sum of the minimum heights of the same parts.  The basis for 
     * the preferred size of a horizontal scrollbar is similar. 
     * <p>
     * The minimumSize is only computed once, subequent
     * calls to this method just return a cached size.  T
     * 
     * @param c The JScrollBar that's delegating this method to us.
     * @return The minimum size of a Basic JScrollBar.
     * @see #getMaximumSize
     * @see #getPreferredSize
     */
    public Dimension getMinimumSize(JComponent c) {
	return getPreferredSize(c);
    }

    
    /**
     * @param c The JScrollBar that's delegating this method to us.
     * @return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
     * @see #getMinimumSize
     * @see #getPreferredSize
     */
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    	
    
    protected JButton createDecreaseButton(int orientation)  {
        return new BasicArrowButton(orientation);
    }

    protected JButton createIncreaseButton(int orientation)  {
        return new BasicArrowButton(orientation);
    }
          		  

    protected void paintDecreaseHighlight(Graphics g)
    {
	Insets insets = scrollbar.getInsets();
	Rectangle thumbR = getThumbBounds();
	g.setColor(trackHighlightColor);

	if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
	    int x = insets.left;
	    int y = decrButton.getY() + decrButton.getHeight();
	    int w = scrollbar.getWidth() - (insets.left + insets.right);
	    int h = thumbR.y - y;
	    g.fillRect(x, y, w, h);
	} 
	else	{
	    int x = decrButton.getX() + decrButton.getHeight();
	    int y = insets.top;
	    int w = thumbR.x - x;
	    int h = scrollbar.getHeight() - (insets.top + insets.bottom);
	    g.fillRect(x, y, w, h);
	}
    }      
	

    protected void paintIncreaseHighlight(Graphics g)
    {
	Insets insets = scrollbar.getInsets();
	Rectangle thumbR = getThumbBounds();
	g.setColor(trackHighlightColor);

	if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
	    int x = insets.left;
	    int y = thumbR.y + thumbR.height;
	    int w = scrollbar.getWidth() - (insets.left + insets.right);
	    int h = incrButton.getY() - y;
	    g.fillRect(x, y, w, h);
	} 
	else {
	    int x = thumbR.x + thumbR.width;
	    int y = insets.top;
	    int w = incrButton.getX() - x;
	    int h = scrollbar.getHeight() - (insets.top + insets.bottom);
	    g.fillRect(x, y, w, h);
	}
    }      


    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)  
    {
        g.setColor(trackColor);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);	

	if(trackHighlight == DECREASE_HIGHLIGHT)	{
	    paintDecreaseHighlight(g);
	} 
	else if(trackHighlight == INCREASE_HIGHLIGHT)		{
	    paintIncreaseHighlight(g);
	}
    }

	
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)  
    {
	if(thumbBounds.isEmpty() || !scrollbar.isEnabled())	{
	    return;
	}

        int w = thumbBounds.width;
        int h = thumbBounds.height;		

	g.translate(thumbBounds.x, thumbBounds.y);

	g.setColor(thumbDarkShadowColor);
	g.drawRect(0, 0, w-1, h-1);    
	g.setColor(thumbColor);
	g.fillRect(0, 0, w-1, h-1);
        
        g.setColor(thumbHighlightColor);
        g.drawLine(1, 1, 1, h-2);
        g.drawLine(2, 1, w-3, 1);
        
        g.setColor(thumbLightShadowColor);
        g.drawLine(2, h-2, w-2, h-2);
        g.drawLine(w-2, 1, w-2, h-3);

	g.translate(-thumbBounds.x, -thumbBounds.y);
    }


    /** 
     * Return the smallest acceptable size for the thumb.  If the scrollbar
     * becomes so small that this size isn't available, the thumb will be
     * hidden.  
     * <p>
     * <b>Warning </b>: the value returned by this method should not be
     * be modified, it's a shared static constant.
     *
     * @return The smallest acceptable size for the thumb.
     * @see #getMaximumThumbSize
     */
    protected Dimension getMinimumThumbSize() { 
	return minimumThumbSize;
    }

    /** 
     * Return the largest acceptable size for the thumb.  To create a fixed 
     * size thumb one make this method and <code>getMinimumThumbSize</code> 
     * return the same value.
     * <p>
     * <b>Warning </b>: the value returned by this method should not be
     * be modified, it's a shared static constant.
     *
     * @return The largest acceptable size for the thumb.
     * @see #getMinimumThumbSize
     */
    protected Dimension getMaximumThumbSize()	{ 
	return maximumThumbSize;
    }


    /*
     * LayoutManager Implementation
     */

    public void addLayoutComponent(String name, Component child) {}
    public void removeLayoutComponent(Component child) {}
    
    public Dimension preferredLayoutSize(Container scrollbarContainer)  {
        return getPreferredSize((JComponent)scrollbarContainer);
    }
    
    public Dimension minimumLayoutSize(Container scrollbarContainer) {
        return getMinimumSize((JComponent)scrollbarContainer);
    }
    

    protected void layoutVScrollbar(JScrollBar sb)  
    {
        Dimension sbSize = sb.getSize();
        Insets sbInsets = sb.getInsets();

	/*
	 * Width and left edge of the buttons and thumb.
	 */
	int itemW = sbSize.width - (sbInsets.left + sbInsets.right);
	int itemX = sbInsets.left;
        
        /* Nominal locations of the buttons, assuming their preferred
	 * size will fit.
	 */
        int decrButtonH = decrButton.getPreferredSize().height;
        int decrButtonY = sbInsets.top;
        
        int incrButtonH = incrButton.getPreferredSize().height;
        int incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
        
        /* The thumb must fit within the height left over after we
	 * subtract the preferredSize of the buttons and the insets.
	 */
        int sbInsetsH = sbInsets.top + sbInsets.bottom;
        int sbButtonsH = decrButtonH + incrButtonH;
        float trackH = sbSize.height - (sbInsetsH + sbButtonsH);
        
        /* Compute the height and origin of the thumb.   The case
	 * where the thumb is at the bottom edge is handled specially 
	 * to avoid numerical problems in computing thumbY.  Enforce
	 * the thumbs min/max dimensions.  If the thumb doesn't
	 * fit in the track (trackH) we'll hide it later.
	 */
	float min = sb.getMinimum();
	float extent = sb.getVisibleAmount();
	float range = sb.getMaximum() - min;
	float value = sb.getValue();

        int thumbH = (range <= 0) 
	    ? getMaximumThumbSize().height : (int)(trackH * (extent / range));
	thumbH = Math.max(thumbH, getMinimumThumbSize().height);
	thumbH = Math.min(thumbH, getMaximumThumbSize().height);
        
	int thumbY = incrButtonY - thumbH;  
	if (sb.getValue() < (sb.getMaximum() - sb.getVisibleAmount())) {
	    float thumbRange = trackH - thumbH;
	    thumbY = (int)(0.5f + (thumbRange * ((value - min) / (range - extent))));
	    thumbY +=  decrButtonY + decrButtonH;
	}

        /* If the buttons don't fit, allocate half of the available 
	 * space to each and move the lower one (incrButton) down.
	 */
        int sbAvailButtonH = (sbSize.height - sbInsetsH);
        if (sbAvailButtonH < sbButtonsH) {
            incrButtonH = decrButtonH = sbAvailButtonH / 2;
            incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
        }
        decrButton.setBounds(itemX, decrButtonY, itemW, decrButtonH);
        incrButton.setBounds(itemX, incrButtonY, itemW, incrButtonH);

	/* Update the trackRect field.
	 */	
	int itrackY = decrButtonY + decrButtonH;
	int itrackH = incrButtonY - itrackY;
	trackRect.setBounds(itemX, itrackY, itemW, itrackH);
	
	/* If the thumb isn't going to fit, zero it's bounds.  Otherwise
	 * make sure it fits between the buttons.  Note that setting the
	 * thumbs bounds will cause a repaint.
	 */
	if(thumbH >= (int)trackH)	{
	    setThumbBounds(0, 0, 0, 0);
	}
	else {
	    if ((thumbY + thumbH) > incrButtonY) {
		thumbY = incrButtonY - thumbH;
	    }
	    if (thumbY  < (decrButtonY + decrButtonH)) {
		thumbY = decrButtonY + decrButtonH + 1;
	    }
	    setThumbBounds(itemX, thumbY, itemW, thumbH);
	}
    }
    

    protected void layoutHScrollbar(JScrollBar sb)  
    {
        Dimension sbSize = sb.getSize();
        Insets sbInsets = sb.getInsets();
        
	/* Height and top edge of the buttons and thumb.
	 */
	int itemH = sbSize.height - (sbInsets.top + sbInsets.bottom);
	int itemY = sbInsets.top;

        /* Nominal locations of the buttons, assuming their preferred
	 * size will fit.
	 */
        int decrButtonW = decrButton.getPreferredSize().width;
        int decrButtonX = sbInsets.left;
        
        int incrButtonW = incrButton.getPreferredSize().width;
        int incrButtonX = sbSize.width - (sbInsets.right + incrButtonW);
        
        /* The thumb must fit within the width left over after we
	 * subtract the preferredSize of the buttons and the insets.
	 */
        int sbInsetsW = sbInsets.left + sbInsets.right;
        int sbButtonsW = decrButtonW + incrButtonW;
        float trackW = sbSize.width - (sbInsetsW + sbButtonsW);
        
        /* Compute the width and origin of the thumb.  Enforce
	 * the thumbs min/max dimensions.  The case where the thumb 
	 * is at the right edge is handled specially to avoid numerical 
	 * problems in computing thumbX.  If the thumb doesn't
	 * fit in the track (trackH) we'll hide it later.
	 */
        float min = sb.getMinimum();
        float extent = sb.getVisibleAmount();
        float range = sb.getMaximum() - min;
        float value = sb.getValue();

        int thumbW = (range <= 0) 
	    ? getMaximumThumbSize().width : (int)(trackW * (extent / range));
        thumbW = Math.max(thumbW, getMinimumThumbSize().width);
        thumbW = Math.min(thumbW, getMaximumThumbSize().width);
        
	int thumbX = incrButtonX - thumbW;
	if (sb.getValue() < (sb.getMaximum() - sb.getVisibleAmount())) {
	    float thumbRange = trackW - thumbW;
	    thumbX = (int)(0.5f + (thumbRange * ((value - min) / (range - extent))));
	    thumbX +=  decrButtonX + decrButtonW;
	}

        /* If the buttons don't fit, allocate half of the available 
         * space to each and move the right one (incrButton) over.
         */
        int sbAvailButtonW = (sbSize.width - sbInsetsW);
        if (sbAvailButtonW < sbButtonsW) {
            incrButtonW = decrButtonW = sbAvailButtonW / 2;
            incrButtonX = sbSize.width - (sbInsets.right + incrButtonW);
        }
        
        decrButton.setBounds(decrButtonX, itemY, decrButtonW, itemH);
        incrButton.setBounds(incrButtonX, itemY, incrButtonW, itemH);

	/* Update the trackRect field.
	 */	
	int itrackX = decrButtonX + decrButtonW;
	int itrackW = incrButtonX - itrackX;
	trackRect.setBounds(itrackX, itemY, itrackW, itemH);

	/* Make sure the thumb fits between the buttons.  Note 
	 * that setting the thumbs bounds causes a repaint.
	 */
	if (thumbW >= (int)trackW) {
	    setThumbBounds(0, 0, 0, 0);
	}
	else {
	    if (thumbX + thumbW > incrButtonX) {
		thumbX = incrButtonX - thumbW;
	    }
	    if (thumbX  < decrButtonX + decrButtonW) {
		thumbX = decrButtonX + decrButtonW + 1;
	    }
	    setThumbBounds(thumbX, itemY, thumbW, itemH);
	}
    }
    

    public void layoutContainer(Container scrollbarContainer) 
    {
	/* If the user is dragging the value, we'll assume that the 
	 * scrollbars layout is OK modulo the thumb which is being 
	 * handled by the dragging code.
	 */
	if (isDragging) {
	    return;
	}

        JScrollBar scrollbar = (JScrollBar)scrollbarContainer;
        switch (scrollbar.getOrientation()) {
        case JScrollBar.VERTICAL:
            layoutVScrollbar(scrollbar);
            break;
            
        case JScrollBar.HORIZONTAL:
            layoutHScrollbar(scrollbar);
            break;
        }
    }


    /**
     * Set the bounds of the thumb and force a repaint that includes
     * the old thumbBounds and the new one.
     *
     * @see #getThumbBounds
     */
    protected void setThumbBounds(int x, int y, int width, int height)
    {
	/* If the thumbs bounds haven't changed, we're done.
	 */
	if ((thumbRect.x == x) && 
	    (thumbRect.y == y) && 
	    (thumbRect.width == width) && 
	    (thumbRect.height == height)) {
	    return;
	}

	/* Update thumbRect, and repaint the union of x,y,w,h and 
	 * the old thumbRect.
	 */
	int minX = Math.min(x, thumbRect.x);
	int minY = Math.min(y, thumbRect.y);
	int maxX = Math.max(x + width, thumbRect.x + thumbRect.width);
	int maxY = Math.max(y + height, thumbRect.y + thumbRect.height);

	thumbRect.setBounds(x, y, width, height);
	scrollbar.repaint(minX, minY, maxX - minX, maxY - minY);
    }


    /**
     * Return the current size/location of the thumb.
     * <p>
     * <b>Warning </b>: the value returned by this method should not be
     * be modified, it's a reference to the actual rectangle, not a copy.
     *
     * @return The current size/location of the thumb.
     * @see #setThumbBounds
     */
    protected Rectangle getThumbBounds() {
	return thumbRect;
    }


    /**
     * Return the current bounds of the track, i.e. the space in between
     * the increment and decrement buttons, less the insets.  The value
     * returned by this method is updated each time the scrollbar is
     * layed out (validated).
     * <p>
     * <b>Warning </b>: the value returned by this method should not be
     * be modified, it's a reference to the actual rectangle, not a copy.
     *
     * @return The current bounds of the scrollbar track.
     * @see #layoutContainer
     */
    protected Rectangle getTrackBounds() {
	return trackRect;
    }

    protected void scrollByBlock(int direction)
    {
	synchronized(scrollbar)	{
	    int oldValue = scrollbar.getValue();
	    int blockIncrement = scrollbar.getBlockIncrement(direction);
	    int delta = blockIncrement * ((direction > 0) ? +1 : -1);

	    scrollbar.setValue(oldValue + delta);			
	    trackHighlight = direction > 0 ? INCREASE_HIGHLIGHT : DECREASE_HIGHLIGHT;
	    Rectangle dirtyRect = getTrackBounds();
	    scrollbar.repaint(dirtyRect.x, dirtyRect.y, dirtyRect.width, dirtyRect.height);
	}
    }
    

    protected void scrollByUnit(int direction)	{
	synchronized(scrollbar)	{

	int delta;
	if(direction > 0)
            delta = scrollbar.getUnitIncrement(direction);
	else
            delta = -scrollbar.getUnitIncrement(direction);
        scrollbar.setValue(delta + scrollbar.getValue()); 
	}
    }

    /**
     * Indicates whether the user can absolutely position the offset with
     * a mouse click (usually the middle mouse button).
     * <p>The return value is determined from the UIManager property
     * ScrollBar.allowsAbsolutePositioning.
     */
    private boolean getSupportsAbsolutePositioning() {
	return supportsAbsolutePositioning;
    }

    /**
     * A listener to listen for model changes.
     *
     */
    protected class ModelListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
	    layoutContainer(scrollbar);
        }
    }


    /**
     * Track mouse drags.
     */
    protected class TrackListener
        extends MouseAdapter implements MouseMotionListener
    {
	protected transient int offset;
	protected transient int currentMouseX, currentMouseY;
		
        public void mouseReleased(MouseEvent e)
        {
	    if(!scrollbar.isEnabled())
		return;

	    if(trackHighlight != NO_HIGHLIGHT) {
		Rectangle r = getTrackBounds();
		scrollbar.repaint(r.x, r.y, r.width, r.height);
	    }

	    trackHighlight = NO_HIGHLIGHT;
	    isDragging = false;
	    offset = 0;
	    scrollTimer.stop();
	    scrollbar.setValueIsAdjusting(false);
	}
		

        /**
	 * If the mouse is pressed above the "thumb" component
	 * then reduce the scrollbars value by one page ("page up"), 
	 * otherwise increase it by one page.  If there is no 
	 * thumb then page up if the mouse is in the upper half
	 * of the track.
	 */
        public void mousePressed(MouseEvent e) 
	{
	    if(!scrollbar.isEnabled())
		return;

	    if (!scrollbar.hasFocus()) {
		scrollbar.requestFocus();
	    }

	    scrollbar.setValueIsAdjusting(true);

            currentMouseX = e.getX();
            currentMouseY = e.getY();
	
	    // Clicked in the Thumb area?
	    if(getThumbBounds().contains(currentMouseX, currentMouseY))	{
                switch (scrollbar.getOrientation()) {
                case JScrollBar.VERTICAL:
		    offset = currentMouseY - getThumbBounds().y;
                    break;
                case JScrollBar.HORIZONTAL:
		    offset = currentMouseX - getThumbBounds().x;
                    break;
                }
		isDragging = true;
		return;
	    }
	    else if (getSupportsAbsolutePositioning() &&
		     SwingUtilities.isMiddleMouseButton(e)) {
                switch (scrollbar.getOrientation()) {
                case JScrollBar.VERTICAL:
		    offset = getThumbBounds().height / 2;
                    break;
                case JScrollBar.HORIZONTAL:
		    offset = getThumbBounds().width / 2;
                    break;
                }
		isDragging = true;
		setValueFrom(e);
		return;
	    }
	    isDragging = false;
							
            Dimension sbSize = scrollbar.getSize();
            int direction = +1;

            switch (scrollbar.getOrientation()) {
            case JScrollBar.VERTICAL:
                if (getThumbBounds().isEmpty()) {
                    int scrollbarCenter = sbSize.height / 2;
		    direction = (currentMouseY < scrollbarCenter) ? -1 : +1;
                } else {
                    int thumbY = getThumbBounds().y;
		    direction = (currentMouseY < thumbY) ? -1 : +1;
                }
                break;                    
            case JScrollBar.HORIZONTAL:
                if (getThumbBounds().isEmpty()) {
                    int scrollbarCenter = sbSize.width / 2;
                    direction = (currentMouseX < scrollbarCenter) ? -1 : +1;
                } else {
                    int thumbX = getThumbBounds().x;
                    direction = (currentMouseX < thumbX) ? -1 : +1;
                }
                break;
            }
	    scrollByBlock(direction);
	    if(!getThumbBounds().contains(currentMouseX, currentMouseY))	{
		scrollTimer.stop();
		scrollListener.setDirection(direction);
		scrollListener.setScrollByBlock(true);
		scrollTimer.start();
	    }
        }
		

        /** 
	 * Set the models value to the position of the top/left
	 * of the thumb relative to the origin of the track.
	 */
	public void mouseDragged(MouseEvent e) {
	    setValueFrom(e);
	}

	private void setValueFrom(MouseEvent e) {
	    if(!scrollbar.isEnabled() || !isDragging) {
		return;
	    }

	    BoundedRangeModel model = scrollbar.getModel();
	    Rectangle thumbR = getThumbBounds();
	    float trackLength;
	    int thumbMin, thumbMax, thumbPos;

            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
		thumbMin = decrButton.getY() + decrButton.getHeight();
		thumbMax = incrButton.getY() - getThumbBounds().height;
		thumbPos = Math.min(thumbMax, Math.max(thumbMin, (e.getY() - offset)));
		setThumbBounds(thumbR.x, thumbPos, thumbR.width, thumbR.height);
		trackLength = getTrackBounds().height;
	    }
	    else {
		thumbMin = decrButton.getX() + decrButton.getWidth();
		thumbMax = incrButton.getX() - getThumbBounds().width;
		thumbPos = Math.min(thumbMax, Math.max(thumbMin, (e.getX() - offset)));
		setThumbBounds(thumbPos, thumbR.y, thumbR.width, thumbR.height);
		trackLength = getTrackBounds().width;
	    }

	    /* Set the scrollbars value.  If the thumb has reached the end of
	     * the scrollbar, then just set the value to its maximum.  Otherwise
	     * compute the value as accurately as possible.
	     */
	    if (thumbPos == thumbMax) {
		scrollbar.setValue(model.getMaximum() - model.getExtent());
	    }
	    else {
		float valueMax = model.getMaximum() - model.getExtent();
		float valueRange = valueMax - model.getMinimum();
		float thumbValue = thumbPos - thumbMin;
		float thumbRange = thumbMax - thumbMin;
		int value = (int)(0.5 + ((thumbValue / thumbRange) * valueRange));
		scrollbar.setValue(value + model.getMinimum());
	    }
        }
		
	public void mouseMoved(MouseEvent e) {
	}
    }
        

    /**
     * Listener for cursor keys.
     */
    protected class ArrowButtonListener extends MouseAdapter
    {		
	// Because we are handling both mousePressed and Actions
	// we need to make sure we don't fire under both conditions.
	// (keyfocus on scrollbars causes action without mousePress
	boolean handledEvent;
	
	public void mousePressed(MouseEvent e)		{
	    if(!scrollbar.isEnabled()) { return; }
	    // not an unmodified left mouse button
	    //if(e.getModifiers() != InputEvent.BUTTON1_MASK) {return; }
	    if( ! SwingUtilities.isLeftMouseButton(e)) { return; }

	    int direction = (e.getSource() == incrButton) ? 1 : -1;
		
	    scrollByUnit(direction);
	    scrollTimer.stop();
	    scrollListener.setDirection(direction);
	    scrollListener.setScrollByBlock(false);
	    scrollTimer.start();

	    handledEvent = true;	
	    if (!scrollbar.hasFocus()) {
		scrollbar.requestFocus();
	    }
	}

	public void mouseReleased(MouseEvent e)		{
	    scrollTimer.stop();
	    handledEvent = false;
	    scrollbar.setValueIsAdjusting(false);
	}
    }


    /**
     * Listener for scrolling events intiated in the
     * ScrollPane.
     */
    protected class ScrollListener implements ActionListener
    {
	int direction = +1;
	boolean useBlockIncrement;

	public ScrollListener()	{
	    direction = +1;
	    useBlockIncrement = false;
	}

        public ScrollListener(int dir, boolean block)	{
	    direction = dir;
	    useBlockIncrement = block;
	}
	
	public void setDirection(int direction) { this.direction = direction; }
	public void setScrollByBlock(boolean block) { this.useBlockIncrement = block; }
					
	public void actionPerformed(ActionEvent e) {
	    if(useBlockIncrement)	{
		scrollByBlock(direction);		
		// Stop scrolling if the thumb catches up with the mouse
		if(scrollbar.getOrientation() == JScrollBar.VERTICAL)	{
		    if(direction > 0)	{
			if(getThumbBounds().y + getThumbBounds().height 
				>= trackListener.currentMouseY)
				    ((Timer)e.getSource()).stop();
		    } else if(getThumbBounds().y <= trackListener.currentMouseY)	{
			((Timer)e.getSource()).stop();
		    }
		} else {
		    if(direction > 0)	{
			if(getThumbBounds().x + getThumbBounds().width 
				>= trackListener.currentMouseX)
				    ((Timer)e.getSource()).stop();
		    } else if(getThumbBounds().x <= trackListener.currentMouseX)	{
		        ((Timer)e.getSource()).stop();
		    }
	        }
	    } else {
		scrollByUnit(direction);
	    }

	    if(direction > 0 
		&& scrollbar.getValue()+scrollbar.getVisibleAmount() 
			>= scrollbar.getMaximum())
		((Timer)e.getSource()).stop();
	    else if(direction < 0 
		&& scrollbar.getValue() <= scrollbar.getMinimum())
	        ((Timer)e.getSource()).stop();
	}
    }


    public class PropertyChangeHandler implements PropertyChangeListener 
    {
        public void propertyChange(PropertyChangeEvent e) {
	    String propertyName = e.getPropertyName();

	    if ("model".equals(propertyName)) {
	        BoundedRangeModel oldModel = (BoundedRangeModel)e.getOldValue();
	        BoundedRangeModel newModel = (BoundedRangeModel)e.getNewValue();
		oldModel.removeChangeListener(modelListener);
		newModel.addChangeListener(modelListener);
		scrollbar.repaint();
		scrollbar.revalidate();
	    } else if ("orientation".equals(propertyName)) {
                Integer orient = (Integer)e.getNewValue();
                if (incrButton instanceof BasicArrowButton) {
                    ((BasicArrowButton)incrButton).setDirection(orient.intValue() == HORIZONTAL?
                                                                EAST : SOUTH);
                }
                if (decrButton instanceof BasicArrowButton) {
                    ((BasicArrowButton)decrButton).setDirection(orient.intValue() == HORIZONTAL?
                                                                WEST : NORTH);
                }
            } else if ("componentOrientation".equals(propertyName)) {
                ComponentOrientation co = scrollbar.getComponentOrientation();
                incrButton.setComponentOrientation(co);
                decrButton.setComponentOrientation(co);
            }
	}
    }


    /**
     * Used for scrolling the scrollbar.
     */
    private static class SharedActionScroller extends AbstractAction {
        private int dir;
        private boolean block;

        SharedActionScroller(int dir, boolean block) {
            this.dir = dir;
            this.block = block;
        }

        public void actionPerformed(ActionEvent e) {
	    JScrollBar scrollBar = (JScrollBar)e.getSource();
	    if (dir == NEGATIVE_SCROLL || dir == POSITIVE_SCROLL) {
		int amount;
		// Don't use the BasicScrollBarUI.scrollByXXX methods as we
		// don't want to use an invokeLater to reset the trackHighlight
		// via an invokeLater
		if (block) {
		    if (dir == NEGATIVE_SCROLL) {
			amount = -1 * scrollBar.getBlockIncrement(-1);
		    }
		    else {
			amount = scrollBar.getBlockIncrement(1);
		    }
		}
		else {
		    if (dir == NEGATIVE_SCROLL) {
			amount = -1 * scrollBar.getUnitIncrement(-1);
		    }
		    else {
			amount = scrollBar.getUnitIncrement(1);
		    }
		}
		scrollBar.setValue(scrollBar.getValue() + amount);
	    }
	    else if (dir == MIN_SCROLL) {
		scrollBar.setValue(scrollBar.getMinimum());
	    }
	    else if (dir == MAX_SCROLL) {
		scrollBar.setValue(scrollBar.getMaximum());
	    }
	}
    }
}
            
            
