package com.lumi.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_EXPIRATION_MINUTES = 5;
    private static final SecureRandom secureRandom = new SecureRandom();

    private static class OtpData {
        String code;
        LocalDateTime expirationTime;
        int attempts;

        OtpData(String code, LocalDateTime expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
            this.attempts = 0;
        }
    }

    private final Map<String, OtpData> otpCache = new ConcurrentHashMap<>();

    public String generateOtp(String identifier) {
        String code = String.format("%06d", secureRandom.nextInt(1000000));
        otpCache.put(identifier, new OtpData(code, LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES)));
        return code;
    }

    public boolean validateOtp(String identifier, String code) {
        OtpData data = otpCache.get(identifier);

        if (data == null) {
            return false;
        }

        if (data.expirationTime.isBefore(LocalDateTime.now())) {
            otpCache.remove(identifier);
            return false;
        }

        if (data.attempts >= 5) {
            otpCache.remove(identifier); // Prevent brute-force on single OTP
            return false; 
        }

        if (data.code.equals(code)) {
            otpCache.remove(identifier); // Single use
            return true;
        } else {
            data.attempts++;
            return false;
        }
    }
}
