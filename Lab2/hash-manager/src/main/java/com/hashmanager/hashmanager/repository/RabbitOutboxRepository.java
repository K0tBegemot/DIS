package com.hashmanager.hashmanager.repository;

import com.hashmanager.hashmanager.api.entity.RabbitOutboxEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;

public interface RabbitOutboxRepository extends JpaRepository<RabbitOutboxEntity, Long> {
    //Skip lock
    @QueryHints(@QueryHint(name = AvailableSettings.JAKARTA_LOCK_TIMEOUT, value = "-2"))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public List<RabbitOutboxEntity> findSkipLockedBy();
}