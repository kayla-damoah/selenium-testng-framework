package com.kayla.qa.tests;

import io.qameta.allure.Feature;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.kayla.qa.base.BaseTest;
import com.kayla.qa.pages.CartPage;
import com.kayla.qa.pages.InventoryPage;
import com.kayla.qa.pages.LoginPage;
import org.testng.annotations.Test;

/** Checks the cart contents and a complete purchase from start to finish. */
@Feature("Checkout")
public class CheckoutTests extends BaseTest {

    @Test
    public void cartShowsTheItemThatWasAdded() {
        InventoryPage inventory = new LoginPage(driver()).open().loginAs("standard_user", "secret_sauce");
        inventory.addToCart("sauce-labs-backpack");
        CartPage cart = inventory.openCart();
        assertEquals(cart.itemNames(), java.util.List.of("Sauce Labs Backpack"));
    }

    @Test(groups = "smoke")
    public void endToEndCheckoutShowsConfirmation() {
        InventoryPage inventory = new LoginPage(driver()).open().loginAs("standard_user", "secret_sauce");
        inventory.addToCart("sauce-labs-bolt-t-shirt");
        CartPage cart = inventory.openCart();
        cart.checkout("Test", "User", "07001");
        assertTrue(cart.confirmationText().contains("Thank you"));
    }
}
