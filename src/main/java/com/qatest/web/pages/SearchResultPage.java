package com.qatest.web.pages;

import com.qatest.web.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class SearchResultPage extends BasePage {

    private static final By RESULT_ARTICLES = By.cssSelector("article");
    private static final By RESULT_TITLE = By.cssSelector(".entry-title a");
    private static final By NO_RESULTS = By.cssSelector(".no-results");

    public SearchResultPage(WebDriver driver) {
        super(driver);
        waitForSearchNavigation();
    }

    private void waitForSearchNavigation() {
        int timeout = ConfigManager.getInstance().getExplicitTimeout();
        new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.urlContains("?s="));
    }

    public int getResultCount() {
        waitForPageToLoad();
        if (driver.findElements(NO_RESULTS).size() > 0) {
            return 0;
        }
        return driver.findElements(RESULT_ARTICLES).size();
    }

    public boolean hasNoResults() {
        waitForPageToLoad();
        return driver.findElements(NO_RESULTS).size() > 0;
    }

    private void waitForPageToLoad() {
        int timeout = ConfigManager.getInstance().getExplicitTimeout();
        new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(d -> d.findElements(RESULT_ARTICLES).size() > 0
                        || d.findElements(NO_RESULTS).size() > 0);
    }

    public String getFirstResultTitle() {
        List<WebElement> titles = driver.findElements(RESULT_TITLE);
        if (titles.isEmpty()) {
            return "";
        }
        // Use textContent via JS for reliability (some themes lazy-render text)
        String text = titles.get(0).getText();
        if (text.isEmpty()) {
            text = (String) ((JavascriptExecutor) driver)
                    .executeScript("return arguments[0].textContent;", titles.get(0));
        }
        return text != null ? text.trim() : "";
    }

    public String getFirstResultLink() {
        List<WebElement> titles = driver.findElements(RESULT_TITLE);
        if (titles.isEmpty()) {
            return "";
        }
        return titles.get(0).getAttribute("href");
    }

    public List<String> getAllResultTitles() {
        return driver.findElements(RESULT_TITLE).stream()
                .map(WebElement::getText)
                .toList();
    }

    public boolean isPageLoaded() {
        try {
            waitForSearchNavigation();
            waitForPageToLoad();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
