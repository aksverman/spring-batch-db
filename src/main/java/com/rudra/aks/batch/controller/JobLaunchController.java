package com.rudra.aks.batch.controller;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lauch")
public class JobLaunchController {

	@Autowired
	JobLauncher	jobLauncher;
	
	@Autowired
	//@Qualifier("firstBatchJob")
	Job			job;
	
	//private static int jobid = 10;
	private static Logger logger = LoggerFactory.getLogger(JobLaunchController.class);
	
	//@Scheduled(fixedDelay = 1000L)
	//@Retryable(include = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 3000))
	@RequestMapping(path = "/lauchJob/{jobid}", method = RequestMethod.GET)
	public	String	lauchJob(@PathVariable("jobid") String jobid) throws Exception{
		
		JobParameters jobParam = new JobParametersBuilder().addString("jobid", String.valueOf(jobid)).toJobParameters();
		JobExecution execution = null;
		try {
			execution = jobLauncher.run(job, jobParam);
		} catch (JobExecutionAlreadyRunningException | JobRestartException 
				| JobInstanceAlreadyCompleteException	| JobParametersInvalidException e) {
			logger.error("Job execution failed with jobid : " + jobid + e);
			throw new Exception();
		}
		
		return "Job Executed with status : " + execution.getStatus();
	}
	
	
}
