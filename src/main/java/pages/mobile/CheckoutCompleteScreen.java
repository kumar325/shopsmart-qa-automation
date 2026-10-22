package pages.mobile;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class CheckoutCompleteScreen extends MobileBasePage {

    private static final By HEADER = id("completeTV");
    private static final By THANK_YOU = id("thankYouTV");
    private static final By CONTINUE_SHOPPING = AppiumBy.accessibilityId("Tap to open catalog");

    public CheckoutCompleteScreen(AndroidDriver driver) {
        super(driver);
        visible(HEADER);
    }

    public String getHeader() {
        return textOf(HEADER);
    }

    public String getThankYouMessage() {
        return textOf(THANK_YOU);
    }

    /** The app empties the cart here (by design), not when the order is placed. */
    public CatalogScreen continueShopping() {
        tap(CONTINUE_SHOPPING);
        return new CatalogScreen(driver);
    }
}
