package com.example.demo.repository;

import com.example.demo.model.Order;
import com.example.demo.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerPhone(String customerPhone);
    @Query("SELECT o FROM Order o WHERE o.customerPhone = :customerPhone AND FUNCTION('DATE', o.ngayVe) = :ngay")
    List<Order> findOrdersByCustomerPhoneAndDate(@Param("customerPhone") String customerPhone, @Param("ngay") LocalDate ngay);

    Page<Order> findByCustomerPhoneAndStatus(String customerPhone, OrderStatus status, Pageable pageable);
    Page<Order> findByCustomerPhoneAndCustomerNameAndStatus(String customerPhone, String customerName, OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") OrderStatus status);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByNgayVeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);


    List<Order> findByOrderCode(String orderCode);
//    Optional<Order> findById(Long id);

    List<Order> findByTrackingNumber(String trackingNumber);
    boolean existsByOrderCode(String orderCode);
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE " +
            "(:orderCode IS NULL OR o.orderCode = :orderCode) AND " +
            "(:address IS NULL OR o.address LIKE %:address%) AND " +
            "(:customerName IS NULL OR o.customerName LIKE %:customerName%) AND " +
            "(:customerPhone IS NULL OR o.customerPhone = :customerPhone) AND " +
            "(:date IS NULL OR FUNCTION('DATE', o.createdAt) = :date) AND " +
            "(:month IS NULL OR FUNCTION('MONTH', o.createdAt) = :month) AND " +
            "(:year IS NULL OR FUNCTION('YEAR', o.createdAt) = :year)")
    List<Order> searchOrders(String orderCode, String address, String customerName, String customerPhone, LocalDate date, Integer month, Integer year);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.status = 'DA_GIAO'")
    Double getTotalDeliveredAmount();
    int countByStatus(OrderStatus status);
}
