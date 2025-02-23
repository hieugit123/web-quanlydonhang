package com.example.demo.controller;

import com.example.demo.model.DeliveryPhoto;
import com.example.demo.model.Order;
import com.example.demo.repository.DeliveryPhotoRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/delivery-photo")
@CrossOrigin(origins = "*")
public class DeliveryPhotoController {

    @Value("${upload.path:/app/uploads/}")
    private String uploadPath;
    private final DeliveryPhotoRepository photoRepository;
    private final OrderRepository orderRepository;

    public DeliveryPhotoController(DeliveryPhotoRepository photoRepository, OrderRepository orderRepository) {
        this.photoRepository = photoRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    @PostMapping("/upload/{orderCode}")
    public ResponseEntity<?> uploadPhoto(@PathVariable String orderCode, @RequestParam("file") MultipartFile file) {
        try {
//            System.out.println("kkk" + orderId);
            List<Order> orderOptional = orderRepository.findByOrderCode(orderCode);
            if (orderOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
            }

            Order order = orderOptional.get(0);

            // Kiểm tra file
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
            }

            // **Tạo thư mục nếu chưa có**
            File directory = new File(uploadPath);
            if (!directory.exists()) {
                directory.mkdirs();
                System.out.println("Created directory: " + uploadPath);
            }

            // Lưu file vào thư mục cố định
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = Paths.get(uploadPath, fileName).toString();
            System.out.println("Saving file to: " + filePath);
            file.transferTo(new File(filePath));

            // Lưu vào database
            DeliveryPhoto photo = new DeliveryPhoto();
            photo.setOrder(order);
            photo.setPhotoUrl("/uploads/" + fileName);
            photoRepository.save(photo);
            order.setUrl(photo.getPhotoUrl());
            orderRepository.save(order);
            System.out.println("🔥 Đang lưu vào database: " + photo.getPhotoUrl());


            return ResponseEntity.ok(photo);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi lưu ảnh");
        }
    }

}
