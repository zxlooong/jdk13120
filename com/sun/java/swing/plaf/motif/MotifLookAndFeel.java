/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.java.swing.plaf.motif;

import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.*;

import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.DefaultEditorKit;

import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 * Implements the Motif Look and Feel.
 * UI classes not implemented specifically for Motif will
 * default to those implemented in Basic.  
 * <p>
 * For the keyboard keys defined for each component in this Look and
 * Feel (L&F), see 
 * <a href="../../../../../../javax/swing/doc-files/Key-Motif.html">Component Keystroke Actions for the Motif L&F</a>.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.135 06/27/03
 * @author unattributed
 */
public class MotifLookAndFeel extends BasicLookAndFeel
{
    public String getName() {
        return "CDE/Motif";
    }

    public String getID() {
        return "Motif";
    }

    public String getDescription() {
        return "The CDE/Motif Look and Feel";
    }


    public boolean isNativeLookAndFeel() {
        String osName = System.getProperty("os.name");
        return (osName != null) && (osName.indexOf("Solaris") != -1);
    }


    public boolean isSupportedLookAndFeel() {
        return true;
    }


    /**
     * Load the SystemColors into the defaults table.  The keys
     * for SystemColor defaults are the same as the names of
     * the public fields in SystemColor.  If the table is being
     * created on a native Motif platform we use the SystemColor
     * values, otherwise we create color objects whose values match
     * the default CDE/Motif colors.
     */
    protected void initSystemColorDefaults(UIDefaults table)
    {
        String[] defaultSystemColors = {
                  "desktop", "#005C5C", /* Color of the desktop background */
            "activeCaption", "#000080", /* Color for captions (title bars) when they are active. */
        "activeCaptionText", "#FFFFFF", /* Text color for text in captions (title bars). */
      "activeCaptionBorder", "#B24D7A", /* Border color for caption (title bar) window borders. */
          "inactiveCaption", "#AEB2C3", /* Color for captions (title bars) when not active. */
      "inactiveCaptionText", "#000000", /* Text color for text in inactive captions (title bars). */
    "inactiveCaptionBorder", "#AEB2C3", /* Border color for inactive caption (title bar) window borders. */
                   "window", "#AEB2C3", /* Default color for the interior of windows */
             "windowBorder", "#AEB2C3", /* ??? */
               "windowText", "#000000", /* ??? */
                     "menu", "#AEB2C3", /* ??? */
                 "menuText", "#000000", /* ??? */
                     "text", "#FFF7E9", /* Text background color */
                 "textText", "#000000", /* Text foreground color */
            "textHighlight", "#000000", /* Text background color when selected */
        "textHighlightText", "#FFF7E9", /* Text color when selected */
         "textInactiveText", "#808080", /* Text color when disabled */
                  "control", "#AEB2C3", /* Default color for controls (buttons, sliders, etc) */
              "controlText", "#000000", /* Default color for text in controls */
         "controlHighlight", "#DCDEE5", /* Highlight color for controls */
       "controlLtHighlight", "#DCDEE5", /* Light highlight color for controls */
            "controlShadow", "#63656F", /* Shadow color for controls */
       "controlLightShadow", "#9397A5", /* Shadow color for controls */           
          "controlDkShadow", "#000000", /* Dark shadow color for controls */
                "scrollbar", "#AEB2C3", /* Scrollbar ??? color. PENDING(jeff) foreground? background? ?*/
                     "info", "#FFF7E9", /* ??? */
                 "infoText", "#000000"  /* ??? */
        };

        // AWT SystemColors only for for CDE on JDK1.2
        loadSystemColors(table, defaultSystemColors, false/*is1dot2*/);
    }


    protected void initClassDefaults(UIDefaults table)
    {
        super.initClassDefaults(table);
        String motifPackageName = "com.sun.java.swing.plaf.motif.";

        Object[] uiDefaults = {
                   "ButtonUI", motifPackageName + "MotifButtonUI",
                 "CheckBoxUI", motifPackageName + "MotifCheckBoxUI",
            "DirectoryPaneUI", motifPackageName + "MotifDirectoryPaneUI",
              "FileChooserUI", motifPackageName + "MotifFileChooserUI",
                    "LabelUI", motifPackageName + "MotifLabelUI",
                  "MenuBarUI", motifPackageName + "MotifMenuBarUI",
                     "MenuUI", motifPackageName + "MotifMenuUI",
                 "MenuItemUI", motifPackageName + "MotifMenuItemUI",
         "CheckBoxMenuItemUI", motifPackageName + "MotifCheckBoxMenuItemUI",
      "RadioButtonMenuItemUI", motifPackageName + "MotifRadioButtonMenuItemUI",
              "RadioButtonUI", motifPackageName + "MotifRadioButtonUI",
             "ToggleButtonUI", motifPackageName + "MotifToggleButtonUI",
                "PopupMenuUI", motifPackageName + "MotifPopupMenuUI",
              "ProgressBarUI", motifPackageName + "MotifProgressBarUI",
                "ScrollBarUI", motifPackageName + "MotifScrollBarUI",
               "ScrollPaneUI", motifPackageName + "MotifScrollPaneUI",
                   "SliderUI", motifPackageName + "MotifSliderUI",
                "SplitPaneUI", motifPackageName + "MotifSplitPaneUI",
               "TabbedPaneUI", motifPackageName + "MotifTabbedPaneUI",
                 "TextAreaUI", motifPackageName + "MotifTextAreaUI",
                "TextFieldUI", motifPackageName + "MotifTextFieldUI",
            "PasswordFieldUI", motifPackageName + "MotifPasswordFieldUI",
                 "TextPaneUI", motifPackageName + "MotifTextPaneUI",
               "EditorPaneUI", motifPackageName + "MotifEditorPaneUI",
                     "TreeUI", motifPackageName + "MotifTreeUI",
            "InternalFrameUI", motifPackageName + "MotifInternalFrameUI",
              "DesktopPaneUI", motifPackageName + "MotifDesktopPaneUI",
                "SeparatorUI", motifPackageName + "MotifSeparatorUI",
       "PopupMenuSeparatorUI", motifPackageName + "MotifPopupMenuSeparatorUI",
               "OptionPaneUI", motifPackageName + "MotifOptionPaneUI",
                 "ComboBoxUI", motifPackageName + "MotifComboBoxUI",
              "DesktopIconUI", motifPackageName + "MotifDesktopIconUI"
        };

        table.putDefaults(uiDefaults);
    }


