package com.kayla.qa.tests;

import io.qameta.allure.Feature;

import static org.testng.Assert.assertEquals;

import com.kayla.qa.base.BaseTest;
import com.kayla.qa.pages.InventoryPage;
import com.kayla.qa.pages.LoginPage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.annotations.Test;

/** Checks the product list: how many items, cart count, and price sorting. */
@Feature("Inventory")
public class InventoryTests extends BaseTest {

    private InventoryPage loggedIn() {
        return new LoginPage(driver()).open().loginAs("standard_user", "secret_sauce");
    }

    @Test(groups = "smoke")
    public void inventoryListsSixProducts() {
        assertEquals(loggedIn().productCount(), 6);
    }

    @Test
    public void cartBadgeTracksAddsAndRemoves() {
        InventoryPage inventory = loggedIn();
        inventory.addToCart("sauce-labs-backpack");
        assertEquals(inventory.cartCount(), 1);
        inventory.addToCart("sauce-labs-bike-light");
        assertEquals(inventory.cartCount(), 2);
        inventory.removeFromCart("sauce-labs-backpack");
        assertEquals(inventory.cartCount(), 1);
    }

    @Test
    public void sortByPriceLowToHigh() {
        InventoryPage inventory = loggedIn();
        inventory.sortBy("lohi");
        List<Double> prices = inventory.prices();
        List<Double> sorted = new ArrayList<>(prices);
        Collections.sort(sorted);
        assertEquals(prices, sorted);
    }

    @Test
    public void sortByPriceHighToLow() {
        InventoryPage inventory = loggedIn();
        inventory.sortBy("hilo");
        List<Double> prices = inventory.prices();
        List<Double> sorted = new ArrayList<>(prices);
        sorted.sort(Collections.reverseOrder());
        assertEquals(prices, sorted);
    }
}
