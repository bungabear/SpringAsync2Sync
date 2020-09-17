# SpringAsync2Sync
Hazelcast map을 사용하여 async wait 상태로 별도 API 응답을 받아 sync로 응답하는 예제

- Hazelcast
  - Transaction
  - IMap
  - Listener
  - EntryTimeout
- Async
  - AsyncConfigurer(ThreadPoolTaskExecutor)
- CompleteFuture 
