package mobile;

import config.ConfigReader;
import driver.MobileDriverManager;
import io.appium.java_client.android.AndroidDriver;
import listeners.ScreenshotListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import pages.mobile.CatalogScreen;

/**
 * Prerequisites (see README): Android emulator running, Appium server on appium.server.url,
 * the APK in apps/, and MOBILE_PASSWORD set.
 *
 * Each test gets a new Appium session, which clears app data: tests start logged out with an
 * empty cart and never depend on each other.
 */
@Listeners(ScreenshotListener.class)
public abstract class MobileBaseTest {

    protected static final String LOCKED_OUT_USER = "alice@example.com";

    @BeforeMethod(alwaysRun = true)
    public void startApp() {
        MobileDriverManager.start();
    }

    @AfterMethod(alwaysRun = true)
    public void closeApp() {
        MobileDriverManager.quit();
    }

    protected AndroidDriver driver() {
        return MobileDriverManager.getDriver();
    }

    protected String username() {
        return ConfigReader.require("mobile.username");
    }

    protected String password() {
        return ConfigReader.require("mobile.password");
    }

    protected CatalogScreen catalog() {
        return new CatalogScreen(driver());
    }

    protected CatalogScreen loginFromMenu() {
        return catalog().openMenu().tapLogIn().loginAs(username(), password());
    }
}
