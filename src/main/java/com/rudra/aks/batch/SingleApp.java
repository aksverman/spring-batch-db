package com.rudra.aks.batch;

import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

import com.rudra.aks.batch.config.BatchConfig;

public class SingleApp {

	public static void main(String [] args) {
		
		AbstractApplicationContext appContext = new AnnotationConfigApplicationContext(BatchConfig.class);
		
		JobLauncher jobLauncher = appContext.getBean(JobLauncher.class);
		Job job = appContext.getBean(Job.class);
		
		executeJob(jobLauncher, job);
		
		appContext.close();
	}
	
	@Scheduled(fixedDelay = 1000L)
	private static void executeJob(JobLauncher jobLauncher, Job job) {
		JobParameters jobParam = new JobParametersBuilder().addDate("date", new Date()).toJobParameters();
		
		try {
			JobExecution execution = jobLauncher.run(job, jobParam);
			System.out.println("job executed : " + execution.getStatus());
		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {
			e.printStackTrace();
		}
		System.out.println("done !!!");

	}
}
