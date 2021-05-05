package com.myorg;

import software.amazon.awscdk.core.App;

public class AwsCdkDeploymentApp {

    public static void main(final String[] args) {
        App app = new App();

        // We can pass parameters to the cdk command line with the -c parameter tp be more flexible
        // For example: cdk deploy -c accountId=123456789 -c region=ap-southeast-2
        // If not provided, CDK CLI will always take the account and region that we have pre-configured with the AWS CLI

//        String accountId = (String) app.getNode().tryGetContext("accountId");
//        Objects.requireNonNull(accountId, "Context variable 'accountId' must not be null");
//
//        String region = (String) app.getNode().tryGetContext("region");
//        Objects.requireNonNull(accountId, "Context variable 'region' must not be null");

        // If you don't specify 'env', this stack will be environment-agnostic.
        // Account/Region-dependent features and context lookups will not work,
        // but a single synthesized template can be deployed anywhere.

        // Uncomment the next block to specialize this stack for the AWS Account
        // and Region that are implied by the current CLI configuration.
                /*
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                */

        // Uncomment the next block if you know exactly what Account and Region you
        // want to deploy the stack to.
                /*
                .env(Environment.builder()
                        .account("123456789012")
                        .region("us-east-1")
                        .build())
                */

        // For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html

        new AwsCdkDeploymentStack(app,
                                  "AwsCdkDeploymentStack");
//                                  StackProps.builder()
//                                            .env(Environment.builder()
//                                                            .account(accountId)
//                                                            .region(region).build())
//                                            .build());

        app.synth();
    }
}
