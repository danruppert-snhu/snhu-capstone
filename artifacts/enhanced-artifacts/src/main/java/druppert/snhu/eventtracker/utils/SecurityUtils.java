package druppert.snhu.eventtracker.utils;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

/**
 * SecurityUtils provides cryptographic utilities for password hashing and salt generation.
 *
 * Uses PBKDF2 with HMAC-SHA1 for secure password hashing
 * Increases security with 100,000 iterations (up from 65,000)
 * Encodes all outputs in Base64 to support safe storage and comparison
 * Prevents instantiation with a private constructor
 */
public class SecurityUtils {

    private SecurityUtils() {}
    /**
     * Generates a cryptographically secure random salt and encodes it in Base64.
     * CS-499
     * Enhances password protection by introducing random salt per user
     * @return A Base64-encoded random salt string
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[Constants.SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password using PBKDF2 with HMAC-SHA1 and a user-specific salt.
     *
     * CS-499
     * Applies 100,000 iterations for stronger brute-force resistance
     * Derives a fixed-length key using PBKDF2
     *
     * @param password Plaintext password to hash
     * @param salt Base64-encoded salt string
     * @return Base64-encoded hashed password
     * @throws Exception If the hashing algorithm fails
     */
    public static String hashPassword(String password, String salt) throws Exception {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, Constants.HASH_ITERATIONS, Constants.KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(Constants.KEY_DERIVATION_ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }
}
