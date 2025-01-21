package url.shortener.coding.challenge.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import url.shortener.coding.challenge.exception.InvalidUrlFormatException
import url.shortener.coding.challenge.exception.UrlExpiredException
import url.shortener.coding.challenge.exception.UrlNotFoundException
import url.shortener.coding.challenge.repository.UrlMappingEntity
import url.shortener.coding.challenge.repository.UrlMappingRepository
import url.shortener.coding.challenge.util.ShortUrlGenerator
import java.time.LocalDateTime

class UrlShortenerServiceTest {

    private val repository: UrlMappingRepository = mock(UrlMappingRepository::class.java)
    private val shortUrlGenerator: ShortUrlGenerator = mock(ShortUrlGenerator::class.java)
    private val service = UrlShortenerService(repository, shortUrlGenerator)

    @Test
    fun `should shorten URL with details successfully`() {
        val longUrl = "https://example.com"
        val shortUrl = "short1234"
        val createdAt = LocalDateTime.now().toString()
        val expiryDate = LocalDateTime.now().plusDays(30).toString()

        val expectedMapping = UrlMappingEntity(shortUrl, longUrl, createdAt, expiryDate)

        `when`(shortUrlGenerator.generateUniqueShortUrl(longUrl, 30)).thenReturn(expectedMapping)

        val result = service.shortenUrlWithDetails(longUrl)

        assertEquals(shortUrl, result.short_url)
        assertEquals(longUrl, result.longUrl)
        assertEquals(createdAt, result.creationDate)
        assertEquals(expiryDate, result.expirationDate)

        verify(repository).save(expectedMapping)
        verify(shortUrlGenerator).generateUniqueShortUrl(longUrl, 30)
    }

    @Test
    fun `should throw InvalidUrlFormatException for invalid long URL`() {
        val invalidUrl = "invalid-url"

        val exception = assertThrows<InvalidUrlFormatException> {
            service.shortenUrlWithDetails(invalidUrl)
        }

        assertEquals("Invalid URL format", exception.message)
    }

    @Test
    fun `should resolve short URL successfully`() {
        val shortUrl = "short1234"
        val longUrl = "https://example.com"
        val createdAt = LocalDateTime.now().toString()
        val expiryDate = LocalDateTime.now().plusDays(30).toString()

        val mapping = UrlMappingEntity(shortUrl, longUrl, createdAt, expiryDate)

        `when`(repository.findByShortUrl(shortUrl)).thenReturn(mapping)

        val result = service.resolveUrl(shortUrl)

        assertEquals(longUrl, result)

        verify(repository).incrementClickCount(shortUrl)
    }

    @Test
    fun `should throw UrlNotFoundException for nonexistent short URL`() {
        val shortUrl = "notfound1234"

        `when`(repository.findByShortUrl(shortUrl)).thenReturn(null)

        val exception = assertThrows<UrlNotFoundException> {
            service.resolveUrl(shortUrl)
        }

        assertEquals("URL not found", exception.message)
    }

    @Test
    fun `should throw UrlExpiredException for expired URL`() {
        val shortUrl = "short1234"
        val longUrl = "https://example.com"
        val createdAt = LocalDateTime.now().minusDays(40).toString()
        val expiryDate = LocalDateTime.now().minusDays(10).toString()

        val mapping = UrlMappingEntity(shortUrl, longUrl, createdAt, expiryDate)

        `when`(repository.findByShortUrl(shortUrl)).thenReturn(mapping)

        val exception = assertThrows<UrlExpiredException> {
            service.resolveUrl(shortUrl)
        }

        assertEquals("URL has expired", exception.message)
    }


}
