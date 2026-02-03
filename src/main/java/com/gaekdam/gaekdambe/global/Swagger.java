package com.gaekdam.gaekdambe.global;

import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Configuration
public class Swagger {

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthenticationPrincipal.class);
    }

    @Bean
    public OpenAPI openAPI() {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(CustomUser.class);

        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("GaekDam API명세서")
                        .version("v1.0")
                        .description("호텔의 고객 정보를 종합적으로 관리 및 직원의 응대 능력을 상승 시키는 시스템"))
                .addServersItem(new Server().url("http://localhost:8082").description("Local Server"));
    }
}

