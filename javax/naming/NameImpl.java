/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.naming;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.util.NoSuchElementException;

/**
  * The implementation class for CompoundName and CompositeName.
  * This class is package private.
  *
  * @author Rosanna Lee
  * @author Scott Seligman
  * @author Aravindan Ranganathan
  * @version 1.5 02/02/06
  * @since 1.3
  */

class NameImpl implements java.io.Serializable {

    private Vector components;

    private String syntaxDirection;
    private String syntaxSeparator;
    private String syntaxSeparator2;
    private boolean syntaxCaseInsensitive;
    private boolean syntaxTrimBlanks;
    private String syntaxEscape;
    private String syntaxBeginQuote1;
    private String syntaxEndQuote1;
    private String syntaxBeginQuote2;
    private String syntaxEndQuote2;
    private String syntaxAvaSeparator;
    private String syntaxTypevalSeparator;

    // escapingStyle gives the method used at creation time for
    // quoting or escaping characters in the name.  It is set to the
    // first style of quote or escape encountered if and when the name
    // is parsed.
    private final int STYLE_NONE = 0;
    private final int STYLE_QUOTE1 = 1;
    private final int STYLE_QUOTE2 = 2;
    private final int STYLE_ESCAPE = 3;
    private int escapingStyle = STYLE_NONE;

    // Returns true if "match" is not null, and n contains "match" at
    // position i.
    private final boolean isA(String n, int i, String match) {
	return (match != null && n.startsWith(match, i));
    }

    private final boolean isMeta(String n, int i) {
	return (isA(n, i, syntaxEscape) ||
		isA(n, i, syntaxBeginQuote1) ||
		isA(n, i, syntaxBeginQuote2) ||
		isSeparator(n, i));
    }

    private final boolean isSeparator(String n, int i) {
	return (isA(n, i, syntaxSeparator) ||
		isA(n, i, syntaxSeparator2));
    }

    private final int skipSeparator(String name, int i) {
	if (isA(name, i, syntaxSeparator)) {
	    i += syntaxSeparator.length();
	} else if (isA(name, i, syntaxSeparator2)) {
	    i += syntaxSeparator2.length();
	}
	return (i);
    }

    private final int extractComp(String name, int i, int len, Vector comps) 
    throws InvalidNameException {
	String beginQuote;
	String endQuote;
	boolean start = true;
	boolean one = false;
	StringBuffer answer = new StringBuffer(len);

	while (i < len) {
	    // handle quoted strings
	    if (start && ((one = isA(name, i, syntaxBeginQuote1)) ||
			  isA(name, i, syntaxBeginQuote2))) {

		// record choice of quote chars being used
		beginQuote = one ? syntaxBeginQuote1 : syntaxBeginQuote2;
		endQuote = one ? syntaxEndQuote1 : syntaxEndQuote2;
		if (escapingStyle == STYLE_NONE) {
		    escapingStyle = one ? STYLE_QUOTE1 : STYLE_QUOTE2;
		}

		// consume string until matching quote
		for (i += beginQuote.length();
		     ((i < len) && !name.startsWith(endQuote, i));
		     i++) {
		    // skip escape character if it is escaping ending quote
		    // otherwise leave as is.
		    if (isA(name, i, syntaxEscape) &&
			isA(name, i + syntaxEscape.length(), endQuote)) {
			i += syntaxEscape.length();
		    }
		    answer.append(name.charAt(i));  // copy char
		}

		// no ending quote found
		if (i >= len)
		    throw 
			new InvalidNameException(name + ": no close quote");
//			new Exception("no close quote");

		i += endQuote.length();

		// verify that end-quote occurs at separator or end of string
		if (i == len || isSeparator(name, i)) {
		    break;
		}
//		throw (new Exception(
		throw (new InvalidNameException(name +
		    ": close quote appears before end of component"));

	    } else if (isSeparator(name, i)) {
		break;

	    } else if (isA(name, i, syntaxEscape)) {
		if (isMeta(name, i + syntaxEscape.length())) {
		    // if escape preceeds meta, consume escape and let
		    // meta through
		    i += syntaxEscape.length();
		    if (escapingStyle == STYLE_NONE) {
			escapingStyle = STYLE_ESCAPE;
		    }
		} else if (i + syntaxEscape.length() >= len) {
		    throw (new InvalidNameException(name +
			": unescaped " + syntaxEscape + " at end of component"));
		}
	    } else if (isA(name, i, syntaxTypevalSeparator) &&
	((one = isA(name, i+syntaxTypevalSeparator.length(), syntaxBeginQuote1)) ||
	    isA(name, i+syntaxTypevalSeparator.length(), syntaxBeginQuote2))) {
		// Handle quote occurring after typeval separator
		beginQuote = one ? syntaxBeginQuote1 : syntaxBeginQuote2;
		endQuote = one ? syntaxEndQuote1 : syntaxEndQuote2;

		i += syntaxTypevalSeparator.length();
		answer.append(syntaxTypevalSeparator+beginQuote); // add back
		
		// consume string until matching quote
		for (i += beginQuote.length();
		     ((i < len) && !name.startsWith(endQuote, i));
		     i++) {
		    // skip escape character if it is escaping ending quote
		    // otherwise leave as is.
		    if (isA(name, i, syntaxEscape) &&
			isA(name, i + syntaxEscape.length(), endQuote)) {
			i += syntaxEscape.length();
		    }
		    answer.append(name.charAt(i));  // copy char
		}

		// no ending quote found
		if (i >= len)
		    throw 
			new InvalidNameException(name + ": typeval no close quote");

		i += endQuote.length();
		answer.append(endQuote); // add back

		// verify that end-quote occurs at separator or end of string
		if (i == len || isSeparator(name, i)) {
		    break;
		}
		throw (new InvalidNameException(name.substring(i) +
		    ": typeval close quote appears before end of component"));
	    }

	    answer.append(name.charAt(i++));
	    start = false;
	}

	if (syntaxDirection.equals("right_to_left"))
	    comps.insertElementAt(answer.toString(), 0);
	else
	    comps.addElement(answer.toString());
	return i;
    }

