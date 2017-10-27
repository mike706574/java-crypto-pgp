package fun.mike.crypto.pgp.alpha;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

import name.neuhalfen.projects.crypto.bouncycastle.openpgp.BouncyGPG;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.algorithms.DefaultPGPAlgorithmSuites;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.encrypting.PGPEncryptingStream;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.PGPUtilities;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.callbacks.KeyringConfigCallbacks;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfig;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfigs;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.util.io.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedPGPCrypto implements PGPCrypto {
    private static final Logger log = LoggerFactory.getLogger(PGPCrypto.class);

    private final String publicKeyRingPath;
    private final String secretKeyRingPath;
    private final String passphrase;

    public FileBasedPGPCrypto(String publicKeyRingPath,
                              String secretKeyRingPath,
                              String passphrase) {
        this.publicKeyRingPath = publicKeyRingPath;
        this.secretKeyRingPath = secretKeyRingPath;
        this.passphrase = passphrase;
    }

    private PGPPublicKey getPublicKey(KeyringConfig config,
                                      String recipient) {
        try {
            PGPPublicKeyRingCollection coll = config.getPublicKeyRings();
            PGPPublicKeyRing publicKeyRing = KeyRingFinder.findPublicKeyRing(coll, recipient)
                    .orElseThrow(() -> new CryptoException(String.format("Failed to find public key ring for recipient \"%s\".",
                            recipient)));
            return PGPUtilities.getEncryptionKey(publicKeyRing);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (PGPException ex) {
            throw new CryptoException(ex);
        }
    }

    private OutputStream getEncryptedOutputStream(OutputStream os,
                                                  String recipient,
                                                  String signer) {
        KeyringConfig config = getConfig();
        try {
            return PGPEncryptingStream.create(config,
                    DefaultPGPAlgorithmSuites.strongSuite(),
                    signer,
                    os,
                    true,
                    getPublicKey(config, recipient));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (PGPException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException ex) {
            throw new CryptoException(ex);
        }
    }

    public void encrypt(String source, String dest, String recipient) {
        try (OutputStream os = Files.newOutputStream(Paths.get(dest));
             InputStream is = Files.newInputStream(Paths.get(source))) {
            encrypt(is, os, recipient);
        } catch (IOException ex) {
            throw new CryptoException(ex);
        }
    }

    public void encrypt(InputStream unencryptedStream,
                        OutputStream encryptedStream,
                        String recipient) {
        try (BufferedOutputStream bos = new BufferedOutputStream(encryptedStream);
             OutputStream eos = getEncryptedOutputStream(bos,
                     recipient,
                     null)) {
            Streams.pipeAll(unencryptedStream, eos);
        } catch (IOException ex) {
            throw new CryptoException(ex);
        }
    }

    public void decrypt(String source, String dest) {
        try (final InputStream is = Files.newInputStream(Paths.get(source));
             final OutputStream os = Files.newOutputStream(Paths.get(dest));
             final BufferedOutputStream bos = new BufferedOutputStream(os);
             final InputStream dis = BouncyGPG.decryptAndVerifyStream()
                     .withConfig(getConfig())
                     .andIgnoreSignatures()
                     .fromEncryptedInputStream(is)) {
            Streams.pipeAll(dis, bos);
        } catch (IOException | NoSuchProviderException ex) {
            throw new CryptoException(ex);
        }
    }

    public InputStream decrypt(InputStream encryptedStream) {
        try {
            return BouncyGPG
                    .decryptAndVerifyStream()
                    .withConfig(getConfig())
                    .andIgnoreSignatures()
                    .fromEncryptedInputStream(encryptedStream);
        } catch (IOException | NoSuchProviderException ex) {
            throw new CryptoException(ex);
        }
    }

    public void decrypt(InputStream encryptedStream, OutputStream decryptedStream) {
        try {
            Streams.pipeAll(decrypt(encryptedStream), decryptedStream);
        } catch (IOException ex) {
            throw new CryptoException(ex);
        }
    }

    private KeyringConfig getConfig() {
        return KeyringConfigs
                .withKeyRingsFromFiles(
                        new File(publicKeyRingPath),
                        new File(secretKeyRingPath),
                        KeyringConfigCallbacks.withPassword(passphrase));
    }
}
