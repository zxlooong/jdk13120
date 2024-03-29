/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package sunw.util;

/**
 * FOR BACKWARD COMPATIBILITY ONLY - DO NOT USE.
 * <p>
 * This is a backwards compatibility class to allow Java Beans that
 * were developed under JDK 1.0.2 to run correctly under JDK 1.1
 * <p>
 * To allow beans development under JDK 1.0.2, JavaSoft delivered three
 * no-op interfaces/classes (sunw.io.Serializable, sunw.util.EventObject
 * and sunw.util.EventListener) that could be downloaded into JDK 1.0.2
 * systems and which would act as placeholders for the real JDK 1.1
 * classes.
 * <p>
 * Now under JDK 1.1 we provide versions of these classes and interfaces
 * that inherit from the real version in java.util and java.io.  These
 * mean that beans developed under JDK 1.0.2 against the sunw.* classes
 * will now continue to work on JDK 1.1 and will (indirectly) inherit
 * from the approrpiate java.* interfaces/classes.
 *
 * @deprecated This is a compatibility type to allow Java Beans that
 * were developed under JDK 1.0.2 to run correctly under JDK 1.1.  The
 * corresponding JDK1.1 type is java.util.EventListener
 *
 * @see java.util.EventListener
 */

public interface EventListener extends java.util.EventListener {
}
