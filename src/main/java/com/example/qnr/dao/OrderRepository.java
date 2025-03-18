package com.example.qnr.dao;

import com.example.qnr.resources.Order;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing the Order entity in the database.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createDate DESC")
    Optional<List<Order>> findOrdersByStatusSorted(@Param("status") String status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Order o WHERE o.status = :status")
    int deleteOrdersByStatus(@Param("status") String status);
}
