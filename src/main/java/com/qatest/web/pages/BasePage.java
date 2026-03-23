package com.qatest.web.pages;

import com.qatest.web.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class BasePage {

    protected final WebDriver driver;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
    }

    protected WebElement waitAndFind(By locator) {
        return WaitUtils.waitForVisible(driver, locator);
    }

    protected WebElement waitAndClick(By locator) {
        WebElement element = WaitUtils.waitForClickable(driver, locator);
        element.click();
        return element;
    }

    protected void type(By locator, String text) {
        WebElement element = waitAndFind(locator);
        element.clear();
        element.sendKeys(text);
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
