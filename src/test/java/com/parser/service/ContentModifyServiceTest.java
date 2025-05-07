package com.parser.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContentModifyServiceTest {

    private final String URI = "/page";

    private ContentModifyService contentModifyService;

    @BeforeEach
    void setUp() {
        contentModifyService = new ContentModifyService();
    }

    @Test
    void modifyResourceContent_shouldAddScript_whenUriEndsWithSlash() {
        String originalHtml = "<html><body><p>Hello world</p></body></html>";

        String modifiedHtml = contentModifyService.modifyResourceContent(URI, originalHtml);

        Assertions.assertTrue(modifiedHtml.contains("<html>"));
        Assertions.assertTrue(modifiedHtml.contains("<body>"));
        Assertions.assertTrue(modifiedHtml.contains("<p>Hello world</p>"));

        Assertions.assertTrue(modifiedHtml.contains("<script>"));
        Assertions.assertTrue(modifiedHtml.contains("MutationObserver"));
        Assertions.assertTrue(modifiedHtml.contains("modifyTextNodes"));
    }

    @Test
    void modifyResourceContent_shouldNotAddScript_whenUriDoesNotEndWithSlash() {
        String resourceUri = "/ai.svg";
        String originalHtml = "<html><body><p>Hello world</p></body></html>";

        String modifiedHtml = contentModifyService.modifyResourceContent(resourceUri, originalHtml);

        Assertions.assertEquals(originalHtml, modifiedHtml);
    }

    @Test
    void modifyResourceContent_shouldHandleEmptyHtml() {
        String modifiedHtml = contentModifyService.modifyResourceContent(URI, "");

        Assertions.assertTrue(modifiedHtml.isBlank());
    }
}
