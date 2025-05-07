package com.parser.controller;

import com.parser.domain.SiteResponse;
import com.parser.error.ContentClientException;
import com.parser.error.ContentFetchException;
import com.parser.service.SpringContentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProxyController {

    private final SpringContentService springContentService;

    @Autowired
    public ProxyController(SpringContentService springContentService) {
        this.springContentService = springContentService;
    }

    @GetMapping("/**")
    public ResponseEntity<String> fetchProxyContent(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        try {
            SiteResponse response = springContentService.fetchSpringRootPage(request.getRequestURI());
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .headers(response.headers())
                    .body(response.body());
        } catch (ContentFetchException ex) {
            return ResponseEntity.internalServerError().body(ex.getMessage());
        } catch (ContentClientException ex){
            return new ResponseEntity<>(ex.getBody(), ex.getResponseHeaders(), ex.getStatus());
        }
    }
}
