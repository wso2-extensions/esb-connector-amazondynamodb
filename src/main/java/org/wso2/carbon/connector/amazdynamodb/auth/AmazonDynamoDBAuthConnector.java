/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector.amazdynamodb.auth;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.connector.amazdynamodb.constants.AmazonDynamoDBConstants;
import org.wso2.carbon.connector.core.AbstractConnector;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Class AmazonDynamoDBAuthConnector which helps to generate authentication signature for Amazon DynamoDB WSO2 ESB
 * Connector.
 */

public class AmazonDynamoDBAuthConnector extends AbstractConnector {
	/**
	 * Connect method which is generating authentication of the connector for each request.
	 *
	 * @param messageContext ESB messageContext.
	 */
	public final void connect(final MessageContext messageContext) {
		final StringBuilder canonicalRequest = new StringBuilder();
		final StringBuilder stringToSign = new StringBuilder();
		final StringBuilder payloadStrBuilder = new StringBuilder();
		final StringBuilder authHeader = new StringBuilder();

		// Generate time-stamp which will be sent to API and to be used in Signature
		final Date date = new Date();
		final TimeZone timeZone = TimeZone.getTimeZone(AmazonDynamoDBConstants.GMT);
		final DateFormat dateFormat = new SimpleDateFormat(AmazonDynamoDBConstants.ISO8601_BASIC_DATE_FORMAT);
		dateFormat.setTimeZone(timeZone);
		final String amzDate = dateFormat.format(date);

		final DateFormat shortDateFormat = new SimpleDateFormat(AmazonDynamoDBConstants.SHORT_DATE_FORMAT);
		shortDateFormat.setTimeZone(timeZone);
		final String shortDate = shortDateFormat.format(date);

		messageContext.setProperty(AmazonDynamoDBConstants.AMZ_DATE, amzDate);
		final Map<String, String> parameterNamesMap = getParameterNamesMap();
		final Map<String, String> parametersMap = getSortedParametersMap(messageContext, parameterNamesMap);
		try {
			canonicalRequest.append(messageContext.getProperty(AmazonDynamoDBConstants.HTTP_METHOD));
			canonicalRequest.append(AmazonDynamoDBConstants.NEW_LINE);
			canonicalRequest.append(messageContext.getProperty(AmazonDynamoDBConstants.HTTP_REQUEST_URI));
			canonicalRequest.append(AmazonDynamoDBConstants.NEW_LINE);

			for (Map.Entry<String, String> entry : parametersMap.entrySet()) {
				payloadStrBuilder.append('"');
				payloadStrBuilder.append(entry.getKey());
				payloadStrBuilder.append('"');
				payloadStrBuilder.append(':');

				if (entry.getValue().substring(0, 1).equals("{") || entry.getValue().substring(0, 1).equals("[")) {
					payloadStrBuilder.append(entry.getValue());
				} else if (entry.getKey().equals(AmazonDynamoDBConstants.API_LIMIT) ||
				           entry.getKey().equals(AmazonDynamoDBConstants.API_CONSISTENT_READ) ||
				           entry.getKey().equals(AmazonDynamoDBConstants.API_SCAN_INDEX_FORWARD) ||
				           entry.getKey().equals(AmazonDynamoDBConstants.API_SEGMENT) ||
				           entry.getKey().equals(AmazonDynamoDBConstants.API_TOTAL_SEGMENT)) {
					payloadStrBuilder.append(entry.getValue());
				} else {
					payloadStrBuilder.append('"');
					payloadStrBuilder.append(entry.getValue());
					payloadStrBuilder.append('"');
				}
				payloadStrBuilder.append(',');
			}

			if (payloadStrBuilder.length() > 0) {
				messageContext.setProperty(AmazonDynamoDBConstants.REQUEST_PAYLOAD,
				                           "{" + payloadStrBuilder.substring(0, payloadStrBuilder.length() - 1) + "}");
			}

			// Appends empty string since no url parameters are used in POST API requests
			canonicalRequest.append("");
			canonicalRequest.append(AmazonDynamoDBConstants.NEW_LINE);

			final Map<String, String> headersMap = getSortedHeadersMap(messageContext, parameterNamesMap);
			final StringBuilder canonicalHeaders = new StringBuilder();
			final StringBuilder signedHeader = new StringBuilder();

			final Set<String> keysSet = headersMap.keySet();
			for (String key : keysSet) {
				canonicalHeaders.append(key);
				canonicalHeaders.append(AmazonDynamoDBConstants.COLON);
				canonicalHeaders.append(headersMap.get(key));
				canonicalHeaders.append(AmazonDynamoDBConstants.NEW_LINE);
				signedHeader.append(key);
				signedHeader.append(AmazonDynamoDBConstants.SEMI_COLON);
			}

			canonicalRequest.append(canonicalHeaders.toString());
			canonicalRequest.append(AmazonDynamoDBConstants.NEW_LINE);

			// Remove unwanted semi-colon at the end of the signedHeader string
			String signedHeaders = "";
			if (signedHeader.length() > 0) {
				signedHeaders = signedHeader.substring(0, signedHeader.length() - 1);
			}

			canonicalRequest.append(signedHeaders);
			canonicalRequest.append(AmazonDynamoDBConstants.NEW_LINE);

			String requestPayload = "";
			if (payloadStrBuilder.length() > 0) {
				requestPayload = "{" + payloadStrBuilder.substring(0, payloadStrBuilder.length() - 1) + "}";
			} else {
				requestPayload = "{}";
			}

			canonicalRequest.append(bytesToHex(hash(messageContext, requestPayload)).toLowerCase());

			stringToSign.append(AmazonDynamoDBConstants.AWS4_HMAC_SHA_256);
			stringToSign.append(AmazonDynamoDBConstants.NEW_LINE);
			stringToSign.append(amzDate);
			stringToSign.append(AmazonDynamoDBConstants.NEW_LINE);

			stringToSign.append(shortDate);
			stringToSign.append(AmazonDynamoDBConstants.FORWARD_SLASH);
			stringToSign.append(messageContext.getProperty(AmazonDynamoDBConstants.REGION));
			stringToSign.append(AmazonDynamoDBConstants.FORWARD_SLASH);
			stringToSign.append(messageContext.getProperty(AmazonDynamoDBConstants.SERVICE));
			stringToSign.append(AmazonDynamoDBConstants.FORWARD_SLASH);
			stringToSign.append(messageContext.getProperty(AmazonDynamoDBConstants.TERMINATION_STRING));

			stringToSign.append(AmazonDynamoDBConstants.NEW_LINE);
			stringToSign.append(bytesToHex(hash(messageContext, canonicalRequest.toString())).toLowerCase());

			final byte[] signingKey =
					getSignatureKey(messageContext,
					                messageContext.getProperty(AmazonDynamoDBConstants.SECRET_ACCESS_KEY).toString(),
					                shortDate, messageContext.getProperty(AmazonDynamoDBConstants.REGION).toString(),
					                messageContext.getProperty(AmazonDynamoDBConstants.SERVICE).toString());

			// Construction of authorization header value to be included in API request
			authHeader.append(AmazonDynamoDBConstants.AWS4_HMAC_SHA_256);
			authHeader.append(" ");
			authHeader.append(AmazonDynamoDBConstants.CREDENTIAL);
			authHeader.append(AmazonDynamoDBConstants.EQUAL);
			authHeader.append(messageContext.getProperty(AmazonDynamoDBConstants.ACCESS_KEY_ID));
			authHeader.append(AmazonDynamoDBConstants.FORWARD_SLASH);
			authHeader.append(shortDate);
			authHeader.append(AmazonDynamoDBConstants.FORWARD_SLASH);
			authHeader.append(messageContext.getProperty(AmazonDynamoDBConstants.REGION));
			authHeader.append(AmazonDynamoDBConstants.FORWARD_SLASH);
			authHeader.append(messageContext.getProperty(AmazonDynamoDBConstants.SERVICE));
			authHeader.append(AmazonDynamoDBConstants.FORWARD_SLASH);
			authHeader.append(messageContext.getProperty(AmazonDynamoDBConstants.TERMINATION_STRING));
			authHeader.append(AmazonDynamoDBConstants.COMMA);
			authHeader.append(AmazonDynamoDBConstants.SIGNED_HEADERS);
			authHeader.append(AmazonDynamoDBConstants.EQUAL);
			authHeader.append(signedHeaders);
			authHeader.append(AmazonDynamoDBConstants.COMMA);
			authHeader.append(AmazonDynamoDBConstants.API_SIGNATURE);
			authHeader.append(AmazonDynamoDBConstants.EQUAL);
			authHeader.append(bytesToHex(hmacSHA256(signingKey, stringToSign.toString())).toLowerCase());

			// Adds authorization header to message context
			messageContext.setProperty(AmazonDynamoDBConstants.AUTHORIZATION_HEADER, authHeader.toString());

		} catch (InvalidKeyException exc) {
			storeErrorResponseStatus(messageContext, exc, AmazonDynamoDBConstants.INVALID_KEY_ERROR_CODE);
			handleException(AmazonDynamoDBConstants.INVALID_KEY_ERROR, exc, messageContext);
		} catch (NoSuchAlgorithmException exc) {
			storeErrorResponseStatus(messageContext, exc, AmazonDynamoDBConstants.NO_SUCH_ALGORITHM_ERROR_CODE);
			handleException(AmazonDynamoDBConstants.NO_SUCH_ALGORITHM_ERROR, exc, messageContext);
		} catch (IllegalStateException exc) {
			storeErrorResponseStatus(messageContext, exc, AmazonDynamoDBConstants.ILLEGAL_STATE_ERROR_CODE);
			handleException(AmazonDynamoDBConstants.CONNECTOR_ERROR, exc, messageContext);
		} catch (UnsupportedEncodingException exc) {
			storeErrorResponseStatus(messageContext, exc, AmazonDynamoDBConstants.UNSUPPORTED_ENCORDING_ERROR_CODE);
			handleException(AmazonDynamoDBConstants.CONNECTOR_ERROR, exc, messageContext);
		}
	}

