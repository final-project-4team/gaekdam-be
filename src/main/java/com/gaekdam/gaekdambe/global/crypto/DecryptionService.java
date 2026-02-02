package com.gaekdam.gaekdambe.global.crypto;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

//  첫 조회: KMS로 DEK 복호화 → 캐시 저장 → 데이터 복호화
// 재조회: 캐시(customerCode,employeeCode등 식별용)에서 DEK 조회 (KMS 호출 X) → 데이터 복호화

@Service
@RequiredArgsConstructor
public class DecryptionService {

    private final KmsService kmsService;

    // DEK 캐시: employeeCode → 복호화된 평문 DEK
    private final Cache<Long, byte[]> dekCache = Caffeine.newBuilder()
            .maximumSize(500_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();

    public byte[] getPlaintextDek(Long cacheCode, byte[] encryptedDek) {
        return dekCache.get(cacheCode, key -> {
            //System.out.println("[DEK Cache Miss] KMS 호출 - employeeCode=" + cacheCode);
            return kmsService.decryptDataKey(encryptedDek);
        });
    }

    // 일반적인 복호화 메서드
  //cacheCode에 직원 코드,고객 코드 등 데이터를 식별할 수 있는 고유한 값 넣기
    public String decrypt(Long cacheCode, byte[] dekEnc, byte[] encryptedData) {
        if (encryptedData == null)
            return null;
        byte[] plaintextDek = getPlaintextDek(cacheCode, dekEnc);
        return AesCryptoUtils.decrypt(encryptedData, plaintextDek);
    }


    // 캐시 초기화
    public void clearCache() {
        dekCache.invalidateAll();
        System.out.println("[DEK Cache] 전체 캐시 삭제됨");
    }
}
