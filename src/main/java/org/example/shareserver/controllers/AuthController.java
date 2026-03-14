package org.example.shareserver.controllers;

import jakarta.validation.Valid;
import org.example.shareserver.models.dtos.GoogleAuthDTO;
import org.example.shareserver.models.dtos.LoginDTO;
import org.example.shareserver.models.dtos.RegisterDTO;
import org.example.shareserver.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO dto) {
        return authService.register(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        String email = loginDTO.getEmail() != null ? loginDTO.getEmail().trim() : "";
        String password = loginDTO.getPassword() != null ? loginDTO.getPassword() : "";
        return authService.login(email, password);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@RequestBody GoogleAuthDTO dto) {
        return authService.loginWithGoogle(dto.getIdToken());
    }
}
