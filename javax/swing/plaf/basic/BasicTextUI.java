/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.beans.*;
import java.io.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.Border;

/**
 * <p>
 * Basis of a text components look-and-feel.  This provides the
 * basic editor view and controller services that may be useful
 * when creating a look-and-feel for an extension of JTextComponent.
 * <p>
 * Most state is held in the associated JTextComponent as bound
 * properties, and the UI installs default values for the 
 * various properties.  This default will install something for
 * all of the properties.  Typically, a LAF implementation will
 * do more however.  At a minimum, a LAF would generally install
 * key bindings.
 * <p>
 * This class also provides some concurrency support if the 
 * Document associated with the JTextComponent is a subclass of
 * AbstractDocument.  Access to the View (or View hierarchy) is
 * serialized between any thread mutating the model and the Swing
 * event thread (which is expected to render, do model/view coordinate
 * translation, etc).  <em>Any access to the root view should first
 * aquire a read-lock on the AbstractDocument and release that lock
 * in a finally block.</em>
 * <p>
 * An important method to define is the {@link #getPropertyPrefix} method
 * which is used as the basis of the keys used to fetch defaults
 * from the UIManager.  The string should reflect the type of 
 * TextUI (eg. TextField, TextArea, etc) without the particular 
 * LAF part of the name (eg Metal, Motif, etc).
 * <p>
 * To build a view of the model, one of the following strategies 
 * can be employed.
 * <ol>
 * <li>
 * One strategy is to simply redefine the 
 * ViewFactory interface in the UI.  By default, this UI itself acts
 * as the factory for View implementations.  This is useful
 * for simple factories.  To do this reimplement the 
 * {@link #create} method.
 * <li>
 * A common strategy for creating more complex types of documents
 * is to have the EditorKit implementation return a factory.  Since
 * the EditorKit ties all of the pieces necessary to maintain a type
 * of document, the factory is typically an important part of that
 * and should be produced by the EditorKit implementation.
 * <li>
 * A less common way to create more complex types is to have
 * the UI implementation create a.
 * seperate object for the factory.  To do this, the 
 * {@link #createViewFactory} method should be reimplemented to 
 * return some factory.
 * </ol>
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with 
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @author  Timothy Prinzing
 * @version 1.45 02/06/02
 */
public abstract class BasicTextUI extends TextUI implements ViewFactory {

    /**
     * Creates a new UI.
     */
    public BasicTextUI() {
        painted = false;
    }

    /**
     * Creates the object to use for a caret.  By default an
     * instance of BasicCaret is created.  This method
     * can be redefined to provide something else that implements
     * the InputPosition interface or a subclass of JCaret.
     *
     * @return the caret object
     */
    protected Caret createCaret() {
        return new BasicCaret();
    }

    /**
     * Creates the object to use for adding highlights.  By default
     * an instance of BasicHighlighter is created.  This method
     * can be redefined to provide something else that implements
     * the Highlighter interface or a subclass of DefaultHighlighter.
     *
     * @return the highlighter
     */
    protected Highlighter createHighlighter() {
        return new BasicHighlighter();
    }

    /**
     * Fetches the name of the keymap that will be installed/used 
     * by default for this UI. This is implemented to create a
     * name based upon the classname.  The name is the the name
     * of the class with the package prefix removed.
     *
     * @return the name
     */
    protected String getKeymapName() {
	String nm = getClass().getName();
	int index = nm.lastIndexOf('.');
	if (index >= 0) {
	    nm = nm.substring(index+1, nm.length());
	}
	return nm;
    }

    /**
     * Creates the keymap to use for the text component, and installs
     * any necessary bindings into it.  By default, the keymap is
     * shared between all instances of this type of TextUI. The
     * keymap has the name defined by the getKeymapName method.  If the
     * keymap is not found, then DEFAULT_KEYMAP from JTextComponent is used.
     * <p>
     * The set of bindings used to create the keymap is fetched 
     * from the UIManager using a key formed by combining the
     * {@link #getPropertyPrefix} method
     * and the string <code>.keyBindings</code>.  The type is expected
     * to be <code>JTextComponent.KeyBinding[]</code>.
     *
     * @return the keymap
     * @see #getKeymapName
     * @see javax.swing.text.JTextComponent
     */
    protected Keymap createKeymap() {
	String nm = getKeymapName();
	Keymap map = JTextComponent.getKeymap(nm);
	if (map == null) {
	    Keymap parent = JTextComponent.getKeymap(JTextComponent.DEFAULT_KEYMAP);
	    map = JTextComponent.addKeymap(nm, parent);
	    String prefix = getPropertyPrefix();
	    Object o = UIManager.get(prefix + ".keyBindings");
	    if ((o != null) && (o instanceof JTextComponent.KeyBinding[])) {
		JTextComponent.KeyBinding[] bindings = (JTextComponent.KeyBinding[]) o;
		JTextComponent.loadKeymap(map, bindings, getComponent().getActions());
	    }
	}
	return map;
    }

    /**
     * This method gets called when a bound property is changed
     * on the associated JTextComponent.  This is a hook
     * which UI implementations may change to reflect how the
     * UI displays bound properties of JTextComponent subclasses.
     * This is implemented to do nothing (i.e. the response to
     * properties in JTextComponent itself are handled prior
     * to calling this method).
     *
     * @param evt the property change event
     */
    protected void propertyChange(PropertyChangeEvent evt) {
    }

    /**
     * Gets the name used as a key to look up properties through the
     * UIManager.  This is used as a prefix to all the standard
     * text properties.
     *
     * @return the name
     */
    protected abstract String getPropertyPrefix();

