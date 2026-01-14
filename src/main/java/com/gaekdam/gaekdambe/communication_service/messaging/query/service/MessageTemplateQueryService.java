package com.gaekdam.gaekdambe.communication_service.messaging.query.service;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.request.MessageTemplateSearch;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageTemplateResponse;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessageTemplateQueryMapper;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageTemplateQueryService {

    private final MessageTemplateQueryMapper mapper;

    public PageResponse<MessageTemplateResponse> getTemplates(
            PageRequest page,
            MessageTemplateSearch search,
            SortRequest sort
    ) {



        List<MessageTemplateResponse> list =
                mapper.findTemplates( search, page, sort);



        long total =
                mapper.countTemplates(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }
}
