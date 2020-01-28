package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
import com.example.demo.services.SplunkLoggingService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;


import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

public class CartControllerTest {

    private UserRepository userRepository = mock(UserRepository.class);

    private CartRepository cartRepository = mock(CartRepository.class);

    private ItemRepository itemRepository = mock(ItemRepository.class);

    private SplunkLoggingService splunkLoggingService = mock(SplunkLoggingService.class);

    private CartController cartController;

    @Before
    public void setup() {
        cartController = new CartController();
        try {
            TestUtils.injectObjects(cartController, "userRepository", userRepository);
            TestUtils.injectObjects(cartController, "cartRepository", cartRepository);
            TestUtils.injectObjects(cartController, "itemRepository",itemRepository);
            TestUtils.injectObjects(cartController, "splunkLoggingService",splunkLoggingService);

            User user = new User();
            user.setId(1L);
            user.setUsername("Oliver");
            user.setPassword("1234");
            Cart cart = new Cart();
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
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));


        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void addToCart() {
        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setUsername("Oliver");
        cartRequest.setItemId(1L);
        cartRequest.setQuantity(5);
        ResponseEntity<Cart> response = cartController.addTocart(cartRequest);

        Cart cart = response.getBody();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(cart);
        assertEquals(7, cart.getItems().size());

    }


    @Test
    public void removeFromCart() {
        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setUsername("Oliver");
        cartRequest.setItemId(1L);
        cartRequest.setQuantity(1);

        ResponseEntity<Cart> response = cartController.removeFromcart(cartRequest);

        Cart cart = response.getBody();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());
    }


    @Test
    public void logError() {
        Exception e = new Exception("generated exception");

        ResponseEntity<String> responseEntity = cartController.logNotCaughtException(e);

        assertEquals(400, responseEntity.getStatusCode().value());
        assertEquals(responseEntity.getBody(), e.getMessage());
    }


}
