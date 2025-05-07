package com.parser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class SiteParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiteParserApplication.class, args);
    }

}
