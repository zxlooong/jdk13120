/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.plaf.metal;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.*;
import java.io.Serializable; 
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * The Metal subclass of BasicTabbedPaneUI.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.19 08/28/98
 * @author Tom Santos
 */

public class MetalTabbedPaneUI extends BasicTabbedPaneUI {

    protected int minTabWidth = 40;
    protected Color tabAreaBackground;
    protected Color selectColor;
    protected Color selectHighlight;

    public static ComponentUI createUI( JComponent x ) {
        return new MetalTabbedPaneUI();
    }  

    protected LayoutManager createLayoutManager() {
        return new TabbedPaneLayout();
    }  

    protected void installDefaults() {
        super.installDefaults();

        tabAreaBackground = UIManager.getColor("TabbedPane.tabAreaBackground");
        selectColor = UIManager.getColor("TabbedPane.selected");
        selectHighlight = UIManager.getColor("TabbedPane.selectHighlight");
    }


    protected void paintTabBorder( Graphics g, int tabPlacement,
                                   int tabIndex, int x, int y, int w, int h, 
                                   boolean isSelected) {
        int bottom = y + (h-1);
        int right = x + (w-1);

        switch ( tabPlacement ) {
        case LEFT:
            paintLeftTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
            break;
        case BOTTOM:
            paintBottomTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
            break;
        case RIGHT:
            paintRightTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
            break;
        case TOP:
        default:
            paintTopTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
        }
    }


    protected void paintTopTabBorder( int tabIndex, Graphics g, 
                                      int x, int y, int w, int h,
                                      int btm, int rght,
                                      boolean isSelected ) {
        int currentRun = getRunForTab( tabPane.getTabCount(), tabIndex );
        int lastIndex = lastTabInRun( tabPane.getTabCount(), currentRun );
        int firstIndex = tabRuns[ currentRun ];
	boolean leftToRight = MetalUtils.isLeftToRight(tabPane);
        int bottom = h - 1;
        int right = w - 1;

        //
        // Paint Gap
        //

        if ( shouldFillGap( currentRun, tabIndex, x, y ) ) {
            g.translate( x, y );

	    if ( leftToRight ) {
	        g.setColor( getColorForGap( currentRun, x, y + 1 ) );
		g.fillRect( 1, 0, 5, 3 );
		g.fillRect( 1, 3, 2, 2 );
	    } else {
	        g.setColor( getColorForGap( currentRun, x + w - 1, y + 1 ) );
		g.fillRect( right - 5, 0, 5, 3 );
		g.fillRect( right - 2, 3, 2, 2 );
	    }

            g.translate( -x, -y );
        }

        g.translate( x, y );

        //
        // Paint Border
        //

        g.setColor( darkShadow );

	if ( leftToRight ) {

	    // Paint slant
	    g.drawLine( 1, 5, 6, 0 );

	    // Paint top
	    g.drawLine( 6, 0, right, 0 );

	    // Paint right
	    if ( tabIndex==lastIndex ) {
	        // last tab in run
	        g.drawLine( right, 1, right, bottom );
	    }

	    // Paint left
	    if ( tabIndex != tabRuns[ runCount - 1 ] ) {
	        // not the first tab in the last run
	        g.drawLine( 0, 0, 0, bottom );
	    } else {
	        // the first tab in the last run
	        g.drawLine( 0, 6, 0, bottom );
	    }
	} else {

	    // Paint slant
	    g.drawLine( right - 1, 5, right - 6, 0 );

	    // Paint top
	    g.drawLine( right - 6, 0, 0, 0 );

	    // Paint right
	    if ( tabIndex != tabRuns[ runCount - 1 ] ) {
	        // not the first tab in the last run
	        g.drawLine( right, 0, right, bottom );
	    } else {
	        // the first tab in the last run
	        g.drawLine( right, 6, right, bottom );
	    }

	    // Paint left
	    if ( tabIndex==lastIndex ) {
	        // last tab in run
	        g.drawLine( 0, 1, 0, bottom );
	    }
	}

        //
        // Paint Highlight
        //

        g.setColor( isSelected ? selectHighlight : highlight );

	if ( leftToRight ) {

	    // Paint slant
	    g.drawLine( 1, 6, 6, 1 );

	    // Paint top
	    g.drawLine( 6, 1, right, 1 );

	    // Paint left
	    g.drawLine( 1, 6, 1, bottom );

	    // paint highlight in the gap on tab behind this one
	    // on the left end (where they all line up)
	    if ( tabIndex==firstIndex && tabIndex!=tabRuns[runCount - 1] ) {
	        //  first tab in run but not first tab in last run
	        if (tabPane.getSelectedIndex()==tabRuns[currentRun+1]) {
		    // tab in front of selected tab
		    g.setColor( selectHighlight );
		}
		else {
		    // tab in front of normal tab
		    g.setColor( highlight );
		}
		g.drawLine( 1, 0, 1, 4 );
	    }
	} else {

	    // Paint slant
	    g.drawLine( right - 1, 6, right - 6, 1 );

	    // Paint top
	    g.drawLine( right - 6, 1, 1, 1 );

	    // Paint left
	    if ( tabIndex==lastIndex ) {
	        // last tab in run
	        g.drawLine( 1, 1, 1, bottom );
	    } else {
	        g.drawLine( 0, 1, 0, bottom );
	    }
	}

        g.translate( -x, -y );
    }

