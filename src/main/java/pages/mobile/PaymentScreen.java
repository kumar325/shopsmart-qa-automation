package pages.mobile;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class PaymentScreen extends MobileBasePage {

    private static final By NAME = id("nameET");
    private static final By CARD_NUMBER = id("cardNumberET");
    private static final By EXPIRATION = id("expirationDateET");
    private static final By SECURITY_CODE = id("securityCodeET");
    private static final By REVIEW_ORDER =
            AppiumBy.accessibilityId("Saves payment info and launches screen to review checkout data");

    public PaymentScreen(AndroidDriver driver) {
        super(driver);
        visible(CARD_NUMBER);
    }

    /** Test card data only: this is a demo app and nothing is charged. */
    public PaymentScreen fillIn(String name, String cardNumber, String expiryMmYy, String securityCode) {
        type(NAME, name);
        type(CARD_NUMBER, cardNumber);
        type(EXPIRATION, expiryMmYy);
        type(SECURITY_CODE, securityCode);
        hideKeyboard();
        return this;
    }

    public ReviewOrderScreen reviewOrder() {
        tap(REVIEW_ORDER);
        return new ReviewOrderScreen(driver);
    }
}
