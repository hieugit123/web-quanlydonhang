package com.example.demo.repository;


import com.example.demo.model.Finance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinanceRepository extends JpaRepository<Finance, Long> {
//    Optional<Finance> findByOrderId(Long orderId);
}