    protected boolean shouldFillGap( int currentRun, int tabIndex, int x, int y ) {
        boolean result = false;

        if ( currentRun == runCount - 2 ) {  // If it's the second to last row.
            Rectangle lastTabBounds = getTabBounds( tabPane, tabPane.getTabCount() - 1 );
	    Rectangle tabBounds = getTabBounds( tabPane, tabIndex );
            if (MetalUtils.isLeftToRight(tabPane)) {
	        int lastTabRight = lastTabBounds.x + lastTabBounds.width - 1;

		// is the right edge of the last tab to the right
		// of the left edge of the current tab?
		if ( lastTabRight > tabBounds.x + 2 ) {
		    return true;
		}
	    } else {
	        int lastTabLeft = lastTabBounds.x;
		int currentTabRight = tabBounds.x + tabBounds.width - 1;

		// is the left edge of the last tab to the left
		// of the right edge of the current tab?
		if ( lastTabLeft < currentTabRight - 2 ) {
		    return true;
		}
            }
        } else {
	    // fill in gap for all other rows except last row
            result = currentRun != runCount - 1;
        }

        return result;
    }

    protected Color getColorForGap( int currentRun, int x, int y ) {
        final int shadowWidth = 4;
        int selectedIndex = tabPane.getSelectedIndex();
        int startIndex = tabRuns[ currentRun + 1 ];
        int endIndex = lastTabInRun( tabPane.getTabCount(), currentRun + 1 );
        int tabOverGap = -1;
        // Check each tab in the row that is 'on top' of this row
        for ( int i = startIndex; i <= endIndex; ++i ) {
            Rectangle tabBounds = getTabBounds( tabPane, i );
            int tabLeft = tabBounds.x;
            int tabRight = (tabBounds.x + tabBounds.width) - 1;
            // Check to see if this tab is over the gap
	    if ( MetalUtils.isLeftToRight(tabPane) ) {
                if ( tabLeft <= x && tabRight - shadowWidth > x ) {
                    return selectedIndex == i ? selectColor : tabPane.getBackgroundAt( i );
                }
            }
            else {
	        if ( tabLeft + shadowWidth < x && tabRight >= x ) {
                    return selectedIndex == i ? selectColor : tabPane.getBackgroundAt( i );
                }
            }
        }

        return tabPane.getBackground();
    }

