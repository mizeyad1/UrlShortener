package url.shortener.coding.challenge.repository

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey


@DynamoDbBean
data class UrlMappingEntity(
    @get:DynamoDbPartitionKey
    var short_url: String = "",
    var longUrl: String = "",
    var creationDate: String = "",
    var expirationDate: String = "",
    var clickCount: Int = 0
)


