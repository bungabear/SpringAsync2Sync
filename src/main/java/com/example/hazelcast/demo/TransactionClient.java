package com.example.hazelcast.demo;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.BaseMap;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
class TransactionClient {
	// 요청에 대한 비동기 응답을 http로 받기 위해, transactionId를 통해 각 요청 페어를 식별하고
	// 타임 아웃시 정해진 횟수 만큼 subTransactionId를 발급하여 요청한다.
	// 비동기 응답을 수신하면, subTransaction을 포함한 연관된 모은 정보를 제거한다.
	// 비동기 응답을 수신하여 changeTransactionState를 수행하면, listener를 통해 대기중인 controller 요청에 전달된다.
	public static final String PREFIX_TRANSACTION_STORE = "TRX_STORE_";
	public static final String KEY_TRX_STORE = "KEY_TRX_STORE";
	private final HazelcastInstance hazelcastInstance
			= Hazelcast.newHazelcastInstance();

	// subTransaction을 추가한다.
	public void addTransaction(String key, String trxId){
		log.error(String.format("addSubTransaction %s %s", key, trxId));
		TransactionContext context = beginTransaction();
		TransactionalMap<String, TransactionVo> map = context.getMap(PREFIX_TRANSACTION_STORE + key);
		map.put(trxId, new TransactionVo(), 60, TimeUnit.SECONDS);
		TransactionalMap<String, String> keyTrxMap = context.getMap(KEY_TRX_STORE);
		keyTrxMap.put(trxId, key, 60, TimeUnit.SECONDS);
		print(map);
		context.commitTransaction();
	}

	// Transaction 상태를 변경한다.
	public boolean changeTransactionState(String trxId, String state){
		log.error(String.format("changeTransactionState %s %s", trxId, state));
		TransactionContext context = beginTransaction();
		TransactionalMap<String, String> keyTrxMap = context.getMap(KEY_TRX_STORE);
		String key = keyTrxMap.get(trxId);
		if (key == null) {
			return false; // 누락되거나 없음
		}
		TransactionalMap<String, TransactionVo> map = context.getMap(PREFIX_TRANSACTION_STORE + key);

		TransactionVo transaction = map.get(trxId);
		if (transaction == null) {
			return false; // 누락되거나 없음
		}
		transaction.setState(state);
		transaction.setResTimestamp();
		map.put(trxId, transaction, 60, TimeUnit.SECONDS);
		print(map);
		context.commitTransaction();
		return true;
	}

	public void removeTransaction(String key){
		log.error(String.format("removeTransaction %s", key));
		TransactionContext context = beginTransaction();
		TransactionalMap<String, TransactionVo> map = context.getMap(PREFIX_TRANSACTION_STORE + key);
		TransactionalMap<String, String> keyTrxMap = context.getMap(KEY_TRX_STORE);
		map.keySet().forEach(it -> {
			keyTrxMap.delete(it);
			map.delete(it);
		});
		print(map);
		context.commitTransaction();
	}

	private <T, K, O extends BaseMap<T, K>> void print(O map){
		map.keySet().forEach(key -> {
			log.error(String.format("%s : %s", key, map.get(key)));
		});
	}

	public IMap<String, TransactionVo> getTransactionMap(String key) {
		return hazelcastInstance.getMap(PREFIX_TRANSACTION_STORE + key);
	}

	public Collection<DistributedObject> getAll(String key) {
		return hazelcastInstance.getDistributedObjects();
	}

	private TransactionContext beginTransaction(){
		TransactionOptions options = new TransactionOptions()
				.setTransactionType( TransactionOptions.TransactionType.ONE_PHASE );
		TransactionContext context = hazelcastInstance.newTransactionContext( options );
		context.beginTransaction();
		return context;
	}

}