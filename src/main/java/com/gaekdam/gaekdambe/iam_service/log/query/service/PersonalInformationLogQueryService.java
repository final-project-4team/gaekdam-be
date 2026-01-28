package com.gaekdam.gaekdambe.iam_service.log.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.PersonalInformationLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.PersonalInformationLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.mapper.PersonalInformationLogMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalInformationLogQueryService {
    private final PersonalInformationLogMapper personalInformationLogMapper;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final KmsService kmsService;
    private final com.gaekdam.gaekdambe.global.crypto.SearchHashService searchHashService;

    public PageResponse<PersonalInformationLogQueryResponse> getPersonalInformationLogs(
            Long hotelGroupCode,
            PageRequest page,
            PersonalInformationLogSearchRequest search,
            SortRequest sort) {

        byte[] accessorNameHash = (search.employeeAccessorName() != null)
                ? searchHashService.nameHash(search.employeeAccessorName())
                : null;

        byte[] targetNameHash = (search.targetName() != null)
                ? searchHashService.nameHash(search.targetName())
                : null;

        List<PersonalInformationLogQueryResponse> list = personalInformationLogMapper
                .findPersonalInformationLogs(hotelGroupCode, page, search, accessorNameHash, targetNameHash, sort);

        List<PersonalInformationLogQueryResponse> decryptedList = list.stream()
                .map(this::decryptNames)
                .toList();

        long total = personalInformationLogMapper.countPersonalInformationLogs(hotelGroupCode, search,
                accessorNameHash, targetNameHash);

        return new PageResponse<>(
                decryptedList,
                page.getPage(),
                page.getSize(),
                total);
    }

    private PersonalInformationLogQueryResponse decryptNames(PersonalInformationLogQueryResponse dto) {
        String accessorName = decryptEmployeeName(dto.employeeAccessorCode(), dto.employeeAccessorName());
        String targetName = decryptTargetName(dto);

        return new PersonalInformationLogQueryResponse(
                dto.personalInformationLogCode(),
                dto.occurredAt(),
                dto.permissionTypeKey(),
                dto.employeeAccessorCode(),
                MaskingUtils.maskName(accessorName),
                dto.employeeAccessorLoginId(),
                dto.targetType(),
                dto.targetCode(),
                MaskingUtils.maskName(targetName),
                dto.purpose());
    }

    private String decryptEmployeeName(Long employeeCode, String fallbackName) {
        if (employeeCode == null)
            return fallbackName;
        try {
            Employee employee = employeeRepository.findById(employeeCode).orElse(null);
            if (employee == null)
                return fallbackName;
            byte[] plaintextDek = kmsService.decryptDataKey(employee.getDekEnc());
            return AesCryptoUtils.decrypt(employee.getEmployeeNameEnc(), plaintextDek);
        } catch (Exception e) {
            return fallbackName;
        }
    }

    private String decryptTargetName(PersonalInformationLogQueryResponse dto) {
        if ("EMPLOYEE".equals(dto.targetType())) {
            return decryptEmployeeName(dto.targetCode(), dto.targetName());
        } else if ("CUSTOMER".equals(dto.targetType())) {
            return decryptCustomerName(dto.targetCode(), dto.targetName());
        }
        return dto.targetName();
    }

    private String decryptCustomerName(Long customerCode, String fallbackName) {
        if (customerCode == null)
            return fallbackName;
        try {
            Customer customer = customerRepository.findById(customerCode).orElse(null);
            if (customer == null)
                return fallbackName;
            byte[] plaintextDek = kmsService.decryptDataKey(customer.getDekEnc());
            return AesCryptoUtils.decrypt(customer.getCustomerNameEnc(), plaintextDek);
        } catch (Exception e) {
            return fallbackName;
        }
    }
}
