# Working with Tables in Amazon DynamoDB

[[Overview]](#overview)  [[Operation details]](#operation-details)  [[Sample configuration]](#sample-configuration)

### Overview 

The following operations allow you to work with tables. Click an operation name to see details on how to use it.
For a sample proxy service that illustrates how to work with tables, see [Sample configuration](#sample-configuration).

| Operation        | Description |
| ------------- |-------------|
| [createTable](#creating-a-table)    | Creates a new table.  |
| [deleteTable](#deleting-a-table)      | Deletes a table.  |
| [describeTable](#retrieving-information-about-a-table)      | Retrieves information about a table.  |
| [listTables](#retrieving-tables-in-the-current-aws-region)      | Retrieves tables in the current AWS region.  |
| [updateTable](#updating-a-table)      | Updates a table.  |
| [describeLimits	](#retrieving-the-current-provisioned-capacity-limits-allowed-in-a-region)      | Retrieves the current provisioned-capacity limits allowed in a region.  |

### Operation details

This section provides further details on the operations related to tables.

#### Creating a table

The createTable operation creates a new table. Table names must be unique within each region.

**createTable**
```xml
<amazondynamodb.createTable>
    <attributeDefinitions>{$ctx:attributeDefinitions}</attributeDefinitions>
    <tableName>{$ctx:tableName}</tableName>
    <keySchema>{$ctx:keySchema}</keySchema>
    <localSecondaryIndexes>{$ctx:localSecondaryIndexes}</localSecondaryIndexes>
    <provisionedThroughput>{$ctx:provisionedThroughput}</provisionedThroughput>
    <StreamSpecification>{$ctx:StreamSpecification}</StreamSpecification>
    <globalSecondaryIndexes>{$ctx:globalSecondaryIndexes}</globalSecondaryIndexes>
</amazondynamodb.createTable>
```

**Properties**
* attributeDefinitions: Required - A list of attributes that describe the key schema for the table and indexes. 
* tableName: Required - The name of the table to create. (Should be of minimum length 3, and maximum length 255.)
* keySchema: Required - Specifies the attributes that make up the primary key for a table or an index. The attributes in keySchema must also be defined in attributeDefinitions. Each KeySchemaElement in the array is composed of:
  * AttributeName - The name of the key attribute.
  * KeyType - The role that the key attribute will assume. Possible values are as follows:
    HASH - partition key
    RANGE - sort key
    **Note :**
    The partition key of an item is also known as its hash attribute, and the sort key of an item is also known as its range attribute. For a simple primary key (partition key), you must provide exactly one element with a KeyType of HASH . For a composite primary key(partition key and sort key), you must provide exactly two elements, in the following order: 
    The first element must have a KeyType of HASH , and the second element must have a KeyType of RANGE .
* localSecondaryIndexes: Optional - One or more local secondary indexes (the maximum is five) to be created on the table. Each index is scoped to a given partition key value. There is a 10 GB size limit per partition key value. Else, the size of a local secondary index is unconstrained. Each local secondary index in the array includes the following:
  * IndexName - The name of the local secondary index. Should be unique for this table.
  * KeySchema - Specifies the key schema for the local secondary index. The key schema should begin with the same partition key as the table.
  * KeySchema - Specifies the key schema for the local secondary index. The key schema should begin with the same partition key as the table.
    * ProjectionType - Possible values are as follows:
      * KEYS_ONLY - Only the index and primary keys are projected into the index.
      * INCLUDE - Only the specified table attributes are projected into the index. The list of projected attributes are in NonKeyAttributes.
      * ALL - All of the table attributes are projected into the index.
    * NonKeyAttributes - A list of one or more non-key attribute names that are projected into the secondary index. The total count of attributes provided in NonKeyAttributes, summed across all of the secondary indexes, should not exceed 20.
* provisionedThroughput: Required - Represents the provisioned throughput setting for a specified table or index.
* StreamSpecification: Optional - The settings for DynamoDB streams on the table. These settings consist of:
  * StreamEnabled - Indicates whether to be enabled (true) or disabled (false) streams.
  * StreamViewType - When an item in the table is modified, the StreamViewType determines what information is written to the table's stream. Possible values for StreamViewType are:
    * KEYS_ONLY - Only the key attributes of the modified item are written to the stream.
    * NEW_IMAGE - The entire item, as it appears after it was modified, is written to the stream.
    * OLD_IMAGE - The entire item, as it appeared before it was modified, is written to the stream.
    * NEW_AND_OLD_IMAGES - Both the new and the old item images of the item are written to the stream.
* globalSecondaryIndexes: Optional - One or more global secondary indexes (the maximum is five) to be created on the table. Each global secondary index in the array includes the following:
  * IndexName - The name of the global secondary index. Should be unique for this table.
  * KeySchema - Specifies the key schema for the global secondary index.
  * Projection - Specifies attributes that are copied (projected) from the table into the index. These are in addition to the primary key attributes and index key attributes, which are automatically projected. Each attribute specification is composed of:
    * ProjectionType - Possible values are as follows:
      * KEYS_ONLY - Only the index and primary keys are projected into the index.
      * INCLUDE - Only the specified table attributes are projected into the index. The list of projected attributes are in NonKeyAttributes.
      * ALL - All of the table attributes are projected into the index.
    * NonKeyAttributes - A list of one or more non-key attribute names that are projected into the secondary index. The total count of attributes provided in NonKeyAttributes, summed across all of the secondary indexes, should not exceed 20.
  * ProvisionedThroughput - Specifies the provisioned throughput setting for the global secondary index.
  
**Sample request**

Following is a sample request that can be handled by the createTable operation.

```json
{
"accessKeyId":"AKIAxxxxxxxxxxxx",
"secretAccessKey":"id4qxxxxxxxx",
"region":"us-east-1",
"blocking":"false",
"attributeDefinitions": [
        {
            "AttributeName": "ForumName",
            "AttributeType": "S"
        },
        {
            "AttributeName": "Subject",
            "AttributeType": "S"
        },
        {
            "AttributeName": "LastPostDateTime",
            "AttributeType": "S"
        }
    ],
    "tableName": "Thread",
    "keySchema": [
        {
            "AttributeName": "ForumName",
            "KeyType": "HASH"
        },
        {
            "AttributeName": "Subject",
            "KeyType": "RANGE"
        }
    ],
    "localSecondaryIndexes": [
        {
            "IndexName": "LastPostIndex",
            "KeySchema": [
                {
                    "AttributeName": "ForumName",
                    "KeyType": "HASH"
                },
                {
                    "AttributeName": "LastPostDateTime",
                    "KeyType": "RANGE"
                }
            ],
            "Projection": {
                "ProjectionType": "KEYS_ONLY"
            }
        }
    ],
    "provisionedThroughput": {
        "ReadCapacityUnits": 5,
        "WriteCapacityUnits": 5
    }
}
```

**Sample response**

Given below is a sample response for the createTable operation.

```json
{
   "TableDescription":{
      "TableArn":"arn:aws:dynamodb:us-west-2:123456789012:table/Thread",
      "AttributeDefinitions":[
         {
            "AttributeName":"ForumName",
            "AttributeType":"S"
         },
         {
            "AttributeName":"LastPostDateTime",
            "AttributeType":"S"
         },
         {
            "AttributeName":"Subject",
            "AttributeType":"S"
         }
      ],
      "CreationDateTime":1.36372808007E9,
      "ItemCount":0,
      "KeySchema":[
         {
            "AttributeName":"ForumName",
            "KeyType":"HASH"
         },
         {
            "AttributeName":"Subject",
            "KeyType":"RANGE"
         }
      ],
      "LocalSecondaryIndexes":[
         {
            "IndexArn":"arn:aws:dynamodb:us-west-2:123456789012:table/Thread/index/LastPostIndex",
            "IndexName":"LastPostIndex",
            "IndexSizeBytes":0,
            "ItemCount":0,
            "KeySchema":[
               {
                  "AttributeName":"ForumName",
                  "KeyType":"HASH"
               },
               {
                  "AttributeName":"LastPostDateTime",
                  "KeyType":"RANGE"
               }
            ],
            "Projection":{
               "ProjectionType":"KEYS_ONLY"
            }
         }
      ],
      "ProvisionedThroughput":{
         "NumberOfDecreasesToday":0,
         "ReadCapacityUnits":5,
         "WriteCapacityUnits":5
      },
      "TableName":"Thread",
      "TableSizeBytes":0,
      "TableStatus":"CREATING"
   }
}
```
**Related Amazon DynamoDB documentation**
https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_CreateTable.html

#### Deleting a table

The deleteTable operation deletes a table and all of its items.  
  
**deleteTable**
```xml
<amazondynamodb.deleteTable>
    <tableName>{$ctx:tableName}</tableName>
</amazondynamodb.deleteTable>
```
**Properties**
* tableName: Required - The name of the table to delete. (Should be of minimum length 3, and maximum length 255.)

**Sample request**

Following is a sample request that can be handled by the deleteTable operation.

```json
{
"accessKeyId":"AKIAxxxxxxxxxxxx",
"secretAccessKey":"id4qxxxxxxxx",
"region":"us-east-1",
"blocking":"false",
    "tableName": "TestTable"
}
```

**Sample response**

Given below is a sample response for the deleteTable operation.

```json
{
   "TableDescription":{
      "TableArn":"arn:aws:dynamodb:us-west-2:123456789012:table/Reply",
      "ItemCount":0,
      "ProvisionedThroughput":{
         "NumberOfDecreasesToday":0,
         "ReadCapacityUnits":5,
         "WriteCapacityUnits":5
      },
      "TableName":"Reply",
      "TableSizeBytes":0,
      "TableStatus":"DELETING"
   }
}
```
**Related Amazon DynamoDB documentation**
https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_DeleteTable.html

#### Retrieving information about a table

The describeTable operation retrieves information about a table, such as the current status of the table, when it was created, the primary key schema, and any indexes on the table.

**describeTable**
```xml
<amazondynamodb.describeTable>
    <tableName>{$ctx:tableName}</tableName>
</amazondynamodb.describeTable>
```

**Properties**
* tableName: Required - The name of the table for which information is to be retrieved. (Should be of minimum length 3, and maximum length 255.)

**Sample request**

Following is a sample request that can be handled by the describeTable operation.

```json
{
"accessKeyId":"AKIAxxxxxxxxxxxx",
"secretAccessKey":"id4qxxxxxxxx",
"region":"us-east-1",
"blocking":"false",
    "tableName": "TestTable"
} 
```

**Sample response**

Given below is a sample response for the describeTable operation.

```json
{
   "Table":{
      "TableArn":"arn:aws:dynamodb:us-west-2:123456789012:table/Thread",
      "AttributeDefinitions":[
         {
            "AttributeName":"ForumName",
            "AttributeType":"S"
         },
         {
            "AttributeName":"LastPostDateTime",
            "AttributeType":"S"
         },
         {
            "AttributeName":"Subject",
            "AttributeType":"S"
         }
      ],
      "CreationDateTime":1.363729002358E9,
      "ItemCount":0,
      "KeySchema":[
         {
            "AttributeName":"ForumName",
            "KeyType":"HASH"
         },
         {
            "AttributeName":"Subject",
            "KeyType":"RANGE"
         }
      ],
      "LocalSecondaryIndexes":[
         {
            "IndexArn":"arn:aws:dynamodb:us-west-2:123456789012:table/Thread/index/LastPostIndex",
            "IndexName":"LastPostIndex",
            "IndexSizeBytes":0,
            "ItemCount":0,
            "KeySchema":[
               {
                  "AttributeName":"ForumName",
                  "KeyType":"HASH"
               },
               {
                  "AttributeName":"LastPostDateTime",
                  "KeyType":"RANGE"
               }
            ],
            "Projection":{
               "ProjectionType":"KEYS_ONLY"
            }
         }
      ],
      "ProvisionedThroughput":{
         "NumberOfDecreasesToday":0,
         "ReadCapacityUnits":5,
         "WriteCapacityUnits":5
      },
      "TableName":"Thread",
      "TableSizeBytes":0,
      "TableStatus":"ACTIVE"
   }
}
```
**Related Amazon DynamoDB documentation**
https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_DescribeTable.html

#### Retrieving tables in the current AWS region

The listTables operation retrieves the tables that you own in the current AWS region. The output from listTables is paginated, with each page returning a maximum of 100 table names.

**listTables**
```xml
<amazondynamodb.listTables>
    <exclusiveStartTableName>{$ctx:exclusiveStartTableName}</exclusiveStartTableName>
    <limit>{$ctx:limit}</limit>
</amazondynamodb.listTables> 
```

**Properties**
* exclusiveStartTableName: Optional - The first table name that the listTables operation evaluates. Use the value returned for LastEvaluatedTableName (This is the name of the last table in the current page of results.) in the previous operation, so that you can obtain the next page of result
* limit: Optional - The maximum number of table names to retrieve. If this parameter is not specified, the limit is 100.

**Sample request**

Following is a sample request that can be handled by the listTables operation.

```json
{
"accessKeyId":"AKIAxxxxxxxxxxxx",
"secretAccessKey":"id4qxxxxxxxx",
"region":"us-east-1",
"blocking":"false",
"exclusiveStartTableName":"Music",
"limit":4
}
```

**Sample response**

Given below is a sample response for the listTables operation.

```json
{
   "LastEvaluatedTableName":"Thread",
   "TableNames":[
      "Forum",
      "Reply",
      "Thread"
   ]
}
```
**Related Amazon DynamoDB documentation**
https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_ListTables.html

#### Updating a table

The updateTable operation updates provisioned throughput settings, global secondary indexes, or DynamoDB Streams settings for a given table. You can only perform one of the following operations at a time:
  * Modify the provisioned throughput settings of the table.
  * Enable or disable streams on the table.
  * Remove a global secondary index from the table.
  * Create a new global secondary index on the table. Once the index begins backfilling, you can use updateTable to perform other operations.

**updateTable**
```xml
<amazondynamodb.updateTable>
    <tableName>{$ctx:tableName}</tableName>
    <attributeDefinitions>{$ctx:attributeDefinitions}</attributeDefinitions>
    <globalSecondaryIndexUpdates>{$ctx:globalSecondaryIndexUpdates}</globalSecondaryIndexUpdates>
    <StreamSpecification>{$ctx:StreamSpecification}</StreamSpecification>
    <provisionedThroughput>{$ctx:provisionedThroughput}</provisionedThroughput>
</amazondynamodb.updateTable>
```

**Properties**
* tableName: Required - The name of the table to be updated. (Should be of minimum length 3, and maximum length 255.)
* attributeDefinitions: Optional - A list of attributes that describe the key schema for the table and indexes. If you are adding a new global secondary index to the table, AttributeDefinitions should include the key element(s) of the new index.
* globalSecondaryIndexUpdates: Optional - An array of one or more global secondary indexes for the table. For each index in the array, you can request one of the following actions:
  * Create - To add a new global secondary index to the table.
  * Update - To modify the provisioned throughput settings of an existing global secondary index.
  * Delete - To remove a global secondary index from the table.
* StreamSpecification: Optional - Represents the DynamoDB streams configuration for the table.
* provisionedThroughput: Optional - The new provisioned throughput setting for the specified table or index.

**Sample request**

Following is a sample request that can be handled by the updateTable operation.

```json
{ 
   "accessKeyId":"AKIAxxxxxxxxxxxx",
   "secretAccessKey":"id4qxxxxxxxx",
   "region":"us-east-1",
   "blocking":"false",
   "tableName":"Thread",
   "provisionedThroughput":{ 
      "ReadCapacityUnits":12,
      "WriteCapacityUnits":12
   }
}
```

**Sample response**

Given below is a sample response for the updateTable operation.

```json
{
   "TableDescription":{
      "TableArn":"arn:aws:dynamodb:us-west-2:123456789012:table/Thread",
      "AttributeDefinitions":[
         {
            "AttributeName":"ForumName",
            "AttributeType":"S"
         },
         {
            "AttributeName":"LastPostDateTime",
            "AttributeType":"S"
         },
         {
            "AttributeName":"Subject",
            "AttributeType":"S"
         }
      ],
      "CreationDateTime":1.363801528686E9,
      "ItemCount":0,
      "KeySchema":[
         {
            "AttributeName":"ForumName",
            "KeyType":"HASH"
         },
         {
            "AttributeName":"Subject",
            "KeyType":"RANGE"
         }
      ],
      "LocalSecondaryIndexes":[
         {
            "IndexName":"LastPostIndex",
            "IndexSizeBytes":0,
            "ItemCount":0,
            "KeySchema":[
               {
                  "AttributeName":"ForumName",
                  "KeyType":"HASH"
               },
               {
                  "AttributeName":"LastPostDateTime",
                  "KeyType":"RANGE"
               }
            ],
            "Projection":{
               "ProjectionType":"KEYS_ONLY"
            }
         }
      ],
      "ProvisionedThroughput":{
         "LastIncreaseDateTime":1.363801701282E9,
         "NumberOfDecreasesToday":0,
         "ReadCapacityUnits":5,
         "WriteCapacityUnits":5
      },
      "TableName":"Thread",
      "TableSizeBytes":0,
      "TableStatus":"UPDATING"
   }
} 
```

**Related Amazon DynamoDB documentation**
https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_UpdateTable.html

#### Retrieving the current provisioned-capacity limits allowed in a region

The describeLimits operation retrieves the current provisioned-capacity limits allowed in a region.

**describeLimits**
```xml
<amazondynamodb.describeLimits/> 
```

**Sample request**

Following is a sample request that can be handled by the describeLimits operation.

```json
{ 
   "accessKeyId":"AKIAxxxxxxxxxxxx",
   "secretAccessKey":"id4qxxxxxxxx",
   "region":"us-east-1",
   "blocking":"false"
}
```

**Sample response**

Given below is a sample response for the describeLimits operation.

```json
{
   "AccountMaxReadCapacityUnits":20000,
   "AccountMaxWriteCapacityUnits":20000,
   "TableMaxReadCapacityUnits":10000,
   "TableMaxWriteCapacityUnits":10000
}
```
**Related Amazon DynamoDB documentation**
https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_DescribeLimits.html

### Sample configuration

Following example illustrates how to connect to Amazon DynamoDB with the init operation and createTable operation.

1. Create a sample proxy as below :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse"
       name="createTable"
       transports="https,http"
       statistics="disable"
       trace="disable"
       startOnLoad="true">
   <target>
      <inSequence>
         <property expression="json-eval($.secretAccessKey)" name="secretAccessKey"/>
         <property expression="json-eval($.accessKeyId)" name="accessKeyId"/>
         <property expression="json-eval($.region)" name="region"/>
         <property expression="json-eval($.blocking)" name="blocking"/>
         <property expression="json-eval($.attributeDefinitions)" name="attributeDefinitions"/>
         <property expression="json-eval($.tableName)" name="tableName"/>
         <property expression="json-eval($.keySchema)" name="keySchema"/>
         <property expression="json-eval($.localSecondaryIndexes)" name="localSecondaryIndexes"/>
         <property expression="json-eval($.provisionedThroughput)" name="provisionedThroughput"/>
         <amazondynamodb.init>
            <secretAccessKey>{$ctx:secretAccessKey}</secretAccessKey>
            <accessKeyId>{$ctx:accessKeyId}</accessKeyId>
            <region>{$ctx:region}</region>
            <blocking>{$ctx:blocking}</blocking>
         </amazondynamodb.init>
         <amazondynamodb.createTable>
            <attributeDefinitions>{$ctx:attributeDefinitions}</attributeDefinitions>
            <tableName>{$ctx:tableName}</tableName>
            <keySchema>{$ctx:keySchema}</keySchema>
            <localSecondaryIndexes>{$ctx:localSecondaryIndexes}</localSecondaryIndexes>
            <provisionedThroughput>{$ctx:provisionedThroughput}</provisionedThroughput>
         </amazondynamodb.createTable>
         <respond/>
      </inSequence>
      <outSequence>
        <send/>
      </outSequence>
   </target>
   <description/>
</proxy>
```
2. Create an json file named createTable.json and copy the configurations given below to it:

```json
{
"accessKeyId":"AKIAxxxxxxxxxxxx",
"secretAccessKey":"id4qxxxxxxxx",
"region":"us-east-1",
"blocking":"false",
"attributeDefinitions": [
        {
            "AttributeName": "ForumName",
            "AttributeType": "S"
        },
        {
            "AttributeName": "Subject",
            "AttributeType": "S"
        },
        {
            "AttributeName": "LastPostDateTime",
            "AttributeType": "S"
        }
    ],
    "tableName": "Thread",
    "keySchema": [
        {
            "AttributeName": "ForumName",
            "KeyType": "HASH"
        },
        {
            "AttributeName": "Subject",
            "KeyType": "RANGE"
        }
    ],
    "localSecondaryIndexes": [
        {
            "IndexName": "LastPostIndex",
            "KeySchema": [
                {
                    "AttributeName": "ForumName",
                    "KeyType": "HASH"
                },
                {
                    "AttributeName": "LastPostDateTime",
                    "KeyType": "RANGE"
                }
            ],
            "Projection": {
                "ProjectionType": "KEYS_ONLY"
            }
        }
    ],
    "provisionedThroughput": {
        "ReadCapacityUnits": 5,
        "WriteCapacityUnits": 5
    }
}
```

3. Replace the credentials with your values.

4. Execute the following curl command:

```bash
curl http://localhost:8280/services/createTable -H "Content-Type: application/json" -d @createTable.json
```
5. Amazon DynamoDB returns an json response similar to the one shown below:
 
```json
{
   "TableDescription":{
      "TableArn":"arn:aws:dynamodb:us-west-2:123456789012:table/Thread",
      "AttributeDefinitions":[
         {
            "AttributeName":"ForumName",
            "AttributeType":"S"
         },
         {
            "AttributeName":"LastPostDateTime",
            "AttributeType":"S"
         },
         {
            "AttributeName":"Subject",
            "AttributeType":"S"
         }
      ],
      "CreationDateTime":1.36372808007E9,
      "ItemCount":0,
      "KeySchema":[
         {
            "AttributeName":"ForumName",
            "KeyType":"HASH"
         },
         {
            "AttributeName":"Subject",
            "KeyType":"RANGE"
         }
      ],
      "LocalSecondaryIndexes":[
         {
            "IndexArn":"arn:aws:dynamodb:us-west-2:123456789012:table/Thread/index/LastPostIndex",
            "IndexName":"LastPostIndex",
            "IndexSizeBytes":0,
            "ItemCount":0,
            "KeySchema":[
               {
                  "AttributeName":"ForumName",
                  "KeyType":"HASH"
               },
               {
                  "AttributeName":"LastPostDateTime",
                  "KeyType":"RANGE"
               }
            ],
            "Projection":{
               "ProjectionType":"KEYS_ONLY"
            }
         }
      ],
      "ProvisionedThroughput":{
         "NumberOfDecreasesToday":0,
         "ReadCapacityUnits":5,
         "WriteCapacityUnits":5
      },
      "TableName":"Thread",
      "TableSizeBytes":0,
      "TableStatus":"CREATING"
   }
}
```