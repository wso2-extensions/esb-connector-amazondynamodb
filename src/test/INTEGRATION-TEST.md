## Integration tests for WSO2 EI Amazon DynamoDB connector
    
### Pre-requisites:

    - Maven 3.x
    - Java 1.8

### Tested Platforms: 

    - Ubuntu 16.04
    - WSO2 EI 6.4.0

STEPS

 1. Download EI 6.4.0 from official website.

 2. This EI should be configured as below; (If you are using the EI version other than 6.4.0)
    	Please make sure that the below mentioned Axis configurations are enabled (repository/conf/axis2/axis2.xml).

    	    <messageFormatter contentType="application/x-amz-json-1.0"  class="org.apache.synapse.commons.json.JsonStreamFormatter"/>
    	    <messageBuilder contentType="application/x-amz-json-1.0" 	class="org.apache.synapse.commons.json.JsonStreamBuilder"/>

 3. Compress modified EI and copy that zip file in to location "{AMAZONDYNAMODB_CONNECTOR_HOME}/repository/".

 4. Prerequisites for AmazonDynamoDB Connector Integration Testing

    Follow these steps before start testing.    
    
        i)  Create a fresh account in Amazon AWS and log on to http://aws.amazon.com/dynamodb/ with the web browser.
        ii)  Save the AWSAccessKeyId and AWSSecretKey while continuing the registration process.

 5. Update the AmazonDynamoDB properties file at location "{AMAZONDYNAMODB_CONNECTOR_HOME}/src/test/resources/artifacts/ESB/connector/config" and "{AMAZONDYNAMODB_CONNECTOR_HOME}/repository/esb-connector-amazondynamodb.properties" as below.

        i)      accessKeyId                     -   The accessKeyId of the user account to generate the signature.
        ii)     secretAccessKey                 -   The secret access key for the application generated from Amazon.
        iii)    region                          -   The region of the application access.
	    iv)     attributeDefinitions            -   An array of attributes that describe the key schema for the table and  						       indexes.
	    v)      attributeDefinitionsOpt         -   An array of attributes that describe the key schema for the table and 						      indexes.
	    vi)     tableName                       -   The name of the table.
	    vii)    tableNameOpt                    -   The name of the table which is not same as 5 vii).
	    vii)    keySchema                       -   An array attributes that make up the primary key for a table or an index.
	    viii)   localSecondaryIndexes           -   One or more local secondary indexes (the maximum is five) to be created 							on the table.
	    ix)     provisionedThroughput           -   The provisioned throughput settings for a specified table or index.
        x)      UpdateProvisionedThroughput     -   The provisioned throughput settings for a specified table or index.
        xi)     sleepTime                       -   An integer value in milliseconds, to wait between API calls to avoid 						     conflicts at API end.
        xii)    item                            -   A map of attribute name/value pairs, one for each attribute.
        xiii)   key                             -   The primary key of the item to be updated.
        xiv)    updateExpression                -   An expression that defines one or more attributes to be updated.
	    xv)     conditionExpression             -   A condition that must be satisfied in order for a conditional update to 							succeed.
        xvi)    expressionAttributeValues       -   One or more values that can be substituted in an expression.
	    xvii)   requestItems                    -   A map of one or more table names and, for each table, a map that 							    describes one or more items to retrieve from that table.
	    xix)    indexName                       -   The name of an index to query.
        xx)     projectionExpression            -   A string that identifies one or more attributes to retrieve from the 						     table.
        xxi)    keyConditionExpression          -   The condition that specifies the key value(s) for items to be retrieved.
        xxii)   queryExpressionAttributeValues  -   One or more values that can be substituted in an expression.
        xxiii)  filterExpression                -   A string that contains conditions that DynamoDB applies after the Query 							operation.
        xxiv)   scanExpressionAttributeValues   -   One or more values that can be substituted in an expression.

 6. Navigate to "<AMAZONDYNAMODB_CONNECTOR_HOME>" and run the following command. <br/>
       ```$ mvn clean install -Dskip-tests=false```
