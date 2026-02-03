package com.gaekdam.gaekdambe.iam_service.log.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.PersonalInformationLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.PersonalInformationLogQueryResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PersonalInformationLogMapper {

        List<PersonalInformationLogQueryResponse> findPersonalInformationLogs(
                        @Param("hotelGroupCode") Long hotelGroupCode,
                        @Param("page") PageRequest page,
                        @Param("search") PersonalInformationLogSearchRequest search,
                        @Param("accessorNameHash") byte[] accessorNameHash,
                        @Param("targetNameHash") byte[] targetNameHash,
                        @Param("sort") SortRequest sort);

        long countPersonalInformationLogs(
                        @Param("hotelGroupCode") Long hotelGroupCode,
                        @Param("search") PersonalInformationLogSearchRequest search,
                        @Param("accessorNameHash") byte[] accessorNameHash,
                        @Param("targetNameHash") byte[] targetNameHash);
}
