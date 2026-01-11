package com.gaekdam.gaekdambe.iam_service.employee.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeDetailResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeListResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.mapper.EmployeeQueryMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeQueryService {

    private final EmployeeQueryMapper employeeQueryMapper;
    private final DecryptionService decryptionService;
    private final SearchHashService searchHashService;

    public record EmployeeListDto(
            Long employeeCode,
            Long employeeNumber,
            String loginId,
            String nameMasked,
            String phoneMasked,
            String emailMasked) {
    }

    public record EmployeeDetailDto(
            Long employeeCode,
            Long employeeNumber,
            String loginId,
            String name,
            String phone,
            String email) {
    }

    public List<EmployeeListDto> getEmployeeList() {
        List<EmployeeListResponse> employees = employeeQueryMapper.findAllEmployees();
        return employees.stream().map(this::toListDto).collect(Collectors.toList());
    }

    public EmployeeDetailDto getEmployeeDetail(Long employeeCode) {
        EmployeeDetailResponse response = employeeQueryMapper.findByEmployeeCode(employeeCode);
        if (response == null)
            throw new IllegalArgumentException("Not found: " + employeeCode);
        return toDetailDto(response);
    }

    public List<EmployeeListDto> findByEmail(String email) {
        byte[] hash = searchHashService.emailHash(email);
        List<EmployeeListResponse> employees = employeeQueryMapper.findByEmailHash(hash);
        return employees.stream().map(this::toListDto).collect(Collectors.toList());
    }

    public List<EmployeeListDto> findByPhone(String phone) {
        byte[] hash = searchHashService.phoneHash(phone);
        List<EmployeeListResponse> employees = employeeQueryMapper.findByPhoneHash(hash);
        return employees.stream().map(this::toListDto).collect(Collectors.toList());
    }

    public List<EmployeeListDto> findByName(String name) {
        byte[] hash = searchHashService.nameHash(name);
        List<EmployeeListResponse> employees = employeeQueryMapper.findByNameHash(hash);
        return employees.stream().map(this::toListDto).collect(Collectors.toList());
    }

    public List<EmployeeListDto> findByPassword(String password) {
        List<EmployeeListResponse> employees = employeeQueryMapper.findByPassword(password);
        return employees.stream().map(this::toListDto).collect(Collectors.toList());
    }

    public List<EmployeeListDto> findByNameAndPhone(String name, String phone) {
        byte[] nameHash = searchHashService.nameHash(name);
        byte[] phoneHash = searchHashService.phoneHash(phone);
        List<EmployeeListResponse> employees = employeeQueryMapper.findByNameAndPhoneHash(nameHash, phoneHash);
        return employees.stream().map(this::toListDto).collect(Collectors.toList());
    }

    private EmployeeListDto toListDto(EmployeeListResponse response) {
        Long code = response.getEmployeeCode();
        byte[] dekEnc = response.getDekEnc();

        String name = decryptionService.decrypt(code, dekEnc, response.getEmployeeNameEnc());
        String phone = decryptionService.decrypt(code, dekEnc, response.getPhoneNumberEnc());
        String email = decryptionService.decrypt(code, dekEnc, response.getEmailEnc());

        return new EmployeeListDto(
                code,
                response.getEmployeeNumber(),
                response.getLoginId(),
                MaskingUtils.maskName(name),
                MaskingUtils.maskPhone(phone),
                (email != null) ? MaskingUtils.maskEmail(email) : null);
    }

    private EmployeeDetailDto toDetailDto(EmployeeDetailResponse response) {
        Long code = response.getEmployeeCode();
        byte[] dekEnc = response.getDekEnc();

        return new EmployeeDetailDto(
                code,
                response.getEmployeeNumber(),
                response.getLoginId(),
                decryptionService.decrypt(code, dekEnc, response.getEmployeeNameEnc()),
                decryptionService.decrypt(code, dekEnc, response.getPhoneNumberEnc()),
                decryptionService.decrypt(code, dekEnc, response.getEmailEnc()));
    }
}
