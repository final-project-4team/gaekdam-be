package com.gaekdam.gaekdambe.dummy.generate.iam_service.permissionType;

import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeSeed;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.infrastructure.PermissionTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DummyPermissionTypeDataTest {

  @Autowired
  PermissionTypeRepository permissionTypeRepository;

  @Transactional
  public void generate() {

      if (permissionTypeRepository.count() > 0) {
          return;
      }

    for (long hotel = 1; hotel <= 10; hotel++) {
      for (PermissionTypeSeed s : PermissionTypeSeed.values()) {
        long hotels=hotel;
        permissionTypeRepository.findByPermissionTypeKey(s.permissionTypeKey)
            .orElseGet(() -> permissionTypeRepository.save(
                PermissionType.createPermissionType(s.permissionTypeKey, s.permissionTypeName,
                    s.permissionTypeKeyResource, s.permissionTypeAction,hotels)
            ));
      }
    }
  }
}

