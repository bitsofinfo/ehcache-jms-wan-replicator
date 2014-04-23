package org.bitsofinfo.ehcache.jms.custom;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Proxy class for a MessageListener that gives us hook
 * to wrap a MessageListener
 * to permit filtering of inbound messages in onMessage(). This is only used
 * as a workaround for inbound message filterering with JMS impls (like Nevado) 
 * that do not support traditional JMS message selectors
 *
 * @see onMessage()  
 * 
 * Someone could improve on this by wiring in a real message selector parsing
 * library to do the filtering here. Obvisouly the functionality here is quite limited.
 * 
 * I.E. ALL INBOUND MESSAGES WILL BE IGNORED: 
 * 		where Message.getProperty(msgProperty) = valueToIgnore
 * 
 * Again... this is only triggered if 
 * 'msgProperty' is not null (passed in through referencing/calling proxies)
 * and for JMS impls that do NOT support selectors...
 *
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class MessageListenerWrapper implements MessageListener {
	
    private static final Logger LOG = Logger.getLogger(MessageListenerWrapper.class.getName());

	private MessageListener proxied = null;
	private String msgProperty = null;
	private String valueToIgnore = null;
	
	public MessageListenerWrapper(MessageListener proxied, String msgProperty, String valueToIgnore) {
		super();
		this.proxied = proxied;
		this.msgProperty = msgProperty;
		this.valueToIgnore = valueToIgnore;
	}


	public void onMessage(Message msg) {
		try {
			if (msg.propertyExists(msgProperty)) {
				String value = msg.getStringProperty(msgProperty);
				if (value != null && value.equalsIgnoreCase(valueToIgnore)) {
					LOG.log(Level.FINE, "onMessage() ignoring message w/ " +
							"property matching " + msgProperty + " = " + valueToIgnore);
					return;
				}
			}
		} catch(Exception e) {
			LOG.log(Level.WARNING, "onMessage() unexepcted error " +
					"inspecting message property: " + e.getMessage(), e);
			return;
		}
		
		proxied.onMessage(msg);
	}


}
