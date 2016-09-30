/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.util;

import org.nrg.util.EditProgressMonitor;

import java.awt.*;

public class SwingProgressMonitorWrapper implements Progress {

    private Optional<SwingProgressMonitor> monitor = Optional.empty();
    private final Container container;
    private Optional<Integer> maximum = Optional.empty();
    private Optional<String> statusText = Optional.empty();

    public SwingProgressMonitorWrapper(final Container container) {
        this.container = container;
    }

    @Override
    public void startProgressBar(int maximumValue) {
        maximum = Optional.of(maximumValue);
        getMonitor();
    }

    @Override
    public void startProgressBar() {
        getMonitor();
    }

    @Override
    public void updateProgressBar(int value) {
        getMonitor().setProgress(value);
    }

    @Override
    public void updateProgressBar(int value, int maximumValue) {
        maximum = Optional.of(maximumValue);
        getMonitor().setProgress(value);
    }

    @Override
    public void endProgressBar() {
        close();
    }

    @Override
    public void updateStatusText(String progressText) {
        statusText = Optional.of(progressText);
        getMonitor().setNote(progressText);
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    private synchronized EditProgressMonitor getMonitor() {
        if (!monitor.isPresent()) {
            final int maximumValue = maximum.orElse(1);
            final String statusTextValue = statusText.orElse("Please wait");
            monitor = Optional.of(new SwingProgressMonitor(container, statusTextValue, statusTextValue, 0, maximumValue));
        }
        return monitor.get();
    }

    private synchronized void close() {
        if (monitor.isPresent()) {
            monitor.get().close();
        }
        monitor = Optional.empty();
        statusText = Optional.empty();
        maximum = Optional.empty();
    }
}
