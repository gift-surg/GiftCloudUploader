/*

 Author: Tom Doel

 Some parts of this software were derived from DicomCleaner, Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.


 */

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.core.io.Resource;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Stores pixel data redaction templates
 */
public class PixelDataAnonymiserFilterCache {

    private List<PixelDataAnonymiseFilter> filters;
    private GiftCloudReporter reporter;
    private GiftCloudProperties properties;


    /**
     * Creates the PixelDataAnonymiser object. Parameters will be set at construction time
     *
     * @param giftCloudProperties Shared properties use to define anonymsation options
     * @param reporter
     */
    public PixelDataAnonymiserFilterCache(final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) {
        this.properties = giftCloudProperties;
        this.reporter = reporter;
        reloadFilters();
    }

    /**
     * @return list of currently loaded anonymisation filters
     */
    public synchronized List<PixelDataAnonymiseFilter> getFilters() {
        return filters;
    }

    /**
     * Reads filters from disk
     */
    public synchronized void reloadFilters() {
        this.filters = readFilters(properties, reporter);
    }

    private List<PixelDataAnonymiseFilter> readFilters(final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) {
        final ArrayList<PixelDataAnonymiseFilter> filters = new ArrayList<PixelDataAnonymiseFilter>();

        // Read in local filters
        FileFilter fileFilter = new WildcardFileFilter("*.gcfilter");
        filters.addAll(readFilterFiles(Arrays.asList(new File(giftCloudProperties.getFilterDirectory().getAbsolutePath()).listFiles(fileFilter)), reporter));

        // Read in predefined filters from resource streams
        final String resourceFilter = "classpath*:uk/**/*.gcfilter";
        filters.addAll(readFilterResources(GiftCloudUtils.getMatchingResources(resourceFilter, reporter), reporter));

        return filters;
    }

    private List<PixelDataAnonymiseFilter> readFilterFiles(final List<File> files, GiftCloudReporter reporter) {
        final ArrayList<PixelDataAnonymiseFilter> filters = new ArrayList<PixelDataAnonymiseFilter>();
        for (final File f : files) {
            try {
                filters.add(PixelDataAnonymiserFilterJsonWriter.readJsonFile(f));
            } catch (final Exception e) {
                reporter.silentLogException(e, "Could not read filter file: " + f.getAbsolutePath());
            }
        }
        return filters;
    }

    private List<PixelDataAnonymiseFilter> readFilterResources(final List<Resource> resources, GiftCloudReporter reporter) {
        final ArrayList<PixelDataAnonymiseFilter> filters = new ArrayList<PixelDataAnonymiseFilter>();
        for (final Resource resource : resources) {
            try {
                filters.add(PixelDataAnonymiserFilterJsonWriter.readJsonResource(resource));
            } catch (final Exception e) {
                reporter.silentLogException(e, "Could not read filter file: " + resource.getFilename());
            }
        }
        return filters;
    }

}

