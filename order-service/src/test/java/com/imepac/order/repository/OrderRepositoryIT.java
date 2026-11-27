package com.imepac.order.repository;

import com.imepac.commons.enums.OrderStatus;
import com.imepac.order.entity.Order;
import com.imepac.order.entity.OrderItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("OrderRepository Integration Tests")
class OrderRepositoryIT {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("order_db_test")
            .withUsername("root")
            .withPassword("root");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        saveOrder(1L, 2);
        saveOrder(1L, 3);
        saveOrder(2L, 1);
        entityManager.flush();
        entityManager.clear();

        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    @Test
    @DisplayName("findAll loads orders and items in a single query")
    void findAllWithoutNPlusOne() {
        List<Order> orders = orderRepository.findAll();
        int totalItems = orders.stream().mapToInt(o -> o.getItems().size()).sum();

        assertThat(orders).hasSize(3);
        assertThat(totalItems).isEqualTo(6);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("findAllByCustomerId loads orders and items in a single query")
    void findByCustomerWithoutNPlusOne() {
        List<Order> orders = orderRepository.findAllByCustomerId(1L);
        int totalItems = orders.stream().mapToInt(o -> o.getItems().size()).sum();

        assertThat(orders).hasSize(2);
        assertThat(totalItems).isEqualTo(5);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    private void saveOrder(Long customerId, int itemCount) {
        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.TEN)
                .items(new ArrayList<>())
                .build();
        for (int i = 0; i < itemCount; i++) {
            order.addItem(OrderItem.builder()
                    .productName("Item " + i)
                    .quantity(1)
                    .unitPrice(BigDecimal.ONE)
                    .build());
        }
        orderRepository.save(order);
    }
}
