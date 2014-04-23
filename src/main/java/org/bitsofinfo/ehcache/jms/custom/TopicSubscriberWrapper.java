package org.bitsofinfo.ehcache.jms.custom;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;


/**
 * Proxy class for a TopicSubscriber that gives us hook
 * to wrap the MessageListener with our custom MessageListener
 * to permit filtering of inbound messages in onMessage(). This is only used
 * as a workaround for inbound message filterering with JMS impls (like Nevado) 
 * that do not support traditional JMS message selectors
 *
 * @see MessageListenerWrapper
 *
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class TopicSubscriberWrapper implements TopicSubscriber {
	
	private TopicSubscriber proxied = null;
	private String msgProperty = null;
	private String valueToIgnore = null;

	public TopicSubscriberWrapper(TopicSubscriber proxied, String msgProperty, String valueToIgnore) {
		super();
		this.proxied = proxied;
		this.msgProperty = msgProperty;
		this.valueToIgnore = valueToIgnore;
		
	}

	public void setMessageListener(MessageListener listener) throws JMSException {
		proxied.setMessageListener(new MessageListenerWrapper(listener, msgProperty, valueToIgnore));;
	}


	public void close() throws JMSException {
		proxied.close();
	}


	public MessageListener getMessageListener() throws JMSException {
		return proxied.getMessageListener();
	}


	public String getMessageSelector() throws JMSException {
		return proxied.getMessageSelector();
	}


	public Message receive() throws JMSException {
		return proxied.receive();
	}


	public Message receive(long arg0) throws JMSException {
		return proxied.receive(arg0);
	}


	public Message receiveNoWait() throws JMSException {
		return proxied.receiveNoWait();
	}

	public boolean getNoLocal() throws JMSException {
		return proxied.getNoLocal();
	}


	public Topic getTopic() throws JMSException {
		return proxied.getTopic();
	}
	

}
