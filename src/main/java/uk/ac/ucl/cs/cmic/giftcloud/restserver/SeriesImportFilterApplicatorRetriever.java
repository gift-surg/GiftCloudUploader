/*
 * uk.ac.ucl.cs.cmic.giftcloud.restserver.SeriesImportFilterApplicatorRetriever
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For retrieving XNAT series import filters for a particular project
 */
public final class SeriesImportFilterApplicatorRetriever {

    private final SeriesImportFilter _siteWideFilters;
    private final SeriesImportFilter _projectFilters;

	public SeriesImportFilterApplicatorRetriever(final GiftCloudServer server, final Optional<String> projectName) throws IOException, JSONException {
        _siteWideFilters = extractSitewideSeriesImportFilters(server);
        if (projectName.isPresent() && StringUtils.isNotBlank(projectName.get())) {
            _projectFilters = extractProjectSeriesImportFilters(server, projectName.get());
        } else {
            _projectFilters = null;
        }
	}

    public boolean checkSeries(final String description) {
        return _siteWideFilters.allow(description) && (_projectFilters == null || _projectFilters.allow(description));
    }

    private SeriesImportFilter extractSitewideSeriesImportFilters(final GiftCloudServer server) throws IOException, JSONException {
        Optional<Map<String, String>> contents = server.getSitewideSeriesImportFilter();
        if (contents.isPresent()) {
            return new SeriesImportFilter(contents);
        } else {
            return NULL_FILTER;
        }
    }

    private SeriesImportFilter extractProjectSeriesImportFilters(final GiftCloudServer server, String projectName) throws IOException, JSONException {
        Optional<Map<String, String>> contents = server.getProjectSeriesImportFilter(projectName);
        if (contents.isPresent()) {
            return new SeriesImportFilter(contents);
        } else {
            return NULL_FILTER;
        }
    }

    static class SeriesImportFilter {

        final private boolean _enabled;
        final private String _mode;
        final private List<Pattern> _patterns = new ArrayList<Pattern>();

        final static private String MODE_BLACKLIST = "blacklist";
        final static private String MODE_WHITELIST = "whitelist";

        final static private String DEFAULT_MODE = MODE_BLACKLIST;
        final static private String DEFAULT_LIST = "";

        private SeriesImportFilter() {
            _enabled = false;
            _mode = DEFAULT_MODE;
        }

        public SeriesImportFilter(final Optional<Map<String, String>> optionalContents) {
            _enabled = optionalContents.isPresent();
            if (!_enabled) {
                _mode = DEFAULT_MODE;
            } else {
                final Map<String, String> contents = optionalContents.get();
                _mode = contents.containsKey("mode") ? contents.get("mode") : DEFAULT_MODE;
                final String rawFilters = contents.containsKey("list") ? contents.get("list") : DEFAULT_LIST;
                if (StringUtils.isNotBlank(rawFilters)) {
                    final String[] filters = rawFilters.trim().split("\n");
                    for (final String filter : filters) {
                        _patterns.add(Pattern.compile(filter, Pattern.CASE_INSENSITIVE));
                    }
                }
            }
        }

        public boolean allow(final String seriesDescription) {
            // If this is not enabled, then everything matches.
            if (!_enabled) {
                return true;
            }

            // Iterate through the patterns.
            for (Pattern pattern : _patterns) {
                Matcher matcher = pattern.matcher(seriesDescription == null ? "" : seriesDescription);
                // If the description matches this pattern...
                if (matcher.matches()) {
                    // Then keep the series if this is a whitelist, don't if not.
                    return _mode.equals(MODE_WHITELIST);
                }
            }

            // If the description didn't match any of the patterns, then keep it if this is a blacklist, don't if not.
            return _mode.equals(MODE_BLACKLIST);
        }
    }

    private static final SeriesImportFilter NULL_FILTER = new SeriesImportFilter();
}