    private static boolean getBoolean(Properties p, String name) {
	return toBoolean(p.getProperty(name));
    }

    private static boolean toBoolean(String name) { 
	return ((name != null) && name.toLowerCase().equals("true"));
    }

    private final void recordNamingConvention(Properties p) {
	syntaxDirection = p.getProperty("jndi.syntax.direction", "flat");
	if (!syntaxDirection.equals("left_to_right") &&
	    !syntaxDirection.equals("right_to_left") &&
	    !syntaxDirection.equals("flat")) {
	    throw new IllegalArgumentException(syntaxDirection +
		"is not a valid value for the jndi.syntax.direction property");
	}	

	if (!syntaxDirection.equals("flat")) {
	    syntaxSeparator = p.getProperty("jndi.syntax.separator");
	    syntaxSeparator2 = p.getProperty("jndi.syntax.separator2");
	    if (syntaxSeparator == null) {
		throw new IllegalArgumentException(
		    "jndi.syntax.separator property required for non-flat syntax");
	    }
	}
	syntaxEscape = p.getProperty("jndi.syntax.escape");

	syntaxCaseInsensitive = getBoolean(p, "jndi.syntax.ignorecase");
	syntaxTrimBlanks = getBoolean(p, "jndi.syntax.trimblanks");

	syntaxBeginQuote1 = p.getProperty("jndi.syntax.beginquote");
	syntaxEndQuote1 = p.getProperty("jndi.syntax.endquote");
	if (syntaxEndQuote1 == null && syntaxBeginQuote1 != null)
	    syntaxEndQuote1 = syntaxBeginQuote1;
	else if (syntaxBeginQuote1 == null && syntaxEndQuote1 != null)
	    syntaxBeginQuote1 = syntaxEndQuote1;
	syntaxBeginQuote2 = p.getProperty("jndi.syntax.beginquote2");
	syntaxEndQuote2 = p.getProperty("jndi.syntax.endquote2");
	if (syntaxEndQuote2 == null && syntaxBeginQuote2 != null)
	    syntaxEndQuote2 = syntaxBeginQuote2;
	else if (syntaxBeginQuote2 == null && syntaxEndQuote2 != null)
	    syntaxBeginQuote2 = syntaxEndQuote2;

	syntaxAvaSeparator = p.getProperty("jndi.syntax.separator.ava");
	syntaxTypevalSeparator = 
	    p.getProperty("jndi.syntax.separator.typeval");
    }

    NameImpl(Properties syntax) {
	recordNamingConvention(syntax);
	components = new Vector();
    }

