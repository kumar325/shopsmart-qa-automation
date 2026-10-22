package web;

import config.ConfigReader;
import driver.DriverManager;
import listeners.ScreenshotListener;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import pages.InventoryPage;
import pages.LoginPage;

/**
 * Every web test gets a brand-new browser (fresh cookies + localStorage), so tests are independent
 * and can run in any order or in parallel.
 *
 * The listener is registered here rather than in testng-web.xml because "mvn test -Dtest=..."
 * bypasses the suite file, which silently disabled screenshots for single-test runs.
 */
@Listeners(ScreenshotListener.class)
public abstract class BaseTest {

    protected static final String STANDARD_USER = "standard_user";
    protected static final String LOCKED_OUT_USER = "locked_out_user";

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        DriverManager.start();
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        DriverManager.quit();
    }

    protected WebDriver driver() {
        return DriverManager.getDriver();
    }

    /** Read from the SAUCEDEMO_PASSWORD environment variable, never from source code. */
    protected String password() {
        return ConfigReader.require("saucedemo.password");
    }

    protected LoginPage openLoginPage() {
        return new LoginPage(driver()).open();
    }

    protected InventoryPage loginAsStandardUser() {
        return openLoginPage().loginAs(STANDARD_USER, password());
    }
}
