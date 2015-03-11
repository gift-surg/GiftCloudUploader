/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    Released under the Simplified BSD.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import org.netbeans.spi.wizard.WizardPage;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.FileSelector;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.UploadSelector;

public class SelectFilesPage extends WizardPage {
	private static final long serialVersionUID = 1L;

	private static final String STEP_DESCRIPTION = "Select files";
	private static final String LONG_DESCRIPTION = "Select the directory containing the session to be uploaded";
	private final FileSelector fileSelector;
	private UploadSelector uploadSelector;

	public static String getDescription() {
		return STEP_DESCRIPTION; 
	}
	
	public SelectFilesPage(final FileSelector fileSelector, final UploadSelector uploadSelector) {
		this.fileSelector = fileSelector;
		this.uploadSelector = uploadSelector;
		setLongDescription(LONG_DESCRIPTION);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.netbeans.spi.wizard.WizardPage#recycle()
	 */
	public void recycle() {
		removeAll();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.netbeans.spi.wizard.WizardPage#renderingPage()
	 */
	public void renderingPage() {
		fileSelector.addToContainer(this);
	}

}
