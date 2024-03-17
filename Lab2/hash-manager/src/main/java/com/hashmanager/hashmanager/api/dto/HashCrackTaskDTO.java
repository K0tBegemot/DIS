package com.hashmanager.hashmanager.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class HashCrackTaskDTO {
    @NotNull
    private String hash;
    @NotNull
    @Positive
    private Long maxLength;
}