    /**
     * Initializes component properties, e.g. font, foreground, 
     * background, caret color, selection color, selected text color,
     * disabled text color, and border color.  The font, foreground, and
     * background properties are only set if their current value is either null
     * or a UIResource, other properties are set if the current
     * value is null.
     * 
     * @see #uninstallDefaults
     * @see #installUI
     */
    protected void installDefaults() 
    {
        String prefix = getPropertyPrefix();
        Font f = editor.getFont();
        if ((f == null) || (f instanceof UIResource)) {
            editor.setFont(UIManager.getFont(prefix + ".font"));
        }

        Color bg = editor.getBackground();
        if ((bg == null) || (bg instanceof UIResource)) {
            editor.setBackground(UIManager.getColor(prefix + ".background"));
        }
        
        Color fg = editor.getForeground();
        if ((fg == null) || (fg instanceof UIResource)) {
            editor.setForeground(UIManager.getColor(prefix + ".foreground"));
        }

        Color color = editor.getCaretColor();
        if ((color == null) || (color instanceof UIResource)) {
            editor.setCaretColor(UIManager.getColor(prefix + ".caretForeground"));
        }

        Color s = editor.getSelectionColor();
        if ((s == null) || (s instanceof UIResource)) {
            editor.setSelectionColor(UIManager.getColor(prefix + ".selectionBackground"));
        }

        Color sfg = editor.getSelectedTextColor();
        if ((sfg == null) || (sfg instanceof UIResource)) {
            editor.setSelectedTextColor(UIManager.getColor(prefix + ".selectionForeground"));
        }

        Color dfg = editor.getDisabledTextColor();
        if ((dfg == null) || (dfg instanceof UIResource)) {
            editor.setDisabledTextColor(UIManager.getColor(prefix + ".inactiveForeground"));
        }

        Border b = editor.getBorder();
        if ((b == null) || (b instanceof UIResource)) {
            editor.setBorder(UIManager.getBorder(prefix + ".border"));
        }

        Insets margin = editor.getMargin();
        if (margin == null || margin instanceof UIResource) {
            editor.setMargin(UIManager.getInsets(prefix + ".margin"));
        }

        Caret caret = editor.getCaret();
        if (caret == null || caret instanceof UIResource) {
            caret = createCaret();
            editor.setCaret(caret);
        
            Object o = UIManager.get(prefix + ".caretBlinkRate");
            if ((o != null) && (o instanceof Integer)) {
                Integer rate = (Integer) o;
                caret.setBlinkRate(rate.intValue());
            }
        }

        Highlighter highlighter = editor.getHighlighter();
        if (highlighter == null || highlighter instanceof UIResource) {
            editor.setHighlighter(createHighlighter());
        }

    }

    /**
     * Sets the component properties that haven't been explicitly overriden to 
     * null.  A property is considered overridden if its current value
     * is not a UIResource.
     * 
     * @see #installDefaults
     * @see #uninstallUI
     */
    protected void uninstallDefaults() 
    {
        if (editor.getCaretColor() instanceof UIResource) {
            editor.setCaretColor(null);
        }
                                                                                         
        if (editor.getSelectionColor() instanceof UIResource) {
            editor.setSelectionColor(null);
        }

        if (editor.getDisabledTextColor() instanceof UIResource) {
            editor.setDisabledTextColor(null);
        }

        if (editor.getSelectedTextColor() instanceof UIResource) {
            editor.setSelectedTextColor(null);
        }

        if (editor.getBorder() instanceof UIResource) {
            editor.setBorder(null);
        }

        if (editor.getMargin() instanceof UIResource) {
            editor.setMargin(null);
        }

        if (editor.getCaret() instanceof UIResource) {
            editor.setCaret(null);
        }

        if (editor.getHighlighter() instanceof UIResource) {
            editor.setHighlighter(null);
        }

    }

    /**
     * Installs listeners for the UI.
     */
    protected void installListeners() {
    }

    /**
     * Uninstalls listeners for the UI.
     */
    protected void uninstallListeners() {
    }

    protected void installKeyboardActions() {
	// backward compatibility support... keymaps for the UI
	// are now installed in the more friendly input map.
        editor.setKeymap(createKeymap()); 

        InputMap km = getInputMap();
	if (km != null) {
	    SwingUtilities.replaceUIInputMap(editor, JComponent.WHEN_FOCUSED,
					     km);
	}
	
	ActionMap map = getActionMap();
	if (map != null) {
	    SwingUtilities.replaceUIActionMap(editor, map);
	}

	updateFocusAcceleratorBinding(false);
    }

    /**
     * Get the InputMap to use for the UI.  
     */
    InputMap getInputMap() {
	InputMap map = new InputMapUIResource();
	InputMap shared = 
	    (InputMap)UIManager.get(getPropertyPrefix() + ".focusInputMap");
	if (shared != null) {
	    map.setParent(shared);
	}
	return map;
    }

    /**
     * Invoked when the focus accelerator changes, this will update the
     * key bindings as necessary.
     */
    void updateFocusAcceleratorBinding(boolean changed) {
	char accelerator = editor.getFocusAccelerator();

	if (changed || accelerator != '\0') {
	    InputMap km = SwingUtilities.getUIInputMap
		        (editor, JComponent.WHEN_IN_FOCUSED_WINDOW);

	    if (km == null && accelerator != '\0') {
		km = new ComponentInputMapUIResource(editor);
		SwingUtilities.replaceUIInputMap(editor, JComponent.
						 WHEN_IN_FOCUSED_WINDOW, km);
		ActionMap am = getActionMap();
		SwingUtilities.replaceUIActionMap(editor, am);
	    }
	    if (km != null) {
		km.clear();
		if (accelerator != '\0') {
		    km.put(KeyStroke.getKeyStroke(accelerator,
						  ActionEvent.ALT_MASK),
			   "requestFocus");
		}
	    }
	}
    }

    /**
     * Fetch an action map to use.  This map is shared across
     * TextUI implementations of a given type.
     */
    ActionMap getActionMap() {
	String mapName = getPropertyPrefix() + ".actionMap";
	ActionMap map = (ActionMap)UIManager.get(mapName);

	if (map == null) {
	    map = createActionMap();
	    if (map != null) {
		UIManager.put(mapName, map);
	    }
	}
        ActionMap componentMap = new ActionMapUIResource();
        componentMap.put("requestFocus", new FocusAction());
        if (map != null) {
            componentMap.setParent(map);
        }
	return componentMap;
    }

    /**
     * Create a default action map.  This is basically the
     * set of actions found exported by the component.
     */
    ActionMap createActionMap() {
	ActionMap map = new ActionMapUIResource();
	Action[] actions = editor.getActions();
	//System.out.println("building map for UI: " + getPropertyPrefix());
	int n = actions.length;
	for (int i = 0; i < n; i++) {
	    Action a = actions[i];
	    map.put(a.getValue(Action.NAME), a);
	    //System.out.println("  " + a.getValue(Action.NAME));
	}
	return map;
    }

