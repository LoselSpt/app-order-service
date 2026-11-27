package com.imepac.order.repository;

import com.imepac.commons.enums.OrderStatus;
import com.imepac.order.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Carrega os itens na mesma consulta para evitar N+1 ao serializar a lista. */
    @Override
    @EntityGraph(attributePaths = "items")
    List<Order> findAll();

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByCustomerId(Long customerId);

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(Long id);
}