    private void loadResourceBundle(UIDefaults table) {
        ResourceBundle bundle = ResourceBundle.getBundle("com.sun.java.swing.plaf.motif.resources.motif");
	Enumeration iter = bundle.getKeys();
	while(iter.hasMoreElements()) {
	    String key = (String)iter.nextElement();
	    //System.out.println("key :" +key+ " value: " + bundle.getObject(key));
	    table.put( key, bundle.getObject(key) );
	}
    }


    protected void initComponentDefaults(UIDefaults table) 
    {
        super.initComponentDefaults(table);

        loadResourceBundle(table);

        FontUIResource dialogPlain12 = new FontUIResource("Dialog", 
                                                          Font.PLAIN, 12);
        FontUIResource serifPlain12 = new FontUIResource("Serif", 
                                                          Font.PLAIN, 12);
        FontUIResource sansSerifPlain12 = new FontUIResource("SansSerif", 
                                                          Font.PLAIN, 12);
        FontUIResource monospacedPlain12 = new FontUIResource("Monospaced", 
                                                          Font.PLAIN, 12);
        ColorUIResource red = new ColorUIResource(Color.red);
        ColorUIResource black = new ColorUIResource(Color.black);
        ColorUIResource white = new ColorUIResource(Color.white);
        ColorUIResource lightGray = new ColorUIResource(Color.lightGray);
        ColorUIResource controlDarker = new ColorUIResource(147, 151, 165);  // slate blue
        ColorUIResource scrollBarTrack = controlDarker;
        ColorUIResource menuItemPressedBackground = new ColorUIResource(165,165,165);
        ColorUIResource menuItemPressedForeground = new ColorUIResource(0,0,0);


        Border loweredBevelBorder = new MotifBorders.BevelBorder(false, 
                                           table.getColor("controlShadow"),
                                           table.getColor("controlLtHighlight"));

        Border raisedBevelBorder = new MotifBorders.BevelBorder(true,                                                                  table.getColor("controlShadow"),
                                           table.getColor("controlLtHighlight"));

        Border marginBorder = new BasicBorders.MarginBorder();

        Border focusBorder = new MotifBorders.FocusBorder(
                                           table.getColor("control"),
                                           table.getColor("activeCaptionBorder"));


        Border focusBevelBorder = new BorderUIResource.CompoundBorderUIResource(
                                          focusBorder,
                                          loweredBevelBorder);

        Border comboBoxBorder = new BorderUIResource.CompoundBorderUIResource(
                                          focusBorder,
                                          raisedBevelBorder);


        Border buttonBorder = new BorderUIResource.CompoundBorderUIResource(
                                      new MotifBorders.ButtonBorder(
                                          table.getColor("controlShadow"),
                                          table.getColor("controlHighlight"),
                                          table.getColor("controlDkShadow"),
                                          table.getColor("activeCaptionBorder")),
                                      marginBorder);

        Border toggleButtonBorder = new BorderUIResource.CompoundBorderUIResource(
                                      new MotifBorders.ToggleButtonBorder(
                                          table.getColor("controlShadow"),
                                          table.getColor("controlHighlight"),
                                          table.getColor("controlDkShadow"),
                                          table.getColor("activeCaptionBorder")),                                                        marginBorder);

        Border textFieldBorder = new BorderUIResource.CompoundBorderUIResource(
                                      focusBevelBorder,
                                      marginBorder);

	Border popupMenuBorder = new BorderUIResource.CompoundBorderUIResource(
				      raisedBevelBorder,
				      new MotifBorders.MotifPopupMenuBorder(
                                        table.getFont("PopupMenu.font"),
                                        table.getColor("PopupMenu.background"),
                                        table.getColor("PopupMenu.foreground"),
                                        table.getColor("controlShadow"),
                                        table.getColor("controlLtHighlight")
					));

        Object menuItemCheckIcon = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) {
                return MotifIconFactory.getMenuItemCheckIcon();
            }
        };

        Object menuItemArrowIcon = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) {
                return MotifIconFactory.getMenuItemArrowIcon();
            }
        };

        Object menuArrowIcon = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) {
                return MotifIconFactory.getMenuArrowIcon();
            }
        };

        Object checkBoxIcon = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) {
                return MotifIconFactory.getCheckBoxIcon();
            }
        };

        Object radioButtonIcon = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) {
                return MotifIconFactory.getRadioButtonIcon();
            }
        };

        Object unselectedTabBackground = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) { 
                Color c = (Color)table.getColor("control");
                return new ColorUIResource(Math.max((int)(c.getRed()*.85),0), 
                                           Math.max((int)(c.getGreen()*.85),0), 
                                           Math.max((int)(c.getBlue()*.85),0));
            }
        };

        Object unselectedTabForeground = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) { 
                Color c = (Color)table.getColor("controlText");
                return new ColorUIResource(Math.max((int)(c.getRed()*.85),0), 
                                           Math.max((int)(c.getGreen()*.85),0), 
                                           Math.max((int)(c.getBlue()*.85),0));
            }
        };

        Object unselectedTabShadow = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) { 
                Color c = (Color)table.getColor("control");
                Color base = new Color(Math.max((int)(c.getRed()*.85),0), 
                                       Math.max((int)(c.getGreen()*.85),0), 
                                       Math.max((int)(c.getBlue()*.85),0));
                return new ColorUIResource(base.darker());
            }
        };

        Object unselectedTabHighlight = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) { 
                Color c = (Color)table.getColor("control");
                Color base = new Color(Math.max((int)(c.getRed()*.85),0), 
                                       Math.max((int)(c.getGreen()*.85),0), 
                                       Math.max((int)(c.getBlue()*.85),0));
                return new ColorUIResource(base.brighter());
            }
        };

	// *** Text

	Object fieldInputMap = new UIDefaults.LazyInputMap(new Object[] {
			   "COPY", DefaultEditorKit.copyAction,
			  "PASTE", DefaultEditorKit.pasteAction,
			    "CUT", DefaultEditorKit.cutAction,
	         "control INSERT", DefaultEditorKit.copyAction,
	           "shift INSERT", DefaultEditorKit.pasteAction,
	           "shift DELETE", DefaultEditorKit.cutAction,
	              "control F", DefaultEditorKit.forwardAction,
	              "control B", DefaultEditorKit.backwardAction,
	              "control D", DefaultEditorKit.deleteNextCharAction,
		     "typed \010", DefaultEditorKit.deletePrevCharAction,
                         "DELETE", DefaultEditorKit.deleteNextCharAction,
                          "RIGHT", DefaultEditorKit.forwardAction,
                           "LEFT", DefaultEditorKit.backwardAction,
                       "KP_RIGHT", DefaultEditorKit.forwardAction,
                        "KP_LEFT", DefaultEditorKit.backwardAction,
	             "shift LEFT", DefaultEditorKit.selectionBackwardAction,
	            "shift RIGHT", DefaultEditorKit.selectionForwardAction,
	           "control LEFT", DefaultEditorKit.previousWordAction,
	          "control RIGHT", DefaultEditorKit.nextWordAction,
	     "control shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
	    "control shift RIGHT", DefaultEditorKit.selectionNextWordAction,
	          "control SLASH", DefaultEditorKit.selectAllAction,
	                   "HOME", DefaultEditorKit.beginLineAction,
	                    "END", DefaultEditorKit.endLineAction,
	             "shift HOME", DefaultEditorKit.selectionBeginLineAction,
	              "shift END", DefaultEditorKit.selectionEndLineAction,
	     "control BACK_SLASH", "unselect"/*DefaultEditorKit.unselectAction*/,
	                  "ENTER", JTextField.notifyAction
	});

	Object multilineInputMap = new UIDefaults.LazyInputMap(new Object[] {
			   "COPY", DefaultEditorKit.copyAction,
			  "PASTE", DefaultEditorKit.pasteAction,
			    "CUT", DefaultEditorKit.cutAction,
	         "control INSERT", DefaultEditorKit.copyAction,
	           "shift INSERT", DefaultEditorKit.pasteAction,
	           "shift DELETE", DefaultEditorKit.cutAction,
	              "control F", DefaultEditorKit.forwardAction,
	              "control B", DefaultEditorKit.backwardAction,
	              "control D", DefaultEditorKit.deleteNextCharAction,
		     "typed \010", DefaultEditorKit.deletePrevCharAction,
                         "DELETE", DefaultEditorKit.deleteNextCharAction,
                          "RIGHT", DefaultEditorKit.forwardAction,
                           "LEFT", DefaultEditorKit.backwardAction,
                       "KP_RIGHT", DefaultEditorKit.forwardAction,
                        "KP_LEFT", DefaultEditorKit.backwardAction,
	             "shift LEFT", DefaultEditorKit.selectionBackwardAction,
	            "shift RIGHT", DefaultEditorKit.selectionForwardAction,
	           "control LEFT", DefaultEditorKit.previousWordAction,
	          "control RIGHT", DefaultEditorKit.nextWordAction,
	     "control shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
	    "control shift RIGHT", DefaultEditorKit.selectionNextWordAction,
	          "control SLASH", DefaultEditorKit.selectAllAction,
	                   "HOME", DefaultEditorKit.beginLineAction,
	                    "END", DefaultEditorKit.endLineAction,
	             "shift HOME", DefaultEditorKit.selectionBeginLineAction,
	              "shift END", DefaultEditorKit.selectionEndLineAction,

		      "control N", DefaultEditorKit.downAction,
		      "control P", DefaultEditorKit.upAction,
			     "UP", DefaultEditorKit.upAction,
			   "DOWN", DefaultEditorKit.downAction,
			"PAGE_UP", DefaultEditorKit.pageUpAction,
		      "PAGE_DOWN", DefaultEditorKit.pageDownAction,
		  "shift PAGE_UP", "selection-page-up",
 	        "shift PAGE_DOWN", "selection-page-down",
	     "ctrl shift PAGE_UP", "selection-page-left",
 	   "ctrl shift PAGE_DOWN", "selection-page-right",
		       "shift UP", DefaultEditorKit.selectionUpAction,
		     "shift DOWN", DefaultEditorKit.selectionDownAction,
			  "ENTER", DefaultEditorKit.insertBreakAction,
			    "TAB", DefaultEditorKit.insertTabAction,
	     "control BACK_SLASH", "unselect"/*DefaultEditorKit.unselectAction*/,
		   "control HOME", DefaultEditorKit.beginAction,
		    "control END", DefaultEditorKit.endAction,
	     "control shift HOME", DefaultEditorKit.selectionBeginAction,
	      "control shift END", DefaultEditorKit.selectionEndAction,
                      "control T", "next-link-action", 
                "control shift T", "previous-link-action", 
                  "control SPACE", "activate-link-action" 
	});

        // *** Tree

        Object treeOpenIcon = LookAndFeel.makeIcon(getClass(), 
                                                           "icons/TreeOpen.gif");

        Object treeClosedIcon = LookAndFeel.makeIcon(getClass(), 
                                                             "icons/TreeClosed.gif");

        Object treeLeafIcon = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) {
                return MotifTreeCellRenderer.loadLeafIcon();
            }
        };

        Object treeExpandedIcon = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) {
                return MotifTreeUI.MotifExpandedIcon.createExpandedIcon();
            }
        };

        Object treeCollapsedIcon = new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) {
                return MotifTreeUI.MotifCollapsedIcon.createCollapsedIcon();
            }
        };

        Border menuBarBorder = new MotifBorders.MenuBarBorder(
                                          table.getColor("controlShadow"),
                                          table.getColor("controlHighlight"),
                                          table.getColor("controlDkShadow"),
                                          table.getColor("activeCaptionBorder"));


        Border menuMarginBorder = new BorderUIResource.CompoundBorderUIResource(
                                          loweredBevelBorder,
                                          marginBorder);


        Border focusCellHighlightBorder = new BorderUIResource.LineBorderUIResource( 
                                                table.getColor("activeCaptionBorder"));

	Object sliderFocusInsets = new InsetsUIResource( 0, 0, 0, 0 );

	// ** for tabbedpane

        Object tabbedPaneTabInsets = new InsetsUIResource(3, 4, 3, 4);

        Object tabbedPaneTabPadInsets = new InsetsUIResource(3, 0, 1, 0);
	 
	Object tabbedPaneTabAreaInsets = new InsetsUIResource(4, 2, 0, 8);

        Object tabbedPaneContentBorderInsets = new InsetsUIResource(2, 2, 2, 2);


        // ** for optionpane

        Object optionPaneBorder = new BorderUIResource.EmptyBorderUIResource(10,0,10,0);

        Object optionPaneButtonAreaBorder = new BorderUIResource.EmptyBorderUIResource(10,10,12,10);

        Object optionPaneMessageAreaBorder = new BorderUIResource.EmptyBorderUIResource(0,10,12,10);


        Object[] defaults = {

            "Desktop.background", table.get("desktop"),
	    "Desktop.ancestorInputMap",
	       new UIDefaults.LazyInputMap(new Object[] {
		 "ctrl F5", "restore", 
		 "ctrl F4", "close",
		 "ctrl F7", "move", 
		 "ctrl F8", "resize",
		   "RIGHT", "right",
		"KP_RIGHT", "right",
		    "LEFT", "left",
		 "KP_LEFT", "left",
		      "UP", "up",
		   "KP_UP", "up",
		    "DOWN", "down",
		 "KP_DOWN", "down",
		  "ESCAPE", "escape",
		 "ctrl F9", "minimize", 
		"ctrl F10", "maximize",
		 "ctrl F6", "selectNextFrame",
		"ctrl TAB", "selectNextFrame",
	     "ctrl alt F6", "selectNextFrame",
       "shift ctrl alt F6", "selectPreviousFrame"	      
	      }),

            "Panel.background", table.get("control"),
            "Panel.foreground", table.get("textText"),
            "Panel.font", dialogPlain12,

            "ProgressBar.font", dialogPlain12,
            "ProgressBar.foreground", controlDarker,
            "ProgressBar.background", table.get("control"), 
	    "ProgressBar.selectionForeground", table.get("control"),
	    "ProgressBar.selectionBackground", table.get("controlText"),
	    "ProgressBar.border", loweredBevelBorder,
            "ProgressBar.cellLength", new Integer(6),
            "ProgressBar.cellSpacing", new Integer(0),

	    // Buttons
	    "Button.margin", new InsetsUIResource(2, 4, 2, 4),
            "Button.border", buttonBorder,
            "Button.background", table.get("control"),
            "Button.foreground", table.get("controlText"),
            "Button.select", table.get("controlLightShadow"),
            "Button.font", dialogPlain12,
	    "Button.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
                         "SPACE", "pressed",
                "released SPACE", "released"
              }),

	    "CheckBox.textIconGap", new Integer(8),
	    "CheckBox.margin", new InsetsUIResource(2, 2, 6, 2),
            "CheckBox.icon", checkBoxIcon,
            "CheckBox.focus", table.get("activeCaptionBorder"),
	    "CheckBox.focusInputMap",
	       new UIDefaults.LazyInputMap(new Object[] {
		            "SPACE", "pressed",
                   "released SPACE", "released"
		 }),

	    "RadioButton.margin", new InsetsUIResource(2, 2, 6, 2),
	    "RadioButton.textIconGap", new Integer(8),
            "RadioButton.background", table.get("control"),
            "RadioButton.foreground", table.get("controlText"),
            "RadioButton.icon", radioButtonIcon,
            "RadioButton.focus", table.get("activeCaptionBorder"),
            "RadioButton.icon", radioButtonIcon,
	    "RadioButton.focusInputMap",
	       new UIDefaults.LazyInputMap(new Object[] {
                          "SPACE", "pressed",
                 "released SPACE", "released"
	      }),

            "ToggleButton.border", toggleButtonBorder,
            "ToggleButton.background", table.get("control"),
            "ToggleButton.foreground", table.get("controlText"),
            "ToggleButton.focus", table.get("controlText"),
            "ToggleButton.select", table.get("controlLightShadow"),
	    "ToggleButton.focusInputMap",
	      new UIDefaults.LazyInputMap(new Object[] {
		            "SPACE", "pressed",
                   "released SPACE", "released"
	        }),

	    // Menus
            "Menu.border", menuMarginBorder,
            "Menu.font", dialogPlain12,
            "Menu.acceleratorFont", dialogPlain12,
            "Menu.foreground", table.get("menuText"),
            "Menu.background", table.get("menu"),
            "Menu.selectionForeground", menuItemPressedForeground,
            "Menu.selectionBackground", menuItemPressedBackground,
            "Menu.checkIcon", menuItemCheckIcon,
            "Menu.arrowIcon", menuArrowIcon,
	    // These window InputMap bindings are used when the Menu is
	    // selected.
	    "Menu.selectedWindowInputMapBindings", new Object[] {
		  "ESCAPE", "cancel",
                    "DOWN", "selectNext",
		 "KP_DOWN", "selectNext",
		      "UP", "selectPrevious",
		   "KP_UP", "selectPrevious",
		    "LEFT", "selectParent",
		 "KP_LEFT", "selectParent",
		   "RIGHT", "selectChild",
		"KP_RIGHT", "selectChild",
		   "ENTER", "return",
		   "SPACE", "return"
	    },

            "MenuBar.border", menuBarBorder,
            "MenuBar.background", table.get("menu"),
            "MenuBar.foreground", table.get("menuText"),
            "MenuBar.font", dialogPlain12,
	    "MenuBar.windowBindings", new Object[] {
		"F10", "takeFocus" },

            "MenuItem.border", menuMarginBorder,
            "MenuItem.font", dialogPlain12,
            "MenuItem.acceleratorFont", dialogPlain12,
            "MenuItem.foreground", table.get("menuText"),
            "MenuItem.background", table.get("menu"),
            "MenuItem.selectionForeground", menuItemPressedForeground,
            "MenuItem.selectionBackground", menuItemPressedBackground,
            "MenuItem.checkIcon", menuItemCheckIcon,
            "MenuItem.arrowIcon", menuItemArrowIcon,

            "RadioButtonMenuItem.border", menuMarginBorder,
            "RadioButtonMenuItem.font", dialogPlain12,
            "RadioButtonMenuItem.acceleratorFont", dialogPlain12,
            "RadioButtonMenuItem.foreground", table.get("menuText"),
            "RadioButtonMenuItem.background", table.get("menu"),
            "RadioButtonMenuItem.selectionForeground", menuItemPressedForeground,
            "RadioButtonMenuItem.selectionBackground", menuItemPressedBackground,
            "RadioButtonMenuItem.checkIcon", radioButtonIcon,
            "RadioButtonMenuItem.arrowIcon", menuItemArrowIcon,

            "CheckBoxMenuItem.border", menuMarginBorder,
            "CheckBoxMenuItem.font", dialogPlain12,
            "CheckBoxMenuItem.acceleratorFont", dialogPlain12,
            "CheckBoxMenuItem.foreground", table.get("menuText"),
            "CheckBoxMenuItem.background", table.get("menu"),
            "CheckBoxMenuItem.selectionForeground", menuItemPressedForeground,
            "CheckBoxMenuItem.selectionBackground", menuItemPressedBackground,
            "CheckBoxMenuItem.checkIcon", checkBoxIcon,
            "CheckBoxMenuItem.arrowIcon", menuItemArrowIcon,

            "PopupMenu.background", table.get("menu"),
            "PopupMenu.border", popupMenuBorder,            
            "PopupMenu.foreground", table.get("menuText"),
            "PopupMenu.font", dialogPlain12,

            "Label.font", dialogPlain12,
            "Label.background", table.get("control"),
            "Label.foreground", table.get("controlText"),

            "Separator.shadow", table.get("controlShadow"),          // DEPRECATED - DO NOT USE!
            "Separator.highlight", table.get("controlLtHighlight"),  // DEPRECATED - DO NOT USE!

            "Separator.background", table.get("controlLtHighlight"),
            "Separator.foreground", table.get("controlShadow"),

            "List.focusCellHighlightBorder", focusCellHighlightBorder,
	    "List.focusInputMap",
	       new UIDefaults.LazyInputMap(new Object[] {
		               "UP", "selectPreviousRow",
		            "KP_UP", "selectPreviousRow",
		         "shift UP", "selectPreviousRowExtendSelection",
		      "shift KP_UP", "selectPreviousRowExtendSelection",
		             "DOWN", "selectNextRow",
		          "KP_DOWN", "selectNextRow",
		       "shift DOWN", "selectNextRowExtendSelection",
		    "shift KP_DOWN", "selectNextRowExtendSelection",
		       "ctrl SPACE", "selectNextRowExtendSelection",
		             "HOME", "selectFirstRow",
		       "shift HOME", "selectFirstRowExtendSelection",
		              "END", "selectLastRow",
		        "shift END", "selectLastRowExtendSelection",
		          "PAGE_UP", "scrollUp",
		    "shift PAGE_UP", "scrollUpExtendSelection",
		        "PAGE_DOWN", "scrollDown",
		  "shift PAGE_DOWN", "scrollDownExtendSelection",
		           "ctrl A", "selectAll",
		       "ctrl SLASH", "selectAll",
		  "ctrl BACK_SLASH", "clearSelection"
		 }),

            "DesktopIcon.icon", LookAndFeel.makeIcon(getClass(), 
                                                             "icons/DesktopIcon.gif"),
	    "DesktopIcon.border", null,
	    // These are a little odd, MotifInternalFrameUI isntalls em!
	    "DesktopIcon.windowBindings", new Object[]
	      { "ESCAPE", "hideSystemMenu" },

	    "InternalFrame.windowBindings", new Object[] {
		"shift ESCAPE", "showSystemMenu",
		  "ctrl SPACE", "showSystemMenu",
	              "ESCAPE", "hideSystemMenu"
	    },

            "ScrollBar.background", scrollBarTrack,
            "ScrollBar.foreground", table.get("control"),
            "ScrollBar.track", scrollBarTrack,
            "ScrollBar.trackHighlight", table.get("controlDkShadow"),
            "ScrollBar.thumb", table.get("control"),
            "ScrollBar.thumbHighlight", table.get("controlHighlight"),
            "ScrollBar.thumbDarkShadow", table.get("controlDkShadow"),
            "ScrollBar.thumbLightShadow", table.get("controlShadow"),
            "ScrollBar.border", loweredBevelBorder,
	    "ScrollBar.allowsAbsolutePositioning", Boolean.TRUE,
	    "ScrollBar.focusInputMap",
	       new UIDefaults.LazyInputMap(new Object[] {
		       "RIGHT", "negativeUnitIncrement",
		    "KP_RIGHT", "negativeUnitIncrement",
		        "DOWN", "positiveUnitIncrement",
		     "KP_DOWN", "positiveUnitIncrement",
		   "PAGE_DOWN", "positiveBlockIncrement",
	      "ctrl PAGE_DOWN", "positiveBlockIncrement",
		        "LEFT", "positiveUnitIncrement",
		     "KP_LEFT", "positiveUnitIncrement",
		          "UP", "negativeUnitIncrement",
		       "KP_UP", "negativeUnitIncrement",
		     "PAGE_UP", "negativeBlockIncrement",
	        "ctrl PAGE_UP", "negativeBlockIncrement",
		        "HOME", "minScroll",
		         "END", "maxScroll"
		 }),
 
            "ScrollPane.font", dialogPlain12,
            "ScrollPane.background", table.get("control"),
            "ScrollPane.foreground", table.get("controlText"),
            "ScrollPane.border", null,
            "ScrollPane.viewportBorder", loweredBevelBorder,
	    "ScrollPane.ancestorInputMap",
	       new UIDefaults.LazyInputMap(new Object[] {
		           "RIGHT", "unitScrollRight",
		        "KP_RIGHT", "unitScrollRight",
		            "DOWN", "unitScrollDown",
		         "KP_DOWN", "unitScrollDown",
		            "LEFT", "unitScrollLeft",
		         "KP_LEFT", "unitScrollLeft",
		              "UP", "unitScrollUp",
		           "KP_UP", "unitScrollUp",
		         "PAGE_UP", "scrollUp",
		       "PAGE_DOWN", "scrollDown",
		    "ctrl PAGE_UP", "scrollLeft",
		  "ctrl PAGE_DOWN", "scrollRight",
		       "ctrl HOME", "scrollHome",
		        "ctrl END", "scrollEnd"
		 }),

            "Slider.border", focusBevelBorder,
            "Slider.foreground", table.get("control"),
            "Slider.background", controlDarker,
            "Slider.highlight", table.get("controlHighlight"),
            "Slider.shadow", table.get("controlShadow"),
            "Slider.focus", table.get("controlDkShadow"),
	    "Slider.focusInsets", sliderFocusInsets,
	    "Slider.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
		         "RIGHT", "positiveUnitIncrement",
		      "KP_RIGHT", "positiveUnitIncrement",
		          "DOWN", "negativeUnitIncrement",
		       "KP_DOWN", "negativeUnitIncrement",
		"ctrl PAGE_DOWN", "negativeBlockIncrement",
		          "LEFT", "negativeUnitIncrement",
		       "KP_LEFT", "negativeUnitIncrement",
		            "UP", "positiveUnitIncrement",
		         "KP_UP", "positiveUnitIncrement",
		  "ctrl PAGE_UP", "positiveBlockIncrement",
		          "HOME", "minScroll",
		           "END", "maxScroll"
	    }),

            "SplitPane.background", table.get("control"),
            "SplitPane.highlight", table.get("controlHighlight"),
            "SplitPane.shadow", table.get("controlShadow"),
            "SplitPane.dividerSize", new Integer(20),
            "SplitPane.activeThumb", table.get("activeCaptionBorder"),
	    "SplitPane.ancestorInputMap",
	       new UIDefaults.LazyInputMap(new Object[] {
		        "UP", "negativeIncrement",
		      "DOWN", "positiveIncrement",
		      "LEFT", "negativeIncrement",
		     "RIGHT", "positiveIncrement",
		     "KP_UP", "negativeIncrement",
		   "KP_DOWN", "positiveIncrement",
		   "KP_LEFT", "negativeIncrement",
		  "KP_RIGHT", "positiveIncrement",
		      "HOME", "selectMin",
		       "END", "selectMax",
		        "F8", "startResize",
		        "F6", "toggleFocus"
		 }),

            "TabbedPane.font", dialogPlain12,
            "TabbedPane.background", table.get("control"),
            "TabbedPane.foreground", table.get("controlText"),
            "TabbedPane.highlight", table.get("controlHighlight"),
            "TabbedPane.lightHighlight", table.get("controlLtHighlight"),
            "TabbedPane.shadow", table.get("controlShadow"),
            "TabbedPane.darkShadow", table.get("controlShadow"),
            "TabbedPane.unselectedTabBackground", unselectedTabBackground,
            "TabbedPane.unselectedTabForeground", unselectedTabForeground,
            "TabbedPane.unselectedTabHighlight", unselectedTabHighlight,
            "TabbedPane.unselectedTabShadow", unselectedTabShadow,
            "TabbedPane.focus", table.get("activeCaptionBorder"),
            "TabbedPane.tabInsets", tabbedPaneTabInsets,
            "TabbedPane.selectedTabPadInsets", tabbedPaneTabPadInsets,
            "TabbedPane.tabAreaInsets", tabbedPaneTabAreaInsets,
            "TabbedPane.contentBorderInsets", tabbedPaneContentBorderInsets,
	    "TabbedPane.focusInputMap",
	      new UIDefaults.LazyInputMap(new Object[] {
		         "RIGHT", "navigateRight",
	              "KP_RIGHT", "navigateRight",
	                  "LEFT", "navigateLeft",
	               "KP_LEFT", "navigateLeft",
	                    "UP", "navigateUp",
	                 "KP_UP", "navigateUp",
	                  "DOWN", "navigateDown",
	               "KP_DOWN", "navigateDown",
	             "ctrl DOWN", "requestFocusForVisibleComponent",
	          "ctrl KP_DOWN", "requestFocusForVisibleComponent",
		}),
	    "TabbedPane.ancestorInputMap",
	       new UIDefaults.LazyInputMap(new Object[] {
		   "ctrl PAGE_DOWN", "navigatePageDown",
	             "ctrl PAGE_UP", "navigatePageUp",
	                  "ctrl UP", "requestFocus",
	               "ctrl KP_UP", "requestFocus",
		 }),


            "Tree.background", controlDarker,                              // default: dark slate blue
            "Tree.hash", table.get("controlDkShadow"),                     // default: black
            "Tree.iconShadow", table.get("controlShadow"),
            "Tree.iconHighlight", table.get("controlHighlight"),
            "Tree.iconBackground", table.get("control"),                              
            "Tree.iconForeground", table.get("controlShadow"),             // default: black
            "Tree.textBackground", controlDarker,             // default: dark slate blue
            "Tree.textForeground", table.get("textText"),           // default: black
            "Tree.selectionBackground", table.get("text"),            // default: white
            "Tree.selectionForeground", table.get("textText"),              // default: black
            "Tree.selectionBorderColor", table.get("activeCaptionBorder"), // default: maroon
            "Tree.openIcon", treeOpenIcon,
            "Tree.closedIcon", treeClosedIcon,
            "Tree.leafIcon", treeLeafIcon,
            "Tree.expandedIcon", treeExpandedIcon,
            "Tree.collapsedIcon", treeCollapsedIcon,
	    "Tree.editorBorder", focusBorder,
	    "Tree.editorBorderSelectionColor", table.get("activeCaptionBorder"),
	    "Tree.rowHeight", new Integer(18),
	    "Tree.drawsFocusBorderAroundIcon", Boolean.TRUE,
	    "Tree.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
		                  "UP", "selectPreviousChangeLead",
		               "KP_UP", "selectPreviousChangeLead",
		            "shift UP", "selectPreviousExtendSelection",
		         "shift KP_UP", "selectPreviousExtendSelection",
		                "DOWN", "selectNextChangeLead",
		             "KP_DOWN", "selectNextChangeLead",
		          "shift DOWN", "selectNextExtendSelection",
		       "shift KP_DOWN", "selectNextExtendSelection",
		               "RIGHT", "selectChildChangeLead",
		            "KP_RIGHT", "selectChildChangeLead",
		                "LEFT", "selectParentChangeLead",
		             "KP_LEFT", "selectParentChangeLead",
		             "PAGE_UP", "scrollUpChangeLead",
		       "shift PAGE_UP", "scrollUpExtendSelection",
		           "PAGE_DOWN", "scrollDownChangeLead",
		     "shift PAGE_DOWN", "scrollDownExtendSelection",
		                "HOME", "selectFirstChangeLead",
		          "shift HOME", "selectFirstExtendSelection",
		                 "END", "selectLastChangeLead",
		           "shift END", "selectLastExtendSelection",
		               "ENTER", "toggle",
		                  "F2", "startEditing",
		              "ctrl A", "selectAll",
		          "ctrl SLASH", "selectAll",
		     "ctrl BACK_SLASH", "clearSelection",
		          "ctrl SPACE", "selectItem",
		         "shift SPACE", "extendSelection",
		      "ctrl PAGE_DOWN", "scrollRightChangeLead",
		        "ctrl PAGE_UP", "scrollLeftChangeLead",
		"ctrl shift PAGE_DOWN", "scrollRightExtendSelection",
		  "ctrl shift PAGE_UP", "scrollLeftExtendSelection",
		               "SPACE", "toggleSelection",
	      }),
	    "Tree.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[] {
		"ESCAPE", "cancel" }),

            "Table.focusCellHighlightBorder", focusCellHighlightBorder,
            "Table.scrollPaneBorder", null,

            //      "Table.background", white,  // cell background color
            //      "Table.focusCellBackground", white,
	    "Table.ancestorInputMap",
	       new UIDefaults.LazyInputMap(new Object[] {
		                "RIGHT", "selectNextColumn",
		             "KP_RIGHT", "selectNextColumn",
		                 "LEFT", "selectPreviousColumn",
		              "KP_LEFT", "selectPreviousColumn",
		                 "DOWN", "selectNextRow",
		              "KP_DOWN", "selectNextRow",
		                   "UP", "selectPreviousRow",
		                "KP_UP", "selectPreviousRow",
		          "shift RIGHT", "selectNextColumnExtendSelection",
		       "shift KP_RIGHT", "selectNextColumnExtendSelection",
		           "shift LEFT", "selectPreviousColumnExtendSelection",
		        "shift KP_LEFT", "selectPreviousColumnExtendSelection",
		           "shift DOWN", "selectNextRowExtendSelection",
		        "shift KP_DOWN", "selectNextRowExtendSelection",
		             "shift UP", "selectPreviousRowExtendSelection",
		          "shift KP_UP", "selectPreviousRowExtendSelection",
		              "PAGE_UP", "scrollUpChangeSelection",
		            "PAGE_DOWN", "scrollDownChangeSelection",
		                 "HOME", "selectFirstColumn",
		                  "END", "selectLastColumn",
		        "shift PAGE_UP", "scrollUpExtendSelection",
		      "shift PAGE_DOWN", "scrollDownExtendSelection",
		           "shift HOME", "selectFirstColumnExtendSelection",
		            "shift END", "selectLastColumnExtendSelection",
		         "ctrl PAGE_UP", "scrollLeftChangeSelection",
		       "ctrl PAGE_DOWN", "scrollRightChangeSelection",
		            "ctrl HOME", "selectFirstRow",
		             "ctrl END", "selectLastRow",
		   "ctrl shift PAGE_UP", "scrollRightExtendSelection",
		 "ctrl shift PAGE_DOWN", "scrollLeftExtendSelection",
		      "ctrl shift HOME", "selectFirstRowExtendSelection",
		       "ctrl shift END", "selectLastRowExtendSelection",
		                  "TAB", "selectNextColumnCell",
		            "shift TAB", "selectPreviousColumnCell",
		                "ENTER", "selectNextRowCell",
		          "shift ENTER", "selectPreviousRowCell",
		               "ctrl A", "selectAll",
		               "ESCAPE", "cancel",
		                   "F2", "startEditing"
		 }),


	    // ToolBar.
	    "ToolBar.ancestorInputMap",
	       new UIDefaults.LazyInputMap(new Object[] {
		        "UP", "navigateUp",
		     "KP_UP", "navigateUp",
		      "DOWN", "navigateDown",
		   "KP_DOWN", "navigateDown",
		      "LEFT", "navigateLeft",
		   "KP_LEFT", "navigateLeft",
		     "RIGHT", "navigateRight",
		  "KP_RIGHT", "navigateRight"
		 }),



            "ComboBox.control", table.get("control"),
            "ComboBox.controlForeground", black,
            "ComboBox.background", table.get("window"), 
            "ComboBox.foreground", black,
            "ComboBox.border", comboBoxBorder,
            "ComboBox.selectionBackground", black,
            "ComboBox.selectionForeground", table.get("text"),
            "ComboBox.disabledBackground", table.get("control"),
            "ComboBox.disabledForeground", table.get("textInactiveText"),
            "ComboBox.font", dialogPlain12,
	    "ComboBox.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[] {
		   "ESCAPE", "hidePopup",
		  "PAGE_UP", "pageUpPassThrough",
		"PAGE_DOWN", "pageDownPassThrough",
		     "HOME", "homePassThrough",
		      "END", "endPassThrough",
		     "DOWN", "selectNext",
		  "KP_DOWN", "selectNext",
		       "UP", "selectPrevious",
		    "KP_UP", "selectPrevious"
	      }),

            "TextField.caretForeground", black,
            "TextField.caretBlinkRate", new Integer(500),
            "TextField.inactiveForeground", table.get("textInactiveText"),
            "TextField.selectionBackground", table.get("textHighlight"),
            "TextField.selectionForeground", table.get("textHighlightText"),
            "TextField.background", table.get("window"),
            "TextField.foreground", table.get("textText"),
            "TextField.font", sansSerifPlain12,
            "TextField.border", textFieldBorder,
	    "TextField.focusInputMap", fieldInputMap,
            
            "PasswordField.caretForeground", black,
            "PasswordField.caretBlinkRate", new Integer(500),
            "PasswordField.inactiveForeground", table.get("textInactiveText"),
            "PasswordField.selectionBackground", table.get("textHighlight"),
            "PasswordField.selectionForeground", table.get("textHighlightText"),
            "PasswordField.background", table.get("window"),
            "PasswordField.foreground", table.get("textText"),
            "PasswordField.font", monospacedPlain12,
            "PasswordField.border", textFieldBorder,
	    "PasswordField.focusInputMap", fieldInputMap,
            
            "TextArea.caretForeground", black,
            "TextArea.caretBlinkRate", new Integer(500),
            "TextArea.inactiveForeground", table.get("textInactiveText"),
            "TextArea.selectionBackground", table.get("textHighlight"),
            "TextArea.selectionForeground", table.get("textHighlightText"),
            "TextArea.background", table.get("window"),
            "TextArea.foreground", table.get("textText"),
            "TextArea.font", monospacedPlain12,
            "TextArea.border", marginBorder,
	    "TextArea.focusInputMap", multilineInputMap,
            
            "TextPane.caretForeground", black,
            "TextPane.caretBlinkRate", new Integer(500),
            "TextPane.inactiveForeground", table.get("textInactiveText"),
            "TextPane.selectionBackground", lightGray,
            "TextPane.selectionForeground", table.get("textHighlightText"),
            "TextPane.background", white,
            "TextPane.foreground", table.get("textText"),
            "TextPane.font", serifPlain12,
            "TextPane.border", marginBorder,
	    "TextPane.focusInputMap", multilineInputMap,
            
            "EditorPane.caretForeground", red,
            "EditorPane.caretBlinkRate", new Integer(500),
            "EditorPane.inactiveForeground", table.get("textInactiveText"),
            "EditorPane.selectionBackground", lightGray,
            "EditorPane.selectionForeground", table.get("textHighlightText"),
            "EditorPane.background", white,
            "EditorPane.foreground", table.get("textText"),
            "EditorPane.font", serifPlain12,
            "EditorPane.border", marginBorder,
	    "EditorPane.focusInputMap", multilineInputMap,


	    "FileChooser.pathLabelMnemonic", new Integer(KeyEvent.VK_P), // 'p'
	    "FileChooser.filterLabelMnemonic", new Integer (KeyEvent.VK_R), // 'r'
	    "FileChooser.foldersLabelMnemonic", new Integer (KeyEvent.VK_L), // 'l'
	    "FileChooser.filesLabelMnemonic", new Integer (KeyEvent.VK_I), // 'i'
	    "FileChooser.enterFileNameLabelMnemonic", new Integer (KeyEvent.VK_N), // 'n'
	    "FileChooser.ancestorInputMap", 
	       new UIDefaults.LazyInputMap(new Object[] {
		     "ESCAPE", "cancelSelection"
		 }),


            "ToolTip.border", raisedBevelBorder,
            "ToolTip.background", table.get("info"),
            "ToolTip.foreground", table.get("infoText"),

            "OptionPane.border", optionPaneBorder,
            "OptionPane.messageAreaBorder", optionPaneMessageAreaBorder,
            "OptionPane.buttonAreaBorder", optionPaneButtonAreaBorder,
            "OptionPane.errorIcon", LookAndFeel.makeIcon(getClass(), 
                                                         "icons/Error.gif"),
            "OptionPane.informationIcon", LookAndFeel.makeIcon(getClass(), 
                                                               "icons/Inform.gif"),
            "OptionPane.warningIcon", LookAndFeel.makeIcon(getClass(), 
                                                           "icons/Warn.gif"),
            "OptionPane.questionIcon", LookAndFeel.makeIcon(getClass(), 
                                                            "icons/Question.gif"),
	    "OptionPane.windowBindings", new Object[] {
		"ESCAPE", "close" },

	    // These bindings are only enabled when there is a default
	    // button set on the rootpane.
	    "RootPane.defaultButtonWindowKeyBindings", new Object[] {
		             "ENTER", "press",
		    "released ENTER", "release",
		        "ctrl ENTER", "press",
	       "ctrl released ENTER", "release"
	      },
        };

        table.putDefaults(defaults);
    }

    /*
     * Returns whether this is being run on a JDK 1.2 or later VM.
     * This is a system-wide, rather than AppContext-wide, state.
     */
    /*package-private*/ static boolean is1dot2 = true;

    static {
        try {
            // Test if method introduced in 1.2 is available.
            Method m = Toolkit.class.getMethod("getMaximumCursorColors", null);
            is1dot2 = (m != null);
        } catch (NoSuchMethodException e) {
            is1dot2 = false;
        }
    }

}

