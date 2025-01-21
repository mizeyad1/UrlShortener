package url.shortener.coding.challenge.util


import org.springframework.stereotype.Component
import url.shortener.coding.challenge.repository.UrlMappingEntity
import url.shortener.coding.challenge.repository.UrlMappingRepository
import java.time.LocalDateTime
import java.util.UUID

@Component
class ShortUrlGenerator(private val repository: UrlMappingRepository) {

    fun generateUniqueShortUrl(longUrl: String, expirationDays: Int): UrlMappingEntity {
        repeat(2) {
            val shortUrl = UUID.randomUUID().toString().substring(0, 8)
            if (repository.findByShortUrl(shortUrl) == null) {
                return UrlMappingEntity(
                    short_url = shortUrl,
                    longUrl = longUrl,
                    creationDate = LocalDateTime.now().toString(),
                    expirationDate = LocalDateTime.now().plusDays(expirationDays.toLong()).toString(),
                    clickCount = 0
                )
            }
        }
        throw RuntimeException("Unable to generate a unique short URL after two attempts")
    }
}
