/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.naming;

/**
  * This is the superclass of all exceptions thrown by
  * operations in the Context and DirContext interfaces.
  * The nature of the failure is described by the name of the subclass.
  * This exception captures the information pinpointing where the operation 
  * failed, such as where resolution last proceeded to.
  * <ul>
  * <li> Resolved Name. Portion of name that has been resolved.
  * <li> Resolved Object. Object to which resolution of name proceeded.
  * <li> Remaining Name. Portion of name that has not been resolved.
  * <li> Explanation. Detail explaining why name resolution failed.
  * <li> Root Exception. The exception that caused this naming exception
  *			to be thrown.
  *</ul>
  * null is an acceptable value for any of these fields. When null,
  * it means that no such information has been recorded for that field.
  *<p>
  * A NamingException instance is not synchronized against concurrent 
  * multithreaded access. Multiple threads trying to access and modify
  * a single NamingException instance should lock the object.
  *
  * @author Rosanna Lee
  * @author Scott Seligman
  * @version 1.7 02/02/06
  * @since 1.3
  */


public class NamingException extends Exception {
    /**
     * Contains the part of the name that has been successfully resolved.
     * It is a composite name and can be null.
     * This field is initialized by the constructors.
     * You should access and manipulate this field
     * through its get and set methods.
     * @serial
     * @see #getResolvedName
     * @see #setResolvedName
     */
    protected Name resolvedName;
    /**
      * Contains the object to which resolution of the part of the name was 
      * successful. Can be null. 
      * This field is initialized by the constructors.
      * You should access and manipulate this field
      * through its get and set methods.
      * @serial
      * @see #getResolvedObj
      * @see #setResolvedObj
      */
    protected Object resolvedObj;
    /**
     * Contains the remaining name that has not been resolved yet.
     * It is a composite name and can be null. 
     * This field is initialized by the constructors.
     * You should access and manipulate this field
     * through its get, set, "append" methods.
     * @serial
     * @see #getRemainingName
     * @see #setRemainingName
     * @see #appendRemainingName
     * @see #appendRemainingComponent
     */
    protected Name remainingName;

    /**
     * Contains the original exception that caused this NamingException to
     * be thrown. This field is set if there is additional
     * information that could be obtained from the original
     * exception, or if there original exception could not be
     * mapped to a subclass of NamingException.
     * Can be null. This field is initialized by the constructors.
     * You should access and manipulate this field
     * through its get and set methods.
     * @serial
     * @see #getRootCause
     * @see #setRootCause
     */
    protected Throwable rootException = null;

    /**
     * Constructs a new NamingException with an explanation.
     * All unspecified fields are set to null.
     *
     * @param	explanation	A possibly null string containing
     * 				additional detail about this exception.
     * @see java.lang.Throwable#getMessage
     */
    public NamingException(String explanation) {
	super(explanation);
	resolvedName = remainingName = null;
	resolvedObj = null;
    }

    /**
      * Constructs a new NamingException.
      * All fields are set to null.
      */
    public NamingException() {
	super();
	resolvedName = remainingName = null;
	resolvedObj = null;
    }

    /**
     * Retrieves the leading portion of the name that was resolved
     * successfully. 
     *
     * @return The part of the name that was resolved successfully. 
     * 		It is a composite name. It can be null, which means
     *		the resolved name field has not been set.
     * @see #getResolvedObj
     * @see #setResolvedName
     */
    public Name getResolvedName() {
	return resolvedName;
    }
    
    /**
     * Retrieves the remaining unresolved portion of the name.
     * @return The part of the name that has not been resolved. 
     * 		It is a composite name. It can be null, which means
     *		the remaining name field has not been set.
     * @see #setRemainingName
     * @see #appendRemainingName
     * @see #appendRemainingComponent
     */
    public Name getRemainingName() {
	return remainingName;
    }

    /**
     * Retrieves the object to which resolution was successful.
     * This is the object to which the resolved name is bound.
     *
     * @return The possibly null object that was resolved so far.
     *	null means that the resolved object field has not been set.
     * @see #getResolvedName
     * @see #setResolvedObj
     */
    public Object getResolvedObj() {
	return resolvedObj;
    }

    /**
      * Retrieves the explanation associated with this exception.
      * 
      * @return The possibly null detail string explaining more 
      * 	about this exception. If null, it means there is no
      *		detail message for this exception.
      *
      * @see java.lang.Throwable#getMessage
      */
    public String getExplanation() {
	return getMessage();
    }

    /**
     * Sets the resolved name field of this exception.
     *<p>
     * <tt>name</tt> is a composite name. If the intent is to set
     * this field using a compound name or string, you must 
     * "stringify" the compound name, and create a composite
     * name with a single component using the string. You can then
     * invoke this method using the resulting composite name.
     *<p>
     * A copy of <code>name</code> is made and stored.
     * Subsequent changes to <code>name</code> does not
     * affect the copy in this NamingException and vice versa.
     *
     * @param name The possibly null name to set resolved name to.
     *		If null, it sets the resolved name field to null.
     * @see #getResolvedName
     */
    public void setResolvedName(Name name) {
	if (name != null)
	    resolvedName = (Name)(name.clone());
	else
	    resolvedName = null;
    }

    /**
     * Sets the remaining name field of this exception.
     *<p>
     * <tt>name</tt> is a composite name. If the intent is to set
     * this field using a compound name or string, you must 
     * "stringify" the compound name, and create a composite
     * name with a single component using the string. You can then
     * invoke this method using the resulting composite name.
     *<p>
     * A copy of <code>name</code> is made and stored.
     * Subsequent changes to <code>name</code> does not
     * affect the copy in this NamingException and vice versa.
     * @param name The possibly null name to set remaining name to.
     *		If null, it sets the remaining name field to null.
     * @see #getRemainingName
     * @see #appendRemainingName
     * @see #appendRemainingComponent
     */
    public void setRemainingName(Name name) {
	if (name != null)
	    remainingName = (Name)(name.clone());
	else
	    remainingName = null;
    }

