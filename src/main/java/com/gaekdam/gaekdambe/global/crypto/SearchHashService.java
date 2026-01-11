package com.gaekdam.gaekdambe.global.crypto;

public interface SearchHashService {
  byte[] emailHash(String email);
  byte[] phoneHash(String phone);
  byte[] nameHash(String name);
}
