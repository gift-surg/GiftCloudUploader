/**
 * Messages
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 1/5/12 by rherri01
 */
package uk.ac.ucl.cs.cmic.giftcloud.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {
    public static final String DEFAULT_TITLE_INPUT = "default.title.input";
    public static final String VOCABULARY_ARCHIVE = "vocabulary.archive";
    public static final String VOCABULARY_PREARCHIVE = "vocabulary.prearchive";
    public static final String UPLOADRESULTPANEL_SUCCESS = "uploadresultpanel.success";
    public static final String UPLOADRESULTPANEL_DEST_LINK = "uploadresultpanel.dest.link";
    public static final String ERROR_TITLE_NOURLSUPPORT = "error.title.nourlsupport";
    public static final String ERROR_MSG_NOURLSUPPORT = "error.msg.nourlsupport";

    public static ResourceBundle getResourceBundle() {
        if (_bundle == null) {
            _bundle = ResourceBundle.getBundle("uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.Messages");
        }
        return _bundle;
    }

    /**
     * Gets a message from the current with the specified key
     * @param key    The key for the resource to be retrieved.
     * @return The requested resource.
     */
    public static String getMessage(String key) {
        return getResourceBundle().getString(key);
    }

    /**
     * Gets a message from the current with the specified key and formats it with the specified values.
     * @param key    The key for the resource to be retrieved.
     * @param args   Objects to be injected into the formatted string.
     * @return The requested resource formatted with the submitted arguments.
     */
    public static String getMessage(String key, Object... args) {
        return String.format(getResourceBundle().getString(key), args);
    }

    /**
     * Sets the application resource bundle to the indicated locale.
     * @param locale    The desired locale.
     */
    public static void setLocale(Locale locale) {
        _bundle = ResourceBundle.getBundle("org.nrg.upload.Messages", locale);
    }

    private static ResourceBundle _bundle;
}
