package eu.addicted2random.a2rclient.osc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OSC address utility methods.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Address {

  /**
   * Pattern to check for reserved characters in OSC address string.
   */
  static public final Pattern RESERVED_TOKEN_CHARACTERS = Pattern.compile("[\\#\\*,\\?\\[\\]{}\\s/]");

  /**
   * Pattern to check if an OSC address is a pattern.
   */
  static public final Pattern IS_PATTERN = Pattern.compile("//|\\?|\\*|\\[*\\]|\\{.*\\}");

  /**
   * Check if a given token is a valid OSC address token.
   * 
   * @param token
   * @return
   */
  static public boolean isValidToken(String token) {
    if (token.length() == 0)
      return false;

    Matcher matcher = RESERVED_TOKEN_CHARACTERS.matcher(token);
    return !matcher.find();
  }

  /**
   * Check if an OSC address looks like a pattern.
   * 
   * @param address
   * @return
   */
  static public boolean isPattern(String address) {
    Matcher matcher = IS_PATTERN.matcher(address);
    return matcher.find();
  }

  /**
   * Check if an OSC address is a valid. This will return false if an address is
   * a pattern.
   * 
   * @param address
   * @return
   */
  static public boolean isValidAddress(String address) {
    return address.startsWith("/") && !isPattern(address);
  }

  /**
   * Check if an OSC address is a pattern.
   * 
   * @param pattern
   * @return
   */
  static public boolean isValidPattern(String pattern) {
    return pattern.startsWith("/") && isPattern(pattern);
  }
}
