package com.lumi.ai.service;

import com.lumi.ai.model.User;
import com.lumi.ai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(User user) {
        if (user == null) throw new IllegalArgumentException("Usuário não pode ser nulo");
        if (user.getPassword() == null || user.getPassword().isBlank()) throw new IllegalArgumentException("Senha é obrigatória");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return userRepository.findTopByEmail(email);
    }

    @Override
    public Optional<User> findById(UUID id) {
        if (id == null) return Optional.empty();
        return userRepository.findById(id);
    }

    @Override
    public User updatePassword(User user, String newPassword) {
        if (user == null) throw new IllegalArgumentException("Usuário não pode ser nulo");
        if (newPassword == null || newPassword.isBlank()) throw new IllegalArgumentException("Nova senha é obrigatória");

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        if (user == null || user.getId() == null) throw new IllegalArgumentException("Usuário ou ID do usuário não pode ser nulo");

        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setRole(user.getRole());
        existingUser.setCpf(user.getCpf());
        existingUser.setStatus(user.getStatus());

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(UUID id) {
        if (id == null) throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        if (!userRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");

        userRepository.deleteById(id);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
