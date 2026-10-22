package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CheckoutCompletePage extends BasePage {

    private static final By COMPLETE_HEADER = By.cssSelector("[data-test='complete-header']");
    private static final By COMPLETE_TEXT = By.cssSelector("[data-test='complete-text']");
    private static final By BACK_HOME = By.cssSelector("[data-test='back-to-products']");

    public CheckoutCompletePage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
    }

    public String getHeader() {
        return textOf(COMPLETE_HEADER);
    }

    public String getMessage() {
        return textOf(COMPLETE_TEXT);
    }

    public InventoryPage backHome() {
        click(BACK_HOME);
        return new InventoryPage(driver).waitUntilLoaded();
    }
}
