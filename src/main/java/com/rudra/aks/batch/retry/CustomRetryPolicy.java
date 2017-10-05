package com.rudra.aks.batch.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.stereotype.Component;

@Component
public class CustomRetryPolicy implements RetryPolicy {

	private static Logger	logger = LoggerFactory.getLogger(CustomRetryPolicy.class);

	
	@Override
	public boolean canRetry(RetryContext context) {
		Throwable lastThrowable = context.getLastThrowable();
		System.out.println("retry policy invoked ...." + lastThrowable);
		return true;
	}
	
	@Override
	public RetryContext open(RetryContext parent) {
		//int retryCount = parent.getRetryCount();
		//logger.info("Retry count: " + retryCount);
		return parent;
	}

	@Override
	public void close(RetryContext context) {

	}

	@Override
	public void registerThrowable(RetryContext context, Throwable throwable) {
		logger.info(throwable.getMessage());
	}

}
