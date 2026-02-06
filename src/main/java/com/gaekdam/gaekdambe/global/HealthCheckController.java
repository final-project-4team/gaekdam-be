package com.gaekdam.gaekdambe.global;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="헬스 체크")
@RestController
public class HealthCheckController {

    @GetMapping("/health")
    @Operation(summary = "헬스 체크", description = "")
    public String ok() {
        return "health OK";
    }
}
