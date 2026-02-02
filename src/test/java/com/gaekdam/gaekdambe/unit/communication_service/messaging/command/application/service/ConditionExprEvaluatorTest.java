package com.gaekdam.gaekdambe.unit.communication_service.messaging.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.ConditionExprEvaluator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class ConditionExprEvaluatorTest {

    private final ConditionExprEvaluator eval = new ConditionExprEvaluator();

    @Test
    void evaluate_emptyExpr_returnsTrue() {
        assertThat(eval.evaluate(null, new HashMap<>())).isTrue();
        assertThat(eval.evaluate("   ", new HashMap<>())).isTrue();
    }

    @Test
    void evaluate_validExpr_returnsResult() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("guestCount", 2);
        assertThat(eval.evaluate("#guestCount > 1", vars)).isTrue();
    }

    @Test
    void evaluate_invalidExpr_returnsFalse() {
        Map<String, Object> vars = new HashMap<>();
        assertThat(eval.evaluate("badFunc(1)", vars)).isFalse();
    }
}
