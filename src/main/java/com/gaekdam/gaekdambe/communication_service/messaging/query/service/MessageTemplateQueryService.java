package com.gaekdam.gaekdambe.communication_service.messaging.query.service;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageTemplateSearch;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.*;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessageTemplateQueryMapper;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessageTemplateSettingMapper;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageTemplateQueryService {

    private final MessageTemplateQueryMapper templateMapper;
    private final MessageTemplateSettingMapper settingMapper;

    /* =========================
       기존: 리스트 / 검색 / 페이징
       ========================= */
    public PageResponse<MessageTemplateResponse> getTemplates(
            PageRequest page,
            MessageTemplateSearch search,
            SortRequest sort
    ) {
        List<MessageTemplateResponse> list =
                templateMapper.findTemplates(search, page, sort);

        long total =
                templateMapper.countTemplates(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }


    public MessageTemplateDetailResponse getTemplate(Long templateCode) {
        return templateMapper.findTemplateDetail(templateCode);
    }

    /* =========================
       설정 화면 (여정 기준)
       ========================= */
    public List<MessageTemplateSettingResponse> getSettingTemplates(
            Long propertyCode
    ) {
        List<MessageTemplateSettingFlatRow> rows =
                settingMapper.findSettingRows(propertyCode);

        Map<Long, MessageTemplateSettingResponse> result = new LinkedHashMap<>();

        for (MessageTemplateSettingFlatRow row : rows) {

            MessageTemplateSettingResponse stage =
                    result.computeIfAbsent(row.getStageCode(), k -> {
                        MessageTemplateSettingResponse s = new MessageTemplateSettingResponse();
                        s.setStageCode(row.getStageCode());
                        s.setStageNameKor(row.getStageNameKor());

                        Map<String, MessageTemplateSettingItem> templates = new HashMap<>();
                        templates.put("FIRST", null);
                        templates.put("REPEAT", null);
                        s.setTemplates(templates);
                        return s;
                    });

            if (row.getTemplateCode() != null) {
                MessageTemplateSettingItem item = new MessageTemplateSettingItem();
                item.setTemplateCode(row.getTemplateCode());
                item.setVisitorType(row.getVisitorType());
                item.setLanguageCode(row.getLanguageCode());
                item.setTitle(row.getTitle());
                item.setActive(row.getIsActive());

                stage.getTemplates()
                        .put(row.getVisitorType().name(), item);
            }
        }

        return new ArrayList<>(result.values());
    }
}
