/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

// ToDo: does this do the same as HttpSetResponseProcessor?

class HttpStringListResponseProcessor extends HttpResponseProcessor<Set<String>> {

    protected final Set<String> streamFromConnection(final InputStream inputStream) throws IOException {
        final List<String> items = Lists.newArrayList();
        items.addAll(MultiUploaderUtils.readStrings(inputStream));
        return Sets.newLinkedHashSet(items);
    }
}
