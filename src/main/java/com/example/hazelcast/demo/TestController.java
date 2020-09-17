package com.example.hazelcast.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
public class TestController {

	@Autowired
	AsyncService asyncService;

	@Autowired
	TransactionClient transactionClient;

	@Async("asyncExecutor")
	@GetMapping("/waitAsync")
	public CompletableFuture<ResponseEntity<String>> waitAsync() {
		return CompletableFuture.completedFuture(asyncService.waitSync());
	}

	@GetMapping("/ack")
	public ResponseEntity<?> ack(@RequestParam(name = "trxId") String trxId) {
		asyncService.resolveAck(trxId, "SUCCESS");
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ExceptionHandler(AsyncRequestTimeoutException.class)
	public final ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, WebRequest request) {
		return new ResponseEntity<>("TIMEOUT", HttpStatus.OK);
	}
}
