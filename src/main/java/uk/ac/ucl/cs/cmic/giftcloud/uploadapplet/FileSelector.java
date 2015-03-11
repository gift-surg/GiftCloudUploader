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


import java.awt.*;
import java.io.File;

public interface FileSelector {

    /**
     * Returns an array of files which have been selected for use
     *
     * @return    the array of Files selected for use
     */
    File[] getSelectedFiles();

    /**
     * Returns the directory containing the files which have been selected for use
     *
     * @return    the directory selected for use
     */
    File getCurrentDirectory();

    /**
     * Requests that any graphical components created by this class are added to the given container.
     * <p>
     * The calling function does not know whether the implementation is graphical or not. Hence, this method allows graphical components, if they do exist, to be embedded in the supplied container
     *
     */
    void addToContainer(final Container container);
}
