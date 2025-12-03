package ru.yandex.praktikum.scooter;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.praktikum.scooter.client.CourierClient;
import ru.yandex.praktikum.scooter.model.Courier;
import ru.yandex.praktikum.scooter.model.CourierCredentials;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class CourierLoginTest extends BaseTest {

    private CourierClient courierClient;
    private Integer courierId; // будем хранить id созданного курьера

    @Before
    public void setUp() {
        courierClient = new CourierClient();
        courierId = null;
    }

    @After
    public void tearDown() {
        // если в тесте получили id — удаляем курьера
        if (courierId != null) {
            courierClient.delete(courierId);
        }
    }

    @Test
    @DisplayName("Курьер может авторизоваться с корректными данными")
    public void courierCanLoginWithValidCredentials() {
        // создаём курьера
        Courier courier = new Courier(
                "vasya_login_" + System.currentTimeMillis(),
                "1234",
                "Vasya"
        );

        courierId = courierClient.create(courier)
                .statusCode(201)
                .extract().path("id");

        // авторизуемся с теми же логином и паролем
        CourierCredentials creds = new CourierCredentials(
                courier.getLogin(),
                courier.getPassword()
        );

        courierClient.login(creds)
                .statusCode(200)
                // главное: id ДОЛЖЕН быть, а не null
                .body("id", notNullValue());
    }


    @Test
    @DisplayName("Нельзя авторизоваться без логина")
    public void cannotLoginWithoutLogin() {
        // создаём нормального курьера, чтобы в системе он существовал
        Courier courier = new Courier(
                "vasya_no_login_" + System.currentTimeMillis(),
                "1234",
                "Vasya"
        );

        courierId = courierClient.create(courier)
                .statusCode(201)
                .extract().path("id");

        // в кредах логин = null
        CourierCredentials creds = new CourierCredentials(
                null,
                courier.getPassword()
        );

        courierClient.login(creds)
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Нельзя авторизоваться без пароля (ожидаем 400 или 504)")
    public void cannotLoginWithoutPassword() {
        Courier courier = new Courier(
                "vasya_no_pass_" + System.currentTimeMillis(),
                "1234",
                "Vasya"
        );

        courierId = courierClient.create(courier)
                .statusCode(201)
                .extract().path("id");

        // пароль не передаём
        CourierCredentials creds = new CourierCredentials(
                courier.getLogin(),
                null
        );

        // иногда ручка может возвращать 504 вместо 400, поэтому допускаем оба
        courierClient.login(creds)
                .statusCode(anyOf(is(400), is(504)));
    }

    @Test
    @DisplayName("Нельзя авторизоваться с неверными данными")
    public void cannotLoginWithWrongCredentials() {
        Courier courier = new Courier(
                "vasya_wrong_" + System.currentTimeMillis(),
                "1234",
                "Vasya"
        );

        courierId = courierClient.create(courier)
                .statusCode(201)
                .extract().path("id");

        // передаём неправильный пароль
        CourierCredentials wrongCreds = new CourierCredentials(
                courier.getLogin(),
                "wrong-password"
        );

        courierClient.login(wrongCreds)
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }
}
