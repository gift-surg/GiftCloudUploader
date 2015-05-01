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

=============================================================================*/package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.api.wizard.WizardResultReceiver;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServer;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

public class EDTHelper {

    interface RunnableReturner<T> extends Runnable, Returner<T> {
    }

    interface Returner<T> {
        T getValue();
    }

    public static SelectProjectPage createSelectProjectPage(final Dimension dimension, final UploadSelector uploadSelector, final GiftCloudReporter reporter) throws InvocationTargetException, InterruptedException {

        final RunnableReturner<SelectProjectPage> returner =
                new RunnableReturner<SelectProjectPage>() {
                    private SelectProjectPage result;

                    public void run() {
                        try {
                            result = new SelectProjectPage(dimension, uploadSelector, reporter);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }

                    public SelectProjectPage getValue() {
                        return result;
                    }
                };
        SwingUtilities.invokeAndWait(returner);

        return returner.getValue();
    }

    public static SelectSubjectPage createSelectSubjectPage(final RestServer restServer, final Dimension dimension, final UploadSelector uploadSelector, final GiftCloudReporter reporter) throws InvocationTargetException, InterruptedException {

        final RunnableReturner<SelectSubjectPage> returner =
                new RunnableReturner<SelectSubjectPage>() {
                    private SelectSubjectPage result;

                    public void run() {
                        result = new SelectSubjectPage(restServer, dimension, uploadSelector, reporter);
                    }

                    public SelectSubjectPage getValue() {
                        return result;
                    }
                };
        SwingUtilities.invokeAndWait(returner);

        return returner.getValue();
    }

    public static SelectSessionPage createSelectSessionPage(final GiftCloudServer giftCloudServer, final Optional<String> projectName, final FileSelector fileSelector, final UploadSelector uploadSelector) throws InvocationTargetException, InterruptedException {

        final RunnableReturner<SelectSessionPage> returner =
                new RunnableReturner<SelectSessionPage>() {
                    private SelectSessionPage result;

                    public void run() {
                        try {
                            result = new SelectSessionPage(giftCloudServer, projectName, fileSelector, uploadSelector);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }

                    public SelectSessionPage getValue() {
                        return result;
                    }
                };
        SwingUtilities.invokeAndWait(returner);

        return returner.getValue();
    }


    public static ConfirmSessionDatePage createConfirmSessionDatePage(final UploadSelector uploadSelector) throws InvocationTargetException, InterruptedException {

        final RunnableReturner<ConfirmSessionDatePage> returner =
                new RunnableReturner<ConfirmSessionDatePage>() {
                    private ConfirmSessionDatePage result;

                    public void run() {
                        result = new ConfirmSessionDatePage(uploadSelector);
                    }

                    public ConfirmSessionDatePage getValue() {
                        return result;
                    }
                };
        SwingUtilities.invokeAndWait(returner);

        return returner.getValue();
    }

    public static SelectFilesPage createSelectFilesPage(final FileSelector fileSelector, final UploadSelector uploadSelector) throws InvocationTargetException, InterruptedException {

        final RunnableReturner<SelectFilesPage> returner =
                new RunnableReturner<SelectFilesPage>() {
                    private SelectFilesPage result;

                    public void run() {
                        result = new SelectFilesPage(fileSelector, uploadSelector);
                    }

                    public SelectFilesPage getValue() {
                        return result;
                    }
                };
        SwingUtilities.invokeAndWait(returner);

        return returner.getValue();
    }


    public static FileSelectorUsingJFileChooser createFileSelectorUsingJFileChooser() throws InvocationTargetException, InterruptedException {

        final RunnableReturner<FileSelectorUsingJFileChooser> returner =
                new RunnableReturner<FileSelectorUsingJFileChooser>() {
                    private FileSelectorUsingJFileChooser result;

                    public void run() {
                        result = new FileSelectorUsingJFileChooser();
                    }

                    public FileSelectorUsingJFileChooser getValue() {
                        return result;
                    }
                };
        SwingUtilities.invokeAndWait(returner);

        return returner.getValue();
    }

    public static AssignSessionVariablesPage createAssignSessionVariablesPage(final UploadSelector uploadSelector) throws InvocationTargetException, InterruptedException {

        final RunnableReturner<AssignSessionVariablesPage> returner =
                new RunnableReturner<AssignSessionVariablesPage>() {
                    private AssignSessionVariablesPage result;

                    public void run() {
                        result = new AssignSessionVariablesPage(uploadSelector);
                    }

                    public AssignSessionVariablesPage getValue() {
                        return result;
                    }
                };
        SwingUtilities.invokeAndWait(returner);

        return returner.getValue();
    }

    public static WizardDisplayer createAndInstallWizard(final GiftCloudServer server, final java.util.List<WizardPage> pages, final Container container, final Map<String, Object> params, final WizardResultReceiver receiver, final GiftCloudReporter reporter) throws InvocationTargetException, InterruptedException {

        final RunnableReturner<WizardDisplayer> returner =
                new RunnableReturner<WizardDisplayer>() {
                    private WizardDisplayer result;

                    public void run() {
                        final Wizard wizard = WizardPage.createWizard(pages.toArray(new WizardPage[pages.size()]), new UploadWizardResultProducer(server, Executors.newCachedThreadPool(), reporter));
                        result = WizardDisplayer.installInContainer(container, null, wizard, null, params, receiver);
                    }

                    public WizardDisplayer getValue() {
                        return result;
                    }
                };
        SwingUtilities.invokeAndWait(returner);

        return returner.getValue();
    }



}
