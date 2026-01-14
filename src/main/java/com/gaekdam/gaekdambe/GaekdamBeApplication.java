package com.gaekdam.gaekdambe;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GaekdamBeApplication {

  public static void main(String[] args) {
    SpringApplication.run(GaekdamBeApplication.class, args);
  }

}
