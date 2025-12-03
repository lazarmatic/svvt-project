import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductSortingFilteringTests {

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
        wait = new WebDriverWait(webDriver, Duration.ofSeconds(15));
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
     * TC4.1: Filtering by category
     */
    @Test
    public void testFilterByCategory() throws InterruptedException {

        webDriver.get(baseUrl + "/proizvodi");
        Thread.sleep(2000);

        WebElement sidebar = webDriver.findElement(By.cssSelector("div.pp__sidebar"));

        WebElement categoryLink = sidebar.findElement(
                By.xpath(".//a[contains(text(), 'Veliki kućanski aparati')]")
        );

        categoryLink.click();
        Thread.sleep(2500);

        List<WebElement> products = webDriver.findElements(
                By.cssSelector("div.products div.product__item")
        );

        assertTrue(products.size() > 0, "No products shown in category.");

        System.out.println("Test passed, products shown based on category.");
    }


    /**
     * TC4.2: Filtering by price
     */
    @Test
    public void testFilterByPriceRange() throws InterruptedException {
        webDriver.get(baseUrl + "/proizvodi");
        Thread.sleep(2000);

        WebElement priceBox = webDriver.findElement(By.cssSelector("div.sidebar__price"));
        WebElement minPriceInput = priceBox.findElement(By.cssSelector("input#price_from"));
        WebElement maxPriceInput = priceBox.findElement(By.cssSelector("input#price_to"));

        final String minValue = "100.00";
        final String maxValue = "1000.00";

        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript(
                "function setVal(el, val) {" +
                        "  el.value = val;" +
                        "  el.dispatchEvent(new Event('input', {bubbles:true}));" +
                        "  el.dispatchEvent(new Event('change', {bubbles:true}));" +
                        "}" +
                        "setVal(arguments[0], arguments[2]);" +
                        "setVal(arguments[1], arguments[3]);" +
                        "if (typeof FilterInput === 'function') { FilterInput(); }",
                minPriceInput, maxPriceInput, minValue, maxValue
        );

        Thread.sleep(1500);

        List<WebElement> products = webDriver.findElements(By.cssSelector("div.products div.product__item"));
        assertTrue(products.size() > 0, "No products after price filter.");

        List<WebElement> priceElements = webDriver.findElements(By.cssSelector(".pi__price .new"));
        for (WebElement p : priceElements) {
            double pr = extractPrice(p.getText());
            assertTrue(pr >= 100 && pr <= 1000, "Price that is outside the range: " + pr);
        }

        System.out.println("Test passed, price filter works correctly.");
    }


    /**
     * TC4.3: Filtering by brand TCL and FRAM
     */
    @Test
    public void testFilterByBrand() throws InterruptedException {
        webDriver.get(baseUrl + "/proizvodi");
        Thread.sleep(2000);

        List<WebElement> brandInputs = webDriver.findElements(By.cssSelector(
                "div.fbli__list input.filter-check-manufacturers[data-prettyvalue='TCL'], " +
                        "div.fbli__list input.filter-check-manufacturers[data-prettyvalue='FRAM']"
        ));

        for (WebElement input : brandInputs) {
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", input);
        }

        Thread.sleep(2500);

        List<WebElement> products = webDriver.findElements(By.cssSelector("div.products div.product__item"));
        assertTrue(products.size() > 0, "No products showing after brand filter.");

        String pageText = webDriver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("TCL") || pageText.contains("FRAM"),
                "Brand names are not found in results.");

        System.out.println("Test passed, brand filtering works.");
    }

    /**
     * TC4.5: Sorting by price">
     */

    /**
     * Extract price helper method.
     */
    private double extractPrice(String priceText) {
        String cleaned = priceText.replaceAll("[^0-9.,]", "").replace(",", ".");
        try {
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Test
    public void testSortByPrice() throws InterruptedException {

        webDriver.get(baseUrl + "/proizvodi");
        Thread.sleep(2000);

        WebElement sortElement = webDriver.findElement(
                By.cssSelector("div.products__page select#requestFilterSort")
        );

        Select sortSelect = new Select(sortElement);

        sortSelect.selectByValue("sortMin");
        Thread.sleep(3000);

        List<WebElement> priceElements = webDriver.findElements(
                By.cssSelector(".pi__price .new")
        );

        if (priceElements.size() < 2) {
            System.out.println("Not enough products to validate sorting.");
            return;
        }

        double prev = 0;
        for (WebElement p : priceElements) {
            double current = extractPrice(p.getText());
            if (current > 0) {
                assertTrue(current >= prev, "Wrong price order.");
                prev = current;
            }
        }

        System.out.println("Test passed, sorting by price works.");
    }

}
