package com.lumi.ai.controller;

import com.lumi.ai.dto.PlanCheckoutRequest;
import com.lumi.ai.model.User;
import com.lumi.ai.repository.UserRepository;
import com.lumi.ai.service.AbacatePayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({ "/api/v1/payments", "/api/payment" })
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final AbacatePayService abacatePayService;
    private final UserRepository userRepository;

    public PaymentController(AbacatePayService abacatePayService, UserRepository userRepository) {
        this.abacatePayService = abacatePayService;
        this.userRepository = userRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> createCheckout(@RequestBody PlanCheckoutRequest request) {
        logger.info("Requisição de checkout recebida: {}", request);
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String email = null;
            var auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                email = auth.getName();
                logger.debug("Usuário autenticado: {}", email);
            }

            User user = null;
            if (email != null) {
                user = userRepository.findTopByEmail(email).orElse(null);
            }

            String checkoutUrl = abacatePayService.createCheckout(request.getPlan(), user);

            responseMap.put("url", checkoutUrl);
            responseMap.put("checkoutUrl", checkoutUrl);
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            logger.error("Erro ao processar checkout: {}", e.getMessage(), e);

            responseMap.put("success", false);
            responseMap.put("error", e.getMessage());
            responseMap.put("details", "Verifique os logs do servidor para mais informações.");

            return ResponseEntity.status(500).body(responseMap);
        }
    }
}