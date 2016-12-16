/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.connector.integration.test.amazondynamodb;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;
import org.wso2.connector.integration.test.base.RestResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AmazonDynamoDBConnectorIntegrationTest extends ConnectorIntegrationTestBase {
	private Map<String, String> esbRequestHeadersMap = new HashMap<String, String>();
	private static int SLEEP_TIME;

	/**
	 * Set up the environment.
	 */
	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {

		init("amazondynamodb-connector-1.0.1-SNAPSHOT");

		esbRequestHeadersMap.put("Content-Type", "application/x-amz-json-1.0");
		SLEEP_TIME = Integer.parseInt(connectorProperties.getProperty("sleepTime"));
	}

	/**
	 * Positive test case for createTable method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {createTable} integration test with mandatory " +
	                                             "parameters.")
	public void testCreateTableWithMandatoryParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createTable");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createTable_mandatory.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("TableDescription").getString("TableStatus"),
		                    "CREATING");
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("TableDescription").getString("TableName"),
		                    connectorProperties.getProperty("tableName"));
		Assert.assertTrue(esbRestResponse.getBody().toString().contains("TableArn"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for createTable method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {createTable} integration test with Optional " +
	                                             "parameters.")
	public void testCreateTableWithOptionalParameters() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createTable");

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createTable_Optional.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("TableDescription").getString("TableStatus"),
		                    "CREATING");
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("TableDescription").getString("TableName"),
		                    connectorProperties.getProperty("tableNameOpt"));
		Assert.assertTrue(esbRestResponse.getBody().toString().contains("LocalSecondaryIndexes"));
		Assert.assertTrue(esbRestResponse.getBody().toString().contains("TableArn"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for createTable method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {createTable} integration test negative case.")
	public void testCreateTableWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:createTable");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "createTable_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getString("message"),
		                    "The parameter 'TableName' is required but was not present in the request");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}

	/**
	 * Positive test case for updateTable method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithMandatoryParameters" },
			description = "amazondynamodb {updateTable} integration test with mandatory parameters.")
	public void testUpdateTableWithMandatoryParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:updateTable");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "updateTable_mandatory.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("TableDescription").getString("TableStatus"),
		                    "UPDATING");
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("TableDescription").getString("TableName"),
		                    connectorProperties.getProperty("tableName"));
		Assert.assertTrue(esbRestResponse.getBody().toString().contains("TableArn"));
		Assert.assertTrue(esbRestResponse.getBody().toString().contains("ProvisionedThroughput"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for updateTable method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {updateTable} integration test negative case.")
	public void testUpdateTableWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:updateTable");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "updateTable_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getString("message"),
		                    "The parameter 'TableName' is required but was not present in the request");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}

	/**
	 * Positive test case for listTables method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" },
			description = "amazondynamodb {listTables} integration test with mandatory parameters.")
	public void testListTablesWithMandatoryParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:listTables");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "listTables_mandatory.json");

		Assert.assertTrue(esbRestResponse.getBody().toString().contains("TableNames"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for listTables method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithMandatoryParameters" },
			description = "amazondynamodb {listTables} integration test with optional parameters.")
	public void testListTablesWithOptionalParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:listTables");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "listTables_optional.json");

		Assert.assertTrue(esbRestResponse.getBody().toString().contains("TableNames"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for describeTable method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithMandatoryParameters" },
			description = "amazondynamodb {describeTable} integration test with mandatory parameters.")
	public void testDescribeTableWithMandatoryParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:describeTable");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "describeTable_mandatory.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("Table").getString("TableStatus"), "ACTIVE");
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("Table").getString("TableName"),
		                    connectorProperties.getProperty("tableName"));
		Assert.assertTrue(esbRestResponse.getBody().toString().contains("TableArn"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for describeTable method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {describeTable} integration test negative case.")
	public void testDescribeTableWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:describeTable");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "describeTable_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getString("message"),
		                    "The parameter 'TableName' is required but was not present in the request");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}

	/**
	 * Positive test case for putItem method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithMandatoryParameters" },
			description = "amazondynamodb {putItem} integration test with mandatory parameters.")
	public void testPutItemWithMandatoryParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:putItem");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "putItem_mandatory.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for putItem method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithOptionalParameters",
	                                           "testPutItemWithMandatoryParameters" },
			description = "amazondynamodb {putItem} integration test with optional parameters.")
	public void testPutItemWithOptionalParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:putItem");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "putItem_optional.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("ConsumedCapacity").getString("TableName"),
		                    connectorProperties.getProperty("tableNameOpt"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for putItem method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {putItem} integration test negative case.")
	public void testPutItemWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:putItem");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "putItem_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getString("message"),
		                    "1 validation error detected: Value null at 'tableName' failed to satisfy constraint:" +
		                    " Member must not be null");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}

	/**
	 * Positive test case for getItem method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithMandatoryParameters",
	                                           "testPutItemWithMandatoryParameters" },
			description = "amazondynamodb {getItem} integration test with mandatory parameters.")
	public void testGetItemWithMandatoryParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:getItem");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getItem_mandatory.json");

		Assert.assertTrue(esbRestResponse.getBody().toString().contains("Item"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for getItem method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithOptionalParameters",
	                                           "testPutItemWithOptionalParameters" },
			description = "amazondynamodb {getItem} integration test with optional parameters.")
	public void testGetItemWithOptionalParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:getItem");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getItem_optional.json");

		Assert.assertTrue(esbRestResponse.getBody().toString().contains("Item"));
		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("ConsumedCapacity").getString("TableName"),
		                    connectorProperties.getProperty("tableNameOpt"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for getItem method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {getItem} integration test negative case.")
	public void testGetItemWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:getItem");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getItem_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getString("message"),
		                    "1 validation error detected: Value null at 'tableName' failed to satisfy constraint:" +
		                    " Member must not be null");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}

	/**
	 * Positive test case for updateItem method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithMandatoryParameters",
	                                           "testPutItemWithMandatoryParameters" },
			description = "amazondynamodb {updateItem} integration test with optional parameters.")
	public void testUpdateItemWithOptionalParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:updateItem");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "updateItem_optional.json");

		Assert.assertTrue(esbRestResponse.getBody().toString().contains("Attributes"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for updateItem method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {updateItem} integration test negative case.")
	public void testUpdateItemWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:updateItem");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "updateItem_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getString("message"),
		                    "1 validation error detected: Value null at 'tableName' failed to satisfy constraint:" +
		                    " Member must not be null");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}

	/**
	 * Positive test case for batchGetItem method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithMandatoryParameters",
	                                           "testPutItemWithMandatoryParameters",
	                                           "testUpdateItemWithOptionalParameters" },
			description = "amazondynamodb {batchGetItem} integration test with mandatory parameters.")
	public void testBatchGetItemWithMandatoryParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:batchGetItem");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "batchGetItem_mandatory.json");

		Assert.assertTrue(esbRestResponse.getBody().toString().contains("Responses"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Positive test case for batchGetItem method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithMandatoryParameters",
	                                           "testPutItemWithMandatoryParameters",
	                                           "testUpdateItemWithOptionalParameters" },
			description = "amazondynamodb {batchGetItem} integration test with optional parameters.")
	public void testBatchGetItemWithOptionalParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:batchGetItem");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "batchGetItem_optional.json");

		Assert.assertTrue(esbRestResponse.getBody().toString().contains("Responses"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for batchGetItem method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {batchGetItem} integration test negative case.")
	public void testBatchGetItemWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:batchGetItem");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "batchGetItem_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getString("message"),
		                    "1 validation error detected: Value null at 'requestItems' failed to satisfy constraint:" +
		                    " Member must not be null");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}

	/**
	 * Positive test case for query method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithMandatoryParameters",
	                                           "testPutItemWithMandatoryParameters",
	                                           "testUpdateItemWithOptionalParameters" },
			description = "amazondynamodb {query} integration test with optional parameters.")
	public void testQueryWithOptionalParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:query");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "query_optional.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("ConsumedCapacity").getString("TableName"),
		                    connectorProperties.getProperty("tableNameOpt"));
		Assert.assertTrue(esbRestResponse.getBody().toString().contains("Items"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for query method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {query} integration test negative case.")
	public void testQueryWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:query");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "query_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getString("message"),
		                    "1 validation error detected: Value null at 'tableName' failed to satisfy constraint:" +
		                    " Member must not be null");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}

	/**
	 * Positive test case for scan method with optional parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, dependsOnMethods = { "testCreateTableWithMandatoryParameters",
	                                           "testPutItemWithMandatoryParameters",
	                                           "testUpdateItemWithOptionalParameters" },
			description = "amazondynamodb {scan} integration test with optional parameters.")
	public void testScanWithOptionalParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:scan");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "scan_optional.json");

		Assert.assertEquals(esbRestResponse.getBody().getJSONObject("ConsumedCapacity").getString("TableName"),
		                    connectorProperties.getProperty("tableNameOpt"));
		Assert.assertTrue(esbRestResponse.getBody().toString().contains("Items"));
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for scan method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {scan} integration test negative case.")
	public void testScanWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:scan");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "scan_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getString("message"),
		                    "1 validation error detected: Value null at 'tableName' failed to satisfy constraint:" +
		                    " Member must not be null");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}

	/**
	 * Positive test case for deleteItem method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, priority = 1,
			description = "amazondynamodb {deleteItem} integration test with mandatory parameters.")
	public void testDeleteItemWithMandatoryParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:deleteItem");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteItem_mandatory.json");
		
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for deleteItem method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {deleteItem} integration test negative case.")
	public void testDeleteItemWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteItem");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteItem_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getString("message"),
		                    "1 validation error detected: Value null at 'tableName' failed to satisfy constraint:" +
		                    " Member must not be null");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}

	/**
	 * Positive test case for deleteTable method with mandatory parameters.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, priority = 2,
			description = "amazondynamodb {deleteTable} integration test with mandatory parameters.")
	public void testDeleteTableWithMandatoryParameters() throws IOException, JSONException, InterruptedException {
		esbRequestHeadersMap.put("Action", "urn:deleteTable");
		Thread.sleep(SLEEP_TIME);

		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteTable_mandatory.json");

		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
	}

	/**
	 * Negative test case for deleteTable method.
	 *
	 * @throws JSONException
	 * @throws IOException
	 */
	@Test(groups = { "wso2.esb" }, description = "amazondynamodb {deleteTable} integration test negative case.")
	public void testDeleteTableWithNegativeCase() throws IOException, JSONException {
		esbRequestHeadersMap.put("Action", "urn:deleteTable");
		RestResponse<JSONObject> esbRestResponse =
				sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "deleteTable_negative.json");

		Assert.assertEquals(esbRestResponse.getBody().getString("message"),
		                    "The parameter 'TableName' is required but was not present in the request");
		Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
	}
}
