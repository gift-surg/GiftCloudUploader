/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.CloseableResource;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

class UploadFileRequest extends HttpRequestWithOutput<Set<String>> {
    private static final int BUF_SIZE = 4096;
    private final File temporaryFile;

    UploadFileRequest(final HttpConnectionWrapper.ConnectionType connectionType,
                      final String url,
                      final File temporaryFile,
                      final HttpResponseProcessor responseProcessor,
                      final GiftCloudProperties giftCloudProperties,
                      final GiftCloudReporter reporter) {
        super(connectionType, url, responseProcessor, giftCloudProperties, reporter);
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
        int chunk;
        while ((chunk = fis.read(buf)) != -1) {
            os.write(buf, 0, chunk);
        }
    }
}