package web;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.InventoryPage;
import pages.SortOption;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Expected order is computed from the data on screen (sort a copy, compare), not hardcoded,
 * so the tests survive products being added or re-priced.
 */
public class SortingTest extends BaseTest {

    @Test(description = "Default order is Name (A to Z)")
    public void defaultSortIsNameAscending() {
        InventoryPage inventory = loginAsStandardUser();

        assertEquals(inventory.getActiveSortLabel(), "Name (A to Z)");
        List<String> names = inventory.getProductNames();
        assertEquals(names, sorted(names, Comparator.naturalOrder()));
    }

    @DataProvider(name = "nameSorts")
    public Object[][] nameSorts() {
        return new Object[][]{
                {SortOption.NAME_A_TO_Z, "Name (A to Z)", Comparator.<String>naturalOrder()},
                {SortOption.NAME_Z_TO_A, "Name (Z to A)", Comparator.<String>reverseOrder()},
        };
    }

    @Test(dataProvider = "nameSorts", description = "Sorting by name orders products alphabetically")
    public void sortByName(SortOption option, String label, Comparator<String> order) {
        // Start from the opposite order so the test proves the sort actually changed something.
        InventoryPage inventory = loginAsStandardUser()
                .sortBy(option == SortOption.NAME_A_TO_Z ? SortOption.NAME_Z_TO_A : SortOption.NAME_A_TO_Z)
                .sortBy(option);

        List<String> names = inventory.getProductNames();
        assertEquals(inventory.getActiveSortLabel(), label);
        assertEquals(names, sorted(names, order));
    }

    @DataProvider(name = "priceSorts")
    public Object[][] priceSorts() {
        return new Object[][]{
                {SortOption.PRICE_LOW_TO_HIGH, "Price (low to high)", Comparator.<BigDecimal>naturalOrder()},
                {SortOption.PRICE_HIGH_TO_LOW, "Price (high to low)", Comparator.<BigDecimal>reverseOrder()},
        };
    }

    @Test(dataProvider = "priceSorts", description = "Sorting by price orders products numerically")
    public void sortByPrice(SortOption option, String label, Comparator<BigDecimal> order) {
        InventoryPage inventory = loginAsStandardUser().sortBy(option);

        List<BigDecimal> prices = inventory.getProductPrices();
        assertEquals(inventory.getActiveSortLabel(), label);
        assertEquals(prices, sorted(prices, order));
    }

    private static <T> List<T> sorted(List<T> list, Comparator<? super T> order) {
        return list.stream().sorted(order).toList();
    }
}
