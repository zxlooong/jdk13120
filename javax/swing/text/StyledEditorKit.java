/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.*;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;

/**
 * This is the set of things needed by a text component
 * to be a reasonably functioning editor for some <em>type</em>
 * of text document.  This implementation provides a default
 * implementation which treats text as styled text and 
 * provides a minimal set of actions for editing styled text.
 *
 * @author  Timothy Prinzing
 * @version 1.34 02/06/02
 */
public class StyledEditorKit extends DefaultEditorKit {

    /**
     * Gets the input attributes for the pane.  When
     * the caret moves and there is no selection, the
     * input attributes are automatically mutated to 
     * reflect the character attributes of the current
     * caret location.  The styled editing actions 
     * use the input attributes to carry out their 
     * actions.
     *
     * @return the attribute set
     */
    public MutableAttributeSet getInputAttributes() {
	return inputAttributes;
    }

    /**
     * Fetches the element representing the current
     * run of character attributes for the caret.
     *
     * @return the element
     */
    public Element getCharacterAttributeRun() {
	return currentRun;
    }

    // --- EditorKit methods ---------------------------

    /**
     * Create a copy of the editor kit.  This
     * allows an implementation to serve as a prototype
     * for others, so that they can be quickly created.
     *
     * @return the copy
     */
    public Object clone() {
	return new StyledEditorKit();
    }

    /**
     * Fetches the command list for the editor.  This is
     * the list of commands supported by the superclass
     * augmented by the collection of commands defined
     * locally for style operations.
     *
     * @return the command list
     */
    public Action[] getActions() {
	return TextAction.augmentList(super.getActions(), this.defaultActions);
    }
   
    /**
     * Creates an uninitialized text storage model
     * that is appropriate for this type of editor.
     *
     * @return the model
     */
    public Document createDefaultDocument() {
	return new DefaultStyledDocument();
    }

    /**
     * Called when the kit is being installed into
     * a JEditorPane. 
     *
     * @param c the JEditorPane
     */
    public void install(JEditorPane c) {
	c.addCaretListener(inputAttributeUpdater);
	c.addPropertyChangeListener(inputAttributeUpdater);
	Caret caret = c.getCaret();
	if (caret != null) {
	    inputAttributeUpdater.updateInputAttributes
		                  (caret.getDot(), caret.getMark(), c);
	}
    }

    /**
     * Called when the kit is being removed from the
     * JEditorPane.  This is used to unregister any 
     * listeners that were attached.
     *
     * @param c the JEditorPane
     */
    public void deinstall(JEditorPane c) {
	c.removeCaretListener(inputAttributeUpdater);
	c.removePropertyChangeListener(inputAttributeUpdater);

	// remove references to current document so it can be collected.
	currentRun = null;
	currentParagraph = null;
    }

   /**
     * Fetches a factory that is suitable for producing 
     * views of any models that are produced by this
     * kit.  This is implemented to return View implementations
     * for the following kinds of elements:
     * <ul>
     * <li>AbstractDocument.ContentElementName
     * <li>AbstractDocument.ParagraphElementName
     * <li>AbstractDocument.SectionElementName
     * <li>StyleConstants.ComponentElementName
     * <li>StyleConstants.IconElementName
     * </ul>
     *
     * @return the factory
     */
    public ViewFactory getViewFactory() {
	return defaultFactory;
    }

    private static final ViewFactory defaultFactory = new StyledViewFactory();

    Element currentRun;
    Element currentParagraph;

    /**
     * This is the set of attributes used to store the
     * input attributes.  
     */  
    MutableAttributeSet inputAttributes = new SimpleAttributeSet() {
        public AttributeSet getResolveParent() {
	    return (currentParagraph != null) ? currentParagraph.getAttributes() : null;
	}

	public Object clone() {
	    return new SimpleAttributeSet(this);
	}
    };

    /**
     * This listener will be attached to the caret of 
     * the text component that the EditorKit gets installed
     * into.  This should keep the input attributes updated
     * for use by the styled actions.
     */
    private AttributeTracker inputAttributeUpdater = new AttributeTracker();

