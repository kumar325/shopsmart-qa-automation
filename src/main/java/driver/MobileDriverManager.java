package driver;

import config.ConfigReader;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * One AndroidDriver (Appium session) per thread, mirroring DriverManager for the web.
 *
 * A new session per test reinstalls nothing (the APK is only installed when missing) but does
 * clear the app's data, so every test starts logged out with an empty cart.
 */
public final class MobileDriverManager {

    private static final ThreadLocal<AndroidDriver> DRIVER = new ThreadLocal<>();

    private MobileDriverManager() {
    }

    public static void start() {
        Path apk = Path.of(ConfigReader.require("android.app.path")).toAbsolutePath();
        if (!Files.exists(apk)) {
            throw new IllegalStateException("App not found at " + apk + ". Download it with: "
                    + "gh release download 2.3.0 -R saucelabs/my-demo-app-android -p 'mda-2.3.0-27.apk' -D apps");
        }

        UiAutomator2Options options = new UiAutomator2Options()
                .setApp(apk.toString())
                .setAutoGrantPermissions(true)
                .setNewCommandTimeout(Duration.ofSeconds(120));

        AndroidDriver driver;
        try {
            driver = new AndroidDriver(URI.create(ConfigReader.require("appium.server.url")).toURL(), options);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid appium.server.url", e);
        }
        DRIVER.set(driver);
        dismissSystemAnrDialog(driver);
    }

    /**
     * A freshly booted emulator can show "Process system isn't responding". The dialog stays on top
     * of the app until dismissed, so every locator would fail with a misleading NoSuchElement.
     * Tapping "Wait" is safe: it only lets the slow system process continue.
     */
    private static void dismissSystemAnrDialog(AndroidDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        List<WebElement> wait = driver.findElements(AppiumBy.id("android:id/aerr_wait"));
        if (!wait.isEmpty()) {
            System.out.println("[MobileDriverManager] Dismissed Android 'isn't responding' system dialog.");
            wait.get(0).click();
        }
    }

    public static AndroidDriver getDriver() {
        AndroidDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("No AndroidDriver for this thread. Was MobileDriverManager.start() called?");
        }
        return driver;
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static void quit() {
        AndroidDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}
