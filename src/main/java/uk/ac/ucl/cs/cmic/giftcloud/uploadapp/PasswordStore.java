package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.spec.InvalidKeySpecException;

import java.nio.file.Path;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;
import java.security.cert.CertificateException;

public class PasswordStore {

    private final KeyStore keyStore;
    private final SecretKeyFactory secretKeyFactory;
    private final Path keystorePath;
    private final String keystorePassword;

    public PasswordStore(final Path keystorePath, final String keystorePassword) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(Files.newInputStream(keystorePath), keystorePassword.toCharArray());
        secretKeyFactory = SecretKeyFactory.getInstance("PBE");
    }

    public void store(final String key, final char[] password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeySpecException {

        SecretKey generatedSecret = secretKeyFactory.generateSecret(new PBEKeySpec(password));
        keyStore.setEntry(key, new SecretKeyEntry(generatedSecret), new PasswordProtection(keystorePassword.toCharArray()));
        keyStore.store(Files.newOutputStream(keystorePath), keystorePassword.toCharArray());
    }

    public char[] retrieve(final String key) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, InvalidKeySpecException {

        SecretKeyEntry entry = (SecretKeyEntry) keyStore.getEntry(key, new PasswordProtection(keystorePassword.toCharArray()));
        PBEKeySpec keySpec = (PBEKeySpec) secretKeyFactory.getKeySpec(entry.getSecretKey(), PBEKeySpec.class);
        return keySpec.getPassword();
    }
}

