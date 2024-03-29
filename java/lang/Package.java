/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.lang;

import java.io.InputStream;
import java.util.Enumeration;

import java.util.StringTokenizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <code>Package</code> objects contain version information
 * about the implementation and specification of a Java package.
 * This versioning information is retrieved and made available
 * by the classloader that loaded the class(es). Typically, it is
 * stored in the manifest that is distributed with the classes.<p>
 *
 * The set of classes that make up the package may implement a
 * particular specification and if
 * so the specification title, version number, and vendor strings 
 * identify that specification. 
 * An application can ask if the package is
 * compatible with a particular version, see the <code>isCompatibleWith</code>
 * method for details. <p>
 *
 * Specification version numbers use a "Dewey Decimal"
 * syntax that consists of positive decimal integers
 * separated by periods ".", for example, "2.0" or "1.2.3.4.5.6.7".
 * This allows an extensible number to be used to
 * represent major, minor, micro, etc versions.
 * The version number must begin with a number. <p>
 *
 * The implementation title, version, and vendor strings identify an
 * implementation and are made available conveniently to enable accurate
 * reporting of the packages involved when a problem occurs. The contents
 * all three implementation strings are vendor specific. The
 * implementation version strings have no specified syntax and should
 * only be compared for equality with desired version identifers.
 *
 * Within each classloader all classes from the same java package have
 * the same Package object.  The static methods allow a package 
 * to be found by name or the set of all packages known
 * to the current class loader to be found.<p>
 *
 * @see ClassLoader#definePackage
 */
public class Package {
    /**
     * Return the name of this package.
     *
     * @return The name of this package using the Java language dot notation
     * 		for the package. i.e  java.lang
     */
    public String getName() {
	return pkgName;
    }


    /**
     * Return the title of the specification that this package implements.
     * @return the specification title, null is returned if it is not known.
     */
    public String getSpecificationTitle() {
	return specTitle;
    }
    
    /**
     * Returns the version number of the specification
     * that this package implements.
     * This version string must be a sequence of positive decimal
     * integers separated by "."'s and may have leading zeros.
     * When version strings are compared the most significant
     * numbers are compared.
     * @return the specification version, null is returned if it is not known.
     */
    public String getSpecificationVersion() {
	return specVersion;
    }

    /**
     * Return the name of the organization, vendor, 
     * or company that owns and maintains the specification 
     * of the classes that implement this package.
     * @return the specification vendor, null is returned if it is not known.
     */
    public String getSpecificationVendor() {
	return specVendor;
    }

    /**
     * Return the title of this package.
     * @return the title of the implementation, null is returned if it is not known.
     */
    public String getImplementationTitle() {
	return implTitle;
    }

    /**
     * Return the version of this implementation. It consists of any string
     * assigned by the vendor of this implementation and does
     * not have any particular syntax specified or expected by the Java
     * runtime. It may be compared for equality with other
     * package version strings used for this implementation
     * by this vendor for this package.
     * @return the version of the implementation, null is returned if it is not known.
     */    
    public String getImplementationVersion() {
    	return implVersion;
    }

    /**
     * Returns the name of the organization,
     * vendor or company that provided this implementation.
     * @return the vendor that implemented this package..
     */
    public String getImplementationVendor() {
    	return implVendor;
    }

    /**
     * Returns true if this package is sealed.
     *
     * @return true if the package is sealed, false otherwise
     */
    public boolean isSealed() {
	return sealBase != null;
    }

    /**
     * Returns true if this package is sealed with respect to the specified
     * code source url.
     *
     * @param url the code source url
     * @return true if this package is sealed with respect to url
     */
    public boolean isSealed(URL url) {
	return url.equals(sealBase);
    }

    /**
     * Compare this package's specification version with a
     * desired version. It returns true if
     * this packages specification version number is greater than or equal
     * to the desired version number. <p>
     *
     * Version numbers are compared by sequentially comparing corresponding
     * components of the desired and specification strings.
     * Each component is converted as a decimal integer and the values
     * compared.
     * If the specification value is greater than the desired
     * value true is returned. If the value is less false is returned.
     * If the values are equal the period is skipped and the next pair of
     * components is compared.
     *
     * @param desired the version string of the desired version.
     * @return true if this package's version number is greater
     * 		than or equal to the desired version number<br>  
     *		
     * @exception NumberFormatException if the desired or current version
     *		is not of the correct dotted form.
     */
    public boolean isCompatibleWith(String desired)
	throws NumberFormatException
    {
    	if (specVersion == null || specVersion.length() < 1) {
	    throw new NumberFormatException("Empty version string");
	}

    	// Until it matches scan and compare numbers
    	StringTokenizer dtok = new StringTokenizer(desired, ".", true);
    	StringTokenizer stok = new StringTokenizer(specVersion, ".", true);
	while (dtok.hasMoreTokens() || stok.hasMoreTokens()) {
	    int dver;
	    int sver;
	    if (dtok.hasMoreTokens()) {
		dver = Integer.parseInt(dtok.nextToken());
	    } else
		dver = 0;

	    if (stok.hasMoreTokens()) {
		sver = Integer.parseInt(stok.nextToken());
	    } else
		sver = 0;

    	    if (sver < dver)
    	    	return false;		// Known to be incompatible
    	    if (sver > dver)
    	    	return true;		// Known to be compatible

    	    // Check for and absorb seperators
    	    if (dtok.hasMoreTokens())
    	    	dtok.nextToken();
    	    if (stok.hasMoreTokens())
    	    	stok.nextToken();
    	    // Compare next component
    	}
    	// All components numerically equal
	return true;
    }

