package com.rudra.aks.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.rudra.aks.batch.config.BatchConfig;

public class SingleApp {

	/*public static void main(String [] args) {
		
		AbstractApplicationContext appContext = new AnnotationConfigApplicationContext(BatchConfig.class);
		
		JobLauncher jobLauncher = appContext.getBean(JobLauncher.class);
		Job job = appContext.getBean(Job.class);
		
		try {
			JobExecution execution = jobLauncher.run(job, new JobParameters());
			System.out.println("job executed : " + execution.getStatus());
		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {
			e.printStackTrace();
		}
		
		System.out.println("done !!!");
		appContext.close();
	}*/
}
