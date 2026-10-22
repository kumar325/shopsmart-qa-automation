package pages.mobile;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * Shipping address form.
 * Note: empty fields report their placeholder hint (e.g. "Rebecca Winter") as their text in the
 * accessibility tree, so never assert on a field's getText() to check what was typed.
 */
public class CheckoutAddressScreen extends MobileBasePage {

    private static final By FULL_NAME = id("fullNameET");
    private static final By ADDRESS_1 = id("address1ET");
    private static final By CITY = id("cityET");
    private static final By ZIP = id("zipET");
    private static final By COUNTRY = id("countryET");
    private static final By TO_PAYMENT = AppiumBy.accessibilityId("Saves user info for checkout");

    public CheckoutAddressScreen(AndroidDriver driver) {
        super(driver);
        visible(FULL_NAME);
    }

    public CheckoutAddressScreen fillIn(String fullName, String address, String city, String zip, String country) {
        type(FULL_NAME, fullName);
        type(ADDRESS_1, address);
        type(CITY, city);
        type(ZIP, zip);
        type(COUNTRY, country);
        hideKeyboard();
        return this;
    }

    public PaymentScreen continueToPayment() {
        tap(TO_PAYMENT);
        return new PaymentScreen(driver);
    }

    public CheckoutAddressScreen submitExpectingErrors() {
        tap(TO_PAYMENT);
        return this;
    }

    /** Field name as used in the resource-id: fullName, address1, city, zip, country. */
    public String getErrorFor(String field) {
        return textOf(id(field + "ErrorTV"));
    }
}
