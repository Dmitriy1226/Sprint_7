package ru.yandex.praktikum.scooter.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.yandex.praktikum.scooter.model.Courier;
import ru.yandex.praktikum.scooter.model.CourierCredentials;

import static io.restassured.RestAssured.given;

public class CourierClient {

    private static final String COURIER_PATH = "/api/v1/courier";

    @Step("Создание курьера")
    public ValidatableResponse create(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post(COURIER_PATH)
                .then();
    }

    @Step("Логин курьера")
    public ValidatableResponse login(CourierCredentials credentials) {
        return given()
                .header("Content-type", "application/json")
                .body(credentials)
                .when()
                .post(COURIER_PATH + "/login")
                .then();
    }

    @Step("Удаление курьера по id")
    public ValidatableResponse delete(int courierId) {
        return given()
                .when()
                .delete(COURIER_PATH + "/" + courierId)
                .then();
    }
}