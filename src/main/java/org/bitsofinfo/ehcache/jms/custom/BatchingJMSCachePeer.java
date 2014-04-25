package org.bitsofinfo.ehcache.jms.custom;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.jms.JMSCachePeer;
import net.sf.ehcache.distribution.jms.JMSEventMessage;

import org.bitsofinfo.ehcache.jms.custom.batch.BatchJMSEventMessage;
import org.bitsofinfo.ehcache.jms.custom.batch.BatchJMSEventMessageUtil;

/**
 * Extension of JMSCachePeer that supports
 * the batching of simple JMSEventMessages by
 * extracting out the cacheName, key, action and 
 * storing them in a BatchedJMSEventMessage 
 * 
 * BatchedJMSEventMessages will queue qualifying events
 * until either the maxBatchQueuingTimeMS is reached OR
 * maxEventsPerBatch is met. These criteria are evaluate
 * in every invocation of send() in addition to a simple
 * background thread (BatchProcessingThread) which does
 * it asynchronously should there be no send() activity
 * 
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class BatchingJMSCachePeer extends JMSCachePeer {

	private static final Logger LOG = Logger.getLogger(BatchingJMSCachePeer.class.getName());

	/**
	 * The current BatchJMSEventMessage that is queued
	 * IMPORTANT all access to this much be synchronized
	 * using the batchLock
	 * 
	 */
	private BatchJMSEventMessage currentBatchJMSEventMessage = null;
	private Object batchLock = new Object();

	/*
	 * The maximum time that a pending BatchJMSEventMessage can
	 * sit waiting to be sent 
	 */
	private long maxBatchQueuingTimeMS = 1000 * 30; // default 30s

	/*
	 * The maximum number of batched entries
	 * that can exist in a BatchJMEEventMessage
	 * 
	 * Some tests: 
	 * 	- Loaded up BatchJMSEventMessage with:
	 * 		- 500 cacheNames to REMOVE_ALL
	 * 		- 500 cacheName -> key ACTIONS
	 * 		- both cacheNames and key names were ~144bytes each
	 * 
	 *  - total message size ~168k (json, compressed and b64 encoded)
	 *  - AWS SNS/SQS size limit 256k, so 500 is safe default
	 *  - Nevado JMS has about a 25% overhead @see http://nevado.skyscreamer.org/performance.html
	 */
	private int maxEventsPerBatch = 500; 


	/**
	 * Constructor 
	 * 
	 * @param maxBatchQueuingTimeMS
	 * @param maxEventsPerBatch
	 * @param cacheManager
	 * @param messageProducer
	 * @param producerSession
	 * @param getQueueSession
	 */
	public BatchingJMSCachePeer(long maxBatchQueuingTimeMS,
								int maxEventsPerBatch,
								CacheManager cacheManager,
								MessageProducer messageProducer, 
								Session producerSession,
								QueueSession getQueueSession) {

		super(cacheManager, messageProducer, producerSession, getQueueSession);
		
		// fire up the batch thread
		BatchProcessingThread thread = new BatchProcessingThread();
		thread.start();
	}

	private BatchJMSEventMessage getBatchJMSEventMessage() {
		synchronized(batchLock) {
			if (this.currentBatchJMSEventMessage == null) {
				LOG.log(Level.FINE, "getBatchJMSEventMessage() creating new BatchJMSEventMessage for queueing");
				this.currentBatchJMSEventMessage = new BatchJMSEventMessage();
			}
			return this.currentBatchJMSEventMessage;
		}
	}

	/**
	 * Send the cache peer with an ordered list of {@link net.sf.ehcache.distribution.EventMessage}s.
	 * <p/>
	 * This enables multiple messages to be delivered in one network invocation.
	 *
	 * @param eventMessages a list of type {@link net.sf.ehcache.distribution.EventMessage}
	 */
	@Override
	public void send(List eventMessages) throws RemoteException {

		if (LOG.isLoggable(Level.FINEST)) {
			LOG.finest("send ( eventMessages = " + eventMessages + " ) called ");
		}

		for (Object eventMessage : eventMessages) {

			// if the event is a JMSEventMessage we need
			// to evaluate it for batching eligibility
			if (eventMessage instanceof JMSEventMessage) {
				try {

					JMSEventMessage jem = (JMSEventMessage)eventMessage;

					// if REMOVE_ALL (for entire cache name) OR a key-remove (and key is Batch message compatible)
					// only then can we proceed 
					if (jem.getEvent() == JMSEventMessage.REMOVE_ALL ||
							(jem.getEvent() == JMSEventMessage.REMOVE && 
							BatchJMSEventMessage.keyIsCompatible(jem))) {

						LOG.log(Level.FINE, "send() JMSEventMessage["+jem.getEvent()+"] is batchable... evaluating...:");

						// synchronize on batch lock
						synchronized(batchLock) {

							BatchJMSEventMessage batchJMSEventMessage = getBatchJMSEventMessage();

							if (jem.getEvent() == JMSEventMessage.REMOVE_ALL) {
								batchJMSEventMessage.addRemovalAllCacheName(jem.getCacheName());

								// deal w/ just REMOVE
							} else {
								batchJMSEventMessage.addCacheEvent(jem.getCacheName(), jem.getSerializableKey(),  jem.getEvent());
							}

							/**
							 * OK, if the max events has been reached
							 * OR we are past the max queueing time
							 * 	- a) set the current "eventMessage" = the batch one
							 *  - b) nullify the currentBatchJMSEventMessage so next invocation
							 *  	 will create a new one
							 */

							long totalQueuedTimeMS = (System.currentTimeMillis() - batchJMSEventMessage.getCreatedAt().getTime());

							// past MAX events?
							if (batchJMSEventMessage.getTotalEvents() >= this.maxEventsPerBatch) {
								LOG.log(Level.FINE, "send() sending BatchJMSEventMessage w/ "
										+ batchJMSEventMessage.getTotalEvents() + " events, which is >= max:" + this.maxEventsPerBatch);
								eventMessage = batchJMSEventMessage;
								this.currentBatchJMSEventMessage = null;


								// have we been queueing the Batch message past the max time?
							} else if (totalQueuedTimeMS > this.maxBatchQueuingTimeMS) {
								LOG.log(Level.FINE, "send() sending BatchJMSEventMessage w/ "
										+ batchJMSEventMessage.getTotalEvents() + " events, who's queued" +
										" time ["+totalQueuedTimeMS+"MS] is > max queueing time of " + maxBatchQueuingTimeMS+"MS");
										eventMessage = batchJMSEventMessage;
										this.currentBatchJMSEventMessage = null;

							} else {

								LOG.log(Level.FINE, "send() BatchJMSEventMessage will remain in queue... neither max events or max queue time has been reached");

								// nullify the eventMessage which will prevent anything
								// from being sent right now.....
								eventMessage = null;
							}

						}
					}

				} catch(Throwable e) {
					LOG.log(Level.SEVERE, "send() unexpected error in" +
							" BatchJMSEventMessage processing..." + e.getMessage(),e);
				}
			}




			// if JMSEventMessage send it as an ObjectMessage
			if (eventMessage instanceof JMSEventMessage) {	
				try {
					ObjectMessage message = producerSession.createObjectMessage((JMSEventMessage) eventMessage);
					messageProducer.send(message);
				} catch (JMSException e) {
					LOG.log(Level.SEVERE, e.getMessage(), e);
					throw new RemoteException(e.getMessage());
				}

				// if a BatchJMSEventMessage.... send that (as a simpler TextMessage)
			} else if (eventMessage instanceof BatchJMSEventMessage) {

				try {
					// convert to our string format for the payload
					String batchPayload = BatchJMSEventMessageUtil.toString((BatchJMSEventMessage)eventMessage);

					TextMessage message = producerSession.createTextMessage(batchPayload);
					messageProducer.send(message);

				} catch (Exception e) {
					LOG.log(Level.SEVERE, "send() ERROR marshalling BatchJMSEventMessage! " + e.getMessage(), e);
					throw new RemoteException(e.getMessage());
				}

			} else {
				// we do nothing, the inbound event was likely just 
				// appended to a queued BatchJMSEventMessage 
				// which will be sent later
			}


		}
	}


	/**
	 * @param message a JMSMessage guaranteed to not be sent to the publishing CacheManager instance.
	 */
	@Override
	public void onMessage(Message message) {

		// we need to evaluate the body to see if it
		// is actually a BatchJMSEventMessage
		if (message instanceof TextMessage) {

			try {
				TextMessage textMessage = (TextMessage)message;
				String payloadText = textMessage.getText();

				// inspect the TextMessage, is it a BatchJMSEventMessage inside?
				if (BatchJMSEventMessageUtil.isBatchJMSEventMessage(payloadText)) {

					// lets unmarshal it, then extract expand individual JMSEventMessages from it
					BatchJMSEventMessage batchMessage = BatchJMSEventMessageUtil.fromString(payloadText);
					Set<JMSEventMessage> convertedMsgs = BatchJMSEventMessageUtil.toJMSEventMessages(batchMessage);

					// for each expanded message, super onMessage()
					// using a dummy ObjectMessage carrier
					// we do this because certain methods in JMSCachePeer are private
					// and we can't override them/call them....
					for (JMSEventMessage msg : convertedMsgs) {
						super.onMessage(new JMSEventMessageCarrier(msg));
					}


					// not a batch msg
				} else {
					super.onMessage(message);
				}

			} catch(Throwable e) {
				LOG.log(Level.SEVERE, "Error attempting to evaluate inbound " +
						"TextMessage converting payload to BatchJMSEventMessage?: " + e.getMessage(), e);
			}


		} else {
			super.onMessage(message);
		}

	}
	
	/**
	 * Invoked by the BatchProcessingThread
	 * 
	 */
	private void batchedMessageSender() {
		while(true) {
			try {
				// sleep 
				Thread.currentThread().sleep(this.maxBatchQueuingTimeMS);
				
				BatchJMSEventMessage toSend = null;
				
				synchronized(batchLock) {
					BatchJMSEventMessage toEval = getBatchJMSEventMessage();
					
					long totalQueuedTimeMS = (System.currentTimeMillis() - toEval.getCreatedAt().getTime());

					if (toEval.getTotalEvents() > 0 && totalQueuedTimeMS > this.maxBatchQueuingTimeMS) {
						
						LOG.log(Level.FINE, "batchedMessageSender() sending BatchJMSEventMessage w/ "
								+ toEval.getTotalEvents() + " events, who's queued" +
								" time ["+totalQueuedTimeMS+"MS] is > max queueing time of " + maxBatchQueuingTimeMS+"MS");
								
						toSend = toEval;
						this.currentBatchJMSEventMessage = null;
					}
				}
				
				// send it!
				if (toSend != null) {
					List msgs = new ArrayList<BatchJMSEventMessage>();
					msgs.add(toSend);
					this.send(msgs);
				}
				
				
			} catch(Throwable e) {
				LOG.log(Level.SEVERE, "batchedMessageSender() error: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Dumb/simple internal thread that will force send() 
	 * the currentBatchJMSEventMessage if its been sitting
	 * here longer than its max queuing time
	 *
	 */
	private final class BatchProcessingThread extends Thread {

		/**
		 * Contructs a new replication daemon thread with normal priority.
		 */
		public BatchProcessingThread() {
			super("BatchJMSEventMessage ProcessingThread");
			setDaemon(true);
			setPriority(Thread.NORM_PRIORITY);
		}

		@Override
		public void run() {
			batchedMessageSender();
		}
	}

}
