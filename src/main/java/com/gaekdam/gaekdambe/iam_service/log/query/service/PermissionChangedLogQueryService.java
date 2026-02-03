package com.gaekdam.gaekdambe.iam_service.log.query.service;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.PermissionChangedLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.PermissionChangedLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.mapper.PermissionChangedLogMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionChangedLogQueryService {
    private final PermissionChangedLogMapper permissionChangedLogMapper;
    private final EmployeeRepository employeeRepository;
    private final KmsService kmsService;

    public PageResponse<PermissionChangedLogQueryResponse> getPermissionChangedLogs(
            Long hotelGroupCode,
            PageRequest page,
            PermissionChangedLogSearchRequest search,
            SortRequest sort) {

        List<PermissionChangedLogQueryResponse> list = permissionChangedLogMapper
                .findPermissionChangedLogs(hotelGroupCode, page, search, sort);

        List<PermissionChangedLogQueryResponse> decryptedList = list.stream()
                .map(this::decryptNames)
                .toList();

        long total = permissionChangedLogMapper.countPermissionChangedLogs(hotelGroupCode, search);

        return new PageResponse<>(
                decryptedList,
                page.getPage(),
                page.getSize(),
                total);
    }

    private PermissionChangedLogQueryResponse decryptNames(PermissionChangedLogQueryResponse dto) {
        String accessorName = decryptEmployeeName(dto.employeeAccessorCode(), dto.employeeAccessorName());
        String changedName = decryptEmployeeName(dto.employeeChangedCode(), dto.employeeChangedName());

        return new PermissionChangedLogQueryResponse(
                dto.permissionChangedLogCode(),
                dto.changedAt(),
                dto.employeeAccessorCode(),
                accessorName,
                dto.employeeAccessorLoginId(),
                dto.employeeChangedCode(),
                changedName,
                dto.employeeChangedLoginId(),
                dto.hotelGroupCode(),
                dto.beforePermissionCode(),
                dto.beforePermissionName(),
                dto.afterPermissionCode(),
                dto.afterPermissionName());
    }

    private String decryptEmployeeName(Long employeeCode, String currentName) {
        if (employeeCode == null)
            return currentName;
        try {
            Employee employee = employeeRepository.findById(employeeCode).orElse(null);
            if (employee == null)
                return currentName;

            byte[] plaintextDek = kmsService.decryptDataKey(employee.getDekEnc());
            return AesCryptoUtils.decrypt(employee.getEmployeeNameEnc(), plaintextDek);
        } catch (Exception e) {
            return currentName;
        }
    }
}