    protected void paintLeftTabBorder( int tabIndex, Graphics g, 
                                       int x, int y, int w, int h,
                                       int btm, int rght,
                                       boolean isSelected ) {
        int tabCount = tabPane.getTabCount();
        int currentRun = getRunForTab( tabCount, tabIndex );
        int lastIndex = lastTabInRun( tabCount, currentRun );
        int firstIndex = tabRuns[ currentRun ];

        g.translate( x, y );

        int bottom = h - 1;
        int right = w - 1;

        //
        // Paint part of the tab above
        //

        if ( tabIndex != firstIndex ) {
            g.setColor( tabPane.getSelectedIndex() == tabIndex - 1 ?
                        selectColor :
                        tabPane.getBackgroundAt( tabIndex - 1 ) );
            g.fillRect( 2, 0, 4, 3 );
            g.drawLine( 2, 3, 2, 3 );
        }


        //
        // Paint Highlight
        //

        g.setColor( isSelected ? selectHighlight : highlight );

        // Paint slant
        g.drawLine( 1, 6, 6, 1 );

        // Paint top
        g.drawLine( 6, 1, right, 1 );

        // Paint left
        g.drawLine( 1, 6, 1, bottom );

        if ( tabIndex != firstIndex ) {
            g.setColor( tabPane.getSelectedIndex() == tabIndex - 1 ?
                        selectHighlight :
                        highlight );
            g.drawLine( 1, 0, 1, 4 );
        }

        //
        // Paint Border
        //

        g.setColor( darkShadow );

        // Paint slant
        g.drawLine( 1, 5, 6, 0 );

        // Paint top
        g.drawLine( 6, 0, right, 0 );

        // Paint left
        if ( tabIndex != firstIndex ) {
            g.drawLine( 0, 0, 0, bottom );
        } else {
            g.drawLine( 0, 6, 0, bottom );
        }

        // Paint bottom
        if ( tabIndex == lastIndex ) {
            g.drawLine( 0, bottom, right, bottom );
        }

        g.translate( -x, -y );
    }


    protected void paintBottomTabBorder( int tabIndex, Graphics g, 
                                         int x, int y, int w, int h,
                                         int btm, int rght,
                                         boolean isSelected ) {
        int tabCount = tabPane.getTabCount();
        int currentRun = getRunForTab( tabCount, tabIndex );
        int lastIndex = lastTabInRun( tabCount, currentRun );
        int firstIndex = tabRuns[ currentRun ];
	boolean leftToRight = MetalUtils.isLeftToRight(tabPane);

        int bottom = h - 1;
        int right = w - 1;

        //
        // Paint Gap
        //

        if ( shouldFillGap( currentRun, tabIndex, x, y ) ) {
            g.translate( x, y );

	    if ( leftToRight ) {
	        g.setColor( getColorForGap( currentRun, x, y ) );
		g.fillRect( 1, bottom - 4, 3, 5 );
		g.fillRect( 4, bottom - 1, 2, 2 );
	    } else {
	        g.setColor( getColorForGap( currentRun, x + w - 1, y ) );
		g.fillRect( right - 3, bottom - 3, 3, 4 );
		g.fillRect( right - 5, bottom - 1, 2, 2 );
		g.drawLine( right - 1, bottom - 4, right - 1, bottom - 4 );
	    }

            g.translate( -x, -y );
        }

        g.translate( x, y );


        //
        // Paint Border
        //

        g.setColor( darkShadow );

	if ( leftToRight ) {

	    // Paint slant
	    g.drawLine( 1, bottom - 5, 6, bottom );

	    // Paint bottom
	    g.drawLine( 6, bottom, right, bottom );

	    // Paint right
	    if ( tabIndex == lastIndex ) {
	        g.drawLine( right, 0, right, bottom );
	    }

	    // Paint left
	    if ( tabIndex != tabRuns[ runCount - 1 ] ) {
	        g.drawLine( 0, 0, 0, bottom );
	    } else {
	        g.drawLine( 0, 0, 0, bottom - 6 );
	    }
	} else {

	    // Paint slant
	    g.drawLine( right - 1, bottom - 5, right - 6, bottom );

	    // Paint bottom
	    g.drawLine( right - 6, bottom, 0, bottom );

	    // Paint right
	    if ( tabIndex != tabRuns[ runCount - 1 ] ) {
	        // not the first tab in the last run
	        g.drawLine( right, 0, right, bottom );
	    } else {
	        // the first tab in the last run
	        g.drawLine( right, 0, right, bottom - 6 );
	    }

	    // Paint left
	    if ( tabIndex==lastIndex ) {
	        // last tab in run
	        g.drawLine( 0, 0, 0, bottom );
	    }
	}

        //
        // Paint Highlight
        //

        g.setColor( isSelected ? selectHighlight : highlight );

	if ( leftToRight ) {

	    // Paint slant
	    g.drawLine( 1, bottom - 6, 6, bottom - 1 );

	    // Paint left
	    g.drawLine( 1, 0, 1, bottom - 6 );

	    // paint highlight in the gap on tab behind this one
	    // on the left end (where they all line up)
	    if ( tabIndex==firstIndex && tabIndex!=tabRuns[runCount - 1] ) {
	        //  first tab in run but not first tab in last run
	        if (tabPane.getSelectedIndex()==tabRuns[currentRun+1]) {
		    // tab in front of selected tab
		    g.setColor( selectHighlight );
		}
		else {
		    // tab in front of normal tab
		    g.setColor( highlight );
		}
		g.drawLine( 1, bottom - 4, 1, bottom );
	    }
	} else {

	    // Paint left
	    if ( tabIndex==lastIndex ) {
	        // last tab in run
	        g.drawLine( 1, 0, 1, bottom - 1 );
	    } else {
	        g.drawLine( 0, 0, 0, bottom - 1 );
	    }
	}

        g.translate( -x, -y );
    }