    /**
     * Tracks caret movement and keeps the input attributes set 
     * to reflect the current set of attribute definitions at the 
     * caret position. 
     * <p>This implements PropertyChangeListener to update the
     * input attributes when the Document changes, as if the Document
     * changes the attributes will almost certainly change.
     */
    class AttributeTracker implements CaretListener, PropertyChangeListener, Serializable {

	/**
	 * Updates the attributes. <code>dot</code> and <code>mark</code>
	 * mark give the positions of the selection in <code>c</code>.
	 */
	void updateInputAttributes(int dot, int mark, JTextComponent c) {
	    // EditorKit might not have installed the StyledDocument yet.
	    Document aDoc = c.getDocument();
	    if (!(aDoc instanceof StyledDocument)) {
		return ;
	    }
	    int start = Math.min(dot, mark);
	    // record current character attributes.
	    StyledDocument doc = (StyledDocument)aDoc;
	    // If nothing is selected, get the attributes from the character
	    // before the start of the selection, otherwise get the attributes
	    // from the character element at the start of the selection.
	    Element run;
	    currentParagraph = doc.getParagraphElement(start);
	    if (currentParagraph.getStartOffset() == start || dot != mark) {
		// Get the attributes from the character at the selection
		// if in a different paragrah!
		run = doc.getCharacterElement(start);
	    }
	    else {
		run = doc.getCharacterElement(Math.max(start-1, 0));
	    }
	    if (run != currentRun) {
		    /*
		     * PENDING(prinz) All attributes that represent a single
		     * glyph position and can't be inserted into should be 
		     * removed from the input attributes... this requires 
		     * mixing in an interface to indicate that condition.  
		     * When we can add things again this logic needs to be
		     * improved!!
		     */ 
		currentRun = run;
		createInputAttributes(currentRun, getInputAttributes());
	    }
	}

        public void propertyChange(PropertyChangeEvent evt) {
	    Object newValue = evt.getNewValue();
	    Object source = evt.getSource();

	    if ((source instanceof JTextComponent) &&
		(newValue instanceof Document)) {
		// New document will have changed selection to 0,0.
		updateInputAttributes(0, 0, (JTextComponent)source);
	    }
	}

	public void caretUpdate(CaretEvent e) {
	    updateInputAttributes(e.getDot(), e.getMark(),
				  (JTextComponent)e.getSource());
	}
    }

    /**
     * Copies the key/values in <code>element</code>s AttributeSet into
     * <code>set</code>. This does not copy component, icon, or element
     * names attributes. Subclasses may wish to refine what is and what
     * isn't copied here. But be sure to first remove all the attributes that
     * are in <code>set</code>.<p>
     * This is called anytime the caret moves over a different location.
     *
     */
    protected void createInputAttributes(Element element,
					 MutableAttributeSet set) {
	set.removeAttributes(set);
	set.addAttributes(element.getAttributes());
	set.removeAttribute(StyleConstants.ComponentAttribute);
	set.removeAttribute(StyleConstants.IconAttribute);
	set.removeAttribute(AbstractDocument.ElementNameAttribute);
	set.removeAttribute(StyleConstants.ComposedTextAttribute);
    }

    // ---- default ViewFactory implementation ---------------------

    static class StyledViewFactory implements ViewFactory {

        public View create(Element elem) {
	    String kind = elem.getName();
	    if (kind != null) {
		if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
		} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
		    return new ParagraphView(elem);
		} else if (kind.equals(AbstractDocument.SectionElementName)) {
		    return new BoxView(elem, View.Y_AXIS);
		} else if (kind.equals(StyleConstants.ComponentElementName)) {
		    return new ComponentView(elem);
		} else if (kind.equals(StyleConstants.IconElementName)) {
		    return new IconView(elem);
		}
	    }
	
