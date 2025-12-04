package ru.yandex.praktikum.scooter;

import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.yandex.praktikum.scooter.client.OrderClient;
import ru.yandex.praktikum.scooter.model.Order;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class OrderCreateTest extends BaseTest {

    private final List<String> color;
    private final OrderClient orderClient = new OrderClient();
    private Integer trackId; // нужно сохранить track для последующей отмены

    public OrderCreateTest(List<String> color) {
        this.color = color;
    }

    @Parameterized.Parameters(name = "Цвет самоката: {0}")
    public static Object[] getColors() {
        return new Object[]{
                Collections.singletonList("BLACK"),
                Collections.singletonList("GREY"),
                List.of("BLACK", "GREY"),
                Collections.emptyList()
        };
    }

    @After
    public void tearDown() {
        // Если заказ был создан — отменяем его
        if (trackId != null) {
            orderClient.cancel(trackId)
                    .statusCode(200);
        }
    }

    @Test
    @DisplayName("Создание заказа с разными вариантами цвета")
    public void orderCanBeCreatedWithDifferentColors() {

        Order order = new Order(
                "Вася",
                "Пупкин",
                "Москва, Кремль, 1",
                "4",
                "+79999999999",
                3,
                "2025-12-31",
                "Позвонить за час",
                color
        );

        // сохраняем track, который вернёт ручка
        trackId = orderClient.create(order)
                .statusCode(201)
                .extract()
                .path("track");

        // убеждаемся, что track вернулся
        org.hamcrest.MatcherAssert.assertThat(trackId, notNullValue());
    }
}
