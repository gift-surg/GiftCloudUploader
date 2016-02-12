/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.httpconnection;

import com.google.common.collect.Sets;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.HttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;


public class HttpSetResponseProcessor extends HttpResponseProcessor<Set<String>> {

    protected final Set<String> streamFromConnection(final InputStream inputStream) throws IOException {
        return Sets.newLinkedHashSet(HttpUtils.readEntityLines(inputStream));
    }

}
