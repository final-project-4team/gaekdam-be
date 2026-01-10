package com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerStatusHistory;
import org.springframework.data.repository.CrudRepository;

public interface CustomerStatusHistoryRepository extends CrudRepository<CustomerStatusHistory, Long> {
}