	/**
	 * getParameterKeys method returns a list of parameter keys.
	 *
	 * @return list of parameter key value.
	 */
	private String[] getParameterKeys() {
		return new String[] { AmazonDynamoDBConstants.REQUEST_ITEMS, AmazonDynamoDBConstants.RETURN_CONSUMED_CAPACITY,
		                      AmazonDynamoDBConstants.ATTRIBUTE_DEFINITIONS, AmazonDynamoDBConstants.TABLE_NAME,
		                      AmazonDynamoDBConstants.KEY_SCHEMA, AmazonDynamoDBConstants.LOCAL_SECONDARY_INDEXES,
		                      AmazonDynamoDBConstants.PROVISIONED_THROUGHPUT,
		                      AmazonDynamoDBConstants.GLOBAL_SECONDARY_INDEXES,
		                      AmazonDynamoDBConstants.STREAM_SPECIFICATION, AmazonDynamoDBConstants.ITEM,
		                      AmazonDynamoDBConstants.CONDITIONAL_OPERATOR,
		                      AmazonDynamoDBConstants.CONDITIONAL_EXPRESSION, AmazonDynamoDBConstants.EXPECTED,
		                      AmazonDynamoDBConstants.EXPRESSION_ATTRIBUTE_NAMES,
		                      AmazonDynamoDBConstants.EXPRESSION_ATTRIBUTE_VALUES,
		                      AmazonDynamoDBConstants.RETURN_ITEM_COLLECTION_METRICS,
		                      AmazonDynamoDBConstants.RETURN_VALUES, AmazonDynamoDBConstants.KEY,
		                      AmazonDynamoDBConstants.ATTRIBUTES_TO_GET, AmazonDynamoDBConstants.CONSISTENT_READ,
		                      AmazonDynamoDBConstants.PROJECTION_EXPRESSION,
		                      AmazonDynamoDBConstants.EXCLUSIVE_START_TABLE_NAME, AmazonDynamoDBConstants.LIMIT,
		                      AmazonDynamoDBConstants.ATTRIBUTE_UPDATES, AmazonDynamoDBConstants.UPDATE_EXPRESSION,
		                      AmazonDynamoDBConstants.GLOBAL_SECONDARY_INDEX_UPDATES,
		                      AmazonDynamoDBConstants.EXCLUSIVE_START_KEY, AmazonDynamoDBConstants.FILTER_EXPRESSION,
		                      AmazonDynamoDBConstants.INDEX_NAME, AmazonDynamoDBConstants.KEY_CONDITION_EXPRESSION,
		                      AmazonDynamoDBConstants.KEY_CONDITIONS, AmazonDynamoDBConstants.QUERY_FILTER,
		                      AmazonDynamoDBConstants.SCAN_INDEX_FORWARD, AmazonDynamoDBConstants.SELECT,
		                      AmazonDynamoDBConstants.SCAN_FILTER, AmazonDynamoDBConstants.SEGMENT,
		                      AmazonDynamoDBConstants.TOTAL_SEGMENT };
	}

