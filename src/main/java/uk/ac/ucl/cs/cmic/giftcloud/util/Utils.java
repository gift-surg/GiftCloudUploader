/*
 * uk.ac.ucl.cs.cmic.giftcloud.util.Utils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.util;

import com.google.common.collect.*;

import java.util.*;


public final class Utils {
	private Utils() {}	// prevent instantiation

	/**
	 * From a Multimap<K,V> M, generates a Multimap<T,V> M',
	 * where each key of M' is:
	 *  - a key from M (of type K), if K is a CharSequence or if
	 *    there there are <= maxInstances keys of type K in M, or
	 *  - K.class otherwise;
	 * and the corresponding values in M' are V's, either the
	 * original values from M (if the key in M' is a key from M),
	 * or the union of all values with a key of type K.class from M.
	 * @param m Multimap from keys (type K) to values (type V)
	 * @param maxInstances maximum instances for non-Stringy keys
	 * @return consolidated Multimap
	 */
	public static <J,T> Multimap<Object,T>
	consolidateKeys(final Multimap<J,T> m, final int maxInstances) {
		final Set<J> remainingKeys = Sets.newLinkedHashSet(m.keySet());
		final Multimap<Object,T> consolidated = LinkedHashMultimap.create();
		while (!remainingKeys.isEmpty()) {
			final List<J> matchingKeys = Lists.newArrayList();
			final Iterator<J> ri = remainingKeys.iterator();
			final J leadKey = ri.next();
			matchingKeys.add(leadKey);
			final Class<?> keyclass = leadKey.getClass();
			ri.remove();
			while (ri.hasNext()) {
				final J key = ri.next();
				if (keyclass.equals(key.getClass())) {
					matchingKeys.add(key);
					ri.remove();
				}
			}
			if (leadKey instanceof CharSequence || matchingKeys.size() <= maxInstances) {
				for (final J key : matchingKeys) {
					consolidated.putAll(key, m.get(key));
				}
			} else {
				for (final J key : matchingKeys) {
					consolidated.putAll(keyclass, m.get(key));
				}
			}
		}
		return consolidated;
	}

}
