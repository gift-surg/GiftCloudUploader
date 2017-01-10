/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Pparts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import java.io.File;
import java.util.Collection;

public class SeriesFileCollection implements FileCollection {

    private final Collection<File> files;
    private final long size;
    private final int fileCount;

    public SeriesFileCollection(final Series series) {
        files = series.getFiles();
        size = series.getSize();
        fileCount = series.getFileCount();
    }

    @Override
    public int getFileCount() {
        return fileCount;
    }

    @Override
    public Collection<File> getFiles() {
        return files;
    }

    @Override
    public long getSize() {
        return size;
    }
}
