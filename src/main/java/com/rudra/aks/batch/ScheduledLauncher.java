package com.rudra.aks.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rudra.aks.batch.controller.JobLaunchController;

@Component
public class ScheduledLauncher {

	@Autowired
	JobLauncher	jobLauncher;
	
	@Autowired
	Job			job;
	
	private static Logger logger = LoggerFactory.getLogger(JobLaunchController.class);
	private static int jobid = 10;
	
	//@Scheduled(fixedDelay = 1000L)
	public void lauchJob() {
		
		JobParameters jobParam = new JobParametersBuilder().addString("jobid", String.valueOf(jobid)).toJobParameters();
		JobExecution execution = null;
		try {
			execution = jobLauncher.run(job, jobParam);
		} catch (JobExecutionAlreadyRunningException | JobRestartException 
				| JobInstanceAlreadyCompleteException	| JobParametersInvalidException e) {
			logger.error("Job execution failed with jobid : " + jobid + e);
		}
		
		logger.info( "Job " + jobid++ + " Executed with status : " + execution.getStatus() );
	
	}
}
