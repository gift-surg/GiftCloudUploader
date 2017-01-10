/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.nrg.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GiftCloudUtils {

    final static String GIFT_CLOUD_APPLICATION_DATA_FOLDER_NAME = "GiftCloudUploader";
    final static String GIFT_CLOUD_UPLOAD_CACHE_FOLDER_NAME = "WaitingForUpload";
    final static String GIFT_CLOUD_REDACTION_TEMPLATES_FOLDER_NAME = "RedactionTemplates";

    private GiftCloudUtils() {
    }

    public static String getDateAsAString() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());
    }

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
     * Returns the folder for storing GiftCloud images waiting to be uploaded, creating the folder if it does not already exist.
     * Will attempt to create a folder in the user directory, but if this is not permitted, will create a folder in the system temporary directory
     *
     * @param reporter for logging warnings
     * @return File object referencing the existing or newly created folder
     */
    public static File createOrGetLocalUploadCacheDirectory(final LoggingReporter reporter) {

        final File appFolder = createOrGetGiftCloudFolder(Optional.of(reporter));

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
    public static File createOrGetTemplateDirectory(final LoggingReporter reporter) {

        final File appFolder = createOrGetGiftCloudFolder(Optional.of(reporter));

        final File templateFolder = new File(appFolder, GIFT_CLOUD_REDACTION_TEMPLATES_FOLDER_NAME);

        if (createDirectoryIfNotExisting(templateFolder)) {
            return templateFolder;
        } else {
            throw new RuntimeException("Unable to create a template folder at " + templateFolder.getAbsolutePath());
        }
    }

    /**
     * Returns a list of resources matching the specified pattern
     *
     * @param pattern
     * @return
     */
    public static List<Resource> getMatchingResources(final String pattern, final GiftCloudReporter reporter) {
        try {
            return Arrays.asList(new PathMatchingResourcePatternResolver().getResources(pattern));
        } catch (Throwable throwable) {
            reporter.silentLogException(throwable, "Error when fetching resources: " + throwable.getLocalizedMessage());
            return new ArrayList<Resource>();
        }
    }

    /**
     * Returns the folder for storing GiftCloud data, creating if it does not already exist.
     * Will attempt to create a folder in the user directory, but if this is not permitted, will create a folder in the system temporary directory
     *
     * @param reporter for logging warnings
     * @return File object referencing the existing or newly created folder
     */
    public static File createOrGetGiftCloudFolder(final Optional<LoggingReporter> reporter) {

        File appFolder = new File(System.getProperty("user.home"), GIFT_CLOUD_APPLICATION_DATA_FOLDER_NAME);

        if (!createDirectoryIfNotExisting(appFolder)) {
            if (reporter.isPresent()) {
                reporter.get().silentWarning("Could not create a folder in the user folder at " + appFolder.getAbsolutePath() + ". Using system temporary folder instead.");
            } else {
                System.out.println("Could not create a folder in the user folder at " + appFolder.getAbsolutePath() + ". Using system temporary folder instead.");
            }
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
        if (directory.getAbsoluteFile().exists()) {
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

    /**
     * Makes a copy of a file with a date-hour timestamp. Any existing backup from the same date-hour will be overwritten.
     * Does not throw an exception; returns false if the backup file could not be created
     *
     * @param fileToCopy the file to use when creating a backup
     * @param folder the location for the backup
     * @param backupPatientListFilenamePrefix a prefix tobe used in the filename of the backup file
     * @param backupPatientListFilenameSuffix a suffix to be used in the filename of the backup file
     * @return true if the backup was created successfully, false if any error occurred
     */
    public static boolean createTimeStampedBackup(final File fileToCopy, final File folder, final String backupPatientListFilenamePrefix, final String backupPatientListFilenameSuffix) {

        try {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyy-HH");
            final File timeStampBackupFile = new File(folder, String.format(backupPatientListFilenamePrefix + "-%s." + backupPatientListFilenameSuffix, simpleDateFormat.format(new Date())));

            if (timeStampBackupFile.exists()) {
                timeStampBackupFile.delete();
            }
            FileUtils.copyFile(fileToCopy, timeStampBackupFile);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }


    /**
     * Compares two version strings, e.g. "1.3.1" "1.3.2" etc. Non-numeric sub-versions are permitted provided they are equal in both strings
     *
     * @param versionString1
     * @param versionString2
     * @return 1 if version1 is greater than version2, -1 if version1 is less than version 2, or 0 if they are equal
     */
    public static int compareVersionStrings(final String versionString1, final String versionString2) {
        final String[] versionNumbers1 = StringUtils.isBlank(versionString1) ? new String[]{} : versionString1.split("\\.", -1);
        final String[] versionNumbers2 = StringUtils.isBlank(versionString2) ? new String[]{} : versionString2.split("\\.", -1);

        int compareIndex = 0;
        while (compareIndex < versionNumbers1.length && compareIndex < versionNumbers2.length) {
            final String subString1 = versionNumbers1[compareIndex];
            final String subString2 = versionNumbers2[compareIndex];
            if (subString1.equals(subString2)) {
                compareIndex++;
            } else {
                return Integer.signum(Integer.valueOf(subString1).compareTo(Integer.valueOf(subString2)));
            }
        }

        // If we get here it means the strings are equal or they have different numbers of substrings
        return Integer.signum(versionNumbers1.length - versionNumbers2.length);
    }

    /**
     * Runs a given method on the EDT, which may or may not be the current thread
     *
     * @param runnable
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public static void runNowOnEdt(final Runnable runnable) throws InterruptedException, InvocationTargetException {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            java.awt.EventQueue.invokeAndWait(runnable);
        }
    }

    /**
     * Runs a given method on the EDT, which may or may not be the current thread. If it is not the current thread, the call will be asynchronous
     *
     * @param runnable
     */
    public static void runLaterOnEdt(final Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            java.awt.EventQueue.invokeLater(runnable);
        }
    }

}
