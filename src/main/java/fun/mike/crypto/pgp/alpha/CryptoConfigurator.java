package fun.mike.crypto.pgp.alpha;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoConfigurator {
    private static final Logger log = LoggerFactory.getLogger(CryptoConfigurator.class);

    public static void configure() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            log.trace("Adding Bouncy Castle provider.");
            Security.addProvider(new BouncyCastleProvider());
        } else {
            log.trace("Bouncy Castle provider already added.");
        }
    }
}
