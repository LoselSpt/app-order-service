package com.imepac.commons.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida do pedido:
 * <pre>
 * PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
 *    └──────────┴────────────┴──────────┴──→ CANCELLED
 * </pre>
 * DELIVERED e CANCELLED são estados terminais.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }

    public Set<OrderStatus> allowedTransitions() {
        return switch (this) {
            case PENDING -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(PROCESSING, CANCELLED);
            case PROCESSING -> EnumSet.of(SHIPPED, CANCELLED);
            case SHIPPED -> EnumSet.of(DELIVERED, CANCELLED);
            case DELIVERED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    public boolean canTransitionTo(OrderStatus next) {
        return next != null && allowedTransitions().contains(next);
    }
}
