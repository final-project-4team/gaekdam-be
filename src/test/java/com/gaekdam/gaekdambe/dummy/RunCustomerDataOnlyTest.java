package com.gaekdam.gaekdambe.dummy;

import com.gaekdam.gaekdambe.dummy.generate.customer_service.customer.DummyCustomerDataTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Rollback(false)
@ActiveProfiles("local-dummy")
@Tag("dummy")
public class RunCustomerDataOnlyTest {

    @Autowired
    DummyCustomerDataTest customerDataTest;

    @Test
    public void generateCustomerOnly() {
        customerDataTest.generate();
    }
}
