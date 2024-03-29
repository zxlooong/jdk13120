/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import java.lang.reflect.Method;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;

/**
 * The Swing JEditorPane text component supports different kinds
 * of content via a plug-in mechanism called an EditorKit.  Because
 * HTML is a very popular format of content, some support is provided
 * by default.  The default support is provided by this class, which
 * supports HTML version 3.2 (with some extensions), and is migrating 
 * toward version 4.0.
 * The &lt;applet&gt; tag is not supported, but some support is provided
 * for the &lt;object&gt; tag.
 * <p>
 * There are several goals of the HTML EditorKit provided, that have
 * an effect upon the way that HTML is modeled.  These
 * have influenced its design in a substantial way.  
 * <dl>
 * <p>
 * <dt>
 * Support editing
 * <dd>
 * It might seem fairly obvious that a plug-in for JEditorPane
 * should provide editing support, but that fact has several
 * design considerations.  There are a substantial number of HTML
 * documents that don't properly conform to an HTML specification.
 * These must be normalized somewhat into a correct form if one
 * is to edit them.  Additionally, users don't like to be presented
 * with an excessive amount of structure editing, so using traditional
 * text editing gestures is preferred over using the HTML structure 
 * exactly as defined in the HTML document.
 * <p>
 * The modeling of HTML is provided by the class <code>HTMLDocument</code>.
 * Its documention describes the details of how the HTML is modeled.
 * The editing support leverages heavily off of the text package.
 * <p>
 * <dt>
 * Extendable/Scalable
 * <dd>
 * To maximize the usefulness of this kit, a great deal of effort
 * has gone into making it extendable.  These are some of the
 * features.
 * <ol>
 *   <li>
 *   The parser is replacable.  The default parser is the Hot Java
 *   parser which is DTD based.  A different DTD can be used, or an
 *   entirely different parser can be used.  To change the parser,
 *   reimplement the getParser method.  The default parser is 
 *   dynamically loaded when first asked for, so the class files
 *   will never be loaded if an alternative parser is used.  The
 *   default parser is in a seperate package called parser below
 *   this package.
 *   <li>
 *   The parser drives the ParserCallback, which is provided by
 *   HTMLDocument.  To change the callback, subclass HTMLDocument
 *   and reimplement the createDefaultDocument method to return
 *   document that produces a different reader.  The reader controls
 *   how the document is structured.  Although the Document provides
 *   HTML support by default, there is nothing preventing support of
 *   non-HTML tags that result in alternative element structures.
 *   <li>
 *   The default view of the models are provided as a hierarchy of
 *   View implementations, so one can easily customize how a particular
 *   element is displayed or add capabilities for new kinds of elements
 *   by providing new View implementations.  The default set of views
 *   are provided by the <code>HTMLFactory</code> class.  This can
 *   be easily changed by subclassing or replacing the HTMLFactory 
 *   and reimplementing the getViewFactory method to return the alternative
 *   factory.
 *   <li>
 *   The View implementations work primarily off of CSS attributes, 
 *   which are kept in the views.  This makes it possible to have
 *   multiple views mapped over the same model that appear substantially
 *   different.  This can be especially useful for printing.  For
 *   most HTML attributes, the HTML attributes are converted to CSS
 *   attributes for display.  This helps make the View implementations
 *   more general purpose
 * </ol>
 * <p>
 * <dt>
 * Asynchronous Loading
 * <dd>
 * Larger documents involve a lot of parsing and take some time
 * to load.  By default, this kit produces documents that will be
 * loaded asynchronously if loaded using <code>JEditorPane.setPage</code>.
 * This is controlled by a property on the document.  The method
 * <a href="#createDefaultDocument">createDefaultDocument</a> can
 * be overriden to change this.  The batching of work is done
 * by the <code>HTMLDocument.HTMLReader</code> class.  The actual
 * work is done by the <code>DefaultStyledDocument</code> and
 * <code>AbstractDocument</code> classes in the text package.
 * <p>
 * <dt>
 * Customization from current LAF
 * <dd>
 * HTML provides a well known set of features without exactly
 * specifying the display characteristics.  Swing has a theme
 * mechanism for its look-and-feel implementations.  It is desirable
 * for the look-and-feel to feed display characteristics into the
 * HTML views.  An user with poor vision for example would want
 * high contrast and larger than typical fonts.
 * <p>
 * The support for this is provided by the <code>StyleSheet</code>
 * class.  The presentation of the HTML can be heavily influenced
 * by the setting of the StyleSheet property on the EditorKit.
 * <p>
 * <dt>
 * Not lossy
 * <dd>
 * An EditorKit has the ability to be read and save documents.
 * It is generally the most pleasing to users if there is no loss
 * of data between the two operation.  The policy of the HTMLEditorKit
 * will be to store things not recognized or not necessarily visible
 * so they can be subsequently written out.  The model of the HTML document
 * should therefore contain all information discovered while reading the
 * document.  This is constrained in some ways by the need to support 
 * editing (i.e. incorrect documents sometimes must be normalized).
 * The guiding principle is that information shouldn't be lost, but
 * some might be synthesized to produce a more correct model or it might
 * be rearranged.
 * </dl>
 *
 * @author  Timothy Prinzing
 * @version 1.99 02/06/02
 */
public class HTMLEditorKit extends StyledEditorKit {
   
    /**
     * Constructs an HTMLEditorKit, creates a StyleContext,
     * and loads the style sheet.
     */
    public HTMLEditorKit() {

    }

    /**
     * Create a copy of the editor kit.  This
     * allows an implementation to serve as a prototype
     * for others, so that they can be quickly created.
     *
     * @return the copy
     */
    public Object clone() {
	return new HTMLEditorKit();
    }

    /**
     * Get the MIME type of the data that this
     * kit represents support for.  This kit supports
     * the type <code>text/html</code>.
     *
     * @return the type
     */
    public String getContentType() {
	return "text/html";
    }

    /**
     * Fetch a factory that is suitable for producing 
     * views of any models that are produced by this
     * kit.  
     *
     * @return the factory
     */
    public ViewFactory getViewFactory() {
	return defaultFactory;
    }

    /**
     * Create an uninitialized text storage model
     * that is appropriate for this type of editor.
     *
     * @return the model
     */
    public Document createDefaultDocument() {
	StyleSheet styles = getStyleSheet();
	StyleSheet ss = new StyleSheet();

	ss.addStyleSheet(styles);

	HTMLDocument doc = new HTMLDocument(ss);
	doc.setParser(getParser());
	doc.setAsynchronousLoadPriority(4);
	doc.setTokenThreshold(100);
	return doc;
    }

    /**
     * Inserts content from the given stream. If <code>doc</code> is
     * an instance of HTMLDocument, this will read
     * HTML 3.2 text. Inserting HTML into a non-empty document must be inside
     * the body Element, if you do not insert into the body an exception will
     * be thrown. When inserting into a non-empty document all tags outside
     * of the body (head, title) will be dropped.
     * 
     * @param in  the stream to read from
     * @param doc the destination for the insertion
     * @param pos the location in the document to place the
     *   content
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *   location within the document
     * @exception RuntimeException (will eventually be a BadLocationException)
     *            if pos is invalid
     */
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {

	if (doc instanceof HTMLDocument) {
	    HTMLDocument hdoc = (HTMLDocument) doc;
	    Parser p = getParser();
	    if (p == null) {
		throw new IOException("Can't load parser");
	    }
	    if (pos > doc.getLength()) {
		throw new BadLocationException("Invalid location", pos);
	    }

	    ParserCallback receiver = hdoc.getReader(pos);
	    Boolean ignoreCharset = (Boolean)doc.getProperty("IgnoreCharsetDirective");
	    p.parse(in, receiver, (ignoreCharset == null) ? false : ignoreCharset.booleanValue());
	    receiver.flush();
	} else {
	    super.read(in, doc, pos);
	}
    }

