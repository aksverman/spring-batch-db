package com.rudra.aks.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
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

import com.rudra.aks.batch.model.UserBO;
import com.rudra.aks.batch.processor.UserItemProcessor;

@Configuration
@Import({DBConfig.class})
@EnableBatchProcessing
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
    public ItemReader<UserBO> itemReader() throws UnexpectedInputException, ParseException {

    	FlatFileItemReader<UserBO> reader = new FlatFileItemReader<UserBO>();
    	reader.setLinesToSkip(1);
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
    protected Step step1(ItemReader<UserBO> reader, 
    						ItemProcessor<UserBO, UserBO> processor, ItemWriter<UserBO> writer) {
        return steps.get("step1").<UserBO, UserBO> chunk(10)
          .reader(reader).processor(processor).writer(writer).build();
    }
 
    @Bean(name = "firstBatchJob")
    public Job job(@Qualifier("step1") Step step1) {
        return jobs.get("firstBatchJob").start(step1).build();
    }
}
