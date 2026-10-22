package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Checkout step one: customer name and postal code. */
public class CheckoutInformationPage extends BasePage {

    private static final By FIRST_NAME = By.cssSelector("[data-test='firstName']");
    private static final By LAST_NAME = By.cssSelector("[data-test='lastName']");
    private static final By POSTAL_CODE = By.cssSelector("[data-test='postalCode']");
    private static final By CONTINUE = By.cssSelector("[data-test='continue']");
    private static final By CANCEL = By.cssSelector("[data-test='cancel']");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    public CheckoutInformationPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
    }

    public CheckoutInformationPage fillIn(String firstName, String lastName, String postalCode) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(POSTAL_CODE, postalCode);
        return this;
    }

    public CheckoutOverviewPage continueToOverview() {
        click(CONTINUE);
        return new CheckoutOverviewPage(driver);
    }

    /** For validation tests: submit and stay on this page. */
    public CheckoutInformationPage submitExpectingError() {
        click(CONTINUE);
        return this;
    }

    public String getErrorMessage() {
        return textOf(ERROR);
    }

    public CartPage cancel() {
        click(CANCEL);
        return new CartPage(driver);
    }
}
