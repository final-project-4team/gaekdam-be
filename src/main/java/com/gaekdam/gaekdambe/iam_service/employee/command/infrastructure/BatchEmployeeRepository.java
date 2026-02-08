package com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class BatchEmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void saveAllBatch(List<Employee> employees) {
        String sql = """
                    INSERT INTO employee (
                        employee_number, login_id, password_hash,
                        email_enc, email_hash, phone_number_enc, phone_number_hash,
                        employee_name_enc, employee_name_hash, dek_enc,
                        hired_at, created_at, updated_at,
                        employee_status, failed_login_count,
                        department_code, hotel_position_code, property_code, hotel_group_code, permission_code
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, employees, employees.size(),
                (PreparedStatement ps, Employee employee) -> {
                    ps.setLong(1, employee.getEmployeeNumber());
                    ps.setString(2, employee.getLoginId());
                    ps.setString(3, employee.getPasswordHash());
                    ps.setBytes(4, employee.getEmailEnc());
                    ps.setBytes(5, employee.getEmailHash());
                    ps.setBytes(6, employee.getPhoneNumberEnc());
                    ps.setBytes(7, employee.getPhoneNumberHash());
                    ps.setBytes(8, employee.getEmployeeNameEnc());
                    ps.setBytes(9, employee.getEmployeeNameHash());
                    ps.setBytes(10, employee.getDekEnc());
                    ps.setTimestamp(11, Timestamp.valueOf(employee.getHiredAt()));
                    ps.setTimestamp(12, Timestamp.valueOf(employee.getCreatedAt()));
                    ps.setTimestamp(13, Timestamp.valueOf(employee.getUpdatedAt()));
                    ps.setString(14, employee.getEmployeeStatus().name());
                    ps.setInt(15, employee.getFailedLoginCount());

                    ps.setLong(16, employee.getDepartment().getDepartmentCode());
                    ps.setLong(17, employee.getHotelPosition().getHotelPositionCode());

                    if (employee.getProperty() != null) {
                        ps.setLong(18, employee.getProperty().getPropertyCode());
                    } else {
                        ps.setNull(18, java.sql.Types.BIGINT);
                    }

                    ps.setLong(19, employee.getHotelGroup().getHotelGroupCode());
                    ps.setLong(20, employee.getPermission().getPermissionCode());
                });
    }
}
