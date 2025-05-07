package com.parser.service;

import com.parser.domain.ContentData;
import com.parser.domain.FetchType;
import com.parser.domain.SiteResponse;
import com.parser.error.ContentClientException;
import com.parser.error.ContentFetchException;
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

import java.util.Date;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SpringContentServiceTest {

    @Mock
    private ContentCacheService contentCacheService;

    @Mock
    private RequestLogService requestLogService;

    @Mock
    private ContentModifyService contentModifyService;

    @Mock
    private RestClient restClient;

    private SpringContentService springContentService;

    @BeforeEach
    void setUp() {
        springContentService = new SpringContentService(contentCacheService, requestLogService, contentModifyService, restClient);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(contentCacheService, requestLogService, contentModifyService, restClient);
    }

    @Test
    void fetchSpringRootPage_shouldReturnCachedContent_whenHtmlIsPresentInCache() {
        String uri = "/cached-content";
        String cachedHtml = "<html>Cached Content</html>";

        ContentData cachedContentData = new ContentData(uri, cachedHtml, new Date());
        Mockito.when(contentCacheService.getByUri(uri)).thenReturn(Optional.of(cachedContentData));

        SiteResponse result = springContentService.fetchSpringRootPage(uri);

        Assertions.assertEquals(200, result.statusCode());
        Assertions.assertEquals(cachedHtml, result.body());
        Assertions.assertEquals(HttpHeaders.EMPTY, result.headers());

        Mockito.verify(contentCacheService, Mockito.times(1)).getByUri(uri);
        Mockito.verify(requestLogService, Mockito.times(1)).logRequest(uri, 200, FetchType.CACHED);
    }

    @Test
    void fetchSpringRootPage_shouldReturnContentFromRestClient_whenCacheIsEmpty() {
        String uri = "/page";
        String originalHtml = "<html>Original Content</html>";
        String modifiedHtml = "<html>Modified Content™</html>";

        SiteResponse fetchedResponse = new SiteResponse(originalHtml, 200, HttpHeaders.EMPTY);

        Mockito.when(contentCacheService.getByUri(uri)).thenReturn(Optional.empty());
        Mockito.when(restClient.fetchDataByUrl(uri)).thenReturn(fetchedResponse);
        Mockito.when(contentModifyService.modifyResourceContent(uri, originalHtml)).thenReturn(modifiedHtml);

        SiteResponse result = springContentService.fetchSpringRootPage(uri);

        Assertions.assertEquals(200, result.statusCode());
        Assertions.assertEquals(modifiedHtml, result.body());

        Mockito.verify(contentCacheService, Mockito.times(1)).getByUri(uri);
        Mockito.verify(restClient, Mockito.times(1)).fetchDataByUrl(uri);
        Mockito.verify(contentModifyService, Mockito.times(1)).modifyResourceContent(uri, originalHtml);
        Mockito.verify(contentCacheService, Mockito.times(1)).cacheContent(uri, modifiedHtml);
        Mockito.verify(requestLogService, Mockito.times(1)).logRequest(uri, 200, FetchType.NOT_CACHED);
    }

    @Test
    void fetchSpringRootPage_shouldThrowContentClientException_whenRestClientReturns4xxError() {
        String uri = "/not-found";
        String originalHtml = "<html>Content is Not Found</html>";
        String modifiedHtml = "<html>Content™ is Not Found</html>";

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/html");

        ContentNotFoundException notFoundException = new ContentNotFoundException(originalHtml, responseHeaders, HttpStatus.NOT_FOUND, "Page not found");
        Mockito.when(contentCacheService.getByUri(uri)).thenReturn(Optional.empty());
        Mockito.when(restClient.fetchDataByUrl(uri)).thenThrow(notFoundException);
        Mockito.when(contentModifyService.modifyResourceContent(uri, originalHtml)).thenReturn(modifiedHtml);

        ContentClientException clientException = Assertions.assertThrows(ContentClientException.class, () -> springContentService.fetchSpringRootPage(uri));

        Assertions.assertEquals(HttpStatus.NOT_FOUND, clientException.getStatus());
        Assertions.assertEquals(modifiedHtml, clientException.getBody());
        Assertions.assertEquals(responseHeaders, clientException.getResponseHeaders());

        Mockito.verify(contentCacheService, Mockito.times(1)).getByUri(uri);
        Mockito.verify(restClient, Mockito.times(1)).fetchDataByUrl(uri);
        Mockito.verify(contentModifyService, Mockito.times(1)).modifyResourceContent(uri, originalHtml);
        Mockito.verify(requestLogService, Mockito.times(1)).logRequest(uri, HttpStatus.NOT_FOUND.value(), FetchType.NOT_CACHED);
    }

    @Test
    void fetchSpringRootPage_shouldThrowContentFetchException_whenRestClientFailsWithInternalError() {
        String uri = "/server-error";

        ContentLoadException loadException = new ContentLoadException("Internal Server Error", 500);
        Mockito.when(contentCacheService.getByUri(uri)).thenReturn(Optional.empty());
        Mockito.when(restClient.fetchDataByUrl(uri)).thenThrow(loadException);

        ContentFetchException fetchException = Assertions.assertThrows(ContentFetchException.class, () -> springContentService.fetchSpringRootPage(uri));

        Assertions.assertEquals("Internal Server Error", fetchException.getMessage());

        Mockito.verify(contentCacheService, Mockito.times(1)).getByUri(uri);
        Mockito.verify(restClient, Mockito.times(1)).fetchDataByUrl(uri);
        Mockito.verify(requestLogService, Mockito.times(1)).logRequest(uri, 500, FetchType.NOT_CACHED);
    }
}
