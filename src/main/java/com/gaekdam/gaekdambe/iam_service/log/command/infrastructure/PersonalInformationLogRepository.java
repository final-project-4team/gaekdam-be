package com.gaekdam.gaekdambe.iam_service.log.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.PersonalInformationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalInformationLogRepository extends JpaRepository<PersonalInformationLog, Long> {

}
