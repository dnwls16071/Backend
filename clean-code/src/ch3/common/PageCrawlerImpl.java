package ch3.common;

import java.util.HashMap;
import java.util.Map;

public class PageCrawlerImpl {

    private static final Map<String, WikiPage> pages = new HashMap<>();

    public static void addPage(String name, WikiPage page) {
        pages.put(name, page);
    }

    public static WikiPage getInheritedPage(String name, WikiPage context) {
        return pages.get(name);
    }

}