package ru.yandex.praktikum.scooter;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import net.datafaker.Faker;
import org.junit.After;
import org.junit.Test;
import ru.yandex.praktikum.scooter.client.CourierClient;
import ru.yandex.praktikum.scooter.model.Courier;
import ru.yandex.praktikum.scooter.model.CourierCredentials;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;

public class CourierCreateTest extends BaseTest {

    private final CourierClient courierClient = new CourierClient();
    private final Faker faker = new Faker();

    // сюда будем сохранять логин/пароль курьера, которого нужно удалить после теста
    private CourierCredentials courierCredentialsForCleanup;

    // Вспомогательный метод: создаёт курьера с рандомными данными
    private Courier getRandomCourier() {
        return new Courier(
                faker.name().username(),
                faker.internet().password(),
                faker.name().firstName()
        );
    }

    @After
    public void tearDown() {
        // Если в тесте мы создали курьера и сохранили его креды — удаляем его
        if (courierCredentialsForCleanup != null) {
            // логинимся и достаём id
            ValidatableResponse loginResponse = courierClient.login(courierCredentialsForCleanup);

            Integer courierId = loginResponse
                    .statusCode(SC_OK)
                    .extract()
                    .path("id");

            // удаляем курьера
            courierClient.delete(courierId)
                    .statusCode(SC_OK)
                    .body("ok", equalTo(true));

            // чтобы не удалить повторно в следующем тесте
            courierCredentialsForCleanup = null;
        }
    }

    @Test
    @DisplayName("Создание курьера: успешный сценарий")
    @Description("Проверяем, что курьер создаётся успешно, и после создания можно авторизоваться")
    public void courierCanBeCreated() {
        Courier courier = getRandomCourier();

        // создаём курьера
        courierClient.create(courier)
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));

        // сохраняем креды для последующего удаления в @After
        courierCredentialsForCleanup = new CourierCredentials(
                courier.getLogin(),
                courier.getPassword()
        );
    }

    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    @Description("Проверяем, что повторное создание курьера с тем же логином возвращает 409")
    public void cannotCreateTwoIdenticalCouriers() {
        Courier courier = getRandomCourier();

        // первый запрос — успех
        courierClient.create(courier)
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));

        // сохраняем креды для удаления курьера в @After
        courierCredentialsForCleanup = new CourierCredentials(
                courier.getLogin(),
                courier.getPassword()
        );

        // второй запрос с тем же логином — ошибка
        courierClient.create(courier)
                .statusCode(SC_CONFLICT)
                .body("message",
                        equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @DisplayName("Нельзя создать курьера без логина")
    @Description("Создание курьера без логина должно возвращать ошибку 400")
    public void cannotCreateCourierWithoutLogin() {
        Courier courier = new Courier(
                null,
                faker.internet().password(),
                faker.name().firstName()
        );

        courierClient.create(courier)
                .statusCode(SC_BAD_REQUEST)
                .body("message",
                        equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Нельзя создать курьера без пароля")
    @Description("Создание курьера без пароля должно возвращать ошибку 400")
    public void cannotCreateCourierWithoutPassword() {
        Courier courier = new Courier(
                faker.name().username(),
                null,
                faker.name().firstName()
        );

        courierClient.create(courier)
                .statusCode(SC_BAD_REQUEST)
                .body("message",
                        equalTo("Недостаточно данных для создания учетной записи"));
    }
}
