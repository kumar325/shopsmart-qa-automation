package web;

import org.testng.annotations.Test;
import pages.CartPage;
import pages.InventoryPage;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CartTest extends BaseTest {

    private static final String BACKPACK = "Sauce Labs Backpack";
    private static final String BIKE_LIGHT = "Sauce Labs Bike Light";
    private static final String ONESIE = "Sauce Labs Onesie";

    @Test(description = "Cart badge is absent before anything is added")
    public void cartStartsEmpty() {
        InventoryPage inventory = loginAsStandardUser();

        assertEquals(inventory.getCartBadgeCount(), 0);
        assertTrue(inventory.openCart().getItemNames().isEmpty());
    }

    @Test(description = "Adding a product updates the badge and the button label")
    public void addSingleItem() {
        InventoryPage inventory = loginAsStandardUser().addToCart(BACKPACK);

        assertEquals(inventory.getCartBadgeCount(), 1);
        assertEquals(inventory.getButtonText(BACKPACK), "Remove");
        assertEquals(inventory.openCart().getItemNames(), List.of(BACKPACK));
    }

    @Test(description = "Multiple products all appear in the cart")
    public void addMultipleItems() {
        InventoryPage inventory = loginAsStandardUser()
                .addToCart(BACKPACK)
                .addToCart(BIKE_LIGHT)
                .addToCart(ONESIE);

        assertEquals(inventory.getCartBadgeCount(), 3);
        List<String> inCart = inventory.openCart().getItemNames();
        assertEquals(inCart.size(), 3);
        assertTrue(inCart.containsAll(List.of(BACKPACK, BIKE_LIGHT, ONESIE)), "Cart contained " + inCart);
    }

    @Test(description = "Removing from the inventory page decrements the badge")
    public void removeFromInventoryPage() {
        InventoryPage inventory = loginAsStandardUser()
                .addToCart(BACKPACK)
                .addToCart(BIKE_LIGHT)
                .removeFromCart(BACKPACK);

        assertEquals(inventory.getCartBadgeCount(), 1);
        assertEquals(inventory.getButtonText(BACKPACK), "Add to cart");
        assertEquals(inventory.openCart().getItemNames(), List.of(BIKE_LIGHT));
    }

    @Test(description = "Removing from the cart page removes the item and updates the badge")
    public void removeFromCartPage() {
        CartPage cart = loginAsStandardUser()
                .addToCart(BACKPACK)
                .addToCart(BIKE_LIGHT)
                .openCart()
                .removeItem(BACKPACK);

        assertEquals(cart.getItemNames(), List.of(BIKE_LIGHT));
        assertEquals(cart.getCartBadgeCount(), 1);
    }

    @Test(description = "Cart contents persist when continuing to shop")
    public void cartPersistsAfterContinueShopping() {
        InventoryPage inventory = loginAsStandardUser()
                .addToCart(BACKPACK)
                .openCart()
                .continueShopping();

        assertEquals(inventory.getCartBadgeCount(), 1);
        assertEquals(inventory.getButtonText(BACKPACK), "Remove");
    }
}
