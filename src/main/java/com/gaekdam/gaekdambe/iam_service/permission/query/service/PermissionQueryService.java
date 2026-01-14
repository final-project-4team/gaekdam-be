package com.gaekdam.gaekdambe.iam_service.permission.query.service;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;

import com.gaekdam.gaekdambe.iam_service.permission.query.dto.request.PermissionQueryRequest;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.request.PermissionSearchRequest;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.response.PermissionListResponse;
import com.gaekdam.gaekdambe.iam_service.permission.query.mapper.PermissionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionQueryService {

  private final PermissionMapper permissionMapper;

  public PageResponse<PermissionListResponse> getPermissionList(PermissionQueryRequest request,Long hotelGroupCode) {
    PageRequest pageReq = new PageRequest();
    pageReq.setPage(request.page());
    pageReq.setSize(request.size());
    Integer offset=pageReq.getOffset();

    PermissionSearchRequest searchReq =
        new PermissionSearchRequest(request.permissionName(),request.resourceName());

    SortRequest sortReq = new SortRequest();
    sortReq.setSortBy(request.sortBy());
    sortReq.setDirection(request.direction());


    List<PermissionListResponse> list = permissionMapper.findPermissionList(pageReq,offset ,searchReq, hotelGroupCode,sortReq);
    long total = permissionMapper.countPermissionList(searchReq);

    return new PageResponse<>(
        list,
        request.page(),
        request.size(),
        total);
  }
}
