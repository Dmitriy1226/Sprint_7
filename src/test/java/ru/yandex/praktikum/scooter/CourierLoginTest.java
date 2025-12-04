package ru.yandex.praktikum.scooter;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.praktikum.scooter.client.CourierClient;
import ru.yandex.praktikum.scooter.model.Courier;
import ru.yandex.praktikum.scooter.model.CourierCredentials;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CourierLoginTest extends BaseTest {

    private CourierClient courierClient;
    private Courier courier;
    private Integer courierId; // будем хранить id созданного курьера

    @Before
    public void setUp() {
        courierClient = new CourierClient();
        // один курьер, которого будем использовать во всех тестах логина
        courier = new Courier(
                "vasya_login_" + System.currentTimeMillis(),
                "1234",
                "Vasya"
        );

        courierId = courierClient.create(courier)
                .statusCode(201)
                .extract()
                .path("id");
    }

    @After
    public void tearDown() {
        // если в тесте получили id — удаляем курьера
        if (courierId != null) {
            courierClient.delete(courierId)
                    .statusCode(200);
        }
    }

    @Test
    @DisplayName("Курьер может авторизоваться с корректными данными")
    @Description("Проверяем, что курьер успешно авторизуется с верным логином и паролем, и в ответе возвращается id")
    public void courierCanLoginWithValidCredentials() {
        CourierCredentials creds = new CourierCredentials(
                courier.getLogin(),
                courier.getPassword()
        );

        courierClient.login(creds)
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("Нельзя авторизоваться без логина")
    @Description("Проверяем, что при попытке авторизоваться без логина возвращается ошибка 400 и корректное сообщение")
    public void cannotLoginWithoutLogin() {
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
    @DisplayName("Нельзя авторизоваться без пароля")
    @Description("Проверяем, что при попытке авторизоваться без пароля возвращается ошибка 400 и корректное сообщение")
    public void cannotLoginWithoutPassword() {
        // пароль не передаём
        CourierCredentials creds = new CourierCredentials(
                courier.getLogin(),
                null
        );

        courierClient.login(creds)
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Нельзя авторизоваться с неверным логином")
    @Description("Проверяем, что авторизация с неверным логином невозможна и возвращается ошибка 404")
    public void cannotLoginWithWrongLogin() {
        CourierCredentials wrongLoginCreds = new CourierCredentials(
                "wrong_" + courier.getLogin(),
                courier.getPassword()
        );

        courierClient.login(wrongLoginCreds)
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Нельзя авторизоваться с неверным паролем")
    @Description("Проверяем, что авторизация с неверным паролем невозможна и возвращается ошибка 404")
    public void cannotLoginWithWrongPassword() {
        CourierCredentials wrongPasswordCreds = new CourierCredentials(
                courier.getLogin(),
                "wrong-password"
        );

        courierClient.login(wrongPasswordCreds)
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }
}
