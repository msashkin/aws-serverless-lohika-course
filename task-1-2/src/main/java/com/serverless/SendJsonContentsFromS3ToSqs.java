package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SendJsonContentsFromS3ToSqs implements RequestHandler<Map<String, Object>, Void> {

    private static final Logger LOG = LogManager.getLogger(SendJsonContentsFromS3ToSqs.class);

    @Override
    public Void handleRequest(Map<String, Object> input, Context context) {
        try {
            String queueUrl = System.getenv("QUEUE_URL");
            String s3Region = System.getenv("S3_REGION");

            LOG.info("Received: {}", input);
            LOG.info("SQS queue url: {}", queueUrl);
            LOG.info("S3 region: {}", s3Region);

            // I'm so sorry :(
            LinkedHashMap s3 = (LinkedHashMap) ((LinkedHashMap) ((List) input.get("Records")).get(0)).get("s3");
            String bucketName = (String) ((LinkedHashMap) s3.get("bucket")).get("name");
            String objectKey = (String) ((LinkedHashMap) s3.get("object")).get("key");

            LOG.info("s3 object: {}", s3);
            LOG.info("Bucket name: {}", bucketName);
            LOG.info("Object key: {}", objectKey);

            S3Client s3Client = S3Client.builder()
                                        .region(Region.of(s3Region))
                                        .build();

            SqsClient sqsClient = SqsClient.builder()
                                           .region(Region.of(s3Region))
                                           .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                                                .bucket(bucketName)
                                                                .key(objectKey)
                                                                .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(s3Object, stringWriter, StandardCharsets.UTF_8);
            String jsonContents = stringWriter.toString();

            LOG.info("Downloaded string: {}", jsonContents);

            sqsClient.sendMessage(SendMessageRequest.builder()
                                                    .queueUrl(queueUrl)
                                                    .messageBody(jsonContents)
                                                    .build());
        } catch (Exception e) {
            LOG.error(e);
        }
        return null;
    }
}
