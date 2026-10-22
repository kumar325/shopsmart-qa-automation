package pages.mobile;

import config.ConfigReader;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Parent of every screen object in the Android app.
 *
 * Locator strategy, most to least preferred:
 *   1. accessibility id (content-desc), e.g. "View cart": stable and also checks accessibility
 *   2. resource-id, e.g. "nameET": stable, set by developers
 *   3. XPath only when relating two elements (e.g. the image next to a given title)
 */
public abstract class MobileBasePage {

    static final String PKG = "com.saucelabs.mydemoapp.android:id/";

    private static final By MENU = AppiumBy.accessibilityId("View menu");
    private static final By CART = AppiumBy.accessibilityId("View cart");
    private static final By CART_BADGE = id("cartTV");

    protected final AndroidDriver driver;
    protected final WebDriverWait wait;

    protected MobileBasePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("mobile.wait.seconds")));
    }

    static By id(String resourceId) {
        return AppiumBy.id(PKG + resourceId);
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void tap(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement field = visible(locator);
        field.clear();
        field.sendKeys(text);
    }

    protected String textOf(By locator) {
        return visible(locator).getText();
    }

    protected List<String> textsOf(By locator) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        return driver.findElements(locator).stream().map(WebElement::getText).toList();
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    /** The on-screen keyboard can cover buttons at the bottom of a form. */
    protected void hideKeyboard() {
        if (driver.isKeyboardShown()) {
            driver.hideKeyboard();
        }
    }

    /** Scrolls the first scrollable container until an element with this resource-id is on screen. */
    protected WebElement scrollTo(String resourceId) {
        return driver.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true))"
                        + ".scrollIntoView(new UiSelector().resourceId(\"" + PKG + resourceId + "\"))"));
    }

    // ---------- header shared by all screens ----------

    public MenuDrawer openMenu() {
        tap(MENU);
        return new MenuDrawer(driver);
    }

    public CartScreen openCart() {
        tap(CART);
        return new CartScreen(driver);
    }

    /** cartTV is only rendered when the cart has items, so absent means 0. */
    public int getCartBadgeCount() {
        return isPresent(CART_BADGE) ? Integer.parseInt(driver.findElement(CART_BADGE).getText()) : 0;
    }
}
