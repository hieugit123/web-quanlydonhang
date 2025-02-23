package com.example.demo.controller;

import com.example.demo.DTO.LoginRequest;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Hoặc "http://127.0.0.1:5501" nếu chỉ chấp nhận một frontend cụ thể
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());

        // Kiểm tra tài khoản tồn tại và mật khẩu đúng
        if (user.isPresent() && user.get().getPassword().equals(request.getPassword())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đăng nhập thành công!");
            response.put("token", "fake-jwt-token"); // Nếu có JWT, thay bằng token thực
            response.put("username", user.get().getUsername());
            response.put("role", user.get().getRole());

            return ResponseEntity.ok(response);
        }

        // Trả về JSON lỗi
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Sai tên đăng nhập hoặc mật khẩu!");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/createuser")
    public ResponseEntity<String> creatAdmin() {
        User user =  new User();
        user.setPassword("123456@");
        user.setRole(Role.ADMIN);
        user.setUsername("xuanzzz123z");
        userRepository.save(user);
        return ResponseEntity.ok("Tạo tài khoản thành công!");
    }

    @PostMapping("/users")
    public ResponseEntity<String> addUser(@RequestBody User user) {
//        if (currentUser.getRole() != Role.ADMIN) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền thêm tài khoản!");
//        }

//        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword())); // Mã hóa mật khẩu
        userRepository.save(user);
        return ResponseEntity.ok("Tạo tài khoản thành công!");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
//        if (currentUser.getRole() != Role.ADMIN) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền xóa tài khoản!");
//        }

        userRepository.deleteById(id);
        return ResponseEntity.ok("Xóa tài khoản thành công!");
    }

    @PatchMapping("/users/{id}/password")
    public ResponseEntity<String> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setPassword(body.get("password")); // Cập nhật mật khẩu
        userRepository.save(user);
        return ResponseEntity.ok("Cập nhật mật khẩu thành công!");
    }

}
