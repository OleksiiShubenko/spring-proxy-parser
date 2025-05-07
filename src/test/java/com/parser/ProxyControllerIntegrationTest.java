package com.parser;

import com.parser.domain.ContentData;
import com.parser.domain.FetchType;
import com.parser.repository.ContentRepository;
import com.parser.repository.RequestLogRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Optional;

import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProxyControllerIntegrationTest {

    @Autowired
    private RequestLogRepository requestLogRepository;

    @Autowired
    private ContentRepository contentRepository;

    @LocalServerPort
    private int port;

    @Value("${spring.data.mongodb.expirySeconds}")
    private Integer expirySeconds;
    private Duration awaitDuration;
    private Duration pollingSeconds;

    private RestTemplate restTemplate;

    private String localHost;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        localHost = "http://localhost:" + port;
        awaitDuration = Duration.ofSeconds((expirySeconds + 30) * 1000L);
        pollingSeconds = Duration.ofSeconds(5);
    }

    @Test
    void fetchContent_shouldReturnModifiedResponse_whenValidEndpointCalled() {
        String pageUri = "/";

        // first request - content is fetched from remote site
        ResponseEntity<String> response = restTemplate.getForEntity(localHost + pageUri, String.class);

        checkSuccessfulResponse(pageUri, response);
        Assertions.assertTrue(requestLogRepository.findAll().stream()
                .anyMatch(log -> log.getRequestUri().equals("/") && log.getResponseStatus() == 200 && log.getFetchType().equals(FetchType.NOT_CACHED)));

        // second request - content is fetched from mongo cache
        response = restTemplate.getForEntity(localHost + pageUri, String.class);
        checkSuccessfulResponse(pageUri, response);
        Assertions.assertTrue(requestLogRepository.findAll().stream()
                .anyMatch(log -> log.getRequestUri().equals("/") && log.getResponseStatus() == 200 && log.getFetchType().equals(FetchType.CACHED)));

        await().atMost(awaitDuration)
                .pollInterval(pollingSeconds)
                .until(() -> contentRepository.findById(pageUri).isEmpty());
    }

    private void checkSuccessfulResponse(String pageUri, ResponseEntity<String> response) {
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("<html"));
        Assertions.assertTrue(response.getBody().contains("™"));

        Optional<ContentData> cachedContentData = contentRepository.findById(pageUri);
        Assertions.assertTrue(cachedContentData.isPresent());
    }

    @Test
    void fetchContent_shouldReturnError_whenPageDoesNotExist() {
        String pageUri = "/not-existing";
        try {
            restTemplate.getForEntity(localHost + pageUri, String.class);
        } catch (HttpClientErrorException ex) {
            Assertions.assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

            Assertions.assertTrue(requestLogRepository.findAll().stream()
                    .anyMatch(log -> log.getRequestUri().equals(pageUri) && log.getResponseStatus() == 404));
        }
    }
}
