package com.example.reservationservice.messagequeue;

import com.example.reservationservice.event.ReservationCanceledEvent;
import com.example.reservationservice.event.ReservationSuccessEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReservationEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ReservationEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
    }
    // 결제 취소 이벤트 발송
    public void sendReservationCanceledEvent(ReservationCanceledEvent event) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            log.error("Error serializing PaymentCanceledEvent: ", ex);
        }

        kafkaTemplate.send("reservation_canceled_topic", jsonInString);
        log.info("Sent PaymentCancelledEvent: {}", event);
    }

    public void sendReservationSuccessEvent(ReservationSuccessEvent event){
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            log.error("Error serializing PaymentCanceledEvent: ", ex);
        }

        kafkaTemplate.send("reservation_success_topic", jsonInString);
        log.info("Sent PaymentCancelledEvent: {}", event);
    }
}