    /**
     * Find a package by name in the callers classloader.
     * The callers classloader is used to find the package instance
     * corresponding to the named class. If the callers classloader
     * is null then the set of packages loaded by the system
     * classloader is searched to find the named package. <p>
     *
     * Packages have attributes for versions and specifications only
     * if the class loader created the package
     * instance with the appropriate attributes. Typically, those
     * attributes are defined in the manifests that accompany
     * the classes.
     *
     * @param name a package name, for example, java.lang.
     * @return the package of the requested name. It may be null if no package
     * 		information is available from the archive or codebase.
     */
    public static Package getPackage(String name) {
	ClassLoader l = ClassLoader.getCallerClassLoader();
	if (l != null) {
	    return l.getPackage(name);
	} else {
	    return getSystemPackage(name);
	}
    }

    /**
     * Get all the packages currently known for the caller's class loader.
     * Those packages correspond to classes loaded via or accessible
     * by name to that class loader.  If the caller's class loader is
     * the bootstrap classloader, which may be represented by 
     * <code>null</code> in some implementations, only packages corresponding 
     * to classes loaded by the bootstrap class loader will be returned.
     *
     * @return a new array of packages known to the callers classloader.
     * An zero length array is returned if none are known.
     */
    public static Package[] getPackages() {
	ClassLoader l = ClassLoader.getCallerClassLoader();
	if (l != null) {
	    return l.getPackages();
	} else {
	    return getSystemPackages();
	}
    }

    /**
     * Get the package for the specified class.
     * The class's class loader is used to find the package instance
     * corresponding to the specified class. If the class loader
     * is the bootstrap class loader, which may be represented by 
     * <code>null</code> in some implementations, then the set of packages 
     * loaded by the bootstrap class loader is searched to find the package.
     * <p>
     * Packages have attributes for versions and specifications only
     * if the class loader created the package
     * instance with the appropriate attributes. Typically those
     * attributes are defined in the manifests that accompany
     * the classes.
     *
     * @param class the class to get the package of.
     * @return the package of the class. It may be null if no package
     * 		information is available from the archive or codebase.  */
    static Package getPackage(Class c) {
	String name = c.getName();
	int i = name.lastIndexOf('.');
	if (i != -1) {
	    name = name.substring(0, i);
	    ClassLoader cl = c.getClassLoader();
	    if (cl != null) {
		return cl.getPackage(name);
	    } else {
		return getSystemPackage(name);
	    }
	} else {
	    return null;
	}
    }

    /**
     * Return the hashcode computed from the package name.
     * @return the hodecode computed from the package name.
     */
    public int hashCode(){
    	return pkgName.hashCode();
    }

    /**
     * Returns the string representation of this Package.
     * Its value is the string "package " and the package name.
     * If the package title is defined it is appended.
     * If the package version is defined it is appended.
     * @return the string representation of the package.
     */
    public String toString() {
	String spec = specTitle;
	String ver =  specVersion;
	if (spec != null && spec.length() > 0)
	    spec = ", " + spec;
	else
	    spec = "";
	if (ver != null && ver.length() > 0)
	    ver = ", version " + ver;
	else
	    ver = "";
	return "package " + pkgName + spec + ver;
    }

    /**
     * Construct a package instance with the specified version
     * information.
     * @param pkgName the name of the package
     * @param spectitle the title of the specification
     * @param specversion the version of the specification
     * @param specvendor the organization that maintains the specification
     * @param impltitle the title of the implementation
     * @param implversion the version of the implementation
     * @param implvendor the organization that maintains the implementation
     * @return a new package for containing the specified information.
     */
    Package(String name, 
	    String spectitle, String specversion, String specvendor,
	    String impltitle, String implversion, String implvendor,
	    URL sealbase)
    {
    	pkgName = name;
	implTitle = impltitle;
	implVersion = implversion;
	implVendor = implvendor;
	specTitle = spectitle;
	specVersion = specversion;
	specVendor = specvendor;
	sealBase = sealbase;
    }

