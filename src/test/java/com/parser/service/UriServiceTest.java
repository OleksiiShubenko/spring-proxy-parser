package com.parser.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UriServiceTest {

    private UriService uriService;

    @BeforeEach
    void setUp() {
        uriService = new UriService();
    }

    @ParameterizedTest
    @CsvSource({
            "'/', '/'",
            "'', '/'",
            "'/index.html', '/index.html'",
            "'/about', '/about/'",
            "'/about/', '/about/'",
            "'/file.js', '/file.js'",
            "'/path.svg', '/path.svg'"
    })
    void prepareUri_shouldProcessUriCorrectly(String inputUri, String expectedOutput) {
        Assertions.assertEquals(expectedOutput, uriService.prepareUri(inputUri));
    }
}
