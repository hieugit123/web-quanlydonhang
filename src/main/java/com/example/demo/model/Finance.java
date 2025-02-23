package com.example.demo.model;

import jakarta.persistence.*;

@Entity
public class Finance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double amountPaid; // Tiền giao

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public Double getAmountSent() {
        return amountSent;
    }

    public void setAmountSent(Double amountSent) {
        this.amountSent = amountSent;
    }

    private Double amountSent; // Đã gửi xếp
}

