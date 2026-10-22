package web;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.CheckoutCompletePage;
import pages.CheckoutInformationPage;
import pages.CheckoutOverviewPage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class CheckoutTest extends BaseTest {

    private static final String BACKPACK = "Sauce Labs Backpack";
    private static final String FLEECE_JACKET = "Sauce Labs Fleece Jacket";

    /** Observed on the live site: $29.99 item -> $2.40 tax. */
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    @Test(description = "Full purchase: totals add up and the order is confirmed")
    public void completePurchaseWithTwoItems() {
        CheckoutOverviewPage overview = loginAsStandardUser()
                .addToCart(BACKPACK)
                .addToCart(FLEECE_JACKET)
                .openCart()
                .checkout()
                .fillIn("Test", "Shopper", "10001")
                .continueToOverview();

        assertEquals(overview.getItemNames(), List.of(BACKPACK, FLEECE_JACKET));

        BigDecimal sumOfItems = overview.getItemPrices().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expectedTax = sumOfItems.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);

        assertEquals(overview.getItemTotal(), sumOfItems, "Item total = sum of line prices");
        assertEquals(overview.getTax(), expectedTax, "Tax = 8% of item total");
        assertEquals(overview.getTotal(), sumOfItems.add(overview.getTax()), "Total = item total + tax");
        assertEquals(overview.getPaymentInfo(), "SauceCard #31337");
        assertEquals(overview.getShippingInfo(), "Free Pony Express Delivery!");

        CheckoutCompletePage complete = overview.finish();

        assertEquals(complete.getTitle(), "Checkout: Complete!");
        assertEquals(complete.getHeader(), "Thank you for your order!");
        assertEquals(complete.getCartBadgeCount(), 0, "Cart is emptied after the order");
    }

    @DataProvider(name = "missingFields")
    public Object[][] missingFields() {
        return new Object[][]{
                {"", "Shopper", "10001", "Error: First Name is required"},
                {"Test", "", "10001", "Error: Last Name is required"},
                {"Test", "Shopper", "", "Error: Postal Code is required"},
        };
    }

    @Test(dataProvider = "missingFields", description = "Each required checkout field is validated")
    public void checkoutRequiresAllFields(String first, String last, String zip, String expectedError) {
        CheckoutInformationPage info = loginAsStandardUser()
                .addToCart(BACKPACK)
                .openCart()
                .checkout()
                .fillIn(first, last, zip)
                .submitExpectingError();

        assertEquals(info.getErrorMessage(), expectedError);
    }

    @Test(description = "Cancelling checkout returns to the cart with items intact")
    public void cancelCheckoutReturnsToCart() {
        List<String> items = loginAsStandardUser()
                .addToCart(BACKPACK)
                .openCart()
                .checkout()
                .cancel()
                .getItemNames();

        assertEquals(items, List.of(BACKPACK));
    }
}
