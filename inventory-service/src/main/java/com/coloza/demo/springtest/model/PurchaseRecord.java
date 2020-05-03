package com.coloza.demo.springtest.model;

public class PurchaseRecord {
    private Integer productId;
    private Integer quantityPurchased;

    public PurchaseRecord() {
    }

    public PurchaseRecord(Integer productId, Integer quantityPurchased) {
        this.productId = productId;
        this.quantityPurchased = quantityPurchased;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantityPurchased() {
        return quantityPurchased;
    }

    public void setQuantityPurchased(Integer quantityPurchased) {
        this.quantityPurchased = quantityPurchased;
    }
}
