package url.shortener.coding.challenge.controller


import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import url.shortener.coding.challenge.exception.UrlExpiredException
import url.shortener.coding.challenge.exception.UrlNotFoundException
import url.shortener.coding.challenge.repository.UrlMappingEntity
import url.shortener.coding.challenge.service.UrlShortenerService
import java.time.LocalDateTime

@WebMvcTest(UrlShortenerController::class)
class UrlShortenerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var service: UrlShortenerService

    @Test
    fun `should shorten URL successfully`() {
        val longUrl = "https://example.com"
        val shortUrl = "short1234"
        val createdAt = LocalDateTime.now().toString()
        val expiryDate = LocalDateTime.now().plusDays(30).toString()

        `when`(service.shortenUrlWithDetails(longUrl)).thenReturn(
            UrlMappingEntity(shortUrl, longUrl, createdAt, expiryDate)
        )

        mockMvc.perform(
            post("/url")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"longUrl": "$longUrl"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.shortUrl").value("http://localhost/url/$shortUrl"))
            .andExpect(jsonPath("$.longUrl").value(longUrl))
            .andExpect(jsonPath("$.createdAt").value(createdAt))
            .andExpect(jsonPath("$.expiryDate").value(expiryDate))

        verify(service).shortenUrlWithDetails(longUrl)
    }

    @Test
    fun `should throw error when longUrl is missing`() {
        mockMvc.perform(
            post("/url")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("longUrl is required"))
    }

    @Test
    fun `should redirect to the original URL successfully`() {
        val shortUrl = "short1234"
        val longUrl = "https://example.com"

        `when`(service.resolveUrl(shortUrl)).thenReturn(longUrl)

        mockMvc.perform(get("/url/$shortUrl"))
            .andExpect(status().isMovedPermanently)
            .andExpect(header().string("Location", longUrl))

        verify(service).resolveUrl(shortUrl)
    }

    @Test
    fun `should return 404 if short URL is not found`() {
        val shortUrl = "notfound1234"

        `when`(service.resolveUrl(shortUrl)).thenThrow(UrlNotFoundException("URL not found"))

        mockMvc.perform(get("/url/$shortUrl"))
            .andExpect(status().isNotFound)

        verify(service).resolveUrl(shortUrl)
    }

    @Test
    fun `should return 410 if short URL has expired`() {
        val shortUrl = "expired1234"

        `when`(service.resolveUrl(shortUrl)).thenThrow(UrlExpiredException("URL has expired"))

        mockMvc.perform(get("/url/$shortUrl"))
            .andExpect(status().isGone)

        verify(service).resolveUrl(shortUrl)
    }
}
