package com.hashmanager.hashmanager.api.dto;

import com.hashmanager.hashmanager.api.entity.HashCrackTaskEntity;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class StatusDTO {
    private HashCrackTaskEntity.Status status;
    private List<String> data;
}