    /**
     * Inserts HTML into an existing document.
     *
     * @param doc       the document to insert into
     * @param offset    the offset to insert HTML at
     * @param popDepth  the number of ElementSpec.EndTagTypes to generate before
     *        inserting
     * @param pushDepth the number of ElementSpec.StartTagTypes with a direction
     *        of ElementSpec.JoinNextDirection that should be generated
     *        before inserting, but after the end tags have been generated
     * @param insertTag the first tag to start inserting into document
     * @exception RuntimeException (will eventually be a BadLocationException)
     *            if pos is invalid
     */
    public void insertHTML(HTMLDocument doc, int offset, String html,
			   int popDepth, int pushDepth,
			   HTML.Tag insertTag) throws
	               BadLocationException, IOException {
	Parser p = getParser();
	if (p == null) {
	    throw new IOException("Can't load parser");
	}
	if (offset > doc.getLength()) {
	    throw new BadLocationException("Invalid location", offset);
	}

	ParserCallback receiver = doc.getReader(offset, popDepth, pushDepth,
						insertTag);
	Boolean ignoreCharset = (Boolean)doc.getProperty
	                        ("IgnoreCharsetDirective");
	p.parse(new StringReader(html), receiver, (ignoreCharset == null) ?
		false : ignoreCharset.booleanValue());
	receiver.flush();
    }

    /**
     * Write content from a document to the given stream
     * in a format appropriate for this kind of content handler.
     * 
     * @param out  the stream to write to
     * @param doc  the source for the write
     * @param pos  the location in the document to fetch the
     *   content
     * @param len  the amount to write out
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *   location within the document
     */
    public void write(Writer out, Document doc, int pos, int len) 
	throws IOException, BadLocationException {

	if (doc instanceof HTMLDocument) {
	    HTMLWriter w = new HTMLWriter(out, (HTMLDocument)doc, pos, len);
	    w.write();
	} else if (doc instanceof StyledDocument) {
	    MinimalHTMLWriter w = new MinimalHTMLWriter(out, (StyledDocument)doc, pos, len);
	    w.write();
	} else {
	    super.write(out, doc, pos, len);
	}
    }

    /**
     * Called when the kit is being installed into the
     * a JEditorPane. 
     *
     * @param c the JEditorPane
     */
    public void install(JEditorPane c) {
	c.addMouseListener(linkHandler);
        c.addMouseMotionListener(linkHandler);
	super.install(c);
    }

    /**
     * Called when the kit is being removed from the
     * JEditorPane.  This is used to unregister any 
     * listeners that were attached.
     *
     * @param c the JEditorPane
     */
    public void deinstall(JEditorPane c) {
	c.removeMouseListener(linkHandler);
        c.removeMouseMotionListener(linkHandler);
	super.deinstall(c);
    }

    /**
     * Default Cascading Style Sheet file that sets
     * up the tag views.
     */
    public static final String DEFAULT_CSS = "default.css";

    /**
     * Set the set of styles to be used to render the various
     * HTML elements.  These styles are specified in terms of
     * CSS specifications.  Each document produced by the kit
     * will have a copy of the sheet which it can add the 
     * document specific styles to.  By default, the StyleSheet
     * specified is shared by all HTMLEditorKit instances.
     * This should be reimplemented to provide a finer granularity
     * if desired.
     */
    public void setStyleSheet(StyleSheet s) {
	defaultStyles = s;
    }

    /**
     * Get the set of styles currently being used to render the
     * HTML elements.  By default the resource specified by
     * DEFAULT_CSS gets loaded, and is shared by all HTMLEditorKit 
     * instances.
     */
    public StyleSheet getStyleSheet() {
	if (defaultStyles == null) {
	    defaultStyles = new StyleSheet();
	    try {
		InputStream is = HTMLEditorKit.getResourceAsStream(DEFAULT_CSS);
		Reader r = new BufferedReader(new InputStreamReader(is));
		defaultStyles.loadRules(r, null);
		r.close();
	    } catch (Throwable e) {
		// on error we simply have no styles... the html
		// will look mighty wrong but still function.
	    }
	}
	return defaultStyles;
    }
    
    /**
     * Fetch a resource relative to the HTMLEditorKit classfile.
     * If this is called on 1.2 the loading will occur under the
     * protection of a doPrivileged call to allow the HTMLEditorKit
     * to function when used in an applet.
     *
     * @param name the name of the resource, relative to the
     *  HTMLEditorKit class
     * @returns a stream representing the resource
     */
    static InputStream getResourceAsStream(String name) {
	try {
	    Class klass;
	    ClassLoader loader = HTMLEditorKit.class.getClassLoader();
	    if (loader != null) {
		klass = loader.loadClass("javax.swing.text.html.ResourceLoader");
	    } else {
		klass = Class.forName("javax.swing.text.html.ResourceLoader");
	    }
	    Class[] parameterTypes = { String.class };
	    Method loadMethod = klass.getMethod("getResourceAsStream", parameterTypes);
	    String[] args = { name };
	    return (InputStream) loadMethod.invoke(null, args);
	} catch (Throwable e) {
	    // If the class doesn't exist or we have some other 
	    // problem we just try to call getResourceAsStream directly.
	    return HTMLEditorKit.class.getResourceAsStream(name);
	}
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
	set.removeAttribute(StyleConstants.ComposedTextAttribute);

	Object o = set.getAttribute(StyleConstants.NameAttribute);
	if (o instanceof HTML.Tag) {
	    HTML.Tag tag = (HTML.Tag)o;
	    // PENDING: we need a better way to express what shouldn't be
	    // copied when editing...
	    if(tag == HTML.Tag.IMG) {
		// Remove the related image attributes, src, width, height
		set.removeAttribute(HTML.Attribute.SRC);
		set.removeAttribute(HTML.Attribute.HEIGHT);
		set.removeAttribute(HTML.Attribute.WIDTH);
		set.addAttribute(StyleConstants.NameAttribute,
				 HTML.Tag.CONTENT);
	    }
	    else if (tag == HTML.Tag.HR || tag == HTML.Tag.BR) {
		// Don't copy HR's or BR's either.
		set.addAttribute(StyleConstants.NameAttribute,
				 HTML.Tag.CONTENT);
	    }
	    else if (tag == HTML.Tag.COMMENT) {
		// Don't copy COMMENTs either
		set.addAttribute(StyleConstants.NameAttribute,
				 HTML.Tag.CONTENT);
		set.removeAttribute(HTML.Attribute.COMMENT);
	    }
	    else if (tag == HTML.Tag.INPUT) {
		// or INPUT either
		set.addAttribute(StyleConstants.NameAttribute,
				 HTML.Tag.CONTENT);
		set.removeAttribute(HTML.Tag.INPUT);
	    }
	    else if (tag instanceof HTML.UnknownTag) {
		// Don't copy unknowns either:(
		set.addAttribute(StyleConstants.NameAttribute,
				 HTML.Tag.CONTENT);
		set.removeAttribute(HTML.Attribute.ENDTAG);
	    }
	}
    }

