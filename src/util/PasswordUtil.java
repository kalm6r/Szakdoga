package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Jelszó hashelés és ellenőrzés segédosztály
 * SHA-256 hashelést használ salt-tal
 */
public class PasswordUtil {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * Jelszó hashelése véletlenszerű salt-tal
     * @param password A nyers jelszó
     * @return A hashelt jelszó (salt + hash, Base64 kódolva)
     */
    public static String hashPassword(String password) {
        try {
            // Véletlenszerű salt generálása
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash generálása
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // Salt és hash összefűzése
            byte[] saltAndHash = new byte[SALT_LENGTH + hashedPassword.length];
            System.arraycopy(salt, 0, saltAndHash, 0, SALT_LENGTH);
            System.arraycopy(hashedPassword, 0, saltAndHash, SALT_LENGTH, hashedPassword.length);
            
            // Base64 kódolás
            return Base64.getEncoder().encodeToString(saltAndHash);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Nem található a hash algoritmus: " + HASH_ALGORITHM, e);
        }
    }
    
    /**
     * Jelszó ellenőrzése a tárolt hash-sel szemben
     * @param password A beírt jelszó
     * @param storedHash A tárolt hashelt jelszó
     * @return true, ha egyezik; false egyébként
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Base64 dekódolás
            byte[] saltAndHash = Base64.getDecoder().decode(storedHash);
            
            // Salt kinyerése
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(saltAndHash, 0, salt, 0, SALT_LENGTH);
            
            // Hash generálása a beírt jelszóból ugyanazzal a salt-tal
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // Összehasonlítás
            if (hashedPassword.length != saltAndHash.length - SALT_LENGTH) {
                return false;
            }
            
            for (int i = 0; i < hashedPassword.length; i++) {
                if (hashedPassword[i] != saltAndHash[SALT_LENGTH + i]) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Egyszerű plaintext jelszó migráció hash-elt verzióra
     * Használd ezt, ha átállsz a régi plaintext jelszavakról
     */
    public static boolean isPlaintext(String password) {
        // Ha nem Base64 formátumú, akkor plaintext
        try {
            Base64.getDecoder().decode(password);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}