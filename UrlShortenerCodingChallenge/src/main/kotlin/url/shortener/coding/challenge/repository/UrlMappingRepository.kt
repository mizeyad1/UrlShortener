package url.shortener.coding.challenge.repository

import org.springframework.stereotype.Repository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema

@Repository
class UrlMappingRepository(private val dynamoDbClient: DynamoDbClient) {

    private val enhancedClient: DynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
        .dynamoDbClient(dynamoDbClient)
        .build()

    private val table: DynamoDbTable<UrlMappingEntity> =
        enhancedClient.table("url_mapping", TableSchema.fromBean(UrlMappingEntity::class.java))

    fun save(mapping: UrlMappingEntity) {
        table.putItem(mapping)
    }

    fun findByShortUrl(shortUrl: String): UrlMappingEntity? {
        return table.getItem { it.key { k -> k.partitionValue(shortUrl) } }
    }

    fun incrementClickCount(shortUrl: String) {
        val mapping = findByShortUrl(shortUrl)
        mapping?.let {
            it.clickCount += 1
            save(it)
        }
    }
}
