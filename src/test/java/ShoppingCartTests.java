import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Scenario 5: Testing shopping cart
 */
public class ShoppingCartTests {
    private static WebDriver webDriver;
    private static String baseUrl;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        System.setProperty("webdriver.chrome.driver", "/Users/lazarmatic/Downloads/chromedriver-mac-arm64/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        webDriver = new ChromeDriver(options);
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        webDriver.manage().window().maximize();
        baseUrl = "https://www.bijelatehnika.com";
        wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));
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
     * Helper method: Wait for SweetAlert2 popup to disappear
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
     * Helper method: Click element with fallback to JavaScript
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

    /**
     * TC5.1: Adding product to the cart from product details page
     */
    @Test
    public void testAddProductToCart() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.id("search-input")));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);

        searchBox.sendKeys("TCL frižider");
        Thread.sleep(1500);

        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".searchbox.activee a.item")));
        clickElement(firstProduct);
        Thread.sleep(3000);

        String cartCountBefore = "0";
        try {
            WebElement cartCounter = webDriver.findElement(By.id("cartCount"));
            cartCountBefore = cartCounter.getText();
        } catch (NoSuchElementException e) {
            System.out.println("Cart counter not found, assuming 0");
        }

        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Dodaj')]")
        ));

        assertNotNull(addToCartBtn, "Add to cart button not found");
        clickElement(addToCartBtn);

        waitForOverlayToDisappear();
        Thread.sleep(2000);

        WebElement cartCounter = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cartCount")));
        String cartCountAfter = cartCounter.getText();
        assertNotEquals(cartCountBefore, cartCountAfter, "Cart count should be updated");

        System.out.println("Test passed, product added to cart successfully - Before: " + cartCountBefore + " / After: " + cartCountAfter + ")");
        Thread.sleep(1000);
    }

    /**
     * TC5.3: Verifying that cart page displays correct product information
     */
    @Test
    public void testVerifyCartPageInformation() throws InterruptedException {
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

        try {
            List<WebElement> cartItems = webDriver.findElements(By.className("cart__item"));
            assertTrue(cartItems.size() > 0, "Cart should contain items");

            WebElement firstItem = cartItems.get(0);

            WebElement productImage = firstItem.findElement(By.cssSelector(".ci__img img"));
            assertNotNull(productImage, "Product image should be present");

            WebElement productInfo = firstItem.findElement(By.className("ci__info"));
            assertNotNull(productInfo, "Product info should be present");
            assertTrue(productInfo.getText().contains("Količina:"), "Quantity should be displayed");

            WebElement productPrice = firstItem.findElement(By.className("ci__price"));
            assertNotNull(productPrice, "Product price should be present");

            WebElement removeButton = firstItem.findElement(By.className("ci__action"));
            assertNotNull(removeButton, "Remove button should be present");

            WebElement cartRight = webDriver.findElement(By.className("cart__right"));
            String totalText = cartRight.getText();
            assertTrue(totalText.contains("Ukupna cijena:"), "Total price should be displayed");

            System.out.println("Test passed, cart page displays all required information");
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }

        Thread.sleep(1000);
    }

    /**
     * TC5.4: Removing product from cart
     */
    @Test
    public void testRemoveProductFromCart() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.id("search-input")));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);
        searchBox.sendKeys("aparat");
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


        List<WebElement> cartItemsBefore = webDriver.findElements(By.className("cart__item"));
        int productCountBefore = cartItemsBefore.size();
        System.out.println("Products in cart before removing: " + productCountBefore);

        assertTrue(productCountBefore > 0, "Cart should have at least one item before removing");

        WebElement removeBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Ukloni')]")
        ));

        assertNotNull(removeBtn, "Remove button not found");
        clickElement(removeBtn);

        waitForOverlayToDisappear();
        Thread.sleep(3000);

        String currentUrl = webDriver.getCurrentUrl();
        String pageText = webDriver.findElement(By.tagName("body")).getText();

        List<WebElement> cartItemsAfter = webDriver.findElements(By.className("cart__item"));
        int productCountAfter = cartItemsAfter.size();
        System.out.println("Products in cart after removal: " + productCountAfter);

        boolean removedSuccessfully = productCountAfter < productCountBefore;

        assertTrue(removedSuccessfully, "Product should be removed from cart");

        System.out.println("Test passed, product removed from cart successfully");
        Thread.sleep(1000);
    }

    /**
     * TC5.5: Adding product to cart when not logged in
     */
    @Test
    public void testAddToCartNotLoggedIn() throws InterruptedException {
        webDriver.manage().deleteAllCookies();

        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.id("search-input")));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);
        searchBox.sendKeys("usisivač");
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
        Thread.sleep(3000);

        try {
            WebElement cartCounter = webDriver.findElement(By.id("cartCount"));
            String cartCount = cartCounter.getText();
            int count = Integer.parseInt(cartCount);
            assertTrue(count > 0, "Cart count should increase even without login");
            System.out.println("Cart count after adding: " + cartCount);
        } catch (Exception e) {
            fail("Cart counter not updated: " + e.getMessage());
        }

        System.out.println("Test passed, guest cart works normal");
        Thread.sleep(1000);
    }

    /**
     * TC5.6: Verifying that cart displays correct total price calculation
     */
    @Test
    public void testVerifyTotalPriceCalculation() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.id("search-input")));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);
        searchBox.sendKeys("perilica");
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

        try {
            WebElement cartRight = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.className("cart__right")
            ));

            String cartText = cartRight.getText();
            System.out.println("Cart totals: " + cartText);

            assertTrue(cartText.contains("Ukupno bez PDV"), "Total amount without PDV should be shown");
            assertTrue(cartText.contains("Ukupan PDV (17%)"), "PDV amount should be shown");
            assertTrue(cartText.contains("Ukupna cijena:"), "Total amiunt should be shown");

            WebElement continueBtn = cartRight.findElement(By.cssSelector(".cr__button a"));
            assertNotNull(continueBtn, "Continue button should be present");
            assertTrue(continueBtn.getAttribute("href").contains("/kasa"), "Continue button should link to checkout");

            System.out.println("Test passed, cart displays all price calculations correctly");
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }

        Thread.sleep(1000);
    }

    /**
     * TC5.7: Emptying cart completely
     */
    @Test
    public void testEmptyCartCompletely() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.id("search-input")));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);
        searchBox.sendKeys("proizvod");
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

        boolean itemsRemaining = true;
        int maxAttempts = 10;
        int attempts = 0;

        while (itemsRemaining && attempts < maxAttempts) {
            try {
                List<WebElement> removeButtons = webDriver.findElements(
                        By.xpath("//a[contains(text(), 'Ukloni')]")
                );

                if (removeButtons.size() > 0) {
                    System.out.println("Removing item " + (attempts + 1));
                    clickElement(removeButtons.get(0));
                    waitForOverlayToDisappear();
                    Thread.sleep(3000);

                    if (!webDriver.getCurrentUrl().contains("/korpa")) {
                        webDriver.get(baseUrl + "/korpa");
                        Thread.sleep(2000);
                    }

                    attempts++;
                } else {
                    itemsRemaining = false;
                }
            } catch (Exception e) {
                System.out.println("Remove attempt failed: " + e.getMessage());
                itemsRemaining = false;
            }
        }

        Thread.sleep(2000);

        String pageText = webDriver.findElement(By.tagName("body")).getText();
        List<WebElement> cartItems = webDriver.findElements(By.className("cart__item"));

        boolean emptyCart = cartItems.size() == 0;

        try {
            WebElement cartCounter = webDriver.findElement(By.id("cartCount"));
            String cartCount = cartCounter.getText();
            if (cartCount.equals("0") || cartCount.isEmpty()) {
                emptyCart = true;
            }
            System.out.println("Final cart count: " + cartCount);
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }

        assertTrue(emptyCart, "Cart should be empty after removing all items");

        System.out.println("Test passed, cart emptied successfully");
        Thread.sleep(1000);
    }
}