	/**
	 * getHeaderKeys method returns a list of parameter keys.
	 *
	 * @return list of parameter key value.
	 */
	private String[] getHeaderKeys() {
		return new String[] { AmazonDynamoDBConstants.HOST, AmazonDynamoDBConstants.CONTENT_TYPE,
		                      AmazonDynamoDBConstants.AMZ_DATE };
	}

	/**
	 * getParametersMap method used to return list of parameter values sorted by expected API parameter names.
	 *
	 * @param messageContext ESB messageContext.
	 * @param namesMap       contains a map of esb parameter names and matching API parameter names
	 * @return assigned parameter values as a HashMap.
	 */
	private Map<String, String> getSortedParametersMap(final MessageContext messageContext,
	                                                   final Map<String, String> namesMap) {
		final String[] singleValuedKeys = getParameterKeys();
		final Map<String, String> parametersMap = new TreeMap<String, String>();
		// Stores sorted, single valued API parameters
		for (final String key : singleValuedKeys) {
			// builds the parameter map only if provided by the user
			if (messageContext.getProperty(key) != null && !("").equals(messageContext.getProperty(key))) {
				parametersMap.put(namesMap.get(key), (String) messageContext.getProperty(key));
			}
		}
		return parametersMap;
	}

	/**
	 * getSortedHeadersMap method used to return list of header values sorted by expected API parameter names.
	 *
	 * @param messageContext ESB messageContext.
	 * @param namesMap       contains a map of esb parameter names and matching API parameter names
	 * @return assigned header values as a HashMap.
	 */
	private Map<String, String> getSortedHeadersMap(final MessageContext messageContext,
	                                                final Map<String, String> namesMap) {
		final String[] headerKeys = getHeaderKeys();
		final Map<String, String> parametersMap = new TreeMap<String, String>();
		// Stores sorted, single valued API parameters
		for (final String key : headerKeys) {
			// builds the parameter map only if provided by the user
			if (messageContext.getProperty(key) != null && !("").equals(messageContext.getProperty(key))) {
				parametersMap.put(namesMap.get(key).toLowerCase(), messageContext.getProperty(key).toString().trim().
						replaceAll(AmazonDynamoDBConstants.TRIM_SPACE_REGEX, AmazonDynamoDBConstants.SPACE));
			}
		}
		return parametersMap;
	}

