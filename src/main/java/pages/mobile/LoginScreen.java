package pages.mobile;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class LoginScreen extends MobileBasePage {

    private static final By USERNAME = id("nameET");
    private static final By PASSWORD = id("passwordET");
    private static final By LOGIN_BUTTON = id("loginBtn");
    private static final By USERNAME_ERROR = id("nameErrorTV");
    private static final By PASSWORD_ERROR = id("passwordErrorTV");

    public LoginScreen(AndroidDriver driver) {
        super(driver);
        visible(LOGIN_BUTTON);
    }

    /** Login started from the menu returns to the catalog. */
    public CatalogScreen loginAs(String username, String password) {
        submit(username, password);
        return new CatalogScreen(driver);
    }

    /** Login started from checkout continues to the shipping address form. */
    public CheckoutAddressScreen loginToContinueCheckout(String username, String password) {
        submit(username, password);
        return new CheckoutAddressScreen(driver);
    }

    public LoginScreen attemptLogin(String username, String password) {
        submit(username, password);
        return this;
    }

    private void submit(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        hideKeyboard();
        tap(LOGIN_BUTTON);
    }

    public String getUsernameError() {
        return textOf(USERNAME_ERROR);
    }

    /** The app shows both "Enter Password" and the locked-out message in this field. */
    public String getPasswordError() {
        return textOf(PASSWORD_ERROR);
    }
}
