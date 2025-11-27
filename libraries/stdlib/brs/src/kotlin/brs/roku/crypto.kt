/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin.brs.roku

import kotlin.brs.*

/**
 * BrightScript roEVPDigest - cryptographic hash functions.
 */
@BrsExternal
public external class RoEVPDigest : RoInterface {
    public constructor()

    /**
     * Initializes the digest with specified algorithm.
     * @param algorithm One of: "md5", "sha1", "sha224", "sha256", "sha384", "sha512"
     * @return True if successful.
     */
    public fun setup(algorithm: String): Boolean

    /**
     * Reinitializes the digest for reuse.
     * @return True if successful.
     */
    public fun reinit(): Boolean

    /**
     * Updates the digest with data.
     * @param data The data to hash.
     * @return True if successful.
     */
    public fun update(data: RoByteArray): Boolean

    /**
     * Updates the digest with string data.
     * @param data The string data to hash.
     * @return True if successful.
     */
    public fun update(data: String): Boolean

    /**
     * Finalizes and returns the digest as a byte array.
     * @return The hash result.
     */
    public fun final(): RoByteArray

    /**
     * Computes and returns digest as hex string in one call.
     * @param data The data to hash.
     * @return The hex-encoded hash.
     */
    public fun process(data: RoByteArray): String

    /**
     * Computes and returns digest as hex string in one call.
     * @param data The string data to hash.
     * @return The hex-encoded hash.
     */
    public fun process(data: String): String
}

/**
 * BrightScript roEVPCipher - symmetric encryption/decryption.
 */
@BrsExternal
public external class RoEVPCipher : RoInterface {
    public constructor()

    /**
     * Sets up cipher for encryption or decryption.
     * @param encrypt True for encryption, false for decryption.
     * @param format Cipher format (e.g., "aes-128-cbc", "aes-256-cbc").
     * @param key The encryption key.
     * @param iv The initialization vector.
     * @param padding 0 for no padding, 1 for PKCS7 padding.
     * @return 0 on success.
     */
    public fun setup(encrypt: Boolean, format: String, key: String, iv: String, padding: Int): Int

    /**
     * Reinitializes the cipher for reuse with same parameters.
     * @return 0 on success.
     */
    public fun reinit(): Int

    /**
     * Processes data through the cipher.
     * @param data The data to encrypt/decrypt.
     * @return The processed data.
     */
    public fun process(data: RoByteArray): RoByteArray

    /**
     * Finalizes the cipher operation.
     * @return Any remaining processed data.
     */
    public fun final(): RoByteArray

    /**
     * Updates cipher with additional data.
     * @param data The data to process.
     * @return The processed data so far.
     */
    public fun update(data: RoByteArray): RoByteArray
}

/**
 * BrightScript roHMAC - Hash-based Message Authentication Code.
 */
@BrsExternal
public external class RoHMAC : RoInterface {
    public constructor()

    /**
     * Sets up HMAC with specified algorithm and key.
     * @param algorithm One of: "md5", "sha1", "sha256"
     * @param key The secret key.
     * @return True on success.
     */
    public fun setup(algorithm: String, key: RoByteArray): Boolean

    /**
     * Reinitializes for reuse.
     * @return True on success.
     */
    public fun reinit(): Boolean

    /**
     * Updates HMAC with data.
     * @param data The data to authenticate.
     */
    public fun update(data: RoByteArray)

    /**
     * Updates HMAC with string data.
     * @param data The string data to authenticate.
     */
    public fun update(data: String)

    /**
     * Finalizes and returns the HMAC.
     * @return The HMAC result.
     */
    public fun final(): RoByteArray

    /**
     * Computes HMAC in one call.
     * @param data The data to authenticate.
     * @return The HMAC result.
     */
    public fun process(data: RoByteArray): RoByteArray
}

/**
 * BrightScript roRSA - RSA public key cryptography.
 */
@BrsExternal
public external class RoRSA : RoInterface {
    public constructor()

    /**
     * Sets the digest algorithm for signing.
     * @param algorithm The digest algorithm (e.g., "sha256").
     * @return 0 on success.
     */
    public fun setDigestAlgorithm(algorithm: String): Int

    /**
     * Imports a private key from PEM format.
     * @param privateKeyPEM The private key in PEM format.
     * @return 0 on success.
     */
    public fun setPrivateKey(privateKeyPEM: String): Int

    /**
     * Imports a public key from PEM format.
     * @param publicKeyPEM The public key in PEM format.
     * @return 0 on success.
     */
    public fun setPublicKey(publicKeyPEM: String): Int

    /**
     * Signs data with the private key.
     * @param data The data to sign.
     * @return The signature.
     */
    public fun sign(data: RoByteArray): RoByteArray

    /**
     * Verifies a signature with the public key.
     * @param data The original data.
     * @param signature The signature to verify.
     * @return 0 if signature is valid.
     */
    public fun verify(data: RoByteArray, signature: RoByteArray): Int

    /**
     * Encrypts data with the public key.
     * @param data The data to encrypt.
     * @return The encrypted data.
     */
    public fun encrypt(data: RoByteArray): RoByteArray

    /**
     * Decrypts data with the private key.
     * @param data The data to decrypt.
     * @return The decrypted data.
     */
    public fun decrypt(data: RoByteArray): RoByteArray
}
