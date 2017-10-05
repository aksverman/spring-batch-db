package com.rudra.aks.batch.processor;

import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.exception.ExceptionHandler;

public class CustomTaskletExceptionHandler implements ExceptionHandler {

	@Override
	public void handleException(RepeatContext context, Throwable throwable) throws Throwable {
		int startedCount = context.getStartedCount();
		
	}

}
