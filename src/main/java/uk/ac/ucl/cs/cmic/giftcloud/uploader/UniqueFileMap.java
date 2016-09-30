/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UniqueFileMap<V> {
    private final Map<String, V> fileMap = new HashMap<String, V>();

    public synchronized void put(final String fileName, final V value) throws IOException {
        final File file = new File(fileName);
        final String canonicalPath = file.getCanonicalPath();
        fileMap.put(canonicalPath, value);
    }

    public synchronized void safeRemove(final File file) throws IOException {
        final String canonicalPath = file.getCanonicalPath();
        if (fileMap.containsKey(canonicalPath)) {
            fileMap.remove(canonicalPath);
        }
    }

    public synchronized Optional<V> get(final File file) throws IOException {
        final String canonicalPath = file.getCanonicalPath();
        if (fileMap.containsKey(canonicalPath)) {
            return Optional.of(fileMap.get(canonicalPath));
        } else {
            return Optional.empty();
        }
    }
}
