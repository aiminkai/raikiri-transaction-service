package com.aimi.service;
import com.aimi.entity.TransactionEntity;
import com.aimi.model.TransactionStatus;
import com.aimi.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorService {

    private final TransactionRepository txRepo;
    private final SagaCommandProducer commandProducer;

    // Для отслеживания текущего шага по txId (в реальности можно хранить в БД)
    private final Map<String, Integer> currentStep = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // При старте можно восстановить незавершённые транзакции из БД
    }

    @Transactional
    public String startNewTransaction() {
        TransactionEntity tx = new TransactionEntity();
        tx.setStatus(TransactionStatus.STARTED);
        tx = txRepo.save(tx);

        log.info("Started new saga: {}", tx.getId());
        currentStep.put(tx.getId(), 1);
        commandProducer.sendExecuteStep(tx.getId(), 1);

        return tx.getId();
    }

    @Transactional
    public void onStepCompleted(String txId, int step) {
        TransactionEntity tx = txRepo.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + txId));

        log.info("Step {} completed for tx {}", step, txId);

        if (step == 1) {
            tx.setStatus(TransactionStatus.STEP1_DONE);
            txRepo.save(tx);
            currentStep.put(txId, 2);
            commandProducer.sendExecuteStep(txId, 2);

        } else if (step == 2) {
            tx.setStatus(TransactionStatus.STEP2_DONE);
            txRepo.save(tx);
            currentStep.put(txId, 3);
            commandProducer.sendExecuteStep(txId, 3);

        } else if (step == 3) {
            tx.setStatus(TransactionStatus.COMPLETED);
            tx.setEndedAt(LocalDateTime.now());
            txRepo.save(tx);
            currentStep.remove(txId);
            log.info("Saga COMPLETED: {}", txId);
        }
    }

    @Transactional
    public void onStepFailed(String txId, int failedStep) {
        TransactionEntity tx = txRepo.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + txId));

        log.warn("Step {} FAILED for tx {}", failedStep, txId);

        // Запускаем компенсацию в обратном порядке
        for (int step = failedStep - 1; step >= 1; step--) {
            commandProducer.sendCompensateStep(txId, step);
        }

        tx.setStatus(TransactionStatus.CANCELLED);
        tx.setEndedAt(LocalDateTime.now());
        txRepo.save(tx);
        currentStep.remove(txId);
        log.info("Saga CANCELLED due to step {} failure: {}", failedStep, txId);
    }
}