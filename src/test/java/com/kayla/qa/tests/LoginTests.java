package com.kayla.qa.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.kayla.qa.base.BaseTest;
import com.kayla.qa.pages.LoginPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Checks that the right people get in and the wrong ones get a clear error. */
public class LoginTests extends BaseTest {

    @Test(groups = "smoke")
    public void validLoginReachesInventory() {
        assertTrue(new LoginPage(driver()).open().loginAs("standard_user", "secret_sauce").isLoaded());
    }

    @DataProvider(name = "badLogins")
    public Object[][] badLogins() {
        return new Object[][] {
            {"locked_out_user", "secret_sauce", "locked out"},
            {"standard_user", "wrong", "do not match"},
            {"", "secret_sauce", "Username is required"},
            {"standard_user", "", "Password is required"},
        };
    }

    @Test(dataProvider = "badLogins")
    public void invalidLoginShowsError(String user, String password, String expected) {
        LoginPage login = new LoginPage(driver()).open();
        login.login(user, password);
        assertTrue(login.errorMessage().contains(expected),
                "Expected error containing '" + expected + "' but was: " + login.errorMessage());
    }

    @Test
    public void unauthenticatedUserCannotOpenInventory() {
        driver().get("https://www.saucedemo.com/inventory.html");
        assertEquals(driver().getCurrentUrl(), "https://www.saucedemo.com/");
    }
}
