package com.kayla.qa.pages;

import io.qameta.allure.Step;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

/**
 * The product list shown after login: add/remove items, read the cart badge,
 * sort, and read prices. Methods that move to another screen return that screen's page object.
 */
public class InventoryPage extends BasePage {

    private static final By ITEMS = By.cssSelector(".inventory_item");
    private static final By PRICES = By.cssSelector(".inventory_item_price");
    private static final By CART_BADGE = By.cssSelector(".shopping_cart_badge");
    private static final By CART_LINK = By.cssSelector(".shopping_cart_link");
    private static final By SORT = By.cssSelector("[data-test='product-sort-container']");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return urlContains("inventory.html");
    }

    public int productCount() {
        visible(ITEMS);
        return driver.findElements(ITEMS).size();
    }

    /**
     * Clicks "Add to cart" and confirms the button flipped to "Remove". That flip is the page
     * telling us it finished updating; clickUntil retries if the click was ignored.
     */
    @Step("Add {0} to the cart")
    public void addToCart(String slug) {
        clickUntil(By.cssSelector("[data-test='add-to-cart-" + slug + "']"),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='remove-" + slug + "']")));
    }

    /** Mirror of addToCart: confirms the button flipped back to "Add to cart". */
    @Step("Remove {0} from the cart")
    public void removeFromCart(String slug) {
        clickUntil(By.cssSelector("[data-test='remove-" + slug + "']"),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='add-to-cart-" + slug + "']")));
    }

    public int cartCount() {
        List<WebElement> badge = driver.findElements(CART_BADGE);
        return badge.isEmpty() ? 0 : Integer.parseInt(badge.get(0).getText());
    }

    @Step("Sort products by {0}")
    public void sortBy(String value) {
        new Select(visible(SORT)).selectByValue(value);
    }

    public List<Double> prices() {
        visible(ITEMS);
        return driver.findElements(PRICES).stream()
                .map(e -> Double.parseDouble(e.getText().replace("$", "")))
                .toList();
    }

    @Step("Open the cart")
    public CartPage openCart() {
        clickUntil(CART_LINK, ExpectedConditions.urlContains("cart.html"));
        return new CartPage(driver);
    }
}
