# BUG REPORT

## Описание проблемы: Некорректная обработка данных и валидация на API

В ходе тестирования API Avito были обнаружены следующие ошибки, связанные с обработкой данных и валидацией запросов:

**1. Не сохраняется имя товара:**

* **Тест:** `2.1. Получение существующего объявления`
* **Ожидаемое поведение:**  Имя товара, указанное при создании объявления, должно сохраняться и возвращаться при получении информации об объявлении.
* **Фактическое поведение:**  API возвращает значение "dsdsd" для поля `name` независимо от имени, указанного в запросе на создание объявления.
* **Пример:**
    ```json
    {
        "createdAt": "2025-02-15 17:26:52.640575 +0300 +0300",
        "id": "b50a7d79-25bd-462b-9630-a2ad0b1b1e91",
        "name": "dsdsd", // Ошибка: неверное имя
        "price": 5,
        "sellerId": 666933,
        "statistics": { ... }
    }
    ```

**2. Отсутствует валидация цены:**

* **Тест:** `5.1 Граничные значения для цены создания объявления`
* **Ожидаемое поведение:** API должен отклонять запросы на создание объявления с отрицательной ценой (price = -1) с кодом ошибки 400.
* **Фактическое поведение:** API принимает запросы с отрицательной ценой и успешно создает объявления.
* **Пример:** Запрос с `price = -1` возвращает код 200 OK.

**3. Отсутствует валидация на длину имени:**

* **Тест:** `1.5. Создание объявления с очень длинным name`
* **Ожидаемое поведение:** API должен отклонять запросы на создание объявления с очень длинным именем (более 1000 символов) с кодом ошибки 400.
* **Фактическое поведение:** API принимает запросы с очень длинным именем и успешно создает объявления.

**4. Отсутствует валидация обязательных полей при создании объявления:**

* **Тесты:**
    * `1.2. Создание объявления с отсутствующим sellerID`
    * `1.3. Создание объявления с отсутствующим name`
    * `1.4. Создание объявления с отсутствующим price`
* **Ожидаемое поведение:** API должен отклонять запросы на создание объявления с отсутствующими обязательными полями (`sellerID`, `name`, `price`) с кодом ошибки 400.
* **Фактическое поведение:** API принимает запросы с отсутствующими обязательными полями и успешно создает объявления.  Это может привести к некорректным данным в системе.


## Шаги для воспроизведения:

Для воспроизведения ошибок достаточно выполнить соответствующие тесты, описанные выше.

## Ожидаемый результат:

API должен корректно обрабатывать данные и валидировать запросы, отклоняя некорректные данные с соответствующими кодами ошибок.

## Фактический результат:

API не выполняет необходимую валидацию и сохраняет некорректные данные.

## Дополнительная информация:

Проблемы с валидацией могут привести к непредсказуемому поведению системы и нарушению целостности данных.  Необходимо исправить ошибки валидации на стороне API.
