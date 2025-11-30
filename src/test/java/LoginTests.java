import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Scenario 2: User Login Authentication
 * Objective: Validating that registered users can log in securely and unauthorized access is prevented.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginTests {

    private static WebDriver webDriver;
    private static String baseUrl;
    private static String loginUrl;

    @BeforeAll
    public static void setUp() {
        System.setProperty("webdriver.chrome.driver",
                "/Users/lazarmatic/Downloads/chromedriver-mac-arm64/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");

        webDriver = new ChromeDriver(options);
        baseUrl = "https://www.bijelatehnika.com";
        loginUrl = baseUrl + "/prijava";
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("\n---- NEW TEST CASE ----");
    }

    @AfterAll
    public static void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    /**
     * Helper method to navigate to login page
     */
    private void navigateToLoginPage() throws InterruptedException {
        webDriver.get(loginUrl);
        Thread.sleep(2000);
        System.out.println("Navigated to login page: " + webDriver.getCurrentUrl());
    }

    /**
     * TC2.1: Login with wrong email
     * Expected: staying on the same page - prijava.
     */
    @Test
    @Order(1)
    public void testLoginWithWrongEmail() throws InterruptedException {

        navigateToLoginPage();

        WebElement loginForm = webDriver.findElement(By.id("loginForm"));

        String invalidEmail = "nonexistent" + System.currentTimeMillis() + "@test.com";
        String anyPassword = "SomePassword123!";

        WebElement emailField = loginForm.findElement(By.name("email"));
        emailField.clear();
        emailField.sendKeys(invalidEmail);
        System.out.println("Entered invalid email: " + invalidEmail);

        WebElement passwordField = loginForm.findElement(By.name("password"));
        passwordField.clear();
        passwordField.sendKeys(anyPassword);
        System.out.println("Entered password: "  + anyPassword);

        Thread.sleep(1000);

        WebElement loginButton = loginForm.findElement(
                By.xpath(".//button[@type='submit']")
        );
        loginButton.click();
        Thread.sleep(3000);

            String currentUrl = webDriver.getCurrentUrl();
            assertTrue(currentUrl.contains("prijava"),
                    "We should stay on login page after failed login");
            System.out.println("Test passed, we remained on login page = login failed");

    }

    /**
     * TC2.3: Login with wrong password
     * Expected: staying on the same page - prijava
     */
    @Test
    @Order(2)
    public void testLoginWithIncorrectPassword() throws InterruptedException {

        navigateToLoginPage();

        WebElement loginForm = webDriver.findElement(By.id("loginForm"));

        String validEmail = "zala@gmail.com";
        String wrongPassword = "wrongpassword123";

        WebElement emailField = loginForm.findElement(By.name("email"));
        emailField.clear();
        emailField.sendKeys(validEmail);
        System.out.println("Entered valid email: " + validEmail);

        WebElement passwordField = loginForm.findElement(By.name("password"));
        passwordField.clear();
        passwordField.sendKeys(wrongPassword);
        System.out.println("Entered incorrect password: " + wrongPassword);

        Thread.sleep(1000);

        WebElement loginButton = loginForm.findElement(
                By.xpath(".//button[@type='submit']")
        );
        loginButton.click();
        Thread.sleep(3000);

            String currentUrl = webDriver.getCurrentUrl();
            assertTrue(currentUrl.contains("prijava"),
                    "Should remain on login page after failed login");
            System.out.println("Test passed - we stayed on login page = login failed");

    }

    /**
     * TC2.4: Login with empty fields
     * Expected: staying on same page - prijava
     */
    @Test
    @Order(3)
    public void testLoginWithEmptyFields() throws InterruptedException {

        navigateToLoginPage();

        WebElement loginForm = webDriver.findElement(By.id("loginForm"));

        System.out.println("Trying to submit login form with empty fields...");

        WebElement loginButton = loginForm.findElement(
                By.xpath(".//button[@type='submit']")
        );
        loginButton.click();
        Thread.sleep(2000);

        String currentUrl = webDriver.getCurrentUrl();
        assertTrue(currentUrl.contains("prijava"),
                "Should remain on login page when fields are empty");
        System.out.println("Remained on login page: " + currentUrl);

        System.out.println("\nTesting with only email filled...");

        WebElement emailField = loginForm.findElement(By.name("email"));
        emailField.clear();
        emailField.sendKeys("zala@gmail.com");

        loginButton = loginForm.findElement(By.xpath(".//button[@type='submit']"));
        loginButton.click();
        Thread.sleep(2000);

        currentUrl = webDriver.getCurrentUrl();
        assertTrue(currentUrl.contains("prijava"),
                "Should remain on login page when password is missing");
        System.out.println("Remained on login page: " + currentUrl);

        System.out.println("\nTest passed - empty fields cannot log us in");
    }

    /**
     * TC2.5: Verifying forgot password link functionality
     * Expected: redirecting to password reset page
     */
    @Test
    @Order(4)
    public void testForgotPasswordLink() throws InterruptedException {

        navigateToLoginPage();

        WebElement forgotPasswordLink = null;

            try {
                forgotPasswordLink = webDriver.findElement(
                        By.xpath("//a[contains(@href, 'zaboravljena')]")
                );
                System.out.println("Found fogrot password link");
            } catch (Exception e2) {
                System.out.println("- No forgot password link found on page");
            }


        if (forgotPasswordLink != null) {
            assertTrue(forgotPasswordLink.isDisplayed(),
                    "Forgot password link should be visible");

            String linkText = forgotPasswordLink.getText();
            String linkHref = forgotPasswordLink.getAttribute("href");

            System.out.println("Link text: " + linkText);
            System.out.println("Link href: " + linkHref);

            forgotPasswordLink.click();
            Thread.sleep(3000);

            String currentUrl = webDriver.getCurrentUrl();
            System.out.println("Navigated to: " + currentUrl);

            assertTrue(currentUrl.contains("zaboravljena"),
                    "Should navigate to password reset page");

            System.out.println("Test passed -link for forgotten password is working");

        } else {
            System.out.println("Something went wrong, maybe page doesnt have forgot password link");
        }
    }

    /**
     * TC2.1: Login with correct email and passwrod
     * Expected: login successfull, redirected to korisnicki racun page
     */
    @Test
    @Order(6)
    public void testLoginWithValidCredentials() throws InterruptedException {
        navigateToLoginPage();

        WebElement loginForm = webDriver.findElement(By.id("loginForm"));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed");

        String validEmail = "zala@gmail.com";
        String validPassword = "zala123";

        WebElement emailField = loginForm.findElement(By.name("email"));
        emailField.clear();
        emailField.sendKeys(validEmail);
        System.out.println("Entered email: " + validEmail);

        WebElement passwordField = loginForm.findElement(By.name("password"));
        passwordField.clear();
        passwordField.sendKeys(validPassword);
        System.out.println("Entered password: " + validPassword);

        Thread.sleep(1000);

        WebElement loginButton = loginForm.findElement(
                By.xpath(".//button[@type='submit']")
        );
        loginButton.click();
        System.out.println("Clicked login button...");
        Thread.sleep(3000);

        String currentUrl = webDriver.getCurrentUrl();
        String expectedUrl = baseUrl + "/korisnicki-racun";

        System.out.println("Current URL after login: " + currentUrl);
        System.out.println("Expected URL: " + expectedUrl);

        assertTrue(currentUrl.contains("korisnicki-racun"),
                "It should redirect us to korisnicki racun page after successful login");

        System.out.println("Test passed, login successful - redirected to user account page");
    }

}