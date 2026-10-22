package pages.mobile;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.Money;

import java.math.BigDecimal;
import java.util.List;

public class CartScreen extends MobileBasePage {

    private static final By TITLE = id("productTV");
    private static final By EMPTY_TITLE = id("noItemTitleTV");
    private static final By ITEM_TITLES = id("titleTV");
    private static final By ITEMS_COUNT = id("itemsTV");
    private static final By TOTAL = id("totalPriceTV");
    private static final By REMOVE = AppiumBy.accessibilityId("Removes product from cart");
    private static final By CHECKOUT = AppiumBy.accessibilityId("Confirms products for checkout");

    public CartScreen(AndroidDriver driver) {
        super(driver);
        // The cart shows either the item list ("My Cart") or the empty state ("No Items").
        wait.until(ExpectedConditions.or(
                ExpectedConditions.textToBe(TITLE, "My Cart"),
                ExpectedConditions.visibilityOfElementLocated(EMPTY_TITLE)));
    }

    public boolean isEmpty() {
        return isPresent(EMPTY_TITLE);
    }

    public String getEmptyTitle() {
        return textOf(EMPTY_TITLE);
    }

    public List<String> getItemTitles() {
        return textsOf(ITEM_TITLES);
    }

    /** e.g. "2 Items" */
    public String getItemsCountText() {
        return textOf(ITEMS_COUNT);
    }

    public BigDecimal getTotal() {
        return Money.parse(textOf(TOTAL));
    }

    public CartScreen removeFirstItem() {
        tap(REMOVE);
        return new CartScreen(driver);
    }

    /** When nobody is logged in, checkout first asks for credentials. */
    public LoginScreen checkoutAsGuest() {
        tap(CHECKOUT);
        return new LoginScreen(driver);
    }

    public CheckoutAddressScreen checkoutAsLoggedInUser() {
        tap(CHECKOUT);
        return new CheckoutAddressScreen(driver);
    }
}
