package fun.mike.crypto.pgp.alpha;

import static fun.mike.map.alpha.Factory.mapOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PGPCryptoTest {
    private PGPCrypto crypto;

    @Before
    public void setUp() {
        CryptoConfigurator.configure();

        IO.nuke("work/");
        IO.mkdir("work/");

        Map<String, Object> config = mapOf("pgp.crypto.type", "File-based",
                "pgp.crypto.public.key.ring.path", "test-public.gpg",
                "pgp.crypto.secret.key.ring.path", "test-secret.gpg",
                "pgp.crypto.passphrase", "crypto-pgp");

        crypto = PGPCryptoFactory.build(config);
    }

    @After
    public void tearDown() {
        IO.nuke("work/");
    }

    @Test
    public void roundTrip() throws Exception {
        IO.copy("foo.txt", "work/foo.txt");

        final String passphrase = "crypto-pgp";

        crypto.encrypt("work/foo.txt", "work/foo.pgp", "crypto-pgp");

        assertTrue(IO.exists("work/foo.pgp"));

        crypto.decrypt("work/foo.pgp", "work/foo.txt.2");

        assertEquals(IO.slurp("work/foo.txt"),
                IO.slurp("work/foo.txt.2"));
    }
}
