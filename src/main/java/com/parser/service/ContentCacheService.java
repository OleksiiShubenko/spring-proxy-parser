package com.parser.service;

import com.parser.domain.ContentData;
import com.parser.repository.ContentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class ContentCacheService {

    private final ContentRepository contentRepository;

    public ContentCacheService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public Optional<ContentData> getByUri(String key) {
        return contentRepository.findById(key);
    }

    public void cacheContent(String key, String html) {
        ContentData contentData = new ContentData(key, html, Date.from(Instant.now()));
        contentRepository.save(contentData);
    }
}
