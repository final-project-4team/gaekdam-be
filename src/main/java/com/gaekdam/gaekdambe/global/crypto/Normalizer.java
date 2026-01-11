package com.gaekdam.gaekdambe.global.crypto;

public final class Normalizer {
  public static String email(String v) {
    return v == null ? null : v.trim().toLowerCase(java.util.Locale.ROOT);
  }
  public static String phone(String v) {
    return v == null ? null : v.replaceAll("[^0-9]", "");
  }
  public static String name(String v) {
    return v == null ? null : v.trim().replaceAll("\\s+", " ");
  }
}
