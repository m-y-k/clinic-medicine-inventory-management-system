//package com.clinicapp.controller;
//
//import com.clinicapp.model.User;
//import com.clinicapp.security.JwtUtil;
//import com.clinicapp.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/auth")
//@CrossOrigin
//public class AuthController {
//    @Autowired private UserService userService;
//    @Autowired private JwtUtil jwtUtil;
//
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody User user) {
//        User saved = userService.register(user);
//        return ResponseEntity.ok(saved);
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
//        String username = body.get("username");
//        String password = body.get("password");
//
//        User user = userService.findByUsername(username);
//        if (user != null && userService.verifyPassword(password, user.getPassword())) {
//            String token = jwtUtil.generateToken(username);
//            Map<String, String> response = new HashMap<>();
//            response.put("token", token);
//            return ResponseEntity.ok(response);
//        }
//        return ResponseEntity.status(401).body("Invalid credentials");
//    }
//}


package com.clinicapp.controller;

import com.clinicapp.model.User;
import com.clinicapp.repository.UserRepository;
import com.clinicapp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return Map.of("message", "User registered successfully");
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(username, user.getRole());
        return Map.of(
                "token", token,
                "role", user.getRole(),
                "username", username
        );
    }
}
