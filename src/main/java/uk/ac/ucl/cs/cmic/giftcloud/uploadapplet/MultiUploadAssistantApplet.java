/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    Released under the Simplified BSD.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudUploaderRestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudDialogs;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.UploaderGuiController;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.MainFrame;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.PropertyStoreFromApplet;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PropertyStore;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

public class MultiUploadAssistantApplet extends JApplet {
    private Optional<GiftCloudReporterFromApplet> reporter = Optional.empty();
    private Optional<UploaderGuiController> giftCloudUploaderMain = Optional.empty();

    /**
     * Default constructor.
     */
    public MultiUploadAssistantApplet() {
        setLayout(new BorderLayout());

    }

    /**
     * Initializes the applet.
     *
     * @see java.applet.Applet#init()
     */
    @Override
    public void init() {
        try {
            final MainFrame mainFrame = new MainFrame(this, new JFrame());
            final GiftCloudDialogs dialogs = new GiftCloudDialogs(mainFrame);
            reporter = Optional.of(new GiftCloudReporterFromApplet(this, dialogs));
            final MultiUploadAppletParameters multiUploadAppletParameters = new MultiUploadAppletParameters(this, reporter.get());
            final PropertyStore propertyStore = new PropertyStoreFromApplet(new MultiUploadParameters(multiUploadAppletParameters));
            giftCloudUploaderMain = Optional.of(new UploaderGuiController(mainFrame, new GiftCloudUploaderRestServerFactory(), propertyStore, dialogs, reporter.get()));


        } catch (Throwable t) {
            if (reporter.isPresent()) {
                reporter.get().reportErrorToUser("Applet initialisation failed", t);
            }
            throw new RuntimeException(t);
        }
    }

    /**
     * Implementation of the {@link java.applet.Applet#start()} method.
     *
     * @see java.applet.Applet#start()
     */
    @Override
    public void start() {
        try {
            giftCloudUploaderMain.get().start(true, new ArrayList<File>());

        } catch (Throwable t) {
            reporter.get().reportErrorToUser("Applet startup failed", t);
            throw new RuntimeException(t);
        }
    }

    /**
     * Implementation of the {@link java.applet.Applet#stop()} method.
     *
     * @see java.applet.Applet#stop()
     */
    @Override
    public void stop()
    {
    }

    /**
     * Implementation of the {@link java.applet.Applet#getAppletInfo()} method.
     *
     * @see java.applet.Applet#getAppletInfo()
     */
    @Override
    public String getAppletInfo() {
        return MultiUploadAppletParameterInfo.getAppletInfo();
    }

    /**
     * Implementation of the {@link java.applet.Applet#getParameterInfo()} method.
     *
     * @see java.applet.Applet#getParameterInfo()
     */
    @Override
    public String[][] getParameterInfo() {
        return MultiUploadAppletParameterInfo.getParameterInfo();
    }
}
