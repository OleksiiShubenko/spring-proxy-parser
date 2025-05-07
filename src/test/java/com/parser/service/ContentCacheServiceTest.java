package com.parser.service;

import com.parser.domain.ContentData;
import com.parser.repository.ContentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ContentCacheServiceTest {

    private final String KEY = "/test";

    @Mock
    private ContentRepository contentRepository;

    private ContentCacheService contentCacheService;

    @BeforeEach
    void setUp() {
        contentCacheService = new ContentCacheService(contentRepository);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(contentRepository);
    }

    @Test
    void getByUri_shouldReturnOptionalContentData_whenUriExists() {
        ContentData contentData = new ContentData(KEY, "<html>Cached Content</html>", new Date());
        Mockito.when(contentRepository.findById(KEY)).thenReturn(Optional.of(contentData));

        Optional<ContentData> result = contentCacheService.getByUri(KEY);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(KEY, result.get().getUri());
        Assertions.assertEquals(contentData.getContent(), result.get().getContent());

        Mockito.verify(contentRepository, Mockito.times(1)).findById(KEY);
    }

    @Test
    void getByUri_shouldReturnEmptyOptional_whenUriDoesNotExist() {
        Mockito.when(contentRepository.findById(KEY)).thenReturn(Optional.empty());

        Optional<ContentData> result = contentCacheService.getByUri(KEY);

        Assertions.assertFalse(result.isPresent());

        Mockito.verify(contentRepository, Mockito.times(1)).findById(KEY);
    }

    @Test
    void cacheContent_shouldSaveContentDataToRepository() {
        String html = "<html> Spring Content</html>";
        ArgumentCaptor<ContentData> contentDataCaptor = ArgumentCaptor.forClass(ContentData.class);

        contentCacheService.cacheContent(KEY, html);

        Mockito.verify(contentRepository, Mockito.times(1)).save(contentDataCaptor.capture());
        ContentData savedContentData = contentDataCaptor.getValue();

        Assertions.assertEquals(KEY, savedContentData.getUri());
        Assertions.assertEquals(html, savedContentData.getContent());
    }
}
