package org.bitsofinfo.ehcache.jms.custom.nevado;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.bitsofinfo.ehcache.jms.custom.CustomizableJMSCacheManagerPeerProviderFactory;
import org.skyscreamer.nevado.jms.NevadoConnectionFactory;
import org.skyscreamer.nevado.jms.connector.amazonaws.AmazonAwsSQSConnectorFactory;
import org.skyscreamer.nevado.jms.destination.NevadoQueue;
import org.skyscreamer.nevado.jms.destination.NevadoTopic;


/**
 * Hackish impl of a Nevado InitialContextFactory 
 * 
 * Returns a Context w/ four properities
 * - topic
 * - getQueue
 * - topicConnection
 * - getQueueConnection
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class NevadoInitialContextFactory implements InitialContextFactory {
	
	private static final Logger LOG = Logger.getLogger(NevadoInitialContextFactory.class.getName());
	
	public Context getInitialContext(Hashtable environment) throws NamingException {


		try {
			if (isBlank(environment.get("nevado.awsAccessKey"))) {
				LOG.log(Level.SEVERE, "createCachePeerProvider() 'awsAccessKey' is REQUIRED");
			}
			if (isBlank(environment.get("nevado.awsSecretKey"))) {
				LOG.log(Level.SEVERE, "createCachePeerProvider() 'awsSecretKey' is REQUIRED");
			}
			if (isBlank(environment.get("nevado.SQSQueueName"))) {
				LOG.log(Level.SEVERE, "createCachePeerProvider() 'SQSQueueName' is REQUIRED");
			}
			if (isBlank(environment.get("nevado.SNSTopicName"))) {
				LOG.log(Level.SEVERE, "createCachePeerProvider() 'SNSTopicName' is REQUIRED");
			}
			
			NevadoConnectionFactory nevadoConnectionFactory = 
						new NevadoConnectionFactory(new AmazonAwsSQSConnectorFactory());
			
			
			nevadoConnectionFactory.setAwsAccessKey(environment.get("nevado.awsAccessKey").toString());
			nevadoConnectionFactory.setAwsSecretKey(environment.get("nevado.awsSecretKey").toString());
	
			Topic topic = (Topic)new NevadoTopic(environment.get("nevado.SNSTopicName").toString());
			Queue getReplyQueue = (Queue)new NevadoQueue(environment.get("nevado.SQSQueueName").toString());
			
			TopicConnection topicConnection = nevadoConnectionFactory.createTopicConnection();
			QueueConnection getReplyQueueConnection = nevadoConnectionFactory.createQueueConnection();
			
			// put in environment
			Hashtable<String,Object> data = new Hashtable<String,Object>();
			data.put(CustomizableJMSCacheManagerPeerProviderFactory.CTX_PROP_TOPIC,topic);
			data.put(CustomizableJMSCacheManagerPeerProviderFactory.CTX_PROP_GET_REPLY_QUEUE,getReplyQueue);
			data.put(CustomizableJMSCacheManagerPeerProviderFactory.CTX_PROP_TOPIC_CONNECTION,topicConnection);
			data.put(CustomizableJMSCacheManagerPeerProviderFactory.CTX_PROP_GET_REPLY_QUEUE_CONNECTION,getReplyQueueConnection);
					
			// return 
			return new NevadoContext(data);
			
		} catch(Exception e) {
			LOG.log(Level.SEVERE, "getInitialContext() error: " + e.getMessage(), e);
			throw new NamingException("Error creating Nevado JMS resources: " + e.getMessage());
		}
	}

	private boolean isBlank(Object str) {
		return !(str instanceof String) || ((String)str).trim().length() == 0;
	}
	
}
