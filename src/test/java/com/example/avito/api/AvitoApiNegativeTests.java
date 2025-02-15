package com.example.avito.api;

import com.example.avito.api.models.Item;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;

import java.util.UUID;

import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AvitoApiNegativeTests {
    private final Faker faker = new Faker(new Locale("en")); // Faker для генерации данных
    private RequestSpecification requestSpec;
    private Integer sellerId; // ID продавца

    @BeforeAll
    void setup() {
        RestAssured.baseURI = AvitoApiConfig.getBaseUrl();
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addFilter(new RequestLoggingFilter()) // Логирование запросов
                .addFilter(new ResponseLoggingFilter()) // Логирование ответов
                .build();

        sellerId = (int) faker.number().numberBetween(111111, 999999);
    }

    @Test
    @DisplayName("1.2. Создание объявления с отсутствующим sellerID")
    void testCreateItemMissingSellerId() {
        Item newItem = new Item(null, faker.commerce().productName(), faker.number().randomDigitNotZero()); // sellerId = null

        given(requestSpec)
                .body(newItem)
                .when()
                .post(AvitoApiConfig.getItemEndpoint())
                .then()
                .statusCode(400); // Ожидаем код 400 Bad Request
        //.body("message", containsString("sellerId is required")); //  Добавьте проверку сообщения об ошибке, если API его возвращает
    }

    @Test
    @DisplayName("1.3. Создание объявления с отсутствующим name")
    void testCreateItemMissingName() {
        Item newItem = new Item(sellerId, null, faker.number().randomDigitNotZero()); // name = null

        given(requestSpec)
                .body(newItem)
                .when()
                .post(AvitoApiConfig.getItemEndpoint())
                .then()
                .statusCode(400); // Ожидаем код 400 Bad Request
    }

    @Test
    @DisplayName("1.4. Создание объявления с отсутствующим price")
    void testCreateItemMissingPrice() {
        Item newItem = new Item(sellerId, faker.commerce().productName(), null); // price = null

        given(requestSpec)
                .body(newItem)
                .when()
                .post(AvitoApiConfig.getItemEndpoint())
                .then()
                .statusCode(400); // Ожидаем код 400 Bad Request
    }


    @Test
    @DisplayName("1.5 . Создание объявления с очень длинным name")
    void testCreateItemWithVeryLongName() {
        String longName = faker.lorem().characters(1001); // Генерируем строку длиной 1001 символ
        Item newItem = new Item(sellerId, longName, faker.number().randomDigitNotZero());

        given(requestSpec)
                .body(newItem)
                .when()
                .post(AvitoApiConfig.getItemEndpoint())
                .then()
                .statusCode(400); // Ожидаем код 400 Bad Request
    }

    @Test
    @DisplayName("2.1. Получение несуществующего объявления по ID")
    void testGetNonExistingItemById() {
        // Генерируем заведомо несуществующий ID (случайный UUID)
        String nonExistingItemId = UUID.randomUUID().toString();
        System.out.println("Используем несуществующий ID: " + nonExistingItemId);

        given(requestSpec)
                .when()
                .get(AvitoApiConfig.getItemEndpoint() +'/' + nonExistingItemId)
                .then()
                .statusCode(404); // Ожидаем код 404 Not Found
    }

    @Test
    @DisplayName("2.2. Получение объявления с невалидным ID")
    void testGetItemWithInvalidId() {
        String invalidItemId = "invalid-uuid-format"; // Невалидный ID (не UUID формат)

        given(requestSpec)
                .when()
                .get(AvitoApiConfig.getItemEndpoint() +'/' + invalidItemId)
                .then()
                .statusCode(400); // Ожидаем код 400 Bad Request
    }

    @Test
    @DisplayName("3.1 Получение статистики несуществующего объявления")
    void testGetStatisticNonexistentItem() {
        int nonexistentId = 999999999; // Предполагаем, что такого ID нет

        given(requestSpec)
                .when()
                .get(AvitoApiConfig.getItemEndpoint() +'/' + nonexistentId)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("3.2. Получение статистики с невалидным ID")
    void testGetStatisticWithInvalidId() {
        String invalidStatisticId = "invalid-statistic-id"; // Невалидный ID статистики (не UUID формат)

        given(requestSpec)
                .pathParam("id", invalidStatisticId)
                .when()
                .get(AvitoApiConfig.getStatisticEndpoint())
                .then()
                .statusCode(400); // Ожидаем код 400 Bad Request или 500 Internal Server Error (зависит от API)
    }

    @Test
    @DisplayName("3.3. Получение объявлений несуществующего продавца")
    void testGetItemsForNonExistentSeller() {
        int nonExistentSellerId = faker.number().numberBetween(1000000, Integer.MAX_VALUE); // Генерируем заведомо несуществующий sellerId

        given(requestSpec)
                .pathParam("sellerId", nonExistentSellerId)
                .when()
                .get(AvitoApiConfig.getSellerItemEndpoint())
                .then()
                .statusCode(200) // Ожидаем 200 OK (или 404 Not Found, или другой код, в зависимости от API)
                .body("$", hasSize(0)); // Проверяем, что размер массива равен 0    }

    }
}
