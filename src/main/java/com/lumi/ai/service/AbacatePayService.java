package com.lumi.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AbacatePayService {

    private static final Logger logger = LoggerFactory.getLogger(AbacatePayService.class);

    @Value("${abacatepay.api.key}")
    private String apiKey;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${abacatepay.webhook.secret}")
    private String webhookSecret;

    private final WebClient webClient;

    public AbacatePayService(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.abacatepay.com/v1")
                .build();
    }

    public String createCheckout(String plan, com.lumi.ai.model.User user) {
        logger.info("Iniciando criação de checkout na AbacatePay. Plano: {}, Usuário: {}",
                plan, (user != null ? user.getEmail() : "Anônimo"));

        Map<String, Object> product = new HashMap<>();
        if ("pro".equalsIgnoreCase(plan)) {
            product.put("name", "Lumi AI Pro");
            product.put("description", "Assinatura mensal do plano Pro");
            product.put("price", 1990);
        } else {
            product.put("name", "Lumi AI Plan");
            product.put("description", "Assinatura Lumi AI");
            product.put("price", 100);
        }
        product.put("quantity", 1);
        product.put("externalId", "PRO_PLAN");

        Map<String, String> customer = new HashMap<>();
        if (user != null) {
            customer.put("name", user.getName() != null && !user.getName().isEmpty() ? user.getName() : "Usuário Lumi");
            customer.put("email", user.getEmail() != null ? user.getEmail() : "contato@lumi-ai.com.br");

            // Sempre enviar taxId e cellphone como string (mesmo que vazia) para evitar 422
            customer.put("taxId", sanitizeNumber(user.getCpf()));
            customer.put("cellphone", sanitizeNumber(user.getPhone()));

            logger.debug("Dados do cliente preparados: email={}, hasTaxId={}, hasCellphone={}",
                    customer.get("email"), !customer.get("taxId").isEmpty(), !customer.get("cellphone").isEmpty());
        } else {
            customer.put("name", "Cliente Lumi AI");
            customer.put("email", "contato@lumi-ai.com.br");
            customer.put("taxId", "");
            customer.put("cellphone", "");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("frequency", "ONE_TIME");
        request.put("methods", List.of("PIX"));
        request.put("products", List.of(product));
        request.put("returnUrl", frontendUrl + "/dashboard");
        request.put("completionUrl", frontendUrl + "/dashboard");
        request.put("customer", customer);

        try {
            logger.debug("Enviando payload para AbacatePay: {}", request);

            JsonNode response = webClient.post()
                    .uri("/billing/create")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.has("data") || response.get("data").get("url") == null) {
                String errorBody = (response != null) ? response.toString() : "No response";
                logger.error("Erro no retorno da AbacatePay: {}", errorBody);
                throw new RuntimeException("Erro na resposta da AbacatePay: " + errorBody);
            }

            String url = response.get("data").get("url").asText();
            logger.info("URL de checkout gerada com sucesso: {}", url);
            return url;

        } catch (WebClientResponseException e) {
            String errorResponse = e.getResponseBodyAsString();
            logger.error("Erro HTTP na AbacatePay ({}): {}", e.getStatusCode(), errorResponse);
            throw new RuntimeException("Erro na API da AbacatePay: " + errorResponse, e);
        } catch (Exception e) {
            logger.error("Erro inesperado na integração AbacatePay: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao processar pagamento", e);
        }
    }

    public boolean verifyWebhookSignature(String payload, String signatureHeader) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hash = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equalsIgnoreCase(signatureHeader);
        } catch (Exception e) {
            logger.error("Falha ao verificar assinatura do webhook: {}", e.getMessage());
            return false;
        }
    }

    private String sanitizeNumber(String value) {
        return value != null ? value.replaceAll("[^0-9]", "") : "";
    }
}
