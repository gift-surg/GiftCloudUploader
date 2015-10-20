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

package uk.ac.ucl.cs.cmic.giftcloud.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.nrg.IOUtils;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.HttpUploadException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MultiUploaderUtils {

    final static String GIFT_CLOUD_APPLICATION_DATA_FOLDER_NAME = "GiftCloudUploader";
    final static String GIFT_CLOUD_UPLOAD_CACHE_FOLDER_NAME = "WaitingForUpload";
    final static String GIFT_CLOUD_REDACTION_TEMPLATES_FOLDER_NAME = "RedactionTemplates";


    public static JSONObject extractJSONEntity(final InputStream in)
            throws IOException, JSONException {
        return new JSONObject(new JSONTokener(new InputStreamReader(in)));
    }

    public static JSONArray extractResultFromEntity(final JSONObject entity)
            throws JSONException {
        return entity.getJSONObject("ResultSet").getJSONArray("Result");
    }

    public static String getErrorEntity(final InputStream errorStream) throws IOException {
        try {
            if (null != errorStream) {
                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                IOUtils.copy(stream, errorStream);
                if (stream.size() > 0) {
                    return stream.toString();
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } catch (IOException ignored) {
            return "";
        } finally {
            if (errorStream != null) {
                try {
                    errorStream.close();
                } catch (IOException ignored) {
                    // Ignore any errors here
                }
            }
        }
    }

    /**
     * Reads a list of newline-separated strings from the provided InputStream.
     * @param in InputStream from which strings will be read
     * @return A list of strings found in the input stream. Each line becomes a string.
     * @throws java.io.IOException
     */
    public static List<String> readStrings(final InputStream in) throws IOException {
        final List<String> items = Lists.newArrayList();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            if (StringUtils.isNotBlank(line)) {
                items.add(line.trim());
            }
        }
        return items;
    }

    public static String buildFailureMessage(final Map<FileCollection, Throwable> failures) {
        final StringBuilder sb = new StringBuilder("<html>");
        buildHTMLFailureMessage(sb, failures);
        return sb.append("</html>").toString();
    }

    private static StringBuilder buildHTMLFailureMessage(final StringBuilder sb, final Map<FileCollection, Throwable> failures) {
        final Multimap<Throwable, FileCollection> inverse = LinkedHashMultimap.create();
        Multimaps.invertFrom(Multimaps.forMap(failures), inverse);
        final Multimap<Object, ?> causes = Utils.consolidateKeys(inverse, 4);
        final MessageFormat format = new MessageFormat("{0} not uploaded: {1}");
        format.setFormatByArgumentIndex(0, new ChoiceFormat(new double[]{0, 1, 2},
                new String[]{"No items", "One item", "{0,number} items"}));
        for (final Object key : causes.keySet()) {
            final Collection<?> items = causes.get(key);
            final Object message;
            if (key instanceof HttpUploadException) {
                final HttpUploadException e = (HttpUploadException) key;
                final StringBuilder m = new StringBuilder("HTTP error ");
                m.append(e.getStatusCode()).append(" - ");
                m.append(e.getMessage()).append("<br>");
                m.append(e.getEntity());
                message = m;
            } else {
                message = key;
            }
            sb.append("<p>").append(format.format(new Object[]{items.size(), message}));
            sb.append("</p><br>");
        }
        return sb;
    }

    /**
     * Returns the folder for storing GiftCloud images waiting to be uploaded, creating the folder if it does not already exist.
     * Will attempt to create a folder in the user directory, but if this is not permitted, will create a folder in the system temporary directory
     *
     * @param reporter for logging warnings
     * @return File object referencing the existing or newly created folder
     */
    public static File createOrGetLocalUploadCacheDirectory(final GiftCloudReporter reporter) {

        final File appFolder = createOrGetGiftCloudFolder(reporter);

        final File uploadCacheFolder = new File(appFolder, GIFT_CLOUD_UPLOAD_CACHE_FOLDER_NAME);

        if (createDirectoryIfNotExisting(uploadCacheFolder)) {
            return uploadCacheFolder;
        } else {
            throw new RuntimeException("Unable to create an upload folder at " + uploadCacheFolder.getAbsolutePath());
        }
    }

    /**
     * Returns the folder for storing GiftCloud pixel data anonymisastion templates, creating the folder if it does not already exist.
     * Will attempt to create a folder in the user directory, but if this is not permitted, will create a folder in the system temporary directory
     *
     * @param reporter for logging warnings
     * @return File object referencing the existing or newly created folder
     */
    public static File createOrGetTemplateDirectory(final GiftCloudReporter reporter) {

        final File appFolder = createOrGetGiftCloudFolder(reporter);

        final File templateFolder = new File(appFolder, GIFT_CLOUD_REDACTION_TEMPLATES_FOLDER_NAME);

        if (createDirectoryIfNotExisting(templateFolder)) {
            return templateFolder;
        } else {
            throw new RuntimeException("Unable to create a template folder at " + templateFolder.getAbsolutePath());
        }
    }

    /**
     * Returns the folder for storing GiftCloud data, creating if it does not already exist.
     * Will attempt to create a folder in the user directory, but if this is not permitted, will create a folder in the system temporary directory
     *
     * @param reporter for logging warnings
     * @return File object referencing the existing or newly created folder
     */
    public static File createOrGetGiftCloudFolder(final GiftCloudReporter reporter) {

        File appFolder = new File(System.getProperty("user.home"), GIFT_CLOUD_APPLICATION_DATA_FOLDER_NAME);

        if (!createDirectoryIfNotExisting(appFolder)) {
            reporter.silentWarning("Could not create an upload folder in the user folder at "  + appFolder.getAbsolutePath() + ". Using system temporary folder instead.");
            appFolder = new File(System.getProperty("java.io.tmpdir"), GIFT_CLOUD_APPLICATION_DATA_FOLDER_NAME);

            if (!createDirectoryIfNotExisting(appFolder)) {
                throw new RuntimeException("Could not create an upload folder in the user folder or in the system temporary folder at " + appFolder.getAbsolutePath());
            }
        }

        return appFolder;
    }

    /**
     * Creates a directory if it does not already exist
     *
     * @param directory a File object referencing the directory
     * @return true if the directory already exists or has been successfully created
     */
    public static boolean createDirectoryIfNotExisting(final File directory) {
        if (directory.exists()) {
            return true;
        } else {
            try {
                return directory.mkdirs();
            } catch (SecurityException e) {
                return false;
            }
        }
    }


    /**
     * Returns true if it is possible to create and delete files in a specified directory
     *
     * @param directory the folder to test
     * @return true if a file can be created in the specified directory
     */
    public static boolean isDirectoryWritable(final String directory) {
        File testFile = null;
        try {
            final File baseFolder = new File(directory);

            final String testfileName = "~testsavegiftclouduploader";
            testFile = new File(baseFolder, testfileName);

            // If a previous test file exists then delete this
            if (testFile.exists()) {
                if (!testFile.delete()) {
                    return false;
                }
                if (testFile.exists()) {
                    return false;
                }
                testFile = new File(baseFolder, testfileName);
            }

            // Attempt to create a new test file
            if (!testFile.createNewFile()) {
                return false;
            }

            // Check that the new test file exists
            if (!testFile.exists()) {
                return false;
            }

            // Attempt to delete the new test file
            if (!testFile.delete()) {
                return false;
            }

            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            if (testFile != null) {
                try {
                    if (testFile.exists()) {
                        testFile.delete();
                    }
                } catch (Throwable t) {

                }
            }
        }
    }

    public static boolean createTimeStampedBackup(final File fileToCopy, final File folder, final String backupPatientListFilenamePrefix, final String backupPatientListFilenameSuffix) {

        try {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyy-HH");
            final File timeStampBackupFile = new File(folder, String.format(backupPatientListFilenamePrefix + "-%s." + backupPatientListFilenameSuffix, simpleDateFormat.format(new Date())));
            final Path timeStampBackupPath = timeStampBackupFile.toPath();

            if (timeStampBackupFile.exists()) {
                timeStampBackupFile.delete();
            }
            Files.copy(fileToCopy.toPath(), timeStampBackupPath);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
