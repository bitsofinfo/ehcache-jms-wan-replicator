package org.bitsofinfo.ehcache.jms.custom;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.distribution.CacheReplicator;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

/**
 * Implementation CacheEventListenerFactory who's purpose
 * is to return a OverridableActionCacheReplicator that proxies another CacheReplicator 
 * to intercept Ehcache actions and translate them to a different action.
 * 
 * I.E. using this you can intercept and Ehcache event that occurs
 * locally (PUT) and translate it to a (REMOVE) which is what would
 * be replicated via the JMS replication. 
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class OverridableActionCacheReplicatorFactory extends CacheEventListenerFactory {

	@Override
	public CacheEventListener createCacheEventListener(Properties properties) {
		
		/**
		 * @see OverridableActionCacheReplicator constants for possible actions
		 * 
		 * factoryClass = net.sf.ehcache.distribution.jms.JMSCacheReplicatorFactory
		 * actionOverrideMap = put->remove,update->remove,evict->nothing
		 */
		
		CacheEventListenerFactory factory = null;
		
		try {
			factory = (CacheEventListenerFactory)
					ClassLoading.loadClass(
							properties.getProperty("factoryClass")).newInstance();
		} catch(Throwable e) {
			throw new CacheException("OverridableActionCacheReplicatorFactory" +
					".createCacheEventListener() error: " + e.getMessage(),e);
		}
		
		Map<String,String> overrideMap = expandDotNotationProperty(properties, "actionOverrideMap");
		CacheReplicator cacheReplicatorToProxy = (CacheReplicator)factory.createCacheEventListener(properties);
		return new OverridableActionCacheReplicator(cacheReplicatorToProxy, overrideMap);
		
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

}
