package pages.mobile;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/** Side menu opened from the header's "View menu" button. */
public class MenuDrawer extends MobileBasePage {

    private static final By MENU_LIST = id("menuRV");
    private static final By LOGIN_ITEM = AppiumBy.accessibilityId("Login Menu Item");
    private static final By LOGOUT_ITEM = AppiumBy.accessibilityId("Logout Menu Item");

    public MenuDrawer(AndroidDriver driver) {
        super(driver);
        visible(MENU_LIST);
    }

    public LoginScreen tapLogIn() {
        tap(LOGIN_ITEM);
        return new LoginScreen(driver);
    }

    /** "Log Out" replaces "Log In" only when a user is logged in. */
    public boolean isLogOutShown() {
        return isPresent(LOGOUT_ITEM);
    }

    public boolean isLogInShown() {
        return isPresent(LOGIN_ITEM);
    }
}
