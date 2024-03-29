/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package javax.swing.plaf.basic;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.text.View;


/**
 * BasicMenuItem implementation
 *
 * @version 1.104 12/02/02
 * @author Georges Saab
 * @author David Karlton
 * @author Arnaud Weber
 * @author Fredrik Lagerblad
 */
public class BasicMenuItemUI extends MenuItemUI
{
    protected JMenuItem menuItem = null;
    protected Color selectionBackground;
    protected Color selectionForeground;    
    protected Color disabledForeground;
    protected Color acceleratorForeground;
    protected Color acceleratorSelectionForeground;
    private   String acceleratorDelimiter;

    protected int defaultTextIconGap;
    protected Font acceleratorFont;

    protected MouseInputListener mouseInputListener;
    protected MenuDragMouseListener menuDragMouseListener;
    protected MenuKeyListener menuKeyListener;
    private   PropertyChangeListener propertyChangeListener;
    
    protected Icon arrowIcon = null;
    protected Icon checkIcon = null;

    protected boolean oldBorderPainted;

    /** Used for accelerator binding, lazily created. */
    InputMap windowInputMap;

    /* diagnostic aids -- should be false for production builds. */
    private static final boolean TRACE =   false; // trace creates and disposes

    private static final boolean VERBOSE = false; // show reuse hits/misses
    private static final boolean DEBUG =   false;  // show bad params, misc.

    /* Client Property keys for text and accelerator text widths */
    private static final String MAX_TEXT_WIDTH =  "maxTextWidth";
    private static final String MAX_ACC_WIDTH  =  "maxAccWidth";

    public static ComponentUI createUI(JComponent c) {
        return new BasicMenuItemUI();
    }

    public void installUI(JComponent c) {
        menuItem = (JMenuItem) c;

        installDefaults();
        installComponents(menuItem);
        installListeners();
        installKeyboardActions();
    }
	

    protected void installDefaults() {
        String prefix = getPropertyPrefix();

        acceleratorFont = UIManager.getFont("MenuItem.acceleratorFont");

        menuItem.setOpaque(true);
        if(menuItem.getMargin() == null || 
           (menuItem.getMargin() instanceof UIResource)) {
            menuItem.setMargin(UIManager.getInsets(prefix + ".margin"));
        }

        defaultTextIconGap = 4;   // Should be from table

        LookAndFeel.installBorder(menuItem, prefix + ".border");
        oldBorderPainted = menuItem.isBorderPainted();
        menuItem.setBorderPainted( ( (Boolean) (UIManager.get(prefix + ".borderPainted")) ).booleanValue() );
        LookAndFeel.installColorsAndFont(menuItem,
                                         prefix + ".background",
                                         prefix + ".foreground",
                                         prefix + ".font");
        
        // MenuItem specific defaults
        if (selectionBackground == null || 
            selectionBackground instanceof UIResource) {
            selectionBackground = 
                UIManager.getColor(prefix + ".selectionBackground");
        }
        if (selectionForeground == null || 
            selectionForeground instanceof UIResource) {
            selectionForeground = 
                UIManager.getColor(prefix + ".selectionForeground");
        }
        if (disabledForeground == null || 
            disabledForeground instanceof UIResource) {
            disabledForeground = 
                UIManager.getColor(prefix + ".disabledForeground");
        }
        if (acceleratorForeground == null || 
            acceleratorForeground instanceof UIResource) {
            acceleratorForeground = 
                UIManager.getColor(prefix + ".acceleratorForeground");
        }
        if (acceleratorSelectionForeground == null || 
            acceleratorSelectionForeground instanceof UIResource) {
            acceleratorSelectionForeground = 
                UIManager.getColor(prefix + ".acceleratorSelectionForeground");
        }
	// Get accelerator delimiter
	acceleratorDelimiter = 
	    UIManager.getString("MenuItem.acceleratorDelimiter");
	if (acceleratorDelimiter == null) { acceleratorDelimiter = "+"; }
        // Icons
        if (arrowIcon == null ||
            arrowIcon instanceof UIResource) {
            arrowIcon = UIManager.getIcon(prefix + ".arrowIcon");
        }
        if (checkIcon == null ||
            checkIcon instanceof UIResource) {
            checkIcon = UIManager.getIcon(prefix + ".checkIcon");
        }
    }

    /**
     * @since 1.3
     */
    protected void installComponents(JMenuItem menuItem){
 	BasicHTML.updateRenderer(menuItem, menuItem.getText());
    }

