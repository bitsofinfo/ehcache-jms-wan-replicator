package org.bitsofinfo.ehcache.jms.custom;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CacheManagerPeerProviderFactory;
import net.sf.ehcache.distribution.jms.AcknowledgementMode;
import net.sf.ehcache.distribution.jms.JMSCacheManagerPeerProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of CacheManagerPeerProviderFactory that returns 
 * a JMSCacheManagerPeerProvider that can be configured for:
 * 
 * a) inbound Ehcache event Message filtering
 * b) overriding the local Ehcache actions that will be replicated to other CachePeers
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class CustomizableJMSCacheManagerPeerProviderFactory extends CacheManagerPeerProviderFactory {
	
	private final Log LOG = LogFactory.getLog(getClass());
	
	public static final String CTX_PROP_TOPIC = "topic";
	public static final String CTX_PROP_TOPIC_CONNECTION = "topicConnection";
	public static final String CTX_PROP_GET_REPLY_QUEUE = "getReplyQueue";
	public static final String CTX_PROP_GET_REPLY_QUEUE_CONNECTION = "getReplyQueueConnection";

	@Override
	public CacheManagerPeerProvider createCachePeerProvider(CacheManager cacheManager, Properties props) {
		
		try {
			
			if (isBlank(props.getProperty("initialContextFactoryName"))) {
				LOG.error("createCachePeerProvider() 'initialContextFactoryName' is REQUIRED");
			}

			String initialContextFactoryName = props.getProperty("initialContextFactoryName");

			TopicConnection topicConnection = null;
			Topic topic = null;
			Queue getReplyQueue = null;
			QueueConnection getReplyQueueConnection = null;
			
			try {
				InitialContextFactory contextFactory = (InitialContextFactory)ClassLoading.loadClass(initialContextFactoryName).newInstance();
				Context context = contextFactory.getInitialContext(props);
				
				topic = (Topic)context.lookup(CTX_PROP_TOPIC);
				getReplyQueue = (Queue)context.lookup(CTX_PROP_GET_REPLY_QUEUE);
				getReplyQueueConnection = (QueueConnection)context.lookup(CTX_PROP_GET_REPLY_QUEUE_CONNECTION);
				topicConnection = (TopicConnection)context.lookup(CTX_PROP_TOPIC_CONNECTION);
				
			} catch(Exception e) {
				LOG.error("createCachePeerProvider() error creating Context: " + initialContextFactoryName, e);
				return null;
			}

			if (isBlank(props.getProperty("messageSelectorsSupported"))) {
				LOG.error("createCachePeerProvider() 'messageSelectorsSupported' is REQUIRED");
			}

			if (isBlank(props.getProperty("messageSelectorsSupported"))) {
				LOG.error("createCachePeerProvider() 'messageSelectorsSupported' is REQUIRED");
			}

			if (isBlank(props.getProperty("maxBatchQueuingTimeMS"))) {
				LOG.error("createCachePeerProvider() 'maxBatchQueuingTimeMS' is REQUIRED");
			}
			
			if (isBlank(props.getProperty("maxEventsPerBatch"))) {
				LOG.error("createCachePeerProvider() 'maxEventsPerBatch' is REQUIRED");
			}



			Map<String,String> messageDecorations = expandDotNotationProperty(props,"messageDecorations");
			
			if (messageDecorations.size() == 0) {
				LOG.warn("createCachePeerProvider() messageDecorations.propName (one or more) is recommended");
			}
			
			boolean messageSelectorsSupported = Boolean.valueOf(props.getProperty("messageSelectorsSupported"));
			
			if (messageSelectorsSupported && isBlank(props.getProperty("messageSelector"))) {
				
				LOG.warn("createCachePeerProvider() messageSelectorsSupported=true but 'messageSelector' is NOT DEFINED.");
				
			} else if (!messageSelectorsSupported && 
						(isBlank(props.getProperty("ignoreMessagePropVal")) || 
						isBlank(props.getProperty("ignoreMessagePropName"))) )	{
				
				LOG.warn("createCachePeerProvider() messageSelectorsSupported=false " +
						"but 'ignoreMessagePropName' and/or 'ignoreMessagePropVal' is NOT DEFINED.");
				
			}
			
			String messageSelector = props.getProperty("messageSelector");
			String ignoreMessagePropName = props.getProperty("ignoreMessagePropName");
			String ignoreMessagePropVal = props.getProperty("ignoreMessagePropVal");
			Long maxBatchQueuingTimeMS = Long.valueOf(props.getProperty("maxBatchQueuingTimeMS"));
			Integer maxEventsPerBatch = Integer.valueOf(props.getProperty("maxEventsPerBatch"));

			
			
			TopicConnection topicConnectionProxy = null;

			if (!messageSelectorsSupported) {	
				
				LOG.info("createCachePeerProvider() Creating JMSCacheManagerPeerProvider " +
						"configured to decorate all outbound JMS Messages w/ properties: " + map2String(messageDecorations) + 
						" AND IGNORE inbound messages where " + ignoreMessagePropName + " = " + ignoreMessagePropVal);
				
				topicConnectionProxy = new TopicConnectionWrapper(topicConnection, 
																  ignoreMessagePropName, ignoreMessagePropVal, 
																  messageDecorations);
				
			} else {
				
				LOG.info("createCachePeerProvider() Creating JMSCacheManagerPeerProvider " +
						"configured to decorate all outbound JMS Messages w/ properties: " + map2String(messageDecorations) + 
						" AND IGNORE filter inbound messages where messageSelector = " + messageSelector);
				
				topicConnectionProxy = new TopicConnectionWrapper(topicConnection, messageSelector, messageDecorations);
			}
			
			
			// register shutdown thread for cleanup
			Runtime.getRuntime().addShutdownHook(new JMSCleanupShutdownHook(topicConnection,getReplyQueueConnection));
			
			return new BatchingJMSCacheManagerPeerProvider(maxBatchQueuingTimeMS,
														   maxEventsPerBatch,
														   cacheManager, 
														   topicConnectionProxy, 
														   topic, 
														   getReplyQueueConnection, 
														   getReplyQueue, 
														   AcknowledgementMode.AUTO_ACKNOWLEDGE, 
														   true);
			
		} catch(Exception e) {
			LOG.error("createCachePeerProvider() unexpected error: " + e.getMessage(), e);
			return null;
		}
	}
	
	private String map2String(Map<String,String> map) {
		StringBuffer sb = new StringBuffer();
		for (String key : map.keySet()) {
			sb.append(" {"+key +" -> " + map.get(key)+"} ");
		}
		return sb.toString();
	}
	
		private Map<String,String> expandDotNotationProperty(Properties props, String propName) {
			
			Map<String,String> map = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
			for (Map.Entry<Object,Object> val : props.entrySet()) {
				String key = val.getKey().toString();
				if (key.toString().indexOf(propName) != -1) {
					String[] parts = key.toString().split("\\.");
					map.put(parts[1],val.getValue().toString());
				}
			}
			return map;
		}
	
	private boolean isBlank(String str) {
		return str == null || str.trim().length() == 0;
	}
	
	private class JMSCleanupShutdownHook extends Thread {
		
		private final Log LOG = LogFactory.getLog(getClass());
		
		private TopicConnection topicConn = null;
		private QueueConnection queueConn = null;

		public JMSCleanupShutdownHook(TopicConnection topicConn, QueueConnection queueConn) {
			super();
			this.topicConn = topicConn;
			this.queueConn = queueConn;
		}
		
	    public void run() {
	      System.out.println("JMSCleanupShutdownHook: Thread initiated.");
	      try {
	    	  topicConn.close();
	      } catch(Throwable e) {
	    	  LOG.warn("JMSCleanupShutdownHook topicConn.close() : " + e.getMessage());
	      }
	      try {
	    	  queueConn.close();
	      } catch(Throwable e) {
	    	  LOG.warn("JMSCleanupShutdownHook queueConn.close() : " + e.getMessage());
	      }
	    }

	  }


}
