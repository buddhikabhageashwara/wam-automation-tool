/*
 * MIT License
 *
 * Copyright (c) 2024 buddhika bhageashwara alwis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package wam.automationtool.application.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public final class AESEncryptDecryptUtil {

  /**
   * Encrypts a given string using AES encryption algorithm.
   *
   * @param strToEncrypt the string to be encrypted
   * @param encryptDecryptSecretKey the secret key used for encryption
   * @param saltValue the salt value used for key derivation
   * @return the encrypted string in Base64 format, or null if an error occurs during encryption
   */
  public static String encrypt(final String strToEncrypt,
                               final String encryptDecryptSecretKey,
                               final String saltValue) {
    String encryptedString = null;
    try {
      // Declare a byte array for the initialization vector (IV)
      byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
      IvParameterSpec ivspec = new IvParameterSpec(iv);
      // Create factory for secret keys
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      // PBEKeySpec class implements KeySpec interface
      KeySpec spec = new PBEKeySpec(encryptDecryptSecretKey.toCharArray(), saltValue.getBytes(), 65536, 256);
      SecretKey tmp = factory.generateSecret(spec);
      SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
      encryptedString = Base64.getEncoder()
              .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
    } catch (InvalidAlgorithmParameterException
            | InvalidKeyException
            | NoSuchAlgorithmException
            | InvalidKeySpecException
            | BadPaddingException
            | IllegalBlockSizeException
            | NoSuchPaddingException e) {
      log.error("Error occurred during encryption {} ", e);
    }
    return encryptedString;
  }

  /**
   * Decrypts a given string that was encrypted using AES encryption algorithm.
   *
   * @param strToDecrypt the string to be decrypted (in Base64 format)
   * @param encryptDecryptSecretKey the secret key used for decryption
   * @param saltValue the salt value used for key derivation
   * @return the decrypted string, or null if an error occurs during decryption
   */
  public static String decrypt(final String strToDecrypt,
                               final String encryptDecryptSecretKey,
                               final String saltValue) {
    String decryptedString = null;
    try {
      // Declare a byte array for the initialization vector (IV)
      byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
      IvParameterSpec ivspec = new IvParameterSpec(iv);
      // Create factory for secret keys
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      // PBEKeySpec class implements KeySpec interface
      KeySpec spec = new PBEKeySpec(encryptDecryptSecretKey.toCharArray(), saltValue.getBytes(), 65536, 256);
      SecretKey tmp = factory.generateSecret(spec);
      SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
      decryptedString = new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    } catch (InvalidAlgorithmParameterException
            | InvalidKeyException
            | NoSuchAlgorithmException
            | InvalidKeySpecException
            | BadPaddingException
            | IllegalBlockSizeException
            | NoSuchPaddingException e) {
      log.error("Error occurred during decryption {} ", e);
    }
    return decryptedString;
  }
}