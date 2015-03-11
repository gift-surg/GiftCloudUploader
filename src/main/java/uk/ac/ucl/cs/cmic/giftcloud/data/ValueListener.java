/*
 * ValueListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

public interface ValueListener {
    void hasChanged(SessionVariable variable);
    void isInvalid(SessionVariable variable, Object value, String message);
}
