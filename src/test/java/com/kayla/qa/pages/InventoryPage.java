package com.kayla.qa.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
     * Clicks "Add to cart", then waits for the button to flip to "Remove". That flip is the page
     * telling us it finished updating; without the wait, the next step can race the re-render.
     */
    public void addToCart(String slug) {
        click(By.cssSelector("[data-test='add-to-cart-" + slug + "']"));
        visible(By.cssSelector("[data-test='remove-" + slug + "']"));
    }

    /** Mirror of addToCart: waits for the button to flip back to "Add to cart". */
    public void removeFromCart(String slug) {
        click(By.cssSelector("[data-test='remove-" + slug + "']"));
        visible(By.cssSelector("[data-test='add-to-cart-" + slug + "']"));
    }

    public int cartCount() {
        List<WebElement> badge = driver.findElements(CART_BADGE);
        return badge.isEmpty() ? 0 : Integer.parseInt(badge.get(0).getText());
    }

    public void sortBy(String value) {
        new Select(visible(SORT)).selectByValue(value);
    }

    public List<Double> prices() {
        visible(ITEMS);
        return driver.findElements(PRICES).stream()
                .map(e -> Double.parseDouble(e.getText().replace("$", "")))
                .toList();
    }

    public CartPage openCart() {
        click(CART_LINK);
        return new CartPage(driver);
    }
}
