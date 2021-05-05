package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.model.ApiGatewayResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.Collections;
import java.util.Map;

public class GetTracksHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger LOG = LogManager.getLogger(GetTracksHandler.class);

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LOG.info("Received: {}", input);
        LOG.info("DynamoDB table name: {}", System.getenv("DYNAMODB_TABLE"));
        LOG.info("Input: {}", input);

        DynamoDbClient ddb = DynamoDbClient.builder().build();

        try {
            ScanResponse scanResponse = ddb.scan(ScanRequest.builder()
                                                            .tableName(System.getenv("DYNAMODB_TABLE"))
                                                            .build());
            return ApiGatewayResponse.builder()
                                     .setStatusCode(HttpStatusCode.OK)
                                     .setObjectBody(scanResponse.items().toString())
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
