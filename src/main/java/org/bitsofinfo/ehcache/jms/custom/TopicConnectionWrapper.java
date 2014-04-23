package org.bitsofinfo.ehcache.jms.custom;

import java.util.Map;

import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

/**
 * Proxy class for a TopicConnection that gives us a hook path
 * to permit filtering of inbound messages in onMessage() calls lower in the stack. 
 * 
 * This is only used as a workaround for inbound message filterering with JMS impls (like Nevado) 
 * that do not support traditional JMS message selectors
 *
 * @see MessageListenerWrapper
 *
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class TopicConnectionWrapper implements TopicConnection {
	
	private TopicConnection proxied = null;
	
	private Map<String,String> messageDecorations = null;
	
	// for JMS impls that support selectors
	private String messageSelector = null;
	
	// for impls were selectors are not supported
	private String msgProperty = null;
	private String valueToIgnore = null;
	

	/**
	 * Call this one for JMS impls that support selectors
	 * 
	 * @param proxied
	 * @param messageSelector
	 * @param messageDecorations
	 */
	public TopicConnectionWrapper(TopicConnection proxied, String messageSelector, Map<String,String> messageDecorations) {
		super();
		this.proxied = proxied;
		this.messageDecorations = messageDecorations;
		this.messageSelector = messageSelector;
	}
	
	/**
	 * Call this one for JMS impls that don't support selectors
	 * 
	 * @see MessageListenerWrapper
	 * 
	 * @param proxied
	 * @param msgProperty
	 * @param valueToIgnore
	 * @param messageDecorations
	 */
	public TopicConnectionWrapper(TopicConnection proxied, String msgProperty, String valueToIgnore, Map<String,String> messageDecorations) {
		super();
		this.proxied = proxied;
		this.messageDecorations = messageDecorations;
		this.msgProperty = msgProperty;
		this.valueToIgnore = valueToIgnore;
	}

	public TopicSession createTopicSession(boolean arg0, int arg1) throws JMSException {
		TopicSession toProxy = proxied.createTopicSession(arg0, arg1);
		
		if (msgProperty != null) {
			
			return new TopicSessionWrapper(toProxy, msgProperty, valueToIgnore, messageDecorations);
			
		} else {
			return new TopicSessionWrapper(toProxy, messageSelector, messageDecorations);
		}
		
	}

	public void close() throws JMSException {
		proxied.close();
	}

	public ConnectionConsumer createConnectionConsumer(Destination arg0,
			String arg1, ServerSessionPool arg2, int arg3) throws JMSException {
		return proxied.createConnectionConsumer(arg0, arg1, arg2, arg3);
	}

	public Session createSession(boolean arg0, int arg1) throws JMSException {
		return proxied.createSession(arg0, arg1);
	}

	public String getClientID() throws JMSException {
		return proxied.getClientID();
	}

	public ExceptionListener getExceptionListener() throws JMSException {
		return proxied.getExceptionListener();
	}

	public ConnectionMetaData getMetaData() throws JMSException {
		return proxied.getMetaData();
	}

	public void setClientID(String arg0) throws JMSException {
		proxied.setClientID(arg0);
	}

	public void setExceptionListener(ExceptionListener arg0)
			throws JMSException {
		proxied.setExceptionListener(arg0);
	}

	public void start() throws JMSException {
		proxied.start();
	}

	public void stop() throws JMSException {
		proxied.stop();
	}

	public ConnectionConsumer createConnectionConsumer(Topic arg0, String arg1,
			ServerSessionPool arg2, int arg3) throws JMSException {
		return proxied.createConnectionConsumer(arg0, arg1, arg2, arg3);
	}

	public ConnectionConsumer createDurableConnectionConsumer(Topic arg0,
			String arg1, String arg2, ServerSessionPool arg3, int arg4)
			throws JMSException {
		return proxied.createDurableConnectionConsumer(arg0, arg1, arg2, arg3, arg4);
	}




}
