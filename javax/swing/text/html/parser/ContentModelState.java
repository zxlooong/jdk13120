/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.text.html.parser;

/**
 * A content model state. This is basically a list of pointers to
 * the BNF expression representing the model (the ContentModel).
 * Each element in a DTD has a content model which describes the
 * elements that may occur inside, and the order in which they can
 * occur.
 * <p>
 * Each time a token is reduced a new state is created.
 * <p>
 * See Annex H on page 556 of the SGML handbook for more information.
 *
 * @see Parser
 * @see DTD
 * @see Element
 * @see ContentModel
 * @author Arthur van Hoff
 * @version 	1.8 02/06/02
 */
class ContentModelState {
    ContentModel model;
    long value;
    ContentModelState next;

    /**
     * Create a content model state for a content model.
     */
    public ContentModelState(ContentModel model) {
	this(model, null, 0);
    }

    /**
     * Create a content model state for a content model given the
     * remaining state that needs to be reduce.
     */
    ContentModelState(Object content, ContentModelState next) {
	this(content, next, 0);
    }

    /**
     * Create a content model state for a content model given the
     * remaining state that needs to be reduce.
     */
    ContentModelState(Object content, ContentModelState next, long value) {
	this.model = (ContentModel)content;
	this.next = next;
	this.value = value;
    }

    /**
     * Return the content model that is relevant to the current state.
     */
    public ContentModel getModel() {
	ContentModel m = model;
	for (int i = 0; i < value; i++) {
	    if (m.next != null) {
		m = m.next;
	    } else {
		return null;
	    }
	}
        return m;
    }

    /**
     * Check if the state can be terminated. That is there are no more
     * tokens required in the input stream.
     * @return true if the model can terminate without further input
     */
    public boolean terminate() {
	switch (model.type) {
	  case '+':
	    if ((value == 0) && !(model).empty()) {
		return false;
	    }
	  case '*':
	  case '?':
	    return (next == null) || next.terminate();

	  case '|':
	    for (ContentModel m = (ContentModel)model.content ; m != null ; m = m.next) {
		if (m.empty()) {
		    return (next == null) || next.terminate();
		}
	    }
	    return false;

	  case '&': {
	    ContentModel m = (ContentModel)model.content;

	    for (int i = 0 ; m != null ; i++, m = m.next) {
		if ((value & (1L << i)) == 0) {
		    if (!m.empty()) {
			return false;
		    }
		}
	    }
	    return (next == null) || next.terminate();
	  }

	  case ',': {
	    ContentModel m = (ContentModel)model.content;
	    for (int i = 0 ; i < value ; i++, m = m.next);

	    for (; (m != null) && m.empty() ; m = m.next);
	    if (m != null) {
		return false;
	    }
	    return (next == null) || next.terminate();
	  }

	default:
	  return false;
	}
    }

    /**
     * Check if the state can be terminated. That is there are no more
     * tokens required in the input stream.
     * @return the only possible element that can occur next
     */
    public Element first() {
	switch (model.type) {
	  case '*':
	  case '?':
	  case '|':
	  case '&':
	    return null;

	  case '+':
	    return model.first();

	  case ',': {
	      ContentModel m = (ContentModel)model.content;
	      for (int i = 0 ; i < value ; i++, m = m.next);
	      return m.first();
	  }

	  default:
	    return model.first();
	}
    }

    /**
     * Advance this state to a new state. An exception is thrown if the
     * token is illegal at this point in the content model.
     * @return next state after reducing a token
     */
    public ContentModelState advance(Object token) {
	switch (model.type) {
	  case '+':
	    if (model.first(token)) {
		return new ContentModelState(model.content,
		        new ContentModelState(model, next, value + 1)).advance(token);
	    }
	    if (value != 0) {
		if (next != null) {
		    return next.advance(token);
		} else {
		    return null;
		}
	    }
	    break;

	  case '*':
	    if (model.first(token)) {
		return new ContentModelState(model.content, this).advance(token);
	    }
	    if (next != null) {
		return next.advance(token);
	    } else {
		return null;
	    }

	  case '?':
	    if (model.first(token)) {
		return new ContentModelState(model.content, next).advance(token);
	    }
	    if (next != null) {
		return next.advance(token);
	    } else {
		return null;
	    }

	  case '|':
	    for (ContentModel m = (ContentModel)model.content ; m != null ; m = m.next) {
		if (m.first(token)) {
		    return new ContentModelState(m, next).advance(token);
		}
	    }
	    break;

	  case ',': {
	    ContentModel m = (ContentModel)model.content;
	    for (int i = 0 ; i < value ; i++, m = m.next);

	    if (m.first(token) || m.empty()) {
		if (m.next == null) {
		    return new ContentModelState(m, next).advance(token);
		} else {
		    return new ContentModelState(m,
			    new ContentModelState(model, next, value + 1)).advance(token);
		}
	    }
	    break;
	  }

	  case '&': {
	    ContentModel m = (ContentModel)model.content;
	    boolean complete = true;

	    for (int i = 0 ; m != null ; i++, m = m.next) {
		if ((value & (1L << i)) == 0) {
		    if (m.first(token)) {
			return new ContentModelState(m,
			        new ContentModelState(model, next, value | (1L << i))).advance(token);
		    }
		    if (!m.empty()) {
			complete = false;
		    }
		}
	    }
	    if (complete) {
		if (next != null) {
		    return next.advance(token);
		} else {
		    return null;
		}
	    }
	    break;
	  }

	  default:
	    if (model.content == token) {
		return next;
	    }
	}

	// We used to throw this exception at this point.  However, it
	// was determined that throwing this exception was more expensive
	// than returning null, and we could not justify to ourselves why
	// it was necessary to throw an exception, rather than simply
	// returning null.  I'm leaving it in a commented out state so
	// that it can be easily restored if the situation ever arises.
	//
	// throw new IllegalArgumentException("invalid token: " + token);
	return null;
    }
}
