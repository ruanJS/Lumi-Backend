package com.lumi.ai.dto.abacatepay;

public class AbacatePayCheckoutResponse {

    private Boolean success;
    private Data data;

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }

    public static class Data {

        private String url;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}