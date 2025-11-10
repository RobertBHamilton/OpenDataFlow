package com.hamiltonlabs.dataflow.core;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.security.MessageDigest;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;

import java.util.Base64;

/** Encryptor/Decryptor incorporating the currently accepted best practices for strength.
  * It uses GCM with random IV prepended.
  *
  */
public class AESCryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BITS = 128; // GCM tag length in bits
    private static final int IV_LENGTH_BYTES = 12; // GCM IV length in bytes

    
    /** creates a secret key from a password.
      * This derives an AES key using 32 bytes of a hash of the password, which is superior to simply padding the password to correct length.
      * @param password the user supplied password 
      * @return the corresponding secret key
      * @throws NoSuchAlgorithmException if the SHA-512 hash is not supported by the java library used.
      */
    private static SecretKey getSecretKey(String password) throws GeneralSecurityException{
	MessageDigest md = MessageDigest.getInstance( "SHA-512" );
	md.update( password.getBytes() );
	byte[] aMessageDigest = md.digest();
	byte[] aesKey=new byte[32];
	System.arraycopy(aMessageDigest,0,aesKey,0,32);
        return new SecretKeySpec(aesKey, "AES");
     }
    /** Encrypt a string.
      * @param plaintext the string to encrypt.
      * @return the base64 encoded encryption.
      * @throws GeneralSecurityException
    */ 
    public static String encrypt(String plaintext, String password) throws GeneralSecurityException{
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH_BYTES];

        secureRandom.nextBytes(iv);
        SecretKey secretKey = getSecretKey(password);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        byte[] ciphertextWithIV = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, ciphertextWithIV, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, ciphertextWithIV, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(ciphertextWithIV);
    }

    /** Decrypt a string.
      * @param base64Encrypted  the base64 string to decrypt.
      * @return the decrypted string.
    */ 
    public static String decrypt(String base64Encrypted, String password) throws GeneralSecurityException{
        byte[] ciphertextWithIV = Base64.getDecoder().decode(base64Encrypted);
        SecretKey secretKey = getSecretKey(password);
        byte[] iv = new byte[IV_LENGTH_BYTES];
        System.arraycopy(ciphertextWithIV, 0, iv, 0, iv.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

        byte[] encryptedBytes = new byte[ciphertextWithIV.length - iv.length];
        System.arraycopy(ciphertextWithIV, iv.length, encryptedBytes, 0, encryptedBytes.length);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    public static void main(String[] args) throws Exception {
        String plaintext = args[1];
        String password = args[0]; // Replace with your own password

        String encrypted = encrypt(plaintext, password);
        System.out.println("Encrypted: " + encrypted);

        String decrypted = decrypt(encrypted, password);
        System.out.println("Decrypted: " + decrypted);
    }
}

