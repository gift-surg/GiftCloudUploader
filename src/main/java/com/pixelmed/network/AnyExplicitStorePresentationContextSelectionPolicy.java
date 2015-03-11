/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * <p>Accept only SOP Classes for storage of composite instances and verification SOP Classes
 * with any explicit transfer syntax (whether compressed or not), also rejecting implicit VR
 * transfer syntaxes if an explicit VR transfer syntax is offered for the same abstract syntax.</p>
 *
 * @author	dclunie
 */
public class AnyExplicitStorePresentationContextSelectionPolicy implements PresentationContextSelectionPolicy {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/AnyExplicitStorePresentationContextSelectionPolicy.java,v 1.1 2013/02/01 13:53:20 dclunie Exp $";
	
	protected AbstractSyntaxSelectionPolicy abstractSyntaxSelectionPolicy;
	protected TransferSyntaxSelectionPolicy transferSyntaxSelectionPolicy;
	
	public AnyExplicitStorePresentationContextSelectionPolicy() {
		abstractSyntaxSelectionPolicy = new CompositeInstanceStoreAbstractSyntaxSelectionPolicy();
		transferSyntaxSelectionPolicy = new AnyExplicitTransferSyntaxSelectionPolicy();
	}
	
	/**
	 * Accept or reject Abstract Syntaxes (SOP Classes).
	 *
	 * Only SOP Classes for storage of composite instances and verification SOP Classes are accepted.
	 *
	 * @param	presentationContexts	a java.util.LinkedList of {@link PresentationContext PresentationContext} objects,
	 *			each of which contains an Abstract Syntax (SOP Class UID) with one or more Transfer Syntaxes
	 * @param	associationNumber	for debugging messages
	 * @param	debugLevel
	 * @return		the java.util.LinkedList of {@link PresentationContext PresentationContext} objects,
	 *			as supplied but with the result/reason field set to either "acceptance" or
	 *			"abstract syntax not supported (provider rejection)"
	 */
	public LinkedList applyPresentationContextSelectionPolicy(LinkedList presentationContexts,int associationNumber,int debugLevel) {
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": Association["+associationNumber+"]: Presentation contexts requested:\n"+presentationContexts);
		presentationContexts = abstractSyntaxSelectionPolicy.applyAbstractSyntaxSelectionPolicy(presentationContexts,associationNumber,debugLevel);				// must be called 1st
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": Association["+associationNumber+"]: Presentation contexts after applyAbstractSyntaxSelectionPolicy:\n"+presentationContexts);
		presentationContexts = transferSyntaxSelectionPolicy.applyTransferSyntaxSelectionPolicy(presentationContexts,associationNumber,debugLevel);				// must be called 2nd
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": Association["+associationNumber+"]: Presentation contexts after applyTransferSyntaxSelectionPolicy:\n"+presentationContexts);
		presentationContexts = transferSyntaxSelectionPolicy.applyExplicitTransferSyntaxPreferencePolicy(presentationContexts,associationNumber,debugLevel);	// must be called 3rd
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": Association["+associationNumber+"]: Presentation contexts after applyExplicitTransferSyntaxPreferencePolicy:\n"+presentationContexts);
		return presentationContexts;
	}
}
