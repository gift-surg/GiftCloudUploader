/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/



package uk.ac.ucl.cs.cmic.giftcloud.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runs tasks consecutively
 */
public class ConsecutiveThreadExecutor {
    private final ExecutorService consecutiveThreadExecutor = Executors.newSingleThreadExecutor();

    /**
     * Creates a new thread executor
     */
    public ConsecutiveThreadExecutor() {
        // ShutdownHook will run regardless of whether Command-Q (on Mac) or window closed
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdownThread();
            }
        });
    }

    /**
     * Adds a new task to the end of the list
     *
     * @param task Runnable to be executed after all the current tasks
     */
    public void submit(final Runnable task) {
        consecutiveThreadExecutor.submit(task);
    }

    /**
     * Terminate the consecutive executor thread
     */
    public void shutdownThread() {
        consecutiveThreadExecutor.shutdown();
    }

    public boolean isShutdown() {
        return consecutiveThreadExecutor.isShutdown();
    }
}