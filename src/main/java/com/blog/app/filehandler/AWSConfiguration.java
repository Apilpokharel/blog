package com.blog.app.filehandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;




@Configuration
public class AWSConfiguration {

	

	
	@Value("${amazon.aws.access_key_id}")
	private String accessKey;
	
	@Value("${amazon.aws.secret_access_key}")
	private String secretKey;
	
	
	@Value("${amazon.s3.region}")
	private String region;
	
	
	@Bean
	public BasicAWSCredentials basicAWSCredentials() {
		return new BasicAWSCredentials(accessKey, secretKey);
	}
	
	
	
	@Bean
	public AmazonS3 amazonS3(AWSCredentials awsCredentials) {
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
		builder.withCredentials(new AWSStaticCredentialsProvider(awsCredentials));
		builder.setRegion(region);
		AmazonS3 s3client = builder.build();
		return s3client;
	}
}
