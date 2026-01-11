package com.gaekdam.gaekdambe.global.crypto;

// 임시 키 관리 서비스(KMS) 인터페이스입니다.
public interface KmsService {

    // 새로운 데이터 키(Data Key)를 생성합니다.
    DataKey generateDataKey();

    // 암호화된 데이터 키 복호화 하여 평문키 반환
    // encryptedDataKey DB에 저장된 암호화된 키 eDek
    byte[] decryptDataKey(byte[] encryptedDataKey);
}
