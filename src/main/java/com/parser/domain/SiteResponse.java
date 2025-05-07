package com.parser.domain;

import org.springframework.http.HttpHeaders;

public record SiteResponse(
        String body,
        int statusCode,
        HttpHeaders headers
) {
}
