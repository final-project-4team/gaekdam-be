package com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

  List<Permission> findByHotelGroup_HotelGroupCode(long hotelGroupCode);

  boolean existsByHotelGroup_HotelGroupCodeAndPermissionName(Long hotelGroupCode, String permissionName);

}
