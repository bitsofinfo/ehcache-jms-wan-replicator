package org.bitsofnfo.ehcache.jms.custom;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Unit test that simulates 3 "dcs", each runs its own
 * cache manager instance w/ 3 caches initially containing TEST_CACHE_TOTAL_ELEMENTS
 * elements. Each cache manager is connected to JMS/Nevado @ AWS 
 * 
 * Once they are all up, DCA updates elements in 2 of 
 * the caches, and removes all elements from one of the caches.
 * 
 * After waiting for a period of time, very verify that those affected
 * elements in both caches in DCB/DCC are removed as expected 
 * 
 * @see src/test/resources/unit-test-ehcache.xml
 * @see src/test/resources/unit-test-bootstrap.xml
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class EhcacheNevadoJMSTest {

	private static final int TEST_CACHE_TOTAL_ELEMENTS = 5000;

	@Test
	public void testEhcacheJMSWanReplicator() {

		// construct, on construction each instance will 
		// have its initial cache populated and all will match
		EhcacheInstance dcA = new EhcacheInstance("dc1");
		EhcacheInstance dcB = new EhcacheInstance("dc2");
		EhcacheInstance dcC = new EhcacheInstance("dc3");
		
		List[] updatedKeys = dcA.updateCacheElements();
		
		dcB.validateRemovedElements(updatedKeys[0], updatedKeys[1]);
		dcC.validateRemovedElements(updatedKeys[0], updatedKeys[1]);
		
	}
	
	public class EhcacheInstance {
		
		private CacheManager cacheManager = null;
		private Cache testCache1 = null;
		private Cache testCache2 = null;
		private Cache testCache3 = null;
		
		/**
		 * Populates 3 caches. Each with TEST_CACHE_TOTAL_ELEMENTS 
		 * simple key_n -> value_n elements
		 * 
		 * @param datacenterId
		 */
		public EhcacheInstance(String datacenterId) {	
			
			System.setProperty("datacenterID", datacenterId);
			System.setProperty("cacheManagerName", datacenterId);
			ApplicationContext context = new ClassPathXmlApplicationContext("/unit-test-bootstrap.xml");
			
			this.cacheManager = (CacheManager)context.getBean("cacheManager");
			
			this.testCache1 = cacheManager.getCache("testCache1");
			this.testCache2 = cacheManager.getCache("testCache2");
			this.testCache3 = cacheManager.getCache("testCache3");
			
			for (int i=0; i < TEST_CACHE_TOTAL_ELEMENTS; i++) {
				int idx = i+1;
				testCache1.put(new Element("key_"+idx, "value_"+idx));
				testCache2.put(new Element("key_"+idx, "value_"+idx));
				testCache3.put(new Element("key_"+idx, "value_"+idx));
			}
		}
		
		public void validateRemovedElements(List<String> cache1KeysToCheck, List<String> cache2KeysToCheck) {
			
			for (String keyToCheck : cache1KeysToCheck) {
				Assert.assertNull(testCache1.get(keyToCheck));
			}
			for (String keyToCheck : cache2KeysToCheck) {
				Assert.assertNull(testCache2.get(keyToCheck));
			}
			
			Assert.assertTrue(testCache3.getSize() == 0);
		}
		
		/**
		 * Updates all even keys in cache1 and all odd keys in cache2.
		 * 
		 * Removes all elements from cache3
		 * 
		 * @return
		 */
		public List<String>[] updateCacheElements() {
			
			ArrayList<String> updatedCache1Keys = new ArrayList<String>();
			ArrayList<String> updatedCache2Keys = new ArrayList<String>();
			
			for (int i=0; i < TEST_CACHE_TOTAL_ELEMENTS; i++) {

				int idx = i+1;
				
				if ( i % 2 == 0 ) {
					testCache1.put(new Element("key_"+idx, "value_updated_"+idx));
					updatedCache1Keys.add("key_"+idx);
					
				} else {
					testCache2.put(new Element("key_"+idx, "value_updated_"+idx));
					updatedCache2Keys.add("key_"+idx);
				}
				
			}
			
			testCache3.removeAll();
			
			List[] toReturn = new List[2]; 
			
			toReturn[0] = updatedCache1Keys;
			toReturn[1] = updatedCache2Keys;
			
			return toReturn;
		}
		
	}
	
	
	

}
