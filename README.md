# AWS Integration Spring Boot Project

This project is a Spring Boot application aims to integrates with following AWS services using the AWS SDK for Java v2. It currently supports two AWS services:

- **Amazon SQS:** is a fully managed message queuing service that decouples and scales distributed systems and applications by enabling reliable communication between components through message passing.
- **Amazon S3:** is a highly scalable, durable, and secure object storage service designed to store and retrieve any amount of data from anywhere on the internet.
- **Amazon SNS:** A fully managed pub/sub messaging service for sending notifications and messages to distributed systems and mobile devices.
- **Amazon DynamoDB:** A NoSQL database service providing fast and flexible performance for applications that need consistent, single-digit millisecond latency.
- **AWS Lambda:** A serverless computing service that runs code in response to events and automatically manages the compute resources.
- **Amazon Cognito:** A service for managing user authentication and access control for your applications.
- **AWS CloudWatch:** A monitoring service for tracking metrics, collecting logs, and setting alarms to help you gain operational insights.


## Developed Endpoints

### SQS Endpoints
- **Send Message:** `POST /aws/sqs/send`
- **Receive Messages:** `GET /aws/sqs/receive`
- **Delete Message:** `DELETE /aws/sqs/delete`

### S3 Endpoints
- **List Buckets:** `GET /aws/s3/buckets`
- **List Objects:** `GET /aws/s3/objects`
- **Upload File:** `POST /aws/s3/upload`
- **Delete Object:** `DELETE /aws/s3/delete/object`
- **Download File:** `GET /aws/s3/download`
- **Copy Object:** `POST /aws/s3/copy`
- **Generate Pre-Signed URL:** `GET /aws/s3/presignedUrl`

## Getting Started

### Prerequisites

- **Java 11 or later**
- **Maven 3.6+**
- **AWS Credentials:**  
  Configure your AWS credentials using environment variables, system properties, or the default credentials provider chain. Ensure that the IAM user has appropriate permissions:
  - **For SQS:** `AmazonSQSFullAccess`
  - **For S3:** Required S3 permissions (e.g., listing, uploading, deleting objects).

### Running the Application

1. **Clone the repository:**

    ```bash
    git clone https://github.com/yourusername/reactive-employee-service.git
    cd reactive-employee-service
    ```

2. **Build the project:**

    ```bash
    mvn clean install
    ```

3. **Run the application:**

    ```bash
    mvn spring-boot:run
    ```

4.	Access the APIs:
The application runs on http://localhost:8080 by default. Use Postman (or any REST client) to send requests to the endpoints described above.

### Testing with Postman
  - A Postman collection file, `postman_test_collection.json`, is included in the project. Import this file into Postman to quickly test the APIs
  - The collection includes sample requests for all the implemented endpoints.
  - Modify request parameters (such as bucket names, queue URLs, file uploads, etc.) as needed to match your AWS setup.
