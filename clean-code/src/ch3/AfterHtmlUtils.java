package ch3;

import ch3.common.*;

public class AfterHtmlUtils {

    private PageData pageData;
    private boolean isSuite;
    private WikiPage testPage;
    private StringBuffer newPageContent;
    private PageCrawler pageCrawler;

    public static String renderPageWithSetupsAndTeardowns(PageData pageData, boolean isSuite) throws Exception {
        boolean isTestPage = pageData.hasAttribute("Test");
        if (isTestPage) {
            AfterHtmlUtils includer = new AfterHtmlUtils(pageData, isSuite);
            includer.render();
        }
        return pageData.getHtml();
    }

    private AfterHtmlUtils(PageData pageData, boolean isSuite) {
        this.pageData = pageData;
        this.isSuite = isSuite;
        this.testPage = pageData.getWikiPage();
        this.newPageContent = new StringBuffer();
        this.pageCrawler = testPage.getPageCrawler();
    }

    private void render() throws Exception {
        includeSetupPages();
        includePageContent();
        includeTeardownPages();
        updatePageContent();
    }

    private void includeSetupPages() throws Exception {
        if (isSuite) {
            includeSuiteSetupPage();
        }
        includeSetupPage();
    }

    private void includeSuiteSetupPage() throws Exception {
        includePage(SuiteResponder.SUITE_SETUP_NAME, "-setup");
    }

    private void includeSetupPage() throws Exception {
        includePage("SetUp", "-setup");
    }

    private void includePageContent() {
        newPageContent.append(pageData.getContent());
    }

    private void includeTeardownPages() throws Exception {
        includeTeardownPage();
        if (isSuite) {
            includeSuiteTeardownPage();
        }
    }

    private void includeTeardownPage() throws Exception {
        includePage("TearDown", "-teardown");
    }

    private void includeSuiteTeardownPage() throws Exception {
        includePage(SuiteResponder.SUITE_TEARDOWN_NAME, "-teardown");
    }

    private void includePage(String pageName, String mode) throws Exception {
        WikiPage page = PageCrawlerImpl.getInheritedPage(pageName, testPage);
        if (page != null) {
            WikiPagePath pagePath = pageCrawler.getFullPath(page);
            String pagePathName = PathParser.render(pagePath);
            newPageContent.append("!include ").append(mode).append(" .").append(pagePathName).append("\n");
        }
    }

    private void updatePageContent() {
        pageData.setContent(newPageContent.toString());
    }

}
