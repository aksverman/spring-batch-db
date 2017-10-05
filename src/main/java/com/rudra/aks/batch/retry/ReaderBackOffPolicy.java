package com.rudra.aks.batch.retry;

import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;

public class ReaderBackOffPolicy implements BackOffPolicy {

	@Override
	public BackOffContext start(RetryContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void backOff(BackOffContext backOffContext) throws BackOffInterruptedException {
		// TODO Auto-generated method stub
	}

}
