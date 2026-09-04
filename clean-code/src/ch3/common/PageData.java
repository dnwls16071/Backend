package ch3.common;

import java.util.HashSet;
import java.util.Set;

public class PageData {

    private final WikiPage wikiPage;
    private String content;
    private final Set<String> attributes = new HashSet<>();

    public PageData(WikiPage wikiPage, String content) {
        this.wikiPage = wikiPage;
        this.content = content;
    }

    public WikiPage getWikiPage() {
        return wikiPage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean hasAttribute(String attribute) {
        return attributes.contains(attribute);
    }

    public void setAttribute(String attribute) {
        attributes.add(attribute);
    }

    public String getHtml() {
        return "<html><body>" + content + "</body></html>";
    }

}