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

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONException;
import org.netbeans.spi.wizard.ResultProgressHandle;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import org.nrg.dcm.edit.ScriptApplicator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public interface RestServer {
    void tryAuthentication() throws IOException;

    Collection<Object> getValues(String path, String key) throws IOException, JSONException;

    Map<String, String> getAliases(String path, String aliasKey, String idKey) throws IOException, JSONException;

    String getString(String path) throws Exception;

    Optional<String> getOptionalString(String path) throws IOException;

    Set<String> getStringList(String path) throws Exception;

    <ApplicatorT> ApplicatorT getApplicator(String path, ScriptApplicatorFactory<ApplicatorT> factory) throws Exception;

    <T> Optional<T> getUsingJsonExtractor(final String query) throws IOException;

    void uploadEcat(String path, String projectName, String sessionId, String subjectLabel, ResultProgressHandle progress, File file) throws Exception;

    String getStringFromStream(String path, InputStream xmlStream) throws Exception;

    String sendSessionVariables(String path, final SessionParameters sessionParameters) throws Exception;

    Set<String> uploadSingleFileAsZip(String relativeUrl, boolean useFixedSizeStreaming, final FileCollection fileCollection,
                              Iterable<ScriptApplicator> applicators,
                              UploadStatisticsReporter progress) throws Exception;

    Set<String> uploadZipFile(String relativeUrl, boolean useFixedSizeStreaming, final FileCollection fileCollection,
                              Iterable<ScriptApplicator> applicators,
                              UploadStatisticsReporter progress) throws Exception;

    // In the event that the user cancels authentication
    void resetCancellation();

    void createResource(final String relativeUrl) throws IOException;
}
