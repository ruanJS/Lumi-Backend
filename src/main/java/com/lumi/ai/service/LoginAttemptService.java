package com.lumi.ai.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 5;
    private final int BLOCK_DURATION_MINUTES = 15;
    
    private static class Attempt {
        int count;
        LocalDateTime lastAttempt;
        Attempt(int count, LocalDateTime lastAttempt) {
            this.count = count;
            this.lastAttempt = lastAttempt;
        }
    }

    private final Map<String, Attempt> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
    }

    public void loginFailed(String key) {
        Attempt attempt = attemptsCache.getOrDefault(key, new Attempt(0, LocalDateTime.now()));
        
        if (attempt.lastAttempt.plusMinutes(BLOCK_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
            attempt.count = 0; // Reset if block expired
        }
        
        attempt.count++;
        attempt.lastAttempt = LocalDateTime.now();
        attemptsCache.put(key, attempt);
    }

    public boolean isBlocked(String key) {
        Attempt attempt = attemptsCache.get(key);
        if (attempt == null) {
            return false;
        }
        
        if (attempt.lastAttempt.plusMinutes(BLOCK_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
            attemptsCache.remove(key);
            return false;
        }
        
        return attempt.count >= MAX_ATTEMPT;
    }
}
