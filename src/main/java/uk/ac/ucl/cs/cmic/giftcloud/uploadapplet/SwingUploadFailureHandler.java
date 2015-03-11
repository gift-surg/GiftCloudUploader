/*
 * SwingUploadFailureHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import uk.ac.ucl.cs.cmic.giftcloud.data.UploadFailureHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Map;

public class SwingUploadFailureHandler implements UploadFailureHandler {
	private static final int DEFAULT_MAX_TRIES = 3;
	public static final String DIALOG_NAME = "Upload error";
	private static final Object[] DIALOG_OPTIONS = new Object[] {
		"Try again",
		"Stop uploading this session"
	};
	private static final Object DEFAULT_DIALOG_OPTION = DIALOG_OPTIONS[0];
	private static final Map<Integer,Boolean> actions = ImmutableMap.of(
			JOptionPane.YES_OPTION, true,
			JOptionPane.NO_OPTION, false
	);
	private static final String NL = System.getProperty("line.separator");

	private final Logger logger = LoggerFactory.getLogger(SwingUploadFailureHandler.class);
	private final Component parent;
	private final int maxTries;
	private final Multimap<Object,Object> failures = LinkedListMultimap.create();
	// Keep one instance of each exception class
	private final Map<Class<? extends Throwable>,Throwable> throwables = Maps.newLinkedHashMap();


	SwingUploadFailureHandler(final Component parent, final int maxTries) {
		this.parent = parent;
		this.maxTries = maxTries;
	}

	SwingUploadFailureHandler(final Component parent) {
		this(parent, DEFAULT_MAX_TRIES);
	}

	public SwingUploadFailureHandler() { this(null, DEFAULT_MAX_TRIES); }

	public boolean shouldRetry(final Object item, final Object cause) {
		if (failures.size() < maxTries) {
			insertFailure(item, cause);
			return true;
		} else {
			final StringBuilder message = new StringBuilder();
			message.append("There was a problem uploading ").append(item);
			message.append(NL).append("(").append(cause).append(")");
			message.append(NL).append("and we've already tried a total of ");
			message.append(maxTries).append(" resend");
			if (failures.size() != 1) {
				message.append("s");
			}
			message.append(":").append(NL);
			appendFailuresDescription(message, failures);
			message.append("Should we try resending ").append(item);
			message.append("?");
			insertFailure(item, cause);
			return actions.get(JOptionPane.showOptionDialog(parent,
					message,
					DIALOG_NAME,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.ERROR_MESSAGE,
					null,
					DIALOG_OPTIONS,
					DEFAULT_DIALOG_OPTION));
		}
	}

	private void insertFailure(final Object item, final Object cause) {
		if (cause instanceof Throwable) {
			final Throwable t = (Throwable)cause;
			final Class<? extends Throwable> tclass = t.getClass();
			failures.put(tclass, item);
			throwables.put(tclass, t);
			logger.debug("unable to upload " + item, t);
		} else {
			logger.debug("unable to upload {}: {}", item, cause);
			failures.put(cause, item);
		}		
	}

	private StringBuilder appendFailuresDescription(final StringBuilder sb,
			final Multimap<Object,?> failures) {
		for (final Object cause : failures.keySet()) {
			if (cause instanceof Class<?>) {
				final Class<?> clazz = (Class<?>)cause;
				sb.append(clazz.getSimpleName());
				final Throwable instance = throwables.get(clazz);
				sb.append(" (").append(instance.getLocalizedMessage());
			} else {
				sb.append(cause);
			}
			sb.append(": ");
			final Collection<?> objects = failures.get(cause);
			switch (objects.size()) {
			case 1:
				sb.append(objects.iterator().next());
				break;

				// TODO: enumerate small numbers?
			default:
				sb.append(objects.size()).append(" items, including ");
				sb.append(objects.iterator().next());
				break;
			}
			sb.append(NL);
		}
		return sb;
	}

	public final String getButtonTextFor(final boolean shouldRetry) {
		return DIALOG_OPTIONS[shouldRetry? 0 : 1].toString();
	}
}
