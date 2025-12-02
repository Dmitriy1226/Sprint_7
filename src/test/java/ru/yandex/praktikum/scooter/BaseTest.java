package ru.yandex.praktikum.scooter;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.junit.Before;

public class BaseTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";

        RestAssured.filters(
                new AllureRestAssured()
        );
    }
}
