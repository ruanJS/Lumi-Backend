package com.lumi.ai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "transaction_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = true)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isSystem = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToMany(mappedBy = "tags")
    private Set<Transaction> transactions;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
