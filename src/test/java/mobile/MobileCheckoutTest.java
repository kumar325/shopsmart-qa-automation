package mobile;

import org.testng.annotations.Test;
import pages.mobile.CartScreen;
import pages.mobile.CatalogScreen;
import pages.mobile.CheckoutAddressScreen;
import pages.mobile.CheckoutCompleteScreen;
import pages.mobile.ReviewOrderScreen;

import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MobileCheckoutTest extends MobileBaseTest {

    private static final String ORANGE_BACKPACK = "Sauce Labs Backpack (orange)";

    @Test(description = "Guest checkout asks for login, then completes and empties the cart")
    public void guestCheckoutRequiresLoginAndCompletes() {
        CartScreen cart = catalog().openProduct(ORANGE_BACKPACK).addToCart().openCart();
        BigDecimal cartTotal = cart.getTotal();

        ReviewOrderScreen review = cart.checkoutAsGuest()
                .loginToContinueCheckout(username(), password())
                .fillIn("Test Shopper", "1 Main St", "New York", "10001", "USA")
                .continueToPayment()
                .fillIn("Test Shopper", "4111111111111111", "0330", "123")
                .reviewOrder();

        assertEquals(review.getDeliveryName(), "Test Shopper");
        assertEquals(review.getItemCountText(), "1 Items");
        BigDecimal shipping = review.getShippingCost();
        assertEquals(review.getTotal(), cartTotal.add(shipping), "Order total = cart total + shipping");

        CheckoutCompleteScreen complete = review.placeOrder();
        assertEquals(complete.getHeader(), "Checkout Complete");
        assertEquals(complete.getThankYouMessage(), "Thank you for your order");

        CatalogScreen catalog = complete.continueShopping();
        assertEquals(catalog.getCartBadgeCount(), 0, "Cart is emptied after Continue Shopping");
    }

    @Test(description = "Submitting an empty shipping form shows each required-field error")
    public void shippingAddressIsValidated() {
        loginFromMenu();
        CheckoutAddressScreen address = catalog().openProduct(ORANGE_BACKPACK).addToCart().openCart()
                .checkoutAsLoggedInUser()
                .submitExpectingErrors();

        assertEquals(address.getErrorFor("fullName"), "Please provide your full name.");
        assertEquals(address.getErrorFor("address1"), "Please provide your address.");
        assertEquals(address.getErrorFor("city"), "Please provide your city.");
        assertEquals(address.getErrorFor("zip"), "Please provide your zip");
    }

    /**
     * BUG-MOB-02: the country error is hardcoded in the layout as "Please provide your" (word missing).
     * Quarantined in "known-defect".
     */
    @Test(groups = "known-defect", description = "BUG-MOB-02: country error names the missing field")
    public void countryErrorMessageIsComplete() {
        loginFromMenu();
        CheckoutAddressScreen address = catalog().openProduct(ORANGE_BACKPACK).addToCart().openCart()
                .checkoutAsLoggedInUser()
                .submitExpectingErrors();

        assertTrue(address.getErrorFor("country").contains("country"),
                "Country error was: '" + address.getErrorFor("country") + "'");
    }
}
