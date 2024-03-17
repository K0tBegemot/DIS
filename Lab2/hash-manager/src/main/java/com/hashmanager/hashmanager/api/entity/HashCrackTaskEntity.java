package com.hashmanager.hashmanager.api.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hashmanager.hashmanager.exception.persistence.EntityNotFoundException;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "hash_crack_task_table")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class HashCrackTaskEntity extends BaseEntity{
    public enum Status{
        IN_PROGRESS("IN_PROGRESS"),
        READY("READY"),
        ERROR("ERROR");
        @Getter(onMethod_ = {@JsonValue})
        private String name;
        private Status(String name1)
        {
            name = name1;
        }
        @JsonCreator
        public static Status of(String name)
        {
            return Arrays.stream(Status.values()).filter((Status value) -> { return value.getName().equals(name);}).findFirst().orElseThrow(EntityNotFoundException::new);
        }
    }
    @Converter(autoApply = true)
    public static class StatusConverter implements AttributeConverter<Status, String>
    {
        @Override
        public String convertToDatabaseColumn(Status attribute) {
            return attribute.getName();
        }

        @Override
        public Status convertToEntityAttribute(String dbData) {
            return Status.of(dbData);
        }
    }
    @Column(name = "hash", nullable = false, unique = false)
    private String hash;
    @Column(name = "max_length", nullable = false, unique = false)
    private Long maxLength;
    @Column(name = "status", nullable = false)
    private Status status;
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "hash_crack_task_data_table",
            joinColumns = {@JoinColumn(name = "parent_task_id")},
            foreignKey = @ForeignKey(name = "hash_crack_task_data_table_fk1"))
    @Column(name = "data", nullable = false)
    @Builder.Default
    private List<String> data = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "parentTask", orphanRemoval = true)
    @Builder.Default
    private List<ExecutorServicePartEntity> parts = new ArrayList<>();
    public void addServicePart(ExecutorServicePartEntity part)
    {
        part.setParentTask(this);
        parts.add(part);
    }
    public void addDataPart(String newData){
        data.add(newData);
    }
    public void addDataPartAll(List<String> newDataList)
    {
        data.addAll(newDataList);
    }
}