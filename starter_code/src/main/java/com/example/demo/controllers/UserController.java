package com.example.demo.controllers;

import java.util.Optional;

import com.example.demo.bean.SplunkLog;
import com.example.demo.services.SplunkLoggingService;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

	final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private SplunkLoggingService splunkLoggingService;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		return ResponseEntity.of(userRepository.findById(id));
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		User user = userRepository.findByUsername(username);



		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}

	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
		User user = new User();
		user.setUsername(createUserRequest.getUsername());
		user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));
		Cart cart = new Cart();
		cartRepository.save(cart);
		user.setCart(cart);

		if((createUserRequest.getPassword().length() < 7) || (!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword()))) {
			SplunkLog splunkLog = new SplunkLog();

			splunkLog.addField("user_creation_failed", createUserRequest.getUsername());
			splunkLog.addField("user_creation_failed", createUserRequest.getPassword());
			splunkLog.addField("user_creation_failed", createUserRequest.getConfirmPassword());

			splunkLoggingService.logToSplunk(splunkLog);

			return ResponseEntity.badRequest().build();
		}

		userRepository.save(user);

		SplunkLog splunkLog = new SplunkLog();

		splunkLog.addField("user_created",user.getUsername());

		splunkLoggingService.logToSplunk(splunkLog);

		return ResponseEntity.ok(user);
	}

	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Data integrity violation")
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> logNotCaughtException(Exception ex) {
		logger.error("The following error has been raised: {}", ex.getMessage());
		SplunkLog splunkLog = new SplunkLog();
		splunkLog.addField("error",ex.getMessage());
		splunkLoggingService.logToSplunk(splunkLog);
		return new ResponseEntity<String>(ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}


}
