package org.bitsofinfo.ehcache.jms.custom.nevado;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * Hackish impl of a Nevado Context 
 *
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class NevadoContext implements Context {
	
	private Hashtable<String,Object> env = null;
	
	public NevadoContext(Hashtable<String,Object> env) {
		this.env = env;
	}

	public Object lookup(String name) throws NamingException {
		return env.get(name);
	}

	public Object lookup(Name name) throws NamingException {
		throw new NamingException("Not supported, call lookup(string)");
	}


	public void bind(Name name, Object obj) throws NamingException {
		throw new NamingException("Not supported");
	}

	public void bind(String name, Object obj) throws NamingException {
		throw new NamingException("Not supported");
	}

	public void rebind(Name name, Object obj) throws NamingException {
		throw new NamingException("Not supported");
	}

	public void rebind(String name, Object obj) throws NamingException {
		throw new NamingException("Not supported");
	}

	public void unbind(Name name) throws NamingException {
		throw new NamingException("Not supported");
	}

	public void unbind(String name) throws NamingException {
		throw new NamingException("Not supported");
	}

	public void rename(Name oldName, Name newName) throws NamingException {
		throw new NamingException("Not supported");
	}

	public void rename(String oldName, String newName) throws NamingException {
		throw new NamingException("Not supported");
	}

	public NamingEnumeration<NameClassPair> list(Name name)
			throws NamingException {
		throw new NamingException("Not supported");
	}

	public NamingEnumeration<NameClassPair> list(String name)
			throws NamingException {
		throw new NamingException("Not supported");
	}

	public NamingEnumeration<Binding> listBindings(Name name)
			throws NamingException {
		throw new NamingException("Not supported");
	}

	public NamingEnumeration<Binding> listBindings(String name)
			throws NamingException {
		throw new NamingException("Not supported");
	}

	public void destroySubcontext(Name name) throws NamingException {
		throw new NamingException("Not supported");
	}

	public void destroySubcontext(String name) throws NamingException {
		throw new NamingException("Not supported");
	}

	public Context createSubcontext(Name name) throws NamingException {
		throw new NamingException("Not supported");
	}

	public Context createSubcontext(String name) throws NamingException {
		throw new NamingException("Not supported");
	}

	public Object lookupLink(Name name) throws NamingException {
		throw new NamingException("Not supported");
	}

	public Object lookupLink(String name) throws NamingException {
		throw new NamingException("Not supported");
	}

	public NameParser getNameParser(Name name) throws NamingException {
		throw new NamingException("Not supported");
	}

	public NameParser getNameParser(String name) throws NamingException {
		throw new NamingException("Not supported");
	}

	public Name composeName(Name name, Name prefix) throws NamingException {
		throw new NamingException("Not supported");
	}

	public String composeName(String name, String prefix)
			throws NamingException {
		throw new NamingException("Not supported");
	}

	public Object addToEnvironment(String propName, Object propVal)
			throws NamingException {
		throw new NamingException("Not supported");
	}

	public Object removeFromEnvironment(String propName) throws NamingException {
		throw new NamingException("Not supported");
	}

	public Hashtable<?, ?> getEnvironment() throws NamingException {
		return (Hashtable)env.clone();
	}

	public void close() throws NamingException {
		throw new NamingException("Not supported");
	}

	public String getNameInNamespace() throws NamingException {
		throw new NamingException("Not supported");
	}


}
