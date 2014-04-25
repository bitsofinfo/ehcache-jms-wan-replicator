package org.bitsofinfo.ehcache.jms.custom.batch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.jms.Action;
import net.sf.ehcache.distribution.jms.JMSEventMessage;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;

/**
 * Utilities for BatchJMSEventMessage operations
 * 
 * marshalling/unmarshalling and expanding to Ehcache JMSEventMessages
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class BatchJMSEventMessageUtil {
	
	public static final String MSG_PREFIX = "batch@:";

	/**
	 * Take a BatchJMSEventMessage and expand it into individual JMSEventMessages
	 * 
	 * @param batchMessage
	 * @return
	 */
	public static Set<JMSEventMessage> toJMSEventMessages(BatchJMSEventMessage batchMessage) {
		
		HashSet<JMSEventMessage> toReturn = new HashSet<JMSEventMessage>();
		
		// convert all "REMOVE_ALL" events...
		for (String cacheName2RemoveAll : batchMessage.getRemoveAllCacheNames()) {
			toReturn.add(new JMSEventMessage(Action.REMOVE_ALL, null, null, cacheName2RemoveAll, null));
		}
		
		// convert all REMOVE events for each cache that has keys to remove
		for (String cacheName : batchMessage.getCacheNamesWithEvents()) {
			for (Serializable keyToRemove : batchMessage.getRemoveEventsFor(cacheName)) {
				toReturn.add(new JMSEventMessage(Action.REMOVE, keyToRemove, 
						new Element(keyToRemove,null), cacheName, null));
			}
		}
		
		// return all expanded JMSEventMessages
		return toReturn;
	}
	
	/**
	 * Take a BatchJMSEventMessage and convert it to a string for transport
	 * (base-64 encoded, compressed JSON string) prefixed with MSG_PREFIX
	 * 
	 * @param batchMessage
	 * @return
	 * @throws Exception
	 */
	public static String toString(BatchJMSEventMessage batchMessage) throws Exception {
		
		String json = new Gson().toJson(batchMessage);
		
		// Compressor with highest level of compression
	    Deflater compressor = new Deflater();
	    compressor.setLevel(Deflater.BEST_COMPRESSION);
	    
	    // Give the compressor the data to compress
	    byte[] jsonBytes = json.getBytes();
	    compressor.setInput(jsonBytes);
	    compressor.finish();
	    
	    // Create an expandable byte array to hold the compressed data.
	    // It is not necessary that the compressed data will be smaller than
	    // the uncompressed data.
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(jsonBytes.length);
	    
	    // Compress the data
	    byte[] buf = new byte[32];
	    while (!compressor.finished()) {
	        int count = compressor.deflate(buf);
	        bos.write(buf, 0, count);
	    }
	    try {
	        bos.close();
	    } catch (IOException e) {
	    }
	    
	    // Get the compressed data
	    byte[] compressedData = bos.toByteArray();
	    
	    return MSG_PREFIX + Base64.encodeBase64String(compressedData);
	}
	
	public static boolean isBatchJMSEventMessage(String canidateString) {
		if (canidateString.startsWith(MSG_PREFIX)) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Take a base64 encoded, compressed JSON string (prefixed w/ MSG_PREFIX) 
	 * and convert it to a BatchJMSEventMessage
	 * 
	 * @param batchMessageString
	 * @return
	 * @throws Exception
	 */
	public static BatchJMSEventMessage fromString(String batchMessageString) throws Exception {
		
		if (!isBatchJMSEventMessage(batchMessageString)) {
			throw new Exception("BatchJMSEventMessageUtil.toString(), cannot process, " +
					"passed msg was not prefixed w/ " + MSG_PREFIX);
		}
		
		byte[] decodedCompressed = Base64.decodeBase64(
				batchMessageString.replaceFirst(MSG_PREFIX, ""));
		
		// Compressor with highest level of compression
	    Inflater inflater = new Inflater();
	    
	    // Give the compressor the data to compress
	    inflater.setInput(decodedCompressed);
	    
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    byte[] buf = new byte[32];
	    while (!inflater.finished()) {
	        int count = inflater.inflate(buf);
	        stream.write(buf, 0, count);
	    }
	    String asJson = new String(stream.toByteArray(),"UTF-8");

		return (BatchJMSEventMessage)new Gson().fromJson(asJson,BatchJMSEventMessage.class);
	}
	
	public static void main(String[] arg) throws Exception {
		
		BatchJMSEventMessage msg = new BatchJMSEventMessage();
		for (int i=0; i< 500; i++) {
			msg.addRemovalAllCacheName(UUID.randomUUID().toString() +UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString());
		}
		
		for (int i=0; i< 500; i++) {
			String x = UUID.randomUUID().toString() +UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
			System.out.println(x.length());
			msg.addCacheEvent(x, 
					UUID.randomUUID().toString() +UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString(), JMSEventMessage.REMOVE);
		}
		
		
		String compressedAsB64 = BatchJMSEventMessageUtil.toString(msg);
		System.out.println(compressedAsB64);
		
		BatchJMSEventMessage reconstituted = BatchJMSEventMessageUtil.fromString(compressedAsB64);
		String compressedAsB642 = BatchJMSEventMessageUtil.toString(reconstituted);

		System.out.println("LENGTH = " + compressedAsB642.length());
		System.out.println(compressedAsB64.equals(compressedAsB642));
		
	}
}
