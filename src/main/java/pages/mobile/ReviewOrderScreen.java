package pages.mobile;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import pages.Money;

import java.math.BigDecimal;

public class ReviewOrderScreen extends MobileBasePage {

    private static final By ORDER_LIST = id("placeOrderRV");
    private static final By FULL_NAME = id("fullNameTV");
    private static final By ITEM_COUNT = id("itemNumberTV");
    private static final By TOTAL = id("totalAmountTV");
    private static final By PLACE_ORDER = AppiumBy.accessibilityId("Completes the process of checkout");

    public ReviewOrderScreen(AndroidDriver driver) {
        super(driver);
        visible(ORDER_LIST);
    }

    public String getDeliveryName() {
        return textOf(FULL_NAME);
    }

    public String getItemCountText() {
        return textOf(ITEM_COUNT);
    }

    public BigDecimal getTotal() {
        return Money.parse(textOf(TOTAL));
    }

    /** The delivery cost line sits below the fold, so scroll it into view first. */
    public BigDecimal getShippingCost() {
        return Money.parse(scrollTo("amountTV").getText());
    }

    public CheckoutCompleteScreen placeOrder() {
        tap(PLACE_ORDER);
        return new CheckoutCompleteScreen(driver);
    }
}
