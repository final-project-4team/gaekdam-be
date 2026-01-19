package com.gaekdam.gaekdambe.hotel_service.property.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.request.PropertySearchByHotelGroupRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.request.PropertySearchRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.response.PropertyListResponse;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PropertyMapper {

    List<PropertyListResponse> findPropertyList(
            @Param("page") PageRequest page,
            @Param("search") PropertySearchRequest search,
            @Param("sort") SortRequest sort);

    long countPropertyList(
            @Param("search") PropertySearchRequest search);

    List<PropertyListResponse> findByHotelGroup(
            @Param("search") PropertySearchByHotelGroupRequest search
    );
}