/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.util.*;

import javax.swing.plaf.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import javax.accessibility.*;

/**
 * A text component to edit various kinds of content.
 * You can find how-to information and examples of using editor panes in
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/text.html">Using Text Components</a>,
 * a section in <em>The Java Tutorial.</em>
 *
 * <p>
 * This component uses implementations of the
 * <code>EditorKit</code> to accomplish its behavior. It effectively
 * morphs into the proper kind of text editor for the kind
 * of content it is given.  The content type that editor is bound
 * to at any given time is determined by the <code>EditorKit</code> currently
 * installed.  If the content is set to a new URL, its type is used
 * to determine the <code>EditorKit</code> that should be used to
 * load the content.
 * <p>
 * By default, the following types of content are known:
 * <dl>
 * <dt><b>text/plain</b>
 * <dd>Plain text, which is the default the type given isn't
 * recognized.  The kit used in this case is an extension of
 * <code>DefaultEditorKit</code> that produces a wrapped plain text view.
 * <dt><b>text/html</b>
 * <dd>HTML text.  The kit used in this case is the class
 * <code>javax.swing.text.html.HTMLEditorKit</code>
 * which provides HTML 3.2 support.
 * <dt><b>text/rtf</b>
 * <dd>RTF text.  The kit used in this case is the class
 * <code>javax.swing.text.rtf.RTFEditorKit</code>
 * which provides a limited support of the Rich Text Format.
 * </dl>
 * <p>
 * There are several ways to load content into this component.
 * <ol>
 * <li>
 * The <a href="#setText">setText</a> method can be used to initialize
 * the component from a string.  In this case the current
 * <code>EditorKit</code> will be used, and the content type will be
 * expected to be of this type.
 * <li>
 * The <a href="#read">read</a> method can be used to initialize the 
 * component from a Reader.  Note that if the content type is HTML,
 * relative references (e.g. for things like images) can't be resolved 
 * unless the &lt;base&gt; tag is used or the <em>Base</em> property
 * on HTMLDocument is set.  In this case the current <code>EditorKit</code>
 * will be used, and the content type will be expected to be of this
 * type.
 * <li>
 * The <a href="#setPage">setPage</a> method can be used to initialize
 * the component from a URL.  In this case, the content type will be
 * determined from the URL, and the registered <code>EditorKit</code>
 * for that content type will be set.
 * </ol>
 * <p>
 * For the keyboard keys used by this component in the standard Look and
 * Feel (L&F) renditions, see the
 * <a href="doc-files/Key-Index.html#JEditorPane">JEditorPane</a> key assignments.
 * <p>
 * Some kinds of content may provide hyperlink support by generating
 * hyperlink events.  The HTML <code>EditorKit</code> will generate
 * hyperlink events if the <code>JEditorPane</code> is <em>not editable</em> 
 * (<code>JEditorPane.setEditable(false);</code> has been called).
 * If HTML frames are embedded in the document, the typical response would be
 * to change a portion of the current document.  The following code
 * fragment is a possible hyperlink listener implementation, that treats 
 * HTML frame events specially, and simply displays any other activated
 * hyperlinks.
 * <code><pre>

&nbsp;    class Hyperactive implements HyperlinkListener {
&nbsp;
&nbsp;        public void hyperlinkUpdate(HyperlinkEvent e) {
&nbsp;	          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
&nbsp;		      JEditorPane pane = (JEditorPane) e.getSource();
&nbsp;		      if (e instanceof HTMLFrameHyperlinkEvent) {
&nbsp;		          HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
&nbsp;		          HTMLDocument doc = (HTMLDocument)pane.getDocument();
&nbsp;		          doc.processHTMLFrameHyperlinkEvent(evt);
&nbsp;		      } else {
&nbsp;		          try {
&nbsp;			      pane.setPage(e.getURL());
&nbsp;		          } catch (Throwable t) {
&nbsp;			      t.printStackTrace();
&nbsp;		          }
&nbsp;		      }
&nbsp;	          }
&nbsp;	      }
&nbsp;    }

 * </pre></code>
 * <p>
 * Culturally dependent information in some documents is handled through
 * a mechanism called character encoding.  Character encoding is an
 * unambiguous mapping of the members of a character set (letters, ideographs,
 * digits, symbols, or control functions) to specific numeric code values. It
 * represents the way the file is stored. Example character encodings are
 * ISO-8859-1, ISO-8859-5, Shift-jis, Euc-jp, and UTF-8. When the file is 
 * passed to an user agent (<code>JEditorPane</code>) it is converted to
 * the document character set (ISO-10646 aka Unicode).
 * <p>
 * There are multiple ways to get a character set mapping to happen 
 * with <code>JEditorPane</code>.  
 * <ol>
 * <li>
 * One way is to specify the character set as a parameter of the MIME 
 * type.  This will be established by a call to the 
 * <a href="#setContentType">setContentType</a> method.  If the content
 * is loaded by the <a href="#setPage">setPage</a> method the content
 * type will have been set according to the specification of the URL.
 * It the file is loaded directly, the content type would be expected to
 * have been set prior to loading.
 * <li>
 * Another way the character set can be specified is in the document itself.
 * This requires reading the document prior to determining the character set
 * that is desired.  To handle this, it is expected that the 
 * <code>EditorKit</code>.read operation throw a
 * <code>ChangedCharSetException</code> which will
 * be caught.  The read is then restarted with a new Reader that uses
 * the character set specified in the <code>ChangedCharSetException</code>
 * (which is an <code>IOException</code>).
 * </ol>
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with 
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @beaninfo
 *   attribute: isContainer false
 * description: A text component to edit various types of content.
 *
 * @author  Timothy Prinzing
 * @version 1.98 02/06/02
 */
public class JEditorPane extends JTextComponent {

    /**
     * Creates a new <code>JEditorPane</code>.
     * The document model is set to <code>null</code>.
     */
    public JEditorPane() {
        super();
    }

    /**
     * Creates a <code>JEditorPane</code> based on a specified URL for input.
     *
     * @param initialPage the URL
     * @exception IOException if the URL is <code>null</code>
     *		or cannot be accessed
     */
    public JEditorPane(URL initialPage) throws IOException {
        this();
        setPage(initialPage);
    }

    /**
     * Creates a <code>JEditorPane</code> based on a string containing
     * a URL specification.
     *
     * @param url the URL
     * @exception IOException if the URL is <code>null</code> or
     *		cannot be accessed
     */
    public JEditorPane(String url) throws IOException {
        this();
        setPage(url);
    }

    /**
     * Creates a <code>JEditorPane</code> that has been initialized
     * to the given text.  This is a convenience constructor that calls the
     * <code>setContentType</code> and <code>setText</code> methods.
     *
     * @param type mime type of the given text
     * @param text the text to initialize with
     */
    public JEditorPane(String type, String text) {
	this();
	setContentType(type);
	setText(text);
    }

    /**
     * Adds a hyperlink listener for notification of any changes, for example
     * when a link is selected and entered.
     *
     * @param listener the listener
     */
    public synchronized void addHyperlinkListener(HyperlinkListener listener) {
        listenerList.add(HyperlinkListener.class, listener);
    }

