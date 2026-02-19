package com.lumi.ai.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
public class Account {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String type;
    private Double balance;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
