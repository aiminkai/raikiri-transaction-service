package com.aimi.controller;

import com.aimi.entity.TransactionEntity;
import com.aimi.model.TransactionStatus;
import com.aimi.repository.TransactionRepository;
import com.aimi.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final SagaOrchestratorService sagaService;
    private final TransactionRepository txRepo;

    @PostMapping
    public String startTransaction() {
        return sagaService.startNewTransaction();
    }

    @GetMapping
    public Page<TransactionEntity> getTransactions(
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        if (status != null) {
            return txRepo.findByStatusAndStartedAtBetween(status, from, to, pageable);
        } else {
            return txRepo.findByStartedAtBetween(from, to, pageable);
        }
    }
}