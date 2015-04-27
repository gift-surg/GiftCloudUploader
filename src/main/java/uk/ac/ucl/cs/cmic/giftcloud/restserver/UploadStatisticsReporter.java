/*
 * uk.ac.ucl.cs.cmic.giftcloud.restserver.UploadStatisticsReporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.util.Utils;

public class UploadStatisticsReporter {
    private static final long SCALE = 1024;
    private final Logger logger = LoggerFactory.getLogger(UploadStatisticsReporter.class);
    private final Progress progress;
    private long bytesToSend = 0, bytesSent = 0;

    public UploadStatisticsReporter(final Progress progress) {
        this.progress = progress;
    }

    private void update() {
        if (bytesSent > 0) {
            final long kToSend;
            if (bytesSent > bytesToSend) {
                logger.error("more bytes sent ({}) than available ({})?", bytesSent, bytesToSend);
                progress.updateStatusText(this.toString());
            } else if ((kToSend = bytesToSend/SCALE) > Integer.MAX_VALUE) {
                logger.debug("progress overflow: {} / {}", bytesSent/SCALE, kToSend);
                progress.updateStatusText(this.toString());
            } else {
                assert bytesSent > 0;
                assert bytesSent/SCALE <= kToSend;
                try {
                    progress.updateStatusText(this.toString());
                    progress.updateProgressBar((int)(bytesSent/SCALE), (int)kToSend);
                } catch (Throwable t) {
                    logger.error("failed to set progress indicator", t);
                }
            }
        } else {
            progress.updateStatusText(this.toString());
        }
    }

    public long addToSend(final long size) {
        bytesToSend += size;
        update();
        return bytesToSend;
    }

    public long addSent(final long size) {
        bytesSent += size;
        update();
        return bytesSent;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (bytesSent > 0) {
            final StringBuilder sb = new StringBuilder();
            Utils.showNearestUnits(sb, bytesSent, "B");
            sb.append("/");
            Utils.showNearestUnits(sb, bytesToSend, "B");
            sb.append(" ");
            return sb.toString();
        } else {
            final StringBuilder sb = new StringBuilder("Preparing...");
            Utils.showNearestUnits(sb, bytesToSend, "B");
            return sb.toString();
        }
    }
}
