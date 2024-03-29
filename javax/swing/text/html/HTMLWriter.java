/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.text.*;
import java.io.Writer;
import java.util.Stack;
import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

/**
 * This is a writer for HTMLDocuments.
 *
 * @author  Sunita Mani
 * @version 1.27, 02/06/02
 */


public class HTMLWriter extends AbstractWriter {
    /*
     * Stores all elements for which end tags have to
     * be emitted.
     */
    private Stack blockElementStack = new Stack();
    private boolean inContent = false;
    private boolean inPre = false;
    /** When inPre is true, this will indicate the end offset of the pre
     * element. */
    private int preEndOffset;
    private boolean inTextArea = false;
    private boolean newlineOutputed = false;
    private boolean completeDoc;

    /*
     * Stores all embedded tags. Embedded tags are tags that are
     * stored as attributes in other tags. Generally they're
     * character level attributes.  Examples include
     * &lt;b&gt;, &lt;i&gt;, &lt;font&gt;, and &lt;a&gt;. 
     */
    private Vector tags = new Vector(10);

    /**
     * Values for the tags.
     */
    private Vector tagValues = new Vector(10);

    /**
     * Used when writing out content.
     */
    private Segment segment;

    /*
     * This is used in closeOutUnwantedEmbeddedTags.
     */
    private Vector tagsToRemove = new Vector(10);

    /**
     * Set to true after the head has been output.
     */
    private boolean wroteHead;

    /**
     * Set to true when entities (such as &lt;) should be replaced.
     */
    private boolean replaceEntities;

    /**
     * Temporary buffer.
     */
    private char[] tempChars;


    /**
     * Creates a new HTMLWriter.
     *
     * @param w   a Writer
     * @param doc  an HTMLDocument
     *
     */
    public HTMLWriter(Writer w, HTMLDocument doc) {
	this(w, doc, 0, doc.getLength());
    }

    /**
     * Creates a new HTMLWriter.
     *
     * @param w  a Writer
     * @param doc an HTMLDocument
     * @param pos the document location from which to fetch the content
     * @param len the amount to write out
     */
    public HTMLWriter(Writer w, HTMLDocument doc, int pos, int len) {
	super(w, doc, pos, len);
	completeDoc = (pos == 0 && len == doc.getLength());
	setLineLength(80);
    }

    /**
     * Iterates over the 
     * Element tree and controls the writing out of
     * all the tags and its attributes.
     *
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *            location within the document.
     *
     */
    public void write() throws IOException, BadLocationException {
	ElementIterator it = getElementIterator();
	Element current = null;
	Element next = null;

	wroteHead = false;
	setCurrentLineLength(0);
	replaceEntities = false;
	setCanWrapLines(false);
	if (segment == null) {
	    segment = new Segment();
	}
	inPre = false;
	while ((next = it.next()) != null) {
	    if (!inRange(next)) {
		continue;
	    }
	    if (current != null) {
		
		/*
		  if next is child of current increment indent
		*/

		if (indentNeedsIncrementing(current, next)) {
		    incrIndent();
		} else if (current.getParentElement() != next.getParentElement()) {
		    /*
		       next and current are not siblings
		       so emit end tags for items on the stack until the
		       item on top of the stack, is the parent of the
		       next.
		    */
		    Element top = (Element)blockElementStack.peek();
		    while (top != next.getParentElement()) {
			/*
			   pop() will return top.
			*/
			blockElementStack.pop();
			if (!synthesizedElement(top)) {
			    if (!matchNameAttribute(top.getAttributes(), HTML.Tag.PRE)) {
				decrIndent();
			    }
			    endTag(top);
			}
			top = (Element)blockElementStack.peek();
		    }
		} else if (current.getParentElement() == next.getParentElement()) {
		    /*
		       if next and current are siblings the indent level
		       is correct.  But, we need to make sure that if current is
		       on the stack, we pop it off, and put out its end tag.
		    */
		    Element top = (Element)blockElementStack.peek();
		    if (top == current) {
			blockElementStack.pop();
			endTag(top);
		    }
		}
	    }
	    if (!next.isLeaf() || isFormElementWithContent(next.getAttributes())) {
		blockElementStack.push(next);
		startTag(next);
	    } else {
		emptyTag(next);
	    }
	    current = next;
	}

	/* Emit all remaining end tags */

	/* A null parameter ensures that all embedded tags
	   currently in the tags vector have their
	   corresponding end tags written out.
	*/
	closeOutUnwantedEmbeddedTags(null);

	while (!blockElementStack.empty()) {
	    current = (Element)blockElementStack.pop();
	    if (!synthesizedElement(current)) {
		if (!matchNameAttribute(current.getAttributes(), HTML.Tag.PRE)) {
		    decrIndent();
		}
		endTag(current);
	    }
	}

	if (completeDoc) {
	    writeAdditionalComments();
	}

	segment.array = null;
    }


