package org.bitsofinfo.ehcache.jms.custom.batch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Contains a map of Ehcache cache actions (numeric constant)
 * to a Set of all affected Keys. This only works with 
 * keys that are Java primitives and are typed with a short
 * type qualifier during transport.
 * 
 * Why are we doing this? The first use case for this is using
 * AWS SNS/SQS which has message size limits. Using this notation
 * for primitives is much smaller than actually serializing them...
 * 
 * example
 * 
 * REMOVE (1) -> (s:key1, i:22, l:22323222322232) etc
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class Action2Keys {
	
	public static final String TYPE_STRING = "s:";
	public static final String TYPE_INTEGER = "i:";
	public static final String TYPE_LONG = "l:";
	public static final String TYPE_FLOAT = "f:";
	public static final String TYPE_DOUBLE = "d:";
	
	private Map<Integer,Set<String>> a2k = new HashMap<Integer,Set<String>>();

	public synchronized void addEvent(Integer action, Serializable key) throws KeyNotPrimitiveException {
		
		String type = null;
		if (key instanceof String) {
			type = TYPE_STRING;
		} else if (key instanceof Integer) {
			type = TYPE_INTEGER;
		} else if (key instanceof Long) {
			type = TYPE_LONG;
		} else if (key instanceof Double) {
			type = TYPE_DOUBLE;
		} else if (key instanceof Float) {
			type = TYPE_FLOAT;
		} else {
			throw new KeyNotPrimitiveException();
		}
		
		String typedKey = type+key.toString();
		
		Set<String> keys = a2k.get(action);
		if (keys == null) {
			keys = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			a2k.put(action,keys);
		}
		keys.add(typedKey);
	}
	
	public Set<Integer> getActions() {
		return a2k.keySet();
	}
	
	public int getTotalEntries() {
		int total = 0;
		for (Integer key : a2k.keySet()) {
			total += a2k.get(key).size();
		}
		return total;
	}
	
	public Set<Serializable> getKeysForAction(Integer action) {
		Set<String> typedKeys = a2k.get(action);
		
		Set<Serializable> reconstituted = new HashSet<Serializable>();
		
		for (String typedKey : typedKeys) {
			String qualifier = typedKey.substring(0,2);
			String value = typedKey.substring(2);
			
			if (qualifier.equals(TYPE_STRING)) {
				reconstituted.add(value);
				
			} else if (qualifier.equals(TYPE_INTEGER)) {
				reconstituted.add(Integer.valueOf(value));
				
			} else if (qualifier.equals(TYPE_LONG)) {
				reconstituted.add(Long.valueOf(value));
				
			} else if (qualifier.equals(TYPE_DOUBLE)) {
				reconstituted.add(Double.valueOf(value));
				
			} else if (qualifier.equals(TYPE_FLOAT)) {
				reconstituted.add(Float.valueOf(value));
				
			} else {
				
			}
		}
		
		return reconstituted;
	}

}
