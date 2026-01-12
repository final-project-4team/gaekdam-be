package com.gaekdam.gaekdambe.global.crypto;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class RandomPassword {

  private static final int PASSWORD_LENGTH=16;
  private static final char[] RND_ALL_CHARS = new char[]{
      //number
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      //uppercase
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      //lowercase
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      //special symbols
      '@', '$', '!', '%', '*', '?', '&'
  };
  public String getRandomPassword() {
    SecureRandom random = new SecureRandom();
    StringBuilder stringBuilder = new StringBuilder();

    int rndAllCharactersLength = RND_ALL_CHARS.length;
    for (int i = 0; i < PASSWORD_LENGTH; i++) {
      stringBuilder.append(RND_ALL_CHARS[random.nextInt(rndAllCharactersLength)]);
    }
    String randomPassword = stringBuilder.toString();

    String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*";
    if (!Pattern.matches(pattern, randomPassword)) {
      return getRandomPassword();
    }
    return randomPassword;
  }
}
