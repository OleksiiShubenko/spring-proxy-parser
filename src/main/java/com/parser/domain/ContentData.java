package com.parser.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "content")
public class ContentData {

    @Id
    private String uri;

    private String content;

    private Date createdAt;

    public ContentData() {
    }

    public ContentData(String uri, String content, Date createdAt) {
        this.uri = uri;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
