/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.collect.ImmutableList;
import org.dcm4che2.data.Tag;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.UploadParameters;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.CallableWithParameter;

import java.util.Collections;
import java.util.Set;


public abstract class CallableUploader implements CallableWithParameter<Set<String>, FileCollection> {
    protected final UploadParameters uploadParameters;
    protected final FileCollection fileCollection;
    protected final GiftCloudServer server;

    public static int MAX_TAG = Collections.max(ImmutableList.of(Tag.SOPInstanceUID,
            Tag.TransferSyntaxUID, Tag.FileMetaInformationVersion, Tag.SOPClassUID));

    public CallableUploader(
            final UploadParameters uploadParameters,
            final GiftCloudServer server) {
        this.uploadParameters = uploadParameters;
        this.fileCollection = uploadParameters.getFileCollection();
        this.server = server;
    }


    public final FileCollection getFileCollection() {
        return fileCollection;
    }

    public final FileCollection getParameter() {
        return fileCollection;
    }


    public interface CallableUploaderFactory {
        CallableUploader create(
                final UploadParameters uploadParameters,
                final GiftCloudServer server);
    }
}