    /**
     * Gets the input attributes used for the styled 
     * editing actions.
     *
     * @return the attribute set
     */
    public MutableAttributeSet getInputAttributes() {
	if (input == null) {
	    input = getStyleSheet().addStyle(null, null);
	}
	return input;
    }

    /**
     * Sets the default cursor.
     *
     * @since 1.3
     */
    public void setDefaultCursor(Cursor cursor) {
	defaultCursor = cursor;
    }

    /**
     * Returns the default cursor.
     *
     * @since 1.3
     */
    public Cursor getDefaultCursor() {
	return defaultCursor;
    }

    /**
     * Sets the cursor to use over links.
     *
     * @since 1.3
     */
    public void setLinkCursor(Cursor cursor) {
	linkCursor = cursor;
    }

    /**
     * Returns the cursor to use over hyper links.
     */
    public Cursor getLinkCursor() {
	return linkCursor;
    }

    /**
     * Fetch the parser to use for reading HTML streams.
     * This can be reimplemented to provide a different
     * parser.  The default implementation is loaded dynamically
     * to avoid the overhead of loading the default parser if
     * it's not used.  The default parser is the HotJava parser
     * using an HTML 3.2 DTD.
     */
    protected Parser getParser() {
	if (defaultParser == null) {
	    try {
                Class c = Class.forName("javax.swing.text.html.parser.ParserDelegator");
                defaultParser = (Parser) c.newInstance();
	    } catch (Throwable e) {
	    }
	}
	return defaultParser;
    }

    // --- variables ------------------------------------------

    private static final Cursor MoveCursor = Cursor.getPredefinedCursor
	                            (Cursor.HAND_CURSOR);
    private static final Cursor DefaultCursor = Cursor.getPredefinedCursor
	                            (Cursor.DEFAULT_CURSOR);

    /** Shared factory for creating HTML Views. */
    private static final ViewFactory defaultFactory = new HTMLFactory();

    MutableAttributeSet input;
    private static StyleSheet defaultStyles = null;
    private LinkController linkHandler = new LinkController();
    private static Parser defaultParser = null;
    private Cursor defaultCursor = DefaultCursor;
    private Cursor linkCursor = MoveCursor;


    /**
     * Class to watch the associated component and fire
     * hyperlink events on it when appropriate.
     */
    public static class LinkController extends MouseAdapter implements MouseMotionListener, Serializable {
        private Element curElem = null;
	private String href = null;
	/** This is used by viewToModel to avoid allocing a new array each
	 * time. */
	private Position.Bias[] bias = new Position.Bias[1];

	/**
         * Called for a mouse click event.
	 * If the component is read-only (ie a browser) then 
	 * the clicked event is used to drive an attempt to
	 * follow the reference specified by a link.
	 *
	 * @param e the mouse event
	 * @see MouseListener#mouseClicked
	 */
        public void mouseClicked(MouseEvent e) {
	    JEditorPane editor = (JEditorPane) e.getSource();

	    if (! editor.isEditable()) {
		Point pt = new Point(e.getX(), e.getY());
		int pos = editor.viewToModel(pt);
		if (pos >= 0) {
		    activateLink(pos, editor, e.getX(), e.getY());
		}
	    }
	}

        // ignore the drags
        public void mouseDragged(MouseEvent e) {
        }

        // track the moving of the mouse.
        public void mouseMoved(MouseEvent e) {
            JEditorPane editor = (JEditorPane) e.getSource();
	    HTMLEditorKit kit = (HTMLEditorKit)editor.getEditorKit();
	    boolean adjustCursor = true;
	    Cursor newCursor = kit.getDefaultCursor();
            if (!editor.isEditable()) {
                Point pt = new Point(e.getX(), e.getY());
                int pos = editor.getUI().viewToModel(editor, pt, bias);
		if (bias[0] == Position.Bias.Backward && pos > 0) {
		    pos--;
		}
                if (pos >= 0) {
                    Document doc = editor.getDocument();
                    if (doc instanceof HTMLDocument) {
                        HTMLDocument hdoc = (HTMLDocument) doc;
                        Element elem = hdoc.getCharacterElement(pos);
			if (curElem != elem) {
			    curElem = elem;
			    AttributeSet a = elem.getAttributes();
			    AttributeSet anchor = (AttributeSet) a.getAttribute(HTML.Tag.A);
			    String href = (anchor != null) ? 
				(String) anchor.getAttribute(HTML.Attribute.HREF) 
				: null;
			    
			    if (href != this.href) {
				// reference changed, fire event(s)
				fireEvents(editor, hdoc, href);
				this.href = href;
				if (href != null) {
				    newCursor = kit.getLinkCursor();
				}
			    }
                        }
			else {
			    adjustCursor = false;
			}
                    }
                }
            }
	    if (adjustCursor && editor.getCursor() != newCursor) {
		editor.setCursor(newCursor);
	    }
        }

	/**
	 * Calls linkActivated on the associated JEditorPane
	 * if the given position represents a link.<p>This is implemented
	 * to forward to the method with the same name, but with the following
	 * args both == -1.
         *
         * @param pos the position
         * @param html the editor pane
	 */
	protected void activateLink(int pos, JEditorPane editor) {
	    activateLink(pos, editor, -1, -1);
	}

	/**
	 * Calls linkActivated on the associated JEditorPane
	 * if the given position represents a link. If this was the result
	 * of a mouse click, <code>x</code> and
	 * <code>y</code> will give the location of the mouse, otherwise
	 * they will be < 0.
         *
         * @param pos the position
         * @param html the editor pane
	 */
        void activateLink(int pos, JEditorPane html, int x, int y) {
	    Document doc = html.getDocument();
	    if (doc instanceof HTMLDocument) {
		HTMLDocument hdoc = (HTMLDocument) doc;
		Element e = hdoc.getCharacterElement(pos);
		AttributeSet a = e.getAttributes();
		AttributeSet anchor = (AttributeSet) a.getAttribute(HTML.Tag.A);
		String href = (anchor != null) ? 
		    (String) anchor.getAttribute(HTML.Attribute.HREF) : null;
		HyperlinkEvent linkEvent = null;

		if (href != null) {
		    linkEvent = createHyperlinkEvent(html, hdoc, href,
						     anchor);
		}
		else if (x >= 0 && y >= 0) {
		    // Check for usemap.
		    Object useMap = a.getAttribute(HTML.Attribute.USEMAP);
		    if (useMap != null && (useMap instanceof String)) {
			Map m = hdoc.getMap((String)useMap);
			if (m != null) {
			    Rectangle bounds;
			    try {
				bounds = html.modelToView(pos);
				Rectangle rBounds = html.modelToView(pos + 1);
				if (bounds != null && rBounds != null) {
				    bounds.union(rBounds);
				}
			    } catch (BadLocationException ble) {
				bounds = null;
			    }
			    if (bounds != null) {
				AttributeSet area = m.getArea
				           (x - bounds.x, y - bounds.y,
					    bounds.width, bounds.height);
				if (area != null) {
				    href = (String)area.getAttribute
					           (HTML.Attribute.HREF);
				    if (href != null) {
					linkEvent = createHyperlinkEvent(html,
						      hdoc, href, anchor);

				    }
				}
			    }
			}
		    }
		}
		if (linkEvent != null) {
		    html.fireHyperlinkUpdate(linkEvent);
		}
	    }
	}

