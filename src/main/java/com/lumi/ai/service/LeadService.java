package com.lumi.ai.service;

import com.lumi.ai.dto.LeadRequestDTO;
import com.lumi.ai.dto.LeadResponseDTO;
import com.lumi.ai.model.Lead;
import com.lumi.ai.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LeadService {
    
    private final LeadRepository leadRepository;
    private final EmailService emailService;

    @Transactional
    public LeadResponseDTO captureLead(LeadRequestDTO request, String ipAddress, String userAgent) {
        if (leadRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já está na lista de espera");
        }

        Lead lead = Lead.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .countryCode(request.getCountryCode())
                .source(request.getSource() != null ? request.getSource() : "landing_page")
                .isWhatsapp(request.getIsWhatsapp() != null ? request.getIsWhatsapp() : false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        lead = leadRepository.save(lead);
        
        // Dispara e-mail de lista de espera
        emailService.sendWaitlistEmail(lead.getEmail(), lead.getName());

        return LeadResponseDTO.fromEntity(lead);
    }

    @Transactional(readOnly = true)
    public Page<LeadResponseDTO> getAllLeads(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return leadRepository.search(search, pageable).map(LeadResponseDTO::fromEntity);
        }
        return leadRepository.findAll(pageable).map(LeadResponseDTO::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public long getTotalLeads() {
        return leadRepository.count();
    }
    
    @Transactional(readOnly = true)
    public long getLeadsToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return leadRepository.countByCreatedAtAfter(startOfDay);
    }
    
    @Transactional(readOnly = true)
    public long getLeadsThisWeek() {
        LocalDateTime startOfWeek = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1).atStartOfDay();
        return leadRepository.countByCreatedAtAfter(startOfWeek);
    }
}
