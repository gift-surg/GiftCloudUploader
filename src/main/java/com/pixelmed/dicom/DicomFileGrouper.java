package com.pixelmed.dicom;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by Tom Doel
 */

public class DicomFileGrouper {

    private Map<String, List<String>> groupedFiles = new HashMap<String, List<String>>();

    public void add(final String sourceFileName) throws IOException, DicomException {
        AttributeList list = new AttributeList();
        list.read(sourceFileName, TagFromName.PixelData);

        Optional<String> seriesInstanceUid = getSeriesInstanceUid(list);

        if (seriesInstanceUid.isPresent()) {
            getOrCreateGroup(seriesInstanceUid.get()).add(sourceFileName);
        } else {
            throw new RuntimeException("No Dicom series UID found. This version of the uploader requires a series uid for every file.");
        }
    }

    public Map<String, List<String>> getGroupedFiles() {
        return groupedFiles;
    }

    private List<String> getOrCreateGroup(final String seriesUid) {
        if (StringUtils.isEmpty(seriesUid)) {
            throw new RuntimeException("No series uid specified.");
        }

        if (!groupedFiles.containsKey(seriesUid)) {
            groupedFiles.put(seriesUid, new ArrayList<String>());
        }

        return groupedFiles.get(seriesUid);
    }

    static private Optional<String> getSeriesInstanceUid(final AttributeList attributeList) {
        final String series_uid = Attribute.getSingleStringValueOrEmptyString(attributeList, TagFromName.SeriesInstanceUID).replaceAll("[^0-9]", "").trim();
        if (StringUtils.isEmpty(series_uid)) {
            return Optional.empty();
        }   else {
            return Optional.of(series_uid);
        }
    }


}
