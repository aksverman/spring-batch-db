package com.rudra.aks.batch.processor;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.rudra.aks.batch.model.UserBO;
import com.rudra.aks.batch.retry.CustomException;

public class UserItemProcessor implements ItemProcessor<UserBO, UserBO> {

	private static Logger	logger = LoggerFactory.getLogger(UserItemProcessor.class);
	
	@Autowired
	@Qualifier("simpleRetry")
	RetryPolicy		simpleRetry;
	
	@Autowired
	@Qualifier("backOff")
	BackOffPolicy	backOffPolicy;
	
	RetryTemplate	retryTemplate;
	
	@PostConstruct
	public void		init() {
		retryTemplate = new RetryTemplate();
		
		retryTemplate.setRetryPolicy(simpleRetry);
		retryTemplate.setBackOffPolicy(backOffPolicy);
	}
	
	/*
	 * 
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	public UserBO process(UserBO item) throws CustomException {
		//logger.info("Start : " + getClass().getName() + " : process()");
		
		UserBO userBO = retryTemplate.execute(new RetryCallImpl(item)/*, new RecoveryCallBackImpl()*/);
		logger.info("User processed : " + userBO);
		return userBO;
		
		/*if( item.getUserid() == 5)
			throw new CustomException("Excluding user with userid : 5");
		return item;*/
	}

	class RetryCallImpl	implements RetryCallback<UserBO, CustomException> {

		UserBO	user;
		public RetryCallImpl(UserBO item) {
			this.user = item;
		}

		@Override
		public UserBO doWithRetry(RetryContext context) throws CustomException {
			if( user.getUserid() == 5)
				throw new CustomException("Excluding user with userid : 5");
			return user;
		}
	}	
	
	
	class RecoveryCallBackImpl	implements RecoveryCallback<UserBO> {

		@Override
		public UserBO recover(RetryContext context) throws Exception {
			String[] attributeNames = context.attributeNames();
			logger.info("RecoveryCallBack invoked" + attributeNames);
			return new UserBO(44, "recovered", "recover@call.back");
		}
	}
}
