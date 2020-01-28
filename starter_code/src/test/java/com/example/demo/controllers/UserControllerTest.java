package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.services.SplunkLoggingService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    private UserController userController;

    private UserRepository userRepo = mock(UserRepository.class);

    private CartRepository cartRepo = mock(CartRepository.class);

    private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);

    private SplunkLoggingService splunkLoggingService = mock(SplunkLoggingService.class);

    @Before
    public void setUp() {
        userController = new UserController();

        try {
            TestUtils.injectObjects(userController, "userRepository", userRepo);
            TestUtils.injectObjects(userController, "cartRepository", cartRepo);
            TestUtils.injectObjects(userController, "bCryptPasswordEncoder", encoder);
            TestUtils.injectObjects(userController, "splunkLoggingService", splunkLoggingService);


            User userSaved = new User();
            userSaved.setUsername("Oliver");
            userSaved.setPassword("password");
            when(userRepo.findByUsername("Oliver")).thenReturn(userSaved);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void create_user_happy_path() throws Exception {

        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest r = new CreateUserRequest();
        r.setUsername("test");
        r.setPassword("testPassword");
        r.setConfirmPassword("testPassword");

        ResponseEntity<User> response = userController.createUser(r);

        assertNotNull(response);
        assertEquals(200,response.getStatusCode().value());

        User user = response.getBody();

        assertNotNull(user);
        assertEquals(0, user.getId());
        assertEquals("test", user.getUsername());
        assertEquals("thisIsHashed", user.getPassword());


    }

    @Test
    public void findUserByName() {
        User user = new User();
        user.setUsername("Oliver");
        user.setPassword("password");
        userRepo.save(user);

        ResponseEntity<User> response = userController.findByUserName("Oliver");

        User userSaved = response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(userSaved);
    }


    @Test
    public void findUserById() {
        User user = new User();
        user.setId(3L);
        user.setUsername("Oliver");
        user.setPassword("password");
        userRepo.save(user);
        when(userRepo.findById(3L)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.findById(3L);

        User userSaved = response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(userSaved);
    }


    @Test
    public void logNotCaughtException() {
        Exception e = new Exception("generated exception");

        ResponseEntity<String> responseEntity = userController.logNotCaughtException(e);

        assertEquals(400, responseEntity.getStatusCode().value());
        assertEquals(responseEntity.getBody(), e.getMessage());
    }

}

