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

import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.FileSelector;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileSelectorUsingJFileChooser implements FileSelector {

    private final JFileChooser fileChooser;
    private static final String PRODUCT_NAME = "*file-chooser*";

    public FileSelectorUsingJFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setControlButtonsAreShown(false);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setName(PRODUCT_NAME);
    }

    @Override
    public File[] getSelectedFiles() {
        return fileChooser.getSelectedFiles();
    }

    @Override
    public File getCurrentDirectory() {
        return fileChooser.getCurrentDirectory();
    }

    @Override
    public void addToContainer(Container container) {
        container.add(fileChooser);
    }
}
