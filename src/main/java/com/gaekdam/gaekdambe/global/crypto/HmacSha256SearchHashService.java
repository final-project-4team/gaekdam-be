package com.gaekdam.gaekdambe.global.crypto;

import com.gaekdam.gaekdambe.global.config.security.CryptoConfig;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class HmacSha256SearchHashService implements SearchHashService {
  private final SecretKeySpec keySpec;

  public HmacSha256SearchHashService(CryptoConfig cryptoConfig) {
    byte[] pepperSecret = cryptoConfig.getHmac().getPepperBytes();
    this.keySpec = new SecretKeySpec(pepperSecret, "HmacSHA256");
  }

  private byte[] hmac(String normalized) {
    if (normalized == null)
      return null;
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(keySpec);
      return mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalStateException("HMAC-SHA256 failed", e);
    }
  }

  @Override
  public byte[] emailHash(String email) {
    return hmac(Normalizer.email(email));
  }

  @Override
  public byte[] phoneHash(String phone) {
    return hmac(Normalizer.phone(phone));
  }

  @Override
  public byte[] nameHash(String name) {
    return hmac(Normalizer.name(name));
  }
}
