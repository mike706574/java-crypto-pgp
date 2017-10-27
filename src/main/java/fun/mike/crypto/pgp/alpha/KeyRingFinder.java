package fun.mike.crypto.pgp.alpha;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;

public class KeyRingFinder {
    public static Optional<PGPPublicKeyRing> findPublicKeyRing(PGPPublicKeyRingCollection coll,
                                                               String userId) {
        List<PGPPublicKeyRing> matchingPublicKeyRings =
                getMatchingPublicKeyRings(coll, userId);

        if (matchingPublicKeyRings.isEmpty()) {
            return Optional.empty();
        }

        if (matchingPublicKeyRings.size() == 1) {
            return Optional.of(matchingPublicKeyRings.get(0));
        }

        String message = String.format("Found %d matching public key rings for user ID \"%s\".",
                matchingPublicKeyRings.size(),
                userId);
        throw new IllegalArgumentException(message);
    }

    private static List<PGPPublicKeyRing> getMatchingPublicKeyRings(PGPPublicKeyRingCollection coll,
                                                                    String userId) {
        try {
            List<PGPPublicKeyRing> publicKeyRings = new LinkedList<>();
            coll.getKeyRings(userId).forEachRemaining(publicKeyRings::add);
            return publicKeyRings;
        } catch (PGPException ex) {
            throw new CryptoException(ex);
        }
    }

    public static Optional<PGPSecretKeyRing> findSecretKeyRing(PGPSecretKeyRingCollection coll,
                                                               String userId) {
        List<PGPSecretKeyRing> matchingSecretKeyRings =
                getMatchingSecretKeyRings(coll, userId);

        if (matchingSecretKeyRings.isEmpty()) {
            return Optional.empty();
        }

        if (matchingSecretKeyRings.size() == 1) {
            return Optional.of(matchingSecretKeyRings.get(0));
        }

        String message = String.format("Found %d matching secret key rings for user ID \"%s\".",
                matchingSecretKeyRings.size(),
                userId);
        throw new IllegalArgumentException(message);
    }

    private static List<PGPSecretKeyRing> getMatchingSecretKeyRings(PGPSecretKeyRingCollection coll,
                                                                    String userId) {
        try {
            List<PGPSecretKeyRing> secretKeyRings = new LinkedList<>();
            coll.getKeyRings(userId, true).forEachRemaining(secretKeyRings::add);
            return secretKeyRings;
        } catch (PGPException ex) {
            throw new CryptoException(ex);
        }
    }

}
