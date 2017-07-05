package com.ryanc16.utils.json;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
/**
 * A json object that uses key value pairs. Extends {@link JsonEntity} and implements {@link java.util.Map Map} interface.
 * @author Ryan
 */
public class JsonObject extends JsonEntity implements Map<String,Object>{
	private TreeMap<String,Object> map = new TreeMap<String,Object>();
	/**
	 * Generates a stringified representation of this element's contents in json format.
	 * @return The generated json string.
	 */
	public String toJsonString() {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for(String key: keySet()) {
			if(first)
				first = false;
			else sb.append(',');
			sb.append('"'+key+'"');
			sb.append(':');
			classCastAndEscape(key, sb);
		}
		sb.append('}');
		return sb.toString();
	}
	
	private void classCastAndEscape(String key, StringBuilder sb) {
		try {
			sb.append((Boolean)get(key));
			return;
		}
		catch(ClassCastException cce) {}
		try {
			sb.append((Integer)get(key));
			return;
		}
		catch(ClassCastException cce) {}
		try {
			sb.append((Float)get(key));
			return;
		}
		catch(ClassCastException cce) {}
		try {
			sb.append((Double)get(key));
			return;
		}
		catch(ClassCastException cce) {}
		try {
			sb.append('"'+escapeString((String)get(key))+'"');
			return;
		}
		catch(ClassCastException cce) {}
		sb.append(get(key).toString());
	}
	
	private String escapeString(String str) {
		return str.replace("\"", "\\\"");//.replaceAll("'","\'").replaceAll("\\", "\\\\");
	}
	/**
	 * Implicitly calls the {@link #toJsonString} method.
	 */
	@Override
	public String toString() {
		return toJsonString();
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		map.clear();
	}

	@Override
	public boolean containsKey(Object arg0) {
		return map.containsKey(arg0);
	}

	@Override
	public boolean containsValue(Object arg0) {
		return map.containsValue(arg0);
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	@Override
	public Object get(Object arg0) {
		return map.get(arg0);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public Object put(String arg0, Object arg1) {
		return map.put(arg0, arg1);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> arg0) {
		map.putAll(arg0);
	}

	@Override
	public Object remove(Object arg0) {
		return map.remove(arg0);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection<Object> values() {
		return map.values();
	}

}
