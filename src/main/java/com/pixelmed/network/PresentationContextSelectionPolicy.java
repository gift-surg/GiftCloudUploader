/* Copyright (c) 2001-2008, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * <p>Implementations of this interface accept or reject Presentation Contexts from a list based on the proposed combinations of Abstract Syntax and Transfer Syntax.</p>
 *
 * @see	com.pixelmed.network.UnencapsulatedExplicitStoreFindMoveGetPresentationContextSelectionPolicy
 *
 * @author	dclunie
 */
public interface PresentationContextSelectionPolicy {

	/**
	 * Accept or reject Presentation Contexts.
	 *
	 * @param	presentationContexts	a java.util.LinkedList of {@link PresentationContext PresentationContext} objects,
	 *			each of which contains an Abstract Syntax (SOP Class UID) with one or more Transfer Syntaxes
	 * @param	associationNumber	for debugging messages
	 * @param	debugLevel
	 * @return		the java.util.LinkedList of {@link PresentationContext PresentationContext} objects,
	 *			as supplied but with the result/reason field set to either "acceptance" or
	 *			"abstract syntax not supported (provider rejection)" or
	 *			"transfer syntaxes not supported (provider rejection)" or
	 *			" no reason (provider rejection)"
	 */
	public LinkedList applyPresentationContextSelectionPolicy(LinkedList presentationContexts,int associationNumber,int debugLevel);
}

