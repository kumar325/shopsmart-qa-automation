package pages.mobile;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.Money;

import java.math.BigDecimal;

/** Product detail: price, quantity selector and Add to cart. */
public class ProductScreen extends MobileBasePage {

    private static final By TITLE = id("productTV");
    private static final By PRICE = id("priceTV");
    private static final By QUANTITY = id("noTV");
    private static final By PLUS = AppiumBy.accessibilityId("Increase item quantity");
    private static final By ADD_TO_CART = AppiumBy.accessibilityId("Tap to add product to cart");

    public ProductScreen(AndroidDriver driver) {
        super(driver);
        visible(ADD_TO_CART);
    }

    public String getTitle() {
        return textOf(TITLE);
    }

    /** UI shows "$ 29.99" (with a space); Money.parse handles both formats. */
    public BigDecimal getPrice() {
        return Money.parse(textOf(PRICE));
    }

    public int getQuantity() {
        return Integer.parseInt(textOf(QUANTITY));
    }

    public ProductScreen increaseQuantity() {
        String before = textOf(QUANTITY);
        tap(PLUS);
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(QUANTITY, before)));
        return this;
    }

    public ProductScreen addToCart() {
        int expected = getCartBadgeCount() + getQuantity();
        tap(ADD_TO_CART);
        wait.until(d -> getCartBadgeCount() == expected);
        return this;
    }
}
