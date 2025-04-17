//package com.example.TES.extractor;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import jakarta.annotation.PreDestroy;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
//@Component
//public class OrderExtractor implements CommandLineRunner {
//
//    private static final Logger logger = LoggerFactory.getLogger(OrderExtractor.class);
//    private final JdbcTemplate jdbcTemplate;
//    private final KafkaProducer<String, String> producer;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Value("${kafka.topic.orders}")
//    private String topic;
//
//    public OrderExtractor(JdbcTemplate jdbcTemplate, @Value("${kafka.bootstrap.servers}") String kafkaServers) {
//        this.jdbcTemplate = jdbcTemplate;
//
//        Properties props = new Properties();
//        props.put("bootstrap.servers", kafkaServers);
//        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        this.producer = new KafkaProducer<>(props);
//    }
//
//    @Scheduled(fixedDelay = 5000)
//    public void extractOrders() {
//        logger.info("Sprawdzanie nowych zamówień w bazie DB_TMS");
//
//        jdbcTemplate.query("SELECT SendingID, Barcode FROM TrackIT.dbo.Sending WHERE SendingID = '300098168'",
//                (rs, rowNum) -> {
//                    try {
//                        Map<String, Object> orderMap = new HashMap<>();
//                        orderMap.put("SendingID", rs.getInt("SendingID"));
//                        orderMap.put("Barcode", rs.getString("Barcode"));
//
//                        String orderJson = objectMapper.writeValueAsString(orderMap);
//                        producer.send(new ProducerRecord<>(topic, orderJson));
//                        logger.info("Wysłano zamówienie do Kafka: " + orderJson);
//                    } catch (Exception e) {
//                        logger.error("Błąd podczas serializacji JSON", e);
//                    }
//                    return null;
//                });
//    }
//
//    @Override
//    public void run(String... args) {
//        extractOrders();
//    }
//
//    @PreDestroy
//    public void closeProducer() {
//        logger.info("Zamykanie producenta Kafka...");
//        producer.close();
//    }
//}