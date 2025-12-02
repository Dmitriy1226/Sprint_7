package ru.yandex.praktikum.scooter;

import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Test;
import ru.yandex.praktikum.scooter.client.CourierClient;
import ru.yandex.praktikum.scooter.model.Courier;
import ru.yandex.praktikum.scooter.model.CourierCredentials;

import static org.hamcrest.Matchers.equalTo;

public class CourierCreateTest extends BaseTest {

    private final CourierClient courierClient = new CourierClient();
    private Integer courierId; // сюда будем сохранять id для удаления

    // Вспомогательный метод: создаёт курьера с уникальным логином
    private Courier getRandomCourier() {
        String login = "courier_" + System.currentTimeMillis();
        String password = "1234";
        String firstName = "Vasya";
        return new Courier(login, password, firstName);
    }

    @After
    public void tearDown() {
        // Если в тесте мы получили id — удаляем курьера после теста
        if (courierId != null) {
            courierClient.delete(courierId)
                    .statusCode(200)
                    .body("ok", equalTo(true));
        }
    }

    @Test
    public void courierCanBeCreated() {
        Courier courier = getRandomCourier();

        // создаём курьера
        ValidatableResponse createResponse = courierClient.create(courier);
        createResponse
                .statusCode(201)
                .body("ok", equalTo(true));

        // авторизуемся и достаём id для последующего удаления
        ValidatableResponse loginResponse = courierClient.login(
                new CourierCredentials(courier.getLogin(), courier.getPassword())
        );

        courierId = loginResponse
                .statusCode(200)
                .extract()
                .path("id");
    }

    @Test
    public void cannotCreateTwoIdenticalCouriers() {
        Courier courier = getRandomCourier();

        // первый запрос — успех
        courierClient.create(courier)
                .statusCode(201)
                .body("ok", equalTo(true));

        // логин, чтобы получить id для удаления
        courierId = courierClient.login(
                        new CourierCredentials(courier.getLogin(), courier.getPassword())
                )
                .statusCode(200)
                .extract()
                .path("id");

        // второй запрос с тем же логином — ошибка
        courierClient.create(courier)
                .statusCode(409)
                .body("message",
                        equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    public void cannotCreateCourierWithoutLogin() {
        Courier courier = new Courier(
                null,
                "1234",
                "Vasya"
        );

        courierClient.create(courier)
                .statusCode(400)
                .body("message",
                        equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    public void cannotCreateCourierWithoutPassword() {
        Courier courier = new Courier(
                "courier_" + System.currentTimeMillis(),
                null,
                "Vasya"
        );

        courierClient.create(courier)
                .statusCode(400)
                .body("message",
                        equalTo("Недостаточно данных для создания учетной записи"));
    }
}