package com.aimi.repository;


import com.aimi.entity.TransactionEntity;
import com.aimi.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
    Page<TransactionEntity> findByStatusAndStartedAtBetween(
            TransactionStatus status,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    Page<TransactionEntity> findByStartedAtBetween(
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}