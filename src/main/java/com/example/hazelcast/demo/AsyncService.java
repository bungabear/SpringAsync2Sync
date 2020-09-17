package com.example.hazelcast.demo;

import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.internal.util.UuidUtil;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class AsyncService {

	@Autowired
	TransactionClient transactionClient;

	public ResponseEntity<String> waitSync() {
		CompletableFuture<String> result = new CompletableFuture<>();
		String key = UuidUtil.newSecureUuidString();
		String trxId = UuidUtil.newSecureUuidString();
		log.error("key : " + key);
		log.error("trxId : " + trxId);
		IMap<String, TransactionVo> map = transactionClient.getTransactionMap(key);
		map.addEntryListener(new EntryAdapter<String, TransactionVo>(){
			@Override
			public void entryUpdated(EntryEvent<String, TransactionVo> event) {
				TransactionVo transaction = event.getValue();
				log.error(event.toString());
				switch (transaction.getState()){
					case "SUCCESS":
						result.complete("SUCCESS");
						break;

					case "ERROR":
						result.complete("ERROR");
						break;

					case "TIMEOUT":
						result.complete("TIMEOUT");
						break;

					default:
				}
			}
		}, trxId, true);
		transactionClient.addTransaction(key, trxId);
		String res = null; // TODO timeout and retry
		try {
			res = result.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			res = "FAIL";
		} catch (ExecutionException e) {
			e.printStackTrace();
			res = "FAIL";
		}
		transactionClient.removeTransaction(key);
		log.error("result : " + res);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	public void resolveAck(String trxID, String state){
		transactionClient.changeTransactionState(trxID, "SUCCESS");
	}

}
