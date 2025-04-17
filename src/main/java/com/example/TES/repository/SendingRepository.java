package com.example.TES.repository;

import com.example.TES.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class SendingRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertSending(OrderEvent event, Integer pickupAdrId, Integer deliveryAdrId, String note, String pickupType, String deliveryType) {

        String sql = "INSERT INTO dbo.Sending (" +
                "SendingID, Barcode, PickupDate, PickupAlias, PickupAttentionName, PickupAdrID, " +
                "DeliveryAlias, DeliveryAttentionName, DeliveryAdrID, DeliveryDate, DeliveryType, " +
                "Weight, Volumen, Height, Length, Width, Note, DeliveryCustID, PickupType, SessionDcreate" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                event.getBarcode(),
                event.getBarcode(),
                LocalDate.parse(event.getPickupDate()),
                event.getPickupAlias(),
                event.getPickupName(),
                pickupAdrId,
                event.getDeliveryAlias(),
                event.getDeliveryName(),
                deliveryAdrId,
                LocalDate.now(),
                deliveryType,
                event.getWeight(),
                event.getVolume(),
                event.getHeight(),
                event.getLength(),
                event.getWidth(),
                note,
                event.getCustId(),
                pickupType,
                Timestamp.valueOf(LocalDateTime.now())
        );
    }
}