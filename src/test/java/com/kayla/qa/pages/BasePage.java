package com.kayla.qa.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
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
            if (attempt == 1) {
                click(locator);
            } else {
                // Fallback: fire the element's click handler directly instead of simulating a mouse press.
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            }
            try {
                new WebDriverWait(driver, Duration.ofSeconds(6)).until(expectedEffect);
                return;
            } catch (TimeoutException e) {
                if (attempt == maxAttempts) throw e;
            }
        }
    }

    /**
     * Types into a field and checks the text lands AND stays. This site sometimes drops keystrokes
     * (or clears the form) while a page is still settling after a client-side navigation, so we
     * give it several spaced-out attempts. The first two press real keys; later ones set the value
     * through JavaScript and fire the "input" event so the page's own code notices the change.
     */
    protected void type(By locator, String text) {
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                WebElement field = visible(locator);
                field.clear();
                if (attempt <= 2) {
                    field.sendKeys(text);
                } else {
                    ((JavascriptExecutor) driver).executeScript(
                            "var setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;"
                                    + "setter.call(arguments[0], arguments[1]);"
                                    + "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));",
                            field, text);
                }
                if (valueHolds(locator, text)) return;
            } catch (StaleElementReferenceException e) {
                // The field was replaced mid-typing; look it up again and retry.
            }
        }
        throw new IllegalStateException("Could not type '" + text + "' into " + locator);
    }

    /** True if the field shows `text` and keeps showing it for about 750 ms. */
    private boolean valueHolds(By locator, String text) {
        try {
            new WebDriverWait(driver, Duration.ofMillis(750))
                    .pollingEvery(Duration.ofMillis(150))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> !text.equals(d.findElement(locator).getAttribute("value")));
            return false; // the value was missing or got wiped within the window
        } catch (TimeoutException e) {
            return true; // never wiped for the whole window
        }
    }

    protected boolean urlContains(String fragment) {
        return wait.until(ExpectedConditions.urlContains(fragment));
    }
}