    /**
     * Writes out the attribute set.  Ignores all
     * attributes with a key of type HTML.Tag,
     * attributes with a key of type StyleConstants,
     * and attributes with a key of type
     * HTML.Attribute.ENDTAG.
     *
     * @param attr   an AttributeSet
     * @exception IOException on any I/O error
     *
     */
    protected void writeAttributes(AttributeSet attr) throws IOException {
	// translate css attributes to html
	convAttr.removeAttributes(convAttr);
	convertToHTML32(attr, convAttr);

	Enumeration names = convAttr.getAttributeNames();
	while (names.hasMoreElements()) {
	    Object name = names.nextElement();
	    if (name instanceof HTML.Tag || 
		name instanceof StyleConstants || 
		name == HTML.Attribute.ENDTAG) {
		continue;
	    }
	    write(" " + name + "=\"" + convAttr.getAttribute(name) + "\"");
	}
    }

    /**
     * Writes out all empty elements (all tags that have no
     * corresponding end tag).
     *
     * @param elem   an Element
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *            location within the document.
     */
    protected void emptyTag(Element elem) throws BadLocationException, IOException {

	if (!inContent && !inPre) {
	    indent();
	}

	AttributeSet attr = elem.getAttributes();
	closeOutUnwantedEmbeddedTags(attr);
	writeEmbeddedTags(attr);

	if (matchNameAttribute(attr, HTML.Tag.CONTENT)) {
	    inContent = true;
	    text(elem);
	} else if (matchNameAttribute(attr, HTML.Tag.COMMENT)) {
	    comment(elem);
	}  else {
	    boolean isBlock = isBlockTag(elem.getAttributes());
	    if (inContent && isBlock ) {
		writeLineSeparator();
		indent();
	    }

	    Object nameTag = (attr != null) ? attr.getAttribute
		              (StyleConstants.NameAttribute) : null;
	    Object endTag = (attr != null) ? attr.getAttribute
		              (HTML.Attribute.ENDTAG) : null;

	    boolean outputEndTag = false;
	    // If an instance of an UNKNOWN Tag, or an instance of a 
	    // tag that is only visible during editing
	    //
	    if (nameTag != null && endTag != null &&
		(endTag instanceof String) &&
		((String)endTag).equals("true")) {
		outputEndTag = true;
	    }

	    if (completeDoc && matchNameAttribute(attr, HTML.Tag.HEAD)) {
		if (outputEndTag) {
		    // Write out any styles.
		    writeStyles(((HTMLDocument)getDocument()).getStyleSheet());
		}
		wroteHead = true;
	    }

	    write('<');
	    if (outputEndTag) {
		write('/');
	    }
	    write(elem.getName());
	    writeAttributes(attr);
	    write('>');
	    if (matchNameAttribute(attr, HTML.Tag.TITLE) && !outputEndTag) {
		Document doc = elem.getDocument();
		String title = (String)doc.getProperty(Document.TitleProperty);
		write(title);
	    } else if (!inContent || isBlock) {
		writeLineSeparator();
		if (isBlock && inContent) {
		    indent();
		}
	    }
	}
    }

    /**
     * Determines if the HTML.Tag associated with the
     * element is a block tag.
     *
     * @param attr  an AttributeSet
     * @return  true if tag is block tag, false otherwise.
     */
    protected boolean isBlockTag(AttributeSet attr) {
	Object o = attr.getAttribute(StyleConstants.NameAttribute);
	if (o instanceof HTML.Tag) {
	    HTML.Tag name = (HTML.Tag) o;
	    return name.isBlock();
	}
	return false;
    }


