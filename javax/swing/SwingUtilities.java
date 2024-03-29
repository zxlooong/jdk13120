/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.applet.*;

import java.awt.*;
import java.awt.event.*;

import java.util.Vector;
import java.util.Hashtable;

import java.lang.reflect.*;

import javax.accessibility.*;
import javax.swing.plaf.UIResource;
import javax.swing.text.View;

/**
 * A collection of utility methods for Swing.
 *
 * @version 1.96 02/06/02
 * @author unknown
 */
public class SwingUtilities implements SwingConstants
{
    // These states are system-wide, rather than AppContext wide.
    private static boolean canAccessEventQueue = false;
    private static boolean eventQueueTested = false;


    /** 
     * Return true if <code>a</code> contains <code>b</code>
     */
    public static final boolean isRectangleContainingRectangle(Rectangle a,Rectangle b) {
        if (b.x >= a.x && (b.x + b.width) <= (a.x + a.width) &&
            b.y >= a.y && (b.y + b.height) <= (a.y + a.height)) {
            return true;
        }
        return false;
    }

    /**
     * Return the rectangle (0,0,bounds.width,bounds.height) for the component <code>aComponent</code>
     */
    public static Rectangle getLocalBounds(Component aComponent) {
        Rectangle b = new Rectangle(aComponent.getBounds());
        b.x = b.y = 0;
        return b;
    }


    /**
     * @return the first Window ancestor of c, or null if component is not contained inside a window
     */
    public static Window getWindowAncestor(Component c) {
        for(Container p = c.getParent(); p != null; p = p.getParent()) {
            if (p instanceof Window) {
                return (Window)p;
            }
        }
        return null;
    }


    /**
     * Convert a <code>aPoint</code> in <code>source</code> coordinate system to
     * <code>destination</code> coordinate system.
     * If <code>source></code>is null,<code>aPoint</code> is assumed to be in <code>destination</code>'s
     * root component coordinate system.
     * If <code>destination</code>is null, <code>aPoint</code> will be converted to <code>source</code>'s
     * root component coordinate system.
     * If both <code>source</code> and <code>destination</code> are null, return <code>aPoint</code>
     * without any conversion.
     */
    public static Point convertPoint(Component source,Point aPoint,Component destination) {
        Point p;

        if(source == null && destination == null)
            return aPoint;
        if(source == null) {
            source = getWindowAncestor(destination);
            if(source == null)
                throw new Error("Source component not connected to component tree hierarchy");
        }
        p = new Point(aPoint);
        convertPointToScreen(p,source);
        if(destination == null) {
            destination = getWindowAncestor(source);
            if(destination == null)
                throw new Error("Destination component not connected to component tree hierarchy");
        }
        convertPointFromScreen(p,destination);
        return p;
    }

    /**
     * Convert the point <code>(x,y)</code> in <code>source</code> coordinate system to
     * <code>destination</code> coordinate system.
     * If <code>source></code>is null,<code>(x,y)</code> is assumed to be in <code>destination</code>'s
     * root component coordinate system.
     * If <code>destination</code>is null, <code>(x,y)</code> will be converted to <code>source</code>'s
     * root component coordinate system.
     * If both <code>source</code> and <code>destination</code> are null, return <code>(x,y)</code>
     * without any conversion.
     */
    public static Point convertPoint(Component source,int x, int y,Component destination) {
        Point point = new Point(x,y);
        return convertPoint(source,point,destination);
    }

    /** 
     * Convert the rectangle <code>aRectangle</code> in <code>source</code> coordinate system to
     * <code>destination</code> coordinate system.
     * If <code>source></code>is null,<code>aRectangle</code> is assumed to be in <code>destination</code>'s
     * root component coordinate system.
     * If <code>destination</code>is null, <code>aRectangle</code> will be converted to <code>source</code>'s
     * root component coordinate system.
     * If both <code>source</code> and <code>destination</code> are null, return <code>aRectangle</code>
     * without any conversion.
     */
    public static Rectangle convertRectangle(Component source,Rectangle aRectangle,Component destination) {
        Point point = new Point(aRectangle.x,aRectangle.y);
        point =  convertPoint(source,point,destination);
        return new Rectangle(point.x,point.y,aRectangle.width,aRectangle.height);
    }

    /**
     * Convenience method for searching above <code>comp</code> in the
     * component hierarchy and returns the first object of class <code>c</code> it
     * finds. Can return null, if a class <code>c</code> cannot be found.
     */
    public static Container getAncestorOfClass(Class c, Component comp) {
        if(comp == null || c == null)
            return null;

        Container parent = comp.getParent();
        while(parent != null && !(c.isInstance(parent)))
            parent = parent.getParent();
        return parent;
    }

    /**
     * Convenience method for searching above <code>comp</code> in the
     * component hierarchy and returns the first object of <code>name</code> it
     * finds. Can return null, if <code>name</code> cannot be found.
     */
    public static Container getAncestorNamed(String name, Component comp) {
        if(comp == null || name == null)
            return null;

        Container parent = comp.getParent();
        while(parent != null && !(name.equals(parent.getName())))
            parent = parent.getParent();
        return parent;
    }

    /**
     * Returns the deepest visible descendent Component of <code>parent</code> 
     * that contains the location <code>x</code>, <code>y</code>. 
     * If <code>parent</code> does not contain the specified location,
     * then <code>null</code> is returned.  If <code>parent</code> is not a 
     * container, or none of <code>parent</code>'s visible descendents 
     * contain the specified location, <code>parent</code> is returned.
     *
     * @param parent the root component to begin the search
     * @param x the x target location 
     * @param y the y target location  
     */
    public static Component getDeepestComponentAt(Component parent, int x, int y) {
        if (!parent.contains(x, y)) {
            return null;
        }
        if (parent instanceof Container) {        
            Component components[] = ((Container)parent).getComponents();
            for (int i = 0 ; i < components.length ; i++) {
                Component comp = components[i];
                if (comp != null && comp.isVisible()) {
                    Point loc = comp.getLocation();
                    if (comp instanceof Container) {
                        comp = getDeepestComponentAt(comp, x - loc.x, y - loc.y);
                    } else {
                        comp = comp.getComponentAt(x - loc.x, y - loc.y);
                    }
                    if (comp != null && comp.isVisible()) {
                        return comp;
                    }
                }
            }
        }
        return parent;
    }