    NameImpl(Properties syntax, String n) throws InvalidNameException {
	this(syntax);

	boolean rToL = syntaxDirection.equals("right_to_left");
	boolean compsAllEmpty = true;
	int len = n.length();

	for (int i = 0; i < len; ) {
	    i = extractComp(n, i, len, components);

	    String comp = rToL
		? (String)components.firstElement()
		: (String)components.lastElement();
	    if (comp.length() >= 1) {
		compsAllEmpty = false;
	    }

	    if (i < len) {
		i = skipSeparator(n, i);
		if ((i == len) && !compsAllEmpty) {
		    // Trailing separator found.  Add an empty component.
		    if (rToL) {
			components.insertElementAt("", 0);
		    } else {
			components.addElement("");
		    }
		}
	    }
	}
    }

    NameImpl(Properties syntax, Enumeration comps) {
	this(syntax);

	// %% comps could shrink in the middle.
	while (comps.hasMoreElements())
	    components.addElement(comps.nextElement());
    }
/*
    // Determines whether this component needs any escaping.
    private final boolean escapingNeeded(String comp) {
	int len = comp.length();
	for (int i = 0; i < len; i++) {
	    if (i == 0) {
		if (isA(comp, 0, syntaxBeginQuote1) ||
		    isA(comp, 0, syntaxBeginQuote2)) {
		    return (true);
		}
	    }
	    if (isSeparator(comp, i)) {
		return (true);
	    }
	    if (isA(comp, i, syntaxEscape)) {
		i += syntaxEscape.length();
		if (i >= len || isMeta(comp, i)) {
		    return (true);
		}
	    }
	}
	return (false);
    }
*/
    private final String stringifyComp(String comp) {
	int len = comp.length();
	boolean escapeSeparator = false, escapeSeparator2 = false;
	String beginQuote = null, endQuote = null;
	StringBuffer strbuf = new StringBuffer(len);

	// determine whether there are any separators; if so escape
	// or quote them
	if (syntaxSeparator != null &&
	    comp.indexOf(syntaxSeparator) >= 0) {
	    if (syntaxBeginQuote1 != null) {
		beginQuote = syntaxBeginQuote1;
		endQuote = syntaxEndQuote1;
	    } else if (syntaxBeginQuote2 != null) {
		beginQuote = syntaxBeginQuote2;
		endQuote = syntaxEndQuote2;
	    } else if (syntaxEscape != null)
		escapeSeparator = true;
	}
	if (syntaxSeparator2 != null &&
	    comp.indexOf(syntaxSeparator2) >= 0) {
	    if (syntaxBeginQuote1 != null) {
		if (beginQuote == null) {
		    beginQuote = syntaxBeginQuote1;
		    endQuote = syntaxEndQuote1;
		}
	    } else if (syntaxBeginQuote2 != null) {
		if (beginQuote == null) {
		    beginQuote = syntaxBeginQuote2;
		    endQuote = syntaxEndQuote2;
		}
	    } else if (syntaxEscape != null)
		escapeSeparator2 = true;
	}

	// if quoting component, 
	if (beginQuote != null) {

	    // start string off with opening quote
	    strbuf = strbuf.append(beginQuote);

	    // component is being quoted, so we only need to worry about
	    // escaping end quotes that occur in component
	    for (int i = 0; i < len; ) {
		if (comp.startsWith(endQuote, i)) {
		    // end-quotes must be escaped when inside a quoted string
		    strbuf.append(syntaxEscape).append(endQuote);
		    i += endQuote.length();
		} else {
		    // no special treatment required
		    strbuf.append(comp.charAt(i++));
		}
	    }

	    // end with closing quote
	    strbuf.append(endQuote);

	} else {

	    // When component is not quoted, add escape for:
	    // 1. leading quote 
	    // 2. an escape preceding any meta char
	    // 3. an escape at the end of a component
	    // 4. separator

	    // go through characters in component and escape where necessary
	    boolean start = true;
	    for (int i = 0; i < len; ) {
		// leading quote must be escaped
		if (start && isA(comp, i, syntaxBeginQuote1)) {
		    strbuf.append(syntaxEscape).append(syntaxBeginQuote1);
		    i += syntaxBeginQuote1.length();
		} else if (start && isA(comp, i, syntaxBeginQuote2)) {
		    strbuf.append(syntaxEscape).append(syntaxBeginQuote2);
		    i += syntaxBeginQuote2.length();
		} else

		// Escape an escape preceding meta characters, or at end.
		// Other escapes pass through.
		if (isA(comp, i, syntaxEscape)) {
		    if (i + syntaxEscape.length() >= len) {
			// escape an ending escape
			strbuf.append(syntaxEscape);
		    } else if (isMeta(comp, i + syntaxEscape.length())) {
			// escape meta strings
			strbuf.append(syntaxEscape);
		    }
		    strbuf.append(syntaxEscape);
		    i += syntaxEscape.length();
		} else

		// escape unescaped separator
		if (escapeSeparator && comp.startsWith(syntaxSeparator, i)) {
		    // escape separator
		    strbuf.append(syntaxEscape).append(syntaxSeparator);
		    i += syntaxSeparator.length();
		} else if (escapeSeparator2 &&
			   comp.startsWith(syntaxSeparator2, i)) {
		    // escape separator2
		    strbuf.append(syntaxEscape).append(syntaxSeparator2);
		    i += syntaxSeparator2.length();
		} else {
		    // no special treatment required
		    strbuf.append(comp.charAt(i++));
		}
		start = false;
	    }
	}
	return (strbuf.toString());
    }

