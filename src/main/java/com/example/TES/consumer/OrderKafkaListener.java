package com.example.TES.consumer;

import com.example.TES.dto.OrderEvent;
import com.example.TES.service.OrderProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderKafkaListener {

    private final ObjectMapper objectMapper;

    @Autowired
    private OrderProcessingService orderProcessingService;

    public OrderKafkaListener() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @KafkaListener(topics = "danxils_orders", groupId = "tes-consumer-group")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            log.info("Received message from Kafka: {}", record.value());
            OrderEvent orderEvent = objectMapper.readValue(record.value(), OrderEvent.class);
            orderProcessingService.processOrder(orderEvent);
        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
        }
    }
}
