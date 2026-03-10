package com.lumi.ai.dto.abacatepay;

public class AbacatePayCustomer {

    private String name;
    private String email;
    private String taxId;
    private String cellphone;

    public AbacatePayCustomer() {}

    public AbacatePayCustomer(String name, String email, String taxId, String cellphone) {
        this.name = name;
        this.email = email;
        this.taxId = taxId;
        this.cellphone = cellphone;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getTaxId() { return taxId; }
    public String getCellphone() { return cellphone; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public void setCellphone(String cellphone) { this.cellphone = cellphone; }
}