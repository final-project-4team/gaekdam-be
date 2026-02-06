package com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation;

import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 어노테이션이 붙은 메서드는 실행 성공 시 자동으로 개인정보 조회 로그가 기록됩니다.
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogPersonalInfo {

  //권한 타입
  PermissionTypeKey type();

  //조회 목적
  String purpose() default "상세 정보 조회";
}
