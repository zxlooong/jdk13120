/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.sql;

/**
 * <P>An exception that provides information on  database access
 * warnings. Warnings are silently chained to the object whose method
 * caused it to be reported.
 *
 * @see Connection#getWarnings
 * @see Statement#getWarnings
 * @see ResultSet#getWarnings 
 */
public class SQLWarning extends SQLException {

    /**
     * Constructs a fully-specified <code>SQLWarning</code> object
	 * initialized with the given values.
     *
     * @param reason a description of the warning 
     * @param SQLState an XOPEN code identifying the warning
     * @param vendorCode a database vendor-specific warning code
     */
     public SQLWarning(String reason, String SQLstate, int vendorCode) {
	super(reason, SQLstate, vendorCode);
	DriverManager.println("SQLWarning: reason(" + reason + 
			      ") SQLstate(" + SQLstate + 
			      ") vendor code(" + vendorCode + ")");
    }


    /**
     * Constructs an <code>SQLWarning</code> object
     * with the given reason and SQLState;
     * the vendorCode defaults to 0.
     *
     * @param reason a description of the warning 
     * @param SQLState an XOPEN code identifying the warning
     */
    public SQLWarning(String reason, String SQLstate) {
	super(reason, SQLstate);
	DriverManager.println("SQLWarning: reason(" + reason + 
				  ") SQLState(" + SQLstate + ")");
    }

    /**
     * Constructs an <code>SQLWarning</code> object
     * with the given value for a reason; SQLState defaults to
     * <code>null</code>, and vendorCode defaults to 0.
     *
     * @param reason a description of the warning 
     */
    public SQLWarning(String reason) {
	super(reason);
	DriverManager.println("SQLWarning: reason(" + reason + ")");
    }

    /**
     * Constructs a default <code>SQLWarning</code> object.
     * The reason defaults to <code>null</code>, SQLState
     * defaults to <code>null</code>, and vendorCode defaults to 0.
     *
     */
    public SQLWarning() {
	super();
	DriverManager.println("SQLWarning: ");
    }


    /**
     * Retrieves the warning chained to this <code>SQLWarning</code> object.
     *
     * @return the next <code>SQLException</code> in the chain; <code>null</code> if none
     */
    public SQLWarning getNextWarning() {
	try {
	    return ((SQLWarning)getNextException());
	} catch (ClassCastException ex) {
	    // The chained value isn't a SQLWarning.
	    // This is a programming error by whoever added it to
	    // the SQLWarning chain.  We throw a Java "Error".
	    throw new Error("SQLWarning chain holds value that is not a SQLWarning");
	}
    }

    /**
     * Adds an <code>SQLWarning</code> object to the end of the chain.
     *
     * @param w the new end of the <code>SQLException</code> chain
     */
    public void setNextWarning(SQLWarning w) {
	setNextException(w);
    }

}