    /*
     * Construct a package using the attributes from the specified manifest.
     *
     * @param name the package name
     * @param man the optional manifest for the package
     * @param url the optional code source url for the package
     */
    private Package(String name, Manifest man, URL url) {
	String path = name.replace('.', '/').concat("/");
	String sealed = null;
	Attributes attr = man.getAttributes(path);
	if (attr != null) {
	    specTitle   = attr.getValue(Name.SPECIFICATION_TITLE);
	    specVersion = attr.getValue(Name.SPECIFICATION_VERSION);
	    specVendor  = attr.getValue(Name.SPECIFICATION_VENDOR);
	    implTitle   = attr.getValue(Name.IMPLEMENTATION_TITLE);
	    implVersion = attr.getValue(Name.IMPLEMENTATION_VERSION);
	    implVendor  = attr.getValue(Name.IMPLEMENTATION_VENDOR);
	    sealed      = attr.getValue(Name.SEALED);
	}
	attr = man.getMainAttributes();
	if (attr != null) {
	    if (specTitle == null) {
		specTitle = attr.getValue(Name.SPECIFICATION_TITLE);
	    }
	    if (specVersion == null) {
		specVersion = attr.getValue(Name.SPECIFICATION_VERSION);
	    }
	    if (specVendor == null) {
		specVendor = attr.getValue(Name.SPECIFICATION_VENDOR);
	    }
	    if (implTitle == null) {
		implTitle = attr.getValue(Name.IMPLEMENTATION_TITLE);
	    }
	    if (implVersion == null) {
		implVersion = attr.getValue(Name.IMPLEMENTATION_VERSION);
	    }
	    if (implVendor == null) {
		implVendor = attr.getValue(Name.IMPLEMENTATION_VENDOR);
	    }
	    if (sealed == null) {
		sealed = attr.getValue(Name.SEALED);
	    }
	}
	if ("true".equalsIgnoreCase(sealed)) {
	    sealBase = url;
	}
	pkgName = name;
    }

    /*
     * Returns the loaded system package for the specified name.
     */
    static Package getSystemPackage(String name) {
	synchronized (pkgs) {
	    Package pkg = (Package)pkgs.get(name);
	    if (pkg == null) {
		name = name.replace('.', '/').concat("/");
		String fn = getSystemPackage0(name);
		if (fn != null) {
		    pkg = defineSystemPackage(name, fn);
		}
	    }
	    return pkg;
	}
    }

    /*
     * Return an array of loaded system packages.
     */
    static Package[] getSystemPackages() {
	// First, update the system package map with new package names
	String[] names = getSystemPackages0();
	synchronized (pkgs) {
	    for (int i = 0; i < names.length; i++) {
		defineSystemPackage(names[i], getSystemPackage0(names[i]));
	    }
	    return (Package[])pkgs.values().toArray(new Package[pkgs.size()]);
	}
    }

    private static Package defineSystemPackage(final String iname,
					       final String fn) 
    {
	return (Package) AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		String name = iname;
		// Get the cached code source url for the file name
		URL url = (URL)urls.get(fn);
		if (url == null) {
		    // URL not found, so create one
		    File file = new File(fn);
		    try {
			url = file.toURL();
		    } catch (MalformedURLException e) {
		    }
		    if (url != null) {
			urls.put(fn, url);
			// If loading a JAR file, then also cache the manifest
			if (file.isFile()) {
			    mans.put(fn, loadManifest(fn));
			}
		    }
		}
		// Convert to "."-separated package name
		name = name.substring(0, name.length() - 1).replace('/', '.');
		Package pkg;
		Manifest man = (Manifest)mans.get(fn);
		if (man != null) {
		    pkg = new Package(name, man, url);
		} else {
		    pkg = new Package(name, null, null, null,
				      null, null, null, null);
		}
		pkgs.put(name, pkg);
		return pkg;
	    }
	});
    }

    /*
     * Returns the Manifest for the specified JAR file name.
     */
    private static Manifest loadManifest(String fn) {
	try {
	    FileInputStream fis = new FileInputStream(fn);
	    JarInputStream jis = new JarInputStream(fis, false);
	    Manifest man = jis.getManifest();
	    jis.close();
	    return man;
	} catch (IOException e) {
	    return null;
	}
    }

    // The map of loaded system packages
    private static Map pkgs = new HashMap(31);
    
    // Maps each directory or zip file name to its corresponding url
    private static Map urls = new HashMap(10);

    // Maps each code source url for a jar file to its manifest
    private static Map mans = new HashMap(10);

    private static native String getSystemPackage0(String name);
    private static native String[] getSystemPackages0();

    /*
     * Private storage for the package name and attributes.
     */
    private String pkgName;
    private String specTitle;
    private String specVersion;
    private String specVendor;
    private String implTitle;
    private String implVersion;
    private String implVendor;
    private URL sealBase;
}
