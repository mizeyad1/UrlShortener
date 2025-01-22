# **URL Shortener Service**

A Spring Boot-based URL Shortener Service that generates and resolves short URLs using UUIDs. The service integrates with DynamoDB for persistent storage.

---

## **Features**

- Generate short URLs using **UUID**.
- Resolve short URLs to their original long URLs.
- Handle URL expiration.
- DynamoDB integration for persistent storage.

---

## **Prerequisites**

- **Java**: JDK 17 or later
- **Maven**: 3.8.x or later
- **AWS CLI**: Configured with DynamoDB access
- **Docker**: (Optional) for running DynamoDB locally

---

## **Setting Up DynamoDB**

To use DynamoDB with the URL Shortener Service, you can run the DynamoDB locally

---

### Local DynamoDB

1. **Run DynamoDB Locally Using Docker**
   - Ensure Docker is installed on your system.
   - Pull and run the DynamoDB local Docker image:
     ```bash
     docker run -d -p 8000:8000 amazon/dynamodb-local
     ```
   - DynamoDB will be accessible at `http://localhost:8000`.

2. **Create the `UrlMapping` Table**
   - Use the AWS CLI or a DynamoDB client to create a table with the following configuration:
     - **Table Name**: `UrlMapping`
     - **Partition Key**: `shortUrl` (String)
   - Example CLI command:
     ```bash
     aws dynamodb create-table \
       --table-name UrlMapping \
       --attribute-definitions AttributeName=shortUrl,AttributeType=S \
       --key-schema AttributeName=shortUrl,KeyType=HASH \
       --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
       --endpoint-url http://localhost:8000
     ```

3. **Verify the Table**
   - List tables to confirm the `UrlMapping` table was created:
     ```bash
     aws dynamodb list-tables --endpoint-url http://localhost:8000
     ```

---







