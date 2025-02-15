package com.example.avito.api;

public class AvitoApiConfig {
    public static final String BASE_URL = "https://qa-internship.avito.com";
    public static final String ITEM_ENDPOINT = "/api/1/item";
    public static final String STATISTIC_ENDPOINT = "/api/1/statistic/{id}";
    public static final String SELLER_ITEM_ENDPOINT = "/api/1/{sellerId}/item";


    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getItemEndpoint() {
        return ITEM_ENDPOINT;
    }

    public static String getStatisticEndpoint() {
        return STATISTIC_ENDPOINT;
    }

    public static String getSellerItemEndpoint() {
        return SELLER_ITEM_ENDPOINT;
    }
}