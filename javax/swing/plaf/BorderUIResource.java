/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.plaf;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.border.*;
import javax.swing.Icon;
import javax.swing.plaf.UIResource;


/*
 * A Border wrapper class which implements UIResource.  UI
 * classes which set border properties should use this class
 * to wrap any borders specified as defaults.
 *
 * This class delegates all method invocations to the
 * Border "delegate" object specified at construction.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @see javax.swing.plaf.UIResource
 * @version 1.13 02/06/02
 * @author Amy Fowler
 * 
 */
public class BorderUIResource implements Border, UIResource, Serializable
{
    static Border etched;
    static Border loweredBevel;
    static Border raisedBevel;
    static Border blackLine;

    public static Border getEtchedBorderUIResource() {
        if (etched == null) {
            etched = new EtchedBorderUIResource();
        }
        return etched;
    }

    public static Border getLoweredBevelBorderUIResource() {
        if (loweredBevel == null) {
            loweredBevel = new BevelBorderUIResource(BevelBorder.LOWERED);
        }
        return loweredBevel;
    }

    public static Border getRaisedBevelBorderUIResource() {
        if (raisedBevel == null) {
            raisedBevel = new BevelBorderUIResource(BevelBorder.RAISED);
        }
        return raisedBevel;
    }

    public static Border getBlackLineBorderUIResource() {
        if (blackLine == null) {
            blackLine = new LineBorderUIResource(Color.black);
        }
        return blackLine;
    }

    private Border delegate;

    /**
     * Creates a UIResource border object which wraps
     * an existing Border instance.
     * @param delegate the border being wrapped
     */
    public BorderUIResource(Border delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("null border delegate argument");
        }
        this.delegate = delegate;
    }

    public void paintBorder(Component c, Graphics g, int x, int y,
                            int width, int height) {        
        delegate.paintBorder(c, g, x, y, width, height);
    }

    public Insets getBorderInsets(Component c)       {
        return delegate.getBorderInsets(c);
    }

    public boolean isBorderOpaque() { 
        return delegate.isBorderOpaque();
    }

    public static class CompoundBorderUIResource extends CompoundBorder implements UIResource {
        public CompoundBorderUIResource(Border outsideBorder, Border insideBorder) {
            super(outsideBorder, insideBorder);
        }

    }

    public static class EmptyBorderUIResource extends EmptyBorder implements UIResource {

        public EmptyBorderUIResource(int top, int left, int bottom, int right)   {
            super(top, left, bottom, right);
        }
        public EmptyBorderUIResource(Insets insets) {
            super(insets);
        }
    }

    public static class LineBorderUIResource extends LineBorder implements UIResource {

        public LineBorderUIResource(Color color) {
            super(color);
        }

        public LineBorderUIResource(Color color, int thickness)  {
            super(color, thickness);
        }
    }


    public static class BevelBorderUIResource extends BevelBorder implements UIResource {

        public BevelBorderUIResource(int bevelType) {
            super(bevelType);
        }

        public BevelBorderUIResource(int bevelType, Color highlight, Color shadow) {
            super(bevelType, highlight, shadow);
        }

        public BevelBorderUIResource(int bevelType, 
                                     Color highlightOuter, Color highlightInner,
                                     Color shadowOuter, Color shadowInner) {
            super(bevelType, highlightOuter, highlightInner, shadowOuter, shadowInner);
        }
    }

    public static class EtchedBorderUIResource extends EtchedBorder implements UIResource {

        public EtchedBorderUIResource()    {
            super();
        }

        public EtchedBorderUIResource(int etchType)    {
            super(etchType);
        }

        public EtchedBorderUIResource(Color highlight, Color shadow)    {
            super(highlight, shadow);
        }

        public EtchedBorderUIResource(int etchType, Color highlight, Color shadow)    {
            super(etchType, highlight, shadow);
        }
    }

    public static class MatteBorderUIResource extends MatteBorder implements UIResource {

        public MatteBorderUIResource(int top, int left, int bottom, int right, 
                                     Color color)   {
            super(top, left, bottom, right, color);
        }

        public MatteBorderUIResource(int top, int left, int bottom, int right, 
                                     Icon tileIcon)   {
            super(top, left, bottom, right, tileIcon);
        }

        public MatteBorderUIResource(Icon tileIcon)   {
            super(tileIcon);
        }
    }

    public static class TitledBorderUIResource extends TitledBorder implements UIResource {

        public TitledBorderUIResource(String title)     {
            super(title);
        }

        public TitledBorderUIResource(Border border)       {
            super(border);
        }
        
        public TitledBorderUIResource(Border border, String title) {
            super(border, title);
        }

        public TitledBorderUIResource(Border border, 
                        String title,
                        int titleJustification,
                        int titlePosition)      {
            super(border, title, titleJustification, titlePosition);
        }

        public TitledBorderUIResource(Border border, 			
                        String title,
                        int titleJustification,
                        int titlePosition,
                        Font titleFont) {
            super(border, title, titleJustification, titlePosition, titleFont);
        }

        public TitledBorderUIResource(Border border,                     
                        String title,
                        int titleJustification,
                        int titlePosition,
                        Font titleFont,
                        Color titleColor)       {
            super(border, title, titleJustification, titlePosition, titleFont, titleColor);
        }
    }
 
}
