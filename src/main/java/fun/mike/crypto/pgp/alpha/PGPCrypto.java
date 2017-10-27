package fun.mike.crypto.pgp.alpha;

import java.io.InputStream;
import java.io.OutputStream;

public interface PGPCrypto {
    public void encrypt(String source, String dest, String recipient);

    public void encrypt(InputStream unencryptedStream,
                        OutputStream encryptedStream,
                        String recipient);

    public void decrypt(String source, String dest);

    public InputStream decrypt(InputStream encryptedStream);

    public void decrypt(InputStream encryptedStream, OutputStream decryptedStream);
}
