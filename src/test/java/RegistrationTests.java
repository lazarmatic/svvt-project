import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Scenario 1: User Registration Functionality
 * Objective: Verifying that new users can successfully register on the website with valid credentials,
 * and that they cannot register with empty fields, or invalid email formats,already registered emails..
 */
public class RegistrationTests {

    private static WebDriver webDriver;
    private static String baseUrl;
    private static String loginRegisterUrl;

    @BeforeAll
    public static void setUp() {
        // Setting the path to my chromedriver
        System.setProperty("webdriver.chrome.driver",
                "/Users/lazarmatic/Downloads/chromedriver-mac-arm64/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");

        webDriver = new ChromeDriver(options);
        baseUrl = "https://www.bijelatehnika.com";
        loginRegisterUrl = baseUrl + "/prijava";
    }

    @BeforeEach
    public void setUpEach() {
        System.out.println("\n---- NEW TEST CASE ----");
    }

    @AfterAll
    public static void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    /**
     * TC1.1: Registering with valid data (first_name, last_name, email, password, phone number)
     * Expected: Registered successfully, confirmation message/popup/alert displayed
     */
    @Test
    public void testRegisterWithValidData() throws InterruptedException {

        // Navigating to registration page
        webDriver.get(loginRegisterUrl);
        Thread.sleep(500);
        System.out.println("Navigated to login/registration page: " + webDriver.getCurrentUrl());

        // Generating unique email
        String timestamp = String.valueOf(System.currentTimeMillis());
        String testEmail = "testuser" + timestamp + "@test.com";

        // Filling registration form
        WebElement registerForm = webDriver.findElement(By.id("registerForm"));
        assertTrue(registerForm.isDisplayed(), "Registration form should be displayed");

        registerForm.findElement(By.name("first_name")).sendKeys("Test");
        registerForm.findElement(By.name("last_name")).sendKeys("Korisnik");
        registerForm.findElement(By.name("email")).sendKeys(testEmail);
        registerForm.findElement(By.name("password")).sendKeys("TestLozinka123!");
        registerForm.findElement(By.name("phone_number")).sendKeys("061234567");

        // Submitting the form
        registerForm.findElement(By.xpath(".//button[@type='submit']")).click();
        System.out.println("Clicking registration button...");
        Thread.sleep(500); // waiting for popup to appear

        // Check if swal-2 popup is shown
        WebElement body = webDriver.findElement(By.tagName("body"));
        String bodyClasses = body.getAttribute("class");
        System.out.println("Body classes after submission: " + bodyClasses);

        boolean isPopupShown = bodyClasses.contains("swal2-shown");
        assertTrue(isPopupShown, "Popup should be displayed after registration");

        // Extracting popup text
        WebElement popupTextElement = webDriver.findElement(
                By.cssSelector(".swal2-popup .swal2-html-container")
        );
        String popupText = popupTextElement.getText();
        System.out.println("Popup text: " + popupText);

        // Proving based on text content
        boolean registrationSuccess = popupText.toLowerCase().contains("uspješno");
        assertTrue(registrationSuccess, "Registration should be successful (popup contains 'uspješno')");

        System.out.println("Registration test passed - success popup is found with text: " + popupText);
    }


    /**
     * TC1.2: Registering with existing email address
     * Expected: Error message "Doslo je do greske" or similar
     */
    @Test
    public void testRegisterWithExistingEmail() throws InterruptedException {

        // Navigating to registration page
        webDriver.get(loginRegisterUrl);
        Thread.sleep(500);

        // Finding the registration form
        WebElement registerForm = webDriver.findElement(By.id("registerForm"));
        assertTrue(registerForm.isDisplayed(), "Registration form should be displayed");

        // Filling form with an email that already exists
        String existingEmail = "asdasd@gmail.com";

        registerForm.findElement(By.name("first_name")).sendKeys("Test");
        registerForm.findElement(By.name("last_name")).sendKeys("Korisnik");
        registerForm.findElement(By.name("email")).sendKeys(existingEmail);
        registerForm.findElement(By.name("password")).sendKeys("TestLozinka123!");
        registerForm.findElement(By.name("phone_number")).sendKeys("061234567");

        // Submitting the form
        registerForm.findElement(By.xpath(".//button[@type='submit']")).click();
        System.out.println("Clicking registration button...");
        Thread.sleep(500); // waiting for popup

        // Checkng if swal-2 popup is shown
        WebElement body = webDriver.findElement(By.tagName("body"));
        String bodyClasses = body.getAttribute("class");
        assertTrue(bodyClasses.contains("swal2-shown"), "Popup should be displayed after registration attempt");

        // Extractinc popup text
        WebElement popupTextElement = webDriver.findElement(
                By.cssSelector(".swal2-popup .swal2-html-container")
        );
        String popupText = popupTextElement.getText();
        System.out.println("Popup text: " + popupText);

        // Proving that it contains an error message
        boolean isError = popupText.toLowerCase().contains("došlo je do greške");
        assertTrue(isError, "Error popup should tell that is existing email");

        System.out.println("Existing email test passed - error popup detected with text: " + popupText);
    }

