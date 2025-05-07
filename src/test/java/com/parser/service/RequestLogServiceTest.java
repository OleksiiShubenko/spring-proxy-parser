package com.parser.service;

import com.parser.domain.FetchType;
import com.parser.domain.RequestLog;
import com.parser.repository.RequestLogRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RequestLogServiceTest {

    @Mock
    private RequestLogRepository requestLogRepository;

    private RequestLogService requestLogService;

    @BeforeEach
    void setUp() {
        requestLogService = new RequestLogService(requestLogRepository);
    }

    @Test
    void logRequest_shouldSaveRequestLogToRepository() {
        String uri = "/test";
        int status = 200;

        ArgumentCaptor<RequestLog> requestLogCaptor = ArgumentCaptor.forClass(RequestLog.class);

        requestLogService.logRequest(uri, status, FetchType.NOT_CACHED);

        Mockito.verify(requestLogRepository).save(requestLogCaptor.capture());
        RequestLog capturedLog = requestLogCaptor.getValue();

        Assertions.assertNotNull(capturedLog);
        Assertions.assertEquals(uri, capturedLog.getRequestUri());
        Assertions.assertEquals(status, capturedLog.getResponseStatus());
        Assertions.assertNotNull(capturedLog.getTimestamp());

        Mockito.verify(requestLogRepository, Mockito.times(1)).save(capturedLog);
    }
}
