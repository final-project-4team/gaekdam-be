package com.gaekdam.gaekdambe.global.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

  private final SecretKey key;
  private final long accessTokenValidity;
  private final long refreshTokenValidity;

    public JwtTokenProvider(
            @Value("${jwt.secret-b64}") String secretKeyBase64,
            @Value("${jwt.access-expiration}") long accessTokenValidity,
            @Value("${jwt.refresh-expiration}") long refreshTokenValidity
    ) {

        // 디버그용 (CI에서 값 들어오는지 확인)
        System.out.println("JWT_SECRET_B64 RAW = [" + secretKeyBase64 + "]");

        byte[] decodedKey = Base64.getDecoder().decode(secretKeyBase64);
        this.key = Keys.hmacShaKeyFor(decodedKey);

        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

  // Access Token 생성
  public String createAccessToken(String userId, String role,Long hotelGroupCode,Long propertyCode) {
    return createToken(userId, role, "access",hotelGroupCode,propertyCode,accessTokenValidity);
  }

  // Refresh Token 생성
  public String createRefreshToken(String userId, String role,Long hotelGroupCode,Long propertyCode) {
    return createToken(userId, role, "refresh",hotelGroupCode,propertyCode, refreshTokenValidity);
  }

  // 공통 생성 로직
  private String createToken(String userId, String role, String type,Long hotelGroupCode,Long propertyCode, long validity) {
    Date now = new Date();

    return Jwts.builder()
        .setSubject(userId)  // sub
        .addClaims(Map.of(
            "role", role,
            "type", type,
            "hotelGroupCode", hotelGroupCode,
            "propertyCode", propertyCode
        ))
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + validity))
        .signWith(key)
        .compact();
  }

  // username 추출
  public String getUsername(String token) {
    try {
      return parseClaims(token).getSubject();
    } catch (Exception e) {
      return null;
    }
  }

  //role 추출
  public String getRole(String token) {
    try {
      return parseClaims(token).get("role", String.class);
    } catch (Exception e) {
      return null;
    }
  }

  public Long getHotelGroupCode(String token) {
    try {
      return parseClaims(token).get("hotelGroupCode", Long.class);
    } catch (Exception e) {
      return null;
    }
  }

  public Long getPropertyCode(String token) {
    try {
      return parseClaims(token).get("propertyCode", Long.class);
    } catch (Exception e) {
      return null;
    }
  }

  // tokenType(access/refresh)
  public String getTokenType(String token) {
    try {
      return parseClaims(token).get("type", String.class);
    } catch (Exception e) {
      return null;
    }
  }

  // 토큰 검증
  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // Claims 파싱
  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }


  // 예외 던지기
  public void validateTokenOrThrow(String token) {
    Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token);
  }

}