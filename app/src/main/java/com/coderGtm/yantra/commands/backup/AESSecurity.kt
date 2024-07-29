package com.coderGtm.yantra.commands.backup

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AESSecurity {
    private const val KEY_SIZE = 256
    private const val GCM_NONCE_LENGTH = 12
    private const val GCM_TAG_LENGTH = 16
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val SALT_LENGTH = 16
    private const val ITERATION_COUNT = 65536

    private fun generateKeyFromPassword(password: CharArray, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec: KeySpec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_SIZE)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    fun encryptFile(file: File, password: CharArray, encryptedFile: File) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val nonce = ByteArray(GCM_NONCE_LENGTH)
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(nonce)
        SecureRandom().nextBytes(salt)
        val secretKey = generateKeyFromPassword(password, salt)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        FileInputStream(file).use { inputStream ->
            FileOutputStream(encryptedFile).use { outputStream ->
                outputStream.write(nonce)
                outputStream.write(salt)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val encryptedBytes = cipher.update(buffer, 0, bytesRead)
                    if (encryptedBytes != null) {
                        outputStream.write(encryptedBytes)
                    }
                }
                val finalBytes = cipher.doFinal()
                if (finalBytes != null) {
                    outputStream.write(finalBytes)
                }
            }
        }
    }

    fun decryptFile(encryptedFile: File, password: CharArray, decryptedFile: File): Boolean {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        FileInputStream(encryptedFile).use { inputStream ->
            val nonce = ByteArray(GCM_NONCE_LENGTH)
            val salt = ByteArray(SALT_LENGTH)
            inputStream.read(nonce)
            inputStream.read(salt)
            val secretKey = generateKeyFromPassword(password, salt)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            FileOutputStream(decryptedFile).use { outputStream ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val decryptedBytes = cipher.update(buffer, 0, bytesRead)
                    if (decryptedBytes != null) {
                        outputStream.write(decryptedBytes)
                    }
                }
                val finalBytes = try { cipher.doFinal() } catch (e: Exception) { null }
                if (finalBytes != null) {
                    outputStream.write(finalBytes)
                }
                else {
                    return false
                }
            }
        }
        return true
    }
}
