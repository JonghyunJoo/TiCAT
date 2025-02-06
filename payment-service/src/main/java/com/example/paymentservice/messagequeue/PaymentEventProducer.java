package com.example.paymentservice.messagequeue;

import com.example.paymentservice.event.PaymentCanceledEvent;
import com.example.paymentservice.event.PaymentFailedEvent;
import com.example.paymentservice.event.PaymentSuccessEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public PaymentEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // 결제 성공 이벤트 발송
    public void sendPaymentSuccessEvent(PaymentSuccessEvent event) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            log.error("Error serializing PaymentSuccessEvent: ", ex);
        }

        kafkaTemplate.send("payment_success_topic", jsonInString);
        log.info("Sent PaymentSuccessEvent: {}", event);
    }

    public void sendPaymentFailedEvent(PaymentFailedEvent event) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            log.error("Error serializing PaymentSuccessEvent: ", ex);
        }

        kafkaTemplate.send("payment_failed_topic", jsonInString);
        log.info("Sent PaymentSuccessEvent: {}", event);
    }

    // 결제 취소 이벤트 발송
    public void sendPaymentCanceledEvent(PaymentCanceledEvent event) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            log.error("Error serializing PaymentCanceledEvent: ", ex);
        }

        kafkaTemplate.send("payment_canceled_topic", jsonInString);
        log.info("Sent PaymentCanceledEvent: {}", event);
    }
}
