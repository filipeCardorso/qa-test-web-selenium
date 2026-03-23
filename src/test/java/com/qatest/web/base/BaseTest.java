package com.qatest.web.base;

import com.qatest.web.driver.DriverManager;
import com.qatest.web.pages.HomePage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseTest {

    protected HomePage homePage;

    @BeforeEach
    void setUp() {
        homePage = new HomePage(DriverManager.getDriver());
    }

    @AfterEach
    void tearDown() {
        DriverManager.quitDriver();
    }
}
