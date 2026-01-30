package com.gaekdam.gaekdambe.customer_service.customer.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerListSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerStatusHistoryRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.*;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerListItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.mapper.CustomerMapper;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.assembler.CustomerResponseAssembler;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.condition.CustomerListSearchParam;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerContactRow;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerDetailRow;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerListRow;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerStatusHistoryRow;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerStatusRow;
import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.Normalizer;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.LogPersonalInfo;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerQueryService {

  private static final Set<String> CUSTOMER_LIST_SORT_WHITELIST = Set.of(
      "created_at", "customer_code", "customer_name", "last_used_date");

  private static final Set<String> STATUS_HISTORY_SORT_WHITELIST = Set.of(
      "changed_at", "customer_status_history_code");

  private final CustomerMapper customerMapper;
  private final CustomerResponseAssembler assembler;
  private final SearchHashService searchHashService;
  private final DecryptionService decryptionService;

  public PageResponse<CustomerListItem> getCustomerList(CustomerListSearchRequest request) {
    PageRequest page = buildPageRequest(request.getPage(), request.getSize());

    SortRequest sort = buildSortRequest(
        request.getSortBy(),
        request.getDirection(),
        CUSTOMER_LIST_SORT_WHITELIST,
        "created_at");

    CustomerListSearchParam search = buildCustomerListSearchParam(request);

    List<CustomerListRow> rows = customerMapper.findCustomers(page, search, sort);
    long total = customerMapper.countCustomers(search);

    List<CustomerListItem> items = rows.stream()
        .map(assembler::toCustomerListItem)
        .toList();

    return new PageResponse<>(items, page.getPage(), page.getSize(), total);
  }

  @LogPersonalInfo(type = PermissionTypeKey.CUSTOMER_READ, purpose = "고객 정보 조회")
  public CustomerDetailResponse getCustomerDetail(Long hotelGroupCode, Long customerCode, String reason) {
    CustomerDetailRow detailRow = customerMapper.findCustomerDetail(hotelGroupCode, customerCode);
    if (detailRow == null) {
      throw new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 고객입니다.");
    }

    List<CustomerContactRow> contactRows = customerMapper.findCustomerContacts(hotelGroupCode,
        customerCode);
    return assembler.toCustomerDetailResponse(detailRow, contactRows);
  }

  public CustomerStatusResponse getCustomerStatus(Long hotelGroupCode, Long customerCode) {
    CustomerStatusRow row = customerMapper.findCustomerStatus(hotelGroupCode, customerCode);
    if (row == null) {
      throw new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 고객입니다.");
    }
    return assembler.toCustomerStatusResponse(row);
  }

  public CustomerStatusHistoryResponse getCustomerStatusHistories(
      Long hotelGroupCode,
      Long customerCode,
      CustomerStatusHistoryRequest request) {
    PageRequest page = buildPageRequest(request.getPage(), request.getSize());

    SortRequest sort = buildSortRequest(
        request.getSortBy(),
        request.getDirection(),
        STATUS_HISTORY_SORT_WHITELIST,
        "changed_at");

    List<CustomerStatusHistoryRow> rows = customerMapper.findCustomerStatusHistories(hotelGroupCode, customerCode, page,
        sort);

    long total = customerMapper.countCustomerStatusHistories(hotelGroupCode, customerCode);
    return assembler.toCustomerStatusHistoryResponse(rows, page.getPage(), page.getSize(), total);
  }

  public CustomerMarketingConsentResponse getCustomerMarketingConsents(Long hotelGroupCode,
      Long customerCode) {
    List<CustomerContactRow> rows = customerMapper.findCustomerMarketingConsents(hotelGroupCode,
        customerCode);
    return assembler.toCustomerMarketingConsentResponse(customerCode, rows);
  }

  // Page / Sort builders

  private PageRequest buildPageRequest(int page, int size) {
    PageRequest pageRequest = new PageRequest();

    int safePage = (page <= 0) ? 1 : page;
    int safeSize = (size <= 0) ? 20 : size;

    pageRequest.setPage(safePage);
    pageRequest.setSize(safeSize);
    return pageRequest;
  }

  private SortRequest buildSortRequest(String sortBy, String direction, Set<String> whitelist,
      String defaultSort) {
    SortRequest sort = new SortRequest();
    sort.setSortBy(normalizeSortBy(sortBy, whitelist, defaultSort));
    sort.setDirection(normalizeDirection(direction));
    return sort;
  }


  // SearchParam Builder (정리)

  private CustomerListSearchParam buildCustomerListSearchParam(CustomerListSearchRequest request) {
    KeywordParts parts = new KeywordParts(
        request.getCustomerCode(),
        request.getCustomerName(),
        request.getPhoneNumber(),
        request.getEmail());

    // keyword를 분해하는 로직을 별도 메서드로 분리
    applyKeywordIfNeeded(request.getKeyword(), parts);

    // normalize
    String normalizedName = Normalizer.name(parts.customerName);
    String normalizedPhone = Normalizer.phone(parts.phoneNumber);
    String normalizedEmail = Normalizer.email(parts.email);

    // 해시 생성 로직 분리
    Hashes hashes = buildHashes(normalizedName, normalizedPhone, normalizedEmail);

    log.info("[CustomerList] parts: customerCode={}, name='{}', phone='{}', email='{}'",
        parts.customerCode, normalizedName, normalizedPhone, normalizedEmail);
    log.info("[CustomerList] hashes: nameHash={}, phoneHash={}, emailHash={}",
        hashes.nameHash, hashes.phoneHash, hashes.emailHash);

    return new CustomerListSearchParam(
        request.getHotelGroupCode(),
        parts.customerCode,
        hashes.nameHash,
        hashes.phoneHash,
        hashes.emailHash,
        request.getStatus(),
        request.getContractType(),
        request.getNationalityType(),
        request.getMembershipGradeCode(),
        request.getLoyaltyGradeCode(),
        request.getInflowChannel());
  }

  /**
   * keyword 적용을 별도 함수로 분리 - 상세검색 값이 비어있을 때만 keyword를 분해해서 세팅
   */
  private void applyKeywordIfNeeded(String keywordRaw, KeywordParts parts) {
    if (hasAnyDetailCondition(parts)) {
      return;
    }
    if (isBlank(keywordRaw)) {
      return;
    }

    KeywordResolved resolved = resolveKeyword(keywordRaw.trim());

    switch (resolved.type) {
      case CUSTOMER_CODE -> parts.customerCode = resolved.customerCode;
      case EMAIL -> parts.email = resolved.value;
      case PHONE -> parts.phoneNumber = resolved.value;
      case NAME -> parts.customerName = resolved.value;
    }
  }

  private boolean hasAnyDetailCondition(KeywordParts parts) {
    return parts.customerCode != null
        || !isBlank(parts.customerName)
        || !isBlank(parts.phoneNumber)
        || !isBlank(parts.email);
  }

  /**
   * 전체검색 판별 로직만 담당 - 이메일 우선 - 전화(숫자 8자리 이상) 우선 - 그 다음 고객코드(숫자만) -
   * 나머지 이름
   */
  private KeywordResolved resolveKeyword(String keyword) {
    if (keyword.contains("@")) {
      return KeywordResolved.email(keyword);
    }

    String digits = keyword.replaceAll("[^0-9]", "");
    if (!digits.isBlank() && digits.length() >= 8) {
      return KeywordResolved.phone(keyword); // 원본을 넣고, Normalizer.phone에서 digits로 정리됨
    }

    if (keyword.matches("^\\d+$")) {
      try {
        return KeywordResolved.customerCode(Long.parseLong(keyword));
      } catch (NumberFormatException ignore) {
        // fallthrough
      }
    }

    return KeywordResolved.name(keyword);
  }

  private Hashes buildHashes(String normalizedName, String normalizedPhone,
      String normalizedEmail) {
    return new Hashes(
        toHex(searchHashService.nameHash(normalizedName)),
        toHex(searchHashService.phoneHash(normalizedPhone)),
        toHex(searchHashService.emailHash(normalizedEmail)));
  }

  // Utils

  private String normalizeSortBy(String sortBy, Set<String> whitelist, String defaultSort) {
    if (isBlank(sortBy)) {
      return defaultSort;
    }
    String normalized = sortBy.trim().toLowerCase();
    return whitelist.contains(normalized) ? normalized : defaultSort;
  }

  private String normalizeDirection(String direction) {
    if (isBlank(direction)) {
      return "DESC";
    }
    String normalized = direction.trim().toUpperCase();
    return normalized.equals("ASC") ? "ASC" : "DESC";
  }

  private boolean isBlank(String v) {
    return v == null || v.isBlank();
  }

  private String toHex(byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  // =========================
  // 내부 DTO(가독성용)
  // =========================

  private static class KeywordParts {

    private Long customerCode;
    private String customerName;
    private String phoneNumber;
    private String email;

    private KeywordParts(Long customerCode, String customerName, String phoneNumber, String email) {
      this.customerCode = customerCode;
      this.customerName = customerName;
      this.phoneNumber = phoneNumber;
      this.email = email;
    }
  }

  private record Hashes(String nameHash, String phoneHash, String emailHash) {

  }

  private enum KeywordType {
    CUSTOMER_CODE, EMAIL, PHONE, NAME
  }

  private record KeywordResolved(KeywordType type, String value, Long customerCode) {

    static KeywordResolved customerCode(Long code) {
      return new KeywordResolved(KeywordType.CUSTOMER_CODE, null, code);
    }

    static KeywordResolved email(String v) {
      return new KeywordResolved(KeywordType.EMAIL, v, null);
    }

    static KeywordResolved phone(String v) {
      return new KeywordResolved(KeywordType.PHONE, v, null);
    }

    static KeywordResolved name(String v) {
      return new KeywordResolved(KeywordType.NAME, v, null);
    }
  }

  // 고객활동쪽에서 추가함
  @LogPersonalInfo(type = PermissionTypeKey.CUSTOMER_READ, purpose = "고객 정보 조회")
  public CustomerBasicResponse getCustomerBasic(
      Long hotelGroupCode,
      Long customerCode,
      String reason) {
    CustomerBasicRow row = customerMapper.findCustomerBasic(hotelGroupCode, customerCode);

    if (row == null) {
      throw new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 고객입니다.");
    }

    String customerName = null;
    if (row.getCustomerNameEnc() != null && row.getDekEnc() != null) {
      customerName = decryptionService.decrypt(
          row.getCustomerCode(),
          row.getDekEnc(),
          row.getCustomerNameEnc());
    }

    String phoneNumber = null;
    if (row.getPhoneEnc() != null && row.getDekEnc() != null) {
      phoneNumber = decryptionService.decrypt(
          row.getCustomerCode(),
          row.getDekEnc(),
          row.getPhoneEnc());
    }

    return new CustomerBasicResponse(
        row.getCustomerCode(),
        customerName,
        phoneNumber);
  }

}
