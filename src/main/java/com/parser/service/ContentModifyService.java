package com.parser.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class ContentModifyService {

    public String modifyResourceContent(String uri, String body) {
        String modifiedContent = body;
        if (isResourceModify(uri) && !body.isBlank()) {
            modifiedContent = modifyContent(body);
        }
        return modifiedContent;
    }

    private String modifyContent(String content) {
        return modifyHtml(content);
    }

    /*
        Allow to modify data only on the pages,
     */
    private boolean isResourceModify(String uri) {
        return !uri.contains(".");
    }

    /*
        add a script which will add ™ to each 6-letter word. It was done because pages are dynamically recreated/rendered
     */
    private String modifyHtml(String html) {
        Document doc = Jsoup.parse(html);

        String observerScript = """
                <script>
                    function modifyTextNodes() {
                        const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
                        let node;
                        while (node = walker.nextNode()) {
                            if (node.nodeValue) {
                                node.nodeValue = node.nodeValue.replace(/\\b(\\w{6})(?!™)\\b/g, '$1™');
                            }
                        }
                    }
                
                    const observer = new MutationObserver(() => {
                        modifyTextNodes();
                    });
                
                    observer.observe(document.body, { childList: true, subtree: true });
                
                    modifyTextNodes();
                </script>
                """;

        doc.body().append(observerScript);
        return doc.outerHtml();
    }
}
