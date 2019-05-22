# Amazon DynamoDB EI Connector

The Amazon DynamoDB [connector](https://docs.wso2.com/display/EI650/Working+with+Connectors) allows you to access the [Amazon DynamoDB REST API](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.API.html) through WSO2 EI. 
Amazon DynamoDB is a fully managed NoSQL database service that provides fast and predictable performance with seamless scalability. 
AmazonDynamoDB enables customers to offload the administrative burdens of operating and scaling distributed databases to AWS, so they don’t have to worry about hardware provisioning, setup and configuration, replication, software patching, or cluster scaling.

## Compatibility

| Connector version | Supported Amazon DynamoDB API version | Supported WSO2 ESB/EI version |
| ------------- | ---------------|------------- |
| [1.0.1](https://github.com/wso2-extensions/esb-connector-amazondynamodb/tree/org.wso2.carbon.connector.amazondynamodb-1.0.1) | 2012-08-10 | EI 6.5.0 |
| [1.0.0](https://github.com/wso2-extensions/esb-connector-amazondynamodb/tree/org.wso2.carbon.connector.amazondynamodb-1.0.0) | 2012-08-10 | ESB 5.0.0, EI 6.3.0, EI 6.4.0|

## Getting started

#### Download and install the connector

1. Download the connector from the [WSO2 Store](https://store.wso2.com/store/assets/esbconnector/details/3fcaf309-1a69-4edf-870a-882bb76fdaa1) by clicking the **Download Connector** button.
2. You can then follow this [documentation](https://docs.wso2.com/display/EI650/Working+with+Connectors+via+the+Management+Console) to add the connector to your WSO2 EI instance and to enable it (via the management console).
3. For more information on using connectors and their operations in your WSO2 EI configurations, see [Using a Connector](https://docs.wso2.com/display/EI650/Using+a+Connector).
4. If you want to work with connectors via WSO2 EI Tooling, see [Working with Connectors via Tooling](https://docs.wso2.com/display/EI650/Working+with+Connectors+via+Tooling).

#### Configuring the connector operations

To get started with Amazon DynamoDB connector and their operations, see [Configuring Amazon DynamoDB Operations](docs/config.md).

## Building From the Source

Follow the steps given below to build the Amazon DynamoDB connector from the source code:

1. Get a clone or download the source from [Github](https://github.com/wso2-extensions/esb-connector-amazondynamodb).
2. Run the following Maven command from the `esb-connector-amazondynamodb` directory: `mvn clean install`.
3. The Amazon DynamoDB connector zip file is created in the `esb-connector-amazondynamodb/target` directory

## How You Can Contribute

As an open source project, WSO2 extensions welcome contributions from the community.
Check the [issue tracker](https://github.com/wso2-extensions/esb-connector-amazondynamodb/issues) for open issues that interest you. We look forward to receiving your contributions.
