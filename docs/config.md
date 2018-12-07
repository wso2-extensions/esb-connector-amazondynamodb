# Configuring Amazon DynamoDB Operations

[[Prerequisites]](#Prerequisites) [[Initializing the Connector]](#initializing-the-connector)

## Prerequisites

### Get User Credentials

Before you can use the Amazon DynamoDB connector, you must get an AWS access key ID and secret access key. For more information, see [Setting Up DynamoDB (Web Service)](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/SettingUp.DynamoWebService.html).

## Initializing the Connector

To use the Amazon DynamoDB connector, add the <amazondynamodb.init> element in your configuration before carrying out any other operations.
To authenticate, it uses the Signature Version 4 signing specification, which describes how to construct signed requests to AWS. Whenever you send a request to AWS, you must include authorization information with your request so that AWS can verify the authenticity of the request.
AWS uses the authorization information from your request to recreate your signature and then compares that signature with the one that you sent. These two signatures must match for you to successfully access AWS. [Click here](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html) for further reference on the signing process.

**init**
```xml
<amazondynamodb.init>
    <region>{$ctx:region}</region>
    <secretAccessKey>{$ctx:secretAccessKey}</secretAccessKey>
    <accessKeyId>{$ctx:accessKeyId}</accessKeyId>
    <blocking>{$ctx:blocking}</blocking>
</amazondynamodb.init>
```
**Properties** 
* region: The region of the application access. 
* secretAccessKey: The secret access key.
* accessKeyId: The accessKeyId of the user account to generate the signature.
* blocking: Boolean type, this property helps the connector perform blocking invocations to Amazon DynamoDB. 

## Additional information

Ensure that the following Axis2 configurations are added and enabled in the <ESB_HOME>\repository\conf\axis2\axis2.xml file.

### Required message formatters
**messageFormatters**
```xml
<messageFormatter contentType="application/x-amz-json-1.0"
class="org.apache.synapse.commons.json.JsonStreamFormatter"/>
```

### Required message builders
**messageBuilders**
```xml
<messageBuilder contentType="application/x-amz-json-1.0"
class="org.apache.synapse.commons.json.JsonStreamBuilder"/>
```
**Note :**
If you want to perform blocking invocations, ensure that the above builder and formatter are added and enabled in the <ESB_HOME>\repository\conf\axis2\axis2_blocking_client.xml file.

Now that you have connected to Amazon DynamoDB, use the information in the following topics to perform various operations with the connector:

[Working with Items in Amazon DynamoDB](items.md)

[Working with Tables in Amazon DynamoDB](tables.md)
