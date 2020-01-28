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

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;

@RestController
@RequestMapping("/api/item")
public class ItemController {

	final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private SplunkLoggingService splunkLoggingService;
	
	@GetMapping
	public ResponseEntity<List<Item>> getItems() {
		return ResponseEntity.ok(itemRepository.findAll());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Item> getItemById(@PathVariable Long id) {
		return ResponseEntity.of(itemRepository.findById(id));
	}
	
	@GetMapping("/name/{name}")
	public ResponseEntity<List<Item>> getItemsByName(@PathVariable String name) {
		List<Item> items = itemRepository.findByName(name);
		return items == null || items.isEmpty() ? ResponseEntity.notFound().build()
				: ResponseEntity.ok(items);
			
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
