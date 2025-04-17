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
//public class StatusExtractor implements CommandLineRunner {
//
//    private static final Logger logger = LoggerFactory.getLogger(StatusExtractor.class);
//    private final JdbcTemplate jdbcTemplate;
//    private final KafkaProducer<String, String> producer;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Value("${kafka.topic.statuses}")
//    private String topic;
//
//    public StatusExtractor(JdbcTemplate jdbcTemplate, @Value("${kafka.bootstrap-servers}") String kafkaServers) {
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
//    public void extractStatuses() {
//        logger.info("Sprawdzanie nowych statusów w bazie DB_TMS");
//
//        String query = """
//                SELECT
//                    Eveny_UserName, Event, She_SendingID, She_Transfer, Longitude, Latitude,
//                    HubExternalId, PicureGuid, AdrID, ScanQuality, Source
//                FROM TrackIT.dbo.StatusHistoryExt_Log
//                """;
//
//        jdbcTemplate.query(query, (rs, rowNum) -> {
//            try {
//                Map<String, Object> statusUpdate = new HashMap<>();
//                statusUpdate.put("Id", 0);  // Generator ID
//                statusUpdate.put("DeviceId", "");  // Brak danych
//                statusUpdate.put("UserName", rs.getString("Eveny_UserName"));
//                statusUpdate.put("ScanReportCode", rs.getString("Event"));
//                statusUpdate.put("BarCode", "");  // Na razie puste
//                statusUpdate.put("OrderNo", rs.getString("She_SendingID"));
//                statusUpdate.put("Recipient", "");
//                statusUpdate.put("Sender", "");
//                statusUpdate.put("Comments", "");
//                statusUpdate.put("Parameters", "");
//                statusUpdate.put("TriggerTime", rs.getTimestamp("She_Transfer"));
//                statusUpdate.put("Longitude", rs.getDouble("Longitude"));
//                statusUpdate.put("Latitude", rs.getDouble("Latitude"));
//                statusUpdate.put("RouteCode", "");
//                statusUpdate.put("RouteDate", "");  // Możemy ustawić getdate() today
//                statusUpdate.put("HubExternalId", rs.getString("HubExternalId"));
//                statusUpdate.put("SignatureGuid", "00000000-0000-0000-0000-000000000000");
//                statusUpdate.put("PictureGuid", rs.getString("PicureGuid"));
//                statusUpdate.put("AddressId", rs.getInt("AdrID"));
//                statusUpdate.put("ManualScan", rs.getString("ScanQuality") == null);
//                statusUpdate.put("LocationEstimated", true);
//                statusUpdate.put("RouteId", 0);
//                statusUpdate.put("RouteLegId", 0);
//                statusUpdate.put("ClientName", rs.getString("Source"));
//                statusUpdate.put("TimeWindowFrom", "");
//                statusUpdate.put("TimeWindowTo", "");
//                statusUpdate.put("Duration", 0);
//                statusUpdate.put("Distance", 0);
//
//                String statusJson = objectMapper.writeValueAsString(statusUpdate);
//                producer.send(new ProducerRecord<>(topic, statusJson));
//                logger.info("Wysłano status do Kafka: " + statusJson);
//            } catch (Exception e) {
//                logger.error("Błąd podczas serializacji JSON", e);
//            }
//            return null;
//        });
//    }
//
//    @Override
//    public void run(String... args) {
//        extractStatuses();
//    }
//
//    @PreDestroy
//    public void closeProducer() {
//        logger.info("Zamykanie producenta Kafka...");
//        producer.close();
//    }
//}
