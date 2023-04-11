package com.sonarsource.demo.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class AWSConfig {

    private String accessKey = "AKIA33KVYFLTLW7ERL37";
    private String secretKey = "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";

    // @Value("${environment.AWS_ACCESS_KEY_ID}")
    // private String accessKey;
    // @Value("${environment.AWS_SECRET_ACCESS_KEY}")
    // private String secretKey;

    public AWSCredentials credentials() {
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials()))
            .withRegion(Regions.US_EAST_1)
            .build();
    }
}