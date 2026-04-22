package com.minimartph.Minit.controller;

import com.minimartph.Minit.dto.LoginRequest;
import com.minimartph.Minit.dto.LoginResponse;
import com.minimartph.Minit.entity.User;
import com.minimartph.Minit.security.JwtUtil;
import com.minimartph.Minit.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private JwtUtil jwtUtil;

  @Autowired private UserService userService;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    User user = userService.findUserByUsername(request.getUsername());
    String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

    return ResponseEntity.ok(
        new LoginResponse(user.getId(), token, user.getUsername(), user.getRole().name()));
  }
}
