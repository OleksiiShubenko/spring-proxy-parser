package com.parser.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Instant timestamp;

    @Column
    private String requestUri;

    @Column
    private int responseStatus;

    @Column
    @Enumerated(EnumType.STRING)
    private FetchType fetchType;

    public RequestLog() {
    }

    public RequestLog(Instant timestamp, String requestUri, int responseStatus, FetchType fetchType) {
        this.timestamp = timestamp;
        this.requestUri = requestUri;
        this.responseStatus = responseStatus;
        this.fetchType = fetchType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    public void setFetchType(FetchType fetchType) {
        this.fetchType = fetchType;
    }
}
