package com.gaekdam.gaekdambe.customer_service.customer.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerListSearchRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.request.CustomerStatusHistoryRequest;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerDetailResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerMarketingConsentResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerStatusHistoryResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.CustomerStatusResponse;
import com.gaekdam.gaekdambe.customer_service.customer.query.dto.response.item.CustomerListItem;
import com.gaekdam.gaekdambe.customer_service.customer.query.mapper.CustomerMapper;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.assembler.CustomerResponseAssembler;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.condition.CustomerListSearchParam;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerContactRow;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerDetailRow;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerListRow;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerStatusHistoryRow;
import com.gaekdam.gaekdambe.customer_service.customer.query.service.model.row.CustomerStatusRow;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerQueryService {

    // MyBatis ORDER BY에서 ${}를 쓸 가능성이 있으니, 허용 컬럼만 통과시키기 위한 whitelist
    private static final Set<String> CUSTOMER_LIST_SORT_WHITELIST = Set.of(
            "created_at", "customer_code", "customer_name", "last_used_date"
    );

    private static final Set<String> STATUS_HISTORY_SORT_WHITELIST = Set.of(
            "changed_at", "customer_status_history_code"
    );

    private final CustomerMapper customerMapper;
    private final CustomerResponseAssembler assembler;

    /**
     * 고객 목록 조회
     * - page/size: 페이징
     * - sortBy/direction: 드롭다운 정렬
     * - keyword + 상세검색: 서비스에서 keyword를 name/phone/email/customerCode로 분배 후 hash 생성
     */
    public PageResponse<CustomerListItem> getCustomerList(CustomerListSearchRequest request) {
        PageRequest page = buildPageRequest(request.getPage(), request.getSize());

        SortRequest sort = buildSortRequest(
                request.getSortBy(),
                request.getDirection(),
                CUSTOMER_LIST_SORT_WHITELIST,
                "created_at"
        );

        CustomerListSearchParam search = buildCustomerListSearchParam(request);

        List<CustomerListRow> rows = customerMapper.findCustomers(page, search, sort);
        long total = customerMapper.countCustomers(search);

        List<CustomerListItem> items = rows.stream()
                .map(assembler::toCustomerListItem)
                .toList();

        // page/size는 "보정된 값"으로 내려주는 게 화면/페이징 상태 유지에 더 안전함
        return new PageResponse<>(items, page.getPage(), page.getSize(), total);
    }

    /**
     * 고객 상세 조회
     */
    public CustomerDetailResponse getCustomerDetail(Long hotelGroupCode, Long customerCode) {
        CustomerDetailRow detailRow = customerMapper.findCustomerDetail(hotelGroupCode, customerCode);
        if (detailRow == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 고객입니다.");
        }

        List<CustomerContactRow> contactRows = customerMapper.findCustomerContacts(hotelGroupCode, customerCode);
        return assembler.toCustomerDetailResponse(detailRow, contactRows);
    }

    /**
     * 고객 상태 조회
     */
    public CustomerStatusResponse getCustomerStatus(Long hotelGroupCode, Long customerCode) {
        CustomerStatusRow row = customerMapper.findCustomerStatus(hotelGroupCode, customerCode);
        if (row == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 고객입니다.");
        }
        return assembler.toCustomerStatusResponse(row);
    }

    /**
     * 고객 상태 변경 이력 조회 (paging)
     */
    public CustomerStatusHistoryResponse getCustomerStatusHistories(
            Long hotelGroupCode,
            Long customerCode,
            CustomerStatusHistoryRequest request
    ) {
        PageRequest page = buildPageRequest(request.getPage(), request.getSize());

        SortRequest sort = buildSortRequest(
                request.getSortBy(),
                request.getDirection(),
                STATUS_HISTORY_SORT_WHITELIST,
                "changed_at"
        );

        List<CustomerStatusHistoryRow> rows =
                customerMapper.findCustomerStatusHistories(hotelGroupCode, customerCode, page, sort);

        long total = customerMapper.countCustomerStatusHistories(hotelGroupCode, customerCode);

        return assembler.toCustomerStatusHistoryResponse(rows, page.getPage(), page.getSize(), total);
    }

    /**
     * 연락처별 마케팅 수신 동의 조회
     */
    public CustomerMarketingConsentResponse getCustomerMarketingConsents(Long hotelGroupCode, Long customerCode) {
        List<CustomerContactRow> rows = customerMapper.findCustomerMarketingConsents(hotelGroupCode, customerCode);
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

    private SortRequest buildSortRequest(String sortBy, String direction, Set<String> whitelist, String defaultSort) {
        SortRequest sort = new SortRequest();
        sort.setSortBy(normalizeSortBy(sortBy, whitelist, defaultSort));
        sort.setDirection(normalizeDirection(direction));
        return sort;
    }

    // SearchParam Builder

    private CustomerListSearchParam buildCustomerListSearchParam(CustomerListSearchRequest request) {
        Long customerCode = request.getCustomerCode();
        String customerName = request.getCustomerName();
        String phoneNumber = request.getPhoneNumber();
        String email = request.getEmail();

        // keyword 우선 적용(상세검색 값이 비어있을 때만)
        if (isBlank(customerName) && isBlank(phoneNumber) && isBlank(email)
                && customerCode == null && !isBlank(request.getKeyword())) {

            String keyword = request.getKeyword().trim();

            if (keyword.matches("^\\d+$")) {
                try {
                    customerCode = Long.parseLong(keyword);
                } catch (NumberFormatException ignore) {
                }
            } else if (keyword.contains("@")) {
                email = keyword;
            } else if (keyword.replaceAll("[^0-9]", "").length() >= 8) {
                phoneNumber = keyword;
            } else {
                customerName = keyword;
            }
        }

        String customerNameHash = toSha256Hex(normalizeName(customerName));
        String phoneHash = toSha256Hex(normalizePhone(phoneNumber));
        String emailHash = toSha256Hex(normalizeEmail(email));

        return new CustomerListSearchParam(
                request.getHotelGroupCode(),
                customerCode,
                customerNameHash,
                phoneHash,
                emailHash,
                request.getStatus(),
                request.getContractType(),
                request.getNationalityType(),
                request.getMembershipGradeCode(),
                request.getLoyaltyGradeCode(),
                request.getInflowChannel()
        );
    }


    private String normalizeSortBy(String sortBy, Set<String> whitelist, String defaultSort) {
        if (isBlank(sortBy)) return defaultSort;
        String normalized = sortBy.trim().toLowerCase();
        return whitelist.contains(normalized) ? normalized : defaultSort;
    }

    private String normalizeDirection(String direction) {
        if (isBlank(direction)) return "DESC";
        String normalized = direction.trim().toUpperCase();
        return normalized.equals("ASC") ? "ASC" : "DESC";
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    private String normalizePhone(String phone) {
        if (isBlank(phone)) return null;
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.isBlank() ? null : digits;
    }

    private String normalizeEmail(String email) {
        if (isBlank(email)) return null;
        return email.trim().toLowerCase();
    }

    private String normalizeName(String name) {
        if (isBlank(name)) return null;
        return name.trim();
    }

    private String toSha256Hex(String raw) {
        if (raw == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("해시 생성 실패");
        }
    }
}
