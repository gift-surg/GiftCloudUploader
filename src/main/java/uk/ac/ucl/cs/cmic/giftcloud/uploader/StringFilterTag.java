/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.DicomException;

/**
 * A PixelDataAnonymiseFilterRequiredTag representing an integer value
 */
public class StringFilterTag extends PixelDataAnonymiseFilterRequiredTag<String> {
    public StringFilterTag(final int dicomGroup, final int dicomElement, final String value) {
        super("String", dicomGroup, dicomElement, value);
    }

    public StringFilterTag(final Attribute attribute) throws DicomException {
        this(attribute.getGroup(), attribute.getElement(), attribute.getStringValues()[0]);
    }

    @Override
    public boolean matches(final Attribute attribute) throws DicomException {
        final String[] stringValues = attribute.getStringValues();
        return stringValues.length == 1 && stringValues[0].equals(getValue());
    }
}
