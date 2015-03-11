/*
 * uk.ac.ucl.cs.cmic.giftcloud.util.MapRegistry
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MapRegistry<T> implements Registry<T> {
	private final Map<T,T> map;
	
	public MapRegistry(final Map<T,T> map) { this.map = map; }
	
	public MapRegistry() { this(new LinkedHashMap<T,T>()); }
	
	public T get(final T template) {
		synchronized (map) {
			final T t = map.get(template);
			if (null == t) {
				map.put(template, template);
				return template;
			} else {
				return t;
			}
		}
	}
	
	public T get(final int i) {
		synchronized (map) {
			final Iterator<T> it = map.values().iterator();
			for (int ii = 0; ii < i; ii++) {
				it.next();
			}
			return it.next();
		}
	}
	
	public int getIndex(final Object o) {
		synchronized (map) {
			final Iterator<T> it = map.values().iterator();
			for (int i = 0; it.hasNext(); i++) {
				if (it.next().equals(o)) {
					return i;
				}
			}
			return -1;
		}
	}
	
	public Collection<T> getAll() {
		return Collections.unmodifiableCollection(map.values());
	}
	
	public boolean isEmpty() { return map.isEmpty(); }
	
	public Iterator<T> iterator() { return map.values().iterator(); }
	
	public int size() { return map.size(); }
}
