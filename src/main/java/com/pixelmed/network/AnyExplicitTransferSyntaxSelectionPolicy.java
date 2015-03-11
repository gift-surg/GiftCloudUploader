/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import com.pixelmed.dicom.TransferSyntax;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * <p>Accept any explicit transfer syntax (whether compressed or not), also rejecting implicit VR
 * transfer syntaxes if an explicit VR transfer syntax is offered for the same abstract syntax.</p>
 *
 * @author	dclunie
 */
public class AnyExplicitTransferSyntaxSelectionPolicy extends TransferSyntaxSelectionPolicy {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/AnyExplicitTransferSyntaxSelectionPolicy.java,v 1.1 2013/02/01 13:53:20 dclunie Exp $";
	
	/**
	 * Accept or reject Presentation Contexts, preferring Explicit over Implicit VR.
	 *
	 * Should be called after Abstract Syntax selection has been performed.
	 *
	 * Should be called before {@link com.pixelmed.network.TransferSyntaxSelectionPolicy#applyExplicitTransferSyntaxPreferencePolicy(LinkedList,int,int) applyExplicitTransferSyntaxPreferencePolicy()}.
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
	public LinkedList applyTransferSyntaxSelectionPolicy(LinkedList presentationContexts,int associationNumber,int debugLevel) {
		ListIterator pcsi = presentationContexts.listIterator();
		while (pcsi.hasNext()) {
			PresentationContext pc = (PresentationContext)(pcsi.next());
			boolean foundImplicitVRLittleEndian = false;
			List tsuids = pc.getTransferSyntaxUIDs();
			// discard old list and make a new one ...
			pc.newTransferSyntaxUIDs();
			boolean addedOne=false;
			ListIterator tsuidsi = tsuids.listIterator();
			while (tsuidsi.hasNext()) {
				String transferSyntaxUID=(String)(tsuidsi.next());
				if (transferSyntaxUID != null) {
					if (transferSyntaxUID.equals(TransferSyntax.ImplicitVRLittleEndian)) {
						foundImplicitVRLittleEndian = true;
					}
					else {
						TransferSyntax ts = new TransferSyntax(transferSyntaxUID);
						if (ts.isRecognized() && ts.isExplicitVR()) {
							pc.addTransferSyntaxUID(transferSyntaxUID);
							addedOne=true;
						}
					}
				}
			}
			if (!addedOne) {
				if (foundImplicitVRLittleEndian) {
					pc.addTransferSyntaxUID(TransferSyntax.ImplicitVRLittleEndian);
				}
				else {
					pc.setResultReason((byte)4);				// transfer syntaxes not supported (provider rejection)
				}
			}
		}
		return presentationContexts;
	}
}