    /**
     * Sets the resolved object field of this exception.
     * @param obj The possibly null object to set resolved object to.
     *		  If null, the resolved object field is set to null.
     * @see #getResolvedObj
     */
    public void setResolvedObj(Object obj) {
	resolvedObj = obj;
    }

    /**
      * Add name as the last component in remaining name.
      * @param name The component to add.
      *   	If name is null, this method does not do anything.
      * @see #setRemainingName
      * @see #getRemainingName
      * @see #appendRemainingName
      */
    public void appendRemainingComponent(String name) {
	if (name != null) {
	    try {
		if (remainingName == null) {
		    remainingName = new CompositeName();
		}
		remainingName.add(name);
	    } catch (NamingException e) {
		throw new IllegalArgumentException(e.toString());
	    }
	}
    }

    /**
      * Add components from 'name' as the last components in 
      * remaining name.
      *<p>
      * <tt>name</tt> is a composite name. If the intent is to append
      * a compound name, you should "stringify" the compound name
      * then invoke the overloaded form that accepts a String parameter.
      *<p>
      * Subsequent changes to <code>name</code> does not
      * affect the remaining name field in this NamingException and vice versa.
      * @param name The possibly null name containing ordered components to add.
      * 		If name is null, this method does not do anything.
      * @see #setRemainingName
      * @see #getRemainingName
      * @see #appendRemainingComponent
      */
    public void appendRemainingName(Name name) {
	if (name == null) {
	    return;
	}
	if (remainingName != null) {
	    try {
		remainingName.addAll(name);
	    } catch (NamingException e) {
		throw new IllegalArgumentException(e.toString());
	    }
	} else {
	    remainingName = (Name)(name.clone());
	}
    }

    /**
      * Retrieves the root cause of this NamingException, if any.
      * The root cause of a naming exception is used when the service provider
      * wants to indicate to the caller a non-naming related exception
      * but at the same time want to use the NamingException structure
      * to indicate how far the naming operation proceeded.
      *
      * @return The possibly null exception that caused this naming 
      *    exception. If null, it means no root cause has been
      *	   set for this naming exception.
      * @see #setRootCause
      * @see #rootException
      */
    public Throwable getRootCause() {
	return rootException;
    }

    /**
      * Records that the root cause of this NamingException.
      * If <tt>e</tt> is <tt>this</tt>, this method does no do anything.
      * @param e The possibly null exception that caused the naming 
      * 	 operation to fail. If null, it means this naming
      * 	 exception has no root cause.
      * @see #getRootCause
      * @see #rootException
      */
    public void setRootCause(Throwable e) {
	if (e != this) {
	    rootException = e;
	}
    }

    /**
     * Generates the string representation of this exception.
     * The string representation consists of this exception's class name, 
     * its detailed message, and if it has a root cause, the string
     * representation of the root cause exception, followed by
     * the remaining name (if it is not null). 
     * This string is used for debugging and not meant to be interpreted
     * programmatically.
     *
     * @return The non-null string containing the string representation 
     * of this exception.
     */
    public String toString() {
	String answer = super.toString();

	if (rootException != null) {
	    answer += " [Root exception is " + rootException + "]";
	}
	if (remainingName != null) {
	    answer += "; remaining name '" + remainingName + "'";
	}
	return answer;
    }

    /**
      * Generates the string representation in more detail.
      * This string representation consists of the information returned
      * by the toString() that takes no parameters, plus the string
      * representation of the resolved object (if it is not null).
      * This string is used for debugging and not meant to be interpreted
      * programmatically.
      *
      * @param detail If true, include details about the resolved object
      *			in addition to the other information.
      * @return The non-null string containing the string representation. 
      */
    public String toString(boolean detail) {
	if (!detail || resolvedObj == null) {
	    return toString();
	} else {
	    return (toString() + "; resolved object " + resolvedObj);
	}
    }

    /**
     * Use serialVersionUID from JNDI 1.1.1 for interoperability
     */
    private static final long serialVersionUID = -1299181962103167177L;

    /**
     * Prints this exception's stack trace to <tt>System.err</tt>.
     * If this exception has a root exception, the stack trace of the
     * root exception is printed instead.
     */
    public void printStackTrace() {
	printStackTrace(System.err);
    }

    /**
     * Prints this exception's stack trace to a print stream.
     * If this exception has a root exception, the stack trace of the
     * root exception is printed instead.
     * @param ps The non-null print stream to which to print.
     */
    public void printStackTrace(java.io.PrintStream ps) {
	if (rootException != null) {
	    String superString = super.toString();
	    synchronized (ps) {
		ps.print(superString
			 + (superString.endsWith(".") ? "" : ".")
			 + "  Root exception is ");
		rootException.printStackTrace(ps);
	    }
	} else {
	    super.printStackTrace(ps);
	}
    }

    /**
     * Prints this exception's stack trace to a print writer.
     * If this exception has a root exception, the stack trace of the
     * root exception is printed instead.
     * @param pw The non-null print writer to which to print.
     */
    public void printStackTrace(java.io.PrintWriter pw) {
	if (rootException != null) {
	    String superString = super.toString();
	    synchronized (pw) {
		pw.print(superString
			 + (superString.endsWith(".") ? "" : ".")
			 + "  Root exception is ");
		rootException.printStackTrace(pw);
	    }
	} else {
	    super.printStackTrace(pw);
	}
    }
};
