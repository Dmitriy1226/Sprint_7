package ru.yandex.praktikum.scooter;

import io.qameta.allure.junit4.DisplayName;
import org.junit.Test;
import ru.yandex.praktikum.scooter.client.OrderClient;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

public class OrdersListTest extends BaseTest {

    private final OrderClient orderClient = new OrderClient();

    @Test
    @DisplayName("В ответе на запрос списка заказов приходит непустой список")
    public void ordersListShouldBeReturned() {
        orderClient.getOrders()
                .statusCode(200)
                .body("orders", notNullValue())
                .body("orders.size()", greaterThan(0));
    }
}
