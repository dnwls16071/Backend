package ch3.common;

public class PageCrawler {

    private final WikiPage rootPage;

    public PageCrawler(WikiPage rootPage) {
        this.rootPage = rootPage;
    }

    public WikiPagePath getFullPath(WikiPage page) {
        return new WikiPagePath(rootPage.getName() + "." + page.getName());
    }

}