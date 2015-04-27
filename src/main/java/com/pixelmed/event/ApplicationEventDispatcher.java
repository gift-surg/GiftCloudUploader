/* Copyright (c) 2001-2006, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.event;

import java.lang.ref.WeakReference;

import java.util.Vector;
import java.util.Iterator;

/**
 * @author      dclunie
 */
public class ApplicationEventDispatcher implements EventDispatcher
{
	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/event/ApplicationEventDispatcher.java,v 1.7 2006/10/23 17:21:51 dclunie Exp $";

	// Thanks to Chris Beckey for rewriting this to use Singleton pattern

	private static ApplicationEventDispatcher applicationEventDispatcher;

	public static synchronized ApplicationEventDispatcher getApplicationEventDispatcher() {
		if( applicationEventDispatcher == null) {
			applicationEventDispatcher = new ApplicationEventDispatcher();
		}
		if (applicationEventDispatcher == null) {
			throw new RuntimeException("Internal error - cannot make new ApplicationEventDispatcher");
		}
		return applicationEventDispatcher;
	}

	private ApplicationEventDispatcher() {}

	/***/
	private Vector listeners = new Vector();		// Vectors are synchronized

	/**
	 * @param	l
	 */
	public void addListener(Listener l) {
		synchronized (listeners) {
			listeners.addElement(new WeakReference(l));
		}
	}

	/**
	 * @param	l
	 */
	public void removeListener(Listener l) {
		synchronized (listeners) {
			Iterator i = listeners.iterator();
			while (i.hasNext()) {
				WeakReference r = (WeakReference)(i.next());
				if (r == null) {
					System.err.println("ApplicationEventDispatcher.removeListener(): tidy up weak reference that was nulled - this should not happen");
					i.remove();			// tidy up weak reference that was nulled during garbage collection, unrelated to this removeListener() request
				}
				else {
					Listener listener = (Listener)(r.get());
					if (listener == null) {
						System.err.println("ApplicationEventDispatcher.removeListener(): tidy up weak reference that was nulled - this should not happen");
						i.remove();		// tidy up weak reference that was nulled during garbage collection, unrelated to this removeListener() request
					}
					else if (listener.equals(l)) {
						i.remove();		// remove the explicitly requested listener
					}
				}
			}
		}
	}

	/**
	 * @param	context		does nothing if null (i.e., does not remove listeners with null context)
	 */
	public void removeAllListenersForEventContext(EventContext context) {
		if (context != null) {
			synchronized (listeners) {
				Iterator i=listeners.iterator();
				while (i.hasNext()) {
					WeakReference r = (WeakReference)(i.next());
					if (r == null) {
						System.err.println("ApplicationEventDispatcher.removeAllListenersForEventContext(): tidy up weak reference that was nulled - this should not happen");
						i.remove();			// tidy up weak reference that was nulled during garbage collection, unrelated to this removeListener() request
					}
					else {
						Listener listener = (Listener)(r.get());
						if (listener == null) {
							System.err.println("ApplicationEventDispatcher.removeAllListenersForEventContext(): tidy up weak reference that was nulled - this should not happen");
							i.remove();			// tidy up weak reference that was nulled during garbage collection, unrelated to this removeListener() request
						}
						else if (listener.getEventContext() == context) {
							i.remove();
						}
					}
				}
			}
		}
	}

	/**
	 * @param	event
	 */
	public synchronized void processEvent(Event event) {
		Class eventClass = event.getClass();
		EventContext eventContext = event.getEventContext();
		synchronized (listeners) {
			Iterator i=listeners.iterator();
			while (i.hasNext()) {
				WeakReference r = (WeakReference)(i.next());
				if (r == null) {
					System.err.println("ApplicationEventDispatcher.processEvent(): tidy up weak reference that was nulled - this should not happen");
					i.remove();			// tidy up weak reference that was nulled during garbage collection, unrelated to this removeListener() request
				}
				else {
					Listener listener = (Listener)(r.get());
					if (listener == null) {
						System.err.println("ApplicationEventDispatcher.processEvent(): tidy up weak reference that was nulled - this should not happen");
						i.remove();			// tidy up weak reference that was nulled during garbage collection, unrelated to this removeListener() request
					}
					else {
						EventContext listenerEventContext = listener.getEventContext();
						if (listener.getClassOfEventHandled() == eventClass	// this is inefficient; should use something hashed by event.getClass() :(
								&& (eventContext == null							// events without context match listeners of any context
								|| listenerEventContext == null					// listeners without context match events of any context
								|| listenerEventContext == eventContext)) {
							listener.changed(event);
						}
					}
				}
			}
		}
	}
}
