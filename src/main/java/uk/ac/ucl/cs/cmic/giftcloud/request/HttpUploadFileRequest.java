/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.request;

import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnection;
import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnectionBuilder;
import uk.ac.ucl.cs.cmic.giftcloud.util.CloseableResource;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public class HttpUploadFileRequest extends HttpRequestWithOutput<Set<String>> {
    private static final int BUF_SIZE = 4096;
    private final File temporaryFile;

    public HttpUploadFileRequest(final HttpConnection.ConnectionType connectionType,
                                 final String url,
                                 final File temporaryFile,
                                 final HttpResponseProcessor responseProcessor,
                                 final HttpProperties httpProperties,
                                 final GiftCloudReporter reporter) {
        super(connectionType, url, responseProcessor, httpProperties, reporter);
        this.temporaryFile = temporaryFile;
    }


    @Override
    protected void prepareConnection(final HttpConnectionBuilder connectionBuilder) throws IOException {
        super.prepareConnection(connectionBuilder);
        connectionBuilder.setContentType(HttpConnectionBuilder.CONTENT_TYPE_ZIP);

        final long zipFileSize = temporaryFile.length();
        connectionBuilder.setFixedLengthStreamingMode(zipFileSize);
    }

    @Override
    protected void streamToConnection(final OutputStream outputStream) throws IOException {
        new CloseableResource<Void, FileInputStream>() {
            @Override
            public Void run() throws IOException {
                resource = new FileInputStream(temporaryFile);
                writeChunk(resource, outputStream);
                return null;
            }
        }.tryWithResource();
    }

    private void writeChunk(FileInputStream fis, OutputStream os) throws IOException {
        final byte[] buf = new byte[BUF_SIZE];
        int bytesRead;

        while ((bytesRead = fis.read(buf)) > 0) {
            os.write(buf, 0, bytesRead);
        }
    }
}
