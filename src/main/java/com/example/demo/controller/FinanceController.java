package com.example.demo.controller;

import com.example.demo.model.Finance;
import com.example.demo.repository.FinanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "*")
public class FinanceController {

    @Autowired
    private FinanceRepository financeRepository;

    // **Lấy item đầu tiên**
    @GetMapping("/first")
    public ResponseEntity<Finance> getFirstFinance() {
        Optional<Finance> finance = financeRepository.findAll().stream().findFirst();
        return finance.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // **Thêm mới một tài chính**
    @PostMapping("/add")
    public ResponseEntity<Finance> addFinance(@RequestBody Finance finance) {
        Finance savedFinance = financeRepository.save(finance);
        return ResponseEntity.ok(savedFinance);
    }

    // **Cập nhật tài chính theo ID**
    @PutMapping("/update/{id}")
    public ResponseEntity<Finance> updateFinance(@PathVariable Long id, @RequestBody Finance financeDetails) {
        Optional<Finance> existingFinance = financeRepository.findById(id);
        if (existingFinance.isPresent()) {
            Finance finance = existingFinance.get();
            finance.setAmountPaid(financeDetails.getAmountPaid());
            finance.setAmountSent(financeDetails.getAmountSent());
            financeRepository.save(finance);
            return ResponseEntity.ok(finance);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // **Xóa tài chính theo ID**
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFinance(@PathVariable Long id) {
        if (financeRepository.existsById(id)) {
            financeRepository.deleteById(id);
            return ResponseEntity.ok("Deleted successfully!");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
