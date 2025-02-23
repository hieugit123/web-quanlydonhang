package com.example.demo.repository;

import com.example.demo.model.DeliveryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryPhotoRepository extends JpaRepository<DeliveryPhoto, Long> {
}
