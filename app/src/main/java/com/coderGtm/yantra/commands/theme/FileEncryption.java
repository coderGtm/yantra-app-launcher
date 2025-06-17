package com.coderGtm.yantra.commands.theme;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class FileEncryption {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 16;
    private static final int SALT_LENGTH = 16;
    private static final int ITERATION_COUNT = 100000;

    public static void encryptTextToFile(String text, File outputFile, String password) throws Exception {
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        byte[] iv = generateRandomBytes(IV_LENGTH);
        SecretKey key = deriveKeyFromPassword(password, salt);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(salt);
            fos.write(iv);
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            fos.write(encrypted);
        }
    }

    public static String decryptFileToText(File inputFile, String password) throws Exception {
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[IV_LENGTH];

            if (fis.read(salt) != SALT_LENGTH || fis.read(iv) != IV_LENGTH) {
                throw new IllegalArgumentException("Invalid file format");
            }

            SecretKey key = deriveKeyFromPassword(password, salt);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                decryptedStream.write(buffer, 0, bytesRead);
            }

            byte[] decryptedData = cipher.doFinal(decryptedStream.toByteArray());
            return new String(decryptedData, StandardCharsets.UTF_8);
        }
    }

    private static SecretKey deriveKeyFromPassword(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
