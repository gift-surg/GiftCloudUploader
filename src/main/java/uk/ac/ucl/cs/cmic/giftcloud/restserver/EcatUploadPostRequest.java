/*
 * org.nrg.ecat.ModifyingUploadProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.io.ByteStreams;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.nrg.ecat.HeaderModification;
import org.nrg.ecat.MatrixData;
import org.nrg.ecat.var.Variable;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.ResultProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.util.CloseableResource;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.nrg.ecat.MainHeader.*;

public class EcatUploadPostRequest extends HttpRequestWithOutput<Void> {
	private static final String CONTENT_TYPE = "application/zip";
	private static final Variable[] toClear = {
		PATIENT_AGE,
		PATIENT_HEIGHT,
		PATIENT_WEIGHT,
		PATIENT_BIRTH_DATE,
	};

	private final Logger logger = LoggerFactory.getLogger(EcatUploadPostRequest.class);
	private final String name;
	private final int size;
	private final ResultProgressListener progress;
	private final Collection<HeaderModification> modifications;
	private File f;
	private File tempzip;

	/**
	 *
	 */
	public EcatUploadPostRequest(final String urlString, final File f, final ResultProgressHandle progress,
                                 final String project, final String subject, final String session, final MultiUploadReporter reporter)
	throws IOException {
		super(HttpConnectionWrapper.ConnectionType.POST, urlString, new HttpEmptyResponseProcessor(), reporter);

        this.f = f;
		size = (int)f.length();
		if (size < 0) {
			throw new UnsupportedOperationException("cannot upload files with size beyond integer range");
		}
		name = f.getName();
		this.progress = new ResultProgressListener(progress, 0, size);
		modifications = new ArrayList<HeaderModification>();
		modifications.add(STUDY_DESCRIPTION.createValueModification(project));
		modifications.add(PATIENT_NAME.createValueModification(subject));
		modifications.add(PATIENT_ID.createValueModification(session));
		for (final Variable v : toClear) {
			modifications.add(v.createClearModification());
		}

	}

    @Override
    protected void prepareConnection(final HttpConnectionBuilder connectionBuilder) throws IOException {
        super.prepareConnection(connectionBuilder);
        connectionBuilder.setContentType(CONTENT_TYPE);
        createZipFile(f);
        final int zipsize = (int) tempzip.length();  //TODO: Uh Oh... this is a naive and stupid cast.
        connectionBuilder.setFixedLengthStreamingMode(zipsize);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        tempzip.delete();
    }

    @Override
    protected void streamToConnection(final OutputStream outputStream) throws IOException {
        new CloseableResource<Void, FileInputStream>() {
            @Override
            public Void run() throws IOException {
                resource = new FileInputStream(tempzip);
                ByteStreams.copy(resource, outputStream);
                return null;
            }
        }.tryWithResource();
	}

	private void createZipFile(final File f) throws IOException {
        new CloseableResource<Void, FileInputStream>() {
            @Override
            public Void run() throws IOException {
                final FileInputStream in = new FileInputStream(f);
                resource = in;

                tempzip = File.createTempFile("scan-upload", ".zip");
                logger.debug("creating zip file {}", tempzip);

                new CloseableResource<Void, FileOutputStream>() {
                    @Override
                    public Void run() throws IOException {
                        final FileOutputStream fos = new FileOutputStream(tempzip);
                        resource = fos;

                        new CloseableResource<Void, ZipOutputStream>() {
                            @Override
                            public Void run() throws IOException {
                                final ZipOutputStream zout = new ZipOutputStream(fos);
                                resource = zout;

                                final ZipEntry ze = new ZipEntry(name);
                                zout.putNextEntry(ze);
                                MatrixData.copyWithModifications(zout, in, modifications, progress);
                                zout.closeEntry();
                                zout.flush();

                                return null;
                            }
                        }.tryWithResource();

                        return null;
                    }
                }.tryWithResource();

                return null;
            }
        }.tryWithResource();

	}
}
