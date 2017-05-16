/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudUploaderRestClientFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GiftCloudUploaderDaemon implements Daemon {
    private static GiftCloudUploaderDaemonLauncher launcher = null;
    private static GiftCloudUploaderDaemon daemon = new GiftCloudUploaderDaemon();
    private static boolean stop = false;

    public GiftCloudUploaderDaemon() {
        daemon = this;
    }

    /**
     * Initialise daemon (Unix)
     *
     * @param context
     * @throws Exception
     */
    @Override
    public void init(DaemonContext context) throws Exception {
    }

    /**
     * Start daemon (Unix)
     */
    @Override
    public void start() {
        startDaemon(new ArrayList<File>());
    }

    /**
     * Stop daemon (Unix)
     */
    @Override
    public void stop() {
        stopDaemon();
    }

    /**
     * Destroy daemon (Unix)
     */
    @Override
    public void destroy() {
    }

    /**
     * Start the daemon (Windows)
     */
    public static void startWindows() {
        getDaemon().startDaemon(new ArrayList<File>());
    }

    /**
     * Stop the daemon (Windows)
     */
    public void stopWindows() {
        stopDaemon();
        synchronized(this) {
            this.notify();
        }
    }

    /**
     * Shared method to start the daemon
     *
     * @param fileList list of files to import on startup
     */
    private void startDaemon(List<File> fileList) {
        if (launcher == null) {
            launcher = new GiftCloudUploaderDaemonLauncher(new GiftCloudUploaderRestClientFactory(), fileList);
        }
    }

    /**
     * Shared method to stop the daemon
     */
    public void stopDaemon() {
        if (launcher != null) {
            launcher.quit();
        }
    }

    /**
     * Run the daemon from the command line
     * @param arg
     */
    public static void main(String arg[]) {
        final List<File> fileList = new ArrayList<File>();
        if (arg.length==2) {
            fileList.add(new File(arg[1]));
        }

        getDaemon().startDaemon(fileList);
    }

    private static GiftCloudUploaderDaemon getDaemon() {
        if (daemon != null) {
            daemon = new GiftCloudUploaderDaemon();
        }
        return daemon;
    }
}