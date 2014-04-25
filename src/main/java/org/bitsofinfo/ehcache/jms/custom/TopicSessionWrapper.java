package org.bitsofinfo.ehcache.jms.custom;

import java.io.Serializable;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;


/**
 * Proxy class for a TopicSession that gives us hook
 * to wrap the TopicSession to permit filtering of inbound messages. 
 * 
 * Properties 'msgProperty' and 'valueToIgnore' should only specified when used 
 * with JMS impls (like Nevado) that do not support message selectors. In this
 * case these two properties are used by the TopicSubscriberWrapper this 
 * creates in createSubscriber() (only if 'msgProperty' is not null)
 * 
 * The 'msgSelector' property should be provided when this is used with 
 * JMS impls that support selectors
 *
 *
 * @see TopicConnectionWrapper 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class TopicSessionWrapper implements TopicSession {
	
	private TopicSession proxied = null;
	private Map<String,String> msgDecorations = null; 
	
	// for impls where selectors ARE supported
	private String msgSelector = null;
	
	// for impls were selectors are not supported
	private String msgProperty = null;
	private String valueToIgnore = null;
	

	/**
	 * Call this for impls that support message selectors on topics
	 * 
	 * @param proxied
	 * @param selector
	 * @param messageDecorations
	 */
	public TopicSessionWrapper(TopicSession proxied, String selector, Map<String,String> messageDecorations) {
		super();
		this.proxied = proxied;
		this.msgDecorations = messageDecorations;
		
		if (selector != null && selector.trim().length() > 0) {
			this.msgSelector = selector;
		}
	}
	

	/**
	 * Call this for impls that do NOT support message selectors on topics
	 * 
	 * @param proxied
	 * @param selector
	 * @param messageDecorations
	 */
	public TopicSessionWrapper(TopicSession proxied, String msgProperty, String valueToIgnore, Map<String,String> messageDecorations) {
		super();
		this.proxied = proxied;
		this.msgDecorations = messageDecorations;
		this.msgProperty = msgProperty;
		this.valueToIgnore = valueToIgnore;
	}
	

	public TopicPublisher createPublisher(Topic arg0) throws JMSException {
		return proxied.createPublisher(arg0);
	}

	
	/**
	 * Decorate the message w/ the configured decorations
	 * 
	 * @param message
	 * @return
	 * @throws JMSException
	 */
	private Message decorateMessage(Message message) throws JMSException {
		for (String msgProp : msgDecorations.keySet()) {
			message.setStringProperty(msgProp, msgDecorations.get(msgProp));
		}
		return message;
	}
	
	/**
	 * Create a TopicSubscriber (proxied) if message selector not supported
	 */
	public TopicSubscriber createSubscriber(Topic topic, String selector, boolean arg2) throws JMSException {
		
		if (selector != null) {
			selector+= " AND ("+msgSelector+")";
			
		} else if (msgSelector != null) {
			selector = msgSelector;
		}
		
		TopicSubscriber topicSubscriber =  proxied.createSubscriber(topic, selector, arg2);
		
		// if msgProperty and value to ignore are present, wrap it for basic filtering
		// because the actual JMS impl does not support selectors
		if (msgProperty != null) {
			topicSubscriber = new TopicSubscriberWrapper(topicSubscriber, msgProperty, valueToIgnore);
		}
		
		return topicSubscriber;
	}
	

	public ObjectMessage createObjectMessage() throws JMSException {
		return (ObjectMessage)decorateMessage(proxied.createObjectMessage());
	}

	public ObjectMessage createObjectMessage(Serializable arg0)
			throws JMSException {
		return (ObjectMessage)decorateMessage(proxied.createObjectMessage(arg0));
	}

	public void close() throws JMSException {
		proxied.close();
	}

	public void commit() throws JMSException {
		proxied.commit();
	}

	public QueueBrowser createBrowser(Queue arg0) throws JMSException {
		return proxied.createBrowser(arg0);
	}

	public QueueBrowser createBrowser(Queue arg0, String arg1)
			throws JMSException {
		return proxied.createBrowser(arg0, arg1);
	}

	public BytesMessage createBytesMessage() throws JMSException {
		return (BytesMessage)decorateMessage(proxied.createBytesMessage());
	}

	public MessageConsumer createConsumer(Destination arg0) throws JMSException {
		return proxied.createConsumer(arg0);
	}

	public MessageConsumer createConsumer(Destination arg0, String arg1)
			throws JMSException {
		return proxied.createConsumer(arg0, arg1);
	}

	public MessageConsumer createConsumer(Destination arg0, String arg1,
			boolean arg2) throws JMSException {
		return proxied.createConsumer(arg0, arg1, arg2);
	}

	public MapMessage createMapMessage() throws JMSException {
		return (MapMessage)decorateMessage(proxied.createMapMessage());
	}

	public Message createMessage() throws JMSException {
		return (Message)decorateMessage(proxied.createMessage());
	}

	public MessageProducer createProducer(Destination arg0) throws JMSException {
		return proxied.createProducer(arg0);
	}

	public Queue createQueue(String arg0) throws JMSException {
		return proxied.createQueue(arg0);
	}

	public StreamMessage createStreamMessage() throws JMSException {
		return (StreamMessage)decorateMessage(proxied.createStreamMessage());
	}

	public TemporaryQueue createTemporaryQueue() throws JMSException {
		return proxied.createTemporaryQueue();
	}

	public TextMessage createTextMessage() throws JMSException {
		return (TextMessage)decorateMessage(proxied.createTextMessage());
	}

	public TextMessage createTextMessage(String arg0) throws JMSException {
		return (TextMessage)decorateMessage(proxied.createTextMessage(arg0));
	}

	public int getAcknowledgeMode() throws JMSException {
		return proxied.getAcknowledgeMode();
	}

	public MessageListener getMessageListener() throws JMSException {
		return proxied.getMessageListener();
	}

	public boolean getTransacted() throws JMSException {
		return proxied.getTransacted();
	}

	public void recover() throws JMSException {
		proxied.recover();
	}

	public void rollback() throws JMSException {
		proxied.rollback();
	}

	public void run() {
		proxied.run();
	}

	public void setMessageListener(MessageListener arg0) throws JMSException {
		proxied.setMessageListener(arg0);
	}

	public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1)
			throws JMSException {
		return proxied.createDurableSubscriber(arg0, arg1);
	}

	public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1,
			String arg2, boolean arg3) throws JMSException {
		return proxied.createDurableSubscriber(arg0, arg1, arg2, arg3);
	}

	public TopicSubscriber createSubscriber(Topic arg0) throws JMSException {
		return proxied.createSubscriber(arg0);
	}

	public TemporaryTopic createTemporaryTopic() throws JMSException {
		return proxied.createTemporaryTopic();
	}

	public Topic createTopic(String arg0) throws JMSException {
		return proxied.createTopic(arg0);
	}

	public void unsubscribe(String arg0) throws JMSException {
		proxied.unsubscribe(arg0);
	}



}
