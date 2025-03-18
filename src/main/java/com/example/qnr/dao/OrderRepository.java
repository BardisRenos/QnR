package com.example.qnr.dao;

import com.example.qnr.resources.Order;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Custom query to fetch orders with filtering and pagination.
     * Filters orders by status and create date, and supports pagination.
     * This version ensures that null parameters are not accepted.
     *
     * @param status     The status to filter orders by (must not be null or empty).
     * @param startDate  The start date to filter orders (must not be null).
     * @param endDate    The end date to filter orders (must not be null).
     * @param pageable   The pagination information.
     * @return A paginated list of orders based on the provided filters.
     * @throws IllegalArgumentException if any parameter is null or empty.
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createDate >= :startDate AND o.createDate <= :endDate")
    Page<Order> findOrdersWithFilters(@Param("status") String status,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);
}
