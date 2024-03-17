package com.hashmanager.hashmanager.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "diapason_part_table")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class DiapasonPartEntity extends BaseEntity{
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_executor_id", foreignKey = @ForeignKey(name = "diapason_part_table_fk1"), nullable = false)
    private ExecutorServicePartEntity parentExecutor;
    @Column(name = "word_number", nullable = false)
    private Long wordNumber;
    @Column(name = "first_word", nullable = false)
    private Long firstWordIndex;
    @Column(name = "word_length", nullable = false)
    private Long wordLength;
}
