package ru.yandex.praktikum.scooter.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.yandex.praktikum.scooter.model.Order;

import static io.restassured.RestAssured.given;

public class OrderClient {

    private static final String ORDERS_PATH = "/api/v1/orders";

    @Step("Создать заказ")
    public ValidatableResponse create(Order order) {
        return given()
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(ORDERS_PATH)
                .then();
    }

    @Step("Получить список заказов")
    public ValidatableResponse getOrders() {
        return given()
                .when()
                .get(ORDERS_PATH)
                .then();
    }

    @Step("Отменить заказ")
    public ValidatableResponse cancel(int trackId) {
        return given()
                .queryParam("track", trackId)
                .when()
                .put(ORDERS_PATH + "/cancel")
                .then();
    }
}
