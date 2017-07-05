package com.ryanc16.utils.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
/**
 * A json array. Extends {@link JsonEntity} and implements {@link java.util.List} interface.
 * @author Ryan
 */
public class JsonArray extends JsonEntity implements List<Object>{
	private ArrayList<Object> list = new ArrayList<Object>();
	/**
	 * Generates a stringified representation of this element's contents in json format.
	 * @return The generated json string.
	 */
	public String toJsonString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		boolean first = true;
		for(int i=0;i<size();i++) {
			if(first)
				first = false;
			else sb.append(',');
			classCastAndEscape(i, sb);
		}
		sb.append(']');
		return sb.toString();
	}
	/**
	 * Implicitly calls the {@link #toJsonString} method.
	 */
	@Override
	public String toString() {
		return toJsonString();
	}
	
	private void classCastAndEscape(int index, StringBuilder sb) {
		try {
			sb.append((Boolean)get(index));
			return;
		}
		catch(ClassCastException cce) {}
		try {
			sb.append((Integer)get(index));
			return;
		}
		catch(ClassCastException cce) {}
		try {
			sb.append((Float)get(index));
			return;
		}
		catch(ClassCastException cce) {}
		try {
			sb.append((Double)get(index));
			return;
		}
		catch(ClassCastException cce) {}
		try {
			sb.append('"'+escapeString((String)get(index))+'"');
			return;
		}
		catch(ClassCastException cce) {}
		sb.append(get(index).toString());
	}

	private String escapeString(String str) {
		return str.replace("\"","\\\"");
	}
	
	@Override
	public boolean add(Object arg0) {
		return list.add(arg0);
	}

	@Override
	public void add(int arg0, Object arg1) {
		list.add(arg0,arg1);
	}

	@Override
	public boolean addAll(Collection<? extends Object> arg0) {
		return list.addAll(arg0);
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends Object> arg1) {
		return addAll(arg0,arg1);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public boolean contains(Object arg0) {
		return list.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return list.containsAll(arg0);
	}

	@Override
	public Object get(int arg0) {
		return list.get(arg0);
	}

	@Override
	public int indexOf(Object arg0) {
		return list.indexOf(arg0);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}

	@Override
	public int lastIndexOf(Object arg0) {
		return list.lastIndexOf(arg0);
	}

	@Override
	public ListIterator<Object> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<Object> listIterator(int arg0) {
		return list.listIterator(arg0);
	}

	@Override
	public boolean remove(Object arg0) {
		return list.remove(arg0);
	}

	@Override
	public Object remove(int arg0) {
		return list.remove(arg0);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		return list.removeAll(arg0);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		return list.retainAll(arg0);
	}

	@Override
	public Object set(int arg0, Object arg1) {
		return list.set(arg0, arg1);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List<Object> subList(int arg0, int arg1) {
		return list.subList(arg0, arg1);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return list.toArray(arg0);
	}
	
}