    protected String getPropertyPrefix() {
        return "MenuItem";
    }

    protected void installListeners() {
        mouseInputListener = createMouseInputListener(menuItem);
        menuDragMouseListener = createMenuDragMouseListener(menuItem);
        menuKeyListener = createMenuKeyListener(menuItem);
        propertyChangeListener = createPropertyChangeListener(menuItem);

        menuItem.addMouseListener(mouseInputListener);
        menuItem.addMouseMotionListener(mouseInputListener);
        menuItem.addMenuDragMouseListener(menuDragMouseListener);
        menuItem.addMenuKeyListener(menuKeyListener);
	menuItem.addPropertyChangeListener(propertyChangeListener);
    }

    protected void installKeyboardActions() {
	ActionMap actionMap = getActionMap();
	
	SwingUtilities.replaceUIActionMap(menuItem, actionMap);
	updateAcceleratorBinding();
    }

    public void uninstallUI(JComponent c) {
	menuItem = (JMenuItem)c;
        uninstallDefaults();
        uninstallComponents(menuItem);
        uninstallListeners();
        uninstallKeyboardActions();

	
	//Remove the textWidth and accWidth values from the parent's Client Properties.
	Container parent = menuItem.getParent();
	if ( (parent != null && parent instanceof JComponent)  && 
	     !(menuItem instanceof JMenu && ((JMenu) menuItem).isTopLevelMenu())) {
	    JComponent p = (JComponent) parent;
	    p.putClientProperty(BasicMenuItemUI.MAX_ACC_WIDTH, null );
	    p.putClientProperty(BasicMenuItemUI.MAX_TEXT_WIDTH, null ); 
	}

	menuItem = null;
    }


    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(menuItem);
        menuItem.setBorderPainted( oldBorderPainted );
        if (menuItem.getMargin() instanceof UIResource)
            menuItem.setMargin(null);
        if (arrowIcon instanceof UIResource)
            arrowIcon = null;
        if (checkIcon instanceof UIResource)
            checkIcon = null;
    }

    /**
     * @since 1.3
     */
    protected void uninstallComponents(JMenuItem menuItem){
	BasicHTML.updateRenderer(menuItem, "");
    }

    protected void uninstallListeners() {
        menuItem.removeMouseListener(mouseInputListener);
        menuItem.removeMouseMotionListener(mouseInputListener);
        menuItem.removeMenuDragMouseListener(menuDragMouseListener);
        menuItem.removeMenuKeyListener(menuKeyListener);
        menuItem.removePropertyChangeListener(propertyChangeListener);

        mouseInputListener = null;
        menuDragMouseListener = null;
        menuKeyListener = null;
	propertyChangeListener = null;
    }

    protected void uninstallKeyboardActions() {
	SwingUtilities.replaceUIActionMap(menuItem, null);
	if (windowInputMap != null) {
	    SwingUtilities.replaceUIInputMap(menuItem, JComponent.
					   WHEN_IN_FOCUSED_WINDOW, null);
	    windowInputMap = null;
	}
    }

    protected MouseInputListener createMouseInputListener(JComponent c) {
        return new MouseInputHandler();
    }

    protected MenuDragMouseListener createMenuDragMouseListener(JComponent c) {
        return new MenuDragMouseHandler();
    }

    protected MenuKeyListener createMenuKeyListener(JComponent c) {
        return new MenuKeyHandler();
    }

    private PropertyChangeListener createPropertyChangeListener(JComponent c) {
        return new PropertyChangeHandler();
    }

    ActionMap getActionMap() {
	String propertyPrefix = getPropertyPrefix();
	String uiKey = propertyPrefix + ".actionMap";
	ActionMap am = (ActionMap)UIManager.get(uiKey);
	if (am == null) {
	    am = createActionMap();
	    UIManager.put(uiKey, am);
	}
	return am;
    }

    ActionMap createActionMap() {
	ActionMap map = new ActionMapUIResource();
	map.put("doClick", new ClickAction());
	return map;
    }

    InputMap createInputMap(int condition) {
	if (condition == JComponent.WHEN_IN_FOCUSED_WINDOW) {
	    return new ComponentInputMapUIResource(menuItem);
	}
	return null;
    }

    void updateAcceleratorBinding() {
	KeyStroke accelerator = menuItem.getAccelerator();

	if (accelerator != null) {
	    if (windowInputMap == null) {
		windowInputMap = createInputMap(JComponent.
						WHEN_IN_FOCUSED_WINDOW);
		SwingUtilities.replaceUIInputMap(menuItem,
			   JComponent.WHEN_IN_FOCUSED_WINDOW, windowInputMap);
	    }
	    windowInputMap.put(accelerator, "doClick");
	}
	else if (windowInputMap != null) {
	    windowInputMap.clear();
	}
    }

    public Dimension getMinimumSize(JComponent c) {
	Dimension d = null;
 	View v = (View) c.getClientProperty(BasicHTML.propertyKey);
 	if (v != null) {
	    d = getPreferredSize(c);
 	    d.width -= v.getPreferredSpan(View.X_AXIS) - v.getMinimumSpan(View.X_AXIS);
 	}
 	return d;	
    }

    public Dimension getPreferredSize(JComponent c) {
        return getPreferredMenuItemSize(c,
                                        checkIcon, 
                                        arrowIcon, 
                                        defaultTextIconGap);
    }

    public Dimension getMaximumSize(JComponent c) {
	Dimension d = null;
 	View v = (View) c.getClientProperty(BasicHTML.propertyKey);
 	if (v != null) {
	    d = getPreferredSize(c);
 	    d.width += v.getMaximumSpan(View.X_AXIS) - v.getPreferredSpan(View.X_AXIS);
 	}
 	return d;
    }

  // these rects are used for painting and preferredsize calculations.
  // they used to be regenerated constantly.  Now they are reused.
    static Rectangle zeroRect = new Rectangle(0,0,0,0);
    static Rectangle iconRect = new Rectangle();
    static Rectangle textRect = new Rectangle();
    static Rectangle acceleratorRect = new Rectangle();
    static Rectangle checkIconRect = new Rectangle();
    static Rectangle arrowIconRect = new Rectangle();
    static Rectangle viewRect = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);
    static Rectangle r = new Rectangle();

    private void resetRects() {
        iconRect.setBounds(zeroRect);
        textRect.setBounds(zeroRect);
        acceleratorRect.setBounds(zeroRect);
        checkIconRect.setBounds(zeroRect);
        arrowIconRect.setBounds(zeroRect);
        viewRect.setBounds(0,0,Short.MAX_VALUE, Short.MAX_VALUE);
        r.setBounds(zeroRect);
    }

    protected Dimension getPreferredMenuItemSize(JComponent c,
                                                     Icon checkIcon,
                                                     Icon arrowIcon,
                                                     int defaultTextIconGap) {
        JMenuItem b = (JMenuItem) c;
        Icon icon = (Icon) b.getIcon(); 
        String text = b.getText();
        KeyStroke accelerator =  b.getAccelerator();
        String acceleratorText = "";

        if (accelerator != null) {
            int modifiers = accelerator.getModifiers();
            if (modifiers > 0) {
                acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
                //acceleratorText += "-";
                acceleratorText += acceleratorDelimiter;
          }
            acceleratorText += KeyEvent.getKeyText(accelerator.getKeyCode());
        }

        Font font = b.getFont();
        FontMetrics fm = b.getToolkit().getFontMetrics(font);
        FontMetrics fmAccel = b.getToolkit().getFontMetrics( acceleratorFont );

        resetRects();
        
        layoutMenuItem(
                  fm, text, fmAccel, acceleratorText, icon, checkIcon, arrowIcon,
                  b.getVerticalAlignment(), b.getHorizontalAlignment(),
                  b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                  viewRect, iconRect, textRect, acceleratorRect, checkIconRect, arrowIconRect,
                  text == null ? 0 : defaultTextIconGap,
                  defaultTextIconGap
                  );
        // find the union of the icon and text rects
        r.setBounds(textRect);
        r = SwingUtilities.computeUnion(iconRect.x,
                                        iconRect.y,
                                        iconRect.width,
                                        iconRect.height,
                                        r);
        //   r = iconRect.union(textRect);

	
	// To make the accelerator texts appear in a column, find the widest MenuItem text
	// and the widest accelerator text.

	//Get the parent, which stores the information.
	Container parent = menuItem.getParent();
	
	//Check the parent, and see that it is not a top-level menu.
        if (parent != null && parent instanceof JComponent && 
	    !(menuItem instanceof JMenu && ((JMenu) menuItem).isTopLevelMenu())) {
	    JComponent p = (JComponent) parent;
	    
	    //Get widest text so far from parent, if no one exists null is returned.
	    Integer maxTextWidth = (Integer) p.getClientProperty(BasicMenuItemUI.MAX_TEXT_WIDTH);
	    Integer maxAccWidth = (Integer) p.getClientProperty(BasicMenuItemUI.MAX_ACC_WIDTH);
	    
	    int maxTextValue = maxTextWidth!=null ? maxTextWidth.intValue() : 0;
	    int maxAccValue = maxAccWidth!=null ? maxAccWidth.intValue() : 0;
	    
	    //Compare the text widths, and adjust the r.width to the widest.
	    if (r.width < maxTextValue) {
		r.width = maxTextValue;
	    } else {
		p.putClientProperty(BasicMenuItemUI.MAX_TEXT_WIDTH, new Integer(r.width) );
	    }
	    
	  //Compare the accelarator widths.
	    if (acceleratorRect.width > maxAccValue) {
		maxAccValue = acceleratorRect.width;
		p.putClientProperty(BasicMenuItemUI.MAX_ACC_WIDTH, new Integer(acceleratorRect.width) );
	    }
	    
	    //Add on the widest accelerator 
	    r.width += maxAccValue;
	    r.width += defaultTextIconGap;
	    
	}
	
	if( useCheckAndArrow() ) {
	    // Add in the checkIcon
	    r.width += checkIconRect.width;
	    r.width += defaultTextIconGap;

	    // Add in the arrowIcon
	    r.width += defaultTextIconGap;
	    r.width += arrowIconRect.width;
        }	

	r.width += 2*defaultTextIconGap;

        Insets insets = b.getInsets();
        if(insets != null) {
            r.width += insets.left + insets.right;
            r.height += insets.top + insets.bottom;
        }

        // if the width is even, bump it up one. This is critical
        // for the focus dash line to draw properly
        if(r.width%2 == 0) {
            r.width++;
        }

        // if the height is even, bump it up one. This is critical
        // for the text to center properly
        if(r.height%2 == 0) {
            r.height++;
        }
/*
	if(!(b instanceof JMenu && ((JMenu) b).isTopLevelMenu()) ) {
	    
	    // Container parent = menuItem.getParent();
	    JComponent p = (JComponent) parent;
	    
	    System.out.println("MaxText: "+p.getClientProperty(BasicMenuItemUI.MAX_TEXT_WIDTH));
	    System.out.println("MaxACC"+p.getClientProperty(BasicMenuItemUI.MAX_ACC_WIDTH));
	    
	    System.out.println("returning pref.width: " + r.width);
	    System.out.println("Current getSize: " + b.getSize() + "\n");
        }*/
	return r.getSize();
    }

    /**
     * We draw the background in paintMenuItem()
     * so override update (which fills the background of opaque
     * components by default) to just call paint().
     *
     */
    public void update(Graphics g, JComponent c) {
        paint(g, c);
    }

    public void paint(Graphics g, JComponent c) {
        paintMenuItem(g, c, checkIcon, arrowIcon,
                      selectionBackground, selectionForeground,
                      defaultTextIconGap);
    }


    protected void paintMenuItem(Graphics g, JComponent c,
                                     Icon checkIcon, Icon arrowIcon,
                                     Color background, Color foreground,
                                     int defaultTextIconGap) {
        JMenuItem b = (JMenuItem) c;
        ButtonModel model = b.getModel();

        //   Dimension size = b.getSize();
        int menuWidth = b.getWidth();
        int menuHeight = b.getHeight();
        Insets i = c.getInsets();
	
        resetRects();

        viewRect.setBounds( 0, 0, menuWidth, menuHeight );

        viewRect.x += i.left;
        viewRect.y += i.top;
        viewRect.width -= (i.right + viewRect.x);
        viewRect.height -= (i.bottom + viewRect.y);


        Font holdf = g.getFont();
        Font f = c.getFont();
        g.setFont( f );
        FontMetrics fm = g.getFontMetrics( f );
        FontMetrics fmAccel = g.getFontMetrics( acceleratorFont );

        // Paint background
        Color holdc = g.getColor();
        if(c.isOpaque()) {
            if (model.isArmed()|| (c instanceof JMenu && model.isSelected())) {
                g.setColor(background);
                g.fillRect(0,0, menuWidth, menuHeight);
            } else {
                g.setColor(b.getBackground());
                g.fillRect(0,0, menuWidth, menuHeight);
            }
            g.setColor(holdc);
        }

        // get Accelerator text
        KeyStroke accelerator =  b.getAccelerator();
        String acceleratorText = "";
        if (accelerator != null) {
            int modifiers = accelerator.getModifiers();
            if (modifiers > 0) {
                acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
                //acceleratorText += "-";
                acceleratorText += acceleratorDelimiter;
          }
            acceleratorText += KeyEvent.getKeyText(accelerator.getKeyCode());
        }
        
        // layout the text and icon
        String text = layoutMenuItem(
            fm, b.getText(), fmAccel, acceleratorText, b.getIcon(),
            checkIcon, arrowIcon,
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect, acceleratorRect, 
            checkIconRect, arrowIconRect,
            b.getText() == null ? 0 : defaultTextIconGap,
            defaultTextIconGap
        );
          
        // Paint the Check
        if (checkIcon != null) {
            if(model.isArmed() || (c instanceof JMenu && model.isSelected())) {
                g.setColor(foreground);
            } else {
                g.setColor(holdc);
            }
            if( useCheckAndArrow() )
		checkIcon.paintIcon(c, g, checkIconRect.x, checkIconRect.y);
            g.setColor(holdc);
        }

        // Paint the Icon
        if(b.getIcon() != null) { 
            Icon icon;
            if(!model.isEnabled()) {
                icon = (Icon) b.getDisabledIcon();
            } else if(model.isPressed() && model.isArmed()) {
                icon = (Icon) b.getPressedIcon();
                if(icon == null) {
                    // Use default icon
                    icon = (Icon) b.getIcon();
                } 
            } else {
                icon = (Icon) b.getIcon();
            }
              
            if (icon!=null)   
                icon.paintIcon(c, g, iconRect.x, iconRect.y);
        }

        // Draw the Text
        if(text != null) {
 	    View v = (View) c.getClientProperty(BasicHTML.propertyKey);
 	    if (v != null) {
 		v.paint(g, textRect);
 	    } else {
		if(!model.isEnabled()) {
		    // *** paint the text disabled
		    if ( UIManager.get("MenuItem.disabledForeground") instanceof Color )
			{
			    g.setColor( UIManager.getColor("MenuItem.disabledForeground") );
			    BasicGraphicsUtils.drawString(g,text,model.getMnemonic(),
							  textRect.x, textRect.y + fm.getAscent());
			}
		    else
			{
			    g.setColor(b.getBackground().brighter());
			    BasicGraphicsUtils.drawString(g,text,model.getMnemonic(),
							  textRect.x, textRect.y + fm.getAscent());
			    g.setColor(b.getBackground().darker());
			    BasicGraphicsUtils.drawString(g,text,model.getMnemonic(),
							  textRect.x - 1, textRect.y + fm.getAscent() - 1);
			}
		} else {
		    // *** paint the text normally
		    if (model.isArmed()|| (c instanceof JMenu && model.isSelected())) {
			g.setColor(foreground);
		    } else {
			g.setColor(holdc);
		    }
		    BasicGraphicsUtils.drawString(g,text, 
						  model.getMnemonic(),
						  textRect.x,
						  textRect.y + fm.getAscent());
		}
	    }
	}
	
        // Draw the Accelerator Text
        if(acceleratorText != null && !acceleratorText.equals("")) {

	  //Get the maxAccWidth from the parent to calculate the offset.
	  int accOffset = 0;
	  Container parent = menuItem.getParent();
	  if (parent != null && parent instanceof JComponent) {
	    JComponent p = (JComponent) parent;
	    Integer maxValueInt = (Integer) p.getClientProperty(BasicMenuItemUI.MAX_ACC_WIDTH);
	    int maxValue = maxValueInt!=null ? maxValueInt.intValue() : 0;
	    
	    //Calculate the offset, with which the accelerator texts will be drawn with.
	    accOffset = maxValue - acceleratorRect.width;
	  }
	  
	  g.setFont( acceleratorFont );
            if(!model.isEnabled()) {
                // *** paint the acceleratorText disabled
	      if ( disabledForeground != null )
		  {
                  g.setColor( disabledForeground );
                  BasicGraphicsUtils.drawString(g,acceleratorText,0,
                                                acceleratorRect.x - accOffset, 
                                                acceleratorRect.y + fmAccel.getAscent());
                }
                else
                {
                  g.setColor(b.getBackground().brighter());
                  BasicGraphicsUtils.drawString(g,acceleratorText,0,
                                                acceleratorRect.x - accOffset, 
						acceleratorRect.y + fmAccel.getAscent());
                  g.setColor(b.getBackground().darker());
                  BasicGraphicsUtils.drawString(g,acceleratorText,0,
                                                acceleratorRect.x - accOffset - 1, 
						acceleratorRect.y + fmAccel.getAscent() - 1);
                }
            } else {
                // *** paint the acceleratorText normally
                if (model.isArmed()|| (c instanceof JMenu && model.isSelected())) {
                    g.setColor( acceleratorSelectionForeground );
                } else {
                    g.setColor( acceleratorForeground );
                }
                BasicGraphicsUtils.drawString(g,acceleratorText, 0,
                                              acceleratorRect.x - accOffset,
                                              acceleratorRect.y + fmAccel.getAscent());
            }
        }

        // Paint the Arrow
        if (arrowIcon != null) {
            if(model.isArmed() || (c instanceof JMenu &&model.isSelected()))
                g.setColor(foreground);
            if(useCheckAndArrow())
                arrowIcon.paintIcon(c, g, arrowIconRect.x, arrowIconRect.y);
        }
        g.setColor(holdc);
        g.setFont(holdf);
    }


    /** 
     * Compute and return the location of the icons origin, the 
     * location of origin of the text baseline, and a possibly clipped
     * version of the compound labels string.  Locations are computed
     * relative to the viewRect rectangle. 
     */

    private String layoutMenuItem(
        FontMetrics fm,
        String text,
        FontMetrics fmAccel,
        String acceleratorText,
        Icon icon,
        Icon checkIcon,
        Icon arrowIcon,
        int verticalAlignment,
        int horizontalAlignment,
        int verticalTextPosition,
        int horizontalTextPosition,
        Rectangle viewRect, 
        Rectangle iconRect, 
        Rectangle textRect,
        Rectangle acceleratorRect,
        Rectangle checkIconRect, 
        Rectangle arrowIconRect, 
        int textIconGap,
        int menuItemGap
        )
    {

        SwingUtilities.layoutCompoundLabel(
                            menuItem, fm, text, icon, verticalAlignment, 
                            horizontalAlignment, verticalTextPosition, 
                            horizontalTextPosition, viewRect, iconRect, textRect, 
                            textIconGap);

        /* Initialize the acceelratorText bounds rectangle textRect.  If a null 
         * or and empty String was specified we substitute "" here 
         * and use 0,0,0,0 for acceleratorTextRect.
         */
        if( (acceleratorText == null) || acceleratorText.equals("") ) {
            acceleratorRect.width = acceleratorRect.height = 0;
            acceleratorText = "";
        }
        else {
            acceleratorRect.width = SwingUtilities.computeStringWidth( fmAccel, acceleratorText );
            acceleratorRect.height = fmAccel.getHeight();
        }

        /* Initialize the checkIcon bounds rectangle's width & height.
         */

	if( useCheckAndArrow()) {
	    if (checkIcon != null) {
		checkIconRect.width = checkIcon.getIconWidth();
		checkIconRect.height = checkIcon.getIconHeight();
	    } 
	    else {
		checkIconRect.width = checkIconRect.height = 0;
	    }
	    
	    /* Initialize the arrowIcon bounds rectangle width & height.
	     */
	    
	    if (arrowIcon != null) {
		arrowIconRect.width = arrowIcon.getIconWidth();
		arrowIconRect.height = arrowIcon.getIconHeight();
	    } else {
		arrowIconRect.width = arrowIconRect.height = 0;
	    }
        }

        Rectangle labelRect = iconRect.union(textRect);
        if( BasicGraphicsUtils.isLeftToRight(menuItem) ) {
            textRect.x += menuItemGap;
            iconRect.x += menuItemGap;

            // Position the Accelerator text rect
            acceleratorRect.x = viewRect.x + viewRect.width - arrowIconRect.width 
                             - menuItemGap - acceleratorRect.width;
            
            // Position the Check and Arrow Icons 
            if (useCheckAndArrow()) {
                checkIconRect.x = viewRect.x + menuItemGap;
                textRect.x += menuItemGap + checkIconRect.width;
                iconRect.x += menuItemGap + checkIconRect.width;
                arrowIconRect.x = viewRect.x + viewRect.width - menuItemGap
                                  - arrowIconRect.width;
            }
        } else {
            textRect.x -= menuItemGap;
            iconRect.x -= menuItemGap;

            // Position the Accelerator text rect
            acceleratorRect.x = viewRect.x + arrowIconRect.width + menuItemGap;

            // Position the Check and Arrow Icons 
            if (useCheckAndArrow()) {
                checkIconRect.x = viewRect.x + viewRect.width - menuItemGap
                                  - checkIconRect.width;
                textRect.x -= menuItemGap + checkIconRect.width;
                iconRect.x -= menuItemGap + checkIconRect.width;      
                arrowIconRect.x = viewRect.x + menuItemGap;
            }
        }

        // Align the accelertor text and the check and arrow icons vertically
        // with the center of the label rect.  
        acceleratorRect.y = labelRect.y + (labelRect.height/2) - (acceleratorRect.height/2);
        if( useCheckAndArrow() ) {
            arrowIconRect.y = labelRect.y + (labelRect.height/2) - (arrowIconRect.height/2);
            checkIconRect.y = labelRect.y + (labelRect.height/2) - (checkIconRect.height/2);
        }

        /*
        System.out.println("Layout: text="+menuItem.getText()+"\n\tv="
                           +viewRect+"\n\tc="+checkIconRect+"\n\ti="
                           +iconRect+"\n\tt="+textRect+"\n\tacc="
                           +acceleratorRect+"\n\ta="+arrowIconRect+"\n");
        */
        
        return text;
    }

    /*
     * Returns false if the component is a JMenu and it is a top
     * level menu (on the menubar).
     */
    private boolean useCheckAndArrow(){
	boolean b = true;
	if((menuItem instanceof JMenu) &&
	   (((JMenu)menuItem).isTopLevelMenu())) {
	    b = false;
	}
	return b;
    }

    public MenuElement[] getPath() {
        MenuSelectionManager m = MenuSelectionManager.defaultManager();
        MenuElement oldPath[] = m.getSelectedPath();
        MenuElement newPath[];
        int i = oldPath.length;
        if (i == 0)
            return new MenuElement[0];
        Component parent = menuItem.getParent();
        if (oldPath[i-1].getComponent() == parent) {
            // The parent popup menu is the last so far
            newPath = new MenuElement[i+1];
            System.arraycopy(oldPath, 0, newPath, 0, i);
            newPath[i] = menuItem;
        } else {
            // A sibling menuitem is the current selection
            // 
            //  This probably needs to handle 'exit submenu into 
            // a menu item.  Search backwards along the current
            // selection until you find the parent popup menu,
            // then copy up to that and add yourself...
            int j;
            for (j = oldPath.length-1; j >= 0; j--) {
                if (oldPath[j].getComponent() == parent)
                    break;
            }
            newPath = new MenuElement[j+2];
            System.arraycopy(oldPath, 0, newPath, 0, j+1);
            newPath[j+1] = menuItem;
            /*
            System.out.println("Sibling condition -- ");
            System.out.println("Old array : ");
            printMenuElementArray(oldPath, false);
            System.out.println("New array : ");
            printMenuElementArray(newPath, false);
            */
        }
        return newPath;
    }

    void printMenuElementArray(MenuElement path[], boolean dumpStack) {
        System.out.println("Path is(");
        int i, j;
        for(i=0,j=path.length; i<j ;i++){
            for (int k=0; k<=i; k++)
                System.out.print("  ");
            MenuElement me = (MenuElement) path[i];
            if(me instanceof JMenuItem) 
                System.out.println(((JMenuItem)me).getText() + ", ");
            else if (me == null)
                System.out.println("NULL , ");
            else
                System.out.println("" + me + ", ");
        }
        System.out.println(")");

        if (dumpStack == true)
            Thread.dumpStack();
    }
    protected class MouseInputHandler implements MouseInputListener {
        public void mouseClicked(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {
        }
        public void mouseReleased(MouseEvent e) {
            MenuSelectionManager manager = 
                MenuSelectionManager.defaultManager();
            Point p = e.getPoint();
            if(p.x >= 0 && p.x < menuItem.getWidth() &&
               p.y >= 0 && p.y < menuItem.getHeight()) {
               manager.clearSelectedPath();
               menuItem.doClick(0);
            } else {
                manager.processMouseEvent(e);
            }
        }
        public void mouseEntered(MouseEvent e) {
            MenuSelectionManager manager = MenuSelectionManager.defaultManager();
	    int modifiers = e.getModifiers();
	    // 4188027: drag enter/exit added in JDK 1.1.7A, JDK1.2	    
	    if ((modifiers & (InputEvent.BUTTON1_MASK |
			      InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) !=0 ) {
		MenuSelectionManager.defaultManager().processMouseEvent(e);
	    } else {
	    manager.setSelectedPath(getPath());
	     }
        }
        public void mouseExited(MouseEvent e) {
            MenuSelectionManager manager = MenuSelectionManager.defaultManager();

	    int modifiers = e.getModifiers();
	    // 4188027: drag enter/exit added in JDK 1.1.7A, JDK1.2
	    if ((modifiers & (InputEvent.BUTTON1_MASK |
			      InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) !=0 ) {
		MenuSelectionManager.defaultManager().processMouseEvent(e);
	    } else {

		MenuElement path[] = manager.getSelectedPath();
		if (path.length > 1) {
		    MenuElement newPath[] = new MenuElement[path.length-1];
		    int i,c;
		    for(i=0,c=path.length-1;i<c;i++)
			newPath[i] = path[i];
		    manager.setSelectedPath(newPath);
		}
		}
        }

        public void mouseDragged(MouseEvent e) {
            MenuSelectionManager.defaultManager().processMouseEvent(e);
        }
        public void mouseMoved(MouseEvent e) {
        }
    }


    private class MenuDragMouseHandler implements MenuDragMouseListener {
        public void menuDragMouseEntered(MenuDragMouseEvent e) {
             MenuSelectionManager manager = e.getMenuSelectionManager();
             MenuElement path[] = e.getPath();
             manager.setSelectedPath(path);
        }
        public void menuDragMouseDragged(MenuDragMouseEvent e) {
            MenuSelectionManager manager = e.getMenuSelectionManager();
            MenuElement path[] = e.getPath();
            manager.setSelectedPath(path);
        }
        public void menuDragMouseExited(MenuDragMouseEvent e) {}
        public void menuDragMouseReleased(MenuDragMouseEvent e) {
            MenuSelectionManager manager = e.getMenuSelectionManager();
            MenuElement path[] = e.getPath();
            Point p = e.getPoint();
            if(p.x >= 0 && p.x < menuItem.getWidth() &&
               p.y >= 0 && p.y < menuItem.getHeight()) {
               manager.clearSelectedPath();
               menuItem.doClick(0);
            } else {
                manager.clearSelectedPath();
            }
        }
    }

    private class MenuKeyHandler implements MenuKeyListener {
        public void menuKeyTyped(MenuKeyEvent e) {
	    if (DEBUG) {
		System.out.println("in BasicMenuItemUI.menuKeyTyped for " + menuItem.getText());
	    }
            int key = menuItem.getMnemonic();
            if(key == 0)
                return;
            if(lower(key) == lower((int)(e.getKeyChar()))) {
                MenuSelectionManager manager = 
                    e.getMenuSelectionManager();
                manager.clearSelectedPath();
                menuItem.doClick(0);
                e.consume();
            }
        }
        public void menuKeyPressed(MenuKeyEvent e) {
	    if (DEBUG) {
		System.out.println("in BasicMenuItemUI.menuKeyPressed for " + menuItem.getText());
	    }
	}
        public void menuKeyReleased(MenuKeyEvent e) {}

        private int lower(int ascii) {
            if(ascii >= 'A' && ascii <= 'Z')
                return ascii + 'a' - 'A';
            else
                return ascii;
        }

    }

    private class PropertyChangeHandler implements PropertyChangeListener {
	public void propertyChange(PropertyChangeEvent e) {
	    String name = e.getPropertyName();

	    if (name.equals("labelFor") || name.equals("displayedMnemonic") ||
		name.equals("accelerator")) {
		updateAcceleratorBinding();
	    } else if (name.equals("text")) {
		// remove the old html view client property if one
		// existed, and install a new one if the text installed
		// into the JLabel is html source.
		JMenuItem lbl = ((JMenuItem) e.getSource());
		String text = lbl.getText();
		BasicHTML.updateRenderer(lbl, text);
	    }
	}
    }  

    private static class ClickAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) {
	    JMenuItem mi = (JMenuItem)e.getSource();
	    MenuSelectionManager.defaultManager().clearSelectedPath();
	    mi.doClick();
	}
    }
}
