package org.bitsofinfo.ehcache.jms.custom.batch;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sf.ehcache.distribution.LegacyEventMessage;
import net.sf.ehcache.distribution.jms.JMSEventMessage;

/**
 * An ehcache "jms" event message that represents
 * a batch of many different individual JMSEventMessages
 * 
 * Given there are really only two categories sent
 * 	- REMOVE_ALL from <cacheName>
 *  - For <cacheName> REMOVE this "key"
 *  
 *  We use this class to summarize N individual events
 *  into one single message. Why the crazy variable names?
 *  Because we will serialize this to JSON and we are trying
 *  to keep all names to a minimum, keeping in mind these batch
 *  messages might be sent to something like AWS/SNS/SQS that has
 *  message size limitations
 * 
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class BatchJMSEventMessage {

    public static final int PUT = LegacyEventMessage.PUT;
    public static final int REMOVE = LegacyEventMessage.REMOVE;
    public static final int REMOVE_ALL = LegacyEventMessage.REMOVE_ALL;
    
    private Date createdAt = new Date();
    
    // cachenames -> action2keys per cache
    private Map<String,Action2Keys> c_a2k = new TreeMap<String,Action2Keys>(String.CASE_INSENSITIVE_ORDER);
    
    // list of REMOVE_ALL cacheNames
    private Set<String> ra_c = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    
    public void addRemovalAllCacheName(String cacheName) {
    	ra_c.add(cacheName);
    }
    
    public static boolean keyIsCompatible(JMSEventMessage jem) {
    	if (jem.getSerializableKey() instanceof String ||
				jem.getSerializableKey() instanceof Integer ||
				jem.getSerializableKey() instanceof Double ||
				jem.getSerializableKey() instanceof Long ||
				jem.getSerializableKey() instanceof Float) {
    		return true;
    	}
    	return false;
    }
    
    public synchronized void addCacheEvent(String cacheName, 
    									   Serializable key, 
    									   Integer cacheAction) throws KeyNotPrimitiveException {
    	
    	Action2Keys action2Keys = c_a2k.get(cacheName);
    	if (action2Keys == null) {
    		action2Keys = new Action2Keys();
    		c_a2k.put(cacheName, action2Keys);
    	}
    	action2Keys.addEvent(cacheAction, key);
    	
    }
    
    public Date getCreatedAt() {
    	return this.createdAt;
    }
    
    public int getTotalEvents() {
    	int total = ra_c.size();
    	
    	for (String key : c_a2k.keySet()) {
    		total += c_a2k.get(key).getTotalEntries();
    	}
    	
    	return total;
    }
    
    public Set<String> getRemoveAllCacheNames() {
    	return ra_c;
    }
    
    public Set<String> getCacheNamesWithEvents() {
    	return c_a2k.keySet();
    }
    
    public Set<Serializable> getRemoveEventsFor(String cacheName) {
    	return c_a2k.get(cacheName).getKeysForAction(REMOVE);	
    }
    

}
