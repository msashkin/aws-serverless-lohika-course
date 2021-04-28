package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.model.ApiGatewayResponse;
import com.serverless.model.CreateTrackRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateTrackHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger LOG = LogManager.getLogger(CreateTrackHandler.class);

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LOG.info("Received: {}", input);
        LOG.info("DynamoDB table name: {}", System.getenv("DYNAMODB_TABLE"));

        CreateTrackRequest createTrackRequest = CreateTrackRequest.fromJson((String) input.get("body")); // not safe but for learning it's ok

        DynamoDbClient ddb = DynamoDbClient.builder().build();

        Map<String, AttributeValue> itemValues = new HashMap<>();

        String id = UUID.randomUUID().toString();
        itemValues.put("id", AttributeValue.builder().s(id).build());
        itemValues.put("title", AttributeValue.builder().s(createTrackRequest.getTitle()).build());
        itemValues.put("artist", AttributeValue.builder().s(createTrackRequest.getArtist()).build());

        PutItemRequest request = PutItemRequest.builder()
                                               .tableName(System.getenv("DYNAMODB_TABLE"))
                                               .item(itemValues)
                                               .build();
        try {
            ddb.putItem(request);
            return ApiGatewayResponse.builder()
                                     .setStatusCode(HttpStatusCode.CREATED)
                                     .setObjectBody(id)
                                     .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                                     .build();
        } catch (DynamoDbException e) {
            LOG.error(e);
            return ApiGatewayResponse.builder()
                                     .setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                                     .setObjectBody("Something went wrong. See logs.")
                                     .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                                     .build();
        }
    }
}
