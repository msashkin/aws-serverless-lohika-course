# aws-serverless-lohika-course

AWS Serverless Training Lohika Course

## Serverless Quickstart

[Quickstart](https://www.serverless.com/framework/docs/providers/aws/guide/quick-start/)

[Create command](https://www.serverless.com/framework/docs/providers/aws/cli-reference/create/)

## Serverless create a Java Maven app in a folder

```sh
serverless create --template aws-java-maven --path customFolderName
```

## To configure AWS credentials used by Serverless

[Docs](https://www.serverless.com/framework/docs/providers/aws/guide/credentials/)

Define a different profile for Serverless in `~/.aws/credentials`:

```sh
[profileName1]
aws_access_key_id=***************
aws_secret_access_key=***************

[serverless]
aws_access_key_id=***************
aws_secret_access_key=***************
```

Switch to a different AWS profile per project:

```sh
export AWS_PROFILE="serverless"
```

Now everything is set to execute all the serverless CLI options like `sls deploy`

## Deploying Lambdas

```sh
serverless deploy
```

```sh
serverless deploy -f myFunction
```

## Testing Lambdas

[How to Test Serverless Applications](https://www.serverless.com/blog/how-test-serverless-applications)

Local lambda invocation using a custom event

```sh
serverless invoke local -f myFunction -p myEvent.json
```

Remote lambda invocation using a custom event

```sh
serverless invoke -f myFunction -p myEvent.json
```

Investigating Internal Server Errors by using tailing logs

```sh
serverless logs -f myFunction --tail
```