    /**
     * Writes out a start tag for the element.
     * Ignores all synthesized elements.
     *
     * @param elem   an Element
     * @exception IOException on any I/O error
     */
    protected void startTag(Element elem) throws IOException, BadLocationException {

	if (synthesizedElement(elem)) {
	    return;
	}

	// Determine the name, as an HTML.Tag.
	AttributeSet attr = elem.getAttributes();
	Object nameAttribute = attr.getAttribute(StyleConstants.NameAttribute);
	HTML.Tag name;
	if (nameAttribute instanceof HTML.Tag) {
	    name = (HTML.Tag)nameAttribute;
	}
	else {
	    name = null;
	}

	if (name == HTML.Tag.PRE) {
	    inPre = true;
	    preEndOffset = elem.getEndOffset();
	}

	// write out end tags for item on stack
	closeOutUnwantedEmbeddedTags(attr);

	if (inContent) { 
	    writeLineSeparator();
	    inContent = false;
	    newlineOutputed = false;
	}

	if (completeDoc && name == HTML.Tag.BODY && !wroteHead) {
	    // If the head has not been output, output it and the styles.
	    wroteHead = true;
	    indent();
	    write("<head>");
	    writeLineSeparator();
	    incrIndent();
	    writeStyles(((HTMLDocument)getDocument()).getStyleSheet());
	    decrIndent();
	    writeLineSeparator();
	    indent();
	    write("</head>");
	    writeLineSeparator();
	}

	indent();
	write('<');
	write(elem.getName());
	writeAttributes(attr);
	write('>');
	if (name != HTML.Tag.PRE) {
	    writeLineSeparator();
	}

	if (name == HTML.Tag.TEXTAREA) {
	    textAreaContent(elem.getAttributes());
	} else if (name == HTML.Tag.SELECT) {
	    selectContent(elem.getAttributes());
	} else if (completeDoc && name == HTML.Tag.BODY) {
	    // Write out the maps, which is not stored as Elements in
	    // the Document.
	    writeMaps(((HTMLDocument)getDocument()).getMaps());
	}
	else if (name == HTML.Tag.HEAD) {
	    wroteHead = true;
	}
    }

    
    /**
     * Writes out text that is contained in a TEXTAREA form
     * element.
     *
     * @param attr  an AttributeSet
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *            location within the document.
     */
    protected void textAreaContent(AttributeSet attr) throws BadLocationException, IOException {
	Document doc = (Document)attr.getAttribute(StyleConstants.ModelAttribute);
	if (doc != null && doc.getLength() > 0) {
	    if (segment == null) {
		segment = new Segment();
	    }
	    doc.getText(0, doc.getLength(), segment);
	    if (segment.count > 0) {
		inTextArea = true;
		incrIndent();
		indent();
		setCanWrapLines(true);
		replaceEntities = true;
		write(segment.array, segment.offset, segment.count);
		replaceEntities = false;
		setCanWrapLines(false);
		writeLineSeparator();
		inTextArea = false;
		decrIndent();
	    }
	}
    }


    /**
     * Writes out text.  If a range is specified when the constructor
     * is invoked, then only the appropriate range of text is written
     * out.
     *
     * @param elem   an Element
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *            location within the document.
     */
    protected void text(Element elem) throws BadLocationException, IOException {
	int start = Math.max(getStartOffset(), elem.getStartOffset());
	int end = Math.min(getEndOffset(), elem.getEndOffset());
	if (start < end) {
	    if (segment == null) {
		segment = new Segment();
	    }
	    getDocument().getText(start, end - start, segment);
	    newlineOutputed = false;
	    if (segment.count > 0) {
		if (segment.array[segment.offset + segment.count - 1] == '\n'){
		    newlineOutputed = true;
		}
		if (inPre && end == preEndOffset) {
		    if (segment.count > 1) {
			segment.count--;
		    }
		    else {
			return;
		    }
		}
		replaceEntities = true;
		setCanWrapLines(!inPre);
		write(segment.array, segment.offset, segment.count);
		setCanWrapLines(false);
		replaceEntities = false;
	    }
	}
    }

