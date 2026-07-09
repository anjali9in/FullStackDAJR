1. ### How to use S3 in application

- Create an S3 bucket in AWS console.
- Create an IAM user with programmatic access and attach a policy with S3 full access.
- and create role for ec-2-s3 role and attach it to ec2 in security group and give permission to access s3 bucket from ec2 instance.

- Note down the access key and secret key of the IAM user.
- In your application, use the AWS SDK to interact with S3.
    - add the AWS SDK dependency in your project (for example, in Maven or Gradle).
    ```xml
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>s3</artifactId>
            <version>2.25.x</version> 
        </dependency>
    ```

    - You can use the access key and secret key to authenticate your requests.
    - add secrets key, bucket name, region in application.properties file or in config file.

    - configure s3client in project by 
    ```java

    @Bean
    public S3Client s3Client() {

        // 1. Create the credentials object using the injected keys
        AwsBasicCredentials credentials = AwsBasicCredentials.create(${accessKey}, ${secretKey});
        
        // 2. Build and return the S3Client
        return S3Client.builder()
                .region(Region.of(${region}))
                .credentialsProvider(StaticCredentialsProvider
                .create(credentials))
                .build();
    }
    ```
    
    - Use the SDK methods to upload, download, and manage files in your S3 bucket.
    
-- -- -- --- --- --- -- -- --

2. ### How to use DynamoDB in application

- Create a DynamoDB table in AWS console.
- Create an IAM user with programmatic access and attach a policy with DynamoDB full access.
- Note down the access key and secret key of the IAM user.
- In your application, use the AWS SDK to interact with DynamoDB.
    - add the AWS SDK dependency in your project (for example, in Maven or Gradle).
    ```xml
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>dynamodb</artifactId>
            <version>2.25.x</version>
        </dependency>
    ```
