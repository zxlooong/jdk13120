/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.naming.spi;

import java.util.Hashtable;
import javax.naming.*;

/**
  * This class is for dealing with federations/continuations.
  *
  * @author Rosanna Lee
  * @author Scott Seligman
  * @version 1.5 02/02/06
  * @since 1.3
  */

class ContinuationContext implements Context, Resolver {
    protected CannotProceedException cpe;
    protected Context contCtx = null;

    protected ContinuationContext(CannotProceedException cpe) {
	this.cpe = cpe;
    }

    protected Context getTargetContext() throws NamingException {
	if (contCtx == null) {
	    if (cpe.getResolvedObj() == null)
		throw (NamingException)cpe.fillInStackTrace();

	    contCtx = NamingManager.getContext(cpe.getResolvedObj(),
					       cpe.getAltName(),
					       cpe.getAltNameCtx(),
					       cpe.getEnvironment());
	    if (contCtx == null)
		throw (NamingException)cpe.fillInStackTrace();
	}
	return contCtx;
    }

    public Object lookup(Name name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.lookup(name);
    }

    public Object lookup(String name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.lookup(name);
    }

    public void bind(Name name, Object newObj) throws NamingException {
	Context ctx = getTargetContext();
	ctx.bind(name, newObj);
    }

    public void bind(String name, Object newObj) throws NamingException {
	Context ctx = getTargetContext();
	ctx.bind(name, newObj);
    }

    public void rebind(Name name, Object newObj) throws NamingException {
	Context ctx = getTargetContext();
	ctx.rebind(name, newObj);
    }
    public void rebind(String name, Object newObj) throws NamingException {
	Context ctx = getTargetContext();
	ctx.rebind(name, newObj);
    }

    public void unbind(Name name) throws NamingException {
	Context ctx = getTargetContext();
	ctx.unbind(name);
    }
    public void unbind(String name) throws NamingException {
	Context ctx = getTargetContext();
	ctx.unbind(name);
    }

    public void rename(Name name, Name newName) throws NamingException {
	Context ctx = getTargetContext();
	ctx.rename(name, newName);
    }
    public void rename(String name, String newName) throws NamingException {
	Context ctx = getTargetContext();
	ctx.rename(name, newName);
    }
	    
    public NamingEnumeration list(Name name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.list(name);
    }
    public NamingEnumeration list(String name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.list(name);
    }


    public NamingEnumeration listBindings(Name name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.listBindings(name);
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.listBindings(name);
    }

    public void destroySubcontext(Name name) throws NamingException {
	Context ctx = getTargetContext();
	ctx.destroySubcontext(name);
    }
    public void destroySubcontext(String name) throws NamingException {
	Context ctx = getTargetContext();
	ctx.destroySubcontext(name);
    }
	    
    public Context createSubcontext(Name name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.createSubcontext(name);
    }
    public Context createSubcontext(String name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.createSubcontext(name);
    }
	    
    public Object lookupLink(Name name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.lookupLink(name);
    }
    public Object lookupLink(String name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.lookupLink(name);
    }

    public NameParser getNameParser(Name name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.getNameParser(name);
    }

    public NameParser getNameParser(String name) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.getNameParser(name);
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
	Context ctx = getTargetContext();
	return ctx.composeName(name, prefix);
    }

    public String composeName(String name, String prefix)
	    throws NamingException {
	Context ctx = getTargetContext();
	return ctx.composeName(name, prefix);
    }

    public Object addToEnvironment(String propName, Object value)
	throws NamingException {
	Context ctx = getTargetContext();
	return ctx.addToEnvironment(propName, value);
    }

    public Object removeFromEnvironment(String propName)
	throws NamingException {
	Context ctx = getTargetContext();
	return ctx.removeFromEnvironment(propName);
    }

    public Hashtable getEnvironment() throws NamingException {
	Context ctx = getTargetContext();
	return ctx.getEnvironment();
    }

    public String getNameInNamespace() throws NamingException {
	Context ctx = getTargetContext();
	return ctx.getNameInNamespace();
    }

    public ResolveResult resolveToClass(Name name, Class contextType)
	    throws NamingException {
	if (cpe.getResolvedObj() == null)
	    throw (NamingException)cpe.fillInStackTrace();

	Resolver res = NamingManager.getResolver(cpe.getResolvedObj(),
						 cpe.getAltName(),
						 cpe.getAltNameCtx(),
						 cpe.getEnvironment());
	if (res == null)
	    throw (NamingException)cpe.fillInStackTrace();
	return res.resolveToClass(name, contextType);
    }

    public ResolveResult resolveToClass(String name, Class contextType)
	    throws NamingException {
	if (cpe.getResolvedObj() == null)
	    throw (NamingException)cpe.fillInStackTrace();

	Resolver res = NamingManager.getResolver(cpe.getResolvedObj(),
						 cpe.getAltName(),
						 cpe.getAltNameCtx(),
						 cpe.getEnvironment());
	if (res == null)
	    throw (NamingException)cpe.fillInStackTrace();
	return res.resolveToClass(name, contextType);
    }

    public void close() throws NamingException {
	cpe = null;
	if (contCtx != null) {
	    contCtx.close();
	    contCtx = null;
	}
    }
}
