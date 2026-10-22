package mobile;

import org.testng.annotations.Test;
import pages.mobile.LoginScreen;
import pages.mobile.MenuDrawer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Note for interviews: this demo app accepts ANY non-empty password for any user except the
 * locked-out one (verified in its source). So "valid login" proves the flow works, not that
 * credentials are checked; a wrong-password test would be meaningless here.
 */
public class MobileLoginTest extends MobileBaseTest {

    @Test(description = "Logging in from the menu replaces 'Log In' with 'Log Out'")
    public void validLoginShowsLogOutInMenu() {
        // loginFromMenu() taps "Log In", which only exists while logged out: that is the precondition.
        MenuDrawer menu = loginFromMenu().openMenu();

        assertTrue(menu.isLogOutShown(), "Log Out should be offered after login");
        assertFalse(menu.isLogInShown(), "Log In should no longer be offered");
    }

    @Test(description = "Empty username shows 'Username is required'")
    public void emptyUsernameIsRejected() {
        LoginScreen login = catalog().openMenu().tapLogIn().attemptLogin("", password());

        assertEquals(login.getUsernameError(), "Username is required");
    }

    @Test(description = "Empty password shows 'Enter Password'")
    public void emptyPasswordIsRejected() {
        LoginScreen login = catalog().openMenu().tapLogIn().attemptLogin(username(), "");

        assertEquals(login.getPasswordError(), "Enter Password");
    }

    @Test(description = "Locked-out user cannot log in")
    public void lockedOutUserIsRejected() {
        LoginScreen login = catalog().openMenu().tapLogIn().attemptLogin(LOCKED_OUT_USER, password());

        assertEquals(login.getPasswordError(), "Sorry this user has been locked out.");
    }
}
