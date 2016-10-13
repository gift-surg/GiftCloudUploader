/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;

import java.util.Set;


public abstract class CallableUploader implements CallableWithParameter<Set<String>, FileCollection> {
    public abstract FileCollection getFileCollection();
}
