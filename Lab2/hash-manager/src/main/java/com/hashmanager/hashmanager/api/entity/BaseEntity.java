package com.hashmanager.hashmanager.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(builderMethodName = "defaultBuilder")
@Getter
@Setter
@MappedSuperclass
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    @Builder.Default
    @Column(name = "version", nullable = false)
    private long version = 0L;
}