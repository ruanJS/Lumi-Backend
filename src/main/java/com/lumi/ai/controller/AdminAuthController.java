package com.lumi.ai.controller;

import com.lumi.ai.dto.AdminLoginRequestDTO;
import com.lumi.ai.dto.AuthResponse;
import com.lumi.ai.model.User;
import com.lumi.ai.repository.UserRepository;
import com.lumi.ai.service.EmailService;
import com.lumi.ai.service.LoginAttemptService;
import com.lumi.ai.service.OtpService;
import com.lumi.ai.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody AdminLoginRequestDTO request,
            HttpServletRequest httpRequest) {
        
        String ip = httpRequest.getRemoteAddr();
        
        if (loginAttemptService.isBlocked(ip)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas. Tente novamente em 15 minutos.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(ip);
            throw new BadCredentialsException("Credenciais inválidas");
        }

        // Check if user is Admin
        if (user.getRole() != com.lumi.ai.model.enums.UserRole.ADMIN) {
            loginAttemptService.loginFailed(ip);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seu usuário não possui permissões administrativas.");
        }

        loginAttemptService.loginSucceeded(ip);
        
        // Generate and send OTP
        String otp = otpService.generateOtp(user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp);

        return ResponseEntity.ok(Map.of("message", "Código enviado para seu e-mail"));
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (!otpService.validateOtp(email, code)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código inválido ou expirado");
        }

        User user = userRepository.findByEmail(email).get();
        return ResponseEntity.ok(authService.generateAdminToken(user));
    }
}
