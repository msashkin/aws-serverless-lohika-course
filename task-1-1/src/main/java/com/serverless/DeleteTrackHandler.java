package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.model.ApiGatewayResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeleteTrackHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger LOG = LogManager.getLogger(DeleteTrackHandler.class);

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LOG.info("Received: {}", input);
        LOG.info("DynamoDB table name: {}", System.getenv("DYNAMODB_TABLE"));

        LinkedHashMap pathParameters = (LinkedHashMap) input.get("pathParameters");
        LOG.info("Path params: {}", pathParameters);

        String id = (String) pathParameters.get("id");
        LOG.info("Id to delete: {}", id);

        DynamoDbClient ddb = DynamoDbClient.builder().build();

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s(id).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                                                     .tableName(System.getenv("DYNAMODB_TABLE"))
                                                     .key(key)
                                                     .build();
        try {
            ddb.deleteItem(request);
            return ApiGatewayResponse.builder()
                                     .setStatusCode(HttpStatusCode.OK)
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
