package org.restapp.services;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.inject.Named;
import javax.inject.Singleton;
import net.iharder.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple wrapper around third party password encoder.
 *
 * @author aniket
 */
@Named
@Singleton
public class PasswordEncoder {

  private static final Logger LOG = LoggerFactory.getLogger(PasswordEncoder.class.getSimpleName());

  public static final String DEFAULT_ALGORIGHM = "SHA-512";
  public static final String DEFAULT_ENCODING = "UTF-8";

  /**
   * Encodes the password(plaintext) using SHA-512 hashing algorithm (MUST be available with the VM)
   * using specified salt (which can be null)
   *
   * @param password The password to encode
   * @param salt Optional salt for encoding
   * @return the encoded password
   */
  public String encode(String password, byte[] salt) {
    return encode(password, salt, DEFAULT_ALGORIGHM);
  }

  /**
   * Encodes the specified password(plaintext) using the specified algorithm and salt.
   *
   * @param password The password to encode
   * @param salt Optional salt for encoding
   * @param hashAlgorithm Optional hash algorithm e.g. "SHA". Uses default if not specified
   * @return The encoded password
   */
  public String encode(String password, byte[] salt, String hashAlgorithm) {
    if(hashAlgorithm == null) {
      hashAlgorithm = DEFAULT_ALGORIGHM;
    }
    try {
      MessageDigest mdSha = MessageDigest.getInstance(hashAlgorithm);
      String saltAndPasword = (salt != null ? Base64.encodeBytes(salt) : "") + password;

      mdSha.update(saltAndPasword.getBytes(DEFAULT_ENCODING));
      byte[] out = mdSha.digest();
      String encoded = Base64.encodeBytes(out);
      return encoded;
    }catch(NoSuchAlgorithmException | UnsupportedEncodingException e) {
      throw new ServiceException(e);
    }
  }

  /**
   * Matches the plain text password with the encoded password, using specified salt
   *
   * @param plainTextPassword The plain text password to match
   * @param encodedPassword The existing encoded password
   * @param salt The salt provided for encoding existing password
   * @return true if the passwords match
   */
  public boolean matches(String plainTextPassword, String encodedPassword, byte[] salt) {
    // LOG.debug("\n{}\n{}", encode(plainTextPassword, salt), encodedPassword);
    return encode(plainTextPassword, salt).equals(encodedPassword);
  }
}