	/**
	 * getParameterNamesMap returns a map of esb parameter names and corresponding API parameter names.
	 *
	 * @return generated map.
	 */
	private Map<String, String> getParameterNamesMap() {
		final Map<String, String> map = new HashMap<String, String>();
		map.put(AmazonDynamoDBConstants.TARGET, AmazonDynamoDBConstants.API_TARGET);
		map.put(AmazonDynamoDBConstants.SIGNATURE_METHOD, AmazonDynamoDBConstants.API_SIGNATURE_METHOD);
		map.put(AmazonDynamoDBConstants.TIMESTAMP, AmazonDynamoDBConstants.API_TIMESTAMP);
		map.put(AmazonDynamoDBConstants.ACCESS_KEY_ID, AmazonDynamoDBConstants.AWS_ACCESS_KEY_ID);
		map.put(AmazonDynamoDBConstants.REQUEST_ITEMS, AmazonDynamoDBConstants.API_REQUEST_ITEMS);
		map.put(AmazonDynamoDBConstants.RETURN_CONSUMED_CAPACITY, AmazonDynamoDBConstants.API_RETURN_CONSUMED_CAPACITY);
		map.put(AmazonDynamoDBConstants.ATTRIBUTE_DEFINITIONS, AmazonDynamoDBConstants.API_ATTRIBUTE_DEFINITIONS);
		map.put(AmazonDynamoDBConstants.TABLE_NAME, AmazonDynamoDBConstants.API_TABLE_NAME);
		map.put(AmazonDynamoDBConstants.KEY_SCHEMA, AmazonDynamoDBConstants.API_KEY_SCHEMA);
		map.put(AmazonDynamoDBConstants.LOCAL_SECONDARY_INDEXES, AmazonDynamoDBConstants.API_LOCAL_SECONDARY_INDEXES);
		map.put(AmazonDynamoDBConstants.PROVISIONED_THROUGHPUT, AmazonDynamoDBConstants.API_PROVISIONED_THROUGHPUT);
		map.put(AmazonDynamoDBConstants.STREAM_SPECIFICATION, AmazonDynamoDBConstants.API_STREAM_SPECIFICATION);
		map.put(AmazonDynamoDBConstants.GLOBAL_SECONDARY_INDEXES, AmazonDynamoDBConstants.API_GLOBAL_SECONDARY_INDEXES);
		map.put(AmazonDynamoDBConstants.ITEM, AmazonDynamoDBConstants.API_ITEM);
		map.put(AmazonDynamoDBConstants.CONDITIONAL_OPERATOR, AmazonDynamoDBConstants.API_CONDITIONAL_OPERATOR);
		map.put(AmazonDynamoDBConstants.CONDITIONAL_EXPRESSION, AmazonDynamoDBConstants.API_CONDITIONAL_EXPRESSION);
		map.put(AmazonDynamoDBConstants.EXPECTED, AmazonDynamoDBConstants.API_EXPECTED);
		map.put(AmazonDynamoDBConstants.EXPRESSION_ATTRIBUTE_NAMES,
		        AmazonDynamoDBConstants.API_EXPRESSION_ATTRIBUTE_NAMES);
		map.put(AmazonDynamoDBConstants.EXPRESSION_ATTRIBUTE_VALUES,
		        AmazonDynamoDBConstants.API_EXPRESSION_ATTRIBUTE_VALUES);
		map.put(AmazonDynamoDBConstants.RETURN_ITEM_COLLECTION_METRICS,
		        AmazonDynamoDBConstants.API_RETURN_ITEM_COLLECTION_METRICS);
		map.put(AmazonDynamoDBConstants.RETURN_VALUES, AmazonDynamoDBConstants.API_RETURN_VALUES);
		map.put(AmazonDynamoDBConstants.KEY, AmazonDynamoDBConstants.API_KEY);
		map.put(AmazonDynamoDBConstants.CONSISTENT_READ, AmazonDynamoDBConstants.API_CONSISTENT_READ);
		map.put(AmazonDynamoDBConstants.ATTRIBUTES_TO_GET, AmazonDynamoDBConstants.API_ATTRIBUTES_TO_GET);
		map.put(AmazonDynamoDBConstants.PROJECTION_EXPRESSION, AmazonDynamoDBConstants.API_PROJECTION_EXPRESSION);
		map.put(AmazonDynamoDBConstants.EXCLUSIVE_START_TABLE_NAME,
		        AmazonDynamoDBConstants.API_EXCLUSIVE_START_TABLE_NAME);
		map.put(AmazonDynamoDBConstants.LIMIT, AmazonDynamoDBConstants.API_LIMIT);
		map.put(AmazonDynamoDBConstants.UPDATE_EXPRESSION, AmazonDynamoDBConstants.API_UPDATE_EXPRESSION);
		map.put(AmazonDynamoDBConstants.ATTRIBUTE_UPDATES, AmazonDynamoDBConstants.API_ATTRIBUTE_UPDATES);
		map.put(AmazonDynamoDBConstants.GLOBAL_SECONDARY_INDEX_UPDATES,
		        AmazonDynamoDBConstants.API_GLOBAL_SECONDARY_INDEX_UPDATES);
		map.put(AmazonDynamoDBConstants.EXCLUSIVE_START_KEY, AmazonDynamoDBConstants.API_EXCLUSIVE_START_KEY);
		map.put(AmazonDynamoDBConstants.FILTER_EXPRESSION, AmazonDynamoDBConstants.API_FILTER_EXPRESSION);
		map.put(AmazonDynamoDBConstants.INDEX_NAME, AmazonDynamoDBConstants.API_INDEX_NAME);
		map.put(AmazonDynamoDBConstants.KEY_CONDITION_EXPRESSION, AmazonDynamoDBConstants.API_KEY_CONDITION_EXPRESSION);
		map.put(AmazonDynamoDBConstants.KEY_CONDITIONS, AmazonDynamoDBConstants.API_KEY_CONDITIONS);
		map.put(AmazonDynamoDBConstants.QUERY_FILTER, AmazonDynamoDBConstants.API_QUERY_FILTER);
		map.put(AmazonDynamoDBConstants.SCAN_INDEX_FORWARD, AmazonDynamoDBConstants.API_SCAN_INDEX_FORWARD);
		map.put(AmazonDynamoDBConstants.SELECT, AmazonDynamoDBConstants.API_SELECT);
		map.put(AmazonDynamoDBConstants.SEGMENT, AmazonDynamoDBConstants.API_SEGMENT);
		map.put(AmazonDynamoDBConstants.TOTAL_SEGMENT, AmazonDynamoDBConstants.API_TOTAL_SEGMENT);
		map.put(AmazonDynamoDBConstants.SCAN_FILTER, AmazonDynamoDBConstants.API_SCAN_FILTER);

		// Header parameters
		map.put(AmazonDynamoDBConstants.HOST, AmazonDynamoDBConstants.API_HOST);
		map.put(AmazonDynamoDBConstants.CONTENT_TYPE, AmazonDynamoDBConstants.API_CONTENT_TYPE);
		map.put(AmazonDynamoDBConstants.AMZ_DATE, AmazonDynamoDBConstants.API_AMZ_DATE);

		return map;
	}

