/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.ZipSeriesUploader
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import org.nrg.dcm.edit.AttributeException;
import org.nrg.dcm.edit.ScriptApplicator;
import org.nrg.dcm.edit.ScriptEvaluationException;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.SeriesZipper;
import uk.ac.ucl.cs.cmic.giftcloud.util.CloseableResource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.zip.ZipOutputStream;

class ZipSeriesPostRequestChunked extends HttpRequestWithOutput<Set<String>> {
    private final FileCollection fileCollection;
    private final SeriesZipper zipper;
    private final UploadStatisticsReporter progress;

    ZipSeriesPostRequestChunked(final HttpConnectionWrapper.ConnectionType connectionType,
                                final String url,
                                final FileCollection fileCollection,
                                final Iterable<ScriptApplicator> applicators,
                                final UploadStatisticsReporter progress,
                                final HttpResponseProcessor responseProcessor) {
        super(connectionType, url, responseProcessor);
        this.fileCollection = fileCollection;
        this.zipper = new SeriesZipper(applicators);
        this.progress = progress;
    }


    @Override
    protected void prepareConnection(final HttpConnectionBuilder connectionBuilder) throws IOException {
        super.prepareConnection(connectionBuilder);

        connectionBuilder.setContentType(HttpConnectionBuilder.CONTENT_TYPE_ZIP);
        connectionBuilder.setChunkedStreamingMode(0);
    }

    @Override
    protected void streamToConnection(final OutputStream outputStream) throws IOException {
        new CloseableResource<Void, ZipOutputStream>() {
            @Override
            public Void run() throws IOException {
                resource = new ZipOutputStream(outputStream);
                for (final File file : fileCollection.getFiles()) {
                    progress.addSent(file.length());

                    try {
                        zipper.addFileToZip(file, resource, zipper.getStopTagInputHandler());
                    } catch (AttributeException e) {
                        throw new IOException("The following attribute error occurred while processing a Dicom file for zip upload: " + e.getCause().getLocalizedMessage(), e);
                    } catch (ScriptEvaluationException e) {
                        throw new IOException("The following script error occurred while processing a Dicom file for zip upload: " + e.getCause().getLocalizedMessage(), e);
                    }
                }
                return null;
            }
        }.tryWithResource();
    }
}