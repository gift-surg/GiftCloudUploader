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

import com.google.common.collect.Lists;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.api.wizard.WizardResultReceiver;
import org.netbeans.spi.wizard.WizardPage;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerHelper;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionParams;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MultiUploadWizard implements WizardResultReceiver {
    private MultiUploadReporter reporter;
    private String giftCloudUrl;

    public MultiUploadWizard(final GiftCloudServer server, final RestServerHelper restServerHelper, final Dimension dimension, final MultiUploadParameters multiUploadParameters, final String giftCloudUrl, final MultiUploadReporter reporter) throws InvocationTargetException, InterruptedException, ExecutionException {
        this.giftCloudUrl = giftCloudUrl;
        final UploadSelector uploadSelector = new UploadSelector(restServerHelper, multiUploadParameters, reporter);

        this.reporter = reporter;
        final Optional<String> projectName = multiUploadParameters.getProjectName();
        final SessionParams params = SessionParams.fromRestServer(multiUploadParameters, uploadSelector, reporter);

        final java.util.List<WizardPage> pages = Lists.newArrayList();

        if (!uploadSelector.isProjectSet()) {
            pages.add(EDTHelper.createSelectProjectPage(dimension, uploadSelector, reporter));
        }

        if (!uploadSelector.isSubjectSet()) {
            pages.add(EDTHelper.createSelectSubjectPage(restServerHelper, dimension, uploadSelector, reporter));
        }

        if (!uploadSelector.getDateFromSession() && !uploadSelector.isDateSet()) {
            pages.add(EDTHelper.createConfirmSessionDatePage(uploadSelector));
        }

        // Creation of a JFileChooser has to be done on the EDT
        final FileSelector fileSelector = EDTHelper.createFileSelectorUsingJFileChooser();
        pages.add(EDTHelper.createSelectFilesPage(fileSelector, uploadSelector));
        pages.add(EDTHelper.createSelectSessionPage(server, projectName, fileSelector, uploadSelector));
        pages.add(EDTHelper.createAssignSessionVariablesPage(uploadSelector));

        restServerHelper.resetCancellation();

        final WizardDisplayer displayer = EDTHelper.createAndInstallWizard(server, pages, reporter.getContainer(), params.getParams(), this, reporter);
        displayer.setCloseHandler(closeHandler);
    }

    /**
     * Creates a listener to handle the closing of the applet. This allows for a clean exit.
     */
    private transient final ActionListener closeHandler = new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            try {
                reporter.loadWebPage(giftCloudUrl);
            } catch (MalformedURLException mue) {
                reporter.exit();
            }
        }
    };

    /**
     * Implementation of the {@link org.netbeans.api.wizard.WizardResultReceiver#cancelled(java.util.Map)} method.
     *
     * @see org.netbeans.api.wizard.WizardResultReceiver#cancelled(java.util.Map)
     */
    @Override
    public void cancelled(@SuppressWarnings("rawtypes") Map arg0) {
        try {
            reporter.loadWebPage(giftCloudUrl);
        } catch (MalformedURLException mue) {
            reporter.exit();
        }
    }

    /**
     * Implementation of the {@link org.netbeans.api.wizard.WizardResultReceiver#finished(Object)} method.
     *
     * @see org.netbeans.api.wizard.WizardResultReceiver#finished(Object)
     */
    @Override
    public void finished(Object arg0) {
    }
}
