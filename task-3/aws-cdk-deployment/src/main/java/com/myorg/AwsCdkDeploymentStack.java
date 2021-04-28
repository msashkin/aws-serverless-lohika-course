package com.myorg;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.s3.Bucket;

public class AwsCdkDeploymentStack extends Stack {

    public AwsCdkDeploymentStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AwsCdkDeploymentStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here

        Bucket.Builder.create(this, "msaschin-test-bucket")
                      .build();
    }
}
