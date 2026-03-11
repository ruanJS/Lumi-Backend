package com.lumi.ai.service;

import com.lumi.ai.dto.*;
import com.lumi.ai.model.PasswordResetToken;
import com.lumi.ai.model.User;
import com.lumi.ai.model.enums.UserRole;
import com.lumi.ai.model.enums.UserStatus;
import com.lumi.ai.repository.PasswordResetTokenRepository;
import com.lumi.ai.repository.UserRepository;
import com.lumi.ai.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl extends AuthService {

    private static final String CODE_RESET_STRING = "0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           PasswordResetTokenRepository tokenRepository,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req, User requestingUser) {
        if (req.getEmail() == null || req.getEmail().isBlank())
            throw new IllegalArgumentException("Email é obrigatório");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email já registrado");
        if (req.getPassword() == null || req.getPassword().isBlank())
            throw new IllegalArgumentException("Senha é obrigatória");
        UserRole role = UserRole.USER;
        try {
            if (req.getRole() != null) {
                UserRole requestedRole = UserRole.valueOf(req.getRole().toUpperCase());
                
                if (requestedRole == UserRole.ADMIN) {
                    long adminCount = userRepository.countByRole(UserRole.ADMIN);
                    // Allow if no admin exists (bootstrap) or if the requester is an ADMIN
                    if (adminCount == 0 || (requestingUser != null && requestingUser.getRole() == UserRole.ADMIN)) {
                        role = UserRole.ADMIN;
                    } else {
                        role = UserRole.USER;
                    }
                } else {
                    role = requestedRole;
                }
            }
        } catch (IllegalArgumentException e) {
            role = UserRole.USER;
        }

        User user = new User();
        user.setName(req.getName().trim());
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(role);
        user.setStatus(UserStatus.ATIVO);
        user.setPhone(req.getPhone());
        user.setCpf(req.getCpf());
        userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getName(), req.getPassword());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(AuthRequest req) {
        User user = userRepository.findTopByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new BadCredentialsException("Credenciais inválidas");

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken))
            throw new BadCredentialsException("Refresh token inválido ou expirado");

        String userId = jwtService.getUserId(refreshToken);

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        if (!request.getNewPassword().equals(request.getConfirmationPassword()))
            throw new IllegalArgumentException("Senhas não coincidem");
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
            throw new BadCredentialsException("Senha atual incorreta");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void createPasswordResetToken(String email) {
        userRepository.findTopByEmail(email).ifPresent(user -> {
            tokenRepository.deleteAll(tokenRepository.findAllByUserId(user.getId()));
            PasswordResetToken token = new PasswordResetToken();
            token.setToken(generateNumericCode(CODE_LENGTH));
            token.setUser(user);
            token.setExpiryDate(Instant.now().plusSeconds(600));
            tokenRepository.save(token);
            emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
        });
    }

    @Override
    public void validateResetCode(String code, String email) {
        User user = userRepository.findTopByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        PasswordResetToken token = tokenRepository.findByToken(code)
                .orElseThrow(() -> new IllegalArgumentException("Código inválido"));

        if (!token.getUser().equals(user) || token.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(token);
            throw new IllegalArgumentException("Código inválido ou expirado");
        }
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Código inválido"));
        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Código expirado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }

    @Override
    public UserDto getMe(UUID userId) {
        return new UserDto(userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado")));
    }

    @Override
    public UserDto getUserById(UUID id) {
        return new UserDto(userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado")));
    }

    @Override
    public UserDto updateUser(UUID id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        userRepository.save(user);
        return new UserDto(user);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        userRepository.delete(user);
    }

    @Override
    public List<UserDto> getAllUsers(String role) {
        return userRepository.findAll().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public AuthResponse generateAdminToken(User user) {
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String userId       = user.getId().toString();
        String email        = user.getEmail();
        String accessToken  = jwtService.generateToken(userId, email);
        String refreshToken = jwtService.generateRefreshToken(userId, email);
        return new AuthResponse(accessToken, refreshToken, new UserDto(user));
    }

    private String generateNumericCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CODE_RESET_STRING.charAt(random.nextInt(CODE_RESET_STRING.length())));
        }
        return sb.toString();
    }
}
