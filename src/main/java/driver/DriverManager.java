package driver;

import org.openqa.selenium.WebDriver;

/**
 * Holds one WebDriver per thread. When TestNG runs tests in parallel, each thread gets its own
 * browser, so tests never share (and corrupt) each other's session.
 */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void start() {
        DRIVER.set(DriverFactory.create());
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("No WebDriver for this thread. Was DriverManager.start() called?");
        }
        return driver;
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static void quit() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            // remove() prevents a stale driver leaking to the next test on a reused thread
            DRIVER.remove();
        }
    }
}
