/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.IOException;

/**
 * An interface for storing and retrieving properties and passwords
 */
public interface PropertyStore {

    /**
     * Retrieves the property with the given name
     */
    String getProperty(final String propertyName);

    /**
     * Sets the property with the given name
     */
    void setProperty(final String propertyName, final String propertyValue);

    /**
     * Gets the password with the given name
     */
    Optional<char[]> getPassword(final String passwordKey);

    /**
     * Sets the password with the given name
     */
    void setPassword(final String passwordKey, char[] lastPassword);

    /**
     * Store any changes to properties
     */
    void save(String comment) throws IOException;
}
