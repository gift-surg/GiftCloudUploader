/*
 * uk.ac.ucl.cs.cmic.giftcloud.restserver.PETTracerRetriever
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.collect.Sets;
import uk.ac.ucl.cs.cmic.giftcloud.util.CloseableResource;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

public final class PETTracerRetriever implements Callable<Set<String>> {
    private static final String DEFAULT_TRACERS_RESOURCE = "/uk/ac/ucl/cs/cmic/giftcloud/PET-tracers.txt";
    private static final Set<String> defaultTracers = getDefaultTracers(DEFAULT_TRACERS_RESOURCE);

    private final RestServer restServer;
    private String projectName;

    public PETTracerRetriever(final RestServer restServer, final String projectName) {
        this.restServer = restServer;
        this.projectName = projectName;
    }

    public Set<String> call() {
        try {
            // check to see if we got back a status page instead of a list of tracers
            // if so, there's no project specific list, so just get the site list instead
            try {
                return restServer.getProjectTracers(projectName);
            } catch (Throwable t) {
                return restServer.getSiteTracers();
            }
        // Cancellation exceptions should terminate the whole operation, while other exceptions result in default behaviour
        } catch (CancellationException e) {
            throw e;
        } catch (Throwable t) {
            return getDefaultTracers();
        }
    }


    public static Set<String> getDefaultTracers() {
        return Sets.newLinkedHashSet(defaultTracers);
    }

    private static Set<String> getDefaultTracers(final String resourceName) {
//        try (final InputStream in = PETTracerRetriever.class.getResourceAsStream(resource)) {
//            return Sets.newLinkedHashSet(MultiUploaderUtils.readStrings(in));
//        } catch (IOException e) {
//            throw new RuntimeException("Unable to read default PET tracers", e);
//        }

        try {
            return new CloseableResource<Set<String>, InputStream>() {
                @Override
                public Set<String> run() throws IOException {
                    resource = PETTracerRetriever.class.getResourceAsStream(resourceName);
                    return Sets.newLinkedHashSet(MultiUploaderUtils.readStrings(resource));
                }
            }.tryWithResource();
        } catch (Throwable e) {
            throw new RuntimeException("Unable to read default PET tracers", e);
        }
    }
}
