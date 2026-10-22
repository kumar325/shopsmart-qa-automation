package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.math.BigDecimal;
import java.util.List;

public class InventoryPage extends BasePage {

    private static final By INVENTORY_LIST = By.cssSelector("[data-test='inventory-list']");
    private static final By ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By ITEM_PRICES = By.cssSelector("[data-test='inventory-item-price']");
    private static final By SORT_DROPDOWN = By.cssSelector("[data-test='product-sort-container']");
    private static final By ACTIVE_SORT = By.cssSelector("[data-test='active-option']");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    InventoryPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        visible(INVENTORY_LIST);
        return this;
    }

    public List<String> getProductNames() {
        return textsOf(ITEM_NAMES);
    }

    public List<BigDecimal> getProductPrices() {
        return textsOf(ITEM_PRICES).stream().map(Money::parse).toList();
    }

    public InventoryPage sortBy(SortOption option) {
        new Select(visible(SORT_DROPDOWN)).selectByValue(option.value());
        return this;
    }

    public String getActiveSortLabel() {
        return textOf(ACTIVE_SORT);
    }

    /**
     * Finds the product card by its visible name, then the button inside that card.
     * Scoping to the card is more robust than building ids like "add-to-cart-sauce-labs-backpack".
     */
    private By buttonInCard(String productName) {
        return By.xpath("//div[@data-test='inventory-item'][.//div[@data-test='inventory-item-name' and normalize-space()='"
                + productName + "']]//button");
    }

    public InventoryPage addToCart(String productName) {
        click(buttonInCard(productName));
        wait.until(ExpectedConditions.textToBe(buttonInCard(productName), "Remove"));
        return this;
    }

    public InventoryPage removeFromCart(String productName) {
        click(buttonInCard(productName));
        wait.until(ExpectedConditions.textToBe(buttonInCard(productName), "Add to cart"));
        return this;
    }

    public String getButtonText(String productName) {
        return textOf(buttonInCard(productName));
    }
}