    protected void uninstallKeyboardActions() {
        editor.setKeymap(null);
	SwingUtilities.replaceUIInputMap(editor, JComponent.
					 WHEN_IN_FOCUSED_WINDOW, null);
	SwingUtilities.replaceUIActionMap(editor, null);
    }
    
    /**
     * Paints a background for the view.  This will only be
     * called if isOpaque() on the associated component is
     * true.  The default is to paint the background color 
     * of the component.
     *
     * @param g the graphics context
     */
    protected void paintBackground(Graphics g) {
        g.setColor(editor.getBackground());
        g.fillRect(0, 0, editor.getWidth(), editor.getHeight());
    }

    /**
     * Fetches the text component associated with this
     * UI implementation.  This will be null until
     * the ui has been installed.
     *
     * @return the editor component
     */
    protected final JTextComponent getComponent() {
        return editor;
    }

    /**
     * Flags model changes.
     * This is called whenever the model has changed.
     * It is implemented to rebuild the view hierarchy
     * to represent the default root element of the
     * associated model.
     */
    protected void modelChanged() {
        // create a view hierarchy
        ViewFactory f = rootView.getViewFactory();
        Document doc = editor.getDocument();
        Element elem = doc.getDefaultRootElement();
        setView(f.create(elem));
    }

    /**
     * Sets the current root of the view hierarchy and calls invalidate().
     * If there were any child components, they will be removed (i.e.
     * there are assumed to have come from components embedded in views).
     *
     * @param v the root view
     */
    protected final void setView(View v) {
	editor.removeAll();
        rootView.setView(v);
        painted = false;
        editor.revalidate();
        editor.repaint();
    }

    /**
     * Paints the interface safely with a guarantee that
     * the model won't change from the view of this thread.  
     * This does the following things, rendering from 
     * back to front.
     * <ol>
     * <li>
     * If the component is marked as opaque, the background
     * is painted in the current background color of the
     * component.
     * <li>
     * The highlights (if any) are painted.
     * <li>
     * The view hierarchy is painted.
     * <li>
     * The caret is painted.
     * </ol>
     *
     * @param g the graphics context
     */
    protected void paintSafely(Graphics g) {
	painted = true;
	Highlighter highlighter = editor.getHighlighter();
	Caret caret = editor.getCaret();
	
	// paint the background
	if (editor.isOpaque()) {
	    paintBackground(g);
	}
	
	// paint the highlights
	if (highlighter != null) {
	    highlighter.paint(g);
	}

	// paint the view hierarchy
	Rectangle alloc = getVisibleEditorRect();
	rootView.paint(g, alloc);
	    
	// paint the caret
	if (caret != null) {
	    caret.paint(g);
	}
    }

    // --- ComponentUI methods --------------------------------------------

    /**
     * Installs the UI for a component.  This does the following
     * things.
     * <ol>
     * <li>
     * Set the associated component to opaque (can be changed
     * easily by a subclass or on JTextComponent directly),
     * which is the most common case.  This will cause the
     * component's background color to be painted.
     * <li>
     * Install the default caret and highlighter into the 
     * associated component.
     * <li>
     * Attach to the editor and model.  If there is no 
     * model, a default one is created.
     * <li>
     * create the view factory and the view hierarchy used
     * to represent the model.
     * </ol>
     *
     * @param c the editor component
     * @see ComponentUI#installUI
     */
    public void installUI(JComponent c) {
        if (c instanceof JTextComponent) {
            editor = (JTextComponent) c;

            // install defaults
            installDefaults();

            // common case is background painted... this can
            // easily be changed by subclasses or from outside
            // of the component.
            editor.setOpaque(true);
            editor.setAutoscrolls(true);

            // attach to the model and editor
            editor.addPropertyChangeListener(updateHandler);
            Document doc = editor.getDocument();
            if (doc == null) {
                // no model, create a default one.  This will
                // fire a notification to the updateHandler 
                // which takes care of the rest. 
                editor.setDocument(getEditorKit(editor).createDefaultDocument());
            } else {
                doc.addDocumentListener(updateHandler);
                modelChanged();
            }

            // install keymap
            installListeners();
            installKeyboardActions();

	    LayoutManager oldLayout = editor.getLayout();
	    if ((oldLayout == null) || (oldLayout instanceof UIResource)) {
		// by default, use default LayoutManger implementation that
		// will position the components associated with a View object.
		editor.setLayout(updateHandler);
	    }

        } else {
            throw new Error("TextUI needs JTextComponent");
        }
    }

    /**
     * Deinstalls the UI for a component.  This removes the listeners,
     * uninstalls the highlighter, removes views, and nulls out the keymap.
     *
     * @param c the editor component
     * @see ComponentUI#uninstallUI
     */
    public void uninstallUI(JComponent c) {
        // detach from the model
        editor.removePropertyChangeListener(updateHandler);
        editor.getDocument().removeDocumentListener(updateHandler);

        // view part
        painted = false;
        uninstallDefaults();
        rootView.setView(null);
        c.removeAll();
	LayoutManager lm = c.getLayout();
	if (lm instanceof UIResource) {
	    c.setLayout(null);
	}

        // controller part
        uninstallKeyboardActions();
        uninstallListeners();
    }

    /**
     * Superclass paints background in an uncontrollable way
     * (i.e. one might want an image tiled into the background).
     * To prevent this from happening twice, this method is
     * reimplemented to simply paint.
     * <p>
     * <em>NOTE:</em> Superclass is also not thread-safe in 
     * it's rendering of the background, although that's not
     * an issue with the default rendering.
     */
    public void update(Graphics g, JComponent c) {
	paint(g, c);
    }

