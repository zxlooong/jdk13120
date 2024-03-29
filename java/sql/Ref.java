/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.sql;
 
/**
 * The mapping in the Java programming language of an SQL <code>REF</code>
 * value, which is a reference to an
 * SQL structured type value in the database.
 * <P>
 * SQL <code>REF</code> values are stored in a special table that contains
 * instances of a referenceable SQL structured type, and each <code>REF</code>
 * value is a unique identifier for one instance in that table. 
 * An SQL <code>REF</code> value may be used in place of the
 * SQL structured type it references; it may be used as either a column value in a
 * table or an attribute value in a structured type.
 * <P>
 * Because an SQL <code>REF</code> value is a logical pointer to an
 * SQL structured type, a <code>Ref</code> object is by default also a logical
 * pointer; thus, retrieving an SQL <code>REF</code> value as
 * a <code>Ref</code> object does not materialize
 * the attributes of the structured type on the client.
 * <P>
 * A <code>Ref</code> object can be saved to persistent storage and is dereferenced by
 * passing it as a parameter to an SQL statement and executing the 
 * statement.
 * <P>
 * The <code>Ref</code> interface is new in the JDBC 2.0 API.
 * @see Struct
 * 
 */
public interface Ref {

  /**
   * Retrieves the fully-qualified SQL name of the SQL structured type that
   * this <code>Ref</code> object references.
   * 
   * @return the fully-qualified SQL name of the referenced SQL structured type 
   * @exception SQLException if a database access error occurs
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   */
  String getBaseTypeName() throws SQLException;

}
