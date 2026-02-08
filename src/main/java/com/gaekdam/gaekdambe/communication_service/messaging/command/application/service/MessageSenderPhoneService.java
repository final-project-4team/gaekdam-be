package com.gaekdam.gaekdambe.communication_service.messaging.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageSenderPhoneCreateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSenderPhone;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSenderPhoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageSenderPhoneService {

    private final MessageSenderPhoneRepository repository;

    @Transactional(readOnly = true)
    public List<MessageSenderPhone> findAll(Long hotelGroupCode) {
        return repository.findAllByHotelGroupCodeOrderBySenderPhoneCodeAsc(hotelGroupCode);
    }




    public MessageSenderPhone getActiveSender(Long hotelGroupCode) {
        return repository.findByHotelGroupCodeAndActiveTrue(hotelGroupCode)
                .orElseThrow(() ->
                        new IllegalStateException("활성화된 발신번호가 없습니다.")
                );
    }




    public void activate(Long hotelGroupCode, Long senderPhoneCode) {

        // 기존 active 비활성화
        repository.findByHotelGroupCodeAndActiveTrue(hotelGroupCode)
                .ifPresent(MessageSenderPhone::deactivate);

        // 선택한 번호 활성화
        MessageSenderPhone phone = repository.findById(senderPhoneCode)
                .orElseThrow(() -> new IllegalArgumentException("발신번호 없음"));

        if (!phone.getHotelGroupCode().equals(hotelGroupCode)) {
            throw new IllegalArgumentException("다른 호텔 그룹의 발신번호입니다.");
        }

        phone.activate();
    }



    public void create(Long hotelGroupCode, MessageSenderPhoneCreateRequest request) {

        MessageSenderPhone phone = MessageSenderPhone.builder()
                .hotelGroupCode(hotelGroupCode)
                .phoneNumber(request.getPhoneNumber())
                .label(request.getLabel())
                .active(false) // 처음엔 비활성
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(phone);
    }
}

