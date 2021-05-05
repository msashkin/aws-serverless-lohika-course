package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.model.CreateTrackRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SendJsonContentsFromSqsToDynamoDbHandler implements RequestHandler<Map<String, Object>, Void> {

    private static final Logger LOG = LogManager.getLogger(SendJsonContentsFromSqsToDynamoDbHandler.class);

    @Override
    public Void handleRequest(Map<String, Object> input, Context context) {
        try {
            String dynamoDbTable = System.getenv("DYNAMODB_TABLE");

            LOG.info("Received: {}", input);
            LOG.info("DynamoDB table name: {}", dynamoDbTable);

            String body = (String) ((LinkedHashMap) ((List) input.get("Records")).get(0)).get("body");

            LOG.info("Body object: {}", body);

            DynamoDbClient ddb = DynamoDbClient.builder().build();

            for (Object o : new ObjectMapper().readValue(body, List.class)) {
                LinkedHashMap bodyEntry = (LinkedHashMap) o;

                LOG.info("Body entry: {}", bodyEntry);

                CreateTrackRequest createTrackRequest = CreateTrackRequest.fromMap(bodyEntry);

                Map<String, AttributeValue> itemValues = new HashMap<>();

                String id = UUID.randomUUID().toString();
                itemValues.put("id", AttributeValue.builder().s(id).build());
                itemValues.put("title", AttributeValue.builder().s(createTrackRequest.getTitle()).build());
                itemValues.put("artist", AttributeValue.builder().s(createTrackRequest.getArtist()).build());

                PutItemRequest request = PutItemRequest.builder()
                                                       .tableName(dynamoDbTable)
                                                       .item(itemValues)
                                                       .build();
                try {
                    ddb.putItem(request);
                } catch (DynamoDbException e) {
                    LOG.error(e);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        return null;
    }
}