    protected void paintRightTabBorder( int tabIndex, Graphics g, 
                                        int x, int y, int w, int h,
                                        int btm, int rght,
                                        boolean isSelected ) {
        int tabCount = tabPane.getTabCount();
        int currentRun = getRunForTab( tabCount, tabIndex );
        int lastIndex = lastTabInRun( tabCount, currentRun );
        int firstIndex = tabRuns[ currentRun ];

        g.translate( x, y );

        int bottom = h - 1;
        int right = w - 1;

        //
        // Paint part of the tab above
        //

        if ( tabIndex != firstIndex ) {
            g.setColor( tabPane.getSelectedIndex() == tabIndex - 1 ?
                        tabAreaBackground :
                        tabPane.getBackgroundAt( tabIndex - 1 ) );
            g.fillRect( right - 5, 0, 5, 3 );
            g.fillRect( right - 2, 3, 2, 2 );
        }


        //
        // Paint Highlight
        //

        g.setColor( isSelected ? selectHighlight : highlight );

        // Paint slant
        g.drawLine( right - 6, 1, right - 1, 6 );

        // Paint top
        g.drawLine( 0, 1, right - 6, 1 );

        // Paint left
	if ( !isSelected ) {
	    g.drawLine( 0, 1, 0, bottom );
	}


        //
        // Paint Border
        //

        g.setColor( darkShadow );

        // Paint slant
        g.drawLine( right - 6, 0, right, 6 );

        // Paint top
        g.drawLine( 0, 0, right - 6, 0 );

        // Paint right
        if ( tabIndex != firstIndex ) {
            g.drawLine( right, 0, right, bottom );
        } else {
            g.drawLine( right, 6, right, bottom );
        }

        // Paint bottom
        if ( tabIndex == lastIndex ) {
            g.drawLine( 0, bottom, right, bottom );
        }

        g.translate( -x, -y );
    }

    public void update( Graphics g, JComponent c ) {
	if ( c.isOpaque() ) {
	    g.setColor( tabAreaBackground );
	    g.fillRect( 0, 0, c.getWidth(),c.getHeight() );
	}
	paint( g, c );
    }

    protected void paintTabBackground( Graphics g, int tabPlacement,
                                       int tabIndex, int x, int y, int w, int h, boolean isSelected ) {
        int slantWidth = h / 2;
        if ( isSelected ) {
            g.setColor( selectColor );
        } else {
            g.setColor( tabPane.getBackgroundAt( tabIndex ) );
        }

	if (MetalUtils.isLeftToRight(tabPane)) {
	    switch ( tabPlacement ) {
                case LEFT:
		    g.fillRect( x + 5, y + 1, w - 5, h - 1);
		    g.fillRect( x + 2, y + 4, 3, h - 4 );
		    break;
                case BOTTOM:
		    g.fillRect( x + 2, y, w - 2, h - 4 );
		    g.fillRect( x + 5, y + (h - 1) - 3, w - 5, 3 );
		    break;
                case RIGHT:
		    g.fillRect( x + 1, y + 1, w - 5, h - 1);
		    g.fillRect( x + (w - 1) - 3, y + 5, 3, h - 5 );
		    break;
                case TOP:
                default:
		    g.fillRect( x + 4, y + 2, (w - 1) - 3, (h - 1) - 1 );
		    g.fillRect( x + 2, y + 5, 2, h - 5 );
	    }
	} else {
	    switch ( tabPlacement ) {
                case LEFT:
		    g.fillRect( x + 5, y + 1, w - 5, h - 1);
		    g.fillRect( x + 2, y + 4, 3, h - 4 );
		    break;
                case BOTTOM:
		    g.fillRect( x, y, w - 5, h - 1 );
		    g.fillRect( x + (w - 1) - 4, y, 4, h - 5);
		    g.fillRect( x + (w - 1) - 4, y + (h - 1) - 4, 2, 2);
		    break;
                case RIGHT:
		    g.fillRect( x + 1, y + 1, w - 5, h - 1);
		    g.fillRect( x + (w - 1) - 3, y + 5, 3, h - 5 );
		    break;
                case TOP:
                default:
		    g.fillRect( x, y + 2, (w - 1) - 3, (h - 1) - 1 );
		    g.fillRect( x + (w - 1) - 3, y + 4, 3, h - 4 );
	    }
	}
    }

