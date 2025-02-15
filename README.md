# Avito API Tests

Этот проект содержит автоматизированные тесты для API Avito, написанные на Java с использованием RestAssured и JUnit 5.
- Тесты разделены на два класса AvitoApiPositiveTests с позитивными проверками и AvitoApiNegativeTests с негативными соответственно, так же есть конфигурационный класс AvitoApiConfig в котором находится URL и все необходимые ручки.
- Некоторые тесты падают, так как были обнаружены (на мой взгляд) проблемы с API, подробнее в BUGS.md
- Тест-кейсы находятся в отдельном TESTCASES.md
## Функциональность

Проект предоставляет тесты для следующих endpoints API:

- **Создание объявления (POST /api/1/item):**  Создание новых объявлений.
- **Получение объявления по ID (GET /api/1/item/{id}):** Получение информации о существующих объявлениях.
- **Получение объявлений продавца (GET /api/1/{sellerID}/item):**  Получение списка объявлений для определенного продавца.
- **Получение статистики по объявлению (GET /api/1/statistic/{id}):** Получение статистики просмотров, лайков и контактов для объявления.

## Структура проекта

- `src/main/java/com/example/avito/api/models`: Содержит POJO модели данных (Item).
- `src/main/java/com/example/avito/api/AvitoApiConfig.java`: Содержит конфигурацию API (базовый URL, endpoints).
- `src/test/java/com/example/avito/api`: Содержит тестовые классы (AvitoApiPositiveTests, AvitoApiNegativeTests).
## Инструкции по запуску
   При использовании **Intellij IDEA 2024.3.1.1** достаточно найти вкладку с Maven на правой панели управления и выбрать команды 'clean', 'install' и 'test' через gui  
   Можно отдельно запустить позитивные и негативные проверки, запустив класс AvitoApiPositiveTests или AvitoApiNegativeTests

1.  **Клонируйте репозиторий:**

    ```bash
    git clone https://github.com/dieraw/AvitoAPI.git
    ```

2.  **Перейдите в директорию проекта:**

    ```bash
    cd avito-api-tests
    ```

3.  **Соберите проект:**

    ```bash
    mvnw clean install  (или mvn clean install, если у вас установлен Maven)
    ```

4.  **Запустите тесты:**

    ```bash
    mvnw test (или mvn test)
    ```


## Системные требования

*   Java 11 или выше
*   Git
*   Maven
