package fun.mike.crypto.pgp.alpha;

import static fun.mike.map.alpha.Get.populatedString;
import static fun.mike.map.alpha.Get.requiredStringEnum;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PGPCryptoFactory {
    private static final String TYPE_KEY = "pgp.crypto.type";

    private static final String PUBLIC_KEY_RING_PATH_KEY = "pgp.crypto.public.key.ring.path";
    private static final String SECRET_KEY_RING_PATH_KEY = "pgp.crypto.secret.key.ring.path";
    private static final String PASSPHRASE_KEY = "pgp.crypto.passphrase";

    private static final String FILE_BASED_TYPE = "File-based";
    private static final List<String> TYPE_OPTIONS = Arrays.asList(FILE_BASED_TYPE);

    public static PGPCrypto build(Map<String, Object> config) {
        String cryptoType = requiredStringEnum(config, TYPE_KEY, TYPE_OPTIONS);

        if (cryptoType.equalsIgnoreCase(FILE_BASED_TYPE)) {
            String publicKeyRingPath = populatedString(config, PUBLIC_KEY_RING_PATH_KEY);
            String secretKeyRingPath = populatedString(config, SECRET_KEY_RING_PATH_KEY);
            String passphrase = populatedString(config, PASSPHRASE_KEY);
            return new FileBasedPGPCrypto(publicKeyRingPath,
                    secretKeyRingPath,
                    passphrase);
        }

        throw new IllegalStateException("Factory failed to produce PGPCrypto instance.");
    }
}
