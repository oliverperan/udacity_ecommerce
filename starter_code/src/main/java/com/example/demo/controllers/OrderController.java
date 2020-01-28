package com.example.demo.controllers;

import java.util.List;

import com.example.demo.bean.SplunkLog;
import com.example.demo.services.SplunkLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;

@RestController
@RequestMapping("/api/order")
public class OrderController {

	final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	SplunkLoggingService splunkLoggingService;
	
	
	@PostMapping("/submit/{username}")
	public ResponseEntity<UserOrder> submit(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		if(user == null) {
			SplunkLog splunkLog = new SplunkLog();
			splunkLog.addField("order_submit_failure",username);
			splunkLoggingService.logToSplunk(splunkLog);
			return ResponseEntity.notFound().build();
		}
		UserOrder order = UserOrder.createFromCart(user.getCart());
		orderRepository.save(order);
		return ResponseEntity.ok(order);
	}
	
	@GetMapping("/history/{username}")
	public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		if(user == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(orderRepository.findByUser(user));
	}


	@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="Data integrity violation")
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> logNotCaughtException(Exception ex) {
		logger.error("The following error has been raised: {}", ex.getMessage());
		SplunkLog splunkLog = new SplunkLog();
		splunkLog.addField("error",ex.getMessage());
		splunkLoggingService.logToSplunk(splunkLog);
		return new ResponseEntity<String>(ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}


}
