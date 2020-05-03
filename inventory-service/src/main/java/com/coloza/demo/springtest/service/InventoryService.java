package com.coloza.demo.springtest.service;

import com.coloza.demo.springtest.model.InventoryRecord;

import java.util.Optional;

public interface InventoryService {

    Optional<InventoryRecord> getInventoryRecord(Integer productId);

    Optional<InventoryRecord> purchaseProduct(Integer productId, Integer quantity);
}
