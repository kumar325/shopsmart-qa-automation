package listeners;

import driver.DriverManager;
import driver.MobileDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Saves a PNG to target/screenshots when a test fails. TestNG calls onTestFailure before
 * @AfterMethod, so the browser is still open at this point.
 */
public class ScreenshotListener implements ITestListener {

    private static final Path SCREENSHOT_DIR = Path.of("target", "screenshots");

    @Override
    public void onTestFailure(ITestResult result) {
        TakesScreenshot camera;
        if (DriverManager.hasDriver()) {
            camera = (TakesScreenshot) DriverManager.getDriver();
        } else if (MobileDriverManager.hasDriver()) {
            camera = MobileDriverManager.getDriver();
        } else {
            return;
        }
        try {
            byte[] png = camera.getScreenshotAs(OutputType.BYTES);
            Files.createDirectories(SCREENSHOT_DIR);
            String name = result.getTestClass().getRealClass().getSimpleName() + "_" + result.getName()
                    + "_" + System.currentTimeMillis() + ".png";
            Path file = SCREENSHOT_DIR.resolve(name);
            Files.write(file, png);
            System.out.println("Screenshot saved: " + file.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            // A screenshot problem must never hide the real test failure.
            System.err.println("Could not save screenshot: " + e.getMessage());
        }
    }
}
