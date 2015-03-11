/* Copyright (c) 2001-2008, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import com.pixelmed.dicom.SOPClass;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * <p>Accept only SOP Classes for storage, query or retrieval of composite instances and verification SOP Classes.</p>
 *
 * @author	dclunie
 */
public class CompositeInstanceStoreFindMoveGetAbstractSyntaxSelectionPolicy implements AbstractSyntaxSelectionPolicy {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/CompositeInstanceStoreFindMoveGetAbstractSyntaxSelectionPolicy.java,v 1.1 2008/09/25 16:35:37 dclunie Exp $";
	
	/**
	 * Accept or reject Abstract Syntaxes (SOP Classes).
	 *
	 * Only SOP Classes for storage, query or retrieval of composite instances and verification SOP Classes are accepted.
	 *
	 * Should be called before Transfer Syntax selection is performed.
	 *
	 * @param	presentationContexts	a java.util.LinkedList of {@link PresentationContext PresentationContext} objects,
	 *			each of which contains an Abstract Syntax (SOP Class UID)
	 * @param	associationNumber	for debugging messages
	 * @param	debugLevel
	 * @return		the java.util.LinkedList of {@link PresentationContext PresentationContext} objects,
	 *			as supplied but with the result/reason field set to either "acceptance" or
	 *			"abstract syntax not supported (provider rejection)"
	 */
	public LinkedList applyAbstractSyntaxSelectionPolicy(LinkedList presentationContexts,int associationNumber,int debugLevel) {
		ListIterator pcsi = presentationContexts.listIterator();
		while (pcsi.hasNext()) {
			PresentationContext pc = (PresentationContext)(pcsi.next());
			String abstractSyntaxUID = pc.getAbstractSyntaxUID();
			pc.setResultReason(
				SOPClass.isImageStorage(abstractSyntaxUID)
			     || SOPClass.isNonImageStorage(abstractSyntaxUID)
			     || SOPClass.isVerification(abstractSyntaxUID)
			     || SOPClass.isCompositeInstanceQuery(abstractSyntaxUID)
			     || SOPClass.isCompositeInstanceRetrieveWithMove(abstractSyntaxUID)
			     || SOPClass.isCompositeInstanceRetrieveWithGet(abstractSyntaxUID)
				? (byte)0 : (byte)3);	// acceptance :  abstract syntax not supported (provider rejection)			      
		}
		return presentationContexts;
	}
}