    public String toString() {
	StringBuffer answer = new StringBuffer();
	String comp;
	boolean compsAllEmpty = true;
	int size = components.size();

	for (int i = 0; i < size; i++) {
	    if (syntaxDirection.equals("right_to_left")) {
		comp =
		    stringifyComp((String) components.elementAt(size - 1 - i));
	    } else {
		comp = stringifyComp((String) components.elementAt(i));
	    }
	    if ((i != 0) && (syntaxSeparator != null))
		answer.append(syntaxSeparator);
	    if (comp.length() >= 1)
		compsAllEmpty = false;
	    answer = answer.append(comp);
	}
	if (compsAllEmpty && (size >= 1) && (syntaxSeparator != null))
	    answer = answer.append(syntaxSeparator);
	return (answer.toString());
    }

    public boolean equals(Object obj) {
	if ((obj != null) && (obj instanceof NameImpl)) {
	    NameImpl target = (NameImpl)obj;
	    if (target.size() ==  this.size()) {
		Enumeration mycomps = getAll();
		Enumeration comps = target.getAll();
		while (mycomps.hasMoreElements()) {
		    // %% comps could shrink in the middle.
		    String my = (String)mycomps.nextElement();
		    String his = (String)comps.nextElement();
		    if (syntaxTrimBlanks) {
			my = my.trim();
			his = his.trim();
		    }
		    if (syntaxCaseInsensitive) {
			if (!(my.equalsIgnoreCase(his)))
			    return false;
		    } else {
			if (!(my.equals(his)))
			    return false;
		    }
		}
		return true;
	    }
	}
	return false;
    }

    /**
      * Compares obj to this NameImpl to determine ordering.
      * Takes into account syntactic properties such as
      * elimination of blanks, case-ignore, etc, if relevant.
      *
      * Note: using syntax of this NameImpl and ignoring
      * that of comparison target.
      */
    public int compareTo(NameImpl obj) {
	if (this == obj) {
	    return 0;
	}

	int len1 = size();
	int len2 = obj.size();
	int n = Math.min(len1, len2);

	int index1 = 0, index2 = 0;

	while (n-- != 0) {
	    String comp1 = get(index1++);
	    String comp2 = obj.get(index2++);

	    // normalize according to syntax
	    if (syntaxTrimBlanks) {
		comp1 = comp1.trim();
		comp2 = comp2.trim();
	    }
	    if (syntaxCaseInsensitive) {
		comp1 = comp1.toLowerCase();
		comp2 = comp2.toLowerCase();
	    }
	    int local = comp1.compareTo(comp2);
	    if (local != 0) {
		return local;
	    }
	}

	return len1 - len2;
    }

    public int size() {
	return (components.size());
    }

    public Enumeration getAll() {
	return components.elements();
    }

    public String get(int posn) {
	return ((String) components.elementAt(posn));
    }

    public Enumeration getPrefix(int posn) {
	if (posn < 0 || posn > size()) {
	    throw new ArrayIndexOutOfBoundsException(posn);
	}
	return new NameImplEnumerator(components, 0, posn);
    }

    public Enumeration getSuffix(int posn) {
	int cnt = size();
	if (posn < 0 || posn > cnt) {
	    throw new ArrayIndexOutOfBoundsException(posn);
	}
	return new NameImplEnumerator(components, posn, cnt);
    }

