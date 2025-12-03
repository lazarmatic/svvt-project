import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CheckoutTests {

    private static WebDriver webDriver;
    private static WebDriverWait wait;
    private String baseUrl = "https://bijelatehnika.com";

    @BeforeAll
    public static void setUp() {
        System.setProperty("webdriver.chrome.driver", "/Users/lazarmatic/Downloads/chromedriver-mac-arm64/chromedriver");

        webDriver = new ChromeDriver();
        webDriver.manage().window().maximize();
        wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
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

    private void addProductToCart() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.id("search-input")));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);
        searchBox.sendKeys("mašina");
        Thread.sleep(1500);

        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".searchbox.activee a.item")));
        clickElement(firstProduct);
        Thread.sleep(3000);

        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Dodaj')]")
        ));
        clickElement(addToCartBtn);
        waitForOverlayToDisappear();
        Thread.sleep(2000);

        webDriver.get(baseUrl + "/korpa");
        Thread.sleep(3000);

        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".cr__button a[href='/kasa'], .cr__button a[href='" + baseUrl + "/kasa']")));
        clickElement(continueBtn);
        Thread.sleep(3000);
    }

    /**
     * Helper method: Waiting for SweetAlert2 popup to disappear
     */
    private void waitForOverlayToDisappear() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.cssSelector(".swal2-container, .swal2-backdrop-show, .swal2-shown")
            ));
            Thread.sleep(800);
        } catch (Exception e) {
            System.out.println("Something went wrong while waiting for overlay to disappear");
        }
    }

    /**
     * Helper method: Clicking element with fallback to JavaScript
     */
    private void clickElement(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (ElementClickInterceptedException | TimeoutException e) {
            System.out.println("Standard click failed, using JavaScript click");
            JavascriptExecutor js = (JavascriptExecutor) webDriver;
            js.executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", element);
        }
    }

    private void fillCheckoutForm(String email, String name, String phone,
                                  String paymentName, String paymentPhone, String paymentAddress,
                                  String paymentCity, String paymentZip,
                                  String deliveryName, String deliveryPhone,
                                  String deliveryAddress, String deliveryCity,
                                  String deliveryZip) throws InterruptedException {

        webDriver.findElement(By.id("email")).sendKeys(email);
        webDriver.findElement(By.id("name")).sendKeys(name);
        webDriver.findElement(By.id("phone_number")).sendKeys(phone);

        webDriver.findElement(By.id("payment_name")).sendKeys(paymentName);
        webDriver.findElement(By.id("payment_phone_number")).sendKeys(paymentPhone);
        webDriver.findElement(By.id("payment_address")).sendKeys(paymentAddress);
        webDriver.findElement(By.id("payment_city")).sendKeys(paymentCity);
        webDriver.findElement(By.id("payment_postal_zip")).sendKeys(paymentZip);

        webDriver.findElement(By.id("delivery_name")).sendKeys(deliveryName);
        webDriver.findElement(By.id("delivery_phone_number")).sendKeys(deliveryPhone);
        webDriver.findElement(By.id("delivery_address")).sendKeys(deliveryAddress);
        webDriver.findElement(By.id("delivery_city")).sendKeys(deliveryCity);
        webDriver.findElement(By.id("delivery_postal_zip")).sendKeys(deliveryZip);
    }

    @Test
    public void testcompleteCheckout() throws InterruptedException {
        addProductToCart();

        fillCheckoutForm(
                "test@email.com",
                "Test User",
                "061123456",
                "Test User",
                "061123456",
                "Test Address 1",
                "Sarajevo",
                "71000",
                "Test User",
                "061123456",
                "Delivery Address 1",
                "Sarajevo",
                "71000"
        );

        String beforeSubmitUrl = webDriver.getCurrentUrl();

        webDriver.findElement(By.cssSelector("button[type='submit']")).click();
        Thread.sleep(3000);

        String afterSubmitUrl = webDriver.getCurrentUrl();

        assertNotEquals(afterSubmitUrl, beforeSubmitUrl,
                "Checkout didnt go through");

        assertTrue(afterSubmitUrl.contains("/placanje"),
                "Expected checkout to navigate to /placanje, but got: " + afterSubmitUrl);
        System.out.println("Test passed, checkout went through succesfully");
    }


    @Test
    public void testmissingShippingAddress() throws InterruptedException {
        addProductToCart();

        fillCheckoutForm(
                "test@email.com",
                "Test User",
                "061123456",
                "Test User",
                "061123456",
                "Test Address 1",
                "Sarajevo",
                "71000",
                "", "", "", "", ""
        );

        String beforeSubmitUrl = webDriver.getCurrentUrl();

        webDriver.findElement(By.cssSelector("button[type='submit']")).click();
        Thread.sleep(2000);

        String afterSubmitUrl = webDriver.getCurrentUrl();

        assertEquals(afterSubmitUrl, beforeSubmitUrl,
                "Checkout shouldnt go through because delivery info is missing. URL changed to: " + afterSubmitUrl);
        System.out.println("Test passed, checkout didnt go through");
    }


    @Test
    public void testinvalidPhoneNumber() throws InterruptedException {
        addProductToCart();

        fillCheckoutForm(
                "test@email.com",
                "Test User",
                "inasda@$@##543",
                "Test User",
                "aslskasdsd",
                "Test Address 1",
                "Sarajevo",
                "71000",
                "Test User",
                "06asasfa$#$#$",
                "Delivery Address 1",
                "Sarajevo",
                "71000"
        );

        webDriver.findElement(By.cssSelector("button[type='submit']")).click();
        Thread.sleep(2000);

        String currentUrl = webDriver.getCurrentUrl();

        assertTrue(currentUrl.contains("/kasa"), "Submission should be blocked for wrong phones. Current URL: " + currentUrl);
        System.out.println("Current URL: " + currentUrl + " Webpage fails with validating phone number.");
    }


    @Test
    public void testcancelCheckout() throws InterruptedException {
        addProductToCart();

        fillCheckoutForm("test@email.com", "Test User", "061123456",
                "Test User", "061123456", "Test Address 1", "Sarajevo", "71000",
                "Test User", "061123456", "Delivery Address 1", "Sarajevo", "71000");

        webDriver.findElement(By.cssSelector("button[type='submit']")).click();
        Thread.sleep(3000);


        webDriver.navigate().back();
        Thread.sleep(2000);

        List<WebElement> cartItems = webDriver.findElements(By.className("cart__item"));
        assertTrue(cartItems.size() > 0, "Test failed - Items should still be in the cart after cancelling checkout");

    }

    @Test
    public void testorderConfirmationPage() throws InterruptedException {
        addProductToCart();

        fillCheckoutForm("test@email.com", "Test User", "061123456",
                "Test User", "061123456", "Test Address 1", "Sarajevo", "71000",
                "Test User", "061123456", "Delivery Address 1", "Sarajevo", "71000");

        webDriver.findElement(By.cssSelector("button[type='submit']")).click();
        Thread.sleep(3000);

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("body")));
        String pageText = confirmation.getText();
        assertTrue(pageText.matches(".*\\bBT-[0-9]+\\b.*") || pageText.contains("narudžbu"), "Order number should be displayed");

        System.out.println("Test passed, order confirmation page: " + pageText);
    }
}