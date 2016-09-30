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

import com.google.common.collect.Sets;
import uk.ac.ucl.cs.cmic.giftcloud.util.HttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;


public class HttpSetResponseProcessor extends HttpResponseProcessor<Set<String>> {

    protected final Set<String> streamFromConnection(final InputStream inputStream) throws IOException {
        return Sets.newLinkedHashSet(HttpUtils.readEntityLines(inputStream));
    }

}
