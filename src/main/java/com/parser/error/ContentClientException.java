package com.parser.error;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

public class ContentClientException extends RuntimeException {

    private final String body;
    private final HttpStatusCode status;
    private final HttpHeaders responseHeaders;

    public ContentClientException(String body, HttpHeaders responseHeaders, HttpStatusCode status, String message) {
        super(message);
        this.body = body;
        this.status = status;
        this.responseHeaders = responseHeaders;
    }

    public String getBody() {
        return body;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public HttpHeaders getResponseHeaders() {
        return responseHeaders;
    }
}
