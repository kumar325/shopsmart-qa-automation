package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class CartPage extends BasePage {

    private static final By CART_LIST = By.cssSelector("[data-test='cart-list']");
    private static final By ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By CHECKOUT_BUTTON = By.cssSelector("[data-test='checkout']");
    private static final By CONTINUE_SHOPPING = By.cssSelector("[data-test='continue-shopping']");

    public CartPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        visible(CART_LIST);
    }

    /** Uses findElements (no wait) because an empty cart legitimately has zero items. */
    public List<String> getItemNames() {
        return driver.findElements(ITEM_NAMES).stream().map(e -> e.getText()).toList();
    }

    public CartPage removeItem(String productName) {
        By removeButton = By.xpath("//div[@data-test='inventory-item'][.//div[@data-test='inventory-item-name' and normalize-space()='"
                + productName + "']]//button");
        click(removeButton);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(removeButton));
        return this;
    }

    public InventoryPage continueShopping() {
        click(CONTINUE_SHOPPING);
        return new InventoryPage(driver).waitUntilLoaded();
    }

    public CheckoutInformationPage checkout() {
        click(CHECKOUT_BUTTON);
        return new CheckoutInformationPage(driver);
    }
}
