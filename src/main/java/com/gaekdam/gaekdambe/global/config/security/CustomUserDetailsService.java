package com.gaekdam.gaekdambe.global.config.security;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity.PermissionMapping;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.infrastructure.PermissionMappingRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final EmployeeRepository employeeRepository;
  private final PermissionRepository permissionRepository;
  private final PermissionMappingRepository permissionMappingRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
    return loadUserByUsername(employeeId, null, null);
  }

  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String employeeId,Long hotelGroupCode,Long propertyCode) throws UsernameNotFoundException {

    Employee emp = employeeRepository.findByLoginId(employeeId)
        .orElseThrow(() -> new UsernameNotFoundException("Employee not found: " + employeeId));

    Permission permission = permissionRepository.findById(emp.getPermission().getPermissionCode())
        .orElseThrow(() -> new UsernameNotFoundException("Role not found for employee"));

    List<GrantedAuthority> authorities = new ArrayList<>();

    // 인증 리스트에 권한 추가
   // authorities.add(new SimpleGrantedAuthority("ROLE_" + permission.getPermissionName()));

    // 권한에 해당하는 세부 권한 타입 리스트 추가
    List<PermissionMapping> mappings = permissionMappingRepository.findAllByPermission(permission);
    for (PermissionMapping mapping : mappings) {
      String authority = mapping.getPermissionType().getPermissionTypeKey().name();
      authorities.add(new SimpleGrantedAuthority(authority));
    }

    return new CustomUser(
        emp.getLoginId(), // principal(식별자)
        emp.getPasswordHash(), // DB에 저장된 암호화된 비밀번호(BCrypt 등)
        authorities,
        hotelGroupCode,
        propertyCode
    );
  }
}