	/**
	 * Creates and returns a new instance of HyperlinkEvent. If
	 * <code>hdoc</code> is a frame document a HTMLFrameHyperlinkEvent
	 * will be created.
	 */
	HyperlinkEvent createHyperlinkEvent(JEditorPane html,
					    HTMLDocument hdoc, String href,
					    AttributeSet anchor) {
	    URL u;
	    try {
		URL base = hdoc.getBase();
		u = new URL(base, href);
		// Following is a workaround for 1.2, in which
		// new URL("file://...", "#...") causes the filename to
		// be lost.
		if (href != null && "file".equals(u.getProtocol()) &&
		    href.startsWith("#")) {
		    String baseFile = base.getFile();
		    String newFile = u.getFile();
		    if (baseFile != null && newFile != null &&
			!newFile.startsWith(baseFile)) {
			u = new URL(base, baseFile + href);
		    }
		}
	    } catch (MalformedURLException m) {
		u = null;
	    }
	    HyperlinkEvent linkEvent = null;

	    if (!hdoc.isFrameDocument()) {
		linkEvent = new HyperlinkEvent(html, HyperlinkEvent.EventType.
					       ACTIVATED, u, href);
	    } else {
		String target = (anchor != null) ?
		    (String)anchor.getAttribute(HTML.Attribute.TARGET) : null;
		if ((target == null) || (target.equals(""))) {
		    target = "_self";
		}
		linkEvent = new HTMLFrameHyperlinkEvent(html, HyperlinkEvent.
					EventType.ACTIVATED, u, href, target);
	    }
	    return linkEvent;
	}

	void fireEvents(JEditorPane editor, HTMLDocument doc, String href) {
	    if (this.href != null) {
		// fire an exited event on the old link
		URL u;
		try {
		    u = new URL(doc.getBase(), this.href);
		} catch (MalformedURLException m) {
		    u = null;
		}
		HyperlinkEvent exit = new HyperlinkEvent(editor,
							 HyperlinkEvent.EventType.EXITED,
							 u, this.href);
		editor.fireHyperlinkUpdate(exit);
	    }
	    if (href != null) {
		// fire an entered event on the new link
		URL u;
		try {
		    u = new URL(doc.getBase(), href);
		} catch (MalformedURLException m) {
		    u = null;
		}
		HyperlinkEvent entered = new HyperlinkEvent(editor,
							    HyperlinkEvent.EventType.ENTERED,
							    u, href);
		editor.fireHyperlinkUpdate(entered);
	    }
	}
    }

    /**
     * Interface to be supported by the parser.  This enables
     * providing a different parser while reusing some of the
     * implementation provided by this editor kit.
     */
    public static abstract class Parser {
	/**
	 * Parse the given stream and drive the given callback 
	 * with the results of the parse.  This method should
	 * be implemented to be thread-safe.
	 */
	public abstract void parse(Reader r, ParserCallback cb, boolean ignoreCharSet) throws IOException;

    }

    /**
     * The result of parsing drives these callback methods.
     * The open and close actions should be balanced.  The
     * <code>flush</code> method will be the last method
     * called, to give the receiver a chance to flush any
     * pending data into the document.
     * <p>Refer to DocumentParser, the default parser used, for further
     * information on the contents of the AttributeSets, the positions, and
     * other info.
     *
     * @see javax.swing.text.html.parser.DocumentParser
     */
    public static class ParserCallback {
	/**
	 * This is passed as an attribute in the attributeset to indicate
	 * the element is implied eg, the string '&lt;&gt;foo&lt;\t&gt;' 
	 * contains an implied html element and an implied body element.
	 *
	 * @since 1.3
	 */
	public static final Object IMPLIED = "_implied_";


        public void flush() throws BadLocationException {
	}

        public void handleText(char[] data, int pos) {
	}

        public void handleComment(char[] data, int pos) {
	}

	public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
	}

	public void handleEndTag(HTML.Tag t, int pos) {
	}

