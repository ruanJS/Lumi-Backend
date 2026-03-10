package com.lumi.ai.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumi.ai.model.User;
import com.lumi.ai.model.enums.UserStatus;
import com.lumi.ai.service.AbacatePayService;
import com.lumi.ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping({ "/api/webhooks", "/webhook" })
public class WebhookController {

    private final AbacatePayService abacatePayService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Value("${abacatepay.webhook.secret}")
    private String webhookSecret;

    @Autowired
    public WebhookController(
            AbacatePayService abacatePayService,
            UserService userService) {

        this.abacatePayService = abacatePayService;
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/abacatepay")
    public ResponseEntity<?> handleAbacatePayWebhook(
            @RequestParam(value = "webhookSecret", required = false) String secretFromUrl,
            @RequestHeader(value = "X-Abacate-Signature", required = false) String signature,
            @RequestBody String payloadRaw) {

        try {
            System.out.println("Webhook AbacatePay recebido");

            // 1️⃣ Validação de Segurança
            boolean isAuthorized = false;

            // Valida por Secret na URL (se fornecido)
            if (secretFromUrl != null && secretFromUrl.equals(webhookSecret)) {
                isAuthorized = true;
            }

            // Valida por Assinatura HMAC (se fornecido - prioritário e mais seguro)
            if (signature != null) {
                boolean validSignature = abacatePayService.verifyWebhookSignature(payloadRaw, signature);
                if (validSignature) {
                    isAuthorized = true;
                } else {
                    System.err.println("Assinatura HMAC inválida");
                    return ResponseEntity.status(401).body("Invalid signature");
                }
            }

            // Se não houver segredo na URL nem assinatura válida, bloqueia
            if (!isAuthorized) {
                System.err.println("Webhook não autorizado: Segredo ausente ou inválido");
                return ResponseEntity.status(401).body("Unauthorized");
            }

            // 2️⃣ Parse do JSON
            JsonNode root = objectMapper.readTree(payloadRaw);
            String event = root.path("event").asText();

            if (event == null || event.isEmpty()) {
                event = root.path("type").asText();
            }

            System.out.println("Evento Webhook: " + event);

            // 3️⃣ Extrair email do cliente para ativação
            String customerEmail = root.path("data").path("customer").path("email").asText();

            if (customerEmail == null || customerEmail.isEmpty()) {
                System.err.println("Email do cliente não encontrado no payload");
                return ResponseEntity.ok("Webhook ignored: Email missing");
            }

            Optional<User> userOptional = userService.findByEmail(customerEmail);

            if (userOptional.isEmpty()) {
                System.err.println("Usuário não encontrado: " + customerEmail);
                return ResponseEntity.ok("Webhook ignored: User not found");
            }

            User user = userOptional.get();

            // 4️⃣ Processar conforme o evento v1
            switch (event) {
                case "billing.paid":
                    user.setStatus(UserStatus.ATIVO);
                    System.out.println("ASSINATURA ATIVADA: " + customerEmail);
                    break;

                case "billing.disputed":
                    user.setStatus(UserStatus.BLOQUEADO);
                    System.out.println("ASSINATURA BLOQUEADA (Chargeback): " + customerEmail);
                    break;

                case "billing.failed":
                case "billing.refunded":
                    System.out.println("Evento de pagamento (falha/reembolso): " + event + " para " + customerEmail);
                    break;

                default:
                    System.out.println("Evento ignorado pelo sistema: " + event);
                    return ResponseEntity.ok("Event received and ignored");
            }

            userService.updateUser(user);
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            System.err.println("Erro ao processar webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}