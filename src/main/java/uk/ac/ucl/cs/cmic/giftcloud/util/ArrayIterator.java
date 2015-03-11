/*
 * uk.ac.ucl.cs.cmic.giftcloud.util.ArrayIterator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<T> implements Iterator<T> {
	private final T[] ts;
	private int i = 0;
	
	public ArrayIterator(T...ts) {
		this.ts = ts;
	}
	
	public boolean hasNext() {
		return i < ts.length;
	}
	
	public T next() {
		if (i >= ts.length) {
			throw new NoSuchElementException();
		}
		return ts[i++];
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
