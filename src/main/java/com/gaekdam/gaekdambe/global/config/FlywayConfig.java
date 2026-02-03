package com.gaekdam.gaekdambe.global.config;



import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

  @Bean
  public FlywayMigrationStrategy cleanMigrateStrategy() {
    return flyway -> {
      // 1. 꼬인 족보(실패 기록)를 수리합니다. (이게 핵심!)
      flyway.repair();

      // 2. 수리가 끝났으니 마이그레이션을 진행합니다.
      flyway.migrate();
    };
  }
}