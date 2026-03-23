package com.qatest.web.pages;

import com.qatest.web.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {

    private static final By SEARCH_ICON = By.cssSelector(".ast-search-menu-icon");
    private static final By SEARCH_FIELD = By.cssSelector(".ast-search-menu-icon .search-field");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage navigate() {
        driver.get(ConfigManager.getInstance().getBaseUrl());
        return this;
    }

    public SearchResultPage searchFor(String term) {
        waitAndClick(SEARCH_ICON);
        type(SEARCH_FIELD, term);
        waitAndFind(SEARCH_FIELD).sendKeys(Keys.ENTER);
        return new SearchResultPage(driver);
    }
}
