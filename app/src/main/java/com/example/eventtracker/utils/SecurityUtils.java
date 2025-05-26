package com.example.eventtracker.utils;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class SecurityUtils {

    private SecurityUtils() {}

    private static final int SALT_LENGTH = 16;

    //CS-499 - software engineering: Increase PPBKDF2 iteration count from 65k to 100k
    private static final int HASH_ITERATIONS = 100000;
    private static final int KEY_LENGTH = 128;
    /**
     * Generates a secure, random salt encoded in Base64 format.
     * @return Base64-encoded salt string
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password using PBKDF2 with HMAC-SHA1 and a given salt.
     * @param password Plaintext password to hash
     * @param salt Base64-encoded salt
     * @return Base64-encoded hashed password
     * @throws Exception If the hashing algorithm fails
     */
    public static String hashPassword(String password, String salt) throws Exception {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, HASH_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }
}
