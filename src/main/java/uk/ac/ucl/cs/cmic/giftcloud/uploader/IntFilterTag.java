/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Some parts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

 ============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.DicomException;

/**
 * A PixelDataAnonymiseFilterRequiredTag representing an integer value
 */
public class IntFilterTag extends PixelDataAnonymiseFilterRequiredTag<Integer> {
    public IntFilterTag(final int dicomGroup, final int dicomElement, final Integer value) {
        super("Integer", dicomGroup, dicomElement, value);
    }

    public IntFilterTag(final Attribute attribute) throws DicomException {
        this(attribute.getGroup(), attribute.getElement(), attribute.getIntegerValues()[0]);
    }

    @Override
    public boolean matches(final Attribute attribute) throws DicomException {
        final int[] integerValues = attribute.getIntegerValues();
        return integerValues.length == 1 && integerValues[0] == getValue();
    }
}
