package org.bitsofinfo.ehcache.jms.custom;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.CacheReplicator;

/**
 * Implementation CacheReplicator<-CacheEventListener who's purpose
 * is to proxy another CacheReplicator and intercept Ehcache actions
 * and translate them to a different action.
 * 
 * I.E. using this you can intercept and Ehcache event that occurs
 * locally (PUT) and translate it to a (REMOVE) which is what would
 * be replicated via the JMS replication. 
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class OverridableActionCacheReplicator implements CacheReplicator {
	
	private static final Logger LOG = Logger.getLogger(OverridableActionCacheReplicator.class.getName());
	
	public static final String ACTION_PUT = "put";
	public static final String ACTION_REMOVE = "remove";
	public static final String ACTION_UPDATE = "update";
	public static final String ACTION_EXPIRE = "expire";
	public static final String ACTION_EVICT = "evict";
	public static final String ACTION_REMOVE_ALL = "removeAll";
	public static final String ACTION_NOTHING = "nothing";
	
	private CacheReplicator proxiedReplicator = null;
	private TreeMap<String,String> actionOverrideMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	
	public OverridableActionCacheReplicator(CacheReplicator replicator, Map<String,String> overrideMap) {
		this.proxiedReplicator = replicator;
		
		if (overrideMap != null) {
			for (String key : overrideMap.keySet()) {
				actionOverrideMap.put(key, overrideMap.get(key));
			}
		}
	}

	public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
		handleNotification(ACTION_REMOVE, cache, element);
	}

	public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
		handleNotification(ACTION_PUT, cache, element);
	}

	public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
		handleNotification(ACTION_UPDATE, cache, element);
	}

	public void notifyElementExpired(Ehcache cache, Element element) {
		handleNotification(ACTION_EXPIRE, cache, element);
	}

	public void notifyElementEvicted(Ehcache cache, Element element) {
		handleNotification(ACTION_EVICT, cache, element);
	}

	public void notifyRemoveAll(Ehcache cache) {
		handleNotification(ACTION_REMOVE_ALL, cache, null);
	}

	public void dispose() {
		proxiedReplicator.dispose();
	}

	public boolean isReplicateUpdatesViaCopy() {
		return proxiedReplicator.isReplicateUpdatesViaCopy();
	}

	public boolean notAlive() {
		return proxiedReplicator.notAlive();
	}

	public boolean alive() {
		return proxiedReplicator.alive();
	}
	
	private void handleNotification(String notifiedAction, Ehcache cache, Element element) throws CacheException {
		String notifiedActionOverride = actionOverrideMap.get(notifiedAction);
		if (notifiedActionOverride != null && notifiedActionOverride.trim().length() > 0) {
			LOG.info("handleNotification() received notifiedAction of: " + notifiedAction + 
					", we are OVERRIDING replicator action to: " + notifiedActionOverride);
			notifiedAction = notifiedActionOverride;
		}
		
		if (notifiedAction.equalsIgnoreCase(ACTION_PUT)) {
			proxiedReplicator.notifyElementPut(cache, element);
			
		} else if (notifiedAction.equalsIgnoreCase(ACTION_REMOVE)) {
			proxiedReplicator.notifyElementRemoved(cache, element);

		} else if (notifiedAction.equalsIgnoreCase(ACTION_EVICT)) {
			proxiedReplicator.notifyElementEvicted(cache, element);
			
		} else if (notifiedAction.equalsIgnoreCase(ACTION_EXPIRE)) {
			proxiedReplicator.notifyElementExpired(cache, element);
			
		} else if (notifiedAction.equalsIgnoreCase(ACTION_UPDATE)) {
			proxiedReplicator.notifyElementUpdated(cache, element);
			
		} else if (notifiedAction.equalsIgnoreCase(ACTION_REMOVE_ALL)) {
			proxiedReplicator.notifyRemoveAll(cache);
			
		} else if (notifiedAction.equalsIgnoreCase(ACTION_NOTHING)) {
			// do nothing!
			
		} else {
			throw new CacheException("OverridableActionCacheReplicator " +
					"INVALID notifiedActionOverride: " + notifiedAction);
		}
	}
	
	@Override
	 public Object clone() throws CloneNotSupportedException {
	       super.clone();
	       return new OverridableActionCacheReplicator((CacheReplicator)proxiedReplicator.clone(), actionOverrideMap);
	 }


}
