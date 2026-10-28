package com.kayla.qa.pages;

import io.qameta.allure.Step;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * The login screen. Knows WHERE things are (username box, password box, button)
 * and HOW to use them (login). Tests never touch those selectors directly.
 */
public class LoginPage extends BasePage {

    private static final String URL = "https://www.saucedemo.com/";
    private static final By USERNAME = By.cssSelector("[data-test='username']");
    private static final By PASSWORD = By.cssSelector("[data-test='password']");
    private static final By SUBMIT = By.cssSelector("[data-test='login-button']");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Step("Open the login page")
    public LoginPage open() {
        driver.get(URL);
        return this;
    }

    @Step("Log in as {0}")
    public void login(String user, String password) {
        type(USERNAME, user);
        type(PASSWORD, password);
        click(SUBMIT);
    }

    public InventoryPage loginAs(String user, String password) {
        login(user, password);
        return new InventoryPage(driver);
    }

    public String errorMessage() {
        return visible(ERROR).getText();
    }
}
