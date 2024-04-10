package com.saimon;

import com.saimon.configuration.AwsConfiguration;
import com.saimon.service.ExpensesService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        DynamoDbClient dynamoDbClient = new AwsConfiguration().getClient();

        ExpensesService expensesService = new ExpensesService();
        String userEmail = "";
        String userUuid = "";

        while(userEmail.isBlank() || userUuid.isBlank()) {
            System.out.println("Input your user email: \n");

            var userEmailInput = scan.next();

            System.out.printf("Verifying user email: %s existence in the database.. \n", userEmailInput);

            var keyToGet = new HashMap<String, AttributeValue>();
            keyToGet.put(
                    "user_email", AttributeValue.builder().s(userEmailInput).build()
            );

            GetItemRequest itemRequest = GetItemRequest.builder()
                    .key(keyToGet)
                    .attributesToGet("user_email", "user_uuid", "user_name")
                    .tableName("users")
                    .build();
            try {
                Map<String, AttributeValue> items = dynamoDbClient.getItem(itemRequest).item();

                if (items.size() == 0) {

                    System.out.printf("This email is not registered.. \nAre you sure this is the correct email %s \n", userEmailInput);
                    System.out.println("Y/N: ");
                    var inputYN = scan.next();

                    if (inputYN.toUpperCase().equals("Y")) {
                        HashMap<String, AttributeValue> putItem = new HashMap<String, AttributeValue>();
                        putItem.put("user_email", AttributeValue.builder().s(userEmailInput).build());
                        putItem.put("user_uuid", AttributeValue.builder().s(UUID.randomUUID().toString()).build());

                        System.out.println("What is your name? ");
                        putItem.put("user_name", AttributeValue.builder().s(scan.nextLine()).build());

                        System.out.println("Please enter your phone number: ");
                        putItem.put("user_phone", AttributeValue.builder().s(scan.nextLine()).build());

                        putItem.put("user_date", AttributeValue.builder().s(LocalDate.now().toString()).build());

                        PutItemRequest putItemRequest = PutItemRequest.builder()
                                .tableName("users")
                                .item(putItem)
                                .build();
                        try {
                            PutItemResponse putResponse = dynamoDbClient.putItem(putItemRequest);
                            userUuid = putResponse.attributes().get("user_uuid").s();
                            System.out.println("New client registered in the database...");
                            userEmail = userEmailInput;
                        } catch (Exception e) {
                            System.out.println("The system has failed to register the user in the database...");
                            System.out.println("Please contact support team and provide the following: ");
                            System.out.println(e.getMessage());
                        }
                    }
                } else {
                    userEmail = items.get("user_email").s();
                    userUuid = items.get("user_uuid").s();
                }
            } catch (DynamoDbException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.printf("Your user email is %s and your id is %s..", userEmail, userUuid);

        var action = 0;

        while (action != 9) {
            action = 0;
            while (action == 0) {
                System.out.println("\nWhat do you want to do? \n");
                System.out.println("1. Register new expend.");
                System.out.println("2. Review current month expenses.");
                System.out.println("3. Review past month expenses.");
                System.out.println("4. Register new money income.");
                System.out.println("5. Review current month incomes.");
                System.out.println("6. Generate reports.");
                System.out.println("9. Exit.");
                System.out.println("Input JUST ONE the number: ");

                try {
                    action = Integer.valueOf(scan.next());
                } catch (Exception e) {
                    action = 0;
                }

            }

            switch (action) {
                case 1:
                    expensesService.registerNewExpense(userUuid);
                    break;
                case 2:
                    expensesService.retrieveCurrentMonthExpense(userUuid);
                    break;
                case 3:
                    System.out.println("Review past month expenses to implement..");
                    break;
                case 4:
                    System.out.println("Register new income to implement..");
                    break;
                case 5:
                    System.out.println("Review current month income to implement..");
                    break;
                case 6:
                    System.out.println("Generate reports, to implement..");
                    break;
                case 9:
                    System.out.println("Logged out from system.. Good bye..");
                    break;
                default:
                    System.out.println("You didn't select any valid number, please try again...");
                    break;
            }
        }
    }

}