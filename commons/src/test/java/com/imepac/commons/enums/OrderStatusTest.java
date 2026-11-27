package com.imepac.commons.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderStatus transitions")
class OrderStatusTest {

    @ParameterizedTest(name = "{0} -> {1} is allowed")
    @CsvSource({
            "PENDING, CONFIRMED",
            "CONFIRMED, PROCESSING",
            "PROCESSING, SHIPPED",
            "SHIPPED, DELIVERED",
            "PENDING, CANCELLED",
            "CONFIRMED, CANCELLED",
            "PROCESSING, CANCELLED",
            "SHIPPED, CANCELLED"
    })
    void allowedTransitions(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} is rejected")
    @CsvSource({
            "PENDING, DELIVERED",
            "PENDING, SHIPPED",
            "CONFIRMED, DELIVERED",
            "SHIPPED, CONFIRMED",
            "PROCESSING, PENDING",
            "CONFIRMED, CONFIRMED"
    })
    void rejectedTransitions(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @ParameterizedTest(name = "{0} is terminal")
    @EnumSource(value = OrderStatus.class, names = {"DELIVERED", "CANCELLED"})
    void terminalStates(OrderStatus status) {
        assertThat(status.isTerminal()).isTrue();
        assertThat(status.allowedTransitions()).isEmpty();
    }

    @Test
    void nullIsNeverAllowed() {
        assertThat(OrderStatus.PENDING.canTransitionTo(null)).isFalse();
    }
}
