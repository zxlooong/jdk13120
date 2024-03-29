/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.text.html.parser;

import java.util.Vector;
import java.util.Enumeration;
import java.io.*;


/**
 * A representation of a content model. A content model is
 * basically a restricted BNF expression. It is restricted in
 * the sense that it must be deterministic. This means that you
 * don't have to represent it as a finite state automata.<p>
 * See Annex H on page 556 of the SGML handbook for more information.
 *
 * @author   Arthur van Hoff
 * @version  1.7,02/06/02
 *
 */
public final class ContentModel implements Serializable {
    /**
     * Type. Either '*', '?', '+', ',', '|', '&'.
     */
    public int type;

    /**
     * The content. Either an Element or a ContentModel.
     */
    public Object content;

    /**
     * The next content model (in a ',', '|' or '&' expression).
     */
    public ContentModel next;

    public ContentModel() {
    }

    /**
     * Create a content model for an element.
     */
    public ContentModel(Element content) {
	this(0, content, null);
    }

    /**
     * Create a content model of a particular type.
     */
    public ContentModel(int type, ContentModel content) {
	this(type, content, null);
    }

    /**
     * Create a content model of a particular type.
     */
    public ContentModel(int type, Object content, ContentModel next) {
	this.type = type;
	this.content = content;
	this.next = next;
    }

    /**
     * Return true if the content model could
     * match an empty input stream.
     */
    public boolean empty() {
	switch (type) {
	  case '*':
	  case '?':
	    return true;

	  case '+':
	  case '|':
	    for (ContentModel m = (ContentModel)content ; m != null ; m = m.next) {
		if (m.empty()) {
		    return true;
		}
	    }
	    return false;

	  case ',':
	  case '&':
	    for (ContentModel m = (ContentModel)content ; m != null ; m = m.next) {
		if (!m.empty()) {
		    return false;
		}
	    }
	    return true;

	  default:
	    return false;
	}
    }

    /**
     * Update elemVec with the list of elements that are
     * part of the this contentModel.
     */
     public void getElements(Vector elemVec) {
	 switch (type) {
	 case '*':
	 case '?':
	 case '+':
	     ((ContentModel)content).getElements(elemVec);
	     break;
	 case ',':
	 case '|':
	 case '&':
	     for (ContentModel m=(ContentModel)content; m != null; m=m.next){
		 m.getElements(elemVec);
	     }
	     break;
	 default:
	     elemVec.addElement(content);
	 }
     }

     private boolean valSet[];
     private boolean val[];
     // A cache used by first().  This cache was found to speed parsing
     // by about 10% (based on measurements of the 4-12 code base after
     // buffering was fixed).

    /**
     * Return true if the token could potentially be the
     * first token in the input stream.
     */
    public boolean first(Object token) {
	switch (type) {
	  case '*':
	  case '?':
	  case '+':
	    return ((ContentModel)content).first(token);

	  case ',':
	    for (ContentModel m = (ContentModel)content ; m != null ; m = m.next) {
		if (m.first(token)) {
		    return true;
		}
		if (!m.empty()) {
		    return false;
		}
	    }
	    return false;

	  case '|':
	  case '&': {
	    Element e = (Element) token;
	    if (valSet == null) {
		valSet = new boolean[Element.maxIndex + 1];
		val = new boolean[Element.maxIndex + 1];
		// All Element instances are created before this ever executes
	    }
	    if (valSet[e.index]) {
		return val[e.index];
	    }
	    for (ContentModel m = (ContentModel)content ; m != null ; m = m.next) {
		if (m.first(token)) {
		    val[e.index] = true;
		    break;
		}
	    }
	    valSet[e.index] = true;
	    return val[e.index];
	  }

	  default:
	    return (content == token);
	}
    }

    /**
     * Return the element that must be next.
     */
    public Element first() {
	switch (type) {
	  case '&':
	  case '|':
	  case '*':
	  case '?':
	    return null;

	  case '+':
	  case ',':
	    return ((ContentModel)content).first();

	  default:
	    return (Element)content;
	}
    }

    /**
     * Convert to a string.
     */
    public String toString() {
	switch (type) {
	  case '*':
	    return content + "*";
	  case '?':
	    return content + "?";
	  case '+':
	    return content + "+";

	  case ',':
	  case '|':
	  case '&':
	    char data[] = {' ', (char)type, ' '};
	    String str = "";
	    for (ContentModel m = (ContentModel)content ; m != null ; m = m.next) {
		str = str + m;
		if (m.next != null) {
		    str += new String(data);
		}
	    }
	    return "(" + str + ")";

	  default:
	    return content.toString();
	}
    }
}
