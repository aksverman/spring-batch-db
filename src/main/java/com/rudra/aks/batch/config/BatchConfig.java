package com.rudra.aks.batch.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.ResourcesItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.rudra.aks.batch.model.UserBO;
import com.rudra.aks.batch.processor.UserItemProcessor;
import com.rudra.aks.batch.retry.CustomException;
import com.rudra.aks.batch.retry.CustomRetryPolicy;
import com.rudra.aks.batch.util.CustomChunkListener;
import com.rudra.aks.batch.util.FirsthJobListener;

@Configuration
@Import({DBConfig.class})
public class BatchConfig {

	@Autowired
	DataSource	dataSource;
    
    @Autowired
    private JobBuilderFactory jobs;
 
    @Autowired
    private StepBuilderFactory steps;
 
    @Value("record.txt")
    private Resource inputCsv;
    
    
    @Bean
    //@Retryable(include = Exception.class, maxAttempts =2 , backoff = @Backoff(delay = 2000))
    public ItemReader<UserBO> itemReader() throws UnexpectedInputException, ParseException {

    	FlatFileItemReader<UserBO> reader = new FlatFileItemReader<UserBO>();
    	//reader.setLinesToSkip(1);
    	reader.setResource(new ClassPathResource("/record.txt"));
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        String[] tokens = { "userid", "username", "emailid" };
        tokenizer.setNames(tokens);
        tokenizer.setDelimiter(",");
        
        BeanWrapperFieldSetMapper<UserBO> fieldSetMapper = new BeanWrapperFieldSetMapper<UserBO>();
        fieldSetMapper.setTargetType(UserBO.class);
        
        DefaultLineMapper<UserBO> lineMapper = new DefaultLineMapper<UserBO>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);
        reader.setSaveState(false);
        return reader;
    }
 
    @Bean
    public ItemProcessor<UserBO, UserBO> itemProcessor() {
        return new UserItemProcessor();
    }
 
    /*@Bean
    public ItemWriter<UserBO> itemWriter()
      throws MalformedURLException {
        StaxEventItemWriter<UserBO> itemWriter = 
          new StaxEventItemWriter<UserBO>();
        itemWriter.setRootTagName("UserBORecord");
        itemWriter.setResource(outputXml);
        return itemWriter;
    } */
 
    @Bean
    public ItemWriter<UserBO>	dbItemWriter() {
    	JdbcBatchItemWriter<UserBO> dbWriter = new JdbcBatchItemWriter<UserBO>();
    	dbWriter.setDataSource(dataSource);
    	dbWriter.setSql("insert into USER_BATCH(userid, username, emailid) values (:userid, :username, :emailid)");
    	dbWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<UserBO>());
    	return dbWriter;
    }
     
    @Bean
    protected Step step1(ItemReader<UserBO> reader, ItemProcessor<UserBO, UserBO> processor, ItemWriter<UserBO> writer) {
        return steps.get("step1")
        			//.tasklet(new Step2Tasklet()).taskExecutor(new SimpleAsyncTaskExecutor()).build();
        			.<UserBO, UserBO> chunk(5)
        				.reader(reader)
        			.processor(processor)
        				.faultTolerant()
        				.retry(Exception.class)
						.retryLimit(2)
						.retryPolicy(new CustomRetryPolicy())
        			.writer(writer)
        				.faultTolerant()
        				/*.retry(Exception.class)
        				.retryLimit(4)
        				.retryPolicy(simpleRetry())
        				.backOffPolicy(backOffPolicy())*/
        			.skipLimit(2).skip(Exception.class)
        			//.stream(customItemStream())
        			.taskExecutor(taskExecutor()).listener(new CustomChunkListener())
        			.build();
    }
    
   /* private ItemStream customItemStream() {
    	ResourcesItemReader reader = new ResourcesItemReader();
    	
    	return reader;
	}*/

	@Bean("simpleRetry")
    public RetryPolicy simpleRetry() {
    	Map<Class<? extends Throwable>, Boolean> ex = new HashMap<Class<? extends Throwable>, Boolean>();
    	ex.put(CustomException.class, true);
		ex.put(Exception.class, true);
		
    	SimpleRetryPolicy policy = new SimpleRetryPolicy(4, ex);
		policy.setMaxAttempts(4);
    	return policy;
	}

	/*
     * Setting BackOffPolicy with fixed delay & exponential delay
     * 
     * exponential delay will start with 1.5 secs till max 10 secs 
     * with interval of double the previous retry time.
     * 
     */
    @Bean("backOff")
    public BackOffPolicy backOffPolicy() {
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(12000);
		
		ExponentialBackOffPolicy expBackOffPolicy2 = new ExponentialBackOffPolicy();
		expBackOffPolicy2.setInitialInterval(1500);
		expBackOffPolicy2.setMultiplier(4);
		expBackOffPolicy2.setMaxInterval(10000);
		
    	return expBackOffPolicy2;
	}

	@Bean
    public	TaskExecutor	taskExecutor() {
    	/*SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
		taskExecutor.setConcurrencyLimit(2);*/
		
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(2);
		taskExecutor.setMaxPoolSize(3);
    	taskExecutor.setThreadNamePrefix("job-thread");
    	return taskExecutor;
    }
    
    @Bean(name = "firstBatchJob")
    public Job job(@Qualifier("step1") Step step1) {
    	return jobs.get("firstBatchJob")
    			.incrementer(new RunIdIncrementer())
    			.listener(new FirsthJobListener() )
    			.start(step1)
    			.build();
    }
    
    /*@Bean
    protected Step step2() {
        return steps.get("step2").tasklet(new Step2Tasklet()).build();
    }
    
    @Bean
    protected Step step3() {
        return steps.get("step3").tasklet(new Step3Tasklet()).build();
    }
    
    @Bean
    protected Step step4(ItemReader<UserBO> reader, ItemProcessor<UserBO, UserBO> processor, ItemWriter<UserBO> writer) {
        return steps.get("step4")
        			//.flow(flowBuilder()).build().execute();
        			.<UserBO, UserBO> chunk(10000)
        			.reader(reader)//.faultTolerant().retryLimit(3).backOffPolicy()
        			.processor(processor)
        			.writer(writer)
        			.build();
    }
 
    //@Bean(name = "paralleljobs")
    public	Job	parallelJob() {
    	return  jobs.get("paralleljobs")
    				.incrementer(new RunIdIncrementer())
    				.start(step1(itemReader(), itemProcessor(), dbItemWriter()))
    				.split(new SimpleAsyncTaskExecutor()).add(flowBuilder())
    				.next(step4(itemReader(), itemProcessor(), dbItemWriter()))
    				.end().build();

    				
    }
   
	private Flow	flowBuilder() {
    	final Flow	flow1 = new FlowBuilder<Flow>("flowto23").from(step2()).next(step3()).end();
    	return flow1;
    }*/

}
