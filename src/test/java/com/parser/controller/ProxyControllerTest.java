package com.parser.controller;

import com.parser.domain.SiteResponse;
import com.parser.error.ContentClientException;
import com.parser.error.ContentFetchException;
import com.parser.service.SpringContentService;
import jakarta.servlet.http.HttpServletRequest;
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

@ExtendWith(MockitoExtension.class)
public class ProxyControllerTest {

    @Mock
    private SpringContentService springContentService;

    private ProxyController proxyController;
    
    private final String URI = "/test";
    private final StringBuffer URL_BUFFER = new StringBuffer("http://localhost/test");
    

    @BeforeEach
    void setUp() {
        proxyController = new ProxyController(springContentService);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(springContentService);
    }


    @Test
    void fetchProxyContent_shouldReturnOkResponse_whenServiceReturnsValidResponse() {
        String body = "<html>Hello, World!</html>";
        HttpHeaders mockHeaders = new HttpHeaders();
        mockHeaders.add("Content-Type", "text/html");

        SiteResponse mockResponse = new SiteResponse(body, 200, mockHeaders);

        Mockito.when(springContentService.fetchSpringRootPage(URI))
                .thenReturn(mockResponse);

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURL()).thenReturn(URL_BUFFER);
        Mockito.when(mockRequest.getRequestURI()).thenReturn(URI);

        ResponseEntity<String> response = proxyController.fetchProxyContent(mockRequest);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(body, response.getBody());
        Assertions.assertEquals("text/html", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));

        Mockito.verify(springContentService, Mockito.times(1)).fetchSpringRootPage(URI);
    }

    @Test
    void fetchProxyContent_shouldReturnInternalServerError_whenServiceThrowsContentFetchException() {
        Mockito.when(springContentService.fetchSpringRootPage(URI))
                .thenThrow(new ContentFetchException("Failed to fetch content"));

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURL()).thenReturn(URL_BUFFER);
        Mockito.when(mockRequest.getRequestURI()).thenReturn(URI);

        ResponseEntity<String> response = proxyController.fetchProxyContent(mockRequest);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertEquals("Failed to fetch content", response.getBody());

        Mockito.verify(springContentService, Mockito.times(1)).fetchSpringRootPage(URI);
    }

    @Test
    void fetchProxyContent_shouldReturnCustomResponse_whenServiceThrowsContentClientException() {
        String body = "<html>404 Not Found</html>";
        HttpHeaders mockHeaders = new HttpHeaders();
        mockHeaders.add("Content-Type", "application/json");

        ContentClientException mockException = new ContentClientException(body, mockHeaders, HttpStatus.BAD_REQUEST, "Error");

        Mockito.when(springContentService.fetchSpringRootPage(URI))
                .thenThrow(mockException);

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURL()).thenReturn(URL_BUFFER);
        Mockito.when(mockRequest.getRequestURI()).thenReturn(URI);

        ResponseEntity<String> response = proxyController.fetchProxyContent(mockRequest);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(body, response.getBody());
        Assertions.assertEquals("application/json", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));

        Mockito.verify(springContentService, Mockito.times(1)).fetchSpringRootPage(URI);
    }
}