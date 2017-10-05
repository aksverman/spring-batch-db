package com.rudra.aks.batch.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.item.Chunk;
import org.springframework.stereotype.Component;

import com.rudra.aks.batch.model.UserBO;
import com.rudra.aks.batch.retry.CustomException;

@Component
public class CustomChunkListener implements ChunkListener {

	private static Logger logger = LoggerFactory.getLogger(CustomChunkListener.class);

		
	@Override
	public void beforeChunk(ChunkContext context) {
		
	}

	@Override
	public void afterChunk(ChunkContext context) {
		
	}

	@Override
	public void afterChunkError(ChunkContext context) {
		String[] attributeNames = context.attributeNames();
		logger.info("Attributes : " + attributeNames);
		

		@SuppressWarnings("unchecked")
		Chunk<UserBO> c = (Chunk<UserBO>)context.getAttribute("INPUTS");
		List<UserBO> failedChunkItems = c.getItems();
		
		logger.error("Failed chunk items : " + failedChunkItems);
		 
		CustomException cause = (CustomException)context.getAttribute("sb_rollback_exception");
		logger.info(" Exception cause: " + cause.getMsg());
		
	}

}
