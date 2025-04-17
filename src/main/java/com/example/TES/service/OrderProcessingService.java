package com.example.TES.service;

import com.example.TES.dto.OrderEvent;
import com.example.TES.repository.AdrRepository;
import com.example.TES.repository.AliasRepository;
import com.example.TES.repository.SendingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final AdrRepository adrRepository;
    private final AliasRepository aliasRepository;
    private final SendingRepository sendingRepository;

    public void processOrder(OrderEvent event) {
        log.info("\uD83D\uDC41\u200D\uD83D\uDDE8 Processing order with barcode: {}", event.getBarcode());

        Integer pickupAdrId = resolveOrCreateAddress(
                event.getCustId(),
                event.getPickupAlias(),
                event.getPickupName(),
                event.getPickupStreet(),
                event.getPickupHouseNo(),
                event.getPickupPostalCode(),
                event.getPickupCity()
        );

        Integer deliveryAdrId = resolveOrCreateAddress(
                event.getCustId(),
                event.getDeliveryAlias(),
                event.getDeliveryName(),
                event.getDeliveryStreet(),
                event.getDeliveryHouseNo(),
                event.getDeliveryPostalCode(),
                event.getDeliveryCity()
        );

        String note = "*S " + Optional.ofNullable(event.getDriverInfo()).orElse("") +
                "\n*Z " + Optional.ofNullable(event.getInvoiceInfo()).orElse("");

        sendingRepository.insertSending(
                event,
                pickupAdrId,
                deliveryAdrId,
                note,
                mapPickupType(event.getCustId(), event.getPickupAlias()),
                mapDeliveryType(event.getCustId(), event.getDeliveryAlias())
        );
    }

    private Integer resolveOrCreateAddress(String custId, String alias, String name,
                                           String street, String houseNo, String postalCode, String city) {
        Integer adrId = aliasRepository.findAdrIdByAlias(custId, alias);
        if (adrId != null) return adrId;

        Integer streetId = adrRepository.findStreetId(street, postalCode);
        if (streetId == null) {
            streetId = adrRepository.insertStreet(street, postalCode);
        }

        adrId = adrRepository.insertAdr(streetId, city, houseNo);
        aliasRepository.insertAlias(custId, alias, adrId, name);

        return adrId;
    }

    private String mapPickupType(String custId, String alias) {
        return "F"; // tymczasowa logika – można rozbudować per klient
    }

    private String mapDeliveryType(String custId, String alias) {
        return "D"; // tymczasowa logika – można rozbudować per klient
    }
}
