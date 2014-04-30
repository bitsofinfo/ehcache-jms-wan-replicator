package org.bitsofinfo.ehcache.jms.custom;

import static net.sf.ehcache.distribution.jms.JMSUtil.CACHE_MANAGER_UID;
import static net.sf.ehcache.distribution.jms.JMSUtil.localCacheManagerUid;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.jms.AcknowledgementMode;
import net.sf.ehcache.distribution.jms.JMSCacheManagerPeerProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Batching JMSCacheManagerPeerProvider
 * 
 * @see init()
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class BatchingJMSCacheManagerPeerProvider extends JMSCacheManagerPeerProvider {

	private final Log LOG = LogFactory.getLog(getClass());
	 
	private long maxBatchQueuingTimeMS;
	private int maxEventsPerBatch;
	
	/**
	 * Constructor
	 * 
	 * @param maxBatchQueuingTimeMS
	 * @param maxEventsPerBatch
	 * @param cacheManager
	 * @param replicationTopicConnection
	 * @param replicationTopic
	 * @param getQueueConnection
	 * @param getQueue
	 * @param acknowledgementMode
	 * @param listenToTopic
	 */
	public BatchingJMSCacheManagerPeerProvider(long maxBatchQueuingTimeMS,
												int maxEventsPerBatch,				
												CacheManager cacheManager,
												TopicConnection replicationTopicConnection, 
												Topic replicationTopic,
												QueueConnection getQueueConnection, 
												Queue getQueue,
												AcknowledgementMode acknowledgementMode, 
												boolean listenToTopic) {
		
		super(cacheManager, replicationTopicConnection, replicationTopic,
				getQueueConnection, getQueue, acknowledgementMode, listenToTopic);
		
		this.maxBatchQueuingTimeMS = maxBatchQueuingTimeMS;
		this.maxEventsPerBatch = maxEventsPerBatch;
	}

	
	/**
     * Notifies providers to initialize themselves.
     * <p/>
     * 
     * NOTE! This is a direct COPY of JMSCacheManagerPeerProvider's init() method
     * with the ONLY change being the CachePeer we instantiate which is
     * BatchingJMSCachePeer instead of the default JMSCachePeer 
     *
     * @throws CacheException
     */
	@Override
    public void init() {

        try {

            topicPublisherSession = replicationTopicConnection.createTopicSession(false, acknowledgementMode.toInt());
            replicationTopicConnection.setExceptionListener(new ExceptionListener() {

                public void onException(JMSException e) {
                    LOG.error("Exception on replication Connection: " + e.getMessage(), e);
                }
            });

            topicPublisher = topicPublisherSession.createPublisher(replicationTopic);

            if (listenToTopic) {

                LOG.debug("Listening for message on topic " + replicationTopic.getTopicName());
                //ignore messages we have sent. The third parameter is noLocal, which means do not deliver back to the sender
                //on the same connection
                TopicSession topicSubscriberSession = replicationTopicConnection.createTopicSession(false, acknowledgementMode.toInt());
                topicSubscriber = topicSubscriberSession.createSubscriber(replicationTopic, null, true);
                replicationTopicConnection.start();
            }


            //noLocal is only supported in the JMS spec for topics. We need to use a message selector
            //on the queue to achieve the same effect.
            getQueueSession = getQueueConnection.createQueueSession(false, acknowledgementMode.toInt());
            String messageSelector = CACHE_MANAGER_UID + " <> " + localCacheManagerUid(cacheManager);
            getQueueRequestReceiver = getQueueSession.createReceiver(getQueue, messageSelector);


            getQueueConnection.start();


        } catch (JMSException e) {
            throw new CacheException("Exception while creating JMS connections: " + e.getMessage(), e);
        }


        /**
         * THIS LINE IS ALL THAT IS DIFFERENT
         * From Ehcache JMS Replication/distribution projects JMSCachePeer.java
         */
        cachePeer = new BatchingJMSCachePeer(maxBatchQueuingTimeMS, maxEventsPerBatch, 
        		cacheManager, topicPublisher, topicPublisherSession, getQueueSession);
        /**
         * END DIFFERENCE
         */
        
        remoteCachePeers.add(cachePeer);
        try {
            if (listenToTopic) {
                topicSubscriber.setMessageListener(cachePeer);
            }
            getQueueRequestReceiver.setMessageListener(cachePeer);
        } catch (JMSException e) {
            LOG.error("Cannot register " + cachePeer + " as messageListener", e);
        }


    }

}
