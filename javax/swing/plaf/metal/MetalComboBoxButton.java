/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.plaf.metal;

import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.border.*;
import java.io.Serializable;

/**
 * JButton subclass to help out MetalComboBoxUI
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @see MetalComboBoxButton
 * @version 1.28 02/06/02
 * @author Tom Santos
 */
public class MetalComboBoxButton extends JButton {
    protected JComboBox comboBox;
    protected JList listBox;
    protected CellRendererPane rendererPane;
    protected Icon comboIcon;
    protected boolean iconOnly = false;

    public final JComboBox getComboBox() { return comboBox;}
    public final void setComboBox( JComboBox cb ) { comboBox = cb;}

    public final Icon getComboIcon() { return comboIcon;}
    public final void setComboIcon( Icon i ) { comboIcon = i;}

    public final boolean isIconOnly() { return iconOnly;}
    public final void setIconOnly( boolean isIconOnly ) { iconOnly = isIconOnly;}

    MetalComboBoxButton() {
        super( "" );
        DefaultButtonModel model = new DefaultButtonModel() {
            public void setArmed( boolean armed ) {
                super.setArmed( isPressed() ? true : armed );
            }
        };

        setModel( model );
    }

    public MetalComboBoxButton( JComboBox cb, Icon i, 
                                CellRendererPane pane, JList list ) {
        this();
        comboBox = cb;
        comboIcon = i;
        rendererPane = pane;
        listBox = list;
        setEnabled( comboBox.isEnabled() );
        setRequestFocusEnabled( comboBox.isEnabled() );
    }

    public MetalComboBoxButton( JComboBox cb, Icon i, boolean onlyIcon,
                                CellRendererPane pane, JList list ) {
        this( cb, i, pane, list );
        iconOnly = onlyIcon;
    }

    public boolean isFocusTraversable() {
       return (!comboBox.isEditable()) && comboBox.isEnabled();
    }

    public void paintComponent( Graphics g ) {

        boolean leftToRight = MetalUtils.isLeftToRight(comboBox);

        // Paint the button as usual
        super.paintComponent( g );

        Insets insets = getInsets();

        int width = getWidth() - (insets.left + insets.right);
        int height = getHeight() - (insets.top + insets.bottom);

        if ( height <= 0 || width <= 0 ) {
            return;
        }

        int left = insets.left;
        int top = insets.top;
        int right = left + (width - 1);
        int bottom = top + (height - 1);

        int iconWidth = 0;
        int iconLeft = (leftToRight) ? right : left;

        // Paint the icon
        if ( comboIcon != null ) {
            iconWidth = comboIcon.getIconWidth();
            int iconHeight = comboIcon.getIconHeight();
            int iconTop = 0;

            if ( iconOnly ) {
                iconLeft = (getWidth() / 2) - (iconWidth / 2);
                iconTop = (getHeight() / 2) - (iconHeight / 2);
            }
            else {
	        if (leftToRight) {
		    iconLeft = (left + (width - 1)) - iconWidth;
		}
		else {
		    iconLeft = left;
		}
                iconTop = (top + ((bottom - top) / 2)) - (iconHeight / 2);
            }

            comboIcon.paintIcon( this, g, iconLeft, iconTop );

            // Paint the focus
            if ( hasFocus() ) {
                g.setColor( MetalLookAndFeel.getFocusColor() );
                g.drawRect( left - 1, top - 1, width + 3, height + 1 );
            }
        }

        // Let the renderer paint
        if ( ! iconOnly && comboBox != null ) {
            ListCellRenderer renderer = comboBox.getRenderer();
            Component c;
            boolean renderPressed = getModel().isPressed();
            c = renderer.getListCellRendererComponent(listBox,
                                                      comboBox.getSelectedItem(),
                                                      -1,
                                                      renderPressed,
                                                      false);
            c.setFont(rendererPane.getFont());

            if ( model.isArmed() && model.isPressed() ) {
                if ( isOpaque() ) {
                    c.setBackground(UIManager.getColor("Button.select"));
                }
                c.setForeground(comboBox.getForeground());
            }
            else if ( !comboBox.isEnabled() ) {
                if ( isOpaque() ) {
                    c.setBackground(UIManager.getColor("ComboBox.disabledBackground"));
                }
                c.setForeground(UIManager.getColor("ComboBox.disabledForeground"));
            }
            else {
                c.setForeground(comboBox.getForeground());
                c.setBackground(comboBox.getBackground());
            }


            int cWidth = width - (insets.right + iconWidth);
	    if (leftToRight) {
	        rendererPane.paintComponent( g, c, this, 
					     left, top, cWidth, height );
	    }
	    else {
	        rendererPane.paintComponent( g, c, this, 
					     left + iconWidth, top, cWidth, height );
	    }
        }
    }
}
