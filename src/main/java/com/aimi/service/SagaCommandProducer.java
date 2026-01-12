package com.aimi.service;

import com.aimi.model.SagaCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SagaCommandProducer {

    private final KafkaTemplate<String, SagaCommand> kafkaTemplate;

    public void sendExecuteStep(String txId, int step) {
        SagaCommand cmd = new SagaCommand();
        cmd.setTxId(txId);
        cmd.setStep(step);
        cmd.setAction("execute");
        kafkaTemplate.send("saga.commands", txId, cmd);
    }

    public void sendCompensateStep(String txId, int step) {
        SagaCommand cmd = new SagaCommand();
        cmd.setTxId(txId);
        cmd.setStep(step);
        cmd.setAction("compensate");
        kafkaTemplate.send("saga.commands", txId, cmd);
    }
}