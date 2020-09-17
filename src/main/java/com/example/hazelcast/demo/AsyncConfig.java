package com.example.hazelcast.demo;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * https://myjamong.tistory.com/103
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

	//기본 Thread 수
	private static int TASK_CORE_POOL_SIZE = 10;
	//최대 Thread 수
	private static int TASK_MAX_POOL_SIZE = 100;
	//QUEUE 수
	private static int TASK_QUEUE_CAPACITY = 0;
	private static int TASK_THREAD_ALIVE_TIME = 60;
	//Thread Bean Name
	public final String EXECUTOR_BEAN_NAME = "asyncExecutor";

	@Bean(name=EXECUTOR_BEAN_NAME)
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setBeanName(EXECUTOR_BEAN_NAME);
		executor.setCorePoolSize(TASK_CORE_POOL_SIZE);
		executor.setMaxPoolSize(TASK_MAX_POOL_SIZE);
		executor.setQueueCapacity(TASK_QUEUE_CAPACITY);
		executor.setKeepAliveSeconds(TASK_THREAD_ALIVE_TIME);
		executor.setWaitForTasksToCompleteOnShutdown(false);
		executor.initialize();
		return executor;
	}

	/*
	 * Thread Process도중 에러 발생시
	 */
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new AsyncUncaughtExceptionHandler() {
			@Override
			public void handleUncaughtException(Throwable ex, Method method, Object... params) {
				ex.printStackTrace();
			}
		};
	}
}