	public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
	}

	public void handleError(String errorMsg, int pos){
	}

	/**
	 * This is invoked after the stream has been parsed, but before
	 * <code>flush</code>. <code>eol</code> will be one of \n, \r
	 * or \r\n, which ever is encountered the most in parsing the
	 * stream.
	 *
	 * @since 1.3
	 */
	public void handleEndOfLineString(String eol) {
	}
    }

    /**
     * A factory to build views for HTML.  The following 
     * table describes what this factory will build by
     * default.
     *
     * <table>
     * <tr>
     * <th align=left>Tag<th align=left>View created
     * </tr><tr>
     * <td>HTML.Tag.CONTENT<td>InlineView
     * </tr><tr>
     * <td>HTML.Tag.IMPLIED<td>javax.swing.text.html.ParagraphView
     * </tr><tr>
     * <td>HTML.Tag.P<td>javax.swing.text.html.ParagraphView
     * </tr><tr>
     * <td>HTML.Tag.H1<td>javax.swing.text.html.ParagraphView
     * </tr><tr>
     * <td>HTML.Tag.H2<td>javax.swing.text.html.ParagraphView
     * </tr><tr>
     * <td>HTML.Tag.H3<td>javax.swing.text.html.ParagraphView
     * </tr><tr>
     * <td>HTML.Tag.H4<td>javax.swing.text.html.ParagraphView
     * </tr><tr>
     * <td>HTML.Tag.H5<td>javax.swing.text.html.ParagraphView
     * </tr><tr>
     * <td>HTML.Tag.H6<td>javax.swing.text.html.ParagraphView
     * </tr><tr>
     * <td>HTML.Tag.DT<td>javax.swing.text.html.ParagraphView
     * </tr><tr>
     * <td>HTML.Tag.MENU<td>ListView
     * </tr><tr>
     * <td>HTML.Tag.DIR<td>ListView
     * </tr><tr>
     * <td>HTML.Tag.UL<td>ListView
     * </tr><tr>
     * <td>HTML.Tag.OL<td>ListView
     * </tr><tr>
     * <td>HTML.Tag.LI<td>BlockView
     * </tr><tr>
     * <td>HTML.Tag.DL<td>BlockView
     * </tr><tr>
     * <td>HTML.Tag.DD<td>BlockView
     * </tr><tr>
     * <td>HTML.Tag.BODY<td>BlockView
     * </tr><tr>
     * <td>HTML.Tag.HTML<td>BlockView
     * </tr><tr>
     * <td>HTML.Tag.CENTER<td>BlockView
     * </tr><tr>
     * <td>HTML.Tag.DIV<td>BlockView
     * </tr><tr>
     * <td>HTML.Tag.BLOCKQUOTE<td>BlockView
     * </tr><tr>
     * <td>HTML.Tag.PRE<td>BlockView
     * </tr><tr>
     * <td>HTML.Tag.BLOCKQUOTE<td>BlockView
     * </tr><tr>
     * <td>HTML.Tag.PRE<td>BlockView
     * </tr><tr>
     * <td>HTML.Tag.IMG<td>ImageView
     * </tr><tr>
     * <td>HTML.Tag.HR<td>HRuleView
     * </tr><tr>
     * <td>HTML.Tag.BR<td>BRView
     * </tr><tr>
     * <td>HTML.Tag.TABLE<td>javax.swing.text.html.TableView
     * </tr><tr>
     * <td>HTML.Tag.INPUT<td>FormView
     * </tr><tr>
     * <td>HTML.Tag.SELECT<td>FormView
     * </tr><tr>
     * <td>HTML.Tag.TEXTAREA<td>FormView
     * </tr><tr>
     * <td>HTML.Tag.OBJECT<td>ObjectView
     * </tr><tr>
     * <td>HTML.Tag.FRAMESET<td>FrameSetView
     * </tr><tr>
     * <td>HTML.Tag.FRAME<td>FrameView
     * </tr>
     * </table>
     */
    public static class HTMLFactory implements ViewFactory {
    
	/**
	 * Creates a view from an element.
	 *
	 * @param elem the element
	 * @return the view
	 */
        public View create(Element elem) {
	    Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
	    if (o instanceof HTML.Tag) {
		HTML.Tag kind = (HTML.Tag) o;
		if (kind == HTML.Tag.CONTENT) {
		    return new InlineView(elem);
		} else if (kind == HTML.Tag.IMPLIED) {
		    String ws = (String) elem.getAttributes().getAttribute(
			CSS.Attribute.WHITE_SPACE);
		    if ((ws != null) && ws.equals("pre")) {
			return new LineView(elem);
		    }
		    return new javax.swing.text.html.ParagraphView(elem);
		} else if ((kind == HTML.Tag.P) ||
			   (kind == HTML.Tag.H1) ||
			   (kind == HTML.Tag.H2) ||
			   (kind == HTML.Tag.H3) ||
			   (kind == HTML.Tag.H4) ||
			   (kind == HTML.Tag.H5) ||
			   (kind == HTML.Tag.H6) ||
			   (kind == HTML.Tag.DT)) {
		    // paragraph
		    return new javax.swing.text.html.ParagraphView(elem);
		} else if ((kind == HTML.Tag.MENU) || 
			   (kind == HTML.Tag.DIR) ||
			   (kind == HTML.Tag.UL)   || 
			   (kind == HTML.Tag.OL)) {
		    return new ListView(elem);
		} else if (kind == HTML.Tag.BODY) {
		    // reimplement major axis requirements to indicate that the
		    // block is flexible for the body element... so that it can
		    // be stretched to fill the background properly.
		    return new BlockView(elem, View.Y_AXIS) {
                        protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
                            r = super.calculateMajorAxisRequirements(axis, r);
			    r.maximum = Integer.MAX_VALUE;
			    return r;
			}
		    };
		} else if (kind == HTML.Tag.HTML) {
		    return new BlockView(elem, View.Y_AXIS);
		} else if ((kind == HTML.Tag.LI) || 
			   (kind == HTML.Tag.CENTER) ||
			   (kind == HTML.Tag.DL) ||
			   (kind == HTML.Tag.DD) || 
			   (kind == HTML.Tag.DIV) ||
			   (kind == HTML.Tag.BLOCKQUOTE) || 
			   (kind == HTML.Tag.PRE)) {
		    // vertical box
		    return new BlockView(elem, View.Y_AXIS);
		} else if (kind == HTML.Tag.NOFRAMES) {
		    return new NoFramesView(elem, View.Y_AXIS);
		} else if (kind==HTML.Tag.IMG) {
		    return new ImageView(elem);
		} else if (kind == HTML.Tag.ISINDEX) {
		    return new IsindexView(elem);
		} else if (kind == HTML.Tag.HR) {
		    return new HRuleView(elem);
		} else if (kind == HTML.Tag.BR) {
		    return new BRView(elem);
		} else if (kind == HTML.Tag.TABLE) {
		    return new javax.swing.text.html.TableView(elem);
		} else if ((kind == HTML.Tag.INPUT) ||
			   (kind == HTML.Tag.SELECT) ||
			   (kind == HTML.Tag.TEXTAREA)) {
		    return new FormView(elem);
		} else if (kind == HTML.Tag.OBJECT) {
		    return new ObjectView(elem);
		} else if (kind == HTML.Tag.FRAMESET) {
                     if (elem.getAttributes().isDefined(HTML.Attribute.ROWS)) {
                         return new FrameSetView(elem, View.Y_AXIS);
                     } else if (elem.getAttributes().isDefined(HTML.Attribute.COLS)) {
                         return new FrameSetView(elem, View.X_AXIS);
                     }
                     throw new Error("Can't build a"  + kind + ", " + elem + ":" +
                                     "no ROWS or COLS defined.");
                } else if (kind == HTML.Tag.FRAME) {
 		    return new FrameView(elem);
                } else if (kind instanceof HTML.UnknownTag) {
		    return new HiddenTagView(elem);
		} else if (kind == HTML.Tag.COMMENT) {
		    return new CommentView(elem);
		} else if (kind == HTML.Tag.HEAD) {
		    // Make the head never visible, and never load its
		    // children. For Cursor positioning,
		    // getNextVisualPositionFrom is overriden to always return
		    // the end offset of the element.
		    return new BlockView(elem, View.X_AXIS) {
			public float getPreferredSpan(int axis) {
			    return 0;
			}
			public float getMinimumSpan(int axis) {
			    return 0;
			}
			public float getMaximumSpan(int axis) {
			    return 0;
			}
			protected void loadChildren(ViewFactory f) {
			}
			public int getNextVisualPositionFrom(int pos,
				     Position.Bias b, Shape a, 
				     int direction, Position.Bias[] biasRet) {
			    return getElement().getEndOffset();
			}
		    };
		} else if ((kind == HTML.Tag.TITLE) ||
			   (kind == HTML.Tag.META) ||
			   (kind == HTML.Tag.LINK) ||
			   (kind == HTML.Tag.STYLE) ||
			   (kind == HTML.Tag.SCRIPT) ||
			   (kind == HTML.Tag.AREA) ||
			   (kind == HTML.Tag.MAP) ||
			   (kind == HTML.Tag.PARAM) ||
			   (kind == HTML.Tag.APPLET)) {
		    return new HiddenTagView(elem);
		}
	    }
	    // If we get here, it's either an element we don't know about
	    // or something from StyledDocument that doesn't have a mapping to HTML.
	    String nm = elem.getName();
	    if (nm != null) {
		if (nm.equals(AbstractDocument.ContentElementName)) {
		    return new LabelView(elem);
		} else if (nm.equals(AbstractDocument.ParagraphElementName)) {
		    return new ParagraphView(elem);
		} else if (nm.equals(AbstractDocument.SectionElementName)) {
		    return new BoxView(elem, View.Y_AXIS);
		} else if (nm.equals(StyleConstants.ComponentElementName)) {
		    return new ComponentView(elem);
		} else if (nm.equals(StyleConstants.IconElementName)) {
		    return new IconView(elem);
		}
	    }
	
	    // default to text display
	    return new LabelView(elem);
	}

    }

    // --- Action implementations ------------------------------

/** The bold action identifier
*/
    public static final String	BOLD_ACTION = "html-bold-action";
/** The italic action identifier
*/
    public static final String	ITALIC_ACTION = "html-italic-action";
/** The paragraph left indent action identifier
*/
    public static final String	PARA_INDENT_LEFT = "html-para-indent-left";
/** The paragraph right indent action identifier
*/
    public static final String	PARA_INDENT_RIGHT = "html-para-indent-right";
