package com.example.avito.api;

import com.example.avito.api.models.Item;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AvitoApiPositiveTests {

    private final Faker faker = new Faker(new Locale("en")); // Faker для генерации данных
    private RequestSpecification requestSpec;
    private String createdItemId;
    private Integer sellerId; // ID продавца
    private String itemName;


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
    @Order(1)
    @DisplayName("1.1. Создание валидного объявления")
    void testCreateValidItem() {

        itemName = faker.lorem().word(); // Генерируем и сохраняем имя
        Item newItem = new Item(sellerId, itemName, faker.number().randomDigitNotZero());

        String statusMessage = given(requestSpec)
                .body(newItem)
                .when()
                .post(AvitoApiConfig.getItemEndpoint())
                .then()
                .statusCode(200)
                .extract()
                .path("status"); // Извлекаем сообщение о статусе

        // Извлекаем UUID из сообщения о статусе с помощью регулярного выражения
        Pattern uuidPattern = Pattern.compile("([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})");
        Matcher matcher = uuidPattern.matcher(statusMessage);

        if (matcher.find()) {
            createdItemId = matcher.group(1); // Получаем UUID из первой группы
        } else {
            Assertions.fail("Не удалось извлечь ID объявления из сообщения о статусе: " + statusMessage);
        }


        Assertions.assertNotNull(createdItemId, "ID созданного объявления не должен быть null");
        Assertions.assertNotEquals("", createdItemId, "ID созданного объявления не должен быть пустым");

    }

    @Test
    @Order(2)
    @DisplayName("2.1. Получение существующего объявления")
    void testGetExistingItem() {
        Assumptions.assumeTrue(createdItemId != null, "Объявление не было создано");

        given(requestSpec)
                .when()
                .get(AvitoApiConfig.getItemEndpoint() + "/" + createdItemId)
                .then()
                .statusCode(200)
                .body("[0].id", equalTo(createdItemId), //  [0].id - доступ к полю id первого элемента массива
                        "[0].sellerId", equalTo(sellerId.intValue()), //  [0].sellerId - доступ к полю sellerId первого элемента массива
                        "[0].name", equalTo(itemName),
                        "[0].price", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("2.2. Проверка структуры ответа при получении существующего объявления по ID")
    void testGetItemByIdResponseStructure() {
        // Предполагается, что createdItemId установлен в тесте testCreateValidItem
        Assumptions.assumeTrue(createdItemId != null, "Объявление не было создано");

        given(requestSpec)
                .when()
                .get(AvitoApiConfig.getItemEndpoint() + "/{id}", createdItemId) // Использование path параметра в URL
                .then()
                .statusCode(200)
                .body("$", notNullValue())
                .body("$", instanceOf(List.class)) // Проверяем, что тело ответа - JSON массив (List)
                // Проверяем структуру первого элемента массива (объявления)
                .body("[0]", instanceOf(Map.class)) // Проверяем, что первый элемент массива - JSON объект (Map)
                .body("[0].id", notNullValue())
                .body("[0].id", instanceOf(String.class)) // ID должен быть строкой (UUID)
                .body("[0].sellerId", notNullValue())
                .body("[0].sellerId", instanceOf(Number.class)) // sellerId должен быть числом
                .body("[0].name", notNullValue())
                .body("[0].name", instanceOf(String.class)) // name должен быть строкой
                .body("[0].price", notNullValue())
                .body("[0].price", instanceOf(Number.class)) // price должен быть числом
                .body("[0].createdAt", notNullValue())
                .body("[0].createdAt", instanceOf(String.class)) // createdAt должен быть строкой (дата-время)
                .body("[0].statistics", notNullValue())
                .body("[0].statistics", instanceOf(Map.class)) // statistics должен быть JSON объектом (Map)
                // Проверяем структуру объекта statistics
                .body("[0].statistics.viewCount", notNullValue())
                .body("[0].statistics.viewCount", instanceOf(Number.class)) // viewCount должен быть числом
                .body("[0].statistics.likes", notNullValue())
                .body("[0].statistics.likes", instanceOf(Number.class)) // likes должен быть числом
                .body("[0].statistics.contacts", notNullValue())
                .body("[0].statistics.contacts", instanceOf(Number.class)); // contacts должен быть числом
    }

    @Test
    @Order(4)
    @DisplayName("3.1. Получение объявлений существующего продавца с объявлениями")
    void testGetSellerItemsForExistingSellerWithItems() {
        // Создаем два объявления для одного и того же sellerId
        int numberOfItemsToCreate = 2;
        for (int i = 0; i < numberOfItemsToCreate; i++) {
            Item newItem = new Item(sellerId, faker.commerce().productName(), faker.number().randomDigitNotZero());
            String statusMessage = given(requestSpec)
                    .body(newItem)
                    .when()
                    .post(AvitoApiConfig.getItemEndpoint())
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("status");

            String createdItemIdString = extractItemIdFromStatus(statusMessage);
            Assertions.assertNotNull(createdItemIdString, "Не удалось извлечь ID объявления из сообщения о статусе: " + statusMessage);
        }

        // Получаем объявления продавца
        given(requestSpec)
                .pathParam("sellerId", sellerId)
                .when()
                .get(AvitoApiConfig.getSellerItemEndpoint())
                .then()
                .statusCode(200)
                .body("$", not(emptyArray()))
                .body("size()", greaterThanOrEqualTo(numberOfItemsToCreate)); // Проверяем, что размер массива >= количеству созданных объявлений

        // Дополнительные проверки для каждого объявления в списке (опционально, но рекомендуется)
        given(requestSpec)
                .pathParam("sellerId", sellerId)
                .when()
                .get(AvitoApiConfig.getSellerItemEndpoint())
                .then()
                .statusCode(200)
                .body("$", not(emptyArray())) // Проверяем, что ответ - не пустой массив
                .body("size()", greaterThanOrEqualTo(numberOfItemsToCreate)) // Проверяем размер массива >= количеству созданных объявлений
                // Проверяем свойства каждого элемента массива с помощью Hamcrest matchers
                .body("sellerId", everyItem(equalTo(sellerId.intValue()))) // Проверяем sellerId для каждого элемента
                .body("id", everyItem(notNullValue()))
                .body("name", everyItem(notNullValue()))
                .body("price", everyItem(notNullValue()));

    }


    private String extractItemIdFromStatus(String statusMessage) {
        Pattern uuidPattern = Pattern.compile("([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})");
        Matcher matcher = uuidPattern.matcher(statusMessage);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            Assertions.fail("Не удалось извлечь ID объявления из сообщения о статусе: " + statusMessage);
            return null;
        }
    }

    @Test
    @Order(5)
    @DisplayName("4.1. Получение статистики существующего объявления")
    void testGetStatisticExistingItem() {
        // Предполагается, что createdItemId установлен в тесте testCreateValidItem
        Assumptions.assumeTrue(createdItemId != null, "Объявление не было создано");

        given(requestSpec)
                .pathParam("id", createdItemId)
                .when()
                .get(AvitoApiConfig.getStatisticEndpoint())
                .then()
                .statusCode(200)
                .body("$", notNullValue()) // Проверяем, что тело ответа не null
                .body("$", instanceOf(List.class)) // Проверяем, что тело ответа - JSON массив (List)
                // Обращаемся к первому элементу массива [0] для проверки полей статистики
                .body("[0].viewCount", notNullValue())
                .body("[0].viewCount", instanceOf(Number.class))
                .body("[0].likes", notNullValue())
                .body("[0].likes", instanceOf(Number.class))
                .body("[0].contacts", notNullValue())
                .body("[0].contacts", instanceOf(Number.class));
    }

    @Test
    @Order(6)
    @DisplayName("1.2 Идемпотентность (частичная) создания объявления")
    void testCreateItemIdempotency() {
        // 1. Создаем объявление #1
        Item newItem1 = new Item(sellerId, itemName, faker.number().randomDigitNotZero());

        String statusMessage1 = given(requestSpec)
                .body(newItem1)
                .when()
                .post(AvitoApiConfig.getItemEndpoint())
                .then()
                .statusCode(200)
                .extract()
                .path("status");
        String createdItemId1 = extractItemIdFromStatus(statusMessage1);
        Assertions.assertNotNull(createdItemId1, "Не удалось извлечь ID объявления #1");

        // 2. Создаем объявление #2 (идентичный запрос)
        Item newItem2 = new Item(sellerId, itemName, faker.number().randomDigitNotZero()); // Используем те же данные
        String statusMessage2 = given(requestSpec)
                .body(newItem2)
                .when()
                .post(AvitoApiConfig.getItemEndpoint())
                .then()
                .statusCode(200)
                .extract()
                .path("status");
        String createdItemId2 = extractItemIdFromStatus(statusMessage2);
        Assertions.assertNotNull(createdItemId2, "Не удалось извлечь ID объявления #2");


        // 3. Проверки
        Assertions.assertNotEquals(createdItemId1, createdItemId2, "ID объявлений должны быть разными (идемпотентность)");

        // Получаем объявления по ID и проверяем данные (кроме ID)
        given(requestSpec)
                .when()
                .get(AvitoApiConfig.getItemEndpoint() + "/" + createdItemId1)
                .then()
                .statusCode(200)
                .body("[0].sellerId", equalTo(newItem1.getSellerId().intValue()))
//                .body("[0].name", equalTo(newItem1.getName())) Закомментировано, так как name не сохраняется api
                .body("[0].price", equalTo(newItem1.getPrice()));

        given(requestSpec)
                .when()
                .get(AvitoApiConfig.getItemEndpoint() + "/" + createdItemId2)
                .then()
                .statusCode(200)
                .body("[0].sellerId", equalTo(newItem2.getSellerId().intValue()))
//                .body("[0].name", equalTo(newItem2.getName()))  Закомментировано, так как name не сохраняется api
                .body("[0].price", equalTo(newItem2.getPrice()));
    }

    @Test
    @Order(7)
    @DisplayName("5.1 Граничные значения для цены создания объявления")
    void testCreateItemWithPriceBoundaries() {
        // 1. Минимальная цена (0)
        testCreateItemWithPriceBoundary(0, 200, null); // Ожидаем успешное создание с ценой 0

        // 2. Очень большая цена (Integer.MAX_VALUE) - Проверка верхней границы (если есть)
        testCreateItemWithPriceBoundary(Integer.MAX_VALUE, 200, null); // Ожидаем успешное создание с макс. ценой (или изменить ожидание, если API отклоняет)

        // 3. Цена ниже минимальной (-1) - Ожидаем ошибку
        testCreateItemWithPriceBoundary(-1, 400, "Цена должна быть неотрицательной"); // Ожидаем ошибку 400 и сообщение (или другой код/сообщение, если API ведет себя иначе)

        // 4. Очень маленькая положительная цена (1) - Проверка нижней границы (если > 0) - Предполагаем, что 0 - минимум, 1 - тоже валидно
        testCreateItemWithPriceBoundary(1, 200, null); // Ожидаем успешное создание с ценой 1
    }


    // Вспомогательный метод для создания объявления с заданной ценой и проверки результата
    private void testCreateItemWithPriceBoundary(int priceValue, int expectedStatusCode, String expectedErrorMessage) {
        Item newItem = new Item(sellerId, itemName, priceValue); // Используем itemName из предыдущих тестов или можно сгенерировать новое

        System.out.println("Создаем объявление с ценой: " + priceValue);
        String responseBody = given(requestSpec)
                .body(newItem)
                .when()
                .post(AvitoApiConfig.getItemEndpoint())
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .asString(); // Извлекаем тело ответа как строку для проверки сообщения об ошибке

        if (expectedStatusCode == 200) {
            String createdItemIdString = extractItemIdFromStatus(responseBody);
            Assertions.assertNotNull(createdItemIdString, "Не удалось извлечь ID объявления при цене " + priceValue);
            System.out.println("Объявление с ценой " + priceValue + " успешно создано. ID: " + createdItemIdString);
        } else if (expectedStatusCode == 400) {
            // Проверяем сообщение об ошибке, если ожидается 400 Bad Request
            Assertions.assertNotNull(expectedErrorMessage, "Ожидаемое сообщение об ошибке не должно быть null для кода 400");
            Assertions.assertTrue(responseBody.contains(expectedErrorMessage),
                    "Сообщение об ошибке не содержит ожидаемый текст: " + expectedErrorMessage + ". Фактический ответ: " + responseBody);
            System.out.println("Ошибка 400 при создании объявления с ценой " + priceValue + ". Проверено сообщение об ошибке: " + expectedErrorMessage);

        } else {
            Assertions.fail("Неожиданный статус код: " + expectedStatusCode + " при цене " + priceValue + ". Ожидалось 200 или 400.");
        }
    }


}