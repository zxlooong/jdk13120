/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import javax.swing.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;



/*
 * @version 1.44 02/11/99
 * @author Hans Muller
 */

public class BasicGraphicsUtils
{

    private static final Insets GROOVE_INSETS = new Insets(2, 2, 2, 2);
    private static final Insets ETCHED_INSETS = new Insets(2, 2, 2, 2);

    public static void drawEtchedRect(Graphics g, int x, int y, int w, int h,
                                      Color shadow, Color darkShadow,
                                      Color highlight, Color lightHighlight)
    {
        Color oldColor = g.getColor();  // Make no net change to g
        g.translate(x, y);

        g.setColor(shadow);
        g.drawLine(0, 0, w-1, 0);      // outer border, top
        g.drawLine(0, 1, 0, h-2);      // outer border, left

        g.setColor(darkShadow);
        g.drawLine(1, 1, w-3, 1);      // inner border, top
        g.drawLine(1, 2, 1, h-3);      // inner border, left

        g.setColor(lightHighlight);
        g.drawLine(w-1, 0, w-1, h-1);  // outer border, bottom
        g.drawLine(0, h-1, w-1, h-1);  // outer border, right

        g.setColor(highlight);
        g.drawLine(w-2, 1, w-2, h-3);  // inner border, right
        g.drawLine(1, h-2, w-2, h-2);  // inner border, bottom

        g.translate(-x, -y);
        g.setColor(oldColor);
    }


    /**
     * Returns the amount of space taken up by a border drawn by
     * <code>drawEtchedRect()</code>
     *
     * @return  the inset of an etched rect
     */
    public static Insets getEtchedInsets() {
        return ETCHED_INSETS;
    }


