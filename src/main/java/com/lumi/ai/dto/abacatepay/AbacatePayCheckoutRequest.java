package com.lumi.ai.dto.abacatepay;

import java.util.List;

public class AbacatePayCheckoutRequest {

    private String frequency;
    private List<String> methods;
    private List<AbacatePayProduct> products;
    private String returnUrl;
    private String completionUrl;
    private AbacatePayCustomer customer;

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public List<String> getMethods() { return methods; }
    public void setMethods(List<String> methods) { this.methods = methods; }

    public List<AbacatePayProduct> getProducts() { return products; }
    public void setProducts(List<AbacatePayProduct> products) { this.products = products; }

    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }

    public String getCompletionUrl() { return completionUrl; }
    public void setCompletionUrl(String completionUrl) { this.completionUrl = completionUrl; }

    public AbacatePayCustomer getCustomer() { return customer; }
    public void setCustomer(AbacatePayCustomer customer) { this.customer = customer; }
}