package driver;

import config.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.util.Map;

/**
 * Creates browser instances. Selenium Manager (built into Selenium 4.6+) downloads the matching
 * driver binary automatically, so no chromedriver.exe is committed or installed manually.
 */
public final class DriverFactory {

    /*
     * Chrome's Password Manager shows a "password found in a data breach" dialog after logging in
     * with SauceDemo's public password. That browser-level dialog swallows all mouse input, so
     * every click after login silently does nothing. Disabling leak detection is the fix;
     * the other two prefs stop the "Save password?" prompt as well.
     */
    private static final Map<String, Object> PASSWORD_MANAGER_OFF = Map.of(
            "credentials_enable_service", false,
            "profile.password_manager_enabled", false,
            "profile.password_manager_leak_detection", false);

    private DriverFactory() {
    }

    public static WebDriver create() {
        String browser = ConfigReader.require("browser").toLowerCase();
        boolean headless = ConfigReader.getBoolean("headless");

        return switch (browser) {
            case "chrome" -> new ChromeDriver(chromeOptions(headless));
            case "edge" -> new EdgeDriver(edgeOptions(headless));
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser + " (use chrome or edge)");
        };
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1280,900");
        options.setExperimentalOption("prefs", PASSWORD_MANAGER_OFF);
        return options;
    }

    private static EdgeOptions edgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1280,900");
        options.setExperimentalOption("prefs", PASSWORD_MANAGER_OFF);
        return options;
    }
}