    /**
     * Removes a hyperlink listener.
     *
     * @param listener the listener
     */
    public synchronized void removeHyperlinkListener(HyperlinkListener listener) {
        listenerList.remove(HyperlinkListener.class, listener);
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  This is normally called
     * by the currently installed <code>EditorKit</code> if a content type
     * that supports hyperlinks is currently active and there
     * was activity with a link.  The listener list is processed
     * last to first.
     *
     * @param e the event
     * @see EventListenerList
     */
    public void fireHyperlinkUpdate(HyperlinkEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==HyperlinkListener.class) {
                ((HyperlinkListener)listeners[i+1]).hyperlinkUpdate(e);
            }          
        }
    }


    /**
     * Sets the current URL being displayed.  The content type of the
     * pane is set, and if the editor kit for the pane is
     * non-<code>null</code>, then
     * a new default document is created and the URL is read into it.
     * If the URL contains and reference location, the location will
     * be scrolled to by calling the <code>scrollToReference</code> 
     * method.  If the desired URL is not the one currently being
     * displayed, the <code>getStream</code> method is called to
     * give subclasses control over the stream provided.
     * <p>
     * This may load either synchronously or asynchronously
     * depending upon the document returned by the <code>EditorKit</code>.
     * If the <code>Document</code> is of type
     * <code>AbstractDocument</code> and has a value returned by 
     * <code>AbstractDocument.getAsynchronousLoadPriority</code>
     * that is greater than or equal to zero, the page will be
     * loaded on a separate thread using that priority.
     * <p>
     * If the document is loaded synchronously, it will be
     * filled in with the stream prior to being installed into
     * the editor with a call to <code>setDocument</code>, which
     * is bound and will fire a property change event.  If an
     * <code>IOException</code> is thrown the partially loaded
     * document will
     * be discarded and neither the document or page property
     * change events will be fired.  If the document is 
     * successfully loaded and installed, a view will be
     * built for it by the UI which will then be scrolled if 
     * necessary, and then the page property change event
     * will be fired.
     * <p>
     * If the document is loaded asynchronously, the document
     * will be installed into the editor immediately using a
     * call to <code>setDocument</code> which will fire a 
     * document property change event, then a thread will be
     * created which will begin doing the actual loading.  
     * In this case, the page property change event will not be 
     * fired by the call to this method directly, but rather will be 
     * fired when the thread doing the loading has finished.
     * It will also be fired on the event-dispatch thread.
     * Since the calling thread can not throw an <code>IOException</code>
     * in the event of failure on the other thread, the page 
     * property change event will be fired when the other 
     * thread is done whether the load was successful or not.
     * 
     * @param page the URL of the page
     * @exception IOException for a <code>null</code> or invalid
     *		page specification, or exception from the stream being read
     * @see #getPage
     * @beaninfo
     *  description: the URL used to set content
     *        bound: true
     *       expert: true
     */
    public void setPage(URL page) throws IOException {
        if (page == null) {
            throw new IOException("invalid url");
        }
	URL loaded = getPage();


	// reset scrollbar
	scrollRectToVisible(new Rectangle(0,0,1,1));
	boolean reloaded = false;
	if ((loaded == null) || (! loaded.sameFile(page))) {

	    // different url, load the new content
	    InputStream in = getStream(page);
	    if (kit != null) {
		Document doc = kit.createDefaultDocument();
		if (pageProperties != null) {
		    // transfer properties discovered in stream to the
		    // document property collection.
		    for (Enumeration e = pageProperties.keys(); e.hasMoreElements() ;) {
			Object key = e.nextElement();
			doc.putProperty(key, pageProperties.get(key));
		    }
		    pageProperties.clear();
		}
		if (doc.getProperty(Document.StreamDescriptionProperty) == null) {
		    doc.putProperty(Document.StreamDescriptionProperty, page);
		}

		// At this point, one could either load up the model with no
		// view notifications slowing it down (i.e. best synchronous
		// behavior) or set the model and start to feed it on a seperate
		// thread (best asynchronous behavior).
		synchronized(this) {
		    if (loading != null) {
			// we are loading asynchronously, so we need to cancel 
                        // the old stream.
                        loading.cancel();
                        loading = null;
                    }
                }
		if (doc instanceof AbstractDocument) {
		    AbstractDocument adoc = (AbstractDocument) doc;
		    int p = adoc.getAsynchronousLoadPriority();
		    if (p >= 0) {
			// load asynchronously
			setDocument(doc);
			synchronized(this) {
                            loading = new PageStream(in);
                            Thread pl = new PageLoader(doc, loading, p, loaded, page);
			    pl.start();
			}
			return;
		    }
		}
		read(in, doc);
		setDocument(doc);  
		reloaded = true;
	    }
	}
	final String reference = page.getRef();
	if (reference != null) {
	    if (!reloaded) {
		scrollToReference(reference);
	    }
	    else {
		// Have to scroll after painted.
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			scrollToReference(reference);
		    }
		});
	    }
	}
        firePropertyChange("page", loaded, page);
    }


    /**
     * This method initializes from a stream.  If the kit is
     * set to be of type <code>HTMLEditorKit</code>, and the
     * <code>desc</code> parameter is an <code>HTMLDocument</code>,
     * then it invokes the <code>HTMLEditorKit</code> to initiate
     * the read. Otherwise it calls the superclass
     * method which loads the model as plain text.
     *
     * @param in the stream from which to read
     * @param desc an object describing the stream
     * @exception IOException as thrown by the stream being
     * 		used to initialize
     * @see JTextComponent#read
     * @see #setDocument
     */
    public void read(InputStream in, Object desc) throws IOException {

	if (desc instanceof HTMLDocument && 
	    kit instanceof HTMLEditorKit) {
	    HTMLDocument hdoc = (HTMLDocument) desc;
	    setDocument(hdoc);
	    read(in, hdoc);
	} else {
	    String charset = (String) getClientProperty("charset");
	    Reader r = (charset != null) ? new InputStreamReader(in, charset) :
		new InputStreamReader(in);
	    super.read(r, desc);
	}
    }


    /**
     * This method invokes the <code>EditorKit</code> to initiate a
     * read.  In the case where a <code>ChangedCharSetException</code>
     * is thrown this exception will contain the new CharSet.
     * Therefore the <code>read</code> operation
     * is then restarted after building a new Reader with the new charset.
     *
     * @param in the inputstream to use
     * @param doc the document to load
     *
     */
    void read(InputStream in, Document doc) throws IOException {
	try {
	    String charset = (String) getClientProperty("charset");
	    Reader r = (charset != null) ? new InputStreamReader(in, charset) :
		new InputStreamReader(in);
	    kit.read(r, doc, 0);
	} catch (BadLocationException e) {
	    throw new IOException(e.getMessage());
	} catch (ChangedCharSetException e1) {
	    String charSetSpec = e1.getCharSetSpec();
	    if (e1.keyEqualsCharSet()) {
		putClientProperty("charset", charSetSpec);
	    } else {
		setCharsetFromContentTypeParameters(charSetSpec);
	    }
	    in.close();
	    URL url = (URL)doc.getProperty(Document.StreamDescriptionProperty);
	    URLConnection conn = url.openConnection();
	    in = conn.getInputStream();
	    try {
		doc.remove(0, doc.getLength());
	    } catch (BadLocationException e) {}
	    doc.putProperty("IgnoreCharsetDirective", new Boolean(true));
	    read(in, doc);
	}
    }


    /**
     * Thread to load a stream into the text document model.
     */
    class PageLoader extends Thread {

	/**
	 * Construct an asynchronous page loader.
	 */
	PageLoader(Document doc, InputStream in, int priority, URL old,
		   URL page) {
	    setPriority(priority);
	    this.in = in;
	    this.old = old;
	    this.page = page;
	    this.doc = doc;
	}

	/**
	 * Try to load the document, then scroll the view
	 * to the reference (if specified).  When done, fire
	 * a page property change event.
	 */
        public void run() {
	    try {
		read(in, doc);
		synchronized(JEditorPane.this) {
                    loading = null;
                }
		URL page = (URL) doc.getProperty(Document.StreamDescriptionProperty);
		String reference = page.getRef();
		if (reference != null) {
		    // scroll the page if necessary, but do it on the
		    // event thread... that is the only guarantee that 
		    // modelToView can be safely called.
		    Runnable callScrollToReference = new Runnable() {
                        public void run() {
			    URL u = (URL) getDocument().getProperty
				(Document.StreamDescriptionProperty);
			    String ref = u.getRef();
			    scrollToReference(ref);
			}
		    };
		    SwingUtilities.invokeLater(callScrollToReference);
		}
	    } catch (IOException ioe) {
		getToolkit().beep();
	    } finally {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        firePropertyChange("page", old, page);
                    }
                });
	    }
	}

	/**
	 * The stream to load the document with
	 */
	InputStream in;
	
	/**
	 * URL of the old page that was replaced (for the property change event)
	 */
	URL old;

	/**
	 * URL of the page being loaded (for the property change event)
	 */
	URL page;

	/**
	 * The Document instance to load into. This is cached in case a
	 * new Document is created between the time the thread this is created
	 * and run.
	 */
	Document doc;
    }

    static class PageStream extends FilterInputStream {

        boolean canceled;

        public PageStream(InputStream i) {
            super(i);
            canceled = false;
        }

        /**
         * Cancel the loading of the stream by throwing
         * an IOException on the next request.
         */
        public synchronized void cancel() {
            canceled = true;
        }

        protected synchronized void checkCanceled() throws IOException {
            if (canceled) {
                throw new IOException("page canceled");
            }
        }

        public int read() throws IOException {
            checkCanceled();
            return super.read();
        }

        public long skip(long n) throws IOException {
            checkCanceled();
            return super.skip(n);
        }

        public int available() throws IOException {
            checkCanceled();
            return super.available();
        }

        public void reset() throws IOException {
            checkCanceled();
            super.reset();
        }

    }

    /**
     * Fetches a stream for the given URL, which is about to 
     * be loaded by the <code>setPage</code> method.  By
     * default, this simply opens the URL and returns the
     * stream.  This can be reimplemented to do useful things
     * like fetch the stream from a cache, monitor the progress
     * of the stream, etc.
     * <p>
     * This method is expected to have the the side effect of
     * establishing the content type, and therefore setting the
     * appropriate <code>EditorKit</code> to use for loading the stream.
     * <p>
     * If this the stream was an http connection, redirects
     * will be followed and the resulting URL will be set as
     * the <code>Document.StreamDescriptionProperty</code> so that relative
     * URL's can be properly resolved.
     *
     * @param page  the URL of the page
     */
    protected InputStream getStream(URL page) throws IOException {
	URLConnection conn = page.openConnection();
	if (conn instanceof HttpURLConnection) {
	    HttpURLConnection hconn = (HttpURLConnection) conn;
	    hconn.setInstanceFollowRedirects(false);
	    int response = hconn.getResponseCode();
	    boolean redirect = (response >= 300 && response <= 399);

	    /*
	     * In the case of a redirect, we want to actually change the URL
	     * that was input to the new, redirected URL
	     */
	    if (redirect) {
		String loc = conn.getHeaderField("Location");
		if (loc.startsWith("http", 0)) {
		    page = new URL(loc);
		} else {
		    page = new URL(page, loc);
		}
		return getStream(page);
	    }
	}
	if (pageProperties == null) {
	    pageProperties = new Hashtable();
	}
	String type = conn.getContentType();
	if (type != null) {
	    setContentType(type);
	    pageProperties.put("content-type", type);
	}
	pageProperties.put(Document.StreamDescriptionProperty, page);
	String enc = conn.getContentEncoding();
	if (enc != null) {
	    pageProperties.put("content-encoding", enc);
	}
	InputStream in = conn.getInputStream();
	return in;
    }

    /**
     * Scrolls the view to the given reference location
     * (that is, the value returned by the <code>UL.getRef</code>
     * method for the URL being displayed).  By default, this
     * method only knows how to locate a reference in an
     * HTMLDocument.  The implementation calls the
     * <code>scrollRectToVisible</code> method to
     * accomplish the actual scrolling.  If scrolling to a
     * reference location is needed for document types other
     * than HTML, this method should be reimplemented.
     * This method will have no effect if the component
     * is not visible.
     * 
     * @param reference the named location to scroll to
     */
    protected void scrollToReference(String reference) {
	Document d = getDocument();
	if (d instanceof HTMLDocument) {
	    HTMLDocument doc = (HTMLDocument) d;
	    HTMLDocument.Iterator iter = doc.getIterator(HTML.Tag.A);
	    for (; iter.isValid(); iter.next()) {
		AttributeSet a = iter.getAttributes();
		String nm = (String) a.getAttribute(HTML.Attribute.NAME);
		if ((nm != null) && nm.equals(reference)) {
		    // found a matching reference in the document.
		    try {
			Rectangle r = modelToView(iter.getStartOffset());
			if (r != null) {
			    // the view is visible, scroll it to the 
			    // center of the current visible area.
			    Rectangle vis = getVisibleRect();
			    //r.y -= (vis.height / 2);
			    r.height = vis.height;
			    scrollRectToVisible(r);
                            int index = viewToModel(new Point(r.x,r.y));
                            index = Math.min(index, d.getLength()-1);
                            index = Math.max(index, 0);
                            setCaretPosition(index);
			}
		    } catch (BadLocationException ble) {
			getToolkit().beep();
		    }
		}
	    }
	}
    }

    /**
     * Gets the current URL being displayed.  If a URL was 
     * not specified in the creation of the document, this
     * will return <code>null</code>, and relative URL's will not be 
     * resolved.
     *
     * @return the URL, or <code>null</code> if none
     */
    public URL getPage() {
        return (URL) getDocument().getProperty(Document.StreamDescriptionProperty);
    }

    /**
     * Sets the current URL being displayed.
     *
     * @param url the URL for display
     * @exception IOException for a <code>null</code> or invalid URL
     *		specification
     */
    public void setPage(String url) throws IOException {
        if (url == null) {
            throw new IOException("invalid url");
        }
        URL page = new URL(url);
        setPage(page);
    }

    /**
     * Gets the class ID for the UI.
     *
     * @return the string "EditorPaneUI"
     * @see JComponent#getUIClassID
     * @see UIDefaults#getUI
     */
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * Creates the default editor kit (<code>PlainEditorKit</code>) for when
     * the component is first created.
     *
     * @return the editor kit
     */
    protected EditorKit createDefaultEditorKit() {
        return new PlainEditorKit();
    }

    /**
     * Fetches the currently installed kit for handling content.
     * <code>createDefaultEditorKit</code> is called to set up a default
     * if necessary.
     *
     * @return the editor kit
     */
    public EditorKit getEditorKit() {
        if (kit == null) {
            kit = createDefaultEditorKit();
        }
        return kit;
    }

    /**
     * Gets the type of content that this editor 
     * is currently set to deal with.  This is 
     * defined to be the type associated with the
     * currently installed <code>EditorKit</code>.
     *
     * @return the content type, <code>null</code> if no editor kit set
     */
    public final String getContentType() {
        return (kit != null) ? kit.getContentType() : null;
    }

    /**
     * Sets the type of content that this editor
     * handles.  This calls <code>getEditorKitForContentType</code>,
     * and then <code>setEditorKit</code> if an editor kit can
     * be successfully located.  This is mostly convenience method
     * that can be used as an alternative to calling 
     * <code>setEditorKit</code> directly.
     * <p>
     * If there is a charset definition specified as a parameter
     * of the content type specification, it will be used when
     * loading input streams using the associated <code>EditorKit</code>.
     * For example if the type is specified as 
     * <code>text/html; charset=EUC-JP</code> the content
     * will be loaded using the <code>EditorKit</code> registered for
     * <code>text/html</code> and the Reader provided to
     * the <code>EditorKit</code> to load unicode into the document will
     * use the <code>EUC-JP</code> charset for translating
     * to unicode.
     * 
     * @param type the non-<code>null</code> mime type for the content editing
     *   support
     * @see #getContentType
     * @beaninfo
     *  description: the type of content
     */
    public final void setContentType(String type) {
	// The type could have optional info is part of it,
	// for example some charset info.  We need to strip that
	// of and save it.
	int parm = type.indexOf(";");
	if (parm > -1) {
	    // Save the paramList.
	    String paramList = type.substring(parm);
	    // update the content type string.
	    type = type.substring(0, parm).trim();
	    if (type.toLowerCase().startsWith("text/")) {
		setCharsetFromContentTypeParameters(paramList);
	    }
	}
        if ((kit == null) || (! type.equals(kit.getContentType()))) {
            EditorKit k = getEditorKitForContentType(type);
            if (k != null) {
                setEditorKit(k);
            }
        }
    }

    /**
     * This method gets the charset information specified as part
     * of the content type in the http header information.
     */
    private void setCharsetFromContentTypeParameters(String paramlist) {
	String charset = null;
	try {
	    // paramlist is handed to us with a leading ';', strip it.
	    int semi = paramlist.indexOf(';');
	    if (semi > -1 && semi < paramlist.length()-1) {
		paramlist = paramlist.substring(semi + 1);
	    }

	    if (paramlist.length() > 0) {
		// parse the paramlist into attr-value pairs & get the
		// charset pair's value
		HeaderParser hdrParser = new HeaderParser(paramlist);
		charset = hdrParser.findValue("charset");
		if (charset != null) {
		    putClientProperty("charset", charset);
		}
	    }
	}
	catch (IndexOutOfBoundsException e) {
	    // malformed parameter list, use charset we have
	}
	catch (NullPointerException e) {
	    // malformed parameter list, use charset we have
	}
	catch (Exception e) {
	    // malformed parameter list, use charset we have; but complain
	    System.err.println("JEditorPane.getCharsetFromContentTypeParameters failed on: " + paramlist);
	    e.printStackTrace();
	}
    }


    /**
     * Sets the currently installed kit for handling
     * content.  This is the bound property that
     * establishes the content type of the editor.
     * Any old kit is first deinstalled, then if kit is
     * non-<code>null</code>,
     * the new kit is installed, and a default document created for it.
     * A <code>PropertyChange</code> event ("editorKit") is always fired when
     * <code>setEditorKit</code> is called.
     * <p>
     * <em>NOTE: This has the side effect of changing the model,
     * because the <code>EditorKit</code> is the source of how a
     * particular type
     * of content is modeled.  This method will cause <code>setDocument</code>
     * to be called on behalf of the caller to ensure integrity
     * of the internal state.</em>
     * 
     * @param kit the desired editor behavior
     * @see #getEditorKit
     * @beaninfo
     *  description: the currently installed kit for handling content
     *        bound: true
     *       expert: true
     */
    public void setEditorKit(EditorKit kit) {
        EditorKit old = this.kit;
        if (old != null) {
            old.deinstall(this);
        }
        this.kit = kit;
        if (this.kit != null) {
            this.kit.install(this);
            setDocument(this.kit.createDefaultDocument());
        }
        firePropertyChange("editorKit", old, kit);
    }

    /**
     * Fetches the editor kit to use for the given type
     * of content.  This is called when a type is requested
     * that doesn't match the currently installed type.
     * If the component doesn't have an <code>EditorKit</code> registered
     * for the given type, it will try to create an 
     * <code>EditorKit</code> from the default <code>EditorKit</code> registry.
     * If that fails, a <code>PlainEditorKit</code> is used on the
     * assumption that all text documents can be represented
     * as plain text.
     * <p>
     * This method can be reimplemented to use some
     * other kind of type registry.  This can
     * be reimplemented to use the Java Activation
     * Framework, for example.
     *
     * @param type the non-</code>null</code> content type
     * @return the editor kit
     */  
    public EditorKit getEditorKitForContentType(String type) {
        if (typeHandlers == null) {
            typeHandlers = new Hashtable(3);
        }
        EditorKit k = (EditorKit) typeHandlers.get(type);
        if (k == null) {
            k = createEditorKitForContentType(type);
            if (k != null) {
                setEditorKitForContentType(type, k);
            }
        }
        if (k == null) {
            k = createDefaultEditorKit();
        }
        return k;
    }

    /**
     * Directly sets the editor kit to use for the given type.  A 
     * look-and-feel implementation might use this in conjunction
     * with <code>createEditorKitForContentType</code> to install handlers for
     * content types with a look-and-feel bias.
     *
     * @param type the non-<code>null</code> content type
     * @param k the editor kit to be set
     */
    public void setEditorKitForContentType(String type, EditorKit k) {
        if (typeHandlers == null) {
            typeHandlers = new Hashtable(3);
        }
        typeHandlers.put(type, k);
    }

    /**
     * Replaces the currently selected content with new content
     * represented by the given string.  If there is no selection
     * this amounts to an insert of the given text.  If there
     * is no replacement text (i.e. the content string is empty
     * or <code>null</code>) this amounts to a removal of the
     * current selection.  The replacement text will have the
     * attributes currently defined for input.  If the component is not
     * editable, beep and return.  
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see 
     * <A HREF="http://java.sun.com/products/jfc/swingdoc-archive/threads.html">Threads
     * and Swing</A> for more information.     
     *
     * @param content  the content to replace the selection with.  This
     *   value can be <code>null</code>
     */
    public void replaceSelection(String content) {
        if (! isEditable()) {
            getToolkit().beep();
            return;
        }
        EditorKit kit = getEditorKit();
	if(kit instanceof StyledEditorKit) {
            try {
		Document doc = getDocument();
                Caret caret = getCaret();
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                if (p0 != p1) {
                    doc.remove(p0, p1 - p0);
                }
                if (content != null && content.length() > 0) {
                    doc.insertString(p0, content, ((StyledEditorKit)kit).
				     getInputAttributes());
                }
            } catch (BadLocationException e) {
                getToolkit().beep();
            }
        }
        else {
	    super.replaceSelection(content);
	}
    }

    /**
     * Creates a handler for the given type from the default registry
     * of editor kits.  The registry is created if necessary.  If the
     * registered class has not yet been loaded, an attempt
     * is made to dynamically load the prototype of the kit for the
     * given type.  If the type was registered with a <code>ClassLoader</code>,
     * that <code>ClassLoader</code> will be used to load the prototype.
     * If there was no registered <code>ClassLoader</code>,
     * <code>Class.forName</code> will be used to load the prototype.
     * <p>
     * Once a prototype <code>EditorKit</code> instance is successfully
     * located, it is cloned and the clone is returned.  
     *
     * @param type the content type
     * @return the editor kit, or <code>null</code> if there is nothing
     *   registered for the given type
     */
    public static EditorKit createEditorKitForContentType(String type) {
        EditorKit k = null;
        Hashtable kitRegistry = getKitRegisty();
	k = (EditorKit) kitRegistry.get(type);
        if (k == null) {
            // try to dynamically load the support 
            String classname = (String) getKitTypeRegistry().get(type);
	    ClassLoader loader = (ClassLoader) getKitLoaderRegistry().get(type);
            try {
		Class c;
		if (loader != null) {
		    c = loader.loadClass(classname);
		} else {
		    c = Class.forName(classname);
		}
                k = (EditorKit) c.newInstance();
                kitRegistry.put(type, k);
            } catch (Throwable e) {
                k = null;
            }
        }

        // create a copy of the prototype or null if there
        // is no prototype.
        if (k != null) {
            return (EditorKit) k.clone();
        }
        return null;
    }

    /**
     * Establishes the default bindings of <code>type</code> to
     * <code>classname</code>.  
     * The class will be dynamically loaded later when actually
     * needed, and can be safely changed before attempted uses
     * to avoid loading unwanted classes.  The prototype 
     * <code>EditorKit</code> will be loaded with <code>Class.forName</code>
     * when registered with this method.
     *
     * @param type the non-<code>null</code> content type
     * @param classname the class to load later
     */
    public static void registerEditorKitForContentType(String type, String classname) {
	getKitLoaderRegistry().remove(type);
        getKitTypeRegistry().put(type, classname);
	getKitRegisty().remove(type);
    }

    /**
     * Establishes the default bindings of <code>type</code> to
     * <code>classname</code>.  
     * The class will be dynamically loaded later when actually
     * needed using the given <code>ClassLoader</code>,
     * and can be safely changed 
     * before attempted uses to avoid loading unwanted classes.
     *
     * @param type the non-</code>null</code> content type
     * @param classname the class to load later
     * @param loader the <code>ClassLoader</code> to use to load the name
     */
    public static void registerEditorKitForContentType(String type, String classname, ClassLoader loader) {
        getKitTypeRegistry().put(type, classname);
	getKitLoaderRegistry().put(type, loader);
	getKitRegisty().remove(type);
    }

    /**
     * Returns the currently registered <code>EditorKit</code>
     * class name for the type <code>type</code>.
     *
     * @param type  the non-<code>null</code> content type
     *
     * @since 1.3
     */
    public static String getEditorKitClassNameForContentType(String type) {
	return (String)getKitTypeRegistry().get(type);
    }

    private static Hashtable getKitTypeRegistry() {
	loadDefaultKitsIfNecessary();
	return (Hashtable)SwingUtilities.appContextGet(kitTypeRegistryKey);
    }

    private static Hashtable getKitLoaderRegistry() {
	loadDefaultKitsIfNecessary();
	return (Hashtable)SwingUtilities.appContextGet(kitLoaderRegistryKey);
    }

    private static Hashtable getKitRegisty() {
	Hashtable ht = (Hashtable)SwingUtilities.appContextGet(kitRegistryKey);
	if (ht == null) {
	    ht = new Hashtable(3);
	    SwingUtilities.appContextPut(kitRegistryKey, ht);
	}
	return ht;
    }

    /**
     * This is invoked every time the registries are accessed. Loading
     * is done this way instead of via a static as the static is only
     * called once when running in plugin resulting in the entries only
     * appearing in the first applet.
     */
    private static void loadDefaultKitsIfNecessary() {
	if (SwingUtilities.appContextGet(kitTypeRegistryKey) == null) {
	    Hashtable ht = new Hashtable();
	    SwingUtilities.appContextPut(kitTypeRegistryKey, ht);
	    ht = new Hashtable();
	    SwingUtilities.appContextPut(kitLoaderRegistryKey, ht);
	    registerEditorKitForContentType("text/plain",
				  "javax.swing.JEditorPane$PlainEditorKit");
	    registerEditorKitForContentType("text/html",
				  "javax.swing.text.html.HTMLEditorKit");
	    registerEditorKitForContentType("text/rtf",
				  "javax.swing.text.rtf.RTFEditorKit");
	    registerEditorKitForContentType("application/rtf",
				  "javax.swing.text.rtf.RTFEditorKit");
	}
    }

    // --- java.awt.Component methods --------------------------

    /**
     * Returns the preferred size for the <code>JEditorPane</code>.
     * The preferred size for <code>JEditorPane</code> is slightly altered
     * from the preferred size of the superclass.  If the size
     * of the viewport has become smaller than the minimum size
     * of the component, the scrollable definition for tracking
     * width or height will turn to false.  The default viewport
     * layout will give the preferred size, and that is not desired
     * in the case where the scrollable is tracking.  In that case
     * the <em>normal</em> preferred size is adjusted to the
     * minimum size.  This allows things like HTML tables to
     * shrink down to their minimum size and then be laid out at
     * their minimum size, refusing to shrink any further.
     *
     * @return a <code>Dimension</code> containing the preferred size
     */
    public Dimension getPreferredSize() {
	Dimension d = super.getPreferredSize();
	if (getParent() instanceof JViewport) {
	    JViewport port = (JViewport)getParent();
	    TextUI ui = getUI();
	    if (! getScrollableTracksViewportWidth()) {
		int w = port.getWidth();
		Dimension min = ui.getMinimumSize(this);
		if (w < min.width) {
		    d.width = min.width;
		}
	    }
	    if (! getScrollableTracksViewportHeight()) {
		int h = port.getHeight();
		Dimension min = ui.getMinimumSize(this);
		if (h < min.height) {
		    d.height = min.height;
		}
	    }
	}
	return d;
    }

    // --- JComponent methods ---------------------------------

    /**
     * Turns off tab traversal once focus is gained.
     *
     * @return true, to indicate that the focus is being managed;
     *		or false otherwise
     */
    public boolean isManagingFocus() {
        return managingFocus;
    }

    /**
     * Makes <code>JEditorPane</code> be the root of a focus cycle.
     * This means that, by default, tabbing within the editor
     * pane will move between components of the pane, such as
     * form controls, but not out of the pane.
     * <p>Note that this method always returns true.
     *
     * @see JComponent#isFocusCycleRoot
     * @return true
     */
    public boolean isFocusCycleRoot() {
        return true;
    }

    /**
     * Overridden to handle processing of tab/shift tab. If <code>e</code>
     * identifies a tab, the editor pane is not editable and has components,
     * then the <code>FocusManager</code> is asked to pass focus to the
     * next/previous component.
     *
     * @param e  the current key event
     */
    protected void processComponentKeyEvent(KeyEvent e)  {
        super.processComponentKeyEvent(e);
	if (isManagingFocus()) {
	  if ((e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyChar() == '\t')) {
	      if (e.getID() == KeyEvent.KEY_PRESSED && !isEditable() &&
		  getComponentCount() > 0) {
		  // We aren't editable, and have child components, which
		  // are likely form elements. By setting managingFocus to
		  // false the FocusManager will look at our children and
		  // give one of them focus.
		  managingFocus = false;
		  try {
		      if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 
			  ActionEvent.SHIFT_MASK) {
			  FocusManager.getCurrentManager().
			               focusPreviousComponent(this);
		      }
		      else {
			  FocusManager.getCurrentManager().
			               focusNextComponent(this);
		      }
		      e.consume();
		  }
		  finally {
		      managingFocus = true;
		  }
	      }
	  }
	}
    }

    /**
     * Make sure that TAB and Shift-TAB events get consumed, so that
     * awt doesn't attempt focus traversal.
     *
     * @param e the current key event
     */
    protected void processKeyEvent(KeyEvent e) {
        super.processKeyEvent(e);
        // tab consumption
	// We are actually consuming any TABs modified in any way, because
	// we don't want awt to get anything it can use for focus traversal.
	if (isManagingFocus()) {
	  if ((e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyChar() == '\t')) {
	    e.consume();
	  }
	}
    }

    // --- JTextComponent methods -----------------------------

    /**
     * Sets the text of this <code>TextComponent</code> to the specified
     * content,
     * which is expected to be in the format of the content type of
     * this editor.  For example, if the type is set to <code>text/html</code>
     * the string should be specified in terms of HTML.  
     * <p>
     * This is implemented to remove the contents of the current document,
     * and replace them by parsing the given string using the current
     * <code>EditorKit</code>.  This gives the semantics of the
     * superclass by not changing
     * out the model, while supporting the content type currently set on
     * this component.  The assumption is that the previous content is
     * relatively
     * small, and that the previous content doesn't have side effects.
     * Both of those assumptions can be violated and cause undesirable results.
     * To avoid this, create a new document,
     * <code>getEditorKit().createDefaultDocument()</code>, and replace the
     * existing <code>Document</code> with the new one. You are then assured the
     * previous <code>Document</code> won't have any lingering state.
     * <ol>
     * <li>
     * Leaving the existing model in place means that the old view will be
     * torn down, and a new view created, where replacing the document would
     * avoid the tear down of the old view.
     * <li>
     * Some formats (such as HTML) can install things into the document that
     * can influence future contents.  HTML can have style information embedded
     * that would influence the next content installed unexpectedly.
     * </ol>
     * <p>
     * An alternative way to load this component with a string would be to
     * create a StringReader and call the read method.  In this case the model
     * would be replaced after it was initialized with the contents of the
     * string.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see 
     * <A HREF="http://java.sun.com/products/jfc/swingdoc-archive/threads.html">Threads
     * and Swing</A> for more information.     
     *
     * @param t the new text to be set
     * @see #getText
     * @beaninfo
     * description: the text of this component
     */
    public void setText(String t) {
        try {
	    Document doc = getDocument();
	    doc.remove(0, doc.getLength());
	    Reader r = new StringReader(t);
	    EditorKit kit = getEditorKit();
            kit.read(r, doc, 0);
        } catch (IOException ioe) {
            getToolkit().beep();
        } catch (BadLocationException ble) {
            getToolkit().beep();
	}
    }

    /**
     * Returns the text contained in this <code>TextComponent</code>
     * in terms of the
     * content type of this editor.  If an exception is thrown while
     * attempting to retrieve the text, <code>null</code> will be returned.
     * This is implemented to call <code>JTextComponent.write</code> with
     * a <code>StringWriter</code>.
     *
     * @return the text
     * @see #setText
     */
    public String getText() {
	String txt;
	try {
	    StringWriter buf = new StringWriter();
	    write(buf);
	    txt = buf.toString();
        } catch (IOException ioe) {
            txt = null;
        }
        return txt;
    }

    // --- Scrollable  ----------------------------------------

    /**
     * Returns true if a viewport should always force the width of this 
     * <code>Scrollable</code> to match the width of the viewport.  
     * 
     * @return true if a viewport should force the Scrollables width to
     * match its own, false otherwise
     */
    public boolean getScrollableTracksViewportWidth() {
	if (getParent() instanceof JViewport) {
	    JViewport port = (JViewport)getParent();
	    TextUI ui = getUI();
	    int w = port.getWidth();
	    Dimension min = ui.getMinimumSize(this);
	    Dimension max = ui.getMaximumSize(this);
	    if ((w >= min.width) && (w <= max.width)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns true if a viewport should always force the height of this 
     * <code>Scrollable</code> to match the height of the viewport.  
     * 
     * @return true if a viewport should force the
     *		<code>Scrollable</code>'s height to match its own,
     *		false otherwise
     */
    public boolean getScrollableTracksViewportHeight() {
	if (getParent() instanceof JViewport) {
	    JViewport port = (JViewport)getParent();
	    TextUI ui = getUI();
	    int h = port.getHeight();
	    Dimension min = ui.getMinimumSize(this);
	    if (h >= min.height) {
		Dimension max = ui.getMaximumSize(this);
		if (h <= max.height) {
		    return true;
		}
	    }
	}
	return false;
    }

    // --- Serialization ------------------------------------

    /** 
     * See <code>readObject</code> and <code>writeObject</code> in
     * <code>JComponent</code> for more 
     * information about serialization in Swing.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
	if ((ui != null) && (getUIClassID().equals(uiClassID))) {
	    ui.installUI(this);
	}
    }

    // --- variables ---------------------------------------

    /**
     * Stream currently loading asynchronously (potentially cancelable).
     * Access to this variable should be synchronized.
     */
    PageStream loading;

    /**
     * Current content binding of the editor.
     */
    private EditorKit kit;

    private Hashtable pageProperties;

    /**
     * Table of registered type handlers for this editor.
     */
    private Hashtable typeHandlers;

    /**
     * Indicates whether we are managing focus.
     * @see #processComponentKeyEvent
     * @see #isManagingFocus
     */
    private boolean managingFocus = true;

    /*
     * Private AppContext keys for this class's static variables.
     */
    private static final Object kitRegistryKey = 
        new StringBuffer("JEditorPane.kitRegistry");
    private static final Object kitTypeRegistryKey = 
        new StringBuffer("JEditorPane.kitTypeRegistry");
    private static final Object kitLoaderRegistryKey = 
        new StringBuffer("JEditorPane.kitLoaderRegistry");

    /**
     * @see #getUIClassID
     * @see #readObject
     */
    private static final String uiClassID = "EditorPaneUI";


    /**
     * Returns a string representation of this <code>JEditorPane</code>.
     * This method 
     * is intended to be used only for debugging purposes, and the 
     * content and format of the returned string may vary between      
     * implementations. The returned string may be empty but may not 
     * be <code>null</code>.
     * 
     * @return  a string representation of this <code>JEditorPane</code>
     */
    protected String paramString() {
        String kitString = (kit != null ?
			    kit.toString() : "");
        String typeHandlersString = (typeHandlers != null ?
				     typeHandlers.toString() : "");

        return super.paramString() +
        ",kit=" + kitString +
        ",typeHandlers=" + typeHandlersString;
    }

    
/////////////////
// Accessibility support
////////////////


    /**
     * Gets the AccessibleContext associated with this JEditorPane. 
     * For editor panes, the AccessibleContext takes the form of an 
     * AccessibleJEditorPane. 
     * A new AccessibleJEditorPane instance is created if necessary.
     *
     * @return an AccessibleJEditorPane that serves as the 
     *         AccessibleContext of this JEditorPane
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
	    if (JEditorPane.this.getEditorKit() instanceof HTMLEditorKit) {
		accessibleContext = new AccessibleJEditorPaneHTML();
	    } else {
                accessibleContext = new AccessibleJEditorPane();
	    }
        }
        return accessibleContext;
    }

    /**
     * This class implements accessibility support for the 
     * <code>JEditorPane</code> class.  It provides an implementation of the 
     * Java Accessibility API appropriate to editor pane user-interface 
     * elements.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    protected class AccessibleJEditorPane extends AccessibleJTextComponent {

        /**
         * Gets the accessibleDescription property of this object.  If this
         * property isn't set, return the content type of this JEditorPane
         * instead (e.g. "plain/text", "html/text", etc.
         *
         * @return the localized description of the object; null if
         * this object does not have a description
         *
         * @see #setAccessibleName
         */
        public String getAccessibleDescription() {
            if (accessibleDescription != null) {
                return accessibleDescription;
            } else {
                return JEditorPane.this.getContentType();
            }
        }

        /**
         * Gets the state set of this object.
         *
         * @return an instance of AccessibleStateSet describing the states
         * of the object
         * @see AccessibleStateSet
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.MULTI_LINE);
            return states;
        }
    }

    /**
     * This class provides support for <code>AccessibleHypertext</code>,
     * and is used in instances where the <code>EditorKit</code>
     * installed in this <code>JEditorPane</code> is an instance of
     * <code>HTMLEditorKit</code>.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * baseline for serialized form of Swing objects.
     */
    protected class AccessibleJEditorPaneHTML extends AccessibleJEditorPane {
	public AccessibleText getAccessibleText() {
	    return new JEditorPaneAccessibleHypertextSupport();
	}
    }

    /**
     * What's returned by
     * <code>AccessibleJEditorPaneHTML.getAccessibleText</code>.
     *
     * Provides support for <code>AccessibleHypertext</code> in case
     * there is an HTML document being displayed in this
     * <code>JEditorPane</code>.
     * 
     */
    protected class JEditorPaneAccessibleHypertextSupport
    extends AccessibleJEditorPane implements AccessibleHypertext {

	public class HTMLLink extends AccessibleHyperlink {
	    Element element;

	    public HTMLLink(Element e) {
		element = e;
	    }

	    /**
	     * Since the document a link is associated with may have
	     * changed, this method returns whether this Link is valid
	     * anymore (with respect to the document it references).
	     *
	     * @return a flag indicating whether this link is still valid with
	     *         respect to the AccessibleHypertext it belongs to
	     */
	    public boolean isValid() {
		return JEditorPaneAccessibleHypertextSupport.this.linksValid;
	    }

	    /**
	     * Returns the number of accessible actions available in this Link
	     * If there are more than one, the first one is NOT considered the
	     * "default" action of this LINK object (e.g. in an HTML imagemap).
	     * In general, links will have only one AccessibleAction in them.
	     *
	     * @return the zero-based number of Actions in this object
	     */
	    public int getAccessibleActionCount() {
		return 1;
	    }

	    /**
	     * Perform the specified Action on the object
	     *
	     * @param i zero-based index of actions
	     * @return true if the the action was performed; else false.
	     * @see #getAccessibleActionCount
	     */
	    public boolean doAccessibleAction(int i) {
		if (i == 0 && isValid() == true) {
		    URL u = (URL) getAccessibleActionObject(i);
		    if (u != null) {
			HyperlinkEvent linkEvent =
			    new HyperlinkEvent(JEditorPane.this, HyperlinkEvent.EventType.ACTIVATED, u);
			JEditorPane.this.fireHyperlinkUpdate(linkEvent);
			return true;
		    }
		}
		return false;  // link invalid or i != 0
	    }

	    /**
	     * Return a String description of this particular
	     * link action.  The string returned is the text
	     * within the document associated with the element
	     * which contains this link.
	     *
	     * @param i zero-based index of the actions
	     * @return a String description of the action
	     * @see #getAccessibleActionCount
	     */
	    public String getAccessibleActionDescription(int i) {
		if (i == 0 && isValid() == true) {
		    Document d = JEditorPane.this.getDocument();
		    if (d != null) {
			try {
			    return d.getText(getStartIndex(),
					     getEndIndex() - getStartIndex());
			} catch (BadLocationException exception) {
			    return null;
			}
		    }
		}
		return null;
	    }

	    /**
	     * Returns a URL object that represents the link.
	     *
	     * @param i zero-based index of the actions
	     * @return an URL representing the HTML link itself
	     * @see #getAccessibleActionCount
	     */
	    public Object getAccessibleActionObject(int i) {
		if (i == 0 && isValid() == true) {
		    AttributeSet as = element.getAttributes();
		    AttributeSet anchor = 
			(AttributeSet) as.getAttribute(HTML.Tag.A);
		    String href = (anchor != null) ?
			(String) anchor.getAttribute(HTML.Attribute.HREF) : null;
		    if (href != null) {
			URL u;
			try {
			    u = new URL(JEditorPane.this.getPage(), href);
			} catch (MalformedURLException m) {
			    u = null;
			}
			return u;
		    }
		}
		return null;  // link invalid or i != 0
	    }

	    /**
	     * Return an object that represents the link anchor,
	     * as appropriate for that link.  E.g. from HTML:
	     *   <a href="http://www.sun.com/access">Accessibility</a>
	     * this method would return a String containing the text:
	     * 'Accessibility'.
	     *
	     * Similarly, from this HTML:
	     *   &lt;a HREF="#top"&gt;&lt;img src="top-hat.gif" alt="top hat"&gt;&lt;/a&gt;
	     * this might return the object ImageIcon("top-hat.gif", "top hat");
	     *
	     * @param i zero-based index of the actions
	     * @return an Object representing the hypertext anchor
	     * @see #getAccessibleActionCount
	     */
	    public Object getAccessibleActionAnchor(int i) {
		return getAccessibleActionDescription(i);
	    }


	    /**
	     * Get the index with the hypertext document at which this
	     * link begins
	     *
	     * @return index of start of link
	     */
	    public int getStartIndex() {
		return element.getStartOffset();
	    }

	    /**
	     * Get the index with the hypertext document at which this
	     * link ends
	     *
	     * @return index of end of link
	     */
	    public int getEndIndex() {
		return element.getEndOffset();
	    }
	}

	private class LinkVector extends Vector {
	    public int baseElementIndex(Element e) {
		HTMLLink l;
		for (int i = 0; i < elementCount; i++) {
		    l = (HTMLLink) elementAt(i);
		    if (l.element == e) {
			return i;
		    }
		}
		return -1;
	    }
	}

        LinkVector hyperlinks;
	boolean linksValid = false;

	/**
	 * Build the private table mapping links to locations in the text
	 */
	private void buildLinkTable() {
	    hyperlinks.removeAllElements();
	    Document d = JEditorPane.this.getDocument();
	    if (d != null) {
		ElementIterator ei = new ElementIterator(d);
		Element e;
		AttributeSet as;
		AttributeSet anchor;
		String href;
		while ((e = ei.next()) != null) {
		    if (e.isLeaf()) {
			as = e.getAttributes();
		    anchor = (AttributeSet) as.getAttribute(HTML.Tag.A);
		    href = (anchor != null) ?
			(String) anchor.getAttribute(HTML.Attribute.HREF) : null;
			if (href != null) {
			    hyperlinks.addElement(new HTMLLink(e));
			}
		    }
		}
	    }
	    linksValid = true;
	}

	/**
	 * Make one of these puppies
	 */
	public JEditorPaneAccessibleHypertextSupport() {
	    hyperlinks = new LinkVector();
	    Document d = JEditorPane.this.getDocument();
	    if (d != null) {
		d.addDocumentListener(new DocumentListener() {
		    public void changedUpdate(DocumentEvent theEvent) {
			linksValid = false;
		    }
		    public void insertUpdate(DocumentEvent theEvent) {
			linksValid = false;
		    }
		    public void removeUpdate(DocumentEvent theEvent) {
			linksValid = false;
		    }
		});
	    }
	}

	/**
	 * Returns the number of links within this hypertext doc.
	 *
	 * @return number of links in this hypertext doc.
	 */
	public int getLinkCount() {
	    if (linksValid == false) {
		buildLinkTable();
	    }
	    return hyperlinks.size();
	}

	/**
	 * Returns the index into an array of hyperlinks that
	 * is associated with this character index, or -1 if there
	 * is no hyperlink associated with this index.
	 *
	 * @param character index within the text
	 * @return index into the set of hyperlinks for this hypertext doc.
	 */
	public int getLinkIndex(int charIndex) {
	    if (linksValid == false) {
		buildLinkTable();
	    }
            Element e = null;
	    Document doc = JEditorPane.this.getDocument();
	    if (doc != null) {
		for (e = doc.getDefaultRootElement(); ! e.isLeaf(); ) {
		    int index = e.getElementIndex(charIndex);
		    e = e.getElement(index);
		}
	    }

	    // don't need to verify that it's an HREF element; if
	    // not, then it won't be in the hyperlinks Vector, and
	    // so indexOf will return -1 in any case
	    return hyperlinks.baseElementIndex(e);
	}

	/**
	 * Returns the index into an array of hyperlinks that
	 * index.  If there is no hyperlink at this index, it returns
	 * null.
	 *
	 * @param index into the set of hyperlinks for this hypertext doc.
	 * @return string representation of the hyperlink
	 */
	public AccessibleHyperlink getLink(int linkIndex) {
	    if (linksValid == false) {
		buildLinkTable();
	    }
	    if (linkIndex >= 0 && linkIndex < hyperlinks.size()) {
	        return (AccessibleHyperlink) hyperlinks.elementAt(linkIndex);
	    } else {
		return null;
	    }
	}

	/**
	 * Returns the contiguous text within the document that
	 * is associated with this hyperlink.
	 *
	 * @param index into the set of hyperlinks for this hypertext doc.
	 * @return the contiguous text sharing the link at this index
	 */
	public String getLinkText(int linkIndex) {
	    if (linksValid == false) {
		buildLinkTable();
	    }
	    Element e = (Element) hyperlinks.elementAt(linkIndex);
	    if (e != null) {
		Document d = JEditorPane.this.getDocument();
		if (d != null) {
		    try {
			return d.getText(e.getStartOffset(), 
					 e.getEndOffset() - e.getStartOffset());
		    } catch (BadLocationException exception) {
			return null;
		    }
		}
	    }
	    return null;
	}
    }

    static class PlainEditorKit extends DefaultEditorKit implements ViewFactory {

	/**
	 * Creates a copy of the editor kit.  This
	 * allows an implementation to serve as a prototype
	 * for others, so that they can be quickly created.
	 *
	 * @return the copy
	 */
        public Object clone() {
	    return new PlainEditorKit();
	}

	/**
	 * Fetches a factory that is suitable for producing 
	 * views of any models that are produced by this
	 * kit.  The default is to have the UI produce the
	 * factory, so this method has no implementation.
	 *
	 * @return the view factory
	 */
        public ViewFactory getViewFactory() {
	    return this;
	}

	/**
	 * Creates a view from the given structural element of a
	 * document.
	 *
	 * @param elem  the piece of the document to build a view of
	 * @return the view
	 * @see View
	 */
        public View create(Element elem) {
            Document doc = elem.getDocument();
            Object i18nFlag
                = doc.getProperty("i18n"/*AbstractDocument.I18NProperty*/);
            if ((i18nFlag != null) && i18nFlag.equals(Boolean.TRUE)) {
                // build a view that support bidi
                return createI18N(elem);
            } else {
                return new WrappedPlainView(elem);
            }
        }

        View createI18N(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new PlainParagraph(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)){
                    return new BoxView(elem, View.Y_AXIS);
                }
            }
            return null;
        }

        /**
         * Paragraph for representing plain-text lines that support
         * bidirectional text.
         */
        static class PlainParagraph extends javax.swing.text.ParagraphView {

            PlainParagraph(Element elem) {
                super(elem);
                layoutPool = new LogicalView(elem);
                layoutPool.setParent(this);
            }

            protected void setPropertiesFromAttributes() {
                Component c = getContainer();
                if ((c != null) 
                    && (! c.getComponentOrientation().isLeftToRight()))
                {
                    setJustification(StyleConstants.ALIGN_RIGHT);
                } else {
                    setJustification(StyleConstants.ALIGN_LEFT);
                }
            }

            /**
             * Fetch the constraining span to flow against for
             * the given child index.
             */
            public int getFlowSpan(int index) {
                Component c = getContainer();
                if (c instanceof JTextArea) {
                    JTextArea area = (JTextArea) c;
                    if (! area.getLineWrap()) {
                        // no limit if unwrapped
                        return Integer.MAX_VALUE;
                    }
                }
                return super.getFlowSpan(index);
            }

            protected SizeRequirements calculateMinorAxisRequirements(int axis,
                                                            SizeRequirements r)
            {
                SizeRequirements req 
                    = super.calculateMinorAxisRequirements(axis, r);
                Component c = getContainer();
                if (c instanceof JTextArea) {
                    JTextArea area = (JTextArea) c;
                    if (! area.getLineWrap()) {
                        // min is pref if unwrapped
                        req.minimum = req.preferred;
                    }
                }
                return req;
            }

            /**
             * This class can be used to represent a logical view for 
             * a flow.  It keeps the children updated to reflect the state
             * of the model, gives the logical child views access to the
             * view hierarchy, and calculates a preferred span.  It doesn't
             * do any rendering, layout, or model/view translation.
             */
            static class LogicalView extends CompositeView {
	    
                LogicalView(Element elem) {
                    super(elem);
                }

                protected int getViewIndexAtPosition(int pos) {
                    Element elem = getElement();
                    if (elem.getElementCount() > 0) {
                        return elem.getElementIndex(pos);
                    }
                    return 0;
                }

                protected boolean 
                updateChildren(DocumentEvent.ElementChange ec, 
                               DocumentEvent e, ViewFactory f)
                {
                    return false;
                }

                protected void loadChildren(ViewFactory f) {
                    Element elem = getElement();
                    if (elem.getElementCount() > 0) {
                        super.loadChildren(f);
                    } else {
                        View v = new GlyphView(elem);
                        append(v);
                    }
                }

                public float getPreferredSpan(int axis) {
                    if( getViewCount() != 1 )
                        throw new Error("One child view is assumed.");
                
                    View v = getView(0);
                    //((GlyphView)v).setGlyphPainter(null);
                    return v.getPreferredSpan(axis);
                }

                /**
                 * Forward the DocumentEvent to the given child view.  This
                 * is implemented to reparent the child to the logical view
                 * (the children may have been parented by a row in the flow
                 * if they fit without breaking) and then execute the 
                 * superclass behavior.
                 *
                 * @param v the child view to forward the event to.
                 * @param e the change information from the associated document
                 * @param a the current allocation of the view
                 * @param f the factory to use to rebuild if the view has 
                 *          children
                 * @see #forwardUpdate
                 * @since 1.3
                 */
                protected void forwardUpdateToView(View v, DocumentEvent e, 
                                                   Shape a, ViewFactory f) {
                    v.setParent(this);
                    super.forwardUpdateToView(v, e, a, f);
                }

                // The following methods don't do anything useful, they
                // simply keep the class from being abstract.

                public void paint(Graphics g, Shape allocation) {
                }

                protected boolean isBefore(int x, int y, Rectangle alloc) {
                    return false;
                }

                protected boolean isAfter(int x, int y, Rectangle alloc) {
                    return false;
                }

                protected View getViewAtPoint(int x, int y, Rectangle alloc) {
                    return null;
                }

                protected void childAllocation(int index, Rectangle a) {
                }
            }
        }
    }

/* This is useful for the nightmare of parsing multi-part HTTP/RFC822 headers
 * sensibly:
 * From a String like: 'timeout=15, max=5'
 * create an array of Strings:
 * { {"timeout", "15"},
 *   {"max", "5"}
 * }
 * From one like: 'Basic Realm="FuzzFace" Foo="Biz Bar Baz"'
 * create one like (no quotes in literal):
 * { {"basic", null},
 *   {"realm", "FuzzFace"}
 *   {"foo", "Biz Bar Baz"}
 * }
 * keys are converted to lower case, vals are left as is....
 *
 * author Dave Brown
 */ 


static class HeaderParser {

    /* table of key/val pairs - maxes out at 10!!!!*/
    String raw;
    String[][] tab;
    
    public HeaderParser(String raw) {
	this.raw = raw;
	tab = new String[10][2];
	parse();
    }

    private void parse() {
	
	if (raw != null) {
	    raw = raw.trim();
	    char[] ca = raw.toCharArray();
	    int beg = 0, end = 0, i = 0;
	    boolean inKey = true;
	    boolean inQuote = false;
	    int len = ca.length;
	    while (end < len) {
		char c = ca[end];
		if (c == '=') { // end of a key
		    tab[i][0] = new String(ca, beg, end-beg).toLowerCase();
		    inKey = false;
		    end++;
		    beg = end;
		} else if (c == '\"') {
		    if (inQuote) {
			tab[i++][1]= new String(ca, beg, end-beg);
			inQuote=false;
			do {
			    end++;
			} while (end < len && (ca[end] == ' ' || ca[end] == ','));
			inKey=true;
			beg=end;
		    } else {
			inQuote=true;
			end++;
			beg=end;
		    }
		} else if (c == ' ' || c == ',') { // end key/val, of whatever we're in
		    if (inQuote) {
			end++;
			continue;
		    } else if (inKey) {
			tab[i++][0] = (new String(ca, beg, end-beg)).toLowerCase();
		    } else {
			tab[i++][1] = (new String(ca, beg, end-beg));
		    }
		    while (end < len && (ca[end] == ' ' || ca[end] == ',')) {
			end++;
		    }
		    inKey = true;
		    beg = end;
		} else {
		    end++;
		}
	    } 
	    // get last key/val, if any
	    if (--end > beg) {
		if (!inKey) {
		    if (ca[end] == '\"') {
			tab[i++][1] = (new String(ca, beg, end-beg));
		    } else {
			tab[i++][1] = (new String(ca, beg, end-beg+1));
		    }
		} else {
		    tab[i][0] = (new String(ca, beg, end-beg+1)).toLowerCase();
		}
	    } else if (end == beg) {
		if (!inKey) {
		    if (ca[end] == '\"') {
			tab[i++][1] = String.valueOf(ca[end-1]);
		    } else {
			tab[i++][1] = String.valueOf(ca[end]);
		    }
		} else {
		    tab[i][0] = String.valueOf(ca[end]).toLowerCase();
		}
	    } 
	}
	
    }

    public String findKey(int i) {
	if (i < 0 || i > 10)
	    return null;
	return tab[i][0];
    }

    public String findValue(int i) {
	if (i < 0 || i > 10)
	    return null;
	return tab[i][1];
    }

    public String findValue(String key) {
	return findValue(key, null);
    }

    public String findValue(String k, String Default) {
	if (k == null)
	    return Default;
	k.toLowerCase();
	for (int i = 0; i < 10; ++i) {
	    if (tab[i][0] == null) {
		return Default;
	    } else if (k.equals(tab[i][0])) {
		return tab[i][1];
	    }
	}
	return Default;
    }

    public int findInt(String k, int Default) {
	try {
	    return Integer.parseInt(findValue(k, String.valueOf(Default)));
	} catch (Throwable t) {
	    return Default;
	}
    }
 }

}

