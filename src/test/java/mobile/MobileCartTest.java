package mobile;

import io.appium.java_client.appmanagement.ApplicationState;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.Test;
import pages.mobile.CartScreen;
import pages.mobile.ProductScreen;

import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MobileCartTest extends MobileBaseTest {

    private static final String BACKPACK = "Sauce Labs Backpack";

    /**
     * BUG-MOB-01: "Sauce Labs Backpack (green)" crashes the app when opened (NullPointerException in
     * ProductCatalogFragment, an intentional demo bug). Quarantined in "known-defect".
     */
    private static final String CRASHING_PRODUCT = "Sauce Labs Backpack (green)";

    @Test(description = "Quantity 2 updates the badge, item count and cart total")
    public void addProductWithQuantityTwo() {
        ProductScreen product = catalog().openProduct(BACKPACK);
        BigDecimal unitPrice = product.getPrice();

        product.increaseQuantity().addToCart();
        assertEquals(product.getCartBadgeCount(), 2);

        CartScreen cart = product.openCart();
        assertEquals(cart.getItemTitles(), List.of(BACKPACK));
        assertEquals(cart.getItemsCountText(), "2 Items");
        assertEquals(cart.getTotal(), unitPrice.multiply(BigDecimal.valueOf(2)), "Total = unit price x 2");
    }

    @Test(description = "Removing the only item shows the empty cart state")
    public void removeItemEmptiesCart() {
        CartScreen cart = catalog().openProduct(BACKPACK).addToCart().openCart().removeFirstItem();

        assertTrue(cart.isEmpty(), "Cart should show the empty state");
        assertEquals(cart.getEmptyTitle(), "No Items");
        assertEquals(cart.getCartBadgeCount(), 0);
    }

    @Test(groups = "known-defect", description = "BUG-MOB-01: opening the green backpack shows its details")
    public void greenBackpackOpensProductDetails() {
        ProductScreen product;
        try {
            product = catalog().openProduct(CRASHING_PRODUCT);
        } catch (TimeoutException e) {
            // Turn a vague timeout into a clear diagnosis: is the app even still running?
            // Not getCurrentPackage(): after a crash that returns whatever app is now in front.
            ApplicationState state = driver().queryAppState("com.saucelabs.mydemoapp.android");
            throw new AssertionError("Product screen never appeared; app state is " + state
                    + " (anything other than RUNNING_IN_FOREGROUND means the app crashed or closed)", e);
        }

        assertEquals(product.getTitle(), CRASHING_PRODUCT);
    }
}
