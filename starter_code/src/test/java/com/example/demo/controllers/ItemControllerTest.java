package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;


import com.example.demo.services.SplunkLoggingService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ItemControllerTest {

    private ItemRepository itemRepository = mock(ItemRepository.class);
    private SplunkLoggingService splunkLoggingService = mock(SplunkLoggingService.class);

    private ItemController itemController;



    @Before
    public void setUp() {
        itemController = new ItemController();

        try {
            TestUtils.injectObjects(itemController, "itemRepository", itemRepository);
            TestUtils.injectObjects(itemController, "splunkLoggingService", splunkLoggingService);

            Item item = new Item();
            item.setId(1L);
            item.setDescription("Product 1");
            item.setName("Product 1");
            item.setPrice(BigDecimal.valueOf(20.0));
            itemRepository.save(item);

            Item item2 = new Item();
            item2.setId(2L);
            item2.setDescription("Product 2");
            item2.setName("Product 2");
            item2.setPrice(BigDecimal.valueOf(21.0));
            itemRepository.save(item);

            List<Item> itemsName = new ArrayList<Item>();
            itemsName.add(item2);

            List<Item> items = new ArrayList<Item>();
            items.add(item);
            items.add(item2);

            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(itemRepository.findByName("Product 2")).thenReturn((itemsName));
            when(itemRepository.findAll()).thenReturn(items);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void getItems() {
        ResponseEntity<List<Item>> response = itemController.getItems();

        assertNotNull(response);
        assertEquals(200,response.getStatusCode().value());

        List<Item> items = response.getBody();

        assertNotNull(items);
        assertEquals(2, items.size());

    }

    @Test
    public void getItemById() {
        ResponseEntity<Item> response = itemController.getItemById(1L);
        Item item = response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response);
        assertEquals(item.getName(), "Product 1");
    }

    @Test
    public void getItemsByName() {
        ResponseEntity<List<Item>> responseEntity = itemController.getItemsByName("Product 2");

        List<Item> items = responseEntity.getBody();

        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity);

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Product 2", items.get(0).getName());

    }

    @Test
    public void logError() {
        Exception e = new Exception("generated exception");

        ResponseEntity<String> responseEntity = itemController.logNotCaughtException(e);

        assertEquals(400, responseEntity.getStatusCode().value());
        assertEquals(responseEntity.getBody(), e.getMessage());
    }
}
