package com.hashmanager.hashmanager.repository;

import com.hashmanager.hashmanager.api.entity.ExecutorServicePartEntity;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExecutorServicePartRepository extends JpaRepository<ExecutorServicePartEntity, Long> {
    public Optional<ExecutorServicePartEntity> findByExecutorIdAndParentTask_Id(Long executorId, Long parentHashCrackTaskId);
}