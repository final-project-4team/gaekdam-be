package com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSenderPhone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageSenderPhoneRepository
        extends JpaRepository<MessageSenderPhone, Long> {

    Optional<MessageSenderPhone> findByHotelGroupCodeAndActiveTrue(Long hotelGroupCode);

    List<MessageSenderPhone> findAllByHotelGroupCodeOrderBySenderPhoneCodeAsc(Long hotelGroupCode);
}

