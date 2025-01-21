package url.shortener.coding.challenge.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import url.shortener.coding.challenge.exception.UrlExpiredException
import url.shortener.coding.challenge.exception.UrlNotFoundException
import url.shortener.coding.challenge.service.UrlShortenerService

@RestController
@RequestMapping("/url")
class UrlShortenerController(private val service: UrlShortenerService) {

    @PostMapping
    fun shortenUrl(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, String>> {
        try {
            val longUrl = request["longUrl"] ?: throw IllegalArgumentException("longUrl is required")
            val urlDetails = service.shortenUrlWithDetails(longUrl)
            val response = mapOf(
                "shortUrl" to "http://localhost/url/${urlDetails.short_url}",
                "longUrl" to urlDetails.longUrl,
                "createdAt" to urlDetails.creationDate,
                "expiryDate" to urlDetails.expirationDate
            )
            return ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message.orEmpty()))
        }
    }


    @GetMapping("/{shortUrl}")
    fun resolveUrl(@PathVariable shortUrl: String): ResponseEntity<Void> {
        try {
            val longUrl = service.resolveUrl(shortUrl)
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .header("Location", longUrl)
                .build()
        } catch (e: UrlNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: UrlExpiredException) {
            throw ResponseStatusException(HttpStatus.GONE, e.message)
        }
    }
}
