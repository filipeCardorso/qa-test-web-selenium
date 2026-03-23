package com.qatest.web.pages;

import com.qatest.web.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HomePage extends BasePage {

    private static final By SEARCH_ICON = By.cssSelector(".astra-search-icon");
    private static final By SEARCH_FIELD = By.cssSelector(".search-field");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage navigate() {
        driver.get(ConfigManager.getInstance().getBaseUrl());
        return this;
    }

    public SearchResultPage searchFor(String term) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Activate the slide-search dropdown via JS (headless-safe)
        js.executeScript(
            "var container = document.querySelector('.ast-search-menu-icon');" +
            "container.classList.add('ast-dropdown-active');" +
            "var field = container.querySelector('.search-field');" +
            "field.style.width = '235px';" +
            "field.style.display = 'block';"
        );

        int timeout = ConfigManager.getInstance().getExplicitTimeout();
        WebElement field = new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.visibilityOfElementLocated(SEARCH_FIELD));
        field.clear();
        field.sendKeys(term);
        field.sendKeys(Keys.ENTER);
        return new SearchResultPage(driver);
    }
}
