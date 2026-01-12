package com.gaekdam.gaekdambe.dummy.generate.iam_service.permissionType;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeSeed;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.infrastructure.PermissionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DummyPermissionTypeDataTest {

  @Autowired
  private PermissionTypeRepository permissionTypeRepository;
  @Autowired
  private  HotelGroupRepository hotelGroupRepository;

  @Transactional
  public void generate() {

      if (permissionTypeRepository.count() > 0) {
          return;
      }

    for (long hotel = 1; hotel <= 10; hotel++) {
      for (PermissionTypeSeed s : PermissionTypeSeed.values()) {
        long hotels=hotel;
        HotelGroup hotelGroup=hotelGroupRepository.findById(hotels).orElseThrow();
        permissionTypeRepository.findByPermissionTypeKey(s.permissionTypeKey)
            .orElseGet(() -> permissionTypeRepository.save(
                PermissionType.createPermissionType(s.permissionTypeKey, s.permissionTypeName,
                    s.permissionTypeKeyResource, s.permissionTypeAction,hotelGroup)
            ));
      }
    }
  }
}

