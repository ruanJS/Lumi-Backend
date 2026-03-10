package com.lumi.ai.dto.abacatepay;

public class AbacatePayProduct {

    private String name;
    private String description;
    private Integer price;
    private Integer quantity;
    private String externalId;

    public AbacatePayProduct() {}

    public AbacatePayProduct(String name, String description, Integer price, Integer quantity, String externalId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.externalId = externalId;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getPrice() { return price; }
    public Integer getQuantity() { return quantity; }
    public String getExternalId() { return externalId; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Integer price) { this.price = price; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
}