    /**
     * Writes out the content of the SELECT form element.
     * 
     * @param attr the AttributeSet associated with the form element
     * @exception IOException on any I/O error
     */
    protected void selectContent(AttributeSet attr) throws IOException {
	Object model = attr.getAttribute(StyleConstants.ModelAttribute);
	incrIndent();
	if (model instanceof OptionListModel) {
	    OptionListModel listModel = (OptionListModel)model;
	    int size = listModel.getSize();
	    for (int i = 0; i < size; i++) {
		Option option = (Option)listModel.getElementAt(i);
		writeOption(option);
	    }
	} else if (model instanceof OptionComboBoxModel) {
	    OptionComboBoxModel comboBoxModel = (OptionComboBoxModel)model;
	    int size = comboBoxModel.getSize();
	    for (int i = 0; i < size; i++) {
		Option option = (Option)comboBoxModel.getElementAt(i);
		writeOption(option);
	    }
	}
	decrIndent();
    }


    /**
     * Writes out the content of the Option form element.
     * @param option  an Option
     * @exception IOException on any I/O error
     * 
     */
    protected void writeOption(Option option) throws IOException {
	
	indent();
	write('<');
	write("option ");
	if (option.getValue() != null) {
	    write("value="+ option.getValue());
	}
	if (option.isSelected()) {
	    write(" selected");
	}
	write('>');
	if (option.getLabel() != null) {
	    write(option.getLabel());
	}
	writeLineSeparator();
    }

    /**
     * Writes out an end tag for the element.
     *
     * @param elem    an Element
     * @exception IOException on any I/O error
     */
    protected void endTag(Element elem) throws IOException {
	if (synthesizedElement(elem)) {
	    return;
	}
	if (matchNameAttribute(elem.getAttributes(), HTML.Tag.PRE)) {
	    inPre = false;
	}

	// write out end tags for item on stack
	closeOutUnwantedEmbeddedTags(elem.getAttributes());
	if (inContent) { 
	    if (!newlineOutputed) {
		writeLineSeparator();
	    }
	    newlineOutputed = false;
	    inContent = false;
	}
	indent();
        write('<');
        write('/');
        write(elem.getName());
        write('>');
	writeLineSeparator();
    }



    /**
     * Writes out comments.
     *
     * @param elem    an Element
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *            location within the document.
     */
    protected void comment(Element elem) throws BadLocationException, IOException {
	AttributeSet as = elem.getAttributes();
	if (matchNameAttribute(as, HTML.Tag.COMMENT)) {
	    Object comment = as.getAttribute(HTML.Attribute.COMMENT);
	    if (comment instanceof String) {
		writeComment((String)comment);
	    }
	    else {
		writeComment(null);
	    }
	}
    }


    /**
     * Writes out comment string.
     *
     * @param string   the comment
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *            location within the document.
     */
    void writeComment(String string) throws IOException {
	write("<!--");
	if (string != null) {
	    write(string);
	}
	write("-->");
	writeLineSeparator();
    }


    /**
     * Writes out any additional comments (comments outside of the body)
     * stored under the property HTMLDocument.AdditionalComments.
     */
    void writeAdditionalComments() throws IOException {
	Object comments = getDocument().getProperty
	                                (HTMLDocument.AdditionalComments);

	if (comments instanceof Vector) {
	    Vector v = (Vector)comments;
	    for (int counter = 0, maxCounter = v.size(); counter < maxCounter;
		 counter++) {
		writeComment(v.elementAt(counter).toString());
	    }
	}
    }


    /**
     * Returns true if the element is a
     * synthesized element.  Currently we are only testing
     * for the p-implied tag.
     */
    protected boolean synthesizedElement(Element elem) {
	if (matchNameAttribute(elem.getAttributes(), HTML.Tag.IMPLIED)) {
	    return true;
	}
	return false;
    }


