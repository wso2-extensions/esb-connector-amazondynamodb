# Working with Items in Amazon DynamoDB

[[Overview]](#overview)  [[Operation details]](#operation-details)  [[Sample configuration]](#sample-configuration)

### Overview 

The following operations allow you to work with items. Click an operation name to see details on how to use it.
For a sample proxy service that illustrates how to work with items, see [Sample configuration](#sample-configuration).

| Operation        | Description |
| ------------- |-------------|
| [batchGetItem](#retrieving-attributes-of-an-item)    | Retrieves items of an attribute.  |
| [batchWriteItem](#writing-a-batch-item)      | Writes a batch item.  |
| [deleteItem](#deleting-an-item)      | Deletes an item.  |
| [getItem](#retrieving-an-item)      | Retrieves an item.  |
| [putItem](#creating-an-item)      | Creates a new item.  |
| [query](#querying )      | Accesses items from a table.  |
| [scan](#scanning-an-item)      | Scans an item.   |
| [updateItem](#updating-an-item)      | Updates an item.   |


### Operation details

This section provides further details on the operations related to items.

#### Retrieving attributes of an item

The batchGetItem operation returns the attributes of one or more items from one or more tables.
The requested items are identified by the primary key. A single operation can retrieve up to 16 MB of data, which can contain as many as 100 items. 

The batchGetItem operation returns a partial result if the response size limit is exceeded, the table's provisioned throughput is exceeded, or an internal processing failure occurs. 
If a partial result is returned, the operation returns a value for UnprocessedKeys. You can use this value to retry the operation starting with the next item to get. For example, if you ask to retrieve 100 items, but each individual item is 300 KB in size, the system returns 52 items (so that the16 MB limit is not exceeded) and an appropriate UnprocessedKeys value, so that you can get the next page of results. 
If required, your application can include its own logic to assemble the pages of results into one dataset.

If none of the items can be processed due to insufficient provisioned throughput on all of the tables in the request, batchGetItem will throw an exception. If at least one of the items is successfully processed, batchGetItem completes successfully while returning the keys of the unread items in UnprocessedKeys. By default, batchGetItem performs eventually consistent reads on every table in the request. 
If you want strongly consistent reads instead, you can set ConsistentRead to true for any or all tables. To minimize response latency, batchGetItem retrieves items in parallel.

**batchGetItem**
```xml
<amazondynamodb.batchGetItem>
    <requestItems>{$ctx:requestItems}</requestItems>
    <returnConsumedCapacity>{$ctx:returnConsumedCapacity}</returnConsumedCapacity>
</amazondynamodb.batchGetItem>
```

**Properties**
* requestItems: Required - A map of one or more table names and, for each table, the corresponding primary keys for the items to retrieve. Each table name can be invoked only once. Each element in the map consists of the following:
  * Keys  - Required - An array of primary key attribute values that define specific items in the table. For each primary key, you must provide all of the key attributes. For example, with a hash type primary key, you only need to specify the hash attribute. For a hash-and-range type primary key, you must specify both the hash attribute and the range attribute.
  * AttributesToGet  - Optional - One or more attributes to be retrieved from the table. By default, all attributes are returned. If a specified attribute is not found, it does not appear in the result. Note that  AttributesToGet  has no effect on provisioned throughput consumption. DynamoDB determines capacity units consumed based on the item size, not on the amount of data that is returned to an application.
  * ConsistentRead  - Optional - If true, a strongly consistent read is used; if false (the default), an eventually consistent read is used.
  * ExpressionAttributeNames  - Optional - One or more substitution tokens for attribute names in the ProjectionExpression property.
  * ProjectionExpression  - Optional - A string that identifies one or more attributes to retrieve from the table. These attributes can include scalars, sets, or elements of a JSON document. The attributes in the expression must be separated by commas. If attribute names are not specified, then all attributes are returned. If any of the specified attributes are not found, they do not appear in the result.
  
* returnConsumedCapacity: Optional - Determines the level of detail about provisioned throughput consumption that is returned in the response. If set to TOTAL, the response includes the consumed capacity for tables and indexes.
  If set to INDEXES, the response includes the consumed capacity for indexes. If set to NONE (the default), the consumed capacity is not included in the response.

**Sample request**

Following is a sample request that can be handled by the batchGetItem operation.

```json
{
    "accessKeyId":"AKIAxxxxxxxxxxxx",
    "secretAccessKey":"id4qxxxxxxxx",
    "region":"us-east-1",
    "blocking":"false",
    "requestItems": {
        "Thread": {
        "Keys": [
            {
                "ForumName": {
                    "S": "Amazon Dynamo"
                },
                "Subject": {
                    "S": "How do I update multiple items?"
                }
            }
        ],
        "AttributesToGet": [
            "Tags",
            "Message"
        ]
    }
    },
"returnConsumedCapacity":"TOTAL"
}
```

**Sample response**

Given below is a sample response for the batchGetItem operation.

```json
{
   "Responses":{
      "Test4782":[
         {
            "Message":{
               "S":"I want to update multiple items in a single call. What's the best way to do that?"
            },
            "Tags":{
               "SS":[
                  "HelpMe",
                  "Multiple Items",
                  "Update"
               ]
            }
         }
      ]
   },
   "UnprocessedKeys":{

   }
}
```
**Related Amazon DynamoDB documentation**
http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_BatchGetItem.html

#### Writing a batch item

The batchWriteItem operation puts or deletes multiple items in one or more tables. A single call to batchWriteItem can write up to 16 MB of data, which can comprise as many as 25 put or delete requests. Individual items to be written can be as large as 400 KB. This operation cannot update items. 

The individual PutRequest and DeleteRequest operations specified in batchWriteItem are atomic, but batchWriteItem as a whole is not. If any requested operations fail because the table's provisioned throughput is exceeded or an internal processing failure occurs, the failed operations are returned in the UnprocessedItems response property. You can investigate and optionally resend the requests. Typically, you would call batchWriteItem in a loop. Each iteration would check for unprocessed items and submit a new batchWriteItem request with those unprocessed items until all items have been processed.

Note that if none of the items can be processed due to insufficient provisioned throughput on all of the tables in the request, batchWriteItem will throw an exception. With batchWriteItem, you can efficiently write or delete large amounts of data, such as from Amazon Elastic MapReduce (EMR), or copy data from another database into DynamoDB. To improve performance with these large-scale operations, batchWriteItem does not behave in the same way as individual PutRequest and DeleteRequest calls would. For example, you cannot specify conditions on individual put and delete requests, and batchWriteItem does not return deleted items in the response.

If one or more of the following is true, DynamoDB rejects the entire batch write operation:

  * One or more tables specified in the batchWriteItem request does not exist.
  * Primary key attributes specified on an item in the request do not match those in the corresponding table's primary key schema.
  * You try to perform multiple operations on the same item in the same batchWriteItem request. For example, you cannot put and delete the same item in the same batchWriteItem request.
  * There are more than 25 requests in the batch.
  * The total request size exceeds 16 MB.
  * Any individual item in a batch exceeds 400 KB.
  
**batchWriteItem**
```xml
<amazondynamodb.batchWriteItem>
    <returnItemCollectionMetrics>{$ctx:returnItemCollectionMetrics}</returnItemCollectionMetrics>
    <requestItems>{$ctx:requestItems}</requestItems>
    <returnConsumedCapacity>{$ctx:returnConsumedCapacity}</returnConsumedCapacity>
</amazondynamodb.batchWriteItem>
```
**Properties**
* returnItemCollectionMetrics: Optional -Determines whether item collection metrics are returned. If set to SIZE, statistics on item collection, if any, that were modified during the operation are returned in the response. If set to NONE (the default), no statistics are returned.
* requestItems: Required - A map of one or more table names and, for each table, a list of operations to be performed (DeleteRequest or PutRequest). Each element in the map consists of the following:
  * [DeleteRequest](https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_DeleteRequest.html) - Delete the specified item. The item to be deleted is identified by a Key subelement:
    * Key - Required - A map of primary key attribute values that uniquely identify the item to be deleted. Each entry in this map consists of an attribute name and an attribute value. For each primary key, you must provide all of the key attributes. For example, with a hash type primary key, you only need to specify the hash attribute. For a hash-and-range type primary key, you must specify both the hash attribute and the range attribute.
  * [PutRequest](https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_PutRequest.html) - Put the specified item. The item to be put is identified by an Item subelement:
    * Item - A map of attributes and their values. Each entry in this map consists of an attribute name and an attribute value. Attribute values must not be null, string and binary type attributes must have lengths greater than zero, and set type attributes must not be empty. Requests that contain empty values will be rejected with a ValidationException. If you specify any attributes that are part of an index key, the data types for those attributes must match those of the schema in the table's attribute definition.
* returnConsumedCapacity: Optional - Determines the level of detail about provisioned throughput consumption that is returned in the response: If set to TOTAL, the response includes the consumed capacity for tables and indexes. If set to INDEXES, the response includes the consumed capacity for indexes. If set to NONE (the default), the consumed capacity is not included in the response. 

**Sample request**

Following is a sample request that can be handled by the batchWriteItem operation.

```json
{
    "accessKeyId":"AKIAxxxxxxxxxxxx",
    "secretAccessKey":"id4qxxxxxxxx",
    "region":"us-east-1",
    "blocking":"false",
    "returnConsumedCapacity":"TOTAL",
    "returnItemCollectionMetrics":"SIZE",
    "requestItems": {
        "Forum": [
            {
                "PutRequest": {
                    "Item": {
                        "Name": {
                            "S": "Amazon DynamoDB"
                        },
                        "Category": {
                            "S": "Amazon Web Services"
                        }
                    }
                }
            }
        ]
    }
}
```

**Sample response**

Given below is a sample response for the batchWriteItem operation.

```json
{
   "UnprocessedItems":{
      "Forum":[
         {
            "PutRequest":{
               "Item":{
                  "Name":{
                     "S":"Amazon ElastiCache"
                  },
                  "Category":{
                     "S":"Amazon Web Services"
                  }
               }
            }
         }
      ]
   },
   "ConsumedCapacity":[
      {
         "TableName":"Forum",
         "CapacityUnits":3
      }
   ]
}
```
**Related Amazon DynamoDB documentation**
http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_BatchWriteItem.html

#### Deleting an item

The deleteItem operation deletes a single item in a table by primary key. You can perform a conditional delete operation that deletes the item if it exists, or if it has an expected attribute value. In addition to deleting an item, you can also return the item's attribute values in the same operation, using the returnValues property. Unless you specify conditions, deleteItem is an idempotent operation, and running it multiple times on the same item or attribute does not result in an error response. Conditional deletes are useful for only deleting items if specific conditions are met. If those conditions are met, DynamoDB performs the delete operation. Otherwise, the item is not deleted.

**deleteItem**
```xml
<amazondynamodb.deleteItem>
    <expected>{$ctx:expected}</expected>
    <tableName>{$ctx:tableName}</tableName>
    <returnValues>{$ctx:returnValues}</returnValues>
    <returnItemCollectionMetrics>{$ctx:returnItemCollectionMetrics}</returnItemCollectionMetrics>
    <conditionalOperator>{$ctx:conditionalOperator}</conditionalOperator>
    <conditionExpression>{$ctx:conditionExpression}</conditionExpression>
    <expressionAttributeNames>{$ctx:expressionAttributeNames}</expressionAttributeNames>
    <expressionAttributeValues>{$ctx:expressionAttributeValues}</expressionAttributeValues>
    <returnConsumedCapacity>{$ctx:returnConsumedCapacity}</returnConsumedCapacity>
    <key>{$ctx:key}</key>
</amazondynamodb.deleteItem> 
```

**Properties**
* expected: Optional - A map of attribute/condition pairs. This is the conditional block for the deleteItem operation. Each element of this property consists of an attribute name, a comparison operator, and one or more values. DynamoDB uses the comparison operator to compare the attribute with the value(s) you supply. For each element of this property, the result of the evaluation is either true or false. (For example, {"ForumName":{"ComparisonOperator": "EQ",  "AttributeValueList": [ {"S":"Amazon DynamoDB" }]}})
* Required - The name of the table from which to delete the item. (Minimum length of 3. Maximum length of 255.)
* returnValues: Optional - Use returnValues if you want to get the item attributes as they appeared before they were deleted. For deleteItem, the valid values are:
  * NONE - If returnValues is not specified or if its value is NONE (the default), nothing is returned.
  * ALL_OLD - The content of the old item is returned.
  Valid Values: NONE | ALL_OLD | UPDATED_OLD | ALL_NEW | UPDATED_NEW. 
* returnItemCollectionMetrics: Optional - Determines whether item collection metrics are returned: If set to SIZE, statistics about item collection, if any, that were modified during the operation are returned in the response. If set to NONE (the default), no statistics are returned.
* conditionalOperator: Optional - A logical operator to apply to the conditions in the expected map:
  * AND - If all of the conditions evaluate to true, the entire map evaluates to true (default).
  * OR - If at least one of the conditions evaluate to true, the entire map evaluates to true.
    The operation will succeed only if the entire map evaluates to true.
* key: Required - A map of attribute names to AttributeValue  objects, representing the primary key of the item to delete. For the primary key, you must provide all of the attributes. For example, with a hash type primary key, you only need to specify the hash attribute. For a hash-and-range type primary key, you must specify both the hash attribute and the range attribute.
* conditionExpression: Optional - A condition that must be satisfied in order for a conditional deleteItem operation to succeed. An expression can contain any of the following:
  * Functions: attribute_exists | attribute_not_exists | attribute_type | contains | begins_with | size 
    These function names are case-sensitive.
  * Comparison operators: = | <> | < | > | <= | >= | BETWEEN | IN
  * Logical operators: AND | OR | NOT
* expressionAttributeNames: Optional - One or more substitution tokens for attribute names in an expression. (For example, {"#LP":"LastPostDateTime"})
* expressionAttributeValues: Optional - One or more values that can be substituted in an expression. (For example, { ":avail":{"S":"Available"}, ":back":{"S":"Backordered"}, ":disc":{"S":"Discontinued"} })
* returnConsumedCapacity: Optional - Determines the level of detail about provisioned throughput consumption that is returned in the response: If set to TOTAL, the response includes the consumed capacity for tables and indexes. If set to INDEXES, the response includes consumed capacity for indexes. If set to NONE (the default), consumed capacity is not included in the response.

**Sample request**

Following is a sample request that can be handled by the deleteItem operation.

```json
{
    "accessKeyId":"AKIxxxxxxxxxx",
    "secretAccessKey":"id4xxxxxx",
    "region":"us-east-1",
    "blocking":"false",
    "tableName": "Thread",
    "key": {
        "ForumName": {
            "S": "Amazon DynamoDB"
        },
        "Subject": {
            "S": "How do I update multiple items?"
        }
    },
    "conditionExpression":"attribute_not_exists",
    "returnValues": "ALL_OLD",
    "returnConsumedCapacity":"TOTAL"
} 
```

**Sample response**

Given below is a sample response for the deleteItem operation.

```json
{
   "Attributes":{
      "LastPostedBy":{
         "S":"fred@example.com"
      },
      "ForumName":{
         "S":"Amazon DynamoDB"
      },
      "LastPostDateTime":{
         "S":"201303201023"
      },
      "Tags":{
         "SS":[
            "Update",
            "Multiple Items",
            "HelpMe"
         ]
      },
      "Subject":{
         "S":"How do I update multiple items?"
      },
      "Message":{
         "S":"I want to update multiple items in a single call. What's the best way to do that?"
      }
   }
}
```
**Related Amazon DynamoDB documentation**
http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_DeleteItem.html

#### Retrieving an item

The getItem operation returns a set of attributes for the item with the given primary key. If there is no matching item, getItem does not return any data. This operation provides an eventually consistent read by default. If your application requires a strongly consistent read, set  consistentRead  to true. Although a strongly consistent read might take more time than an eventually consistent read, it always returns the last updated value.

**getItem**
```xml
<amazondynamodb.getItem>
    <attributesToGet>{$ctx:attributesToGet}</attributesToGet>
    <tableName>{$ctx:tableName}</tableName>
    <consistentRead>{$ctx:consistentRead}</consistentRead>
    <key>{$ctx:key}</key>
    <expressionAttributeNames>{$ctx:expressionAttributeNames}</expressionAttributeNames>
    <projectionExpression>{$ctx:projectionExpression}</projectionExpression>
    <returnConsumedCapacity>{$ctx:returnConsumedCapacity}</returnConsumedCapacity> 
</amazondynamodb.getItem> 
```

**Properties**
* attributesToGet: Optional - The names of one or more attributes to retrieve. If no attribute names are specified, all attributes will be returned. If any of the requested attributes are not found, they will not appear in the result. Note that attributesToGet has no effect on provisioned throughput consumption. DynamoDB determines capacity units consumed based on item size, not on the amount of data that is returned to an application. (For example, ["ForumName", "Subject"])
* tableName: Required - The name of the table containing the requested item. (Minimum length of 3. Maximum length of 255.)
* consistentRead: Optional - Determines the read consistency model. If set to true, the operation uses strongly consistent reads. Otherwise, the operation uses eventually consistent reads.
* key: Required - A map of attribute names to AttributeValue objects, representing the primary key of the item to retrieve. For the primary key, you must provide all of the attributes. For example, with a hash type primary key, you only need to specify the hash attribute. For a hash-and-range type primary key, you must specify both the hash attribute and the range attribute.
* expressionAttributeNames: Optional - One or more substitution tokens for attribute names in an expression. (For example, {"#LP":"LastPostDateTime"})
* projectionExpression: Optional - A string that identifies one or more attributes to retrieve from the table. These attributes can include scalars, sets, or elements of a JSON document. The attributes in the expression must be separated by commas. If attribute names are not specified, then all attributes are returned. If any of the specified attributes are not found, they do not appear in the result.
* returnConsumedCapacity:  Optional - Determines the level of detail about provisioned throughput consumption that is returned in the response: If set to TOTAL, the response includes the consumed capacity data for tables and indexes. If set to INDEXES, the response includes consumed capacity for indexes. If set to NONE (the default), consumed capacity is not included in the response.

**Sample request**

Following is a sample request that can be handled by the getItem operation.

```json
{
    "accessKeyId":"AKIxxxxxxxxxx",
    "secretAccessKey":"id4xxxxxx",
    "region":"us-east-1",
    "blocking":"false",
    "tableName": "Thread",
    "key": {
        "ForumName": {
            "S": "Amazon DynamoDB"
        },
        "Subject": {
            "S": "How do I update multiple items?"
        }
    },
    "projectionExpression":"#LP, Message, Tags",
    "consistentRead": true,
    "returnConsumedCapacity": "TOTAL",
    "expressionAttributeNames":{"#LP":"LastPostDateTime"}
} 
```

**Sample response**

Given below is a sample response for the getItem operation.

```json
{
   "ConsumedCapacity":{
      "CapacityUnits":1,
      "TableName":"Thread"
   },
   "Item":{
      "Tags":{
         "SS":[
            "Update",
            "Multiple Items",
            "HelpMe"
         ]
      },
      "LastPostDateTime":{
         "S":"201303190436"
      },
      "Message":{
         "S":"I want to update multiple items in a single call. What's the best way to do that?"
      }
   }
}
```
**Related Amazon DynamoDB documentation**
http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_GetItem.html

#### Creating an item

The putItem operation creates a new item, or replaces an old item with a new item. If an item already exists in the specified table with the same primary key, the new item completely replaces the existing item. You can perform a conditional put (insert a new item if one with the specified primary key does not exist), or replace an existing item if it has certain attribute values.

In addition to creating an item, you can also return the attribute values of the item in the same operation using the returnValues property. When you add an item, the primary key attribute(s) are the only required attributes. Attribute values cannot be null. String and binary type attributes must have a length greater than zero. Set type attributes cannot be empty. Requests with empty values will be rejected with a validation exception. You can request that the putItem operation should return either a copy of the old item (before the update) or a copy of the new item (after the update). 

To prevent a new item from replacing an existing item, use a conditional expression with the putItem operation. The conditional expression should contain the attribute_not_exists function with the name of the attribute being used as the partition key for the table. Since every record must contain that attribute, the attribute_not_exists function will only succeed if no matching item exists. For more information about using this API, see Working with Items.

**putItem**
```xml
<amazondynamodb.putItem>
    <expected>{$ctx:expected}</expected>
    <tableName>{$ctx:tableName}</tableName>
    <item>{$ctx:item}</item>
    <returnValues>{$ctx:returnValues}</returnValues>
    <returnItemCollectionMetrics>{$ctx:returnItemCollectionMetrics}</returnItemCollectionMetrics>
    <conditionalOperator>{$ctx:conditionalOperator}</conditionalOperator>
    <conditionExpression>{$ctx:conditionExpression}</conditionExpression>
    <expressionAttributeNames>{$ctx:expressionAttributeNames}</expressionAttributeNames>
    <expressionAttributeValues>{$ctx:expressionAttributeValues}</expressionAttributeValues>
    <returnConsumedCapacity>{$ctx:returnConsumedCapacity}</returnConsumedCapacity>
</amazondynamodb.putItem>
```

**Properties**
* expected: Optional - A map of attribute/condition pairs. This is the conditional block for the putItem operation. Each element of expected consists of an attribute name, a comparison operator, and one or more values. DynamoDB uses the comparison operator to compare the attribute with the value(s) you supplied. For each expected element, the result of the evaluation is either true or false.
* tableName: Required - The name of the table to contain the item. (Minimum length of 3. Maximum length of 255.)
* item: Required - A map of attribute name/value pairs, one for each attribute. Only the primary key attributes are required, but you can optionally provide other attribute name-value pairs for the item. You must provide all of the attributes for the [primary key](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.html#DataModelPrimaryKey). For example, with a hash type primary key, you only need to specify the hash attribute. For a hash-and-range type primary key, you must specify  both  the hash attribute and the range attribute. If you specify any attributes that are part of an index key, the data types for those attributes must match those of the schema in the table's attribute definition. Each element in the item map is an [AttributeValue](https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_AttributeValue.html) object.
* returnValues: Optional - Use returnValues if you want to get the item attributes as they appeared before they were updated with the putItem request. The possible values are:
  * NONE - If returnValues is not specified, or if its value is NONE (the default), nothing is returned.
  * ALL_OLD - If putItem overwrote an attribute name-value pair, the content of the old item is returned.
  Valid values: NONE | ALL_OLD | UPDATED_OLD | ALL_NEW | UPDATED_NEW
* returnItemCollectionMetrics: Optional - Determines whether item collection metrics are returned: If set to SIZE, statistics about item collection, if any, that were modified during the operation are returned in the response. If set to NONE (the default), no statistics are returned.
* conditionalOperator: Optional - A logical operator to apply to the conditions in the expected map:
  * AND - If all of the conditions evaluate to true, the entire map evaluates to true (default).
  * OR - If at least one of the conditions evaluate to true, the entire map evaluates to true.
    The operation will succeed only if the entire map evaluates to true.
* key: Required - A map of attribute names to AttributeValue  objects, representing the primary key of the item to delete. For the primary key, you must provide all of the attributes. For example, with a hash type primary key, you only need to specify the hash attribute. For a hash-and-range type primary key, you must specify both the hash attribute and the range attribute.
* conditionExpression: Optional - A condition that must be satisfied in order for a conditional deleteItem operation to succeed. An expression can contain any of the following:
  * Functions: attribute_exists | attribute_not_exists | attribute_type | contains | begins_with | size 
    These function names are case-sensitive.
  * Comparison operators: = | <> | < | > | <= | >= | BETWEEN | IN
  * Logical operators: AND | OR | NOT
* expressionAttributeNames: Optional - One or more substitution tokens for attribute names in an expression. (For example, {"#LP":"LastPostDateTime"})
* expressionAttributeValues: Optional - One or more values that can be substituted in an expression. (For example, { ":avail":{"S":"Available"}, ":back":{"S":"Backordered"}, ":disc":{"S":"Discontinued"} })
* returnConsumedCapacity: Optional - Determines the level of detail about provisioned throughput consumption that is returned in the response: If set to TOTAL, the response includes the consumed capacity for tables and indexes. If set to INDEXES, the response includes consumed capacity for indexes. If set to NONE (the default), consumed capacity is not included in the response.

**Sample request**

Following is a sample request that can be handled by the putItem operation.

```json
{
    "accessKeyId":"AKIxxxxxxxxxx",
    "secretAccessKey":"id4xxxxxx",
    "region":"us-east-1",
    "blocking":"false",
    "tableName": "Thread",
    "item": {
        "LastPostDateTime": {
            "S": "201303190422"
        },
        "Tags": {
            "SS": ["Update","Multiple Items","HelpMe"]
        },
        "ForumName": {
            "S": "Amazon Dynamo"
        },
        "Message": {
            "S": "I want to update multiple items in a single call. What's the best way to do that?"
        },
        "Subject": {
            "S": "How do I update multiple items?"
        },
        "LastPostedBy": {
            "S": "fred@example.com"
        }
    },
    "returnValues":"ALL_OLD",
    "returnConsumedCapacity":"TOTAL",
    "returnItemCollectionMetrics":"SIZE",
    "expected":{
        "Message":{
            "ComparisonOperator": "EQ",
             "AttributeValueList": [ {"S":"I want to update multiple item." }]
        }
         
    }
} 
```

**Sample response**

Given below is a sample response for the putItem operation.

```json
{
}
```
**Related Amazon DynamoDB documentation**
http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_PutItem.html

#### Querying

The query operation uses the primary key of a table or a secondary index, to directly access items from that table or index. You can use the KeyConditionExpression property to provide a specific value for the partition key, and the query operation returns all of the items from the table or index with that partition key value. Optionally, you can narrow the scope of the query operation by specifying a sort key value and a comparison operator in KeyConditionExpression. You can use the ScanIndexForward property to get results in forward or reverse order, by sort key.

Queries that do not return results consume the minimum read capacity units according to the type of read. If the total number of items meeting the query criteria exceeds the result set size limit of 1 MB, the query stops and results are returned to the user with a  LastEvaluatedKey  to continue the query in a subsequent operation. Unlike a scan operation, a query operation never returns an empty result set  and  a LastEvaluatedKey . The  LastEvaluatedKey is only provided if the results exceed 1 MB, or if you have used limit.

You can query a table, a local secondary index, or a global secondary index. For a query on a table or on a local secondary index, you can set consistentRead to true and obtain a strongly consistent result. Global secondary indexes support eventually consistent reads only, so do not specify consistentRead when querying a global secondary index.

**query**
```xml
<amazondynamodb.query>
    <limit>{$ctx:limit}</limit>
    <exclusiveStartKey>{$ctx:exclusiveStartKey}</exclusiveStartKey>
    <keyConditions>{$ctx:keyConditions}</keyConditions>
    <attributesToGet>{$ctx:attributesToGet}</attributesToGet>
    <tableName>{$ctx:tableName}</tableName>
    <select>{$ctx:select}</select>
    <scanIndexForward>{$ctx:scanIndexForward}</scanIndexForward>
    <queryFilter>{$ctx:queryFilter}</queryFilter>
    <consistentRead>{$ctx:consistentRead}</consistentRead>
    <indexName>{$ctx:indexName}</indexName>
    <conditionalOperator>{$ctx:conditionalOperator}</conditionalOperator>
    <expressionAttributeNames>{$ctx:expressionAttributeNames}</expressionAttributeNames>
    <expressionAttributeValues>{$ctx:expressionAttributeValues}</expressionAttributeValues>
    <filterExpression>{$ctx:filterExpression}</filterExpression>
    <keyConditionExpression>{$ctx:keyConditionExpression}</keyConditionExpression>
    <projectionExpression>{$ctx:projectionExpression}</projectionExpression>
    <returnConsumedCapacity>{$ctx:returnConsumedCapacity}</returnConsumedCapacity>
</amazondynamodb.query>
```

**Properties**
* limit: Optional - The maximum number of items to evaluate (not necessarily the number of matching items). If DynamoDB processes the number of items up to the limit while processing the results, it stops the operation and returns the matching values up to that point, and a LastEvaluatedKey to apply in a subsequent operation, so that you can pick up from where you left off. Also, if the processed data set size exceeds 1 MB before DynamoDB reaches this limit, it stops the operation and returns the matching values up to the limit, and a LastEvaluatedKey to apply in a subsequent operation to continue the operation. For more information, see [Query and Scan](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Query.html) in the Amazon DynamoDB Developer Guide.
* [exclusiveStartKey](https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_AttributeValue.html): Optional - The primary key of the first item that this operation will evaluate. Use the value that was returned for LastEvaluatedKey in the previous operation. The data type for exclusiveStartKey must be String, Number or Binary. No set data types are allowed.
* [keyConditions](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LegacyConditionalParameters.KeyConditions.html): Optional - The selection criteria for the query. (For example, "SongTitle": {ComparisonOperator: "BETWEEN", AttributeValueList: ["A", "M"]})
* [attributesToGet](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LegacyConditionalParameters.AttributesToGet.html): Optional - The names of one or more attributes to retrieve. If no attribute names are specified, all attributes will be returned. If any of the requested attributes are not found, they will not appear in the result. Note that attributesToGet has no effect on provisioned throughput consumption. DynamoDB determines capacity units consumed based on item size, not on the amount of data that is returned to an application. (For example, ["ForumName", "Subject"])
  You cannot use both attributesToGet and select (see below) together in a query request, unless the value for select is SPECIFIC_ATTRIBUTES. (This usage is equivalent to specifying attributesToGet without any value for select.) If you are querying a local secondary index and request only attributes that are projected into that index, the operation will read only the index and not the table. If any of the requested attributes are not projected into the local secondary index, DynamoDB fetches each of these attributes from the parent table. If you are querying a global secondary index, you can only request attributes that are projected into the index. Global secondary index queries cannot fetch attributes from the parent table. (Minimum of 1 item in the list.)
* tableName: Required - The name of the table containing the requested items. (Minimum length of 3. Maximum length of 255.)
* select: Optional - The attributes to be returned in the result. You can retrieve all item attributes, specific item attributes, the count of matching items, or in the case of an index, some or all of the attributes projected into the index. Set to SPECIFIC_ATTRIBUTES if you are also using attributesToGet (see above). Possible values: ALL_ATTRIBUTES | ALL_PROJECTED_ATTRIBUTES | SPECIFIC_ATTRIBUTES | COUNT
* scanIndexForward: Optional -  Specifies ascending (true) or descending (false) traversal of the index. DynamoDB returns results reflecting the requested order determined by the range key. If the data type is Number, the results are returned in numeric order. For String, the results are returned in the order of ASCII character code values. For Binary, DynamoDB treats each byte of the binary data as unsigned when it compares binary values. Defaults to the ascending order.
* queryFilter: Optional - Evaluates the query results and returns only the desired values. If you specify more than one condition in the queryFilter map, by default all of the conditions must evaluate to true. (You can use the conditionalOperator property described below to OR the conditions instead. If you do this, at least one of the conditions must evaluate to true, rather than all of them.) Each queryFilter element consists of an attribute name to compare, along with the following:
  * AttributeValueList  - One or more values to evaluate against the supplied attribute. The number of values in the list depend on the ComparisonOperator that is used. For the type Number, value comparisons are numeric.  
    String value comparisons for greater than, equals, or less than are based on ASCII character code values. For example, a is greater than A, and aa is greater than B. For Binary, DynamoDB treats each byte of the binary data as unsigned when it compares binary values, for example when evaluating query expressions. 
  * ComparisonOperator  - A comparator for evaluating attributes. For example: equals, greater than, less than, etc.
    The following comparison operators are available:
    EQ | NE | LE | LT | GE | GT | NOT_NULL | NULL | CONTAINS | NOT_CONTAINS | BEGINS_WITH | IN | BETWEEN. For complete descriptions of all comparison operators, see [conditions](https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Condition.html).
    For example,  "LastPostDateTime": {ComparisonOperator: "GT", AttributeValueList: [ 201303190421 ]}
* consistentRead: Optional - Determines the read consistency model. If set to true, the operation uses strongly consistent reads. Otherwise, eventually consistent reads are used. Strongly consistent reads are not supported on global secondary indexes. If you query a global secondary index with consistentRead set to true, you will receive an error message.
* indexName: Optional - The name of an index to query. This can be any local secondary index or global secondary index on the table (Minimum length of 3. Maximum length of 255.)
* conditionalOperator: Optional - A logical operator to apply to the conditions in the queryFilter map:
  * AND - If all of the conditions evaluate to true, the entire map evaluates to true (default).
  * OR - If at least one of the conditions evaluate to true, then the entire map evaluates to true.
  The operation succeeds only if the entire map evaluates to true.
* expressionAttributeNames: Optional - One or more substitution tokens for attribute names in an expression. (For example, {"#LP":"LastPostDateTime"})
* expressionAttributeValues: Optional - One or more values that can be substituted in an expression. (For example, { ":avail":{"S":"Available"}, ":back":{"S":"Backordered"}, ":disc":{"S":"Discontinued"} })
* returnConsumedCapacity: Optional - Determines the level of detail about provisioned throughput consumption that is returned in the response: If set to TOTAL, the response includes the consumed capacity for tables and indexes. If set to INDEXES, the response includes consumed capacity for indexes. If set to NONE (the default), consumed capacity is not included in the response.
* projectionExpression  - Optional - A string that identifies one or more attributes to retrieve from the table. These attributes can include scalars, sets, or elements of a JSON document. The attributes in the expression must be separated by commas. If attribute names are not specified, then all attributes are returned. If any of the specified attributes are not found, those attributes do not appear in the result.
* keyConditionExpression: Optional - The condition that specifies the key value(s) for items to be retrieved by the query operation.
* filterExpression: Optional - A string that contains conditions that DynamoDB applies after the query operation, but before the data is returned. Items that do not satisfy the FilterExpression criteria are not returned. (For example, "LastPostDateTime > :LP")
  For more information, see [Filter Expressions](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Query.html#FilteringResults) in the Amazon DynamoDB Developer Guide. 

**Sample request**

Following is a sample request that can be handled by the query operation.

```json

{
    "accessKeyId":"AKIxxxxxxxxxx",
    "secretAccessKey":"id4xxxxxx",
    "region":"us-east-1",
    "blocking":"false",
    "tableName": "Thread",
    "indexName": "LastPostIndex",
    "limit": 3,
    "consistentRead": true,
    "projectionExpression": "ForumName, #LP",
    "keyConditionExpression": "ForumName = :v1 AND #LP BETWEEN :v2a AND :v2b",
    "expressionAttributeNames":{"#LP":"LastPostDateTime"},
    "expressionAttributeValues": {
        ":v1": {"S": "Amazon Dynamo"},
        ":v2a": {"S": "201303190421"},
        ":v2b": {"S": "201303190425"}
    },
    "returnConsumedCapacity": "TOTAL"
}
```

**Sample response**

Given below is a sample response for the query operation.

```json
{
   "Count":2,
   "ScannedCount":2
} 
```
**Related Amazon DynamoDB documentation**
http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Query.html

#### Scanning an item

The scan operation returns one or more items and item attributes by accessing every item in the table. To have DynamoDB return fewer items, you can provide a scanFilter.

If the total number of scanned items exceeds the maximum data set size limit of 1 MB, the scan stops and results are returned to the user with a LastEvaluatedKey to continue the scan in a subsequent operation. The results also include the number of items exceeding the limit. A scan can result in no table data meeting the filter criteria. The result set is eventually consistent.

By default, scan operations proceed sequentially. For faster performance on large tables, applications can request a parallel scan by specifying the segment and totalSegments properties. For more information, see [Parallel Scan](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Query.html#QueryAndScanParallelScan).

**scan**
```xml
<amazondynamodb.scan>
    <limit>{$ctx:limit}</limit>
    <totalSegments>{$ctx:totalSegments}</totalSegments>
    <exclusiveStartKey>{$ctx:exclusiveStartKey}</exclusiveStartKey>
    <attributesToGet>{$ctx:attributesToGet}</attributesToGet>
    <select>{$ctx:select}</select>
    <segment>{$ctx:segment}</segment>
    <tableName>{$ctx:tableName}</tableName>
    <scanFilter>{$ctx:scanFilter}</scanFilter>
    <conditionalOperator>{$ctx:conditionalOperator}</conditionalOperator>
    <consistentRead>{$ctx:consistentRead}</consistentRead>
    <expressionAttributeNames>{$ctx:expressionAttributeNames}</expressionAttributeNames>
    <expressionAttributeValues>{$ctx:expressionAttributeValues}</expressionAttributeValues>
    <filterExpression>{$ctx:filterExpression}</filterExpression>
    <indexName>{$ctx:indexName}</indexName>
    <projectionExpression>{$ctx:projectionExpression}</projectionExpression>
    <returnConsumedCapacity>{$ctx:returnConsumedCapacity}</returnConsumedCapacity>
</amazondynamodb.scan>  
```

**Properties**
* limit: Optional - The maximum number of items to evaluate (not necessarily the number of matching items). If DynamoDB processes the number of items up to the limit while processing the results, it stops the operation and returns the matching values up to that point, and a LastEvaluatedKey to apply in a subsequent operation, so that you can pick up from where you left off. Also, if the processed data set size exceeds 1 MB before DynamoDB reaches this limit, it stops the operation and returns the matching values up to the limit, and a LastEvaluatedKey to apply in a subsequent operation to continue the operation. For more information, see [Query and Scan](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Query.html) in the Amazon DynamoDB Developer Guide.
* totalSegments: Optional - For a parallel scan request, totalSegments represents the total number of segments into which the scan operation will be divided. The value of totalSegments  corresponds to the number of application workers that will perform the parallel scan. For example, if you want to scan a table using four application threads, you would specify a totalSegments value of 4. The value for totalSegments must be greater than or equal to 1, and less than or equal to 1000000. If you specify a totalSegments value of 1, the scan will be sequential rather than parallel. If you specify totalSegments, you must also specify segment.
* exclusiveStartKey: Optional - The primary key of the first item that this operation will evaluate. Use the value that was returned for LastEvaluatedKey in the previous operation. The data type for exclusiveStartKey must be String, Number or Binary. No set data types are allowed. In a parallel scan, a scan request that includes exclusiveStartKey must specify the same segment whose previous scan returned the corresponding value of LastEvaluatedKey. 
* [attributesToGet](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LegacyConditionalParameters.AttributesToGet.html): Optional - The names of one or more attributes to retrieve. If no attribute names are specified, all attributes will be returned. If any of the requested attributes are not found, they will not appear in the result. Note that attributesToGet has no effect on provisioned throughput consumption. DynamoDB determines capacity units consumed based on item size, not on the amount of data that is returned to an application. (For example, ["ForumName", "Subject"])
* select: Optional - The attributes to be returned in the result. You can retrieve all item attributes, specific item attributes, or the count of matching items.
  * ALL_ATTRIBUTES: Returns all of the item attributes.
  * COUNT: Returns the number of matching items, rather than the matching items themselves.
  * SPECIFIC_ATTRIBUTES: Returns only the attributes listed in attributesToGet. This is equivalent to specifying attributesToGet without specifying any value for select.
  If neither select nor attributesToGet is specified, DynamoDB defaults to ALL_ATTRIBUTES. You cannot use both select and attributesToGet together in a single request unless  the value for select is SPECIFIC_ATTRIBUTES. (This usage is equivalent to specifying attributesToGet without any value for select.) Possible values: ALL_ATTRIBUTES | ALL_PROJECTED_ATTRIBUTES | SPECIFIC_ATTRIBUTES | COUNT.
* segment: Optional - For a parallel scan request, segment identifies an individual segment to be scanned by an application worker. Segment IDs are zero-based, so the first segment is always 0. For example, if you want to scan a table using four application threads, the first thread would specify a segment value of 0, the second thread would specify 1, and so on. The value of LastEvaluatedKey returned from a parallel scan request must be used as exclusiveStartKey with the same Segment ID in a subsequent scan operation. The value for segment must be greater than or equal to 0, and less than the value provided for totalSegments. If you specify segment, you must also specify totalSegments.
* tableName: Required - The name of the table containing the requested items.
* scanFilter: Optional - Evaluates the scan results and returns only the desired values. If you specify more than one condition in the scanFilter map, by default all of the conditions must evaluate to true. In other words, the conditions are ANDed together. (You can use the conditionalOperator property to OR the [conditions](https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Condition.html) instead. If you do this, at least one of the conditions must evaluate to true, rather than all of them.)
* conditionalOperator: Optional - A logical operator to apply to the conditions in the scanFilter map:
  * AND - If  all  of the conditions evaluate to true, the entire map evaluates to true (default).
  * OR - If  at least one  of the conditions evaluates to true, the entire map evaluates to true.
  The operation will succeed only if the entire map evaluates to true.
* consistentRead: Optional - A Boolean value that determines the read consistency model during the scan.  If set to true, the response will contain all of the write operations that completed before the scan operation. Otherwise, the data returned might not contain the results from the recently completed write operations (PutItem, UpdateItem or DeleteItem). Strongly consistent reads are not supported on global secondary indexes. If you query a global secondary index with consistentRead set to true, you will receive an error message.
* expressionAttributeNames: Optional - One or more substitution tokens for attribute names in an expression. (For example, {"#LP":"LastPostDateTime"})
* expressionAttributeValues: Optional - One or more values that can be substituted in an expression. (For example, { ":avail":{"S":"Available"}, ":back":{"S":"Backordered"}, ":disc":{"S":"Discontinued"} })
* returnConsumedCapacity: Optional - Determines the level of detail about provisioned throughput consumption that is returned in the response. If set to TOTAL, the response includes the consumed capacity for tables and indexes. If set to INDEXES, the response includes consumed capacity for indexes. If set to NONE (the default), the consumed capacity is not included in the response.
* projectionExpression  - Optional - A string that identifies one or more attributes to retrieve from the table. These attributes can include scalars, sets, or elements of a JSON document. The attributes in the expression must be separated by commas. If attribute names are not specified, then all attributes are returned. If any of the specified attributes are not found, they do not appear in the result.
* indexName: Optional - The name of an index to scan. This can be any local secondary index or global secondary index on the table (Minimum length of 3. Maximum length of 255.)
* filterExpression: Optional - A string that contains conditions that DynamoDB applies after the scan operation, but before the data is returned to you. Items that do not satisfy the FilterExpression criteria are not returned. For more information, see [Filter Expressions](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Query.html#FilteringResults) in the Amazon DynamoDB Developer Guide.

**Sample request**

Following is a sample request that can be handled by the scan operation.

```json
{
    "accessKeyId":"AKIxxxxxxxxxx",
    "secretAccessKey":"id4xxxxxx",
    "region":"us-east-1",
    "blocking":"false",
    "tableName": "Thread",
    "expressionAttributeNames":{"#LP":"LastPostDateTime"},
    "filterExpression": "#LP = :val",
    "expressionAttributeValues": {":val": {"S": "201303190422"}},
    "returnConsumedCapacity": "TOTAL"
}
```

**Sample response**

Given below is a sample response for the scan operation.

```json
{
   "ConsumedCapacity":{
      "CapacityUnits":0.5,
      "TableName":"Reply"
   },
   "Count":4,
   "Items":[
      {
         "PostedBy":{
            "S":"joe@example.com"
         },
         "ReplyDateTime":{
            "S":"20130320115336"
         },
         "Id":{
            "S":"Amazon DynamoDB#How do I update multiple items?"
         },
         "Message":{
            "S":"Have you looked at BatchWriteItem?"
         }
      },
      {
         "PostedBy":{
            "S":"fred@example.com"
         },
         "ReplyDateTime":{
            "S":"20130320115342"
         },
         "Id":{
            "S":"Amazon DynamoDB#How do I update multiple items?"
         },
         "Message":{
            "S":"No, I didn't know about that.  Where can I find more information?"
         }
      },
      {
         "PostedBy":{
            "S":"joe@example.com"
         },
         "ReplyDateTime":{
            "S":"20130320115347"
         },
         "Id":{
            "S":"Amazon DynamoDB#How do I update multiple items?"
         },
         "Message":{
            "S":"BatchWriteItem is documented in the Amazon DynamoDB API Reference."
         }
      },
      {
         "PostedBy":{
            "S":"fred@example.com"
         },
         "ReplyDateTime":{
            "S":"20130320115352"
         },
         "Id":{
            "S":"Amazon DynamoDB#How do I update multiple items?"
         },
         "Message":{
            "S":"OK, I'll take a look at that.  Thanks!"
         }
      }
   ],
   "ScannedCount":4
}
```
**Related Amazon DynamoDB documentation**
https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Scan.html

#### Updating an item

The updateItem operation edits an existing item's attributes, or inserts a new item if it does not already exist. You can put, delete, or add attribute values. You can also perform a conditional update (insert a new attribute name-value pair if it doesn't exist, or replace an existing name-value pair if it has certain expected attribute values). In addition to updating an item, you can also return the item's attribute values in the same operation using the returnValues property.

**updateItem**
```xml
<amazondynamodb.updateItem>
    <expected>{$ctx:expected}</expected>
    <tableName>{$ctx:tableName}</tableName>
    <returnValues>{$ctx:returnValues}</returnValues>
    <returnItemCollectionMetrics>{$ctx:returnItemCollectionMetrics}</returnItemCollectionMetrics>
    <conditionalOperator>{$ctx:conditionalOperator}</conditionalOperator>
    <attributeUpdates>{$ctx:attributeUpdates}</attributeUpdates>
    <key>{$ctx:key}</key>
    <conditionExpression>{$ctx:conditionExpression}</conditionExpression>
    <expressionAttributeNames>{$ctx:expressionAttributeNames}</expressionAttributeNames>
    <expressionAttributeValues>{$ctx:expressionAttributeValues}</expressionAttributeValues>
    <returnConsumedCapacity>{$ctx:returnConsumedCapacity}</returnConsumedCapacity>
    <updateExpression>{$ctx:updateExpression}</updateExpression>
</amazondynamodb.updateItem> 
```

**Properties**
* expected: Optional - A map of attribute/condition pairs. This is the conditional block for the updateItem operation. Each element of expected consists of an attribute name, a comparison operator, and one or more values. DynamoDB uses the comparison operator to compare the attribute with the value(s) you supplied. For each expected element, the result of the evaluation is either true or false.
  If you specify more than one element in the expected map, by default all of the conditions must evaluate to true. In other words, the conditions are ANDed together. (You can use the  ConditionalOperator  property to OR the conditions instead. If you do this, at least one of the conditions must evaluate to true, rather than all of them.) If the expected map evaluates to true, the conditional operation succeeds. If it evaluates to false, it fails.
* tableName: Required - The name of the table to contain the item. (Minimum length of 3. Maximum length of 255.)
* returnValues: Optional - Use returnValues if you want to get the item attributes as they appeared before they were updated with the putItem request. The possible values are:
  * NONE - If returnValues is not specified, or if its value is NONE (the default), nothing is returned.
  * ALL_OLD - If putItem overwrote an attribute name-value pair, the content of the old item is returned.
  Valid values: NONE | ALL_OLD | UPDATED_OLD | ALL_NEW | UPDATED_NEW
* returnItemCollectionMetrics: Optional - Determines whether item collection metrics are returned: If set to SIZE, statistics about item collection, if any, that were modified during the operation are returned in the response. If set to NONE (the default), no statistics are returned.
* conditionalOperator: Optional - A logical operator to apply to the conditions in the expected map:
  * AND - If all of the conditions evaluate to true, the entire map evaluates to true (default).
  * OR - If at least one of the conditions evaluate to true, the entire map evaluates to true.
    The operation will succeed only if the entire map evaluates to true.
* attributeUpdates: Optional - The names of attributes to be modified, the action to perform on each, and the new value for each. If you are updating an attribute that is an index key attribute for any indexes on that table, the attribute type must match the index key type defined in the AttributesDefinition of the table description. You can use updateItem to update any non-key attributes.
  Attribute values cannot be null. String and binary type attributes must have lengths greater than zero. Set type attributes must not be empty. Requests with empty values will be rejected with a  ValidationException .
* key: Required - A map of attribute names to AttributeValue  objects, representing the primary key of the item to delete. For the primary key, you must provide all of the attributes. For example, with a hash type primary key, you only need to specify the hash attribute. For a hash-and-range type primary key, you must specify both the hash attribute and the range attribute.
* conditionExpression: Optional - A condition that must be satisfied in order for a conditional deleteItem operation to succeed. An expression can contain any of the following:
  * Functions: attribute_exists | attribute_not_exists | attribute_type | contains | begins_with | size 
    These function names are case-sensitive.
  * Comparison operators: = | <> | < | > | <= | >= | BETWEEN | IN
  * Logical operators: AND | OR | NOT
* expressionAttributeNames: Optional - One or more substitution tokens for attribute names in an expression. (For example, {"#LP":"LastPostDateTime"})
* expressionAttributeValues: Optional - One or more values that can be substituted in an expression. (For example, { ":avail":{"S":"Available"}, ":back":{"S":"Backordered"}, ":disc":{"S":"Discontinued"} })
* returnConsumedCapacity: Optional - Determines the level of detail about provisioned throughput consumption that is returned in the response. If set to TOTAL, the response includes the consumed capacity for tables and indexes. If set to INDEXES, the response includes the consumed capacity for indexes. If set to NONE (the default), the consumed capacity is not included in the response.
* updateExpression: Optional - An expression that defines one or more attributes to be updated, the action to be performed on them, and new value(s) for them.
  * SET: Adds one or more attributes and values to an item. If any of these attribute already exist, they are replaced by the new values. You can also use SET to add or subtract from an attribute that is of type Number.
  * REMOVE: Removes one or more attributes from an item.
  * ADD: Adds the specified value to the item, if the attribute does not already exist.
  * DELETE: Deletes an element from a set.
  For more information on update expressions, see Modifying Items and Attributes in the Amazon DynamoDB Developer Guide.

**Sample request**

Following is a sample request that can be handled by the updateItem operation.

```json
{
    "accessKeyId":"AKIxxxxxxxxxx",
    "secretAccessKey":"id4xxxxxx",
    "region":"us-east-1",
    "blocking":"false",
    "tableName": "Thread",
    "key": {
        "ForumName": {
            "S": "Amazon Dynamo"
        },
        "Subject": {
            "S": "How do I update multiple items?"
        }
    },
    "updateExpression": "set LastPostedBy = :val1",
    "conditionExpression": "LastPostedBy = :val2",
    "expressionAttributeValues": {
        ":val1": {"S": "alice@example.com"},
        ":val2": {"S": "fred@example.com"}
    },
    "returnValues": "ALL_NEW"
}
```
Following is another sample request that can be handled by the updateItem operation.

```json
{
    "accessKeyId":"AKIxxxxxxxxxx",
    "secretAccessKey":"id4xxxxxx",
    "region":"us-east-1",
    "blocking":"false",
    "tableName": "Thread",
    "key": {
        "ForumName": {
            "S": "Amazon Dynamo"
        },
        "Subject": {
            "S": "How do I update multiple items?"
        }
    },
    "expected":{ 
      "ForumName":{ 
         "ComparisonOperator":"EQ",
         "AttributeValueList":[ 
            { 
               "S":"Amazon DynamoDB"
            }
         ]
      }
   },
   "attributeUpdates":{ 
      "Message":{ 
         "Action":"PUT",
         "Value":{ 
            "S":"The new Message."
         }
      }
   },
    "returnValues": "ALL_NEW"
} 
```

**Sample response**

Given below is a sample response for the updateItem operation.

```json
{
}
```
**Related Amazon DynamoDB documentation**
http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_UpdateItem.html

### Sample configuration

Following example illustrates how to connect to Amazon DynamoDB with the init operation and batchGetItem operation.

1. Create a sample proxy as below :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse"
       name="batchGetItem"
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
         <property expression="json-eval($.requestItems)" name="requestItems"/>
         <property expression="json-eval($.returnConsumedCapacity)" name="returnConsumedCapacity"/>
         <amazondynamodb.init>
            <secretAccessKey>{$ctx:secretAccessKey}</secretAccessKey>
            <accessKeyId>{$ctx:accessKeyId}</accessKeyId>
            <region>{$ctx:region}</region>
            <blocking>{$ctx:blocking}</blocking>
         </amazondynamodb.init>
         <amazondynamodb.batchGetItem>
            <requestItems>{$ctx:requestItems}</requestItems>
            <returnConsumedCapacity>{$ctx:returnConsumedCapacity}</returnConsumedCapacity>
         </amazondynamodb.batchGetItem>
         <respond/>
      </inSequence>
      <outSequence>
        <send/>
      </outSequence>
   </target>
   <description/>
</proxy>
```
2. Create an json file named batchGetItem.json and copy the configurations given below to it:

```json
{
    "accessKeyId":"AKIAxxxxxxxxxxxx",
    "secretAccessKey":"id4qxxxxxxxx",
    "region":"us-east-1",
    "blocking":"false",
    "requestItems": {
        "Thread": {
        "Keys": [
            {
                "ForumName": {
                    "S": "Amazon Dynamo"
                },
                "Subject": {
                    "S": "How do I update multiple items?"
                }
            }
        ],
        "AttributesToGet": [
            "Tags",
            "Message"
        ]
    }
    },
"returnConsumedCapacity":"TOTAL"
}
```

3. Replace the credentials with your values.

4. Execute the following curl command:

```bash
curl http://localhost:8280/services/batchGetItem -H "Content-Type: application/json" -d @batchGetItem.json
```
5. Amazon DynamoDB returns an json response similar to the one shown below:
 
```json
{
   "Responses":{
      "Test4782":[
         {
            "Message":{
               "S":"I want to update multiple items in a single call. What's the best way to do that?"
            },
            "Tags":{
               "SS":[
                  "HelpMe",
                  "Multiple Items",
                  "Update"
               ]
            }
         }
      ]
   },
   "UnprocessedKeys":{

   }
}
```