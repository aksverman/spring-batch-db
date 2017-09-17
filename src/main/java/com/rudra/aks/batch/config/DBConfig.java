package com.rudra.aks.batch.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DBConfig {

	
	@Bean
	public DataSource	dataSource() {
		BasicDataSource	dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/test");
		dataSource.setUsername( "root" );
		dataSource.setPassword( "root" );
		return dataSource;	
	}
	
	@Bean 
	public PlatformTransactionManager	txManager() {
		return new DataSourceTransactionManager(dataSource());
	}
	
	public JobRepository	getJobRepository() throws Exception {
		JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
		factoryBean.setDataSource(dataSource());
		factoryBean.setTransactionManager(txManager());
		factoryBean.afterPropertiesSet();
		return factoryBean.getObject();
	}
	
	public JobLauncher	getJobLaucher() throws Exception {
		SimpleJobLauncher	jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(getJobRepository());
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}
	
}
