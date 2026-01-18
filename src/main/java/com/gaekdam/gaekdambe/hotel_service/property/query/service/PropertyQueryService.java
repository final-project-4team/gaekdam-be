package com.gaekdam.gaekdambe.hotel_service.property.query.service;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.request.PropertyQueryRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.request.PropertySearchByHotelGroupRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.request.PropertySearchRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.response.PropertyListResponse;
import com.gaekdam.gaekdambe.hotel_service.property.query.mapper.PropertyMapper;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertyQueryService {

    private final PropertyMapper propertyMapper;

    public PageResponse<PropertyListResponse> getPropertyList(PropertyQueryRequest query) {
        PageRequest pageReq = new PageRequest();
        pageReq.setPage(query.page());
        pageReq.setSize(query.size());

        PropertySearchRequest searchReq =
                new PropertySearchRequest(query.propertyCode(), query.propertyCity(), query.propertyName(), query.propertyStatus());

        SortRequest sortReq = new SortRequest();
        sortReq.setSortBy(query.sortBy());
        sortReq.setDirection(query.direction());

        List<PropertyListResponse> list = propertyMapper.findPropertyList(pageReq, searchReq, sortReq);
        long total = propertyMapper.countPropertyList(searchReq);

        return new PageResponse<>(
                list,
                query.page(),
                query.size(),
                total);
    }


    public List<PropertyListResponse> getPropertiesByHotelGroup(
            Long hotelGroupCode
    ) {
        PropertySearchByHotelGroupRequest search = new PropertySearchByHotelGroupRequest();
        search.setHotelGroupCode(hotelGroupCode);

        return propertyMapper.findByHotelGroup(search);
    }

}
