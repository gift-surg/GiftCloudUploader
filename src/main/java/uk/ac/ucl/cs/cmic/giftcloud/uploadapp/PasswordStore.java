/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

/**
 * Class for storing passwords using a local keystore
 */
public final class PasswordStore {

    private final KeyStore keyStore;
    private final SecretKeyFactory secretKeyFactory;
    private final File keystorePath;
    private final String keystorePassword;

    public PasswordStore(final File keystorePath, final String keystorePassword) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        keyStore = KeyStore.getInstance("JCEKS");

        if (keystorePath.exists()) {
            keyStore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
        } else {
            keyStore.load(null, keystorePassword.toCharArray());
        }
        secretKeyFactory = SecretKeyFactory.getInstance("PBE");
    }

    public void store(final String key, final char[] password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeySpecException {
        final char[] nonEmptyPassword = password.length > 0 ? password : new char[]{' '};
        SecretKey generatedSecret = secretKeyFactory.generateSecret(new PBEKeySpec(nonEmptyPassword));
        keyStore.setEntry(key, new SecretKeyEntry(generatedSecret), new PasswordProtection(keystorePassword.toCharArray()));
        keyStore.store(new FileOutputStream(keystorePath), keystorePassword.toCharArray());
    }

    public char[] retrieve(final String key) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, InvalidKeySpecException {

        SecretKeyEntry entry = (SecretKeyEntry) keyStore.getEntry(key, new PasswordProtection(keystorePassword.toCharArray()));
        PBEKeySpec keySpec = (PBEKeySpec) secretKeyFactory.getKeySpec(entry.getSecretKey(), PBEKeySpec.class);
        final char[] password = keySpec.getPassword();
        return password.length == 1 && password[0] == ' ' ? new char[]{} : password;
    }

    public boolean containsKey(final String key) throws KeyStoreException {
        return keyStore.containsAlias(key);
    }
}

