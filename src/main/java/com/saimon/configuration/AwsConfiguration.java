package com.saimon.configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class AwsConfiguration {

    private DynamoDbClient dynamoDbClient;

    public AwsConfiguration() {
        dynamoDbClient = generateDynamoDbClientConfigurations();
    }

    public DynamoDbClient getClient() {
        return dynamoDbClient;
    }

    private DynamoDbClient generateDynamoDbClientConfigurations() {
        return DynamoDbClient
                .builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

}