    /** 
     * Returns a MouseEvent similar to <code>sourceEvent</code> except that its x
     * and y members have been converted to <code>destination</code>'s coordinate
     * system.  If <code>source</code> is null, <code>sourceEvent</code> x and y members
     * are assumed to be into <code>destination</code>'s root component coordinate system.
     * If <code>destination</code> is <code>null</code>, the
     * returned MouseEvent will be in <code>source</code>'s coordinate system.
     * <code>sourceEvent</code> will not be changed. A new event is returned.
     * the <code>source</code> field of the returned event will be set
     * to <code>destination</code> if destination is non null
     * use the translateMouseEvent() method to translate a mouse event from
     * one component to another without changing the source.
     */
    public static MouseEvent convertMouseEvent(Component source,
                                               MouseEvent sourceEvent,
                                               Component destination) {
        Point p = convertPoint(source,new Point(sourceEvent.getX(),
                                                sourceEvent.getY()),
                               destination);
        Component newSource;

        if(destination != null)
            newSource = destination;
        else
            newSource = source;

        return new MouseEvent(newSource,
                              sourceEvent.getID(),
                              sourceEvent.getWhen(),
                              sourceEvent.getModifiers(),
                              p.x,p.y,
                              sourceEvent.getClickCount(),
                              sourceEvent.isPopupTrigger());
    }


    /**
     * Convert a point from a component's coordinate system to
     * screen coordinates.
     *
     * @param p  a Point object (converted to the new coordinate system)
     * @param c  a Component object
     */
    public static void convertPointToScreen(Point p,Component c) {
            Rectangle b;
            int x,y;

            do {
                if(c instanceof JComponent) {
                    x = ((JComponent)c).getX();
                    y = ((JComponent)c).getY();
                } else if(c instanceof java.applet.Applet) {
                    Point pp = c.getLocationOnScreen();
                    x = pp.x;
                    y = pp.y;
                } else {
                    b = c.getBounds();
                    x = b.x;
                    y = b.y;
                }

                p.x += x;
                p.y += y;

                if(c instanceof java.awt.Window || c instanceof java.applet.Applet)
                    break;
                c = c.getParent();
            } while(c != null);
        }

    /**
     * Convert a point from a screen coordinates to a component's 
     * coordinate system
     *
     * @param p  a Point object (converted to the new coordinate system)
     * @param c  a Component object
     */
    public static void convertPointFromScreen(Point p,Component c) {
        Rectangle b;
        int x,y;

        do {
            if(c instanceof JComponent) {
                x = ((JComponent)c).getX();
                y = ((JComponent)c).getY();
            }  else if(c instanceof java.applet.Applet) {
                Point pp = c.getLocationOnScreen();
                x = pp.x;
                y = pp.y;
            } else {
                b = c.getBounds();
                x = b.x;
                y = b.y;
            }

            p.x -= x;
            p.y -= y;

            if(c instanceof java.awt.Window || c instanceof java.applet.Applet)
                break;
            c = c.getParent();
        } while(c != null);
    }

    /** Return <code>aComponent</code>'s window **/
    public static Window windowForComponent(Component aComponent) {
        for (Container p = aComponent.getParent(); p != null; p = p.getParent()) {
            if (p instanceof Window) {
                return (Window)p;
            }
        }
        return null;
    }

    /**
     * Return <code>true</code> if a component <code>a</code> descends from a component <code>b</code>
     */
    public static boolean isDescendingFrom(Component a,Component b) {
        if(a == b)
            return true;
        for(Container p = a.getParent();p!=null;p=p.getParent())
            if(p == b)
                return true;
        return false;
    }


    /**
     * Convenience to calculate the intersection of two rectangles
     * without allocating a new rectangle.
     * If the two rectangles don't intersect, 
     * then the returned rectangle begins at (0,0)
     * and has zero width and height.
     *
     * @param x       the X coordinate of the first rectangle's top-left point
     * @param y       the Y coordinate of the first rectangle's top-left point
     * @param width   the width of the first rectangle
     * @param height  the height of the first rectangle
     * @param dest    the second rectangle
     *
     * @return <code>dest</code>, modified to specify the intersection
     */
    public static Rectangle computeIntersection(int x,int y,int width,int height,Rectangle dest) {
        int x1 = (x > dest.x) ? x : dest.x;
        int x2 = ((x+width) < (dest.x + dest.width)) ? (x+width) : (dest.x + dest.width);
        int y1 = (y > dest.y) ? y : dest.y;
        int y2 = ((y + height) < (dest.y + dest.height) ? (y+height) : (dest.y + dest.height));

        dest.x = x1;
        dest.y = y1;
        dest.width = x2 - x1;
        dest.height = y2 - y1;

	// If rectangles don't intersect, return zero'd intersection.
	if (dest.width < 0 || dest.height < 0) {
	    dest.x = dest.y = dest.width = dest.height = 0;
	}

        return dest;
    }

    /**
     * Convenience to calculate the union of two rectangles without allocating a new rectangle
     * Return dest
     */
    public static Rectangle computeUnion(int x,int y,int width,int height,Rectangle dest) {
        int x1 = (x < dest.x) ? x : dest.x;
        int x2 = ((x+width) > (dest.x + dest.width)) ? (x+width) : (dest.x + dest.width);
        int y1 = (y < dest.y) ? y : dest.y;
        int y2 = ((y+height) > (dest.y + dest.height)) ? (y+height) : (dest.y + dest.height);

        dest.x = x1;
        dest.y = y1;
        dest.width = (x2 - x1);
        dest.height= (y2 - y1);
        return dest;
    }

