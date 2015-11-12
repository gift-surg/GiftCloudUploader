package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.utils.FileUtilities;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PropertyStore;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class PropertyStoreFromApplication implements PropertyStore {

    private GiftCloudReporter reporter;
    private Optional<PasswordStore> passwordStore = null;
    private final Map<String, Optional<char[]>> passwords = new HashMap<String, Optional<char[]>>();
    private final Properties applicationProperties;
    private final String applicationPropertyFileName;

    public PropertyStoreFromApplication(final String applicationPropertyFileName, final GiftCloudReporter reporter) {
        this.applicationPropertyFileName = applicationPropertyFileName;
        this.reporter = reporter;
        try {
            passwordStore = Optional.of(new PasswordStore(new File(System.getProperty("user.home"), ".giftcloudkeys"), "k>9TG*"));
        } catch (Throwable t) {
            reporter.silentLogException(t, "Unable to create or load local password keystore. Passwords will not be saved between sessions");
            passwordStore = Optional.empty();
        }

        applicationProperties = new Properties();
        try {
            if (applicationPropertyFileName != null) {
                String whereFrom = makePathToFileInUsersHomeDirectory(applicationPropertyFileName);
                FileInputStream in = new FileInputStream(whereFrom);
                applicationProperties.load(in);
                in.close();
            }
        } catch (IOException e) {
            reporter.silentLogException(e, "Unable to load properties");
        }
    }

    @Override
    public String getProperty(final String propertyName) {
        return applicationProperties.getProperty(propertyName);
    }

    @Override
    public void setProperty(final String propertyName, final String propertyValue) {
        applicationProperties.setProperty(propertyName, propertyValue);
    }

    @Override
    public Optional<char[]> getPassword(final String passwordKey) {
        if (passwordStore.isPresent()) {
            try {
                // First check if the required key is in the keystore
                if (passwordStore.get().containsKey(passwordKey)) {

                    // Get the password from the keystore
                    final char[] password = passwordStore.get().retrieve(passwordKey);

                    // Store the password in memory cache
                    passwords.put(passwordKey, password.length > 0 ? Optional.of(password) : Optional.<char[]>empty());
                }

            } catch (InvalidKeySpecException e) {
                reporter.silentLogException(e, "Failure when accessing local keystore");
            } catch (UnrecoverableEntryException e) {
                reporter.silentLogException(e, "Failure when accessing local keystore");
            } catch (NoSuchAlgorithmException e) {
                reporter.silentLogException(e, "Failure when accessing local keystore");
            } catch (KeyStoreException e) {
                reporter.silentLogException(e, "Failure when accessing local keystore");
            }
        }

        // Get the password from the memory cache - the the event of a keystore failure, we will at least be able to store passwords for the current session
        if (passwords.containsKey(passwordKey)) {
            return passwords.get(passwordKey);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void setPassword(final String passwordKey, char[] lastPassword) {
        // Store the password in the memory cache, so that we can retrieve it if the keystore fails
        passwords.put(passwordKey, Optional.of(lastPassword));

        // Store the password in the keystore
        if (passwordStore.isPresent()) {
            try {
                passwordStore.get().store(passwordKey, lastPassword);
            } catch (Throwable t) {
                reporter.silentLogException(t, "Failed to store password in the keystore");
            }
        }
    }

    @Override
    public void save(String comment) throws IOException {
        storeProperties(comment);
    }

    private void storeProperties(final String comment) throws IOException {
        if (applicationPropertyFileName == null) {
            throw new IOException("asked to store properties but no applicationPropertyFileName was ever set");
        }
        else {
            String whereTo = makePathToFileInUsersHomeDirectory(applicationPropertyFileName);
            FileOutputStream out = new FileOutputStream(whereTo);
            applicationProperties.store(out,comment);
            out.close();
        }
    }

    private static String makePathToFileInUsersHomeDirectory(final String fileName) {
        return FileUtilities.makePathToFileInUsersHomeDirectory(fileName);
    }
}