package url.shortener.coding.challenge.service

import org.springframework.stereotype.Service
import url.shortener.coding.challenge.exception.InvalidUrlFormatException
import url.shortener.coding.challenge.exception.UrlExpiredException
import url.shortener.coding.challenge.exception.UrlNotFoundException
import url.shortener.coding.challenge.repository.UrlMappingEntity
import url.shortener.coding.challenge.repository.UrlMappingRepository
import url.shortener.coding.challenge.util.ShortUrlGenerator
import java.net.URI
import java.net.URISyntaxException
import java.time.LocalDateTime

@Service
class UrlShortenerService(
    private val repository: UrlMappingRepository,
    private val shortUrlGenerator: ShortUrlGenerator
) {

    fun shortenUrlWithDetails(longUrl: String, expirationDays: Int = 30): UrlMappingEntity {
        validateLongUrl(longUrl)
        val mapping = shortUrlGenerator.generateUniqueShortUrl(longUrl, expirationDays)
        repository.save(mapping)
        return mapping
    }

    fun resolveUrl(shortUrl: String): String {
        val mapping = repository.findByShortUrl(shortUrl)
            ?: throw UrlNotFoundException("URL not found")

        if (LocalDateTime.parse(mapping.expirationDate).isBefore(LocalDateTime.now())) {
            throw UrlExpiredException("URL has expired")
        }

        repository.incrementClickCount(shortUrl)
        return mapping.longUrl
    }

    private fun validateLongUrl(longUrl: String) {
        try {
            val uri = URI(longUrl)
            if (uri.scheme == null || uri.host == null) {
                throw InvalidUrlFormatException("Invalid URL format")
            }
        } catch (e: URISyntaxException) {
            throw InvalidUrlFormatException("Invalid URL format")
        }
    }
}
