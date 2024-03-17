package com.hashmanager.hashmanager.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class RabbitMessageProperties {
    @Column(name = "mime_type", nullable = false)
    private String contentType;
    @Column(name = "encoding", nullable = false)
    private String contentEncoding;
    @Column(name = "content_length", nullable = false)
    private Long contentLength;
    @Embedded
    private RabbitMessageHeaders headers;
}