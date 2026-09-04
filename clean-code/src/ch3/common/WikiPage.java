package ch3.common;

public class WikiPage {

    private final String name;
    private PageCrawler pageCrawler;

    public WikiPage(String name) {
        this.name = name;
        this.pageCrawler = new PageCrawler(this);
    }

    public String getName() {
        return name;
    }

    public PageCrawler getPageCrawler() {
        return pageCrawler;
    }

}