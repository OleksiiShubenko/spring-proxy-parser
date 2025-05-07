package com.parser.service;

import org.springframework.stereotype.Service;

@Service
public class UriService {

    public String prepareUri(String requestUri) {
        if (requestUri.contains(".") || requestUri.endsWith("/")) {
            return requestUri;
        } else {
            return requestUri + "/";
        }
    }
}