    public static void drawGroove(Graphics g, int x, int y, int w, int h,
                                  Color shadow, Color highlight)
    {
        Color oldColor = g.getColor();  // Make no net change to g
        g.translate(x, y);

        g.setColor(shadow);
        g.drawRect(0, 0, w-2, h-2);

        g.setColor(highlight);
        g.drawLine(1, h-3, 1, 1);
        g.drawLine(1, 1, w-3, 1);

        g.drawLine(0, h-1, w-1, h-1);
        g.drawLine(w-1, h-1, w-1, 0);

        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    /**
     * Returns the amount of space taken up by a border drawn by
     * <code>drawGroove()</code>
     *
     * @return  the inset of a groove border
     */
    public static Insets getGrooveInsets() {
        return GROOVE_INSETS;
    }


    public static void drawBezel(Graphics g, int x, int y, int w, int h, 
                                 boolean isPressed, boolean isDefault, 
                                 Color shadow, Color darkShadow, 
                                 Color highlight, Color lightHighlight)
    {
        Color oldColor = g.getColor();  // Make no net change to g
        g.translate(x, y);

        if (isPressed) {
            if (isDefault) {
                g.setColor(darkShadow);          // outer border
                g.drawRect(0, 0, w-1, h-1);
            }

            g.setColor(shadow);         // inner border
            g.drawRect(1, 1, w-3, h-3);

        }
        else {
            if (isDefault) {
                g.setColor(darkShadow);       
                g.drawRect(0, 0, w-1, h-1);

                g.setColor(lightHighlight);   
                g.drawLine(1, 1, 1, h-3);
                g.drawLine(2, 1, w-3, 1);

                g.setColor(highlight);
                g.drawLine(2, 2, 2, h-4);
                g.drawLine(3, 2, w-4, 2);

                g.setColor(shadow);
                g.drawLine(2, h-3, w-3, h-3);
                g.drawLine(w-3, 2, w-3, h-4);

                g.setColor(darkShadow);        
                g.drawLine(1, h-2, w-2, h-2);
                g.drawLine(w-2, h-2, w-2, 1);
            }
            else {
                g.setColor(lightHighlight);    
                g.drawLine(0, 0, 0, h-1);
                g.drawLine(1, 0, w-2, 0);

                g.setColor(highlight);
                g.drawLine(1, 1, 1, h-3);
                g.drawLine(2, 1, w-3, 1);

                g.setColor(shadow);
                g.drawLine(1, h-2, w-2, h-2);
                g.drawLine(w-2, 1, w-2, h-3);

                g.setColor(darkShadow);         
                g.drawLine(0, h-1, w-1, h-1);
                g.drawLine(w-1, h-1, w-1, 0);
            }

            g.translate(-x, -y);
            g.setColor(oldColor);
        }
    }

    public static void drawLoweredBezel(Graphics g, int x, int y, int w, int h,
                                        Color shadow, Color darkShadow, 
                                        Color highlight, Color lightHighlight)  {
        g.setColor(darkShadow);    
        g.drawLine(0, 0, 0, h-1);
        g.drawLine(1, 0, w-2, 0);
 
        g.setColor(shadow);
        g.drawLine(1, 1, 1, h-2);
        g.drawLine(1, 1, w-3, 1);
 
        g.setColor(lightHighlight);         
        g.drawLine(0, h-1, w-1, h-1);
        g.drawLine(w-1, h-1, w-1, 0);

        g.setColor(highlight);
        g.drawLine(1, h-2, w-2, h-2);
        g.drawLine(w-2, h-2, w-2, 1);
     }


    /** Draw a string with the graphics g at location (x,y) just like g.drawString() would.
     *  The first occurence of underlineChar in text will be underlined. The matching is
     *  not case sensitive.
     */
    public static void drawString(Graphics g,String text,int underlinedChar,int x,int y) {

    //        char b[] = new char[1];
      //      String s;
        char lc,uc;
        int index=-1,lci,uci;

        if(underlinedChar != '\0') {
          //           b[0] = (char)underlinedChar;
          //           s = new String(b).toUpperCase();
          //       uc = s.charAt(0);
          uc = Character.toUpperCase((char)underlinedChar);

          //            s = new String(b).toLowerCase();
            lc = Character.toLowerCase((char)underlinedChar);

            uci = text.indexOf(uc);
            lci = text.indexOf(lc);

            if(uci == -1)
                index = lci;
            else if(lci == -1)
                index = uci;
            else
                index = (lci < uci) ? lci : uci;
        }

        g.drawString(text,x,y);
        if(index != -1) {
            FontMetrics fm = g.getFontMetrics();
            //            Rectangle underlineRect = new Rectangle();
            
            int underlineRectX = x + fm.stringWidth(text.substring(0,index));
            int underlineRectY = y;
            int underlineRectWidth = fm.charWidth(text.charAt(index));
            int underlineRectHeight = 1;
            g.fillRect(underlineRectX, underlineRectY + fm.getDescent() - 1,
                       underlineRectWidth, underlineRectHeight);
        }
    }


    public static void drawDashedRect(Graphics g,int x,int y,int width,int height) {
        int vx,vy;

        // draw upper and lower horizontal dashes
        for (vx = x; vx < (x + width); vx+=2) {
            g.drawLine(vx, y, vx, y);
            g.drawLine(vx, y + height-1, vx, y + height-1);
        }

        // draw left and right vertical dashes
        for (vy = y; vy < (y + height); vy+=2) {
            g.drawLine(x, vy, x, vy);
            g.drawLine(x+width-1, vy, x + width-1, vy);
        }
    }

    public static Dimension getPreferredButtonSize(AbstractButton b, int textIconGap)
    {
        if(b.getComponentCount() > 0) {
            return null;
        }

        Icon icon = (Icon) b.getIcon();
        String text = b.getText();

        Font font = b.getFont();
        FontMetrics fm = b.getToolkit().getFontMetrics(font);
          
        Rectangle iconR = new Rectangle();
        Rectangle textR = new Rectangle();
        Rectangle viewR = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);

        SwingUtilities.layoutCompoundLabel(
            (JComponent) b, fm, text, icon,
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewR, iconR, textR, (text == null ? 0 : textIconGap)
        );

        /* The preferred size of the button is the size of 
         * the text and icon rectangles plus the buttons insets.
         */

        Rectangle r = iconR.union(textR);

        Insets insets = b.getInsets();
        r.width += insets.left + insets.right;
        r.height += insets.top + insets.bottom;

        /* Ensure that the width and height of the button is odd,
         * to allow for the focus line.
         */

        if(r.width % 2 == 0) { r.width += 1; }
        if(r.height % 2 == 0) { r.height += 1; }

        return r.getSize();
    }
    
    /*
     * Convenience function for determining ComponentOrientation.  Helps us
     * avoid having Munge directives throughout the code.
     */
    static boolean isLeftToRight( Component c ) {
        return c.getComponentOrientation().isLeftToRight();
    }
}
