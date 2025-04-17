//package com.example.TES.extractor;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PreDestroy;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.clients.producer.RecordMetadata;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.Arrays;
//
//@Component
//public class AddressExtractor implements CommandLineRunner {
//
//    private static final Logger logger = LoggerFactory.getLogger(AddressExtractor.class);
//    private final JdbcTemplate jdbcTemplate;
//    private final KafkaProducer<String, String> producer;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Value("${kafka.topic.address}")
//    private String topic;
//
//    // In-memory tracking of the last processed LSN.
//    // For production, consider persisting this value externally.
//    private byte[] lastProcessedLsn = null;
//
//    // Upper bound delay in seconds (to allow CDC capture latency).
//    private static final int UPPER_BOUND_DELAY_SECONDS = 120; // 2 minutes
//
//    public AddressExtractor(JdbcTemplate jdbcTemplate, @Value("${kafka.bootstrap-servers}") String kafkaServers) {
//        this.jdbcTemplate = jdbcTemplate;
//        Properties props = new Properties();
//        props.setProperty("bootstrap.servers", kafkaServers);
//        props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        // Additional Kafka producer properties can be set here.
//        this.producer = new KafkaProducer<>(props);
//    }
//
//    // Helper method to parse latitude/longitude values.
//    private Double parseDoubleFromString(String value) {
//        if (value == null || value.trim().isEmpty()) {
//            return 0.0;
//        }
//        try {
//            return Double.parseDouble(value.replace(",", "."));
//        } catch (NumberFormatException e) {
//            logger.error("Failed to parse double from value: " + value, e);
//            return 0.0;
//        }
//    }
//
//    // Helper to convert binary LSN to hex string for logging.
//    private String lsnToHex(byte[] lsn) {
//        if (lsn == null) return "null";
//        StringBuilder sb = new StringBuilder("0x");
//        for (byte b : lsn) {
//            sb.append(String.format("%02X", b));
//        }
//        return sb.toString();
//    }
//
//    @Scheduled(fixedDelay = 40000)
//    public void extractAddress() {
//        logger.info("Starting CDC extraction from the Address table.");
//
//        // 1. Retrieve the current (upper bound) LSN, accounting for a delay.
//        String toLsnSql = "SELECT sys.fn_cdc_map_time_to_lsn('largest less than or equal', DATEADD(SECOND, -?, GETDATE()))";
//        byte[] toLsn = jdbcTemplate.queryForObject(toLsnSql, new Object[]{UPPER_BOUND_DELAY_SECONDS}, byte[].class);
//        if (toLsn == null) {
//            logger.warn("Unable to retrieve the current upper bound LSN.");
//            return;
//        }
//        logger.info("Current upper bound LSN retrieved: {}", lsnToHex(toLsn));
//
//        // 2. Determine the lower bound LSN.
//        byte[] lowerBound;
//        if (lastProcessedLsn == null) {
//            // On the first run, compute a default lower bound (5 minutes ago).
//            String lowerBoundSql = "SELECT sys.fn_cdc_map_time_to_lsn('smallest greater than or equal', DATEADD(MINUTE, -5, GETDATE()))";
//            lowerBound = jdbcTemplate.queryForObject(lowerBoundSql, byte[].class);
//            logger.info("No previous LSN found. Using default lower bound LSN: {}", lsnToHex(lowerBound));
//        } else {
//            lowerBound = lastProcessedLsn;
//            logger.info("Using last processed LSN as lower bound: {}", lsnToHex(lowerBound));
//        }
//
//        // 3. Query the CDC table for changes between lowerBound and toLsn.
//        String sql = "SELECT *, dac.__$start_lsn AS start_lsn " +
//                "FROM TrackIT.cdc.dbo_Adr_CT AS dac " +
//                "LEFT JOIN TrackIT.cdc.dbo_Street_CT dsc ON dsc.StreetID = dac.StreetID " +
//                "LEFT JOIN TrackIT.cdc.dbo_Postal_CT dpc ON dpc.PostalID = dsc.PostalID " +
//                "WHERE dac.__$start_lsn >= ? AND dac.__$start_lsn <= ?";
//
//        // Track the maximum LSN encountered in this batch.
//        AtomicReference<byte[]> maxLsn = new AtomicReference<>(lastProcessedLsn);
//
//        List<Integer> processedRows = jdbcTemplate.query(sql, new Object[]{lowerBound, toLsn}, (rs, rowNum) -> {
//            try {
//                byte[] currentLsn = rs.getBytes("start_lsn");
//                logger.debug("Row {} LSN: {}", rowNum, lsnToHex(currentLsn));
//
//                // If lastProcessedLsn is not null and currentLsn equals it, skip to avoid duplicates.
//                if (lastProcessedLsn != null && Arrays.equals(currentLsn, lastProcessedLsn)) {
//                    logger.debug("Skipping row {} as its LSN equals the last processed LSN.", rowNum);
//                    return 0;
//                }
//                // Update maxLsn if currentLsn is greater.
//                if (maxLsn.get() == null || Arrays.compare(currentLsn, maxLsn.get()) > 0) {
//                    maxLsn.set(currentLsn);
//                }
//
//                // Build the address map.
//                Map<String, Object> addressMap = new HashMap<>();
//                addressMap.put("AddressCode", "");
//                addressMap.put("AddressId", rs.getInt("AdrID"));
//                addressMap.put("Name", rs.getString("Name") != null ? rs.getString("Name") : "");
//                addressMap.put("Street", rs.getString("Street") != null ? rs.getString("Street") : "");
//                addressMap.put("HouseNumber", rs.getString("HouseNo") != null ? rs.getString("HouseNo") : "");
//                addressMap.put("PostalCode", rs.getString("Postal") != null ? rs.getString("Postal") : "");
//                addressMap.put("City", "");
//                addressMap.put("Comment", rs.getString("Howto") != null ? rs.getString("Howto") : "");
//                addressMap.put("CareOf", "");
//                addressMap.put("Floor", "");
//                addressMap.put("Door", rs.getString("Doorkey") != null ? rs.getString("Doorkey") : "");
//                addressMap.put("Area", rs.getString("Subcity") != null ? rs.getString("Subcity") : "");
//                addressMap.put("State", "");
//                addressMap.put("CountryCode", "");
//                addressMap.put("CountryId", 0);
//
//                String latitudeStr = rs.getString("Latitude");
//                String longitudeStr = rs.getString("Longitude");
//                Double latitude = parseDoubleFromString(latitudeStr);
//                Double longitude = parseDoubleFromString(longitudeStr);
//                addressMap.put("Latitude", latitude);
//                addressMap.put("Longitude", longitude);
//
//                addressMap.put("StopType", 0);
//                addressMap.put("StopTypeCode", "");
//                addressMap.put("DeliveryEft", null);
//                addressMap.put("DeliveryLtt", null);
//                addressMap.put("PickupEft", null);
//                addressMap.put("PickupLtt", null);
//                addressMap.put("DoorKey", rs.getString("Doorkey") != null ? rs.getString("Doorkey") : "");
//                addressMap.put("CustomerCode", "");
//                addressMap.put("SourceRef", "");
//                addressMap.put("Metadata", "");
//                addressMap.put("Operation", rs.getInt("__$operation"));
//
//                String addressJson = objectMapper.writeValueAsString(addressMap);
//                logger.debug("Sending JSON to Kafka: {}", addressJson);
//                ProducerRecord<String, String> record = new ProducerRecord<>(topic, addressJson);
//                producer.send(record, (RecordMetadata metadata, Exception exception) -> {
//                    if (exception != null) {
//                        logger.error("Error sending message to Kafka: {}", exception.getMessage(), exception);
//                    } else {
//                        logger.info("Message sent to topic {} partition {} offset {}",
//                                metadata.topic(), metadata.partition(), metadata.offset());
//                    }
//                });
//            } catch (Exception e) {
//                logger.error("Error processing row from CDC", e);
//            }
//            return 1; // Each processed row returns 1.
//        });
//        int rowCount = processedRows.size();
//        logger.info("Processed {} row(s) from CDC.", rowCount);
//
//        // Flush messages to Kafka.
//        producer.flush();
//        logger.info("Flushed messages to Kafka.");
//
//        // 4. Update lastProcessedLsn.
//        // Only update if we processed rows with a higher LSN.
//        if (maxLsn.get() != null && (lastProcessedLsn == null || Arrays.compare(maxLsn.get(), lastProcessedLsn) > 0)) {
//            lastProcessedLsn = maxLsn.get();
//            logger.info("Updated last processed LSN to: {}", lsnToHex(lastProcessedLsn));
//        } else if (lastProcessedLsn == null) {
//            // If no rows were processed and no previous LSN exists, anchor to the current upper bound.
//            lastProcessedLsn = toLsn;
//            logger.info("No rows processed; setting last processed LSN to current upper bound: {}", lsnToHex(lastProcessedLsn));
//        } else {
//            logger.info("No new LSN to update.");
//        }
//        logger.info("CDC extraction completed.");
//    }
//
//    @Override
//    public void run(String... args) {
//        // Execute extraction on startup; scheduled tasks will run thereafter.
//        extractAddress();
//    }
//
//    @PreDestroy
//    public void closeProducer() {
//        logger.info("Closing Kafka producer...");
//        try {
//            producer.flush();
//        } finally {
//            producer.close();
//        }
//    }
//}