	/**
	 * Add a Throwable to a message context, the message from the throwable is embedded as the Synapse.
	 * Constant ERROR_MESSAGE.
	 *
	 * @param ctxt      message context to which the error tags need to be added
	 * @param throwable Throwable that needs to be parsed and added
	 * @param errorCode errorCode mapped to the exception
	 */
	private void storeErrorResponseStatus(final MessageContext ctxt, final Throwable throwable, final int errorCode) {
		ctxt.setProperty(SynapseConstants.ERROR_CODE, errorCode);
		ctxt.setProperty(SynapseConstants.ERROR_MESSAGE, throwable.getMessage());
		ctxt.setFaultResponse(true);
	}

	/**
	 * Add a message to message context, the message from the throwable is embedded as the Synapse Constant
	 * ERROR_MESSAGE.
	 *
	 * @param ctxt      message context to which the error tags need to be added
	 * @param message   message to be returned to the user
	 * @param errorCode errorCode mapped to the exception
	 */
	private void storeErrorResponseStatus(final MessageContext ctxt, final String message, final int errorCode) {
		ctxt.setProperty(SynapseConstants.ERROR_CODE, errorCode);
		ctxt.setProperty(SynapseConstants.ERROR_MESSAGE, message);
		ctxt.setFaultResponse(true);
	}

