package com.saimon.service;

import com.saimon.configuration.AwsConfiguration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.Select;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ExpensesService {

    private DynamoDbClient dynamoDbClient;

    public void registerNewExpense(String userUuid) {
        final Scanner scan = new Scanner(System.in);

        HashMap<String, AttributeValue> putItem = new HashMap<>();
        putItem.put("user_uuid", AttributeValue.builder().s(userUuid).build());

        System.out.println("What is the expense concept?");
        putItem.put("expense_name", AttributeValue.builder().s(scan.nextLine()).build());

        System.out.println("What is the expense business?");
        putItem.put("expense_business", AttributeValue.builder().s(scan.nextLine()).build());

        System.out.println("Expense amount: ");
        putItem.put("expense_amount", AttributeValue.builder().s(scan.nextLine()).build());

        System.out.println("Expense date (YYYY-MM-DD): ");
        putItem.put(
                "expense_date",
                AttributeValue
                        .builder()
                        .s(
                                LocalDate
                                .parse(scan.nextLine())
                                .toString()
                        )
                        .build()
        );

        final PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName("expenses")
                .item(putItem)
                .build();
        try {
            dynamoDbClient.putItem(putItemRequest);
            System.out.println("Expense was registered..");
        } catch (Exception e) {
            System.out.println("There was a problem registering your expense..");
            System.out.println("Please share this message with your system administrator..");
            System.out.println(e.getMessage());
        }

    }

    public void retrieveCurrentMonthExpense(String userUuid) {
        final LocalDate currentDate = LocalDate.now();
        final LocalDate firstDayOfTheMonth = LocalDate.of(
                currentDate.getYear(),
                currentDate.getMonthValue(),
                1
        );
        final LocalDate lastDayOfTheMonth = LocalDate.of(
                currentDate.getYear(),
                currentDate.getMonthValue(),
                currentDate.getMonth().length(false)
        );

        final Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":userId", AttributeValue.builder().s(userUuid).build());
        expressionValues.put(":initDate", AttributeValue.builder().s(firstDayOfTheMonth.toString()).build());
        expressionValues.put(":endDate", AttributeValue.builder().s(lastDayOfTheMonth.toString()).build());

        final QueryRequest qre = QueryRequest
                .builder()
                .tableName("expenses")
                .select(Select.ALL_ATTRIBUTES)
                .keyConditionExpression("user_uuid = :userId AND expense_date BETWEEN :initDate AND :endDate")
                .expressionAttributeValues(expressionValues)
                .build();

        try {
            final List<Map<String, AttributeValue>> items = dynamoDbClient.query(qre).items();

            System.out.println("\nExpenses registered this month are the following: ");
            items.forEach(expense -> {
                System.out.println("\nNo " + (items.indexOf(expense) + 1) + ": ");
                System.out.println("\tName: " + expense.get("expense_name").s());
                System.out.println("\tDate: " + expense.get("expense_date").s());
                System.out.println("\tBusiness: " + expense.get("expense_business").s());
                System.out.println("\tAmount: " + expense.get("expense_amount").s());
            });

        } catch (Exception e) {
            System.out.println("There was a problem registering your expense..");
            System.out.println("Please share this message with your system administrator..");
            System.out.println(e.getMessage());
        }

    }

    public ExpensesService() {
        this.dynamoDbClient = new AwsConfiguration().getClient();
    }
}
