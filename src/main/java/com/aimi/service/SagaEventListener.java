package com.aimi.service;

import com.aimi.model.SagaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventListener {

    private static final String TOPIC = "saga.events";
    private static final String GROUP = "transaction-service-group";
    private static final String COMPLETED = "COMPLETED";
    private static final String FAILED = "FAILED";

    private final SagaOrchestratorService orchestrator;

    @KafkaListener(topics = TOPIC, groupId = GROUP)
    public void handleEvent(SagaEvent event) {
        if (COMPLETED.equals(event.getStatus())) {
            orchestrator.onStepCompleted(event.getTxId(), event.getStep());
        } else if (FAILED.equals(event.getStatus())) {
            orchestrator.onStepFailed(event.getTxId(), event.getStep(), event.getDescription());
        }
    }
}