    /**
     * Overidden to do nothing for the Java L&F.
     */
    protected int getTabLabelShiftX( int tabPlacement, int tabIndex, boolean isSelected ) {
        return 0; 
    }


    /**
     * Overidden to do nothing for the Java L&F.
     */
    protected int getTabLabelShiftY( int tabPlacement, int tabIndex, boolean isSelected ) {
        return 0; 
    }


    public void paint( Graphics g, JComponent c ) {
        int tabPlacement = tabPane.getTabPlacement();

        Insets insets = c.getInsets(); Dimension size = c.getSize();

        // Paint the background for the tab area
        if ( tabPane.isOpaque() ) {
            g.setColor( c.getBackground() );
            switch ( tabPlacement ) {
            case LEFT:
                g.fillRect( insets.left, insets.top, 
                            calculateTabAreaWidth( tabPlacement, runCount, maxTabWidth ),
                            size.height - insets.bottom - insets.top );
                break;
            case BOTTOM:
                int totalTabHeight = calculateTabAreaHeight( tabPlacement, runCount, maxTabHeight );
                g.fillRect( insets.left, size.height - insets.bottom - totalTabHeight, 
                            size.width - insets.left - insets.right,
                            totalTabHeight );
                break;
            case RIGHT:
                int totalTabWidth = calculateTabAreaWidth( tabPlacement, runCount, maxTabWidth );
                g.fillRect( size.width - insets.right - totalTabWidth,
                            insets.top, totalTabWidth, 
                            size.height - insets.top - insets.bottom );
                break;
            case TOP:
            default:
                g.fillRect( insets.left, insets.top, 
                            size.width - insets.right - insets.left, 
                            calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) );
                paintHighlightBelowTab();
            }
        }

