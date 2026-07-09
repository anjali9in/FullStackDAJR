package com.core.fullstack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Client s3Client(@Value("${cloud.aws.credentials.access-key}") String accessKey,
                              @Value("${cloud.aws.credentials.secret-key}") String secretKey) {

        // 1. Create the credentials object using the injected keys
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // 2. Build and return the S3Client
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }


   // S3Client with profile without credentials
   @Bean
   @Profile("dev")
   public S3Client s3ClientWithProfile() {
       return S3Client.builder()
               .region(Region.of(region))
               // AWS automatically handles authentication via IAM Roles!
               .build();
   }
}
