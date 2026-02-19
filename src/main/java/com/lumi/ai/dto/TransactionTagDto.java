package com.lumi.ai.dto;

import com.lumi.ai.model.TransactionTag;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTagDto {

    private UUID id;
    private UUID userId;
    private String slug;
    private String name;
    private boolean isSystem;

    public TransactionTagDto(TransactionTag tag) {
        this.id = tag.getId();
        this.userId = tag.getUserId();
        this.slug = tag.getSlug();
        this.name = tag.getName();
        this.isSystem = tag.isSystem();
    }
}
