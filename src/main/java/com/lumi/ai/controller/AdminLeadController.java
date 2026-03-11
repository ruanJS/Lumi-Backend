package com.lumi.ai.controller;

import com.lumi.ai.dto.LeadResponseDTO;
import com.lumi.ai.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/leads")
@RequiredArgsConstructor
public class AdminLeadController {

    private final LeadService leadService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<LeadResponseDTO>> getLeads(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(leadService.getAllLeads(search, pageable));
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getMetrics() {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("total", leadService.getTotalLeads());
        metrics.put("today", leadService.getLeadsToday());
        metrics.put("week", leadService.getLeadsThisWeek());
        return ResponseEntity.ok(metrics);
    }
}
