package com.example.avito.api.models;


public class Item {
    private Long id;
    private Integer sellerId;
    private String name;
    private Integer price;

    // Конструкторы, геттеры и сеттеры

    public Item() { // Пустой конструктор для десериализации
    }

    public Item(Integer sellerId, String name, Integer price) {
        this.sellerId = sellerId;
        this.name = name;
        this.price = price;
    }


    // Геттеры и сеттеры для всех полей

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }


}