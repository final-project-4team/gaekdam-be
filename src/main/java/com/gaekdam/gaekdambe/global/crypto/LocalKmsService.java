package com.gaekdam.gaekdambe.global.crypto;

import com.gaekdam.gaekdambe.global.config.security.CryptoConfig;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

//로컬 개발/테스트용 KMS Mock 구현체
//실제 AWS KMS를 호출하지 않고, application.yml에 설정된 마스터 키를 사용하여데이터 키를 암호화/복호화하는 척 시뮬레이션합니다.
// 이 클래스는  프로덕션 환경에서 사용하면 안됨
@Service
public class LocalKmsService implements KmsService {

    private final byte[] localMasterKey;

    private final SecureRandom secureRandom = new SecureRandom();

    // CryptoConfig를 통해 마스터 키를 주입받습니다.
    public LocalKmsService(CryptoConfig cryptoConfig) {
        this.localMasterKey = cryptoConfig.getLocalKek().getKeyBytes();

    }

    @Override
    public DataKey generateDataKey() {
        // 256비트(32바이트) 평문 데이터 키 생성
        byte[] plaintextKey = new byte[32];
        secureRandom.nextBytes(plaintextKey);

        //  임시 마스터 키로 데이터 키 암호화
        byte[] encryptedKey = encrypt(plaintextKey, localMasterKey);

        return new DataKey(plaintextKey, encryptedKey);
    }

    @Override
    public byte[] decryptDataKey(byte[] encryptedDataKey) {
        // 임시 마스터 키로 데이터 키 복호화
        return decrypt(encryptedDataKey, localMasterKey);
    }

    private byte[] encrypt(byte[] data, byte[] key) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("KMS Mock Encryption failed", e);
        }
    }

    private byte[] decrypt(byte[] encryptedData, byte[] key) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("KMS Mock Decryption failed", e);
        }
    }
}
