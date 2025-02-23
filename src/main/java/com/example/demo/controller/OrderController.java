package com.example.demo.controller;

import com.example.demo.model.Finance;
import com.example.demo.model.Order;
import com.example.demo.model.OrderStatus;
import com.example.demo.repository.FinanceRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

@EnableScheduling
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping
    public Page<Order> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String date) {

        Pageable pageable = PageRequest.of(page, size);

        if (date != null && !date.isEmpty()) {
            try {
                if (date.length() == 4) { // Lọc theo năm (yyyy)
                    int year = Integer.parseInt(date);
                    LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay();
                    LocalDateTime endOfYear = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);
                    return orderRepository.findByNgayVeBetween(startOfYear, endOfYear, pageable);
                } else if (date.length() == 7) { // Lọc theo tháng/năm (yyyy-MM)
                    YearMonth yearMonth = YearMonth.parse(date);
                    LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
                    LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
                    System.out.println("OK DA VAO KKK");
                    return orderRepository.findByNgayVeBetween(startOfMonth, endOfMonth, pageable);
                } else if (date.length() == 10) { // Lọc theo ngày/tháng/năm (yyyy-MM-dd)
                    LocalDate parsedDate = LocalDate.parse(date);
                    LocalDateTime startOfDay = parsedDate.atStartOfDay();
                    LocalDateTime endOfDay = parsedDate.atTime(LocalTime.MAX);
                    return orderRepository.findByNgayVeBetween(startOfDay, endOfDay, pageable);
                }
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Định dạng ngày không hợp lệ. Hãy sử dụng yyyy, yyyy-MM hoặc yyyy-MM-dd.");
            }
        }

        return orderRepository.findAll(pageable);
    }



    @GetMapping("/by-status")
    public List<Order> getOrdersByStatus(@RequestParam String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ");
        }
    }

    @GetMapping("/by-status-new")
    public Page<Order> getOrdersByStatusNew(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            Pageable pageable = PageRequest.of(page, size);
            return orderRepository.findByStatus(orderStatus, pageable);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ");
        }
    }


    @PatchMapping("/{orderCode}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable String orderCode) {
        List<Order> ordersByCode = orderRepository.findByOrderCode(orderCode);

        if (ordersByCode.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Order order = ordersByCode.get(0);
        LocalDate ngayHienTai = LocalDate.now();

        List<Order> orders = orderRepository.findOrdersByCustomerPhoneAndDate(order.getCustomerPhone(), ngayHienTai);

        orders.forEach(o -> {
            if(order.getCustomerName().equals(o.getCustomerName())){
                o.setUpdatedAt(LocalDateTime.now());
                o.setStatus(OrderStatus.DA_GIAO);
                orderRepository.save(o);
            }
        });

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> listHangTon = orderRepository.findByCustomerPhoneAndCustomerNameAndStatus(order.getCustomerPhone(), order.getCustomerName(),OrderStatus.HANG_TON, pageable);

        // Trả về listHangTon kèm status "ok"
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("listHangTon", listHangTon.getContent());

        return ResponseEntity.ok(response);
    }



    @PatchMapping("/{orderCode}/status1")
    public ResponseEntity<String> updateOrderStatus1(@PathVariable String orderCode) {
        List<Order> optionalOrder = orderRepository.findByOrderCode(orderCode);

        if (!optionalOrder.isEmpty()) {
            Order order = optionalOrder.get(0);
            order.setStatus(OrderStatus.DA_VE); // Cập nhật trạng thái thành "ĐÃ GIAO"
            order.setNgayVe(LocalDateTime.now()); // Cập nhật thời gian
            orderRepository.save(order); // Lưu vào database
            return ResponseEntity.ok("Cập nhật trạng thái đơn hàng thành công!");
        } else {
            return ResponseEntity.notFound().build(); // Trả về 404 nếu không tìm thấy đơn hàng
        }
    }

    @GetMapping("/finance/total-delivered")
    public Double getTotalDeliveredAmount() {
        return orderRepository.getTotalDeliveredAmount();
    }


    //Lay theo ngay tao
    @GetMapping("/orders")
    public List<Order> getOrdersByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return orderRepository.findByCreatedAtBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    @GetMapping("/getAll")
    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    // API nhận dữ liệu từ frontend và lưu vào database
    @PostMapping("/upload")
    public ResponseEntity<?> uploadOrders(@RequestBody List<Order> orders) {
        System.out.println("Dữ liệu nhận được từ frontend:");
//        orders.forEach(System.out::println);
        System.out.println(orders.size());
        for (Order order : orders) {
            // Đảm bảo trạng thái mặc định là "chưa về"
            order.setStatus(OrderStatus.CHUA_VE);
            order.setUpdatedAt(LocalDateTime.now());
            order.setCreatedAt(LocalDateTime.now());
            order.setNgayVe(order.getNgayVe());

            // Kiểm tra xem orderCode có trùng không
            if (orderRepository.existsByOrderCode(order.getOrderCode())) {
                continue; // Bỏ qua nếu mã đơn đã tồn tại
            }
            System.out.println(order.getCustomerPhone());
            System.out.println(order.getCustomerName());

            orderRepository.save(order);
        }
        return ResponseEntity.ok("Dữ liệu đã được nhập thành công!");
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchOrders(@RequestParam(required = false) String orderCode,
                                          @RequestParam(required = false) String trackingNumber) {
        if (orderCode != null) {
            List<Order> orders = orderRepository.findByOrderCode(orderCode);
            return ResponseEntity.ok(orders);
        }
        if (trackingNumber != null) {
            List<Order> orders = orderRepository.findByTrackingNumber(trackingNumber);
            return ResponseEntity.ok(orders);
        }
        return ResponseEntity.badRequest().body("Vui lòng nhập mã đơn hoặc mã vận đơn để tìm kiếm.");
    }

    @GetMapping("/search1")
    public List<Order> searchOrders(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerPhone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        return orderRepository.searchOrders(orderCode, address, customerName, customerPhone, date, month, year);
    }

    // Cập nhật trạng thái đơn hàng
    @PutMapping("/update-status/{id}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            return ResponseEntity.ok("Cập nhật trạng thái thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    // Xử lý trạng thái hàng tồn (chạy hàng ngày hoặc theo yêu cầu)
    @Scheduled(cron = "0 0 0 * * ?") // Chạy vào 00:00 mỗi ngày
    @PutMapping("/process-pending")
    public ResponseEntity<?> processPendingOrders() {
        List<Order> orders = orderRepository.findByStatus(OrderStatus.DA_VE);
        for (Order order : orders) {
            if (order.getNgayVe().plusDays(1).isBefore(LocalDateTime.now())) {
                order.setStatus(OrderStatus.HANG_TON);
                order.setStatusDays(order.getStatusDays() + 1);
                orderRepository.save(order);
            }
        }
        return ResponseEntity.ok("Đã cập nhật trạng thái hàng tồn!");
    }

    // Cập nhật trạng thái giao hàng theo số điện thoại
    @PutMapping("/deliver/{customerPhone}")
    public ResponseEntity<?> deliverOrders(@PathVariable String customerPhone) {
        List<Order> orders = orderRepository.findByCustomerPhone(customerPhone);
        boolean hasOldOrders = false;

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.DA_VE) {
                order.setStatus(OrderStatus.DA_GIAO);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            } else if (order.getStatus() == OrderStatus.HANG_TON) {
                hasOldOrders = true;
            }
        }

        if (hasOldOrders) {
            return ResponseEntity.ok("Đã giao đơn hàng trong ngày, nhưng còn đơn hàng tồn chưa giao!");
        }
        return ResponseEntity.ok("Đã giao tất cả đơn hàng!");
    }

    @GetMapping("/summary")
    public Map<String, Integer> getOrderSummary() {
        Map<String, Integer> summary = new HashMap<>();
        summary.put("chuaVe", orderRepository.countByStatus(OrderStatus.CHUA_VE));
        summary.put("daVe", orderRepository.countByStatus(OrderStatus.DA_VE));
        summary.put("daGiao", orderRepository.countByStatus(OrderStatus.DA_GIAO));
        summary.put("hangTon", orderRepository.countByStatus(OrderStatus.HANG_TON));
        return summary;
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateOrder(@RequestBody Order updatedOrder) {
        Optional<Order> existingOrderOpt = orderRepository.findById(updatedOrder.getId());

        if (existingOrderOpt.isPresent()) {
            Order existingOrder = existingOrderOpt.get();

            existingOrder.setCustomerName(updatedOrder.getCustomerName());
            existingOrder.setCustomerPhone(updatedOrder.getCustomerPhone());
            existingOrder.setAddress(updatedOrder.getAddress());
            existingOrder.setAmount(updatedOrder.getAmount());
            existingOrder.setStatus(updatedOrder.getStatus());
            existingOrder.setUpdatedAt(LocalDateTime.now());

            orderRepository.save(existingOrder);
            return ResponseEntity.ok("Cập nhật đơn hàng thành công");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn hàng");
        }
    }
}
