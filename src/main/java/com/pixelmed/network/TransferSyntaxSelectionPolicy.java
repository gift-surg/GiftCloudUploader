/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import com.pixelmed.dicom.TransferSyntax;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * <p>Concrete sub-classes of this abstract class accept or reject Presentation Contexts from a list based on their Transfer Syntax.</p>
 *
 * @see	com.pixelmed.network.UnencapsulatedExplicitTransferSyntaxSelectionPolicy
 *
 * @author	dclunie
 */
public abstract class TransferSyntaxSelectionPolicy {
	
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/TransferSyntaxSelectionPolicy.java,v 1.3 2013/02/01 13:53:20 dclunie Exp $";

	/**
	 * Accept or reject Presentation Contexts, based on TransferSyntax.
	 *
	 * Should be called after Abstract Syntax selection has been performed.
	 *
	 * Should be called before {@link #applyExplicitTransferSyntaxPreferencePolicy(LinkedList,int,int) applyExplicitTransferSyntaxPreferencePolicy()}.
	 *
	 * Does not change the Abstract Syntax.
	 *
	 * @param	presentationContexts	a java.util.LinkedList of {@link PresentationContext PresentationContext} objects,
	 *			each of which contains an Abstract Syntax (SOP Class UID) with one or more Transfer Syntaxes
	 * @param	associationNumber	for debugging messages
	 * @param	debugLevel
	 * @return		the java.util.LinkedList of {@link PresentationContext PresentationContext} objects,
	 *			as supplied but with the Transfer Syntax list culled to the one preferred
	 *			Transfer Syntax (or empty if none acceptable)
	 *			and the result/reason field left alone if one of the Transfer Syntaxes was acceptable, or set to
	 *			"transfer syntaxes not supported (provider rejection)"
	 */
	public abstract LinkedList applyTransferSyntaxSelectionPolicy(LinkedList presentationContexts,int associationNumber,int debugLevel);

	/**
	 * Reject Implicit VR Presentation Contexts when multiple Presentation Contexts are offered for the same Abstract Syntax, if possible.
	 *
	 * Should be called after {@link #applyTransferSyntaxSelectionPolicy(LinkedList,int,int) applyTransferSyntaxSelectionPolicy()}.
	 *
	 * @param	presentationContexts	a java.util.LinkedList of {@link PresentationContext PresentationContext} objects,
	 *			each of which contains a single (accepted) Transfer Syntax
	 * @param	associationNumber	for debugging messages
	 * @param	debugLevel
	 * @return		the java.util.LinkedList of {@link PresentationContext PresentationContext} objects,
	 *			as supplied but with any Presentation Context with an Implicit VR Transfer Syntax
	 *			for which there is another Presentation Context with an Explicit VR Transfer Syntax
	 *			rejected with the result/reason field set to
	 *			"no reason (provider rejection)"
	 */
	public LinkedList applyExplicitTransferSyntaxPreferencePolicy(LinkedList presentationContexts,int associationNumber,int debugLevel) {
//System.err.println("applyExplicitTransferSyntaxPreferencePolicy: start");
		// Objective is to cull list so that we make the choice of
		// explicit over implicit if more than one TS offered and accepted for the same AS
		
		HashSet allAbstractSyntaxesAcceptedWithExplicitVRTransferSyntax = new HashSet();
		
		// Pass 1 - fill allAbstractSyntaxesAcceptedWithExplicitVRTransferSyntax
		
//System.err.println("applyExplicitTransferSyntaxPreferencePolicy: start pass 1");
		ListIterator pcsi = presentationContexts.listIterator();
		while (pcsi.hasNext()) {
//System.err.println("applyExplicitTransferSyntaxPreferencePolicy: iterating");
			PresentationContext pc = (PresentationContext)(pcsi.next());
//System.err.println("applyExplicitTransferSyntaxPreferencePolicy: have pc "+pc);
			String transferSyntaxUID=pc.getTransferSyntaxUID();		// There will only be one by this time
//System.err.println("applyExplicitTransferSyntaxPreferencePolicy: have transferSyntaxUID "+transferSyntaxUID);
			if (transferSyntaxUID != null && TransferSyntax.isExplicitVR(transferSyntaxUID)) {
//System.err.println("applyExplicitTransferSyntaxPreferencePolicy: adding to allAbstractSyntaxesAcceptedWithExplicitVRTransferSyntax: "+pc);
				allAbstractSyntaxesAcceptedWithExplicitVRTransferSyntax.add(pc.getAbstractSyntaxUID());
			}
		}
		
		// Pass 2 - reject any PC with an IVR for an AS that is in allAbstractSyntaxesAcceptedWithExplicitVRTransferSyntax
		
//System.err.println("applyExplicitTransferSyntaxPreferencePolicy: start pass 2");
		pcsi = presentationContexts.listIterator();
		while (pcsi.hasNext()) {
			PresentationContext pc = (PresentationContext)(pcsi.next());
			String transferSyntaxUID=pc.getTransferSyntaxUID();		// There will only be one by this time
			if (transferSyntaxUID != null
			 && TransferSyntax.isImplicitVR(transferSyntaxUID)
			 && allAbstractSyntaxesAcceptedWithExplicitVRTransferSyntax.contains(pc.getAbstractSyntaxUID())) {
//System.err.println("applyExplicitTransferSyntaxPreferencePolicy: rejecting: "+pc);
				pc.setResultReason((byte)2);				// no reason (provider rejection)
			}
		}
//System.err.println("applyExplicitTransferSyntaxPreferencePolicy: done");
		return presentationContexts;
	}
}

