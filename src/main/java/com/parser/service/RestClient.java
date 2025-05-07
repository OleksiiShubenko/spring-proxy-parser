package com.parser.service;

import com.parser.domain.SiteResponse;
import com.parser.error.ContentLoadException;
import com.parser.error.ContentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RestClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final UriService uriService;

    public RestClient(@Value("${baseUrl}") String baseUrl, UriService uriService, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.uriService = uriService;
        this.restTemplate = restTemplate;
    }

    public SiteResponse fetchDataByUrl(String requestUri) {
        String uri = uriService.prepareUri(requestUri);
        String fullUrl = baseUrl + uri;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
            log.debug("Successful response for: {}", fullUrl);

            return new SiteResponse(response.getBody(), response.getStatusCode().value(), response.getHeaders());
        } catch (RestClientException e) {
            if (e instanceof HttpClientErrorException clientError) {
                HttpStatusCode statusCode = clientError.getStatusCode();
                if (statusCode.is4xxClientError()) {
                    throw new ContentNotFoundException(clientError.getResponseBodyAsString(), clientError.getResponseHeaders(), statusCode, clientError.getMessage());
                }
            }
            String errorMessage = String.format("Internal Error. Failed to fetch data from %s. Error: %s", fullUrl, e.getMessage());
            log.error(errorMessage, e);
            throw new ContentLoadException(errorMessage, 500);
        }
    }
}
