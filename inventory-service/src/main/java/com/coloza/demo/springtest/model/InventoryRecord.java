package com.coloza.demo.springtest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryRecord {
    private Integer productId;
    private Integer quantity;
    private String productName;
    private String productCategory;
}
