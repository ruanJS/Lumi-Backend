package com.lumi.ai.service;

import com.lumi.ai.dto.*;
import com.lumi.ai.model.User;

import java.util.List;
import java.util.UUID;

public abstract class AuthService {

    public abstract AuthResponse register(RegisterRequest request, User requestingUser);

    public abstract AuthResponse login(AuthRequest request);

    public abstract void createPasswordResetToken(String email);

    public abstract void validateResetCode(String code, String email);

    public abstract void resetPassword(String token, String newPassword);

    public abstract void changePassword(UUID userId, ChangePasswordRequest request);

    public abstract UserDto getMe(UUID userId);

    public abstract UserDto getUserById(UUID id);

    public abstract UserDto updateUser(UUID id, UserDto dto);

    public abstract void deleteUser(UUID id);

    public abstract List<UserDto> getAllUsers(String role);

    public abstract AuthResponse refreshToken(String refreshToken);

    public abstract AuthResponse generateAdminToken(User user);
}
