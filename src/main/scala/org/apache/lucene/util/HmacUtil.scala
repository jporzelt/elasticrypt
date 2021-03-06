/*
 * Copyright 2017 Workday, Inc.
 *
 * This software is available under the MIT license.
 * Please see the LICENSE.txt file in this project.
 */

package org.apache.lucene.util

import java.security.InvalidKeyException
import java.util.concurrent.ThreadLocalRandom
import javax.crypto.{Mac, SecretKey}

/**
  * Exception thrown by HMAC Util functions.
  * @param message the message attached to the exception, defaults to null
  * @param cause the reason for the thrown exception, defaults to null
  */
class CryptoException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)

/**
  * Utility functions used in the HmacFileHeader class.
  */
object HmacUtil {
  private[this] val HMAC_ERROR_PREFIX: String = "HMAC Error - "
  val DATA_CIPHER_ALGORITHM = "AES"
  val DATA_KEY_SIZE = 256
  val HMAC_SHA256_ALGORITHM = "HmacSHA256"

  /**
    * Returns a Base64-encoded HMAC of the given plaintext bytes using the provided SecretKey.
    * @param plaintext data to be encrypted
    * @param key encryption key
    */
  def hmacValue(plaintext: Array[Byte], key: SecretKey): Array[Byte] = {
    try {
      // Validate parameters
      if (Option(plaintext).isEmpty || plaintext.length == 0) {
        error("No plaintext was provided")
      }
      if (Option(key).isEmpty) {
        error("Null key")
      }

      // Validate we've received an AES key
      if (!key.getAlgorithm.equalsIgnoreCase(DATA_CIPHER_ALGORITHM)) {
        error(s"Expected key of type $DATA_CIPHER_ALGORITHM, but found: ${key.getAlgorithm}")
      }

      // Validate key size is at least 256 bits
      // See https://docs.oracle.com/javase/7/docs/api/javax/crypto/SecretKey.html
      // Keys that implement this interface return the string RAW as their encoding format (see getFormat()),
      // and return the raw key bytes as the result of a getEncoded method call.
      val keySize: Int = key.getEncoded.length * 8
      if (keySize < DATA_KEY_SIZE) {
        error(s"Key is too small. Expected at least $DATA_KEY_SIZE bits but found: $keySize")
      }

      val hmac = Mac.getInstance(HMAC_SHA256_ALGORITHM)
      hmac.init(key)
      hmac.doFinal(plaintext)
    } catch {
      case e: InvalidKeyException => error("InvalidKeyException", Some(e))
    }
  }

  /**
    * Throw exception based on the error.
    * @param msg message to be sent with the exception
    * @param ex exception to be thrown
    */
  private[this] def error(msg: String, ex: Option[Throwable] = None) = ex match {
    case Some(e) => throw new CryptoException(s"$HMAC_ERROR_PREFIX$msg", e)
    case None => throw new CryptoException(s"$HMAC_ERROR_PREFIX$msg")
  }

  /**
    * Generate and return random bytes.
    * @param numBytes number of random bytes wanted
    */
  def generateRandomBytes(numBytes: Int): Array[Byte] = {
    val randomBytes: Array[Byte] = new Array[Byte](numBytes)
    ThreadLocalRandom.current().nextBytes(randomBytes)
    randomBytes
  }
}
