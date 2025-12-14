import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Scenario 9: Session Management and Security
 * Objective: Validate session handling, timeout mechanisms, and security features
 * throughout the user journey.
 *
 * NOTE: This test suite documents security issues found on the website.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SessionManagementTest {

    private static WebDriver webDriver;
    private static WebDriver secondWebDriver;
    private static String baseUrl;
    private static WebDriverWait wait;

    private static final String TEST_EMAIL = "bakirneradin11@gmail.com";
    private static final String TEST_PASSWORD = "Bakir12345";

    @BeforeAll
    public static void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:/Users/Korisnik/Desktop/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");

        webDriver = new ChromeDriver(options);
        baseUrl = "https://bijelatehnika.com";
        wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
        if (secondWebDriver != null) {
            secondWebDriver.quit();
        }
    }

    private void performLogin(WebDriver driver) throws InterruptedException {
        driver.get(baseUrl + "/prijava");
        Thread.sleep(2000);

        try {
            WebElement emailField = driver.findElement(
                    By.xpath("//input[@placeholder='Unesite Vaš email']")
            );
            WebElement passwordField = driver.findElement(
                    By.xpath("//input[@placeholder='Unesite Vašu lozinku']")
            );

            emailField.clear();
            emailField.sendKeys(TEST_EMAIL);
            passwordField.clear();
            passwordField.sendKeys(TEST_PASSWORD);

            Thread.sleep(1000);

            WebElement loginButton = driver.findElement(
                    By.xpath("//button[contains(text(), 'Prijavi se')]")
            );
            loginButton.click();
            Thread.sleep(3000);

            System.out.println("✓ Login performed successfully");
        } catch (Exception e) {
            System.out.println("✗ Login failed: " + e.getMessage());
            throw e;
        }
    }

    private boolean isUserLoggedIn(WebDriver driver) {
        try {
            driver.findElement(
                    By.xpath("//*[contains(text(), 'Odjavi se') or contains(text(), 'Profil')]")
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getLoggedInUsername(WebDriver driver) {
        try {
            driver.get(baseUrl + "/korisnicki-racun");
            Thread.sleep(2000);

            WebElement welcomeMessage = driver.findElement(
                    By.xpath("//*[contains(text(), 'Pozdrav,')]")
            );
            return welcomeMessage.getText();
        } catch (Exception e) {
            return null;
        }
    }

    private void performLogout(WebDriver driver) throws InterruptedException {
        try {
            WebElement logoutLink = driver.findElement(
                    By.xpath("//a[contains(text(), 'Odjavi se') or contains(@href, 'odjava')]")
            );
            logoutLink.click();
            Thread.sleep(2000);
            System.out.println("✓ Logout performed successfully");
        } catch (Exception e) {
            System.out.println("✗ Logout failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * TC9.1: Verify session creation upon login
     * Expected: Session ID generated and stored
     */
    @Test
    @Order(1)
    public void testSessionCreationUponLogin() throws InterruptedException {
        System.out.println("\n=== TC9.1: Session Creation Upon Login ===");

        webDriver.get(baseUrl);
        Thread.sleep(2000);

        Set<Cookie> cookiesBeforeLogin = webDriver.manage().getCookies();
        System.out.println("Cookies before login: " + cookiesBeforeLogin.size());

        performLogin(webDriver);

        String currentUrl = webDriver.getCurrentUrl();
        System.out.println("Current URL after login: " + currentUrl);

        Set<Cookie> cookiesAfterLogin = webDriver.manage().getCookies();
        System.out.println("Cookies after login: " + cookiesAfterLogin.size());

        boolean sessionCookieFound = false;
        for (Cookie cookie : cookiesAfterLogin) {
            String cookieName = cookie.getName().toLowerCase();
            if (cookieName.contains("session") ||
                    cookieName.contains("phpsessid") ||
                    cookieName.contains("auth") ||
                    cookieName.contains("token")) {
                sessionCookieFound = true;
                System.out.println("✓ Session cookie found: " + cookie.getName());
                break;
            }
        }

        assertTrue(sessionCookieFound, "Session cookie should be created");
        assertTrue(isUserLoggedIn(webDriver), "User should be logged in");

        String username = getLoggedInUsername(webDriver);
        System.out.println("✓ Logged in as: " + username);
        assertNotNull(username, "Should be able to get username");

        System.out.println("✓ TC9.1 PASSED: Session created upon login\n");
    }

    /**
     * TC9.2: Verify session persistence across pages
     * Expected: User remains logged in during navigation
     */
    @Test
    @Order(2)
    public void testSessionPersistenceAcrossPages() throws InterruptedException {
        System.out.println("\n=== TC9.2: Session Persistence Across Pages ===");

        if (!isUserLoggedIn(webDriver)) {
            performLogin(webDriver);
        }

        String[] pagesToVisit = {
                baseUrl + "/korisnicki-racun",
                baseUrl + "/shop",
                baseUrl,
        };

        boolean allPagesAccessible = true;

        for (String page : pagesToVisit) {
            webDriver.get(page);
            Thread.sleep(2000);

            boolean loggedIn = isUserLoggedIn(webDriver);
            String currentUrl = webDriver.getCurrentUrl();

            System.out.println("Page: " + page);
            System.out.println("  Current URL: " + currentUrl);
            System.out.println("  Logged in: " + loggedIn);

            if (!loggedIn) {
                System.out.println("  ⚠ SECURITY ISSUE: Session lost on this page!");
                allPagesAccessible = false;
            }
        }

        if (allPagesAccessible) {
            System.out.println("✓ TC9.2 PASSED: Session persists across all pages\n");
        } else {
            System.out.println("⚠ TC9.2 WARNING: Session persistence issues detected\n");
            System.out.println("RECOMMENDATION: Implement proper session management across all pages.\n");
        }
    }

    /**
     * TC9.3: Test concurrent sessions (same user, different browsers)
     * Expected: Handled according to security policy
     */
    @Test
    @Order(3)
    public void testConcurrentSessions() throws InterruptedException {
        System.out.println("\n=== TC9.3: Concurrent Sessions Test ===");

        if (!isUserLoggedIn(webDriver)) {
            performLogin(webDriver);
        }

        assertTrue(isUserLoggedIn(webDriver), "First session should be active");
        System.out.println("✓ First session active");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");
        secondWebDriver = new ChromeDriver(options);

        try {
            performLogin(secondWebDriver);
            Thread.sleep(2000);

            boolean secondSessionActive = isUserLoggedIn(secondWebDriver);
            System.out.println("Second session active: " + secondSessionActive);

            webDriver.navigate().refresh();
            Thread.sleep(2000);
            boolean firstSessionStillActive = isUserLoggedIn(webDriver);

            System.out.println("First session after second login: " + firstSessionStillActive);

            if (firstSessionStillActive && secondSessionActive) {
                System.out.println("✓ Site allows concurrent sessions");
            } else if (!firstSessionStillActive && secondSessionActive) {
                System.out.println("✓ Site invalidates previous session (good security)");
            }

            System.out.println("✓ TC9.3 PASSED: Concurrent session behavior documented\n");
        } finally {
            if (secondWebDriver != null) {
                secondWebDriver.quit();
                secondWebDriver = null;
            }
        }
    }

    /**
     * TC9.4: Verify logout functionality clears session
     * Expected: Session destroyed, cannot access protected pages
     */
    @Test
    @Order(4)
    public void testLogoutClearsSession() throws InterruptedException {
        System.out.println("\n=== TC9.4: Logout Clears Session ===");

        if (!isUserLoggedIn(webDriver)) {
            performLogin(webDriver);
        }

        Set<Cookie> cookiesBeforeLogout = webDriver.manage().getCookies();
        System.out.println("Cookies before logout: " + cookiesBeforeLogout.size());

        performLogout(webDriver);

        Set<Cookie> cookiesAfterLogout = webDriver.manage().getCookies();
        System.out.println("Cookies after logout: " + cookiesAfterLogout.size());

        int cookiesRemoved = 0;
        for (Cookie cookie : cookiesBeforeLogout) {
            boolean stillExists = cookiesAfterLogout.stream()
                    .anyMatch(c -> c.getName().equals(cookie.getName()));
            if (!stillExists) {
                System.out.println("✓ Cookie removed: " + cookie.getName());
                cookiesRemoved++;
            }
        }

        if (cookiesRemoved == 0) {
            System.out.println("⚠ WARNING: No cookies were removed after logout!");
        }

        boolean loggedOut = !isUserLoggedIn(webDriver);
        System.out.println("User logged out: " + loggedOut);

        webDriver.get(baseUrl + "/korisnicki-racun");
        Thread.sleep(2000);

        String currentUrl = webDriver.getCurrentUrl();
        boolean stillLoggedIn = isUserLoggedIn(webDriver);

        System.out.println("After logout, accessing protected page:");
        System.out.println("  URL: " + currentUrl);
        System.out.println("  Still logged in: " + stillLoggedIn);

        if (stillLoggedIn || currentUrl.contains("korisnicki-racun")) {
            System.out.println("⚠ SECURITY ISSUE: Can still access protected content after logout!");
            System.out.println("RECOMMENDATION: Implement proper session destruction on logout.\n");
        } else {
            System.out.println("✓ TC9.4 PASSED: Logout properly clears session\n");
        }
    }

    /**
     * TC9.5: Test "Back" button after logout
     * Expected: Cannot access protected content
     */
    @Test
    @Order(5)
    public void testBackButtonAfterLogout() throws InterruptedException {
        System.out.println("\n=== TC9.5: Back Button After Logout ===");

        performLogin(webDriver);

        webDriver.get(baseUrl + "/korisnicki-racun");
        Thread.sleep(2000);
        System.out.println("✓ On protected page while logged in");

        performLogout(webDriver);
        System.out.println("✓ Logged out");

        webDriver.navigate().back();
        Thread.sleep(2000);

        String currentUrl = webDriver.getCurrentUrl();
        boolean stillLoggedIn = isUserLoggedIn(webDriver);

        System.out.println("After back button:");
        System.out.println("  URL: " + currentUrl);
        System.out.println("  Logged in: " + stillLoggedIn);

        if (stillLoggedIn || currentUrl.contains("korisnicki-racun")) {
            System.out.println("⚠ SECURITY ISSUE: Back button allows access to protected content!");
            System.out.println("RECOMMENDATION: Implement cache control headers to prevent back button access.\n");
        } else {
            System.out.println("✓ TC9.5 PASSED: Back button properly blocked\n");
        }
    }

    /**
     * TC9.6: Verify secure cookie attributes
     * Expected: Session cookies have proper security flags
     */
    @Test
    @Order(6)
    public void testSecureCookieAttributes() throws InterruptedException {
        System.out.println("\n=== TC9.6: Cookie Security Analysis ===");

        if (!isUserLoggedIn(webDriver)) {
            performLogin(webDriver);
        }

        Set<Cookie> cookies = webDriver.manage().getCookies();

        int secureCookies = 0;
        int httpOnlyCookies = 0;
        boolean hasSessionCookie = false;

        System.out.println("\nCookie Analysis:");
        for (Cookie cookie : cookies) {
            String cookieName = cookie.getName().toLowerCase();

            if (cookieName.contains("session") || cookieName.contains("phpsessid")) {
                hasSessionCookie = true;
                System.out.println("\n⚠ Session Cookie: " + cookie.getName());
                System.out.println("  - Secure: " + cookie.isSecure());
                System.out.println("  - HttpOnly: " + cookie.isHttpOnly());
                System.out.println("  - SameSite: " + cookie.getSameSite());

                if (!cookie.isSecure()) {
                    System.out.println("  ⚠ Missing Secure flag!");
                }
                if (!cookie.isHttpOnly()) {
                    System.out.println("  ⚠ Missing HttpOnly flag!");
                }
            }

            if (cookie.isSecure()) secureCookies++;
            if (cookie.isHttpOnly()) httpOnlyCookies++;
        }

        System.out.println("\n=== Summary ===");
        System.out.println("Total cookies: " + cookies.size());
        System.out.println("Secure cookies: " + secureCookies);
        System.out.println("HttpOnly cookies: " + httpOnlyCookies);

        if (baseUrl.startsWith("https") && (secureCookies == 0 || httpOnlyCookies == 0)) {
            System.out.println("\n⚠ SECURITY ISSUE: HTTPS site not using secure cookie attributes!");
            System.out.println("RECOMMENDATIONS:");
            System.out.println("  1. Add 'Secure' flag to all cookies (prevents transmission over HTTP)");
            System.out.println("  2. Add 'HttpOnly' flag to session cookies (prevents XSS attacks)");
            System.out.println("  3. Consider using 'SameSite=Strict' (prevents CSRF attacks)\n");
        } else {
            System.out.println("✓ TC9.6 PASSED: Cookie security attributes properly configured\n");
        }
    }

    /**
     * TC9.7: Test session validation on protected pages
     * Expected: Proper access control for authenticated pages
     */
    @Test
    @Order(7)
    public void testProtectedPageAccessControl() throws InterruptedException {
        System.out.println("\n=== TC9.7: Protected Page Access Control ===");

        // Try accessing without login
        webDriver.manage().deleteAllCookies();
        webDriver.get(baseUrl + "/korisnicki-racun");
        Thread.sleep(2000);

        String urlWithoutLogin = webDriver.getCurrentUrl();
        boolean canAccessWithoutLogin = isUserLoggedIn(webDriver);

        System.out.println("Accessing protected page without login:");
        System.out.println("  URL: " + urlWithoutLogin);
        System.out.println("  Can access: " + canAccessWithoutLogin);

        if (canAccessWithoutLogin || urlWithoutLogin.contains("korisnicki-racun")) {
            System.out.println("⚠ SECURITY ISSUE: Protected page accessible without authentication!");
            System.out.println("RECOMMENDATION: Implement server-side authentication checks.\n");
        } else {
            System.out.println("✓ Properly redirected to login page");
        }

        // Now login and try again
        performLogin(webDriver);
        webDriver.get(baseUrl + "/korisnicki-racun");
        Thread.sleep(2000);

        boolean canAccessWithLogin = isUserLoggedIn(webDriver);
        System.out.println("\nAfter login:");
        System.out.println("  Can access: " + canAccessWithLogin);

        if (canAccessWithLogin) {
            System.out.println("✓ Successfully accessed protected page after login");
        }

        System.out.println("\n✓ TC9.7 COMPLETED: Access control behavior documented\n");
    }
}