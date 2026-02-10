package com.gaekdam.gaekdambe.global.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public final class AesCryptoUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    private static final SecureRandom secureRandom = new SecureRandom();

    // plaintext : 암호화할 평문 문자열
    // key : 암호화에 사용할 대칭키 (32 bytes for AES-256)
    // 암호화된 데이터 (IV + CipherText, Raw Bytes)
    public static byte[] encrypt(String plaintext, byte[] key) {
        if (plaintext == null)
            return null;

        try {
            // 랜덤 IV 생성
            byte[] iv = new byte[IV_LENGTH_BYTE];
            secureRandom.nextBytes(iv);

            // 키 명세 생성(AES키로 사용되는 것으로 명시하여 객체화)
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            // GCM 모드 설정값 생성
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // 암호화 모드 초기화 및 값 세팅
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            // 평문 문자열-> UTF-8바이트로 변환 및 실제 암호화 수행
            // ciphertext || tag형태
          //Tag= 비밀키,IV,암호문기반
          //tag:무결성 방지용
          //복호화시의 새로 계산한 태그와 암호문 뒤에 붙어있는 코드가 일치하는지 검사(불일치 시  코드 바뀐것 ,무결성)
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // 결과 병합 (IV + CipherText)
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            //최종 : (IV+CipherText+Tag)
            return combined;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    // encryptedBytes: 암호화된 데이터 (IV + CipherText)
    // key: 복호화에 사용할 대칭키
    public static String decrypt(byte[] encryptedBytes, byte[] key) {
        if (encryptedBytes == null)
            return null;

        try {
            // IV 추출
            // GCMParameterSpec : AES-GCM 모드에서 필요한 (IV/Nonce + 태그 길이) 를 담는 객체
            // 128비트 태그 길이,encryptedBytes 배열의 0번부터 IV_LENGTH_BYTE 길이만큼 IV로 사용
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, encryptedBytes, 0, IV_LENGTH_BYTE);

            // 키 명세 생성
            SecretKey secretKey = new SecretKeySpec(key, "AES");

            // 복호화 준비
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // IV || ciphertext+tag
            // IV이후의 실제 암호문 복호화
            byte[] plainTextBytes = cipher.doFinal(encryptedBytes, IV_LENGTH_BYTE,
                    encryptedBytes.length - IV_LENGTH_BYTE);

            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