    /**
     * TC1.3: Registering with invalid email format
     * Expected: Email validation error message
     */
    @Test
    public void testRegisterWithInvalidEmailFormat() throws InterruptedException {

        // Navigating to registration page
        webDriver.get(loginRegisterUrl);
        Thread.sleep(500);

        // Finding the registration form.
        WebElement registerForm = webDriver.findElement(By.id("registerForm"));
        assertTrue(registerForm.isDisplayed(), "Registartion form should be displayed");

        // Filling the form with invalid format of email
        String invalidEmail = "asdasdgmail.com";

        registerForm.findElement(By.name("first_name")).sendKeys("Test");
        registerForm.findElement(By.name("last_name")).sendKeys("Korisnik");
        registerForm.findElement(By.name("email")).sendKeys(invalidEmail);
        registerForm.findElement(By.name("password")).sendKeys("TestLozinka123!");
        registerForm.findElement(By.name("phone_number")).sendKeys("061234567");

        // Submitting the form
        registerForm.findElement(By.xpath(".//button[@type='submit']")).click();
        System.out.println("Clicking registration button...");
        Thread.sleep(500); // waiting for popup

        // Checking if swal-2 popup is shown
        WebElement body = webDriver.findElement(By.tagName("body"));
        String bodyClasses = body.getAttribute("class");
        assertTrue(bodyClasses.contains("swal2-shown"), "Popup should be displayed after registration attempt");

        // Extract popup text
        WebElement popupTextElement = webDriver.findElement(
                By.cssSelector(".swal2-popup .swal2-html-container")
        );
        String popupText = popupTextElement.getText();
        System.out.println("Popup text: " + popupText);

        // Proving that it contains an error message
        boolean isError = popupText.toLowerCase().contains("došlo je do greške");
        assertTrue(isError, "Error popup should indicate invalid email");

        System.out.println("invalid email test passed - error popup detected with text: " + popupText);
    }
    /**
     * TC1.4: Registering with empty required fields
     * Expected: html5 validation errors
     */
    @Test
    public void testRegisterWithEmptyFields() throws InterruptedException {

        // Navigating to registration page
        webDriver.get(loginRegisterUrl);
        Thread.sleep(500);

        // Finding form
        WebElement registerForm = webDriver.findElement(By.id("registerForm"));
        assertTrue(registerForm.isDisplayed(), "Registration form should be visible");

        // Clicking submit with empty fields
        WebElement submitButton = registerForm.findElement(
                By.xpath(".//button[@type='submit']")
        );
        submitButton.click();
        Thread.sleep(500);

        // Checking validation for each required field
        String[] fieldNames = {"first_name", "last_name", "email", "password"};

        for (String fieldName : fieldNames) {
            WebElement field = registerForm.findElement(By.name(fieldName));

            // Checking if field is valid (HTML5)
            boolean isValid = (Boolean) ((JavascriptExecutor) webDriver)
                    .executeScript("return arguments[0].checkValidity();", field);

            // The field must be invalid because it's empty
            assertFalse(isValid, "Field '" + fieldName + "' should be invalid when empty");

            // Get browser validation message ("Please fill out this field.")
            String message = (String) ((JavascriptExecutor) webDriver)
                    .executeScript("return arguments[0].validationMessage;", field);

            System.out.println("Validation for '" + fieldName + "': " + message);
        }

        System.out.println("empty field validation est passed - empty fields correctly trigger HTML5 required validation");
    }
}