    public boolean isEmpty() {
	return (components.isEmpty());
    }

    public boolean startsWith(int posn, Enumeration prefix) {
	if (posn < 0 || posn > size()) {
	    return false;
	}
	try {
	    Enumeration mycomps = getPrefix(posn);
	    while (mycomps.hasMoreElements()) {
		String my = (String)mycomps.nextElement();
		String his = (String)prefix.nextElement();
		if (syntaxTrimBlanks) {
		    my = my.trim();
		    his = his.trim();
		}
		if (syntaxCaseInsensitive) {
		    if (!(my.equalsIgnoreCase(his)))
			return false;
		} else {
		    if (!(my.equals(his)))
			return false;
		}
	    }
	} catch (NoSuchElementException e) {
	    return false;
	}
	return true;
    }

    public boolean endsWith(int posn, Enumeration suffix) {
	// posn is number of elements in suffix
	// startIndex is the starting position in this name
	// at which to start the comparison. It is calculated by
	// subtracting 'posn' from size()
	int startIndex = size() - posn;
	if (startIndex < 0 || startIndex > size()) {
	    return false;
	}
	try {
	    Enumeration mycomps = getSuffix(startIndex);
	    while (mycomps.hasMoreElements()) {
		String my = (String)mycomps.nextElement();
		String his = (String)suffix.nextElement();
		if (syntaxTrimBlanks) {
		    my = my.trim();
		    his = his.trim();
		}
		if (syntaxCaseInsensitive) {
		    if (!(my.equalsIgnoreCase(his)))
			return false;
		} else {
		    if (!(my.equals(his)))
			return false;
		}
	    }
	} catch (NoSuchElementException e) {
	    return false;
	}
	return true;
    }

    public boolean addAll(Enumeration comps) throws InvalidNameException {
	boolean added = false;
	while (comps.hasMoreElements()) {
	    try {
		Object comp = comps.nextElement();
		if (size() > 0 && syntaxDirection.equals("flat")) {
		    throw new InvalidNameException(
			"A flat name can only have a single component");
		}
		components.addElement(comp);
		added = true;
	    } catch (NoSuchElementException e) {
		break;	// "comps" has shrunk.
	    }
	}
	return added;
    }

    public boolean addAll(int posn, Enumeration comps) 
    throws InvalidNameException {
	boolean added = false;
	for (int i = posn; comps.hasMoreElements(); i++) {
	    try {
		Object comp = comps.nextElement();
		if (size() > 0 && syntaxDirection.equals("flat")) {
		    throw new InvalidNameException(
			"A flat name can only have a single component");
		}
		components.insertElementAt(comp, i);
		added = true;
	    } catch (NoSuchElementException e) {
		break;	// "comps" has shrunk.
	    }
	}
	return added;
    }

    public void add(String comp) throws InvalidNameException {
	if (size() > 0 && syntaxDirection.equals("flat")) {
	    throw new InvalidNameException(
		"A flat name can only have a single component");
	}
	components.addElement(comp);
    }

    public void add(int posn, String comp) throws InvalidNameException {
	if (size() > 0 && syntaxDirection.equals("flat")) {
	    throw new InvalidNameException(
		"A flat name can only zero or one component");
	}
	components.insertElementAt(comp, posn);
    }

    public Object remove(int posn) {
	Object r = components.elementAt(posn);
	components.removeElementAt(posn);
	return r;
    }

    public int hashCode() {
	int hash = 0;
	for (Enumeration e = getAll(); e.hasMoreElements();) {
	    String comp = (String)e.nextElement();
	    if (syntaxTrimBlanks) {
		comp = comp.trim();
	    }
	    if (syntaxCaseInsensitive) {
		comp = comp.toLowerCase();
	    }
	
	    hash += comp.hashCode();
	}
	return hash;
    }
}

final
class NameImplEnumerator implements Enumeration {
    Vector vector;
    int count;
    int limit;

    NameImplEnumerator(Vector v, int start, int lim) {
	vector = v;
	count = start;
	limit = lim;
    }

    public boolean hasMoreElements() {
	return count < limit;
    }

    public Object nextElement() {
	if (count < limit) {
	    return vector.elementAt(count++);
	}
	throw new NoSuchElementException("NameImplEnumerator");
    }
}

