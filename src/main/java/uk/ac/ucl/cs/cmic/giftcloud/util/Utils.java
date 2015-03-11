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

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public final class Utils {
	private Utils() {}	// prevent instantiation

	/**
	 * Appends the contents of an InputStream to a StringBuffer
	 * @param sb StringBuffer to which data will be appended
	 * @param in InputStream from which data will be read
	 * @return the provided StringBuffer
	 * @throws IOException from the underlying InputStream::read(byte[])
	 */
	public static StringBuilder slurp(final StringBuilder sb, final InputStream in) throws IOException {
		final byte[] buffer = new byte[4096];
		int nread;
		while (0 < (nread = in.read(buffer))) {
			sb.append(new String(buffer, 0, nread));
		}
		return sb;
	}

	/**
	 * Copies the contents of an entire InputStream to a String.
	 * @param in InputStream from which data will be read
	 * @return String representation of the InputStream contents
	 * @throws IOException from the underlying InputStream::read(byte[])
	 */
	public static String slurp(final InputStream in) throws IOException {
		return slurp(new StringBuilder(), in).toString();
	}

	/**
	 * Exception-safe method for getting a canonical, or at least absolute, path
	 * @param f
	 * @return Canonical file if available; otherwise, absolute file.
	 */
	public static File getCanonicalFile(final File f) {
		try {
			return f.getCanonicalFile();
		} catch (IOException e) {
			return f.getAbsoluteFile();
		}
	}

	private static final class LongFactorIterator implements Iterator<Long> {
		private final long scale;
		private Long value;

		LongFactorIterator(final long start, final long scale) {
			value = start;
			this.scale = scale;
		}

		public boolean hasNext() { return null != value; }

		public Long next() {
			if (null == value) {
				throw new NoSuchElementException();
			}
			final long r = value;
			if (value > Long.MAX_VALUE / scale || value < Long.MIN_VALUE / scale) {
				// guard against overflow/underflow
				value = null;
			} else {
				value *= scale;
			}
			return r;
		}

		public void remove() { throw new UnsupportedOperationException(); }
	}

	private static final SortedMap<Long,String> prefixes;
	static {
		final SortedMap<Long,String> m = Maps.newTreeMap();
		// No point in going beyond peta because the long range runs out
		final LongFactorIterator i = new LongFactorIterator(1, 1024);
		for (final String prefix : new String[] { "", "k", "M", "G", "T", "P"}) {
			m.put(i.next(), prefix);
		}
		prefixes = ImmutableSortedMap.copyOf(m);
	}

	private static Map.Entry<Long,String> getPrefixFactor(final long value) {
		final Iterator<Map.Entry<Long,String>> mei = prefixes.entrySet().iterator();
		Map.Entry<Long,String> last = mei.next();
		while (mei.hasNext()) {
			final Map.Entry<Long,String> current = mei.next();
			final long f = current.getKey();
			if (value >= 0.9 * f) {
				last = current;
			} else {
				break;
			}
		}	
		return last;
	}

	public static StringBuilder showNearestUnitFraction(final StringBuilder sb,
			final long v0, final long v1,
			final String units, final String numberFormat) {
		final Map.Entry<Long,String> pf = getPrefixFactor(v1);
		final long factor = pf.getKey();
		final String prefix = pf.getValue();

		float small = (float)v0/factor;
		if (Math.abs(small) < 0.01) {
			// if v0 is so small compared to v1 that it would show as scientific
			// notation, just pretend that it's zero.
			small = 0;
		}
		final Formatter formatter = new Formatter(sb);
		formatter.format(numberFormat, small);
		sb.append("/");
		formatter.format(numberFormat, (float)v1/factor);
		sb.append(" ").append(prefix).append(units);
		return sb;
	}

	public static StringBuilder showNearestUnitFraction(final StringBuilder sb,
			final long v0, final long v1,
			final String units) {
		return showNearestUnitFraction(sb, v0, v1, units, "%.3g");
	}

	public static StringBuilder showNearestUnits(final StringBuilder sb,
			final long value, final String units, final String numberFormat) {
		final Map.Entry<Long,String> pf = getPrefixFactor(value);
		final long factor = pf.getKey();
		final String prefix = pf.getValue();

		final Formatter formatter = new Formatter(sb);
		formatter.format(numberFormat, (float)value/factor);
		sb.append(" ").append(prefix).append(units);
		return sb;
	}

	public static StringBuilder showNearestUnits(final StringBuilder sb, final long value, final String units) {
		return showNearestUnits(sb, value, units, "%.3g");
	}

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

	public static void initColumnSizes(final JTable table) {
		final TableModel model = table.getModel();
		final JTableHeader header = table.getTableHeader();
		final TableCellRenderer hr = header.getDefaultRenderer();
		for (int i = 0; i < model.getColumnCount(); i++) {
			final TableColumn column = table.getColumnModel().getColumn(i);
			final TableCellRenderer chr = column.getHeaderRenderer();
			int w = (null == chr ? hr : chr).getTableCellRendererComponent(table,
					column.getHeaderValue(), false, false, 0, i).getPreferredSize().width;
			for (int j = 0; j < model.getRowCount(); j++) {
				final TableCellRenderer r = table.getCellRenderer(j, i);
				w = Math.max(w, r.getTableCellRendererComponent(table,
						model.getValueAt(j, i), false, false, j, i).getPreferredSize().width);
			}
			column.setPreferredWidth(w + 4);
		}
	}
}
