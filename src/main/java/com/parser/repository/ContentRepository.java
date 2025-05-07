package com.parser.repository;

import com.parser.domain.ContentData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends MongoRepository<ContentData, String> {
}
