package com.parser.service;

import com.parser.domain.FetchType;
import com.parser.domain.RequestLog;
import com.parser.repository.RequestLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RequestLogService {

    private final RequestLogRepository requestLogRepository;

    public RequestLogService(RequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    public void logRequest(String uri, int status, FetchType fetchType) {
        requestLogRepository.save(new RequestLog(Instant.now(), uri, status, fetchType));
    }
}
