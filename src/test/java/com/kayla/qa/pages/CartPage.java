package com.kayla.qa.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** The cart and the checkout form. Ends on the order-confirmation message. */
public class CartPage extends BasePage {

    private static final By ITEM_NAMES = By.cssSelector(".inventory_item_name");
    private static final By CHECKOUT = By.cssSelector("[data-test='checkout']");
    private static final By FIRST_NAME = By.cssSelector("[data-test='firstName']");
    private static final By LAST_NAME = By.cssSelector("[data-test='lastName']");
    private static final By ZIP = By.cssSelector("[data-test='postalCode']");
    private static final By CONTINUE = By.cssSelector("[data-test='continue']");
    private static final By FINISH = By.cssSelector("[data-test='finish']");
    private static final By CONFIRMATION = By.cssSelector("[data-test='complete-header']");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public List<String> itemNames() {
        urlContains("cart.html");
        return driver.findElements(ITEM_NAMES).stream().map(e -> e.getText()).toList();
    }

    public void checkout(String first, String last, String zip) {
        clickUntil(CHECKOUT, ExpectedConditions.visibilityOfElementLocated(FIRST_NAME));
        type(FIRST_NAME, first);
        type(LAST_NAME, last);
        type(ZIP, zip);
        clickUntil(CONTINUE, ExpectedConditions.urlContains("checkout-step-two"));
        clickUntil(FINISH, ExpectedConditions.visibilityOfElementLocated(CONFIRMATION));
    }

    public String confirmationText() {
        return visible(CONFIRMATION).getText();
    }
}
