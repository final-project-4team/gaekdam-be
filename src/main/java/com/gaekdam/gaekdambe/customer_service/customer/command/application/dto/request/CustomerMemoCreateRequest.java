package com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomerMemoCreateRequest {

    @NotBlank
    private String customerMemoContent;
}
