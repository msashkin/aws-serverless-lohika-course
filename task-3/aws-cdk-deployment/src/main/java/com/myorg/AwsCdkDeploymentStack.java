package com.myorg;

import software.amazon.awscdk.core.BundlingOptions;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.DockerVolume;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;
import software.amazon.awscdk.services.lambda.AssetCode;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.S3EventSource;
import software.amazon.awscdk.services.lambda.eventsources.S3EventSourceProps;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.EventType;
import software.amazon.awscdk.services.s3.NotificationKeyFilter;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.amazon.awscdk.services.sqs.Queue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static software.amazon.awscdk.core.BundlingOutput.ARCHIVED;

public class AwsCdkDeploymentStack extends Stack {

    private static final Runtime LAMBDA_RUNTIME = Runtime.JAVA_8; // should be in sync with pom.xml java build properties

    public AwsCdkDeploymentStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AwsCdkDeploymentStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Table tracksTable = new Table(this, "api-dynamodb-dervice-dev", TableProps.builder()
                                                                                  .partitionKey(Attribute.builder()
                                                                                                         .name("id")
                                                                                                         .type(AttributeType.STRING)
                                                                                                         .build())
                                                                                  .build());

        Queue tracksQueue = new Queue(this, "s3-sqs-service-dev");

        Bucket s3Bucket = new Bucket(this, "msaschin-aws-serverless-lohika-course-s3-sqs-service");

        String s3Region = "us-east-1";

        List<String> apiDynamoDbServicePackagingInstructions = Arrays.asList(
                "/bin/sh",
                "-c",
                "cd api-dynamodb-service " +
                        "&& mvn clean install " +
                        "&& cp /asset-input/api-dynamodb-service/target/api-dynamodb-service-0.0.1.jar /asset-output/"
        );

        List<String> s3sqsServicePackagingInstructions = Arrays.asList(
                "/bin/sh",
                "-c",
                "cd s3-sqs-service " +
                        "&& mvn clean install " +
                        "&& cp /asset-input/s3-sqs-service/target/s3-sqs-service-0.0.1.jar /asset-output/"
        );

        List<String> sqsDynamoDbServicePackagingInstructions = Arrays.asList(
                "/bin/sh",
                "-c",
                "cd sqs-dynamodb-service " +
                        "&& mvn clean install " +
                        "&& cp /asset-input/sqs-dynamodb-service/target/sqs-dynamodb-service-0.0.1.jar /asset-output/"
        );

        Function getTracksFunction = lambdaFunction(apiDynamoDbServicePackagingInstructions,
                                                    "getTracks",
                                                    "com.serverless.GetTracksHandler",
                                                    tracksTable.getTableName(),
                                                    tracksQueue.getQueueUrl(),
                                                    s3Region);
        tracksTable.grantReadWriteData(getTracksFunction);


        Function createTrackFunction = lambdaFunction(apiDynamoDbServicePackagingInstructions,
                                                      "createTrack",
                                                      "com.serverless.CreateTrackHandler",
                                                      tracksTable.getTableName(),
                                                      tracksQueue.getQueueUrl(),
                                                      s3Region);
        tracksTable.grantReadWriteData(createTrackFunction);


        Function deleteTrackFunction = lambdaFunction(apiDynamoDbServicePackagingInstructions,
                                                      "deleteTrack",
                                                      "com.serverless.DeleteTrackHandler",
                                                      tracksTable.getTableName(),
                                                      tracksQueue.getQueueUrl(),
                                                      s3Region);
        tracksTable.grantReadWriteData(deleteTrackFunction);


