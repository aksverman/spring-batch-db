package com.rudra.aks.batch.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.rudra.aks.batch.model.UserBO;

//@Component("userReader")
public class UserItemReader implements	ItemReader<UserBO>{

	private static Logger logger = LoggerFactory.getLogger(UserItemReader.class);

	private List<UserBO> usersList;
	
	private static int fileIndex = 0;
	
	@Value("record.txt")
    private Resource inputFile;
	
	
	@PostConstruct
	public void init() throws Exception {
		logger.info("file : " + inputFile);
	
		File	f = new ClassPathResource("record.txt").getFile();
		FileReader	file = new FileReader(f);
		BufferedReader	reader = new BufferedReader(file);
		
		usersList = new ArrayList<UserBO>();
		
		String line = "";
		while( (line = reader.readLine()) != null) {

			UserBO userbo =  new UserBO();
			String [] userline = line.trim().split(",");
			userbo.setUserid(Integer.parseInt(userline[0]));
			userbo.setUsername(userline[1]);
			userbo.setEmailid(userline[2]);
			usersList.add(userbo);
		}
		reader.close();
		
	}
	
	@Override
	public UserBO read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		logger.info(" " + getClass().getName() + " : read()"); 
		
		UserBO userBO = usersList.get(fileIndex++);
		logger.info("Read user : " + userBO);
		return userBO;
	}


}
