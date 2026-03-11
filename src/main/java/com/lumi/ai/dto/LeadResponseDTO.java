package com.lumi.ai.dto;

import com.lumi.ai.model.Lead;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String countryCode;
    private String source;
    private Boolean isWhatsapp;
    private LocalDateTime createdAt;
    
    public static LeadResponseDTO fromEntity(Lead lead) {
        return LeadResponseDTO.builder()
                .id(lead.getId())
                .name(lead.getName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .countryCode(lead.getCountryCode())
                .source(lead.getSource())
                .isWhatsapp(lead.getIsWhatsapp())
                .createdAt(lead.getCreatedAt())
                .build();
    }
}