        super.paint( g, c );
    }

    protected void paintHighlightBelowTab( ) {

    }


    protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                       Rectangle[] rects, int tabIndex, 
                                       Rectangle iconRect, Rectangle textRect,
                                       boolean isSelected) {
        if ( tabPane.hasFocus() && isSelected ) {
            Rectangle tabRect = rects[tabIndex];
	    boolean lastInRun = isLastInRun( tabIndex );
            g.setColor( focus );
            g.translate( tabRect.x, tabRect.y );
            int right = tabRect.width - 1;
            int bottom = tabRect.height - 1;
	    boolean leftToRight = MetalUtils.isLeftToRight(tabPane);
            switch ( tabPlacement ) {
            case RIGHT:
                g.drawLine( right - 6,2 , right - 2,6 );         // slant
                g.drawLine( 1,2 , right - 6,2 );                 // top
                g.drawLine( right - 2,6 , right - 2,bottom );    // right
                g.drawLine( 1,2 , 1,bottom );                    // left
                g.drawLine( 1,bottom , right - 2,bottom );       // bottom
                break;
            case BOTTOM:
	        if ( leftToRight ) {
		    g.drawLine( 2, bottom - 6, 6, bottom - 2 );   // slant
		    g.drawLine( 6, bottom - 2,
				right, bottom - 2 );              // bottom
		    g.drawLine( 2, 0, 2, bottom - 6 );            // left
		    g.drawLine( 2, 0, right, 0 );                 // top
		    g.drawLine( right, 0, right, bottom - 2 );    // right
		} else {
		    g.drawLine( right - 2, bottom - 6,
				right - 6, bottom - 2 );          // slant
		    g.drawLine( right - 2, 0,
				right - 2, bottom - 6 );          // right
		    if ( lastInRun ) {
			// last tab in run
			g.drawLine( 2, bottom - 2,
				    right - 6, bottom - 2 );      // bottom
			g.drawLine( 2, 0, right - 2, 0 );         // top
			g.drawLine( 2, 0, 2, bottom - 2 );        // left
		    } else {
			g.drawLine( 1, bottom - 2,
				    right - 6, bottom - 2 );      // bottom
			g.drawLine( 1, 0, right - 2, 0 );         // top
			g.drawLine( 1, 0, 1, bottom - 2 );        // left
		    }
		}
                break;
            case LEFT:
                g.drawLine( 2, 6, 6, 2 );                         // slant
                g.drawLine( 2, 6, 2, bottom - 1);                 // left
                g.drawLine( 6, 2, right, 2 );                     // top
                g.drawLine( right, 2, right, bottom - 1 );        // right
                g.drawLine( 2, bottom - 1, 
                            right, bottom - 1 );                  // bottom
                break;
            case TOP:
             default:
		    if ( leftToRight ) {
		        g.drawLine( 2, 6, 6, 2 );                     // slant
			g.drawLine( 2, 6, 2, bottom - 1);             // left
			g.drawLine( 6, 2, right, 2 );                 // top
			g.drawLine( right, 2, right, bottom - 1 );    // right
			g.drawLine( 2, bottom - 1,
				    right, bottom - 1 );              // bottom
		    }
		    else {
		        g.drawLine( right - 2, 6, right - 6, 2 );     // slant
			g.drawLine( right - 2, 6,
				    right - 2, bottom - 1);           // right
			if ( lastInRun ) {
			    // last tab in run
			    g.drawLine( right - 6, 2, 2, 2 );         // top
			    g.drawLine( 2, 2, 2, bottom - 1 );        // left
			    g.drawLine( right - 2, bottom - 1,
					2, bottom - 1 );              // bottom
			}
			else {
			    g.drawLine( right - 6, 2, 1, 2 );         // top
			    g.drawLine( 1, 2, 1, bottom - 1 );        // left
			    g.drawLine( right - 2, bottom - 1,
					1, bottom - 1 );              // bottom
			}
		    }
            }
            g.translate( -tabRect.x, -tabRect.y );
        }
    }

    protected void paintContentBorderTopEdge( Graphics g, int tabPlacement,
                                              int selectedIndex,
                                              int x, int y, int w, int h ) {    
	boolean leftToRight = MetalUtils.isLeftToRight(tabPane);
	int right = x + w - 1;

        g.setColor(selectHighlight);

        if (tabPlacement != TOP || selectedIndex < 0 || 
            (rects[selectedIndex].y + rects[selectedIndex].height + 1 < y)) {
            g.drawLine(x, y, x+w-2, y);
        } else {
            Rectangle selRect = rects[selectedIndex];
	    boolean lastInRun = isLastInRun(selectedIndex);

	    if ( leftToRight || lastInRun ) {
	        g.drawLine(x, y, selRect.x + 1, y);
	    } else {
	        g.drawLine(x, y, selRect.x, y);
	    }

            if (selRect.x + selRect.width < right - 1) {
	        if ( leftToRight && !lastInRun ) {
		    g.drawLine(selRect.x + selRect.width, y, right - 1, y);
		} else {
		    g.drawLine(selRect.x + selRect.width - 1, y, right - 1, y);
		}
            } else {
	        g.setColor(shadow); 
                g.drawLine(x+w-2, y, x+w-2, y);
            }
        }
    }

    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
                                                int selectedIndex,
                                                int x, int y, int w, int h) { 
	boolean leftToRight = MetalUtils.isLeftToRight(tabPane);
        int bottom = y + h - 1;
	int right = x + w - 1;

        g.setColor(shadow);
        if (tabPlacement != BOTTOM || selectedIndex < 0 ||
            (rects[selectedIndex].y - 1 > h)) {
            g.setColor(darkShadow);
            g.drawLine(x, y+h-1, x+w-1, y+h-1);
        } else {
            Rectangle selRect = rects[selectedIndex];
	    boolean lastInRun = isLastInRun(selectedIndex);

            g.setColor(darkShadow);

	    if ( leftToRight || lastInRun ) {
	        g.drawLine(x, bottom, selRect.x, bottom);
	    } else {
	        g.drawLine(x, bottom, selRect.x - 1, bottom);
	    }

            if (selRect.x + selRect.width < x + w - 2) {
	        if ( leftToRight && !lastInRun ) {
		    g.drawLine(selRect.x + selRect.width, bottom,
			                           right, bottom);
		} else {
		    g.drawLine(selRect.x + selRect.width - 1, bottom,
			                               right, bottom);
		}
            } 
        }
    }

    protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
                                              int selectedIndex,
                                              int x, int y, int w, int h) { 
        g.setColor(selectHighlight); 
        if (tabPlacement != LEFT || selectedIndex < 0 ||
           (rects[selectedIndex].x + rects[selectedIndex].width + 1< x)) {
            g.drawLine(x, y, x, y+h-2);
        } else {
            Rectangle selRect = rects[selectedIndex];

            g.drawLine(x, y, x, selRect.y + 1);
            if (selRect.y + selRect.height < y + h - 2) {
	      g.drawLine(x, selRect.y + selRect.height + 1, 
			 x, y+h+2);
            } 
        }
    }

    protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
                                               int selectedIndex,
                                               int x, int y, int w, int h) {
        g.setColor(shadow);
        if (tabPlacement != RIGHT || selectedIndex < 0 ||
            rects[selectedIndex].x - 1 > w) {
            g.setColor(darkShadow);
            g.drawLine(x+w-1, y, x+w-1, y+h-1);
        } else {
            Rectangle selRect = rects[selectedIndex];

            g.setColor(darkShadow);
            g.drawLine(x+w-1, y, x+w-1, selRect.y);

            if (selRect.y + selRect.height < y + h - 2) {
                g.setColor(darkShadow);
                g.drawLine(x+w-1, selRect.y + selRect.height, 
                           x+w-1, y+h-2);
            } 
        }
    }

    protected int calculateMaxTabHeight( int tabPlacement ) {
        FontMetrics metrics = getFontMetrics();
        int height = metrics.getHeight();
        boolean tallerIcons = false;

        for ( int i = 0; i < tabPane.getTabCount(); ++i ) {
            Icon icon = tabPane.getIconAt( i );
            if ( icon != null ) {
                if ( icon.getIconHeight() > height ) {
                    tallerIcons = true;
                    break;
                }
            }
        }
        return super.calculateMaxTabHeight( tabPlacement ) - 
                  (tallerIcons ? (tabInsets.top + tabInsets.bottom) : 0);
    }


    protected int getTabRunOverlay( int tabPlacement ) {
        // Tab runs layed out vertically should overlap
        // at least as much as the largest slant
        if ( tabPlacement == LEFT || tabPlacement == RIGHT ) {
            int maxTabHeight = calculateMaxTabHeight(tabPlacement);
            return maxTabHeight / 2;
        }
        return 0;
    }

    // Don't rotate runs!
    protected boolean shouldRotateTabRuns( int tabPlacement, int selectedRun ) {
        return false;
    }

    // Don't pad last run
    protected boolean shouldPadTabRun( int tabPlacement, int run ) {
        return runCount > 1 && run < runCount - 1;
    }

    private boolean isLastInRun( int tabIndex ) {
        int run = getRunForTab( tabPane.getTabCount(), tabIndex );
	int lastIndex = lastTabInRun( tabPane.getTabCount(), run );
	return tabIndex == lastIndex;
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of MetalTabbedPaneUI.
     */  
    public class TabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout {

        protected void normalizeTabRuns( int tabPlacement, int tabCount, 
                                     int start, int max ) {
            // Only normalize the runs for top & bottom;  normalizing
            // doesn't look right for Metal's vertical tabs
            // because the last run isn't padded and it looks odd to have
            // fat tabs in the first vertical runs, but slimmer ones in the
            // last (this effect isn't noticable for horizontal tabs).
            if ( tabPlacement == TOP || tabPlacement == BOTTOM ) {
                super.normalizeTabRuns( tabPlacement, tabCount, start, max );
            }
        }

        // Don't rotate runs!
        protected void rotateTabRuns( int tabPlacement, int selectedRun ) {
        }

        // Don't pad selected tab
        protected void padSelectedTab( int tabPlacement, int selectedIndex ) {
        }
    }

}

