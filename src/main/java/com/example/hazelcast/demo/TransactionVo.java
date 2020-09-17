package com.example.hazelcast.demo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
// 메인 요청 trxId 및 재시도 시 하위 trxId를 가지고 있는다.
public class TransactionVo implements Serializable {
	private static final long serialVersionUID = 1L;

	public TransactionVo() {
		this.state = State.WAIT.name();
		this.reqTimestamp = System.currentTimeMillis();
	}

	@NonNull
	private String state;
	private final long reqTimestamp;
	private long resTimestamp;

	public void setResTimestamp(){
		this.resTimestamp = System.currentTimeMillis();
	}

	public enum State {
		WAIT,
		SUCCESS,
		TIMEOUT,
		ERROR
		;
	}
}
