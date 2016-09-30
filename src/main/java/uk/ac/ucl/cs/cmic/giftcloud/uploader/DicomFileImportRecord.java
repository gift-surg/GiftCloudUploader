/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg


  Parts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public class DicomFileImportRecord extends FileImportRecord {

    private Optional<AttributeList> attributeList;
    private boolean attemptedToGetAttributes = false;
    private Optional<String> seriesIdentifier = Optional.empty();
    private Optional<String> patientId = Optional.empty();
    private Optional<String> visibleName = Optional.empty();
    private Optional<String> modality = Optional.empty();

    public DicomFileImportRecord(List<String> fileNames, final String date, final PendingUploadTask.DeleteAfterUpload deleteAfterUpload, final Optional<AttributeList> attributeList) {
        super(fileNames, date, deleteAfterUpload);
        this.attributeList = attributeList;
    }

    public DicomFileImportRecord(String dicomFileName, final String date, final PendingUploadTask.DeleteAfterUpload deleteAfterUpload, final Optional<AttributeList> attributeList) {
        this(new ArrayList<String>(), date, deleteAfterUpload, attributeList);
        fileNames.add(dicomFileName);
    }

    @Override
    public String getSeriesIdentifier() {
        getAttributes();
        return seriesIdentifier.get();
    }

    @Override
    public String getPatientId() {
        getAttributes();
        return patientId.orElse("Unknown");
    }

    @Override
    public String getVisibleName() {
        getAttributes();
        return visibleName.orElse("Unknown");
    }

    @Override
    public String getModality() {
        getAttributes();
        return modality.orElse("Unknown");
    }

    private void getAttributes() {
        if (!attemptedToGetAttributes) {
            attemptedToGetAttributes = true;
            try {
                if (attributeList.isPresent()) {
                    setVariablesFromAttributes(attributeList.get());
                }
            } catch (Throwable t) {

            }

            if (!seriesIdentifier.isPresent() || !visibleName.isPresent() || !modality.isPresent()) {
                final FileInputStream fis;
                try {
                    fis = new FileInputStream(this.fileNames.get(0));
                    DicomInputStream i = new DicomInputStream(new BufferedInputStream(fis));
                    AttributeList list = new AttributeList();
                    list.read(i, TagFromName.PixelData);
                    attributeList = Optional.of(list);
                    setVariablesFromAttributes(attributeList.get());
                    i.close();
                    fis.close();
                } catch (Throwable t) {
                }
            }
        }

        if (!seriesIdentifier.isPresent()) {
            seriesIdentifier = Optional.of(UUID.randomUUID().toString());
        }
        if (!visibleName.isPresent()) {
            visibleName = seriesIdentifier;
        }
    }

    private void setVariablesFromAttributes(final AttributeList attributes) {
        final String seriesUid;
        try {
            seriesUid = attributes.get(TagFromName.SeriesInstanceUID).getStringValues()[0];
            if (StringUtils.isNotBlank(seriesUid)) {
                seriesIdentifier = Optional.of(seriesUid);
            }
        } catch (DicomException e) {
        }

        try {
            final String name = attributes.get(TagFromName.PatientName).getStringValues()[0];
            if (StringUtils.isNotBlank(name)) {
                visibleName = Optional.of(name);
            }
        } catch (DicomException e) {
        }

        try {
            final String id = attributes.get(TagFromName.PatientID).getStringValues()[0];
            if (StringUtils.isNotBlank(id)) {
                patientId = Optional.of(id);
            }
        } catch (DicomException e) {
        }

        try {
            final String modalityFromFile = attributes.get(TagFromName.Modality).getStringValues()[0];
            if (StringUtils.isNotBlank(modalityFromFile)) {
                modality = Optional.of(modalityFromFile);
            }
        } catch (DicomException e) {
        }


}
}
