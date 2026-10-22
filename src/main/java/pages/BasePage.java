package pages;

import config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Parent of every page object.
 *
 * - Wraps raw Selenium calls in explicit waits, so pages never need Thread.sleep().
 * - Holds the header (title, cart badge, burger menu) that appears on every logged-in page.
 */
public abstract class BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By CART_LINK = By.cssSelector("[data-test='shopping-cart-link']");
    private static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");
    private static final By MENU_BUTTON = By.id("react-burger-menu-btn");
    private static final By LOGOUT_LINK = By.cssSelector("[data-test='logout-sidebar-link']");

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds")));
    }

    // ---------- wait-wrapped helpers ----------

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
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

    /** Non-waiting check: use for elements that may legitimately be absent (e.g. an empty cart badge). */
    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    // ---------- shared header ----------

    public String getTitle() {
        return textOf(TITLE);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /** The badge is removed from the DOM (not just hidden) when the cart is empty, so absent means 0. */
    public int getCartBadgeCount() {
        return isPresent(CART_BADGE) ? Integer.parseInt(driver.findElement(CART_BADGE).getText()) : 0;
    }

    public CartPage openCart() {
        click(CART_LINK);
        return new CartPage(driver);
    }

    public LoginPage logout() {
        click(MENU_BUTTON);
        // The menu slides in with a CSS animation; elementToBeClickable waits until it has finished.
        click(LOGOUT_LINK);
        return new LoginPage(driver);
    }
}
