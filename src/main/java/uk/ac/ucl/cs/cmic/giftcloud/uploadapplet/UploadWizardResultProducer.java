/*
 * UploadWizardResultProducer
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.data.Session;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SessionParameters;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.UploadResult;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.UploadResultsFailure;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.UploadResultsSuccess;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class UploadWizardResultProducer implements WizardResultProducer {

    private GiftCloudServer giftCloudServer;
    private final ExecutorService _executorService;
    private MultiUploadReporter reporter;

    public UploadWizardResultProducer(final GiftCloudServer giftCloudServer, final ExecutorService executorService, final MultiUploadReporter reporter) {
        this.giftCloudServer = giftCloudServer;
        _executorService = executorService;
        this.reporter = reporter;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage.WizardResultProducer#cancel(java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    public boolean cancel(final Map arg0) { return true; }

    /* (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage.WizardResultProducer#finish(java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    public Object finish(final Map arg0){
        return new UploadWizardResult();
    }

    private class UploadWizardResult extends DeferredWizardResult {
        private final Logger logger = LoggerFactory.getLogger(UploadWizardResult.class);

        private Future<UploadResult> upload = null;

        UploadWizardResult() {
            super(true);
        }

        /*
         * (non-Javadoc)
         * @see org.netbeans.spi.wizard.DeferredWizardResult#start(java.util.Map, org.netbeans.spi.wizard.ResultProgressHandle)
         */
        @SuppressWarnings("rawtypes")
        @Override
        public void start(final Map wizardData, final ResultProgressHandle progress) {
            try {
                final UploadSelector uploadSelector = (UploadSelector)wizardData.get(UploadSelector.UPLOAD_SELECTOR_WIZARD_ID);
                final Session session = uploadSelector.getSession();
                logger.trace("Wizard parameter map {}", wizardData);
                final SessionParameters sessionParameters = new WizardSessionParameters(wizardData);
                        // Analytics.enter(UploadAssistantApplet.class, String.format("Upload commenced with wizard parameter map %s", wizardData));
                upload = _executorService.submit(new Callable<UploadResult>() {
                    public UploadResult call() {
                        try {
                            UploadResult result = session.uploadTo(uploadSelector.getProject().toString(), uploadSelector.getSubject().getLabel(), giftCloudServer, sessionParameters, uploadSelector.getProject(), new SwingUploadFailureHandler(), reporter);

                            if (result instanceof UploadResultsSuccess) {
                                final UploadResultsSuccess resultsSuccess = (UploadResultsSuccess)result;
                                final UploadResultPanel resultPanel = new UploadResultPanel(resultsSuccess.getsessionLabel(), resultsSuccess.getSessionViewUrl(), uploadSelector.getWindowName(), uploadSelector.getJSContext());
                                final String fullUrlString = sessionParameters.getBaseURL() + resultsSuccess.getUri();
                                progress.finished(Summary.create(resultPanel, new URL(fullUrlString)));
                            } else if (result instanceof UploadResultsFailure) {
                            } else {
                                throw new RuntimeException("Unknown result type");
                            }
                            return result;

                        } catch (IOException e) {
                            e.printStackTrace();
                            return new UploadResultsFailure("Failed to upload due to the following error:" + e.getLocalizedMessage());
                        }
                    }
                });
                upload.get();



            } catch (final CancellationException ignored) {
                progress.failed("Upload canceled", false);
                // Analytics.enter(UploadAssistantApplet.class, "Upload cancelled by user");
            } catch (final Throwable throwable) {
                System.out.println("upload failed: " + throwable);
                throwable.printStackTrace();
                logger.error("Something went wrong during upload", throwable);
                progress.failed(throwable.getMessage(), false);
                // Analytics.enter(UploadAssistantApplet.class, "Failure during upload", throwable);
            } finally {
                // Analytics.enter(UploadAssistantApplet.class, "Upload completed");
            }
        }

        /*
         * (non-Javadoc)
         * @see org.netbeans.spi.wizard.DeferredWizardResult#abort()
         */
        @Override
        public void abort() {
            if (null != upload) { upload.cancel(true); }
        }
    }

}
