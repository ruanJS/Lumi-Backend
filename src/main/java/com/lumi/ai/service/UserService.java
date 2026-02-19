package com.lumi.ai.service;

import com.lumi.ai.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User createUser(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);

    User updatePassword(User user, String newPassword);

    User updateUser(User user);

    void deleteUser(UUID id);

    List<User> findAllUsers();
}
