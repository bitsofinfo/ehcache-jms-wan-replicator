package org.bitsofnfo.ehcache.jms.custom;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Not really a test, but just a little harness program you can use
 * to bootstrap multiple instances of CacheManager and see puts/remove
 * events be replicated to other peers (that you are running in separate
 * debugging sessions).
 * 
 * You should run this with -DdatacenterID=someName
 * 
 * @see src/test/resources/ehcache.xml
 * @see src/test/resources/bootstrap.xml
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class EhcacheNevadoJMSTest {

	
	/**
	 * IMPORTANT! For each instance of this you run, you will want to adjust the RMI
	 * local port it listens on (the listener) (or just gut the RMI replicator configs
	 * from the ehcache config file)
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		ApplicationContext context = new ClassPathXmlApplicationContext("/bootstrap.xml");
		
		CacheManager cacheManager = (CacheManager)context.getBean("cacheManager");
		
		Cache testCache =cacheManager.getCache("testCache");

		Element key1 = testCache.get("key1");
		Element key2 = testCache.get("key2");
		key1 = testCache.get("key1");
		
		testCache.put(new Element("key1", "value1"));
		testCache.put(new Element("key2", "value2"));
		testCache.remove("key1");
		
		System.exit(0);
	}

}
