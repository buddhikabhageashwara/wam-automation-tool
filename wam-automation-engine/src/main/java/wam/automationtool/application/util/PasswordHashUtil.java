package wam.automationtool.application.util;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public final class PasswordHashUtil {

  // strength 10–12 is common. Higher = slower but stronger.
  private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(12);

  private PasswordHashUtil() {
    // utility class
  }

  /**
   * One-way hashes a raw password using BCrypt. The returned value includes the salt internally.
   *
   * @param rawPassword raw password (plain text)
   * @return BCrypt hash (includes salt)
   */
  public static String hash(final String rawPassword) {
    if (Objects.isNull(rawPassword) || rawPassword.isBlank()) {
      throw new IllegalArgumentException("Password cannot be null/blank");
    }
    return ENCODER.encode(rawPassword);
  }

  /**
   * Verifies a raw password against a stored BCrypt hash.
   *
   * @param rawPassword raw password (plain text)
   * @param storedHash stored BCrypt hash
   * @return true if matches, false otherwise
   */
  public static boolean matches(final String rawPassword, final String storedHash) {
    if (Objects.isNull(rawPassword) || Objects.isNull(storedHash) || storedHash.isBlank()) {
      return false;
    }
    // Avoid exceptions/log noise if DB contains a non-bcrypt value by mistake
    if (!isBcryptHash(storedHash)) {
      log.warn("Password hash verification failed: stored hash is not a BCrypt format");
      return false;
    }
    try {
      return ENCODER.matches(rawPassword, storedHash);
    } catch (final Exception exception) {
      log.warn("Password hash verification failed: {}", exception.getMessage());
      return false;
    }
  }

  private static boolean isBcryptHash(final String hash) {
    if (Objects.isNull(hash) || hash.isBlank()) {
      return false;
    }
    // BCrypt prefixes commonly seen in Spring/Java ecosystems
    return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
  }
}
