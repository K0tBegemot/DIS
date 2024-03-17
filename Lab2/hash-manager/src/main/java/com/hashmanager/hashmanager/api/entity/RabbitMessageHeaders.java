package com.hashmanager.hashmanager.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class RabbitMessageHeaders {
    @Column(name = "class_id_field", nullable = false)
    private String classIdField;
}