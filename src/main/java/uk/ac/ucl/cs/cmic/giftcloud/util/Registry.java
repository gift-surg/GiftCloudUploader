/*
 * uk.ac.ucl.cs.cmic.giftcloud.util.Registry
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

public interface Registry<T> extends Iterable<T> {
	T get(T template);
	T get(int index);
	int getIndex(T o);
	Collection<T> getAll();
	boolean isEmpty();
	int size();
}
