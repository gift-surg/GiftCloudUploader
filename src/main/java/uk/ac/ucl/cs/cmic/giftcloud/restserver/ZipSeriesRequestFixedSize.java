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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.util.CloseableResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

class ZipSeriesRequestFixedSize extends HttpRequestWithOutput<Set<String>> {
    private static final int BUF_SIZE = 4096;

    private final Logger logger = LoggerFactory.getLogger(ZipSeriesRequestFixedSize.class);
    private final FileCollection fileCollection;
    private final SeriesZipper zipper;
    private final UploadStatisticsReporter progress;
    private File temporaryZipFile = null;

    ZipSeriesRequestFixedSize(final HttpConnectionWrapper.ConnectionType connectionType,
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

        try {
            temporaryZipFile = zipper.buildSeriesZipFile(fileCollection);

            // ToDo: handle these exceptions properly
        } catch (AttributeException e) {
            e.printStackTrace();
        } catch (ScriptEvaluationException e) {
            e.printStackTrace();
        }

        final long zipFileSize = temporaryZipFile.length();
        connectionBuilder.setFixedLengthStreamingMode((int) zipFileSize); // ToDo: Fix this cast
    }

    @Override
    protected void streamToConnection(final OutputStream outputStream) throws IOException {
        final long zipFileSize = temporaryZipFile.length();
        final float zipRatio = (float) fileCollection.getSize() / zipFileSize;

        new CloseableResource<Void, FileInputStream>() {
            @Override
            public Void run() throws IOException {
                resource = new FileInputStream(temporaryZipFile);
                writeChunk(zipFileSize, zipRatio, resource, outputStream);
                return null;
            }
        }.tryWithResource();
    }

    @Override
    protected void cleanup() {
        temporaryZipFile.delete();
    }

    private void writeChunk(long zipFileSize, float zipRatio, FileInputStream fis, OutputStream os) throws IOException {
        final byte[] buf = new byte[BUF_SIZE];
        int chunk;
        for (int total = 0; (chunk = fis.read(buf)) != -1; total += chunk) {
            logger.trace("copying {} / {}", total, zipFileSize);
            long adjustedChunkSize = (long) (chunk * zipRatio);
            progress.addSent(adjustedChunkSize);
            os.write(buf, 0, chunk);
        }
    }
}
