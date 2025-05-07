package com.parser.service;

import com.parser.domain.SiteResponse;
import com.parser.error.ContentLoadException;
import com.parser.error.ContentNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

@ExtendWith(MockitoExtension.class)
class RestClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UriService uriService;

    private RestClient restClient;

    private final String BASE_URL = "http://localhost.com";
    private final String REQUEST_URI = "/page";
    private final String PREPARED_URI = "/page/";
    private final String FULL_URI = "http://localhost.com/page/";

    @BeforeEach
    void setUp() {
        restClient = new RestClient(BASE_URL, uriService, restTemplate);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(restTemplate, uriService);
    }

    @Test
    void fetchDataByUrl_shouldReturnSiteResponse_whenApiResponseIsSuccessful() {
        Mockito.when(uriService.prepareUri(REQUEST_URI)).thenReturn(PREPARED_URI);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/html");
        ResponseEntity<String> responseEntity = new ResponseEntity<>("<html>Hello World</html>", headers, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(FULL_URI, String.class)).thenReturn(responseEntity);

        SiteResponse siteResponse = restClient.fetchDataByUrl(REQUEST_URI);

        Assertions.assertNotNull(siteResponse);
        Assertions.assertEquals("<html>Hello World</html>", siteResponse.body());
        Assertions.assertEquals(200, siteResponse.statusCode());
        Assertions.assertEquals(headers, siteResponse.headers());

        Mockito.verify(uriService, Mockito.times(1)).prepareUri(REQUEST_URI);
        Mockito.verify(restTemplate, Mockito.times(1)).getForEntity(FULL_URI, String.class);
    }

    @Test
    void fetchDataByUrl_shouldThrowContentNotFoundException_when4xxErrorOccurs() {
        Mockito.when(uriService.prepareUri(REQUEST_URI)).thenReturn(PREPARED_URI);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found", headers, "Page not found".getBytes(), Charset.defaultCharset());

        Mockito.when(restTemplate.getForEntity(FULL_URI, String.class)).thenThrow(exception);

        ContentNotFoundException contentNotFoundException = Assertions.assertThrows(ContentNotFoundException.class, () -> restClient.fetchDataByUrl(REQUEST_URI));

        Assertions.assertNotNull(contentNotFoundException);
        Assertions.assertEquals("Page not found", contentNotFoundException.getBody());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, contentNotFoundException.getStatus());
        Assertions.assertEquals(headers, contentNotFoundException.getResponseHeaders());

        Mockito.verify(restTemplate, Mockito.times(1)).getForEntity(FULL_URI, String.class);
    }

    @Test
    void fetchDataByUrl_shouldThrowContentLoadException_whenRestClientFails() {
        Mockito.when(uriService.prepareUri(REQUEST_URI)).thenReturn(PREPARED_URI);

        RestClientException exception = new RestClientException("Connection timeout");
        Mockito.when(restTemplate.getForEntity(FULL_URI, String.class)).thenThrow(exception);

        ContentLoadException contentLoadException = Assertions.assertThrows(ContentLoadException.class, () -> restClient.fetchDataByUrl(REQUEST_URI));

        Assertions.assertNotNull(contentLoadException);
        Assertions.assertEquals(500, contentLoadException.getStatus());
        Assertions.assertTrue(contentLoadException.getMessage().contains("Internal Error. Failed to fetch data from http://localhost.com/page/. Error: Connection timeout"));

        Mockito.verify(restTemplate, Mockito.times(1)).getForEntity(FULL_URI, String.class);
    }
}
