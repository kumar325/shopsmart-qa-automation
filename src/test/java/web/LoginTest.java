package web;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.InventoryPage;
import pages.LoginPage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LoginTest extends BaseTest {

    @Test(description = "Standard user lands on the Products page after login")
    public void validLoginOpensInventory() {
        InventoryPage inventory = loginAsStandardUser();

        assertTrue(inventory.getCurrentUrl().endsWith("/inventory.html"), "URL was " + inventory.getCurrentUrl());
        assertEquals(inventory.getTitle(), "Products");
        assertEquals(inventory.getProductNames().size(), 6, "Number of products shown");
    }

    /**
     * Each row: username, password (null = use the real password), expected error.
     * Messages were copied from the live site, not guessed.
     */
    @DataProvider(name = "invalidLogins")
    public Object[][] invalidLogins() {
        return new Object[][]{
                {LOCKED_OUT_USER, null, "Epic sadface: Sorry, this user has been locked out."},
                {STANDARD_USER, "wrong_password", "Epic sadface: Username and password do not match any user in this service"},
                {"unknown_user", null, "Epic sadface: Username and password do not match any user in this service"},
                {"", null, "Epic sadface: Username is required"},
                {STANDARD_USER, "", "Epic sadface: Password is required"},
        };
    }

    @Test(dataProvider = "invalidLogins", description = "Invalid credentials show the correct error and stay on login")
    public void invalidLoginShowsError(String username, String passwordOrNull, String expectedError) {
        String pwd = passwordOrNull == null ? password() : passwordOrNull;

        LoginPage login = openLoginPage().attemptLogin(username, pwd);

        assertEquals(login.getErrorMessage(), expectedError);
        assertTrue(login.isLoginButtonDisplayed(), "Should still be on the login page");
    }

    @Test(description = "Opening /inventory.html without logging in redirects to login with an error")
    public void inventoryRequiresLogin() {
        driver().get(config.ConfigReader.require("base.url") + "/inventory.html");
        LoginPage login = new LoginPage(driver());

        assertEquals(login.getErrorMessage(),
                "Epic sadface: You can only access '/inventory.html' when you are logged in.");
    }

    @Test(description = "Logout returns to the login page and protects inventory again")
    public void logoutReturnsToLogin() {
        LoginPage login = loginAsStandardUser().logout();

        assertTrue(login.isLoginButtonDisplayed());
        driver().navigate().back();
        assertEquals(login.getErrorMessage(),
                "Epic sadface: You can only access '/inventory.html' when you are logged in.",
                "Back button after logout must not show the inventory");
    }
}
