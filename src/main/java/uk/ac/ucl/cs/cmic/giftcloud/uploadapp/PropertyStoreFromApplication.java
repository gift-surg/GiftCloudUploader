package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.PropertyStore;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
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

    private final GiftCloudUploaderApplicationBase applicationBase;
    private GiftCloudReporter reporter;
    private final Properties properties;
    private Optional<PasswordStore> passwordStore = null;
    private final Map<String, Optional<char[]>> passwords = new HashMap<String, Optional<char[]>>();


    public PropertyStoreFromApplication(final GiftCloudUploaderApplicationBase applicationBase, final GiftCloudReporter reporter) {
        this.applicationBase = applicationBase;
        this.reporter = reporter;
        this.properties = applicationBase.getPropertiesFromApplicationBase();
        try {
            passwordStore = Optional.of(new PasswordStore(new File(System.getProperty("user.home"), ".giftcloudkeys"), "k>9TG*"));
        } catch (Throwable t) {
            reporter.silentLogException(t, "Unable to create or load local password keystore. Passwords will not be saved between sessions");
            passwordStore = Optional.empty();
        }
    }

    @Override
    public String getProperty(final String propertyName) {
        return properties.getProperty(propertyName);
    }

    @Override
    public void setProperty(final String propertyName, final String propertyValue) {
        properties.setProperty(propertyName, propertyValue);
    }

    @Override
    public Optional<char[]> getPassword(final String passwordKey) {
        if (passwordStore.isPresent()) {
            try {
                // Get the password from the keystore
                final char[] password = passwordStore.get().retrieve(passwordKey);

                // Store the password in memory cache
                passwords.put(passwordKey, password.length > 0 ? Optional.of(password) : Optional.<char[]>empty());

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
        applicationBase.storePropertiesToApplicationBase(comment);
    }
}