    /**
     * Paints the interface.  This is routed to the
     * paintSafely method under the guarantee that
     * the model won't change from the view of this thread
     * while it's rendering (if the associated model is
     * derived from AbstractDocument).  This enables the 
     * model to potentially be updated asynchronously.
     *
     * @param g the graphics context
     * @param c the editor component
     */
    public final void paint(Graphics g, JComponent c) {
	if ((rootView.getViewCount() > 0) && (rootView.getView(0) != null)) {
	    Document doc = editor.getDocument();
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readLock();
	    }
	    try {
		paintSafely(g);
	    } finally {
		if (doc instanceof AbstractDocument) {
		    ((AbstractDocument)doc).readUnlock();
		}
	    }
	}
    }

    /**
     * Gets the preferred size for the editor component.  If the component
     * has been given a size prior to receiving this request, it will
     * set the size of the view hierarchy to reflect the size of the component
     * before requesting the preferred size of the view hierarchy.  This
     * allows formatted views to format to the current component size before
     * answering the request.  Other views don't care about currently formatted
     * size and give the same answer either way.
     *
     * @param c the editor component
     * @return the size
     */
    public Dimension getPreferredSize(JComponent c) {
	Document doc = editor.getDocument();
	Insets i = c.getInsets();
	Dimension d = c.getSize();

	if (doc instanceof AbstractDocument) {
	    ((AbstractDocument)doc).readLock();
	}
	try {
	    if ((d.width > (i.left + i.right)) && (d.height > (i.top + i.bottom))) {
		rootView.setSize(d.width - i.left - i.right, d.height - i.top - i.bottom);
	    }
	    d.width = (int) Math.min((long) rootView.getPreferredSpan(View.X_AXIS) +
				     (long) i.left + (long) i.right, Integer.MAX_VALUE);
	    d.height = (int) Math.min((long) rootView.getPreferredSpan(View.Y_AXIS) +
				      (long) i.top + (long) i.bottom, Integer.MAX_VALUE);
	} finally {
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readUnlock();
	    }
	}
	return d;
    }

    /**
     * Gets the minimum size for the editor component.
     *
     * @param c the editor component
     * @return the size
     */
    public Dimension getMinimumSize(JComponent c) {
	Document doc = editor.getDocument();
        Insets i = c.getInsets();
	Dimension d = new Dimension();
	if (doc instanceof AbstractDocument) {
	    ((AbstractDocument)doc).readLock();
	}
	try {
	    d.width = (int) rootView.getMinimumSpan(View.X_AXIS) + i.left + i.right;
	    d.height = (int)  rootView.getMinimumSpan(View.Y_AXIS) + i.top + i.bottom;
	} finally {
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readUnlock();
	    }
	}
        return d;
    }

    /**
     * Gets the maximum size for the editor component.
     *
     * @param c the editor component
     * @return the size
     */
    public Dimension getMaximumSize(JComponent c) {
	Document doc = editor.getDocument();
        Insets i = c.getInsets();
	Dimension d = new Dimension();
	if (doc instanceof AbstractDocument) {
	    ((AbstractDocument)doc).readLock();
	}
	try {
	    d.width = (int) Math.min((long) rootView.getMaximumSpan(View.X_AXIS) + 
				     (long) i.left + (long) i.right, Integer.MAX_VALUE);
	    d.height = (int) Math.min((long) rootView.getMaximumSpan(View.Y_AXIS) + 
				      (long) i.top + (long) i.bottom, Integer.MAX_VALUE);
	} finally {
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readUnlock();
	    }
	}
        return d;
    }

    // ---- TextUI methods -------------------------------------------


    /**
     * Gets the allocation to give the root View.  Due
     * to an unfortunate set of historical events this 
     * method is inappropriately named.  The Rectangle
     * returned has nothing to do with visibility.  
     * The component must have a non-zero positive size for 
     * this translation to be computed.
     *
     * @return the bounding box for the root view
     */
    protected Rectangle getVisibleEditorRect() {
	Rectangle alloc = editor.getBounds();
	if ((alloc.width > 0) && (alloc.height > 0)) {
	    alloc.x = alloc.y = 0;
	    Insets insets = editor.getInsets();
	    alloc.x += insets.left;
	    alloc.y += insets.top;
	    alloc.width -= insets.left + insets.right;
	    alloc.height -= insets.top + insets.bottom;
	    return alloc;
	}
	return null;
    }

    /**
     * Converts the given location in the model to a place in
     * the view coordinate system.
     * The component must have a non-zero positive size for 
     * this translation to be computed.
     *
     * @param tc the text component for which this UI is installed
     * @param pos the local location in the model to translate >= 0
     * @return the coordinates as a rectangle, null if the model is not painted
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     * @see TextUI#modelToView
     */
    public Rectangle modelToView(JTextComponent tc, int pos) throws BadLocationException {
	return modelToView(tc, pos, Position.Bias.Forward);
    }

    /**
     * Converts the given location in the model to a place in
     * the view coordinate system.
     * The component must have a non-zero positive size for 
     * this translation to be computed.
     *
     * @param tc the text component for which this UI is installed
     * @param pos the local location in the model to translate >= 0
     * @return the coordinates as a rectangle, null if the model is not painted
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     * @see TextUI#modelToView
     */
    public Rectangle modelToView(JTextComponent tc, int pos, Position.Bias bias) throws BadLocationException {
	Document doc = editor.getDocument();
	if (doc instanceof AbstractDocument) {
	    ((AbstractDocument)doc).readLock();
	}
	try {
	    Rectangle alloc = getVisibleEditorRect();
	    if (alloc != null) {
		rootView.setSize(alloc.width, alloc.height);
		Shape s = rootView.modelToView(pos, alloc, bias);
		if (s != null) {
		  return s.getBounds();
		}
	    }
	} finally {
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readUnlock();
	    }
	}
	return null;
    }

    /**
     * Converts the given place in the view coordinate system
     * to the nearest representative location in the model.
     * The component must have a non-zero positive size for 
     * this translation to be computed.
     *
     * @param tc the text component for which this UI is installed
     * @param pt the location in the view to translate.  This
     *  should be in the same coordinate system as the mouse events.
     * @return the offset from the start of the document >= 0,
     *   -1 if not painted
     * @see TextUI#viewToModel
     */
    public int viewToModel(JTextComponent tc, Point pt) {
	return viewToModel(tc, pt, discardBias);
    }

    /**
     * Converts the given place in the view coordinate system
     * to the nearest representative location in the model.
     * The component must have a non-zero positive size for 
     * this translation to be computed.
     *
     * @param tc the text component for which this UI is installed
     * @param pt the location in the view to translate.  This
     *  should be in the same coordinate system as the mouse events.
     * @return the offset from the start of the document >= 0,
     *   -1 if the component doesn't yet have a positive size.
     * @see TextUI#viewToModel
     */
    public int viewToModel(JTextComponent tc, Point pt,
			   Position.Bias[] biasReturn) {
	int offs = -1;
	Document doc = editor.getDocument();
	if (doc instanceof AbstractDocument) {
	    ((AbstractDocument)doc).readLock();
	}
	try {
	    Rectangle alloc = getVisibleEditorRect();
	    if (alloc != null) {
		rootView.setSize(alloc.width, alloc.height);
		offs = rootView.viewToModel(pt.x, pt.y, alloc, biasReturn);
	    }
	} finally {
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readUnlock();
	    }
	}
        return offs;
    }

    /**
     * Provides a way to determine the next visually represented model 
     * location that one might place a caret.  Some views may not be visible,
     * they might not be in the same order found in the model, or they just
     * might not allow access to some of the locations in the model.
     *
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @param direction the direction from the current position that can
     *  be thought of as the arrow keys typically found on a keyboard.
     *  This may be SwingConstants.WEST, SwingConstants.EAST, 
     *  SwingConstants.NORTH, or SwingConstants.SOUTH.  
     * @return the location within the model that best represents the next
     *  location visual position.
     * @exception BadLocationException
     * @exception IllegalArgumentException for an invalid direction
     */
    public int getNextVisualPositionFrom(JTextComponent t, int pos,
		    Position.Bias b, int direction, Position.Bias[] biasRet)
	            throws BadLocationException{
	Document doc = editor.getDocument();
	if (doc instanceof AbstractDocument) {
	    ((AbstractDocument)doc).readLock();
	}
	try {
	    if (painted) {
		Rectangle alloc = getVisibleEditorRect();
		rootView.setSize(alloc.width, alloc.height);
		return rootView.getNextVisualPositionFrom(pos, b, alloc, direction,
							  biasRet);
	    }
	} finally {
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readUnlock();
	    }
	}
	return -1;
    }

    /**
     * Causes the portion of the view responsible for the
     * given part of the model to be repainted.  Does nothing if
     * the view is not currently painted.
     *
     * @param tc the text component for which this UI is installed
     * @param p0 the beginning of the range >= 0
     * @param p1 the end of the range >= p0
     * @see TextUI#damageRange
     */
    public void damageRange(JTextComponent tc, int p0, int p1) {
	damageRange(tc, p0, p1, Position.Bias.Forward, Position.Bias.Backward);
    }

    /**
     * Causes the portion of the view responsible for the 
     * given part of the model to be repainted.
     *
     * @param p0 the beginning of the range >= 0
     * @param p1 the end of the range >= p0
     */
    public void damageRange(JTextComponent t, int p0, int p1,
			    Position.Bias p0Bias, Position.Bias p1Bias) {
        if (painted) {
            Rectangle alloc = getVisibleEditorRect();
	    Document doc = t.getDocument();
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readLock();
	    }
            try {
		rootView.setSize(alloc.width, alloc.height);
		Shape toDamage = rootView.modelToView(p0, p0Bias,
                                                      p1, p1Bias, alloc);
		Rectangle rect = (toDamage instanceof Rectangle) ?
		                 (Rectangle)toDamage : toDamage.getBounds();
		editor.repaint(rect.x, rect.y, rect.width, rect.height);
            } catch (BadLocationException e) {
	    } finally {
		if (doc instanceof AbstractDocument) {
		    ((AbstractDocument)doc).readUnlock();
		}
	    }
        }
    }

    /**
     * Fetches the EditorKit for the UI.
     *
     * @param tc the text component for which this UI is installed
     * @return the editor capabilities
     * @see TextUI#getEditorKit
     */
    public EditorKit getEditorKit(JTextComponent tc) {
        return defaultKit;
    }

    /**
     * Fetches a View with the allocation of the associated 
     * text component (i.e. the root of the hierarchy) that 
     * can be traversed to determine how the model is being
     * represented spatially.
     * <p>
     * <font color=red><b>NOTE:</b>The View hierarchy can
     * be traversed from the root view, and other things
     * can be done as well.  Things done in this way cannot
     * be protected like simple method calls through the TextUI.
     * Therefore, proper operation in the presence of concurrency
     * must be arranged by any logic that calls this method!
     * </font>
     *
     * @param tc the text component for which this UI is installed
     * @return the view
     * @see TextUI#getRootView
     */
    public View getRootView(JTextComponent tc) {
        return rootView;
    }


    // --- ViewFactory methods ------------------------------

    /**
     * Creates a view for an element.
     * If a subclass wishes to directly implement the factory
     * producing the view(s), it should reimplement this 
     * method.  By default it simply returns null indicating
     * it is unable to represent the element.
     *
     * @param elem the element
     * @return the view
     */
    public View create(Element elem) {
        return null;
    }

    /**
     * Creates a view for an element.
     * If a subclass wishes to directly implement the factory
     * producing the view(s), it should reimplement this 
     * method.  By default it simply returns null indicating
     * it is unable to represent the part of the element.
     *
     * @param elem the element
     * @param p0 the starting offset >= 0
     * @param p1 the ending offset >= p0
     * @return the view
     */
    public View create(Element elem, int p0, int p1) {
        return null;
    }

    public static class BasicCaret extends DefaultCaret implements UIResource {}

    public static class BasicHighlighter extends DefaultHighlighter implements UIResource {}


    // ----- member variables ---------------------------------------

    private static final EditorKit defaultKit = new DefaultEditorKit();
    transient JTextComponent editor;
    transient boolean painted;
    transient RootView rootView = new RootView();
    transient UpdateHandler updateHandler = new UpdateHandler();

    private static final Position.Bias[] discardBias = new Position.Bias[1];

    /**
     * Root view that acts as a gateway between the component
     * and the View hierarchy.
     */
    class RootView extends View {

        RootView() {
            super(null);
        }

        void setView(View v) {
            if (view != null) {
                // get rid of back reference so that the old
                // hierarchy can be garbage collected.
                view.setParent(null);
            }
            view = v;
            if (view != null) {
                view.setParent(this);
            }
        }

	/**
	 * Fetches the attributes to use when rendering.  At the root
	 * level there are no attributes.  If an attribute is resolved
	 * up the view hierarchy this is the end of the line.
	 */
        public AttributeSet getAttributes() {
	    return null;
	}

        /**
         * Determines the preferred span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        public float getPreferredSpan(int axis) {
            if (view != null) {
                return view.getPreferredSpan(axis);
            }
            return 10;
        }

        /**
         * Determines the minimum span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        public float getMinimumSpan(int axis) {
            if (view != null) {
                return view.getMinimumSpan(axis);
            }
            return 10;
        }

        /**
         * Determines the maximum span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        public float getMaximumSpan(int axis) {
	    return Integer.MAX_VALUE;
        }

        /**
         * Specifies that a preference has changed.
         * Child views can call this on the parent to indicate that
         * the preference has changed.  The root view routes this to
         * invalidate on the hosting component.
         * <p>
         * This can be called on a different thread from the
         * event dispatching thread and is basically unsafe to
         * propagate into the component.  To make this safe,
         * the operation is transferred over to the event dispatching 
         * thread for completion.  It is a design goal that all view
         * methods be safe to call without concern for concurrency,
         * and this behavior helps make that true.
         *
         * @param child the child view
         * @param width true if the width preference has changed
         * @param height true if the height preference has changed
         */ 
        public void preferenceChanged(View child, boolean width, boolean height) {
            editor.revalidate();
        }

        /**
         * Determines the desired alignment for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the desired alignment, where 0.0 indicates the origin
         *     and 1.0 the full span away from the origin
         */
        public float getAlignment(int axis) {
            if (view != null) {
                return view.getAlignment(axis);
            }
            return 0;
        }

        /**
         * Renders the view.
         *
         * @param g the graphics context
         * @param allocation the region to render into
         */
        public void paint(Graphics g, Shape allocation) {
            if (view != null) {
                Rectangle alloc = (allocation instanceof Rectangle) ?
		          (Rectangle)allocation : allocation.getBounds();
                view.setSize(alloc.width, alloc.height);
                view.paint(g, allocation);
            }
        }
        
        /**
         * Sets the view parent.
         *
         * @param parent the parent view
         */
        public void setParent(View parent) {
            throw new Error("Can't set parent on root view");
        }

        /** 
         * Returns the number of views in this view.  Since
         * this view simply wraps the root of the view hierarchy
         * it has exactly one child.
         *
         * @return the number of views
         * @see #getView
         */
        public int getViewCount() {
            return 1;
        }

        /** 
         * Gets the n-th view in this container.
         *
         * @param n the number of the view to get
         * @return the view
         */
        public View getView(int n) {
            return view;
        }

	/**
	 * Returns the child view index representing the given position in
	 * the model.  This is implemented to return the index of the only
	 * child.
	 *
	 * @param pos the position >= 0
	 * @returns  index of the view representing the given position, or 
	 *   -1 if no view represents that position
	 * @since 1.3
	 */
        public int getViewIndex(int pos, Position.Bias b) {
	    return 0;
	}
    
        /**
         * Fetches the allocation for the given child view. 
         * This enables finding out where various views
         * are located, without assuming the views store
         * their location.  This returns the given allocation
         * since this view simply acts as a gateway between
         * the view hierarchy and the associated component.
         *
         * @param index the index of the child
         * @param a  the allocation to this view.
         * @return the allocation to the child
         */
        public Shape getChildAllocation(int index, Shape a) {
            return a;
        }

        /**
         * Provides a mapping from the document model coordinate space
         * to the coordinate space of the view mapped to it.
         *
         * @param pos the position to convert
         * @param a the allocated region to render into
         * @return the bounding box of the given position
         */
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            if (view != null) {
                return view.modelToView(pos, a, b);
            }
            return null;
        }

	/**
	 * Provides a mapping from the document model coordinate space
	 * to the coordinate space of the view mapped to it.
	 *
	 * @param p0 the position to convert >= 0
	 * @param b0 the bias toward the previous character or the
	 *  next character represented by p0, in case the 
	 *  position is a boundary of two views. 
	 * @param p1 the position to convert >= 0
	 * @param b1 the bias toward the previous character or the
	 *  next character represented by p1, in case the 
	 *  position is a boundary of two views. 
	 * @param a the allocated region to render into
	 * @return the bounding box of the given position is returned
	 * @exception BadLocationException  if the given position does
	 *   not represent a valid location in the associated document
	 * @exception IllegalArgumentException for an invalid bias argument
	 * @see View#viewToModel
	 */
	public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
	    if (view != null) {
		return view.modelToView(p0, b0, p1, b1, a);
	    }
	    return null;
	}

        /**
         * Provides a mapping from the view coordinate space to the logical
         * coordinate space of the model.
         *
         * @param x x coordinate of the view location to convert
         * @param y y coordinate of the view location to convert
         * @param a the allocated region to render into
         * @return the location within the model that best represents the
         *    given point in the view
         */
        public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
            if (view != null) {
                int retValue = view.viewToModel(x, y, a, bias);
		return retValue;
            }
            return -1;
        }

        /**
         * Provides a way to determine the next visually represented model 
         * location that one might place a caret.  Some views may not be visible,
         * they might not be in the same order found in the model, or they just
         * might not allow access to some of the locations in the model.
         *
         * @param pos the position to convert >= 0
         * @param a the allocated region to render into
         * @param direction the direction from the current position that can
         *  be thought of as the arrow keys typically found on a keyboard.
         *  This may be SwingConstants.WEST, SwingConstants.EAST, 
         *  SwingConstants.NORTH, or SwingConstants.SOUTH.  
         * @return the location within the model that best represents the next
         *  location visual position.
         * @exception BadLocationException
         * @exception IllegalArgumentException for an invalid direction
         */
        public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, 
                                             int direction,
                                             Position.Bias[] biasRet) 
            throws BadLocationException {
            if( view != null ) {
                int nextPos = view.getNextVisualPositionFrom(pos, b, a,
						     direction, biasRet);
		if(nextPos != -1) {
		    pos = nextPos;
		}
		else {
		    biasRet[0] = b;
		}
            } 
            return pos;
        }

        /**
         * Gives notification that something was inserted into the document
         * in a location that this view is responsible for.
         *
         * @param e the change information from the associated document
         * @param a the current allocation of the view
         * @param f the factory to use to rebuild if the view has children
         */
        public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.insertUpdate(e, a, f);
            }
        }
        
        /**
         * Gives notification that something was removed from the document
         * in a location that this view is responsible for.
         *
         * @param e the change information from the associated document
         * @param a the current allocation of the view
         * @param f the factory to use to rebuild if the view has children
         */
        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.removeUpdate(e, a, f);
            }
        }

        /**
         * Gives notification from the document that attributes were changed
         * in a location that this view is responsible for.
         *
         * @param e the change information from the associated document
         * @param a the current allocation of the view
         * @param f the factory to use to rebuild if the view has children
         */
        public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.changedUpdate(e, a, f);
            }
        }

        /**
         * Returns the document model underlying the view.
         *
         * @return the model
         */
        public Document getDocument() {
            return editor.getDocument();
        }
        
        /**
         * Returns the starting offset into the model for this view.
         *
         * @return the starting offset
         */
        public int getStartOffset() {
            if (view != null) {
                return view.getStartOffset();
            }
            return getElement().getStartOffset();
        }

        /**
         * Returns the ending offset into the model for this view.
         *
         * @return the ending offset
         */
        public int getEndOffset() {
            if (view != null) {
                return view.getEndOffset();
            }
            return getElement().getEndOffset();
        }

        /**
         * Gets the element that this view is mapped to.
         *
         * @return the view
         */
        public Element getElement() {
            if (view != null) {
                return view.getElement();
            }
            return editor.getDocument().getDefaultRootElement();
        }

        /**
         * Breaks this view on the given axis at the given length.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @param len specifies where a break is desired in the span
         * @param the current allocation of the view
         * @return the fragment of the view that represents the given span
         *   if the view can be broken, otherwise null
         */
        public View breakView(int axis, float len, Shape a) {
            throw new Error("Can't break root view");
        }

        /**
         * Determines the resizability of the view along the
         * given axis.  A value of 0 or less is not resizable.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the weight
         */
        public int getResizeWeight(int axis) {
            if (view != null) {
                return view.getResizeWeight(axis);
            }
            return 0;
        }

        /**
         * Sets the view size.
         *
         * @param width the width
         * @param height the height
         */
        public void setSize(float width, float height) {
            if (view != null) {
                view.setSize(width, height);
            }
        }

        /**
         * Fetches the container hosting the view.  This is useful for
         * things like scheduling a repaint, finding out the host 
         * components font, etc.  The default implementation
         * of this is to forward the query to the parent view.
         *
         * @return the container
         */
        public Container getContainer() {
            return editor;
        }
        
        /**
         * Fetches the factory to be used for building the
         * various view fragments that make up the view that
         * represents the model.  This is what determines
         * how the model will be represented.  This is implemented
         * to fetch the factory provided by the associated
         * EditorKit unless that is null, in which case this
         * simply returns the BasicTextUI itself which allows
         * subclasses to implement a simple factory directly without
         * creating extra objects.  
         *
         * @return the factory
         */
        public ViewFactory getViewFactory() {
            EditorKit kit = getEditorKit(editor);
            ViewFactory f = kit.getViewFactory();
            if (f != null) {
                return f;
            }
            return BasicTextUI.this;
        }

        private View view;

    }

    /**
     * Handles updates from various places.  If the model is changed,
     * this class unregisters as a listener to the old model and 
     * registers with the new model.  If the document model changes,
     * the change is forwarded to the root view.  If the focus
     * accelerator changes, a new keystroke is registered to request
     * focus.
     */
    class UpdateHandler implements PropertyChangeListener, DocumentListener, LayoutManager2, Runnable, UIResource {

        // --- PropertyChangeListener methods -----------------------

        /**
         * This method gets called when a bound property is changed.
         * We are looking for document changes on the editor.
         */
        public final void propertyChange(PropertyChangeEvent evt) {
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();
            if ((oldValue instanceof Document) || (newValue instanceof Document)) {
                if (oldValue != null) {
                    ((Document)oldValue).removeDocumentListener(this);
                }
                if (newValue != null) {
                    ((Document)newValue).addDocumentListener(this);
                }
                modelChanged();
            }
	    if (JTextComponent.FOCUS_ACCELERATOR_KEY.equals
		(evt.getPropertyName())) {
		updateFocusAcceleratorBinding(true);
            } else if ("componentOrientation".equals(evt.getPropertyName())) {
                ComponentOrientation o=(ComponentOrientation)evt.getNewValue();
                Boolean runDir = o.isLeftToRight() 
                                 ? TextAttribute.RUN_DIRECTION_LTR
                                 : TextAttribute.RUN_DIRECTION_RTL;
                Document doc = editor.getDocument();
                doc.putProperty( TextAttribute.RUN_DIRECTION, runDir );
                modelChanged();
            }
            BasicTextUI.this.propertyChange(evt);
        }

        // --- DocumentListener methods -----------------------

        /**
         * The insert notification.  Gets sent to the root of the view structure
         * that represents the portion of the model being represented by the
         * editor.  The factory is added as an argument to the update so that
         * the views can update themselves in a dynamic (not hardcoded) way.
         *
         * @param e  The change notification from the currently associated
         *  document.
         * @see DocumentListener#insertUpdate
         */
        public final void insertUpdate(DocumentEvent e) {
	    Document doc = e.getDocument();
	    Object o = doc.getProperty("i18n");
	    if (o instanceof Boolean) {
		Boolean i18nFlag = (Boolean) o;
		if (i18nFlag.booleanValue() != i18nView) {
		    // i18n flag changed, rebuild the view
		    i18nView = i18nFlag.booleanValue();
		    modelChanged();
		    return;
		}
	    }

	    // normal insert update
            Rectangle alloc = (painted) ? getVisibleEditorRect() : null;
            rootView.insertUpdate(e, alloc, rootView.getViewFactory());
        }

        /**
         * The remove notification.  Gets sent to the root of the view structure
         * that represents the portion of the model being represented by the
         * editor.  The factory is added as an argument to the update so that
         * the views can update themselves in a dynamic (not hardcoded) way.
         *
         * @param e  The change notification from the currently associated
         *  document.
         * @see DocumentListener#removeUpdate
         */
        public final void removeUpdate(DocumentEvent e) {
	    if ((constraints != null) && (! constraints.isEmpty())) {
		// There may be components to remove.  This method may
		// be called by a thread other than the event dispatching
		// thread.  We really want to do this before any repaints
		// get scheduled so the order here is important.
		if (SwingUtilities.isEventDispatchThread()) {
		    run();
		} else {
		    SwingUtilities.invokeLater(this);
		}
	    }

            Rectangle alloc = (painted) ? getVisibleEditorRect() : null;
            rootView.removeUpdate(e, alloc, rootView.getViewFactory());
	}

        /**
         * The change notification.  Gets sent to the root of the view structure
         * that represents the portion of the model being represented by the
         * editor.  The factory is added as an argument to the update so that
         * the views can update themselves in a dynamic (not hardcoded) way.
         *
         * @param e  The change notification from the currently associated
         *  document.
         * @see DocumentListener#changeUpdate
         */
        public final void changedUpdate(DocumentEvent e) {
            Rectangle alloc = (painted) ? getVisibleEditorRect() : null;
            rootView.changedUpdate(e, alloc, rootView.getViewFactory());
        }

	// --- Runnable methods --------------------------------------

	/**
	 * Weed out components that have an association with a View
	 * that have been removed.
	 */
        public void run() {
	    Vector toRemove = null;
	    Enumeration components = constraints.keys();
	    while (components.hasMoreElements()) {
		Component comp = (Component) components.nextElement();
		View v = (View) constraints.get(comp);
		if (v.getStartOffset() == v.getEndOffset()) {
		    // this view has been removed, its coordinates
		    // have collapsed down to the removal location.
		    if (toRemove == null) {
			toRemove = new Vector();
		    }
		    toRemove.addElement(comp);
		}
	    }
	    if (toRemove != null) {
		int n = toRemove.size();
		for (int i = 0; i < n; i++) {
		    editor.remove((Component) toRemove.elementAt(i));
		}
	    }
	}

	// --- LayoutManager2 methods --------------------------------

	/**
	 * Adds the specified component with the specified name to
	 * the layout.
	 * @param name the component name
	 * @param comp the component to be added
	 */
	public void addLayoutComponent(String name, Component comp) {
	    // not supported
	}

	/**
	 * Removes the specified component from the layout.
	 * @param comp the component to be removed
	 */
	public void removeLayoutComponent(Component comp) {
	    if (constraints != null) {
		// remove the constraint record
		constraints.remove(comp);
	    }
	}

	/**
	 * Calculates the preferred size dimensions for the specified 
	 * panel given the components in the specified parent container.
	 * @param parent the component to be laid out
	 *  
	 * @see #minimumLayoutSize
	 */
	public Dimension preferredLayoutSize(Container parent) {
	    // should not be called (JComponent uses UI instead)
	    return null;
	}

	/** 
	 * Calculates the minimum size dimensions for the specified 
	 * panel given the components in the specified parent container.
	 * @param parent the component to be laid out
	 * @see #preferredLayoutSize
	 */
	public Dimension minimumLayoutSize(Container parent) {
	    // should not be called (JComponent uses UI instead)
	    return null;
	}

	/** 
	 * Lays out the container in the specified panel.  This is
	 * implemented to position all components that were added
	 * with a View object as a constraint.  The current allocation
	 * of the associated View is used as the location of the 
	 * component.
	 * <p>
	 * A read-lock is acquired on the document to prevent the
	 * view tree from being modified while the layout process
	 * is active.
	 *
	 * @param parent the component which needs to be laid out 
	 */
	public void layoutContainer(Container parent) {
	    if ((constraints != null) && (! constraints.isEmpty())) {
		Rectangle alloc = getVisibleEditorRect();
		if (alloc != null) {
		    Document doc = editor.getDocument();
		    if (doc instanceof AbstractDocument) {
			((AbstractDocument)doc).readLock();
		    }
		    try {
			rootView.setSize(alloc.width, alloc.height);
			Enumeration components = constraints.keys();
			while (components.hasMoreElements()) {
			    Component comp = (Component) components.nextElement();
			    View v = (View) constraints.get(comp);
			    Shape ca = calculateViewPosition(alloc, v);
			    if (ca != null) {
				Rectangle compAlloc = (ca instanceof Rectangle) ? 
				    (Rectangle) ca : ca.getBounds();
				comp.setBounds(compAlloc);
			    }
			}
		    } finally {
			if (doc instanceof AbstractDocument) {
			    ((AbstractDocument)doc).readUnlock();
			}
		    }			
		}
	    }
	}

	/**
	 * Find the Shape representing the given view.
	 */
	Shape calculateViewPosition(Shape alloc, View v) {
	    int pos = v.getStartOffset();
	    View child = null;
	    for (View parent = rootView; (parent != null) && (parent != v); parent = child) {
		int index = parent.getViewIndex(pos, Position.Bias.Forward);
		alloc = parent.getChildAllocation(index, alloc);
		child = parent.getView(index);
	    }
	    return (child != null) ? alloc : null;
	}

	/**
	 * Adds the specified component to the layout, using the specified
	 * constraint object.  We only store those components that were added
	 * with a constraint that is of type View.
	 *
	 * @param comp the component to be added
	 * @param constraint  where/how the component is added to the layout.
	 */
	public void addLayoutComponent(Component comp, Object constraint) {
	    if (constraint instanceof View) {
		if (constraints == null) {
		    constraints = new Hashtable(7);
		}
		constraints.put(comp, constraint);
	    }
	}

	/** 
	 * Returns the maximum size of this component.
	 * @see java.awt.Component#getMinimumSize()
	 * @see java.awt.Component#getPreferredSize()
	 * @see LayoutManager
	 */
        public Dimension maximumLayoutSize(Container target) {
	    // should not be called (JComponent uses UI instead)
	    return null;
	}

	/**
	 * Returns the alignment along the x axis.  This specifies how
	 * the component would like to be aligned relative to other 
	 * components.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 */
        public float getLayoutAlignmentX(Container target) {
	    return 0.5f;
	}

	/**
	 * Returns the alignment along the y axis.  This specifies how
	 * the component would like to be aligned relative to other 
	 * components.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 */
        public float getLayoutAlignmentY(Container target) {
	    return 0.5f;
	}

	/**
	 * Invalidates the layout, indicating that if the layout manager
	 * has cached information it should be discarded.
	 */
        public void invalidateLayout(Container target) {
	}

	/**
	 * The "layout constraints" for the LayoutManager2 implementation.
	 * These are View objects for those components that are represented
	 * by a View in the View tree.
	 */
	private Hashtable constraints;

	private boolean i18nView = false;
    }


    /**
     * Registered in the ActionMap.
     */
    class FocusAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
	    editor.requestFocus();
        }

        public boolean isEnabled() {
            return editor.isEditable();
        }
    }
}

