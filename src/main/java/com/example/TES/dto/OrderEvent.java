package com.example.TES.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderEvent {
    @JsonProperty("RequestID")
    private String requestId;

    @JsonProperty("CustID")
    private String custId;

    @JsonProperty("Barcode")
    private String barcode;

    @JsonProperty("PickUpDate")
    private String pickupDate;

    @JsonProperty("PickUpAlias")
    private String pickupAlias;

    @JsonProperty("PickUpName")
    private String pickupName;

    @JsonProperty("PickUpStreet")
    private String pickupStreet;

    @JsonProperty("PickUpHouseNo")
    private String pickupHouseNo;

    @JsonProperty("PickUpPostalCode")
    private String pickupPostalCode;

    @JsonProperty("PickUpCity")
    private String pickupCity;

    @JsonProperty("DeliveryAlias")
    private String deliveryAlias;

    @JsonProperty("DeliveryName")
    private String deliveryName;

    @JsonProperty("DeliveryStreet")
    private String deliveryStreet;

    @JsonProperty("DeliveryHouseNo")
    private String deliveryHouseNo;

    @JsonProperty("DeliveryPostalCode")
    private String deliveryPostalCode;

    @JsonProperty("DeliveryCity")
    private String deliveryCity;

    @JsonProperty("Weight")
    private double weight;

    @JsonProperty("Volume")
    private double volume;

    @JsonProperty("Height")
    private double height;

    @JsonProperty("Length")
    private double length;

    @JsonProperty("Width")
    private double width;

    @JsonProperty("DriverInfo")
    private String driverInfo;

    @JsonProperty("InvoiceInfo")
    private String invoiceInfo;
}
