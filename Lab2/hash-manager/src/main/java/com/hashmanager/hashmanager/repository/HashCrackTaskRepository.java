package com.hashmanager.hashmanager.repository;

import com.hashmanager.hashmanager.api.entity.HashCrackTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashCrackTaskRepository extends JpaRepository<HashCrackTaskEntity, Long> {
}