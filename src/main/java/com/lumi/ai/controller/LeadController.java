package com.lumi.ai.controller;

import com.lumi.ai.dto.LeadRequestDTO;
import com.lumi.ai.dto.LeadResponseDTO;
import com.lumi.ai.service.LeadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping("/capture")
    public ResponseEntity<LeadResponseDTO> captureLead(
            @RequestBody LeadRequestDTO request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        
        return ResponseEntity.ok(leadService.captureLead(request, ipAddress, userAgent));
    }
}