        Function sendJsonContentsFromS3ToSqsFunction = lambdaFunction(s3sqsServicePackagingInstructions,
                                                                      "sendJsonContentsFromS3ToSqs",
                                                                      "com.serverless.SendJsonContentsFromS3ToSqsHandler",
                                                                      tracksTable.getTableName(),
                                                                      tracksQueue.getQueueUrl(),
                                                                      s3Region);
        sendJsonContentsFromS3ToSqsFunction.addEventSource(new S3EventSource(s3Bucket,
                                                                             S3EventSourceProps.builder()
                                                                                               .events(Collections.singletonList(EventType.OBJECT_CREATED))
                                                                                               .filters(Collections.singletonList(NotificationKeyFilter.builder()
                                                                                                                                                       .suffix(".json")
                                                                                                                                                       .build()))
                                                                                               .build()));
        s3Bucket.grantRead(sendJsonContentsFromS3ToSqsFunction);
        tracksQueue.grantSendMessages(sendJsonContentsFromS3ToSqsFunction);


        Function sendJsonContentsFromSqsToDynamoDbFunction = lambdaFunction(sqsDynamoDbServicePackagingInstructions,
                                                                            "sendJsonContentsFromSqsToDynamoDb",
                                                                            "com.serverless.SendJsonContentsFromSqsToDynamoDbHandler",
                                                                            tracksTable.getTableName(),
                                                                            tracksQueue.getQueueUrl(),
                                                                            s3Region);
        sendJsonContentsFromSqsToDynamoDbFunction.addEventSource(new SqsEventSource(tracksQueue));
        tracksTable.grantReadWriteData(sendJsonContentsFromSqsToDynamoDbFunction);
        tracksQueue.grantConsumeMessages(sendJsonContentsFromS3ToSqsFunction);


        RestApi restApi = new RestApi(this, "tracks-api");
        Resource tracks = restApi.getRoot().addResource("tracks");
        Resource track = tracks.addResource("{track_id}");

        LambdaIntegration getTracksLambdaIntegration = new LambdaIntegration(getTracksFunction);
        tracks.addMethod("GET", getTracksLambdaIntegration);
        LambdaIntegration createTrackLambdaIntegration = new LambdaIntegration(createTrackFunction);
        tracks.addMethod("POST", createTrackLambdaIntegration);
        LambdaIntegration deleteTrackLambdaIntegration = new LambdaIntegration(deleteTrackFunction);
        track.addMethod("DELETE", deleteTrackLambdaIntegration);
    }

    private Function lambdaFunction(List<String> packagingInstructions,
                                    String id,
                                    String handlerName,
                                    String dynamoDbTableName,
                                    String queueUrl,
                                    String s3Region) {
        Map<String, String> environment = new HashMap<>();
        environment.put("DYNAMODB_TABLE", dynamoDbTableName);
        environment.put("QUEUE_URL", queueUrl);
        environment.put("S3_REGION", s3Region);

        AssetCode code = Code.fromAsset("../", // we are in 'aws-cdk-deployment' folder and need to go one folder up to explore service folders for code
                                        AssetOptions.builder()
                                                    .bundling(lambdaBundlingOptions(packagingInstructions))
                                                    .build());

        return new Function(this, id, FunctionProps.builder()
                                                   .runtime(LAMBDA_RUNTIME)
                                                   .code(code)
                                                   .handler(handlerName)
                                                   .environment(environment)
                                                   .memorySize(512)
                                                   .timeout(Duration.seconds(20))
                                                   //.logRetention(RetentionDays.ONE_WEEK)
                                                   .build());
    }

    private static BundlingOptions lambdaBundlingOptions(List<String> lambdaCommand) {
        return BundlingOptions.builder()
                              .command(lambdaCommand)
                              .image(LAMBDA_RUNTIME.getBundlingImage())
                              .volumes(Collections.singletonList(
                                      // Mount local .m2 repo to avoid download all the dependencies again inside the container
                                      DockerVolume.builder()
                                                  .hostPath(System.getProperty("user.home") + "/.m2/")
                                                  .containerPath("/root/.m2/")
                                                  .build()))
                              .user("root")
                              .outputType(ARCHIVED)
                              .build();
    }
}
