package com.gaekdam.gaekdambe;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

//엔티티가 생성되고, 변경되는 그 시점을 감지하여 생성시각, 수정시각, 생성한 사람, 수정한 사람을 기록
@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class GaekdamBeApplication {

  public static void main(String[] args) {

    SpringApplication.run(GaekdamBeApplication.class, args);
  }

}
