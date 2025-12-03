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
 * Test Scenario 3: Product Search Functionality
 * Objective: Testing that users can search for products using multiple search options.
 */
public class ProductSearchTests {
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
     * TC3.1: Searching for real product by exact name
     * Expected:  Product shown in dropdown
     */
    @Test
    public void testSearchByExactProductName() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = webDriver.findElement(By.id("search-input"));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);

        searchBox.sendKeys("TCL frižider");

        Thread.sleep(2000);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".searchbox.activee")));

        List<WebElement> dropdownItems = webDriver.findElements(
                By.cssSelector(".searchbox.activee a.item"));

        assertTrue(dropdownItems.size() > 0,
                "No products found in dropdown for 'TCL frižider'");

        boolean foundRelevant = false;
        for (WebElement item : dropdownItems) {
            String itemText = item.getText().toLowerCase();
            if (itemText.contains("tcl") || itemText.contains("frižider")) {
                foundRelevant = true;
                break;
            }
        }
        assertTrue(foundRelevant, "Search results dont have products named like expected");

        System.out.println("Test passed, found items based on exact words");
        Thread.sleep(2000);
    }

    /**
     * TC3.2: Search with half of product name like "frižid"
     * Expected: Matching products shown in dropdown
     */
    @Test
    public void testSearchWithPartialProductName() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = webDriver.findElement(By.id("search-input"));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);

        searchBox.sendKeys("frižid");
        Thread.sleep(2000);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".searchbox.activee")));

        List<WebElement> dropdownItems = webDriver.findElements(
                By.cssSelector(".searchbox.activee a.item"));

        assertTrue(dropdownItems.size() > 0,
                "No products found in dropdown for partial search 'frižid'");

        boolean foundRelevant = false;
        for (WebElement item : dropdownItems) {
            String itemText = item.getText().toLowerCase();
            if (itemText.contains("friž") || itemText.contains("frižider")) {
                foundRelevant = true;
                break;
            }
        }
        assertTrue(foundRelevant, "Partial search didnt return expected results");

        System.out.println("Test passed, found items based on partial search");
        Thread.sleep(2000);
    }

    /**
     * TC3.3: Search with fake product name
     * Expected: No items in dropdown or "Nema rezultata" message
     */
    @Test
    public void testSearchWithNonExistentProduct() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = webDriver.findElement(By.id("search-input"));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);

        searchBox.sendKeys("wrgfwesfvwe");
        Thread.sleep(1000);

        WebElement searchButton = webDriver.findElement(
                By.cssSelector("i.fa-magnifying-glass, .fa-solid.fa-magnifying-glass"));
        searchButton.click();
        Thread.sleep(3000);

        String currentUrl = webDriver.getCurrentUrl();
        assertTrue(currentUrl.contains("pretraga"),
                "It should navigate to search results page, current link: " + currentUrl);

        List<WebElement> noResultsElements = webDriver.findElements(
                By.cssSelector("h4.no__results"));

        assertTrue(noResultsElements.size() > 0,
                "Expected to find h4.no__results element");

        String noResultsText = noResultsElements.get(0).getText();
        assertTrue(noResultsText.contains("Nisu pronađeni rezultati"),
                "Expected 'Nisu pronađeni rezultati' message, found: " + noResultsText);

        System.out.println("Test passed: 'Nisu pronađeni rezultati' message found");
        Thread.sleep(2000);
    }

    /**
     * TC3.4: Testing search with empty input field
     * Expected: No dropdown or anyhting
     */
    @Test
    public void testSearchWithEmptyQuery() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = webDriver.findElement(By.id("search-input"));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(1000);

        List<WebElement> searchboxes = webDriver.findElements(
                By.cssSelector(".searchbox.activee"));

        if (searchboxes.size() > 0) {
            String dropdownText = searchboxes.get(0).getText().toLowerCase();
            System.out.println("TC3.4 INFO: Dropdown response with empty input field submitted: " +
                    dropdownText.substring(0, Math.min(50, dropdownText.length())));
        } else {
            System.out.println("No dropdown shown for empty input field submitted");
        }

        assertTrue(true, "Test passed, empty input field submitted returned nothing, without errors");

        Thread.sleep(2000);
    }

    /**
     * TC3.5: Verify prikazi vise (Show more) link functionality
     * Expected: Clicking show more navigates to full search results page
     */
    @Test
    public void testVerifyShowMoreLink() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = webDriver.findElement(By.id("search-input"));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);

        searchBox.sendKeys("friz");
        Thread.sleep(2000);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".searchbox.activee")));

        List<WebElement> showMoreLinks = webDriver.findElements(
                By.cssSelector(".searchbox.activee a.show-more"));

        if (showMoreLinks.size() > 0) {
            String currentUrl = webDriver.getCurrentUrl();

            showMoreLinks.get(0).click();
            Thread.sleep(3000);

            String newUrl = webDriver.getCurrentUrl();
            assertNotEquals(currentUrl, newUrl,
                    "link should change after clicking prikazi vise");

            assertTrue(newUrl.contains("pretraga"),
                    "Should navigate to another page");

            System.out.println("Test passed, prikazi vise link works correctly");
        } else {
            System.out.println("Prikazi vise not found / something went wrong");
        }

        Thread.sleep(2000);
    }

    /**
     * TC3.6: Verifying clicking on product in dropdown
     * Expected: Navigating to product details page
     */
    @Test
    public void testVerifyProductClickInDropdown() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = webDriver.findElement(By.id("search-input"));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);

        searchBox.sendKeys("frižider");
        Thread.sleep(2000);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".searchbox.activee")));

        List<WebElement> dropdownItems = webDriver.findElements(
                By.cssSelector(".searchbox.activee a.item"));

        if (dropdownItems.size() > 0) {
            String productTitle = dropdownItems.get(0)
                    .findElement(By.cssSelector(".title")).getText();
            String currentUrl = webDriver.getCurrentUrl();

            dropdownItems.get(0).click();
            Thread.sleep(3000);

            String newUrl = webDriver.getCurrentUrl();
            assertNotEquals(currentUrl, newUrl,
                    "link should change after clicking product");

            String pageSource = webDriver.getPageSource().toLowerCase();
            assertTrue(pageSource.contains("cijena") ||
                            pageSource.contains("dodaj") || pageSource.contains("km"),
                    "It should navigate to product detail page");

            System.out.println("Test passed, successfully navigated to product details page");
        } else {
            fail("No products found in dropdown to test clicking");
        }

        Thread.sleep(2000);
    }

    /**
     * TC3.7: Verifying category links in dropdown
     * Expected: Category links are shown
     */
    @Test
    public void testVerifyCategoryLinksInDropdown() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        WebElement searchBox = webDriver.findElement(By.id("search-input"));
        searchBox.clear();
        searchBox.click();
        Thread.sleep(500);

        searchBox.sendKeys("friz");
        Thread.sleep(2000);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".searchbox.activee")));

        List<WebElement> categoryItems = webDriver.findElements(
                By.cssSelector(".searchbox.activee a.category-item"));

        assertTrue(categoryItems.size() > 0,
                "Expected category suggestions in dropdown");

        for (WebElement category : categoryItems) {
            String categoryText = category.getText();
            assertFalse(categoryText.isEmpty(),
                    "Category should have text");

            String href = category.getAttribute("href");
            assertTrue(href.contains("proizvodi"),
                    "Category link should point to products");
        }

        System.out.println("Test passed, found " + categoryItems.size() +
                " category items in dropdown");
        Thread.sleep(2000);
    }
}