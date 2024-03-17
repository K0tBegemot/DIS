package com.hashmanager.hashmanager.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "executor_service_part_table")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class ExecutorServicePartEntity extends BaseEntity{
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_task_id", foreignKey = @ForeignKey(name = "executor_service_part_table_fk1"), nullable = false)
    private HashCrackTaskEntity parentTask;
    @Column(name = "executor_id")
    private Long executorId;
    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "parentExecutor", orphanRemoval = true)
    private List<DiapasonPartEntity> parts = new ArrayList<>();
    public void addDiapason(DiapasonPartEntity part)
    {
        part.setParentExecutor(this);
        parts.add(part);
    }
}