	/**
	 * Hashes the string contents (assumed to be UTF-8) using the SHA-256 algorithm.
	 *
	 * @param messageContext of the connector
	 * @param text           text to be hashed
	 * @return SHA-256 hashed text
	 */
	private byte[] hash(final MessageContext messageContext, final String text) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(AmazonDynamoDBConstants.SHA_256);
			messageDigest.update(text.getBytes(AmazonDynamoDBConstants.UTF_8));
		} catch (Exception exc) {
			storeErrorResponseStatus(messageContext, exc, AmazonDynamoDBConstants.ERROR_CODE_EXCEPTION);
			handleException(AmazonDynamoDBConstants.CONNECTOR_ERROR, exc, messageContext);
		}
		if (messageDigest == null) {
			log.error(AmazonDynamoDBConstants.CONNECTOR_ERROR);
			storeErrorResponseStatus(messageContext, AmazonDynamoDBConstants.CONNECTOR_ERROR,
			                         AmazonDynamoDBConstants.ERROR_CODE_EXCEPTION);
			handleException(AmazonDynamoDBConstants.CONNECTOR_ERROR, messageContext);
		}
		if (messageDigest != null) {
			return messageDigest.digest();
		}
		return null;
	}

	/**
	 * bytesToHex method HexEncoded the received byte array.
	 *
	 * @param bytes bytes to be hex encoded
	 * @return hex encoded String of the given byte array
	 */
	private static String bytesToHex(final byte[] bytes) {
		final char[] hexArray = AmazonDynamoDBConstants.HEX_ARRAY_STRING.toCharArray();
		char[] hexChars = new char[bytes.length * 2];

		for (int j = 0; j < bytes.length; j++) {
			final int byteVal = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[byteVal >>> 4];
			hexChars[j * 2 + 1] = hexArray[byteVal & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 * Provides the HMAC SHA 256 encoded value(using the provided key) of the given data.
	 *
	 * @param key  to use for encoding
	 * @param data to be encoded
	 * @return HMAC SHA 256 encoded byte array
	 * @throws NoSuchAlgorithmException     No such algorithm Exception
	 * @throws InvalidKeyException          Invalid key Exception
	 * @throws UnsupportedEncodingException Unsupported Encoding Exception
	 * @throws IllegalStateException        Illegal State Exception
	 */
	private static byte[] hmacSHA256(final byte[] key, final String data)
			throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
		final String algorithm = AmazonDynamoDBConstants.HAMC_SHA_256;
		final Mac mac = Mac.getInstance(algorithm);
		mac.init(new SecretKeySpec(key, algorithm));
		return mac.doFinal(data.getBytes(AmazonDynamoDBConstants.UTF8));
	}

	/**
	 * Returns the encoded signature key to be used for further encodings as per API doc.
	 *
	 * @param ctx         message context of the connector
	 * @param key         key to be used for signing
	 * @param dateStamp   current date stamp
	 * @param regionName  region name given to the connector
	 * @param serviceName Name of the service being addressed
	 * @return Signature key
	 * @throws UnsupportedEncodingException Unsupported Encoding Exception
	 * @throws IllegalStateException        Illegal Argument Exception
	 * @throws NoSuchAlgorithmException     No Such Algorithm Exception
	 * @throws InvalidKeyException          Invalid Key Exception
	 */
	private static byte[] getSignatureKey(final MessageContext ctx, final String key, final String dateStamp,
	                                      final String regionName, final String serviceName)
			throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException {
		final byte[] kSecret = (AmazonDynamoDBConstants.AWS4 + key).getBytes(AmazonDynamoDBConstants.UTF8);
		final byte[] kDate = hmacSHA256(kSecret, dateStamp);
		final byte[] kRegion = hmacSHA256(kDate, regionName);
		final byte[] kService = hmacSHA256(kRegion, serviceName);
		return hmacSHA256(kService, ctx.getProperty(AmazonDynamoDBConstants.TERMINATION_STRING).toString());
	}
}
