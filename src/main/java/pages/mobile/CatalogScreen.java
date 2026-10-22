package pages.mobile;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

import java.util.List;

/** Home screen: the product grid. */
public class CatalogScreen extends MobileBasePage {

    private static final By PRODUCT_LIST = id("productRV");
    private static final By PRODUCT_TITLES = id("titleTV");

    public CatalogScreen(AndroidDriver driver) {
        super(driver);
        visible(PRODUCT_LIST);
    }

    /** Titles currently rendered on screen (the RecyclerView only renders visible rows). */
    public List<String> getVisibleProductTitles() {
        return textsOf(PRODUCT_TITLES);
    }

    /**
     * Opens a product by its visible title. The tap handler is on the image (productIV), which is a
     * sibling of the title in each grid cell, so XPath relates the two.
     */
    public ProductScreen openProduct(String title) {
        tap(By.xpath("//*[@resource-id='" + PKG + "titleTV' and @text='" + title + "']"
                + "/preceding-sibling::*[@resource-id='" + PKG + "productIV']"));
        return new ProductScreen(driver);
    }
}