/** The  font size increase to next value action identifier
*/
    public static final String	FONT_CHANGE_BIGGER = "html-font-bigger";
/** The font size decrease to next value action identifier
*/
    public static final String	FONT_CHANGE_SMALLER = "html-font-smaller";
/** The Color choice action identifier
     The color is passed as an argument
*/
    public static final String	COLOR_ACTION = "html-color-action";
/** The logical style choice action identifier
     The logical style is passed in as an argument
*/
    public static final String	LOGICAL_STYLE_ACTION = "html-logical-style-action";
    /**
     * Align images at the top.
     */
    public static final String	IMG_ALIGN_TOP = "html-image-align-top";

    /**
     * Align images in the middle.
     */
    public static final String	IMG_ALIGN_MIDDLE = "html-image-align-middle";

    /**
     * Align images at the bottom.
     */
    public static final String	IMG_ALIGN_BOTTOM = "html-image-align-bottom";

    /**
     * Align images at the border.
     */
    public static final String	IMG_BORDER = "html-image-border";


    /** HTML used when inserting tables. */
    private static final String INSERT_TABLE_HTML = "<table border=1><tr><td></td></tr></table>";

    /** HTML used when inserting unordered lists. */
    private static final String INSERT_UL_HTML = "<ul><li></li></ul>";

    /** HTML used when inserting ordered lists. */
    private static final String INSERT_OL_HTML = "<ol><li></li></ol>";

    /** HTML used when inserting hr. */
    private static final String INSERT_HR_HTML = "<hr>";

    /** HTML used when inserting pre. */
    private static final String INSERT_PRE_HTML = "<pre></pre>";


    private static final Action[] defaultActions = {
	new InsertHTMLTextAction("InsertTable", INSERT_TABLE_HTML,
				 HTML.Tag.BODY, HTML.Tag.TABLE),
	new InsertHTMLTextAction("InsertTableRow", INSERT_TABLE_HTML,
				 HTML.Tag.TABLE, HTML.Tag.TR,
				 HTML.Tag.BODY, HTML.Tag.TABLE),
	new InsertHTMLTextAction("InsertTableDataCell", INSERT_TABLE_HTML,
				 HTML.Tag.TR, HTML.Tag.TD,
				 HTML.Tag.BODY, HTML.Tag.TABLE),
	new InsertHTMLTextAction("InsertUnorderedList", INSERT_UL_HTML,
				 HTML.Tag.BODY, HTML.Tag.UL),
	new InsertHTMLTextAction("InsertUnorderedListItem", INSERT_UL_HTML,
				 HTML.Tag.UL, HTML.Tag.LI,
				 HTML.Tag.BODY, HTML.Tag.UL),
	new InsertHTMLTextAction("InsertOrderedList", INSERT_OL_HTML,
				 HTML.Tag.BODY, HTML.Tag.OL),
	new InsertHTMLTextAction("InsertOrderedListItem", INSERT_OL_HTML,
				 HTML.Tag.OL, HTML.Tag.LI,
				 HTML.Tag.BODY, HTML.Tag.OL),
	new InsertHRAction(),
	new InsertHTMLTextAction("InsertPre", INSERT_PRE_HTML,
				 HTML.Tag.BODY, HTML.Tag.PRE),
	new NavigateLinkAction("next-link-action"), 
	new NavigateLinkAction("previous-link-action"), 
	new ActivateLinkAction("activate-link-action")
    };


    /**
     * An abstract Action providing some convenience methods that may
     * be useful in inserting HTML into an existing document.
     * <p>NOTE: None of the convenience methods obtain a lock on the
     * document. If you have another thread modifying the text these
     * methods may have inconsistant behavior, or return the wrong thing.
     */
    public static abstract class HTMLTextAction extends StyledTextAction {
	public HTMLTextAction(String name) {
	    super(name);
	}

	/**
	 * @return HTMLDocument of <code>e</code>.
	 */
	protected HTMLDocument getHTMLDocument(JEditorPane e) {
	    Document d = e.getDocument();
	    if (d instanceof HTMLDocument) {
		return (HTMLDocument) d;
	    }
	    throw new IllegalArgumentException("document must be HTMLDocument");
	}

	/**
	 * @return HTMLEditorKit for <code>e</code>.
	 */
        protected HTMLEditorKit getHTMLEditorKit(JEditorPane e) {
	    EditorKit k = e.getEditorKit();
	    if (k instanceof HTMLEditorKit) {
		return (HTMLEditorKit) k;
	    }
	    throw new IllegalArgumentException("EditorKit must be HTMLEditorKit");
	}

	/**
	 * Returns an array of the Elements that contain <code>offset</code>.
	 * The first elements corresponds to the root.
	 */
	protected Element[] getElementsAt(HTMLDocument doc, int offset) {
	    return getElementsAt(doc.getDefaultRootElement(), offset, 0);
	}

	/**
	 * Recursive method used by getElementsAt.
	 */
	private Element[] getElementsAt(Element parent, int offset,
					int depth) {
	    if (parent.isLeaf()) {
		Element[] retValue = new Element[depth + 1];
		retValue[depth] = parent;
		return retValue;
	    }
	    Element[] retValue = getElementsAt(parent.getElement
			  (parent.getElementIndex(offset)), offset, depth + 1);
	    retValue[depth] = parent;
	    return retValue;
	}

	/**
	 * Returns number of elements, starting at the deepest leaf, needed
	 * to get to an element representing <code>tag</code>. This will
	 * return -1 if no elements is found representing <code>tag</code>,
	 * or 0 if the parent of the leaf at <code>offset</code> represents
	 * <code>tag</code>.
	 */
	protected int elementCountToTag(HTMLDocument doc, int offset,
					HTML.Tag tag) {
	    int depth = -1;
	    Element e = doc.getCharacterElement(offset);
	    while (e != null && e.getAttributes().getAttribute
		   (StyleConstants.NameAttribute) != tag) {
		e = e.getParentElement();
		depth++;
	    }
	    if (e == null) {
		return -1;
	    }
	    return depth;
	}

	/**
	 * Returns the deepest element at <code>offset</code> matching
	 * <code>tag</code>.
	 */
	protected Element findElementMatchingTag(HTMLDocument doc, int offset,
						 HTML.Tag tag) {
	    Element e = doc.getDefaultRootElement();
	    Element lastMatch = null;
	    while (e != null) {
		if (e.getAttributes().getAttribute
		   (StyleConstants.NameAttribute) == tag) {
		    lastMatch = e;
		}
		e = e.getElement(e.getElementIndex(offset));
	    }
	    return lastMatch;
	}
    }


    /**
     * InsertHTMLTextAction can be used to insert an arbitrary string of HTML
     * into an existing HTML document. At least two HTML.Tags need to be
     * supplied. The first Tag, parentTag, identifies the parent in
     * the document to add the elements to. The second tag, addTag,
     * identifies the first tag that should be added to the document as
     * seen in the HTML string. One important thing to remember, is that
     * the parser is going to generate all the appropriate tags, even if
     * they aren't in the HTML string passed in.<p>
     * For example, lets say you wanted to create an action to insert
     * a table into the body. The parentTag would be HTML.Tag.BODY,
     * addTag would be HTML.Tag.TABLE, and the string could be something
     * like &lt;table&gt;&lt;tr&gt;&lt;td&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;.
     * <p>There is also an option to supply an alternate parentTag and
     * addTag. These will be checked for if there is no parentTag at
     * offset.
     */
    public static class InsertHTMLTextAction extends HTMLTextAction {
	public InsertHTMLTextAction(String name, String html,
				    HTML.Tag parentTag, HTML.Tag addTag) {
	    this(name, html, parentTag, addTag, null, null);
	}

	public InsertHTMLTextAction(String name, String html,
				    HTML.Tag parentTag,
				    HTML.Tag addTag,
				    HTML.Tag alternateParentTag,
				    HTML.Tag alternateAddTag) {
	    this(name, html, parentTag, addTag, alternateParentTag,
		 alternateAddTag, true);
	}

	/* public */
	InsertHTMLTextAction(String name, String html,
				    HTML.Tag parentTag,
				    HTML.Tag addTag,
				    HTML.Tag alternateParentTag,
				    HTML.Tag alternateAddTag,
				    boolean adjustSelection) {
	    super(name);
	    this.html = html;
	    this.parentTag = parentTag;
	    this.addTag = addTag;
	    this.alternateParentTag = alternateParentTag;
	    this.alternateAddTag = alternateAddTag;
	    this.adjustSelection = adjustSelection;
	}

	/**
	 * A cover for HTMLEditorKit.insertHTML. If an exception it
	 * thrown it is wrapped in a RuntimeException and thrown.
	 */
	protected void insertHTML(JEditorPane editor, HTMLDocument doc,
				  int offset, String html, int popDepth,
				  int pushDepth, HTML.Tag addTag) {
	    try {
		getHTMLEditorKit(editor).insertHTML(doc, offset, html,
						    popDepth, pushDepth,
						    addTag);
	    } catch (IOException ioe) {
		throw new RuntimeException("Unable to insert: " + ioe);
	    } catch (BadLocationException ble) {
		throw new RuntimeException("Unable to insert: " + ble);
	    }
	}

	/**
	 * This is invoked when inserting at a boundary. It determines
	 * the number of pops, and then the number of pushes that need
	 * to be performed, and then invokes insertHTML.
	 * @since 1.3
	 */
	protected void insertAtBoundary(JEditorPane editor, HTMLDocument doc,
					int offset, Element insertElement,
					String html, HTML.Tag parentTag,
					HTML.Tag addTag) {
	    insertAtBoundry(editor, doc, offset, insertElement, html,
			    parentTag, addTag);
	}

	/**
	 * This is invoked when inserting at a boundary. It determines
	 * the number of pops, and then the number of pushes that need
	 * to be performed, and then invokes insertHTML.
	 * @deprecated As of Java 2 platform v1.3, use insertAtBoundary
	 */
	protected void insertAtBoundry(JEditorPane editor, HTMLDocument doc,
				       int offset, Element insertElement,
				       String html, HTML.Tag parentTag,
				       HTML.Tag addTag) {
	    // Find the common parent.
	    Element e;
	    Element commonParent;
	    boolean isFirst = (offset == 0);

	    if (offset > 0 || insertElement == null) {
		e = doc.getDefaultRootElement();
		while (e != null && e.getStartOffset() != offset &&
		       !e.isLeaf()) {
		    e = e.getElement(e.getElementIndex(offset));
		}
		commonParent = (e != null) ? e.getParentElement() : null;
	    }
	    else {
		// If inserting at the origin, the common parent is the
		// insertElement.
		commonParent = insertElement;
	    }
	    if (commonParent != null) {
		// Determine how many pops to do.
		int pops = 0;
		int pushes = 0;
		if (isFirst && insertElement != null) {
		    e = commonParent;
		    while (e != null && !e.isLeaf()) {
			e = e.getElement(e.getElementIndex(offset));
			pops++;
		    }
		}
		else {
		    e = commonParent;
		    offset--;
		    while (e != null && !e.isLeaf()) {
			e = e.getElement(e.getElementIndex(offset));
			pops++;
		    }

		    // And how many pushes
		    e = commonParent;
		    offset++;
		    while (e != null && e != insertElement) {
			e = e.getElement(e.getElementIndex(offset));
			pushes++;
		    }
		}
		pops = Math.max(0, pops - 1);

		// And insert!
		insertHTML(editor, doc, offset, html, pops, pushes, addTag);
	    }
	}

	/**
	 * If there is an Element with name <code>tag</code> at
	 * <code>offset</code>, this will invoke either insertAtBoundary
	 * or <code>insertHTML</code>. This returns true if there is
	 * a match, and one of the inserts is invoked.
	 */
	/*protected*/
	boolean insertIntoTag(JEditorPane editor, HTMLDocument doc,
			      int offset, HTML.Tag tag, HTML.Tag addTag) {
	    Element e = findElementMatchingTag(doc, offset, tag);
	    if (e != null && e.getStartOffset() == offset) {
		insertAtBoundary(editor, doc, offset, e, html,
				 tag, addTag);
		return true;
	    }
	    else if (offset > 0) {
		int depth = elementCountToTag(doc, offset - 1, tag);
		if (depth != -1) {
		    insertHTML(editor, doc, offset, html, depth, 0, addTag);
		    return true;
		}
	    }
	    return false;
	}

	/**
	 * Called after an insertion to adjust the selection.
	 */
	/* protected */
	void adjustSelection(JEditorPane pane, HTMLDocument doc, 
			     int startOffset, int oldLength) {
	    int newLength = doc.getLength();
	    if (newLength != oldLength && startOffset < newLength) {
		if (startOffset > 0) {
		    String text;
		    try {
			text = doc.getText(startOffset - 1, 1);
		    } catch (BadLocationException ble) {
			text = null;
		    }
		    if (text != null && text.length() > 0 &&
			text.charAt(0) == '\n') {
			pane.select(startOffset, startOffset);
		    }
		    else {
			pane.select(startOffset + 1, startOffset + 1);
		    }
		}
		else {
		    pane.select(1, 1);
		}
	    }
	}

        /**
         * Inserts the HTML into the document.
         *
         * @param ae the event
         */
        public void actionPerformed(ActionEvent ae) {
	    JEditorPane editor = getEditor(ae);
	    if (editor != null) {
		HTMLDocument doc = getHTMLDocument(editor);
		int offset = editor.getSelectionStart();
		int length = doc.getLength();
		boolean inserted;
		// Try first choice
		if (!insertIntoTag(editor, doc, offset, parentTag, addTag) &&
		    alternateParentTag != null) {
		    // Then alternate.
		    inserted = insertIntoTag(editor, doc, offset,
					     alternateParentTag,
					     alternateAddTag);
		}
		else {
		    inserted = true;
		}
		if (adjustSelection && inserted) {
		    adjustSelection(editor, doc, offset, length);
		}
	    }
	}

	/** HTML to insert. */
	protected String html;
	/** Tag to check for in the document. */
	protected HTML.Tag parentTag;
	/** Tag in HTML to start adding tags from. */
	protected HTML.Tag addTag;
	/** Alternate Tag to check for in the document if parentTag is
	 * not found. */
	protected HTML.Tag alternateParentTag;
	/** Alternate tag in HTML to start adding tags from if parentTag
	 * is not found and alternateParentTag is found. */
	protected HTML.Tag alternateAddTag;
	/** True indicates the selection should be adjusted after an insert. */
	boolean adjustSelection;
    }


    /**
     * InsertHRAction is special, at actionPerformed time it will determine
     * the parent HTML.Tag based on the paragraph element at the selection
     * start.
     */
    static class InsertHRAction extends InsertHTMLTextAction {
	InsertHRAction() {
	    super("InsertHR", "<hr>", null, HTML.Tag.IMPLIED, null, null,
		  false);
	}

        /**
         * Inserts the HTML into the document.
         *
         * @param ae the event
         */
        public void actionPerformed(ActionEvent ae) {
	    JEditorPane editor = getEditor(ae);
	    if (editor != null) {
		HTMLDocument doc = getHTMLDocument(editor);
		int offset = editor.getSelectionStart();
		Element paragraph = doc.getParagraphElement(offset);
		if (paragraph.getParentElement() != null) {
		    parentTag = (HTML.Tag)paragraph.getParentElement().
		                  getAttributes().getAttribute
		                  (StyleConstants.NameAttribute);
		    super.actionPerformed(ae);
		}
	    }
	}
	
    }

    /*
     * Action to move the focus on the next or previous hypertext link
     */
    static class NavigateLinkAction extends TextAction {

	FocusHighlightPainter focusPainter = new FocusHighlightPainter(null);
	Object selectionTag;
	boolean focusBack = false;

        /** 
         * Create this action with the appropriate identifier. 
         */
        public NavigateLinkAction(String actionName) {
            super(actionName);
	    if ("previous-link-action".equals(actionName)) {
		focusBack = true;
	    }
        }

        /** The operation to perform when this action is triggered. */
        public void actionPerformed(ActionEvent e) {
            JTextComponent component = getTextComponent(e);
	    if (component.isEditable()) {
		return;
	    }

	    // highlight the next link after the curent caret position
            if (component != null) {
		Document doc = component.getDocument();
		if (doc != null) {
		    
		    HTMLDocument hdoc = (HTMLDocument) doc;
		    HTMLDocument.Iterator ei = hdoc.getIterator(HTML.Tag.A);

		    int currentOffset = component.getCaretPosition();
		    int prevStartOffset = 0;
		    int prevEndOffset = 0;
		    
		    while (ei.isValid()) {
			AttributeSet anchor = ei.getAttributes();
			if (anchor != null) {
			    String href = (String)anchor.getAttribute(HTML.Attribute.HREF);
			    if (href != null) {
				int startOffset = ei.getStartOffset();
				if (focusBack) {
				    if (startOffset >= currentOffset) {
					component.setCaretPosition(prevStartOffset);
					moveCaretPosition(component, prevStartOffset, 
							  prevEndOffset);
					return;
				    }
				} else { // focus forward
				    if (startOffset > currentOffset) {
					component.setCaretPosition(startOffset);
					moveCaretPosition(component, startOffset,
							  ei.getEndOffset());
					return;
				    } 
				}
			    }
			}
			prevStartOffset = ei.getStartOffset();
			prevEndOffset = ei.getEndOffset();
			ei.next();
		    }
                }
            }
        }
	
	/*
	 * Moves the caret from mark to dot
	 */
	private void moveCaretPosition(JTextComponent component, int mark, int dot) {
	    Highlighter h = component.getHighlighter();
	    if (h != null) {
		int p0 = Math.min(dot, mark);
		int p1 = Math.max(dot, mark);
		try {
		    if (selectionTag != null) {
			h.changeHighlight(selectionTag, p0, p1);
		    } else {
			Highlighter.HighlightPainter p = focusPainter;
			selectionTag = h.addHighlight(p0, p1, p);
		    }
		} catch (BadLocationException e) {
		    // XXX 
		}
	    }
	}

	/**
	 * A highlight painter that draws a one-pixel border around
	 * the highlighted area.
	 */ 
	class FocusHighlightPainter extends 
	    DefaultHighlighter.DefaultHighlightPainter {

	    FocusHighlightPainter(Color color) {
		super(color);
	    }
	    
	    /**
	     * Paints a portion of a highlight.
	     *
	     * @param g the graphics context
	     * @param offs0 the starting model offset >= 0
	     * @param offs1 the ending model offset >= offs1
	     * @param bounds the bounding box of the view, which is not
	     *        necessarily the region to paint.
	     * @param c the editor
	     * @param view View painting for
	     * @return region drawing occured in
	     */
	    public Shape paintLayer(Graphics g, int offs0, int offs1,
				    Shape bounds, JTextComponent c, View view) {
		
		Color color = getColor();
		
		if (color == null) {
		    g.setColor(c.getSelectionColor());
		}
		else {
		    g.setColor(color);
		}
		if (offs0 == view.getStartOffset() &&
		    offs1 == view.getEndOffset()) {
		    // Contained in view, can just use bounds.
		    Rectangle alloc;
		    if (bounds instanceof Rectangle) {
			alloc = (Rectangle)bounds;
		    }
		    else {
			alloc = bounds.getBounds();
		    }
		    g.drawRect(alloc.x, alloc.y, alloc.width - 1, alloc.height);
		    return alloc;
		}
		else {
		    // Should only render part of View.
		    try {
			// --- determine locations ---
			Shape shape = view.modelToView(offs0, Position.Bias.Forward,
						       offs1,Position.Bias.Backward,
						       bounds);
			Rectangle r = (shape instanceof Rectangle) ?
			    (Rectangle)shape : shape.getBounds();
			g.drawRect(r.x, r.y, r.width - 1, r.height);
			return r;
		    } catch (BadLocationException e) {
			// can't render
		    }
		}
		// Only if exception
		return null;
	    }
	}
    }

    /*
     * Action to activate the hypertext link that has focus.
     */
    static class ActivateLinkAction extends TextAction {

        /** 
         * Create this action with the appropriate identifier. 
         */
        public ActivateLinkAction(String actionName) {
            super(actionName);
        }
	
        /** The operation to perform when this action is triggered. */
        public void actionPerformed(ActionEvent e) {

            JTextComponent c = getTextComponent(e);
	    if (!(c instanceof JEditorPane)) {
		return;
	    }
	    JEditorPane component = (JEditorPane)c;
	    if (component.isEditable()) {
		return;
	    }
	    
	    // activate the hypertext link at the current caret position
            if (component != null) {
		Document doc = component.getDocument();
		if (doc != null) {
		    
		    HTMLDocument hdoc = (HTMLDocument) doc;
		    HTMLDocument.Iterator ei = hdoc.getIterator(HTML.Tag.A);

		    int currentOffset = component.getCaretPosition();
		    String urlString = null;
		    
		    while (ei.isValid()) {
			AttributeSet anchor = ei.getAttributes();
			if (anchor != null) {
			    String href = 
				(String)anchor.getAttribute(HTML.Attribute.HREF);
			    if (href != null) {
				if (currentOffset >= ei.getStartOffset() &&
				    currentOffset <= ei.getEndOffset()) {
				    urlString = href;
				    break;
				}
			    }
			}
			ei.next();
		    }
		    if (urlString != null) {
			try {
			    URL page = 
				(URL)doc.getProperty(Document.StreamDescriptionProperty);
			    URL url = new URL(page, urlString);
			    HyperlinkEvent linkEvent = new HyperlinkEvent(component, 
                                HyperlinkEvent.EventType.ACTIVATED, url);
			    component.fireHyperlinkUpdate(linkEvent);
			} catch (MalformedURLException m) {
			}			
		    }
		}
	    }
	}
    }
}
