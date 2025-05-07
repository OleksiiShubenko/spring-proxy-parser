package com.parser.service;

import com.parser.domain.ContentData;
import com.parser.domain.FetchType;
import com.parser.domain.SiteResponse;
import com.parser.error.ContentClientException;
import com.parser.error.ContentFetchException;
import com.parser.error.ContentLoadException;
import com.parser.error.ContentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class SpringContentService {

    private final ContentCacheService contentCacheService;
    private final RequestLogService requestLogService;
    private final ContentModifyService contentModifyService;
    private final RestClient restClient;


    @Autowired
    public SpringContentService(
            ContentCacheService contentCacheService,
            RequestLogService requestLogService,
            ContentModifyService contentModifyService,
            RestClient restClient
    ) {
        this.contentCacheService = contentCacheService;
        this.requestLogService = requestLogService;
        this.contentModifyService = contentModifyService;
        this.restClient = restClient;
    }

    public SiteResponse fetchSpringRootPage(String uri) {
        Optional<ContentData> htmlContent = contentCacheService.getByUri(uri);

        if (htmlContent.isPresent()) {
            return processCachedContent(htmlContent.get(), uri);
        }

        try {
            return process2xxResponse(uri);
        } catch (ContentLoadException ex) {
            requestLogService.logRequest(uri, ex.getStatus(), FetchType.NOT_CACHED);
            throw new ContentFetchException(ex.getMessage());
        } catch (ContentNotFoundException ex) {
            String siteHtml = process4xxError(ex, uri);
            throw new ContentClientException(siteHtml, ex.getResponseHeaders(), ex.getStatus(), ex.getMessage());
        }
    }

    private SiteResponse processCachedContent(ContentData contentData, String uri) {
        requestLogService.logRequest(uri, 200, FetchType.CACHED);
        log.info("Html content is fetched from mongo cache for uri: {}", uri);
        return new SiteResponse(contentData.getContent(), 200, HttpHeaders.EMPTY);
    }

    private SiteResponse process2xxResponse(String uri) {
        SiteResponse siteResponse = restClient.fetchDataByUrl(uri);

        String modifiedContent = contentModifyService.modifyResourceContent(uri, siteResponse.body());

        contentCacheService.cacheContent(uri, modifiedContent);
        requestLogService.logRequest(uri, siteResponse.statusCode(), FetchType.NOT_CACHED);
        log.info("Html content is not cached - fetched from site for uri: {}", uri);

        return new SiteResponse(modifiedContent, siteResponse.statusCode(), siteResponse.headers());
    }

    private String process4xxError(ContentNotFoundException ex, String uri) {
        String modifiedContent = contentModifyService.modifyResourceContent(uri, ex.getBody());

        requestLogService.logRequest(uri, ex.getStatus().value(), FetchType.NOT_CACHED);

        log.warn("Html content returned with {} status for uri: {}", ex.getStatus(), uri);
        return modifiedContent;
    }
}
