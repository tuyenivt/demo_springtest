package com.coloza.demo.springtest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PurchaseRecord {
    private Integer productId;
    private Integer quantityPurchased;
}