    /**
     * Convenience returning an array of rect representing the regions within
     * <code>rectA</code> that do not overlap with <code>rectB</code>. If the
     * two Rects do not overlap, returns an empty array
     */
    public static Rectangle[] computeDifference(Rectangle rectA,Rectangle rectB) {
        if (rectB == null || !rectA.intersects(rectB) || isRectangleContainingRectangle(rectB,rectA)) {
            return new Rectangle[0];
        }

        Rectangle t = new Rectangle();
        Rectangle a=null,b=null,c=null,d=null;
        Rectangle result[];
        int rectCount = 0;

        /* rectA contains rectB */
        if (isRectangleContainingRectangle(rectA,rectB)) {
            t.x = rectA.x; t.y = rectA.y; t.width = rectB.x - rectA.x; t.height = rectA.height;
            if(t.width > 0 && t.height > 0) {
                a = new Rectangle(t);
                rectCount++;
            }

            t.x = rectB.x; t.y = rectA.y; t.width = rectB.width; t.height = rectB.y - rectA.y;
            if(t.width > 0 && t.height > 0) {
                b = new Rectangle(t);
                rectCount++;
            }

            t.x = rectB.x; t.y = rectB.y + rectB.height; t.width = rectB.width;
            t.height = rectA.y + rectA.height - (rectB.y + rectB.height);
            if(t.width > 0 && t.height > 0) {
                c = new Rectangle(t);
                rectCount++;
            }

            t.x = rectB.x + rectB.width; t.y = rectA.y; t.width = rectA.x + rectA.width - (rectB.x + rectB.width);
            t.height = rectA.height;
            if(t.width > 0 && t.height > 0) {
                d = new Rectangle(t);
                rectCount++;
            }
        } else {
            /* 1 */
            if (rectB.x <= rectA.x && rectB.y <= rectA.y) {
                if ((rectB.x + rectB.width) > (rectA.x + rectA.width)) {

                    t.x = rectA.x; t.y = rectB.y + rectB.height;
                    t.width = rectA.width; t.height = rectA.y + rectA.height - (rectB.y + rectB.height);
                    if(t.width > 0 && t.height > 0) {
                        a = t;
                        rectCount++;
                    }
                } else if ((rectB.y + rectB.height) > (rectA.y + rectA.height)) {
                    t.setBounds((rectB.x + rectB.width), rectA.y,
                                (rectA.x + rectA.width) - (rectB.x + rectB.width), rectA.height);
                    if(t.width > 0 && t.height > 0) {
                        a = t;
                        rectCount++;
                    }
                } else {
                    t.setBounds((rectB.x + rectB.width), rectA.y,
                                (rectA.x + rectA.width) - (rectB.x + rectB.width),
                                (rectB.y + rectB.height) - rectA.y);
                    if(t.width > 0 && t.height > 0) {
                        a = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds(rectA.x, (rectB.y + rectB.height), rectA.width,
                                (rectA.y + rectA.height) - (rectB.y + rectB.height));
                    if(t.width > 0 && t.height > 0) {
                        b = new Rectangle(t);
                        rectCount++;
                    }
                }
            } else if (rectB.x <= rectA.x && (rectB.y + rectB.height) >= (rectA.y + rectA.height)) {
                if ((rectB.x + rectB.width) > (rectA.x + rectA.width)) {
                    t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                    if(t.width > 0 && t.height > 0) {
                        a = t;
                        rectCount++;
                    }
                } else {
                    t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                    if(t.width > 0 && t.height > 0) {
                        a = new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds((rectB.x + rectB.width), rectB.y,
                                (rectA.x + rectA.width) - (rectB.x + rectB.width),
                                (rectA.y + rectA.height) - rectB.y);
                    if(t.width > 0 && t.height > 0) {
                        b = new Rectangle(t);
                        rectCount++;
                    }
                }
            } else if (rectB.x <= rectA.x) {
                if ((rectB.x + rectB.width) >= (rectA.x + rectA.width)) {
                    t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                    if(t.width>0 && t.height > 0) {
                        a = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds(rectA.x, (rectB.y + rectB.height), rectA.width,
                                (rectA.y + rectA.height) - (rectB.y + rectB.height));
                    if(t.width > 0 && t.height > 0) {
                        b = new Rectangle(t);
                        rectCount++;
                    }
                } else {
                    t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                    if(t.width > 0 && t.height > 0) {
                        a = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds((rectB.x + rectB.width), rectB.y,
                                (rectA.x + rectA.width) - (rectB.x + rectB.width),
                                rectB.height);
                    if(t.width > 0 && t.height > 0) {
                        b = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds(rectA.x, (rectB.y + rectB.height), rectA.width,
                                (rectA.y + rectA.height) - (rectB.y + rectB.height));
                    if(t.width > 0 && t.height > 0) {
                        c = new Rectangle(t);
                        rectCount++;
                    }
                }
            } else if (rectB.x <= (rectA.x + rectA.width) && (rectB.x + rectB.width) > (rectA.x + rectA.width)) {
                if (rectB.y <= rectA.y && (rectB.y + rectB.height) > (rectA.y + rectA.height)) {
                    t.setBounds(rectA.x, rectA.y, rectB.x - rectA.x, rectA.height);
                    if(t.width > 0 && t.height > 0) {
                        a = t;
                        rectCount++;
                    }
                } else if (rectB.y <= rectA.y) {
                    t.setBounds(rectA.x, rectA.y, rectB.x - rectA.x,
                                (rectB.y + rectB.height) - rectA.y);
                    if(t.width > 0 && t.height > 0) {
                        a = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds(rectA.x, (rectB.y + rectB.height), rectA.width,
                                (rectA.y + rectA.height) - (rectB.y + rectB.height));
                    if(t.width > 0 && t.height > 0) {
                        b = new Rectangle(t);
                        rectCount++;
                    }
                } else if ((rectB.y + rectB.height) > (rectA.y + rectA.height)) {
                    t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                    if(t.width > 0 && t.height > 0) {
                        a = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds(rectA.x, rectB.y, rectB.x - rectA.x,
                                (rectA.y + rectA.height) - rectB.y);
                    if(t.width > 0 && t.height > 0) {
                        b = new Rectangle(t);
                        rectCount++;
                    }
                } else {
                    t.setBounds(rectA.x, rectA.y, rectA.width, rectB.y - rectA.y);
                    if(t.width > 0 && t.height > 0) {
                        a = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds(rectA.x, rectB.y, rectB.x - rectA.x,
                                rectB.height);
                    if(t.width > 0 && t.height > 0) {
                        b = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds(rectA.x, (rectB.y + rectB.height), rectA.width,
                                (rectA.y + rectA.height) - (rectB.y + rectB.height));
                    if(t.width > 0 && t.height > 0) {
                        c = new Rectangle(t);
                        rectCount++;
                    }
                }
            } else if (rectB.x >= rectA.x && (rectB.x + rectB.width) <= (rectA.x + rectA.width)) {
                if (rectB.y <= rectA.y && (rectB.y + rectB.height) > (rectA.y + rectA.height)) {
                    t.setBounds(rectA.x, rectA.y, rectB.x - rectA.x, rectA.height);
                    if(t.width > 0 && t.height > 0) {
                        a = new Rectangle(t);
                        rectCount++;
                    }
                    t.setBounds((rectB.x + rectB.width), rectA.y,
                                (rectA.x + rectA.width) - (rectB.x + rectB.width), rectA.height);
                    if(t.width > 0 && t.height > 0) {
                        b = new Rectangle(t);
                        rectCount++;
                    }
                } else if (rectB.y <= rectA.y) {
                    t.setBounds(rectA.x, rectA.y, rectB.x - rectA.x, rectA.height);
                    if(t.width > 0 && t.height > 0) {
                        a = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds(rectB.x, (rectB.y + rectB.height),
                                rectB.width,
                                (rectA.y + rectA.height) - (rectB.y + rectB.height));
                    if(t.width > 0 && t.height > 0) {
                        b = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds((rectB.x + rectB.width), rectA.y,
                                (rectA.x + rectA.width) - (rectB.x + rectB.width), rectA.height);
                    if(t.width > 0 && t.height > 0) {
                        c = new Rectangle(t);
                        rectCount++;
                    }
                } else {
                    t.setBounds(rectA.x, rectA.y, rectB.x - rectA.x, rectA.height);
                    if(t.width > 0 && t.height > 0) {
                        a = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds(rectB.x, rectA.y, rectB.width,
                                rectB.y - rectA.y);
                    if(t.width > 0 && t.height > 0) {
                        b = new Rectangle(t);
                        rectCount++;
                    }

                    t.setBounds((rectB.x + rectB.width), rectA.y,
                                (rectA.x + rectA.width) - (rectB.x + rectB.width), rectA.height);
                    if(t.width > 0 && t.height > 0) {
                        c = new Rectangle(t);
                        rectCount++;
                    }
                }
            }
        }

        result = new Rectangle[rectCount];
        rectCount = 0;
        if(a != null)
            result[rectCount++] = a;
        if(b != null)
            result[rectCount++] = b;
        if(c != null)
            result[rectCount++] = c;
        if(d != null)
            result[rectCount++] = d;
        return result;
    }

    /**
     * Returns true if the mouse event specifies the left mouse button.
     *
     * @param anEvent  a MouseEvent object
     * @return true if the left mouse button was active
     */
    public static boolean isLeftMouseButton(MouseEvent anEvent) {
             return ((anEvent.getModifiers() & InputEvent.BUTTON1_MASK) != 0);
    }

    /**
     * Returns true if the mouse event specifies the middle mouse button.
     *
     * @param anEvent  a MouseEvent object
     * @return true if the middle mouse button was active
     */
    public static boolean isMiddleMouseButton(MouseEvent anEvent) {
        return ((anEvent.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK);
    }

    /**
     * Returns true if the mouse event specifies the right mouse button.
     *
     * @param anEvent  a MouseEvent object
     * @return true if the right mouse button was active
     */
    public static boolean isRightMouseButton(MouseEvent anEvent) {
        return ((anEvent.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
    }

    /**
     * Compute the width of the string using a font with the specified
     * "metrics" (sizes).
     *
     * @param fm   a FontMetrics object to compute with
     * @param str  the String to compute
     * @return an int containing the string width
     */
    public static int computeStringWidth(FontMetrics fm,String str) {
            // You can't assume that a string's width is the sum of its
            // characters' widths in Java2D -- it may be smaller due to
            // kerning, etc.
            return fm.stringWidth(str);
        }

    /**
     * Compute and return the location of the icons origin, the
     * location of origin of the text baseline, and a possibly clipped
     * version of the compound labels string.  Locations are computed
     * relative to the viewR rectangle.
     * The JComponents orientation (LEADING/TRAILING) will also be taken
     * into account and translated into LEFT/RIGHT values accordingly.
     */
    public static String layoutCompoundLabel(JComponent c,
                                             FontMetrics fm,
                                             String text,
                                             Icon icon,
                                             int verticalAlignment,
                                             int horizontalAlignment,
                                             int verticalTextPosition,
                                             int horizontalTextPosition,
                                             Rectangle viewR,
                                             Rectangle iconR,
                                             Rectangle textR,
                                             int textIconGap)
    {
        boolean orientationIsLeftToRight = true;
        int     hAlign = horizontalAlignment;
        int     hTextPos = horizontalTextPosition;

        if (c != null) {
            if (!(c.getComponentOrientation().isLeftToRight())) {
                orientationIsLeftToRight = false;
            }
        }

        // Translate LEADING/TRAILING values in horizontalAlignment
        // to LEFT/RIGHT values depending on the components orientation
        switch (horizontalAlignment) {
        case LEADING: 
            hAlign = (orientationIsLeftToRight) ? LEFT : RIGHT;
            break;
        case TRAILING: 
            hAlign = (orientationIsLeftToRight) ? RIGHT : LEFT;
            break;
        }

        // Translate LEADING/TRAILING values in horizontalTextPosition
        // to LEFT/RIGHT values depending on the components orientation
        switch (horizontalTextPosition) {
        case LEADING: 
            hTextPos = (orientationIsLeftToRight) ? LEFT : RIGHT;
            break;
        case TRAILING: 
            hTextPos = (orientationIsLeftToRight) ? RIGHT : LEFT;
            break;
        }

        return layoutCompoundLabelImpl(c,
				       fm,
				       text,
				       icon,
				       verticalAlignment,
				       hAlign,
				       verticalTextPosition,
				       hTextPos,
				       viewR,
				       iconR,
				       textR,
				       textIconGap);
    }

    /**
     * Compute and return the location of the icons origin, the
     * location of origin of the text baseline, and a possibly clipped
     * version of the compound labels string.  Locations are computed
     * relative to the viewR rectangle.
     * This layoutCompoundLabel() does not know how to handle LEADING/TRAILING
     * values in horizontalTextPosition (they will default to RIGHT) and in
     * horizontalAlignment (they will default to CENTER).
     * Use the other version of layoutCompoundLabel() instead.
     */
    public static String layoutCompoundLabel(
        FontMetrics fm,
        String text,
        Icon icon,
        int verticalAlignment,
        int horizontalAlignment,
        int verticalTextPosition,
        int horizontalTextPosition,
        Rectangle viewR,
        Rectangle iconR,
        Rectangle textR,
        int textIconGap)
    {
	return layoutCompoundLabelImpl(null, fm, text, icon, 
				       verticalAlignment,
				       horizontalAlignment,
				       verticalTextPosition,
				       horizontalTextPosition,
				       viewR, iconR, textR, textIconGap);
    }
				   
    /**
     * Compute and return the location of the icons origin, the
     * location of origin of the text baseline, and a possibly clipped
     * version of the compound labels string.  Locations are computed
     * relative to the viewR rectangle.
     * This layoutCompoundLabel() does not know how to handle LEADING/TRAILING
     * values in horizontalTextPosition (they will default to RIGHT) and in
     * horizontalAlignment (they will default to CENTER).
     * Use the other version of layoutCompoundLabel() instead.
     */
    private static String layoutCompoundLabelImpl(
	JComponent c,        
	FontMetrics fm,
        String text,
        Icon icon,
        int verticalAlignment,
        int horizontalAlignment,
        int verticalTextPosition,
        int horizontalTextPosition,
        Rectangle viewR,
        Rectangle iconR,
        Rectangle textR,
        int textIconGap)
    {
        /* Initialize the icon bounds rectangle iconR.
         */

        if (icon != null) {
            iconR.width = icon.getIconWidth();
            iconR.height = icon.getIconHeight();
        }
        else {
            iconR.width = iconR.height = 0;
        }

        /* Initialize the text bounds rectangle textR.  If a null
         * or and empty String was specified we substitute "" here
         * and use 0,0,0,0 for textR.
         */

        boolean textIsEmpty = (text == null) || text.equals("");

	View v = null;
        if (textIsEmpty) {
            textR.width = textR.height = 0;
            text = "";
        }
        else {
	    v = (c != null) ? (View) c.getClientProperty("html") : null;
	    if (v != null) {
		textR.width = (int) v.getPreferredSpan(View.X_AXIS);
		textR.height = (int) v.getPreferredSpan(View.Y_AXIS);
	    } else {
		textR.width = computeStringWidth(fm,text);
		textR.height = fm.getHeight();
	    }
        }

        /* Unless both text and icon are non-null, we effectively ignore
         * the value of textIconGap.  The code that follows uses the
         * value of gap instead of textIconGap.
         */

        int gap = (textIsEmpty || (icon == null)) ? 0 : textIconGap;

        if (!textIsEmpty) {

            /* If the label text string is too wide to fit within the available
             * space "..." and as many characters as will fit will be
             * displayed instead.
             */

            int availTextWidth;

            if (horizontalTextPosition == CENTER) {
                availTextWidth = viewR.width;
            }
            else {
                availTextWidth = viewR.width - (iconR.width + gap);
            }


            if (textR.width > availTextWidth) {
		if (v != null) {
		    textR.width = availTextWidth;
		} else {
		    String clipString = "...";
		    int totalWidth = computeStringWidth(fm,clipString);
		    int nChars;
		    for(nChars = 0; nChars < text.length(); nChars++) {
			totalWidth += fm.charWidth(text.charAt(nChars));
			if (totalWidth > availTextWidth) {
			    break;
			}
		    }
		    text = text.substring(0, nChars) + clipString;
		    textR.width = computeStringWidth(fm,text);
		}
            }
        }


        /* Compute textR.x,y given the verticalTextPosition and
         * horizontalTextPosition properties
         */

        if (verticalTextPosition == TOP) {
            if (horizontalTextPosition != CENTER) {
                textR.y = 0;
            }
            else {
                textR.y = -(textR.height + gap);
            }
        }
        else if (verticalTextPosition == CENTER) {
            textR.y = (iconR.height / 2) - (textR.height / 2);
        }
        else { // (verticalTextPosition == BOTTOM)
            if (horizontalTextPosition != CENTER) {
                textR.y = iconR.height - textR.height;
            }
            else {
                textR.y = (iconR.height + gap);
            }
        }

        if (horizontalTextPosition == LEFT) {
            textR.x = -(textR.width + gap);
        }
        else if (horizontalTextPosition == CENTER) {
            textR.x = (iconR.width / 2) - (textR.width / 2);
        }
        else { // (horizontalTextPosition == RIGHT)
            textR.x = (iconR.width + gap);
        }

        /* labelR is the rectangle that contains iconR and textR.
         * Move it to its proper position given the labelAlignment
         * properties.
         *
         * To avoid actually allocating a Rectangle, Rectangle.union
         * has been inlined below.
         */
        int labelR_x = Math.min(iconR.x, textR.x);
        int labelR_width = Math.max(iconR.x + iconR.width,
                                    textR.x + textR.width) - labelR_x;
        int labelR_y = Math.min(iconR.y, textR.y);
        int labelR_height = Math.max(iconR.y + iconR.height,
                                     textR.y + textR.height) - labelR_y;

        int dx, dy;

        if (verticalAlignment == TOP) {
            dy = viewR.y - labelR_y;
        }
        else if (verticalAlignment == CENTER) {
            dy = (viewR.y + (viewR.height / 2)) - (labelR_y + (labelR_height / 2));
        }
        else { // (verticalAlignment == BOTTOM)
            dy = (viewR.y + viewR.height) - (labelR_y + labelR_height);
        }

        if (horizontalAlignment == LEFT) {
            dx = viewR.x - labelR_x;
        }
        else if (horizontalAlignment == RIGHT) {
            dx = (viewR.x + viewR.width) - (labelR_x + labelR_width);
        }
        else { // (horizontalAlignment == CENTER)
            dx = (viewR.x + (viewR.width / 2)) -
                 (labelR_x + (labelR_width / 2));
        }

        /* Translate textR and glypyR by dx,dy.
         */

        textR.x += dx;
        textR.y += dy;

        iconR.x += dx;
        iconR.y += dy;

        return text;
    }


    /**
     * Paint a component c on an abitrary graphics g in the
     * specified rectangle, specifying the rectangle's upper left corner
     * and size.  The component is reparented to a private
     * container (whose parent becomes p) which prevents c.validate() and
     * and c.repaint() calls from propogating up the tree.  The intermediate
     * container has no other effect.
     *
     * @param g  the Graphics object to draw on
     * @param c  the Component to draw
     * @param p  the intermedate Container
     * @param x  an int specifying the left side of the area draw in, in pixels,
     *           measured from the left edge of the graphics context
     * @param y  an int specifying the top of the area to draw in, in pixels
     *           measured down from the top edge of the graphics context
     * @param w  an int specifying the width of the area draw in, in pixels
     * @param h  an int specifying the height of the area draw in, in pixels
     */
    public static void paintComponent(Graphics g, Component c, Container p, int x, int y, int w, int h) {
        getCellRendererPane(c, p).paintComponent(g, c, p, x, y, w, h,false);
    }

    /**
     * Paint a component c on an abitrary graphics g in the
     * specified rectangle, specifying a Rectangle object.  The component is reparented to a private
     * container (whose parent becomes p) which prevents c.validate() and
     * and c.repaint() calls from propogating up the tree.  The intermediate
     * container has no other effect.
     *
     * @param g  the Graphics object to draw on
     * @param c  the Component to draw
     * @param p  the intermedate Container
     * @param r  the Rectangle to draw in
     */
    public static void paintComponent(Graphics g, Component c, Container p, Rectangle r) {
        paintComponent(g, c, p, r.x, r.y, r.width, r.height);
    }


    /*
     * Ensure that cell renderer c has a ComponentShell parent and that
     * the shells parent is p.
     */
    private static CellRendererPane getCellRendererPane(Component c, Container p) {
        Container shell = c.getParent();
        if (shell instanceof CellRendererPane) {
            if (shell.getParent() != p) {
                p.add(shell);
            }
        } else {
            shell = new CellRendererPane();
            shell.add(c);
            p.add(shell);
        }
        return (CellRendererPane)shell;
    }

    /**
     * A simple minded look and feel change: ask each node in the tree
     * to updateUI() -- that is, to initialize its UI property with the
     * current look and feel.
     */
    public static void updateComponentTreeUI(Component c) {
        updateComponentTreeUI0(c);
        c.invalidate();
        c.validate();
        c.repaint();
    }

    private static void updateComponentTreeUI0(Component c) {
        if (c instanceof JComponent) {
            ((JComponent) c).updateUI();
        }
        Component[] children = null;
        if (c instanceof JMenu) {
            children = ((JMenu)c).getMenuComponents();
        }
        else if (c instanceof Container) {
            children = ((Container)c).getComponents();
        }
        if (children != null) {
            for(int i = 0; i < children.length; i++) {
                updateComponentTreeUI0(children[i]);
            }
        }
    }


    /**
     * Causes <i>doRun.run()</i> to be executed asynchronously on the
     * AWT event dispatching thread.  This will happen after all
     * pending AWT events have been processed.  This method should
     * be used when an application thread needs to update the GUI.
     * In the following example the <code>invokeLater</code> call queues
     * the <code>Runnable</code> object <code>doHelloWorld</code>
     * on the event dispatching thread and
     * then prints a message.
     * <pre>
     * Runnable doHelloWorld = new Runnable() {
     *     public void run() {
     *         System.out.println("Hello World on " + Thread.currentThread());
     *     }
     * };
     *
     * SwingUtilities.invokeLater(doHelloWorld);
     * System.out.println("This might well be displayed before the other message.");
     * </pre>
     * If invokeLater is called from the event dispatching thread --
     * for example, from a JButton's ActionListener -- the <i>doRun.run()</i> will
     * still be deferred until all pending events have been processed.
     * Note that if the <i>doRun.run()</i> throws an uncaught exception
     * the event dispatching thread will unwind (not the current thread).
     * <p>
     * Additional documentation and examples for this method can be
     * found in
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How to Use Threads</a>,
     * in <em>The Java Tutorial</em>.
     * <p>
     * As of 1.3 this method is just a cover for <code>java.awt.EventQueue.invokeLater()</code>.
     * 
     * @see #invokeAndWait
     */
    public static void invokeLater(Runnable doRun) {
	EventQueue.invokeLater(doRun);
    }


    /**
     * Causes <i>doRun.run()</i> to be executed synchronously on the
     * AWT event dispatching thread.  This call will block until
     * all pending AWT events have been processed and (then)
     * <i>doRun.run()</i> returns. This method should
     * be used when an application thread needs to update the GUI.
     * It should not be called from the EventDispatchThread.
     * Here's an example that creates a new application thread
     * that uses invokeAndWait() to print a string from the event
     * dispatching thread and then, when that's finished, print
     * a string from the application thread.
     * <pre>
     * final Runnable doHelloWorld = new Runnable() {
     *     public void run() {
     *         System.out.println("Hello World on " + Thread.currentThread());
     *     }
     * };
     *
     * Thread appThread = new Thread() {
     *     public void run() {
     *         try {
     *             SwingUtilities.invokeAndWait(doHelloWorld);
     *         }
     *         catch (Exception e) {
     *             e.printStackTrace();
     *         }
     *         System.out.println("Finished on " + Thread.currentThread());
     *     }
     * };
     * appThread.start();
     * </pre>
     * Note that if the Runnable.run() method throws an uncaught exception
     * (on the event dispatching thread) it's caught and rethrown, as
     * an InvocationTargetException, on the callers thread.
     * <p>
     * Additional documentation and examples for this method can be
     * found in
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How to Use Threads</a>,
     * in <em>The Java Tutorial</em>.
     * <p>
     * As of 1.3 this method is just a cover for <code>java.awt.EventQueue.invokeAndWait()</code>.
     *
     * @exception  InterruptedException if we're interrupted while waiting for
     *             the event dispatching thread to finish excecuting <i>doRun.run()</i>
     * @exception  InvocationTargetException  if <i>doRun.run()</i> throws
     *
     * @see #invokeLater
     */
    public static void invokeAndWait(final Runnable doRun)
        throws InterruptedException, InvocationTargetException
    {
	EventQueue.invokeAndWait(doRun);
    }

    /**
     * Returns true if the current thread is an AWT event dispatching thread.
     * <p>
     * As of 1.3 this method is just a cover for 
     * <code>java.awt.EventQueue.isEventDispatchThread()</code>.
     * 
     * @return true if the current thread is an AWT event dispatching thread
     */
    public static boolean isEventDispatchThread()
    {
	  return EventQueue.isDispatchThread();

    }


    /*
     * --- Accessibility Support ---
     *
     */

    /**
     * Get the index of this object in its accessible parent.<p>
     *
     * Note: as of the Java 2 platform v1.3, it is recommended that developers call
     * Component.AccessibleAWTComponent.getAccessibleIndexInParent() instead
     * of using this method.
     *
     * @return -1 of this object does not have an accessible parent.
     * Otherwise, the index of the child in its accessible parent.
     */
    public static int getAccessibleIndexInParent(Component c) {
	return c.getAccessibleContext().getAccessibleIndexInParent();
    }

    /**
     * Returns the Accessible child contained at the local coordinate
     * Point, if one exists.
     *
     * @return the Accessible at the specified location, if it exists
     */
    public static Accessible getAccessibleAt(Component c, Point p) {
        if (c instanceof Container) {
	    return c.getAccessibleContext().getAccessibleComponent().getAccessibleAt(p);
	} else if (c instanceof Accessible) {
            Accessible a = (Accessible) c;
            if (a != null) {
                AccessibleContext ac = a.getAccessibleContext();
                if (ac != null) {
                    AccessibleComponent acmp;
                    Point location;
                    int nchildren = ac.getAccessibleChildrenCount();
                    for (int i=0; i < nchildren; i++) {
                        a = ac.getAccessibleChild(i);
                        if ((a != null)) {
                            ac = a.getAccessibleContext();
                            if (ac != null) {
                                acmp = ac.getAccessibleComponent();
                                if ((acmp != null) && (acmp.isShowing())) {
                                    location = acmp.getLocation();
                                    Point np = new Point(p.x-location.x,
                                                         p.y-location.y);
                                    if (acmp.contains(np)){
                                        return a;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return (Accessible) c;
        }
        return null;
    }

    /**
     * Get the state of this object. <p>
     *
     * Note: as of the Java 2 platform v1.3, it is recommended that developers call
     * Component.AccessibleAWTComponent.getAccessibleIndexInParent() instead
     * of using this method.
     *
     * @return an instance of AccessibleStateSet containing the current state
     * set of the object
     * @see AccessibleState
     */
    public static AccessibleStateSet getAccessibleStateSet(Component c) {
	return c.getAccessibleContext().getAccessibleStateSet();
    }

    /**
     * Returns the number of accessible children in the object.  If all
     * of the children of this object implement Accessible, than this
     * method should return the number of children of this object. <p>
     *
     * Note: as of the Java 2 platform v1.3, it is recommended that developers call
     * Component.AccessibleAWTComponent.getAccessibleIndexInParent() instead
     * of using this method.
     *
     * @return the number of accessible children in the object.
     */
    public static int getAccessibleChildrenCount(Component c) {
	return c.getAccessibleContext().getAccessibleChildrenCount();
    }

    /**
     * Return the nth Accessible child of the object. <p>
     *
     * Note: as of the Java 2 platform v1.3, it is recommended that developers call
     * Component.AccessibleAWTComponent.getAccessibleIndexInParent() instead
     * of using this method.
     *
     * @param i zero-based index of child
     * @return the nth Accessible child of the object
     */
    public static Accessible getAccessibleChild(Component c, int i) {
	return c.getAccessibleContext().getAccessibleChild(i);
    }

    /**
     * Return the child component which has focus, if any.  The HotJava
     * SecurityManager forbids applet access to getFocusOwner(), so if the
     * component is an applet, we check whether a JComponent has focus.
     * Non-Swing components in an applet on HotJava are out-of-luck,
     * unfortunately.
     */
    public static Component findFocusOwner(Component c) {
        if (c instanceof Window) {
            return ((Window)c).getFocusOwner();
        }

        if (c instanceof JComponent && ((JComponent)c).hasFocus()) {
            return c;
        }
        if (c instanceof Container) {
            int n = ((Container)c).getComponentCount();
            for (int i = 0; i < n; i++) {
                Component focusOwner =
                    findFocusOwner(((Container)c).getComponent(i));
                if (focusOwner != null) {
                    return focusOwner;
                }
            }
            return null;
        } else {
            return null;  // Component doesn't have hasFocus().
        }
    }

    /**
     * If c is a JRootPane descendant return its JRootPane ancestor.
     * If c is a RootPaneContainer then return its JRootPane.
     * @return the JRootPane for Component c or null.
     */
    public static JRootPane getRootPane(Component c) {
        if (c instanceof RootPaneContainer) {
            return ((RootPaneContainer)c).getRootPane();
        }
        for( ; c != null; c = c.getParent()) {
            if (c instanceof JRootPane) {
                return (JRootPane)c;
            }
        }
        return null;
    }


    /**
     * Returns the root component for the current component tree.
     * @return the first ancestor of c that's a Window or the last Applet ancestor
     */
    public static Component getRoot(Component c) {
        Component applet = null;
        for(Component p = c; p != null; p = p.getParent()) {
            if (p instanceof Window) {
                return p;
            }
            if (p instanceof Applet) {
                applet = p;
            }
        }
        return applet;
    }

    /**
     * Invokes <code>actionPerformed</code> on <code>action</code> if
     * <code>action</code> is enabled (and non null). The command for the
     * ActionEvent is determined by:
     * <ol>
     *   <li>If the action was registered via
     *       <code>registerKeyboardAction</code>, then the command string
     *       passed in (null will be used if null was passed in).
     *   <li>Action value with name Action.ACTION_COMMAND_KEY, unless null.
     *   <li>String value of the KeyEvent, unless <code>getKeyChar</code>
     *       returns KeyEvent.CHAR_UNDEFINED..
     * </ol>
     * This will return true if <code>action</code> is non-null and
     * actionPerformed is invoked on it.
     *
     * @since 1.3
     */
    public static boolean notifyAction(Action action, KeyStroke ks,
				       KeyEvent event, Object sender,
				       int modifiers) {
	if (action == null || !action.isEnabled()) {
	    return false;
	}
	Object commandO;
	boolean stayNull;

	// Get the command object.
	commandO = action.getValue(Action.ACTION_COMMAND_KEY);
	if (commandO == null && (action instanceof JComponent.ActionStandin)) {
	    // ActionStandin is used for historical reasons to support
	    // registerKeyboardAction with a null value.
	    stayNull = true;
	}
	else {
	    stayNull = false;
	}

	// Convert it to a string.
	String command;

	if (commandO != null) {
	    command = commandO.toString();
	}
	else if (!stayNull && event.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
	    command = String.valueOf(event.getKeyChar());
	}
	else {
	    // Do null for undefined chars, or if registerKeyboardAction
	    // was called with a null.
	    command = null;
	}
	action.actionPerformed(new ActionEvent(sender,
			ActionEvent.ACTION_PERFORMED, command, modifiers));
	return true;
    }


    /**
     * Convenience method to change the UI InputMap for <code>component</code>
     * to <code>uiInputMap</code>. If <code>uiInputMap</code> is null,
     * this removes any previously installed UI InputMap.
     *
     * @since 1.3
     */
    public static void replaceUIInputMap(JComponent component, int type,
					 InputMap uiInputMap) {
	InputMap map = component.getInputMap(type);

	while (map != null) {
	    InputMap parent = map.getParent();
	    if (parent == null || (parent instanceof UIResource)) {
		map.setParent(uiInputMap);
		return;
	    }
	    map = parent;
	}
    }


    /**
     * Convenience method to change the UI ActionMap for <code>component</code>
     * to <code>uiActionMap</code>. If <code>uiActionMap</code> is null,
     * this removes any previously installed UI ActionMap.
     *
     * @since 1.3
     */
    public static void replaceUIActionMap(JComponent component,
					  ActionMap uiActionMap) {
	ActionMap map = component.getActionMap();

	while (map != null) {
	    ActionMap parent = map.getParent();
	    if (parent == null || (parent instanceof UIResource)) {
		map.setParent(uiActionMap);
		return;
	    }
	    map = parent;
	}
    }


    /**
     * Returns the InputMap provided by the UI for condition
     * <code>condition</code> in component <code>component</code>.
     * <p>This will return null if the UI has not installed a InputMap
     * of the specified type.
     *
     * @since 1.3
     */
    public static InputMap getUIInputMap(JComponent component, int condition) {
	InputMap map = component.getInputMap(condition, false);
	while (map != null) {
	    InputMap parent = map.getParent();
	    if (parent instanceof UIResource) {
		return parent;
	    }
	    map = parent;
	}
	return null;
    }

    /**
     * Returns the ActionMap provided by the UI 
     * in component <code>component</code>.
     * <p>This will return null if the UI has not installed an ActionMap.
     *
     * @since 1.3
     */
    public static ActionMap getUIActionMap(JComponent component) {
	ActionMap map = component.getActionMap(false);
	while (map != null) {
	    ActionMap parent = map.getParent();
	    if (parent instanceof UIResource) {
		return parent;
	    }
	    map = parent;
	}
	return null;
    }


    // Don't use String, as it's not guaranteed to be unique in a Hashtable.
    private static final Object sharedOwnerFrameKey =
       new StringBuffer("SwingUtilities.sharedOwnerFrame");

    /**
     * Returns a toolkit-private, shared, invisible Frame
     * to be the owner for JDialogs and JWindows created with
     * null owners.
     */
    static Frame getSharedOwnerFrame() {
        Frame sharedOwnerFrame =
            (Frame)SwingUtilities.appContextGet(sharedOwnerFrameKey);
        if (sharedOwnerFrame == null) {
            sharedOwnerFrame = new Frame() {
                public void show() {
                    // This frame can never be shown
                }
                public synchronized void dispose() {
                    try {
                        getToolkit().getSystemEventQueue();
                        super.dispose();
                    } catch (Exception e) {
                        // untrusted code not allowed to dispose
                    }
                }
            };
            SwingUtilities.appContextPut(sharedOwnerFrameKey,
                                         sharedOwnerFrame);
        }
        return sharedOwnerFrame;
    }

    /* Don't make these AppContext accessors public or protected --
     * since AppContext is in sun.awt in 1.2, we shouldn't expose it
     * even indirectly with a public API.
     */

    static Object appContextGet(Object key) {
        return sun.awt.AppContext.getAppContext().get(key);
    }

    static void appContextPut(Object key, Object value) {
        sun.awt.AppContext.getAppContext().put(key, value);
    }

    static void appContextRemove(Object key) {
        sun.awt.AppContext.getAppContext().remove(key);
    }


    static Class loadSystemClass(String className) throws ClassNotFoundException {
	return Class.forName(className, true, ClassLoader.getSystemClassLoader());
    }


    final static void doPrivileged(final Runnable doRun) {
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                  doRun.run();
                  return null;
                }
            }
        );
    }


   /*
     * Convenience function for determining ComponentOrientation.  Helps us
     * avoid having Munge directives throughout the code.
     */
    static boolean isLeftToRight( Component c ) {
        return c.getComponentOrientation().isLeftToRight();
    }
    private SwingUtilities() {
        throw new Error("SwingUtilities is just a container for static methods");
    }

    /**
     * Returns true if the Icon <code>icon</code> is an instance of
     * ImageIcon, and the image it contains is the same as <code>image</code>.
     */
    static boolean doesIconReferenceImage(Icon icon, Image image) {
	Image iconImage = (icon != null && (icon instanceof ImageIcon)) ?
	                   ((ImageIcon)icon).getImage() : null;
	return (iconImage == image);
    }
}
