package com.kayla.qa.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Explicit waits only; no Thread.sleep and no implicit waits. */
abstract class BasePage {

    protected final WebDriver driver;
    private final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /**
     * Clicks, then checks that the click had its effect; if not, clicks again (up to 3 tries).
     * CI showed that this site sometimes ignores a click made right after the cart updates, and
     * Selenium reports no error when that happens. Checking the outcome, not just the click,
     * makes the step reliable. If the effect never appears, the last TimeoutException is thrown.
     */
    protected void clickUntil(By locator, ExpectedCondition<?> expectedEffect) {
        int maxAttempts = 3;
        for (int attempt = 1; ; attempt++) {
            click(locator);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(6)).until(expectedEffect);
                return;
            } catch (TimeoutException e) {
                if (attempt == maxAttempts) throw e;
            }
        }
    }

    protected void type(By locator, String text) {
        WebElement field = visible(locator);
        field.clear();
        field.sendKeys(text);
    }

    protected boolean urlContains(String fragment) {
        return wait.until(ExpectedConditions.urlContains(fragment));
    }
}
