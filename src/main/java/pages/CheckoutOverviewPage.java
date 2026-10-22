package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.math.BigDecimal;
import java.util.List;

/** Checkout step two: order summary with item total, tax and total. */
public class CheckoutOverviewPage extends BasePage {

    private static final By ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By ITEM_PRICES = By.cssSelector("[data-test='inventory-item-price']");
    private static final By SUBTOTAL = By.cssSelector("[data-test='subtotal-label']");
    private static final By TAX = By.cssSelector("[data-test='tax-label']");
    private static final By TOTAL = By.cssSelector("[data-test='total-label']");
    private static final By PAYMENT_INFO = By.cssSelector("[data-test='payment-info-value']");
    private static final By SHIPPING_INFO = By.cssSelector("[data-test='shipping-info-value']");
    private static final By FINISH = By.cssSelector("[data-test='finish']");

    public CheckoutOverviewPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
    }

    public List<String> getItemNames() {
        return textsOf(ITEM_NAMES);
    }

    public List<BigDecimal> getItemPrices() {
        return textsOf(ITEM_PRICES).stream().map(Money::parse).toList();
    }

    public BigDecimal getItemTotal() {
        return Money.parse(textOf(SUBTOTAL));
    }

    public BigDecimal getTax() {
        return Money.parse(textOf(TAX));
    }

    public BigDecimal getTotal() {
        return Money.parse(textOf(TOTAL));
    }

    public String getPaymentInfo() {
        return textOf(PAYMENT_INFO);
    }

    public String getShippingInfo() {
        return textOf(SHIPPING_INFO);
    }

    public CheckoutCompletePage finish() {
        click(FINISH);
        return new CheckoutCompletePage(driver);
    }
}
