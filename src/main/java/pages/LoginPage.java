package pages;

import config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {

    private static final By USERNAME = By.cssSelector("[data-test='username']");
    private static final By PASSWORD = By.cssSelector("[data-test='password']");
    private static final By LOGIN_BUTTON = By.cssSelector("[data-test='login-button']");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(ConfigReader.require("base.url"));
        visible(LOGIN_BUTTON);
        return this;
    }

    /**
     * Happy path: returns the next page so tests can chain calls fluently.
     * Waits for either outcome so a rejected login fails immediately with SauceDemo's own message
     * instead of a generic 10-second TimeoutException.
     */
    public InventoryPage loginAs(String username, String password) {
        submit(username, password);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/inventory.html"),
                ExpectedConditions.visibilityOfElementLocated(ERROR)));
        if (isPresent(ERROR)) {
            throw new IllegalStateException("Login as '" + username + "' was rejected: " + getErrorMessage());
        }
        return new InventoryPage(driver).waitUntilLoaded();
    }

    /** Unhappy path: stays on the login page so the test can read the error. */
    public LoginPage attemptLogin(String username, String password) {
        submit(username, password);
        return this;
    }

    private void submit(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
    }

    public String getErrorMessage() {
        return textOf(ERROR);
    }

    public boolean isLoginButtonDisplayed() {
        return visible(LOGIN_BUTTON).isDisplayed();
    }
}
