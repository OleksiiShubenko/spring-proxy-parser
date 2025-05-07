package com.parser.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MongoTTLIndexConfig {

    private final MongoTemplate mongoTemplate;
    private final Integer expirySeconds;

    public MongoTTLIndexConfig(MongoTemplate mongoTemplate, @Value("${spring.data.mongodb.expirySeconds}") Integer expirySeconds) {
        this.mongoTemplate = mongoTemplate;
        this.expirySeconds = expirySeconds;
    }

    @PostConstruct
    public void initIndexes() {
        mongoTemplate.indexOps("content")
                .ensureIndex(
                        new Index()
                                .on("createdAt", Sort.Direction.ASC)
                                .expire(expirySeconds, TimeUnit.SECONDS)
                );
    }
}
