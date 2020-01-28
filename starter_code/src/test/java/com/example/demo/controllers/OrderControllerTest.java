package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.services.SplunkLoggingService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderControllerTest {
    private OrderController orderController;

    private UserRepository userRepository = mock(UserRepository.class);

    private OrderRepository orderRepository = mock(OrderRepository.class);

    private SplunkLoggingService splunkLoggingService = mock(SplunkLoggingService.class);

    private Cart cart = new Cart();
    private User user = new User();
    private List<UserOrder> userOrders = new ArrayList<UserOrder>();

    @Before
    public void setup() {
        orderController = new OrderController();
        try {
            TestUtils.injectObjects(orderController, "userRepository", userRepository);
            TestUtils.injectObjects(orderController, "orderRepository", orderRepository);
            TestUtils.injectObjects(orderController, "splunkLoggingService", splunkLoggingService);

            user.setId(1L);
            user.setUsername("Oliver");
            user.setPassword("1234");

            user.setCart(cart);

            Item item = new Item();
            item.setId(1L);
            item.setName("Product 1");
            item.setDescription("Product 1");
            item.setPrice(BigDecimal.valueOf(20.0));

            Item item2 = new Item();
            item2.setId(2L);
            item2.setName("Product 2");
            item2.setDescription("Product 2");
            item2.setPrice(BigDecimal.valueOf(30.0));

            cart.addItem(item);
            cart.addItem(item2);

            when(userRepository.findByUsername("Oliver")).thenReturn(user);


        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void submitOrder() {
        ResponseEntity<UserOrder> response = orderController.submit("Oliver");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        UserOrder userOrder = response.getBody();
        assertNotNull(userOrder);
        assertEquals(2, userOrder.getItems().size());

    }


    @Test
    public void getOrdersForUser() {

        ResponseEntity<UserOrder> response = orderController.submit("Oliver");
        userOrders.add(response.getBody());

        when(orderRepository.findByUser(user)).thenReturn(userOrders);
        user.setCart(null);

        Cart cart = new Cart();

        Item item = new Item();
        item.setId(1L);
        item.setName("Product 1");
        item.setDescription("Product 1");
        item.setPrice(BigDecimal.valueOf(20.0));

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Product 2");
        item2.setDescription("Product 2");
        item2.setPrice(BigDecimal.valueOf(30.0));


        cart.addItem(item);
        cart.addItem(item2);
        user.setCart(cart);

        ResponseEntity<UserOrder> response2 = orderController.submit("Oliver");
        userOrders.add(response2.getBody());

        ResponseEntity<List<UserOrder>> listOrders = orderController.getOrdersForUser("Oliver");

        assertEquals(200, listOrders.getStatusCode().value());
        assertNotNull(listOrders);
        assertEquals(2, listOrders.getBody().size());

    }


    @Test
    public void logError() {
        Exception e = new Exception("generated exception");

        ResponseEntity<String> responseEntity = orderController.logNotCaughtException(e);

        Assert.assertEquals(400, responseEntity.getStatusCode().value());
        Assert.assertEquals(responseEntity.getBody(), e.getMessage());
    }

}
