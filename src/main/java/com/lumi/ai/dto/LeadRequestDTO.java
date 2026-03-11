package com.lumi.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadRequestDTO {
    private String name;
    private String email;
    private String phone;
    private String countryCode;
    private String source;
    private Boolean isWhatsapp;
}