	    // default to text display
            return new LabelView(elem);
	}

    }

    // --- Action implementations ---------------------------------

    private static final Action[] defaultActions = {
	new FontFamilyAction("font-family-SansSerif", "SansSerif"),
	new FontFamilyAction("font-family-Monospaced", "Monospaced"),
	new FontFamilyAction("font-family-Serif", "Serif"),
	new FontSizeAction("font-size-8", 8),
	new FontSizeAction("font-size-10", 10),
	new FontSizeAction("font-size-12", 12),
	new FontSizeAction("font-size-14", 14),
	new FontSizeAction("font-size-16", 16),
	new FontSizeAction("font-size-18", 18),
	new FontSizeAction("font-size-24", 24),
	new FontSizeAction("font-size-36", 36),
	new FontSizeAction("font-size-48", 48),
	new AlignmentAction("left-justify", StyleConstants.ALIGN_LEFT),
	new AlignmentAction("center-justify", StyleConstants.ALIGN_CENTER),
	new AlignmentAction("right-justify", StyleConstants.ALIGN_RIGHT),
	new BoldAction(),
	new ItalicAction(),
	new UnderlineAction()
    };

    /**
     * An action that assumes it's being fired on a JEditorPane
     * with a StyledEditorKit (or subclass) installed.  This has
     * some convenience methods for causing character or paragraph
     * level attribute changes.  The convenience methods will 
     * throw an IllegalArgumentException if the assumption of
     * a StyledDocument, a JEditorPane, or a StyledEditorKit
     * fail to be true.
     * <p>
     * The component that gets acted upon by the action 
     * will be the source of the ActionEvent if the source
     * can be narrowed to a JEditorPane type.  If the source
     * can't be narrowed, the most recently focused text 
     * component is changed.  If neither of these are the
     * case, the action cannot be performed.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public abstract static class StyledTextAction extends TextAction {

        /**
         * Creates a new StyledTextAction from a string action name.
         *
         * @param nm the name of the action
         */
        public StyledTextAction(String nm) {
	    super(nm);
	}

        /**
         * Gets the target editor for an action.
         *
         * @param e the action event
         * @return the editor
         */
        protected final JEditorPane getEditor(ActionEvent e) {
	    JTextComponent tcomp = getTextComponent(e);
	    if (tcomp instanceof JEditorPane) {
		return (JEditorPane) tcomp;
	    }
	    return null;
	}

        /**
         * Gets the document associated with an editor pane.
         *
         * @param e the editor
         * @return the document
         * @exception IllegalArgumentException for the wrong document type
         */
        protected final StyledDocument getStyledDocument(JEditorPane e) {
	    Document d = e.getDocument();
	    if (d instanceof StyledDocument) {
		return (StyledDocument) d;
	    }
	    throw new IllegalArgumentException("document must be StyledDocument");
	}

        /**
         * Gets the editor kit associated with an editor pane.
         *
         * @param e the editor pane
         * @return the kit
         * @exception IllegalArgumentException for the wrong document type
         */
        protected final StyledEditorKit getStyledEditorKit(JEditorPane e) {
	    EditorKit k = e.getEditorKit();
	    if (k instanceof StyledEditorKit) {
		return (StyledEditorKit) k;
	    }
	    throw new IllegalArgumentException("EditorKit must be StyledEditorKit");
	}

	/**
	 * Applies the given attributes to character 
	 * content.  If there is a selection, the attributes
	 * are applied to the selection range.  If there
	 * is no selection, the attributes are applied to
	 * the input attribute set which defines the attributes
	 * for any new text that gets inserted.
	 *
         * @param editor the editor
	 * @param attr the attributes
	 * @param replace   if true, then replace the existing attributes first
	 */
        protected final void setCharacterAttributes(JEditorPane editor, 
					      AttributeSet attr, boolean replace) {
	    int p0 = editor.getSelectionStart();
	    int p1 = editor.getSelectionEnd();
	    if (p0 != p1) {
		StyledDocument doc = getStyledDocument(editor);
		doc.setCharacterAttributes(p0, p1 - p0, attr, replace);
	    }
	    StyledEditorKit k = getStyledEditorKit(editor);
	    MutableAttributeSet inputAttributes = k.getInputAttributes();
	    if (replace) {
		inputAttributes.removeAttributes(inputAttributes);
	    }
	    inputAttributes.addAttributes(attr);
	}

	/**
	 * Applies the given attributes to paragraphs.  If
	 * there is a selection, the attributes are applied
	 * to the paragraphs that intersect the selection.
	 * if there is no selection, the attributes are applied
	 * to the paragraph at the current caret position.
	 *
         * @param editor the editor
	 * @param attr the attributes
	 * @param replace   if true, replace the existing attributes first
	 */
        protected final void setParagraphAttributes(JEditorPane editor, 
					   AttributeSet attr, boolean replace) {
	    int p0 = editor.getSelectionStart();
	    int p1 = editor.getSelectionEnd();
	    StyledDocument doc = getStyledDocument(editor);
	    doc.setParagraphAttributes(p0, p1 - p0, attr, replace);
	}

    }

    /**
     * An action to set the font family in the associated
     * JEditorPane.  This will use the family specified as
     * the command string on the ActionEvent if there is one,
     * otherwise the family that was initialized with will be used.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class FontFamilyAction extends StyledTextAction {

        /**
         * Creates a new FontFamilyAction.
         *
         * @param nm the action name
         * @param family the font family
         */
	public FontFamilyAction(String nm, String family) {
	    super(nm);
	    this.family = family;
	}

        /**
         * Sets the font family.
         *
         * @param e the event
         */
        public void actionPerformed(ActionEvent e) {
	    JEditorPane editor = getEditor(e);
	    if (editor != null) {
		String family = this.family;
		if ((e != null) && (e.getSource() == editor)) {
		    String s = e.getActionCommand();
		    if (s != null) {
			family = s;
		    }
		}
		if (family != null) {
		    MutableAttributeSet attr = new SimpleAttributeSet();
		    StyleConstants.setFontFamily(attr, family);
		    setCharacterAttributes(editor, attr, false);
		} else {
		    Toolkit.getDefaultToolkit().beep();
		}
	    }
	}

	private String family;
    }

    /**
     * An action to set the font size in the associated
     * JEditorPane.  This will use the size specified as
     * the command string on the ActionEvent if there is one,
     * otherwise the size that was initialized with will be used.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class FontSizeAction extends StyledTextAction {

        /**
         * Creates a new FontSizeAction.
         *
         * @param nm the action name
         * @param size the font size
         */
	public FontSizeAction(String nm, int size) {
	    super(nm);
	    this.size = size;
	}

        /**
         * Sets the font size.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e) {
	    JEditorPane editor = getEditor(e);
	    if (editor != null) {
		int size = this.size;
		if ((e != null) && (e.getSource() == editor)) {
		    String s = e.getActionCommand();
		    try {
			size = Integer.parseInt(s, 10);
		    } catch (NumberFormatException nfe) {
		    }
		}
		if (size != 0) {
		    MutableAttributeSet attr = new SimpleAttributeSet();
		    StyleConstants.setFontSize(attr, size);
		    setCharacterAttributes(editor, attr, false);
		} else {
		    Toolkit.getDefaultToolkit().beep();
		}
	    }
	}

	private int size;
    }

    /**
     * An action to set foreground color.  This sets the 
     * <code>StyleConstants.Foreground</code> attribute for the
     * currently selected range of the target JEditorPane.
     * This is done by calling 
     * <code>StyledDocument.setCharacterAttributes</code>
     * on the styled document associated with the target
     * JEditorPane.
     * <p>
     * If the target text component is specified as the
     * source of the ActionEvent and there is a command string,
     * the command string will be interpreted as the foreground
     * color.  It will be interpreted by called 
     * <code>Color.decode</code>, and should therefore be
     * legal input for that method.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class ForegroundAction extends StyledTextAction {

        /**
         * Creates a new ForegroundAction.
         *
         * @param nm the action name
         * @param fg the foreground color
         */
	public ForegroundAction(String nm, Color fg) {
	    super(nm);
	    this.fg = fg;
	}

        /**
         * Sets the foreground color.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e) {
	    JEditorPane editor = getEditor(e);
	    if (editor != null) {
		Color fg = this.fg;
		if ((e != null) && (e.getSource() == editor)) {
		    String s = e.getActionCommand();
		    try {
			fg = Color.decode(s);
		    } catch (NumberFormatException nfe) {
		    }
		}
		if (fg != null) {
		    MutableAttributeSet attr = new SimpleAttributeSet();
		    StyleConstants.setForeground(attr, fg);
		    setCharacterAttributes(editor, attr, false);
		} else {
		    Toolkit.getDefaultToolkit().beep();
		}
	    }
	}

	private Color fg;
    }

    /**
     * An action to set paragraph alignment.  This sets the 
     * <code>StyleConstants.Alignment</code> attribute for the
     * currently selected range of the target JEditorPane.
     * This is done by calling 
     * <code>StyledDocument.setParagraphAttributes</code>
     * on the styled document associated with the target
     * JEditorPane.
     * <p>
     * If the target text component is specified as the
     * source of the ActionEvent and there is a command string,
     * the command string will be interpreted as an integer
     * that should be one of the legal values for the
     * <code>StyleConstants.Alignment</code> attribute.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class AlignmentAction extends StyledTextAction {

        /**
         * Creates a new AlignmentAction.
         *
         * @param nm the action name
         * @param a the alignment >= 0
         */
	public AlignmentAction(String nm, int a) {
	    super(nm);
	    this.a = a;
	}

        /**
         * Sets the alignment.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e) {
	    JEditorPane editor = getEditor(e);
	    if (editor != null) {
		int a = this.a;
		if ((e != null) && (e.getSource() == editor)) {
		    String s = e.getActionCommand();
		    try {
			a = Integer.parseInt(s, 10);
		    } catch (NumberFormatException nfe) {
		    }
		}
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setAlignment(attr, a);
		setParagraphAttributes(editor, attr, false);
	    }
	}

	private int a;
    }

    /**
     * An action to toggle the bold attribute.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class BoldAction extends StyledTextAction {

        /**
         * Constructs a new BoldAction.
         */
	public BoldAction() {
	    super("font-bold");
	}

        /**
         * Toggles the bold attribute.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e) {
	    JEditorPane editor = getEditor(e);
	    if (editor != null) {
		StyledEditorKit kit = getStyledEditorKit(editor);
		MutableAttributeSet attr = kit.getInputAttributes();
		boolean bold = (StyleConstants.isBold(attr)) ? false : true;
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setBold(sas, bold);
		setCharacterAttributes(editor, sas, false);
	    }
	}
    }

    /**
     * An action to toggle the italic attribute.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class ItalicAction extends StyledTextAction {

        /**
         * Constructs a new ItalicAction.
         */
	public ItalicAction() {
	    super("font-italic");
	}

        /**
         * Toggles the italic attribute.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e) {
	    JEditorPane editor = getEditor(e);
	    if (editor != null) {
		StyledEditorKit kit = getStyledEditorKit(editor);
		MutableAttributeSet attr = kit.getInputAttributes();
		boolean italic = (StyleConstants.isItalic(attr)) ? false : true;
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setItalic(sas, italic);
		setCharacterAttributes(editor, sas, false);
	    }
	}
    }

    /**
     * An action to toggle the underline attribute.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class UnderlineAction extends StyledTextAction {

        /**
         * Constructs a new UnderlineAction.
         */
	public UnderlineAction() {
	    super("font-underline");
	}

        /**
         * Toggles the Underline attribute.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e) {
	    JEditorPane editor = getEditor(e);
	    if (editor != null) {
		StyledEditorKit kit = getStyledEditorKit(editor);
		MutableAttributeSet attr = kit.getInputAttributes();
		boolean underline = (StyleConstants.isUnderline(attr)) ? false : true;
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setUnderline(sas, underline);
		setCharacterAttributes(editor, sas, false);
	    }
	}
    }

}