    /**
     * Returns true if the StyleConstants.NameAttribute is
     * equal to the tag that is passed in as a parameter.
     */
    protected boolean matchNameAttribute(AttributeSet attr, HTML.Tag tag) {
	Object o = attr.getAttribute(StyleConstants.NameAttribute);
	if (o instanceof HTML.Tag) {
	    HTML.Tag name = (HTML.Tag) o;
	    if (name == tag) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Searches for embedded tags in the AttributeSet
     * and writes them out.  It also stores these tags in a vector
     * so that when appropriate the corresponding end tags can be
     * written out.
     *
     * @exception IOException on any I/O error
     */
    protected void writeEmbeddedTags(AttributeSet attr) throws IOException {
	
	// translate css attributes to html
	attr = convertToHTML(attr, oConvAttr);

	Enumeration names = attr.getAttributeNames();
	while (names.hasMoreElements()) {
	    Object name = names.nextElement();
	    if (name instanceof HTML.Tag) {
		HTML.Tag tag = (HTML.Tag)name;
		if (tag == HTML.Tag.FORM || tags.contains(tag)) {
		    continue;
		}
		write('<');
		write(tag.toString());
		Object o = attr.getAttribute(tag);
		if (o != null && o instanceof AttributeSet) {
		    writeAttributes((AttributeSet)o);
		}
		write('>');
		tags.addElement(tag);
		tagValues.addElement(o);
	    }
	}
    }


    /**
     * Searches the attribute set for a tag, both of which
     * are passed in as a parameter.  Returns true if no match is found
     * and false otherwise.
     */
    private boolean noMatchForTagInAttributes(AttributeSet attr, HTML.Tag t,
					      Object tagValue) {
	if (attr != null && attr.isDefined(t)) {
	    Object newValue = attr.getAttribute(t);

	    if ((tagValue == null) ? (newValue == null) :
		(newValue != null && tagValue.equals(newValue))) {
		return false;
	    }
	}
	return true;
    }


    /**
     * Searches the attribute set and for each tag
     * that is stored in the tag vector.  If the tag isnt found,
     * then the tag is removed from the vector and a corresponding
     * end tag is written out.
     *
     * @exception IOException on any I/O error
     */
    protected void closeOutUnwantedEmbeddedTags(AttributeSet attr) throws IOException {

	tagsToRemove.removeAllElements();

	// translate css attributes to html
	attr = convertToHTML(attr, null);

	HTML.Tag t;
	Object tValue;
	int firstIndex = -1;
	int size = tags.size();
	// First, find all the tags that need to be removed.
	for (int i = size - 1; i >= 0; i--) {
	    t = (HTML.Tag)tags.elementAt(i);
	    tValue = tagValues.elementAt(i);
	    if ((attr == null) || noMatchForTagInAttributes(attr, t, tValue)) {
		firstIndex = i;
		tagsToRemove.addElement(t);
	    }
	}
	if (firstIndex != -1) {
	    // Then close them out.
	    boolean removeAll = ((size - firstIndex) == tagsToRemove.size());
	    for (int i = size - 1; i >= firstIndex; i--) {
		t = (HTML.Tag)tags.elementAt(i);
		if (removeAll || tagsToRemove.contains(t)) {
		    tags.removeElementAt(i);
		    tagValues.removeElementAt(i);
		}
		write('<');
		write('/');
		write(t.toString());
		write('>');
	    }
	    // Have to output any tags after firstIndex that still remaing,
	    // as we closed them out, but they should remain open.
	    size = tags.size();
	    for (int i = firstIndex; i < size; i++) {
		t = (HTML.Tag)tags.elementAt(i);
		write('<');
		write(t.toString());
		Object o = tagValues.elementAt(i);
		if (o != null && o instanceof AttributeSet) {
		    writeAttributes((AttributeSet)o);
		}
		write('>');
	    }
	}
    }


    /**
     * Determines if the element associated with the attributeset
     * is a TEXTAREA or SELECT.  If true, returns true else
     * false
     */
    private boolean isFormElementWithContent(AttributeSet attr) {
	if (matchNameAttribute(attr, HTML.Tag.TEXTAREA) ||
	    matchNameAttribute(attr, HTML.Tag.SELECT)) {
	    return true;
	}
	return false;
    }


    /**
     * Determines whether a the indentation needs to be
     * incremented.  Basically, if next is a child of current, and
     * next is NOT a synthesized element, the indent level will be
     * incremented.  If there is a parent-child relationship and "next"
     * is a synthesized element, then its children must be indented.
     * This state is maintained by the indentNext boolean.
     *
     * @return boolean that's true if indent level
     *         needs incrementing.
     */
    private boolean indentNext = false;
    private boolean indentNeedsIncrementing(Element current, Element next) {
	if ((next.getParentElement() == current) && !inPre) {
	    if (indentNext) {
		indentNext = false;
		return true;
	    } else if (synthesizedElement(next)) {
		indentNext = true;
	    } else if (!synthesizedElement(current)){
		return true;
	    }
	}
	return false;
    }

    /**
     * Outputs the maps as elements. Maps are not stored as elements in
     * the document, and as such this is used to output them.
     */
    void writeMaps(Enumeration maps) throws IOException {
	if (maps != null) {
	    while(maps.hasMoreElements()) {
		Map map = (Map)maps.nextElement();
		String name = map.getName();

		incrIndent();
		indent();
		write("<map");
		if (name != null) {
		    write(" name=\"");
		    write(name);
		    write("\">");
		}
		else {
		    write('>');
		}
		writeLineSeparator();
		incrIndent();

		// Output the areas
		AttributeSet[] areas = map.getAreas();
		if (areas != null) {
		    for (int counter = 0, maxCounter = areas.length;
			 counter < maxCounter; counter++) {
			indent();
			write("<area");
			writeAttributes(areas[counter]);
			write("></area>");
			writeLineSeparator();
		    }
		}
		decrIndent();
		indent();
		write("</map>");
		writeLineSeparator();
		decrIndent();
	    }
	}
    }

    /**
     * Outputs the styles as a single element. Styles are not stored as
     * elements, but part of the document. For the time being styles are
     * written out as a comment, inside a style tag.
     */
    void writeStyles(StyleSheet sheet) throws IOException {
	if (sheet != null) {
	    Enumeration styles = sheet.getStyleNames();
	    if (styles != null) {
		boolean outputStyle = false;
		while (styles.hasMoreElements()) {
		    String name = (String)styles.nextElement();
		    // Don't write out the default style.
		    if (!StyleContext.DEFAULT_STYLE.equals(name) &&
			writeStyle(name, sheet.getStyle(name), outputStyle)) {
			outputStyle = true;
		    }
		}
		if (outputStyle) {
		    writeStyleEndTag();
		}
	    }
	}
    }

    /**
     * Outputs the named style. <code>outputStyle</code> indicates
     * whether or not a style has been output yet. This will return
     * true if a style is written.
     */
    boolean writeStyle(String name, Style style, boolean outputStyle)
	         throws IOException{
	boolean didOutputStyle = false;
	Enumeration attributes = style.getAttributeNames();
	if (attributes != null) {
	    while (attributes.hasMoreElements()) {
		Object attribute = attributes.nextElement();
		if (attribute instanceof CSS.Attribute) {
		    String value = style.getAttribute(attribute).toString();
		    if (value != null) {
			if (!outputStyle) {
			    writeStyleStartTag();
			    outputStyle = true;
			}
			if (!didOutputStyle) {
			    didOutputStyle = true;
			    indent();
			    write(name);
			    write(" {");
			}
			else {
			    write(";");
			}
			write(' ');
			write(attribute.toString());
			write(": ");
			write(value);
		    }
		}
	    }
	}
	if (didOutputStyle) {
	    write(" }");
	    writeLineSeparator();
	}
	return didOutputStyle;
    }

    void writeStyleStartTag() throws IOException {
	indent();
	write("<style type=\"text/css\">");
	incrIndent();
	writeLineSeparator();
	indent();
	write("<!--");
	incrIndent();
	writeLineSeparator();
    }

    void writeStyleEndTag() throws IOException {
	decrIndent();
	indent();
	write("-->");
	writeLineSeparator();
	decrIndent();
	indent();
	write("</style>");
	writeLineSeparator();
	indent();
    }

    // --- conversion support ---------------------------

    /**
     * Convert the give set of attributes to be html for
     * the purpose of writing them out.  Any keys that
     * have been converted will not appear in the resultant
     * set.  Any keys not converted will appear in the 
     * resultant set the same as the received set.<p>
     * This will put the converted values into <code>to</code>, unless
     * it is null in which case a temporary AttributeSet will be returned.
     */
    AttributeSet convertToHTML(AttributeSet from, MutableAttributeSet to) {
	if (to == null) {
	    to = convAttr;
	}
	to.removeAttributes(to);
	if (writeCSS) {
	    convertToHTML40(from, to);
	} else {
	    convertToHTML32(from, to);
	}
	return to;
    }

    /**
     * If true, the writer will emit CSS attributes in preference
     * to HTML tags/attributes (i.e. It will emit an HTML 4.0
     * style).
     */
    private boolean writeCSS = false;

    /**
     * Buffer for the purpose of attribute conversion
     */
    private MutableAttributeSet convAttr = new SimpleAttributeSet();

    /**
     * Buffer for the purpose of attribute conversion. This can be
     * used if convAttr is being used.
     */
    private MutableAttributeSet oConvAttr = new SimpleAttributeSet();

    /**
     * Create an older style of HTML attributes.  This will 
     * convert character level attributes that have a StyleConstants
     * mapping over to an HTML tag/attribute.  Other CSS attributes
     * will be placed in an HTML style attribute.
     */
    private static void convertToHTML32(AttributeSet from, MutableAttributeSet to) {
	if (from == null) {
	    return;
	}
	Enumeration keys = from.getAttributeNames();
	String value = "";
	while (keys.hasMoreElements()) {
	    Object key = keys.nextElement();
	    if (key instanceof CSS.Attribute) {
		if ((key == CSS.Attribute.FONT_FAMILY) ||
		    (key == CSS.Attribute.FONT_SIZE) ||
		    (key == CSS.Attribute.COLOR)) {
		    
		    createFontAttribute((CSS.Attribute)key, from, to);
		} else if (key == CSS.Attribute.FONT_WEIGHT) {
		    // add a bold tag is weight is bold
		    CSS.FontWeight weightValue = (CSS.FontWeight) 
			from.getAttribute(CSS.Attribute.FONT_WEIGHT);
		    if ((weightValue != null) && (weightValue.getValue() > 400)) {
			to.addAttribute(HTML.Tag.B, SimpleAttributeSet.EMPTY);
		    }
		} else if (key == CSS.Attribute.FONT_STYLE) {
		    String s = from.getAttribute(key).toString();
		    if (s.indexOf("italic") >= 0) {
			to.addAttribute(HTML.Tag.I, SimpleAttributeSet.EMPTY);
		    }
		} else if (key == CSS.Attribute.TEXT_DECORATION) {
		    String decor = from.getAttribute(key).toString();
		    if (decor.indexOf("underline") >= 0) {
			to.addAttribute(HTML.Tag.U, SimpleAttributeSet.EMPTY);
		    }
		    if (decor.indexOf("line-through") >= 0) {
			to.addAttribute(HTML.Tag.STRIKE, SimpleAttributeSet.EMPTY);
		    }
		} else if (key == CSS.Attribute.VERTICAL_ALIGN) {
		    String vAlign = from.getAttribute(key).toString();
		    if (vAlign.indexOf("sup") >= 0) {
			to.addAttribute(HTML.Tag.SUP, SimpleAttributeSet.EMPTY);
		    }
		    if (vAlign.indexOf("sub") >= 0) {
			to.addAttribute(HTML.Tag.SUB, SimpleAttributeSet.EMPTY);
		    }
		} else if (key == CSS.Attribute.TEXT_ALIGN) {
		    to.addAttribute(HTML.Attribute.ALIGN, 
				    from.getAttribute(key).toString());
		} else {
		    // default is to store in a HTML style attribute
		    if (value.length() > 0) {
			value = value + "; ";
		    }
		    value = value + key + ": " + from.getAttribute(key);
		}
	    } else {
		to.addAttribute(key, from.getAttribute(key));
	    }
	}
	if (value.length() > 0) {
	    to.addAttribute(HTML.Attribute.STYLE, value);
	}
    }

    /**
     * Create/update an HTML &lt;font&gt; tag attribute.  The
     * value of the attribute should be a MutableAttributeSet so
     * that the attributes can be updated as they are discovered.
     */
    private static void createFontAttribute(CSS.Attribute a, AttributeSet from, 
				    MutableAttributeSet to) {
	MutableAttributeSet fontAttr = (MutableAttributeSet) 
	    to.getAttribute(HTML.Tag.FONT);
	if (fontAttr == null) {
	    fontAttr = new SimpleAttributeSet();
	    to.addAttribute(HTML.Tag.FONT, fontAttr);
	}
	// edit the parameters to the font tag
	String htmlValue = from.getAttribute(a).toString();
	if (a == CSS.Attribute.FONT_FAMILY) {
	    fontAttr.addAttribute(HTML.Attribute.FACE, htmlValue);
	} else if (a == CSS.Attribute.FONT_SIZE) {
	    fontAttr.addAttribute(HTML.Attribute.SIZE, htmlValue);
	} else if (a == CSS.Attribute.COLOR) {
	    fontAttr.addAttribute(HTML.Attribute.COLOR, htmlValue);
	}
    }
	
    /**
     * Copies the given AttributeSet to a new set, converting
     * any CSS attributes found to arguments of an HTML style
     * attribute.
     */
    private static void convertToHTML40(AttributeSet from, MutableAttributeSet to) {
	Enumeration keys = from.getAttributeNames();
	String value = "";
	while (keys.hasMoreElements()) {
	    Object key = keys.nextElement();
	    if (key instanceof CSS.Attribute) {
		value = value + " " + key + "=" + from.getAttribute(key) + ";";
	    } else {
		to.addAttribute(key, from.getAttribute(key));
	    }
	}
	if (value.length() > 0) {
	    to.addAttribute(HTML.Attribute.STYLE, value);
	}
    }

    //
    // Overrides the writing methods to only break a string when
    // canBreakString is true.
    // In a future release it is likely AbstractWriter will get this
    // functionality.
    //

    /**
     * Writes the line separator. This is overriden to make sure we don't
     * replace the newline content in case it is outside normal ascii.
     */
    protected void writeLineSeparator() throws IOException {
	boolean oldReplace = replaceEntities;
	replaceEntities = false;
	super.writeLineSeparator();
	replaceEntities = oldReplace;
    }

    /**
     * This method is overriden to map any character entities, such as
     * &lt; to &amp;lt;. <code>super.output</code> will be invoked to
     * write the content.
     */
    protected void output(char[] chars, int start, int length)
	           throws IOException {
	if (!replaceEntities) {
	    super.output(chars, start, length);
	    return;
	}
	int last = start;
	length += start;
	for (int counter = start; counter < length; counter++) {
	    // This will change, we need better support character level
	    // entities.
	    switch(chars[counter]) {
		// Character level entities.
	    case '<':
		if (counter > last) {
		    super.output(chars, last, counter - last);
		}
		last = counter + 1;
		output("&lt;");
		break;
	    case '>':
		if (counter > last) {
		    super.output(chars, last, counter - last);
		}
		last = counter + 1;
		output("&gt;");
		break;
	    case '&':
		if (counter > last) {
		    super.output(chars, last, counter - last);
		}
		last = counter + 1;
		output("&amp;");
		break;
	    case '"':
		if (counter > last) {
		    super.output(chars, last, counter - last);
		}
		last = counter + 1;
		output("&quot;");
		break;
		// Special characters
	    case '\n':
	    case '\t':
	    case '\r':
		break;
	    default:
		if (chars[counter] < ' ' || chars[counter] > 127) {
		    if (counter > last) {
			super.output(chars, last, counter - last);
		    }
		    last = counter + 1;
		    // If the character is outside of ascii, write the
		    // numeric value.
		    output("&#");
		    output(String.valueOf((int)chars[counter]));
		    output(";");
		}
		break;
	    }
	}
	if (last < length) {
	    super.output(chars, last, length - last);
	}
    }

    /**
     * This directly invokes super's <code>output</code> after converting
     * <code>string</code> to a char[].
     */
    private void output(String string) throws IOException {
	int length = string.length();
	if (tempChars == null || tempChars.length < length) {
	    tempChars = new char[length];
	}
	string.getChars(0, length, tempChars, 0);
	super.output(tempChars, 0, length);
    }
}
