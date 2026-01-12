package com.aimi.service;

import com.aimi.model.SagaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventListener {

    private final SagaOrchestratorService orchestrator;

    @KafkaListener(topics = "saga.events", groupId = "transaction-service-group")
    public void handleEvent(SagaEvent event) {
        if ("COMPLETED".equals(event.getStatus())) {
            orchestrator.onStepCompleted(event.getTxId(), event.getStep());
        } else if ("FAILED".equals(event.getStatus())) {
            orchestrator.onStepFailed(event.getTxId(), event.getStep());
        }
        // COMPENSATED можно игнорировать — он только для логгирования
    }
}
