import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Scenario 8: Navigation and Menu Functionality
 * Objective: Verifying that all navigation elements, menus, and links work correctly across the bijelatehnika.com website.
 */
public class NavigationTests {

    private static WebDriver webDriver;
    private static String baseUrl;
    private WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        // Setting the path to my chromedriver
        System.setProperty("webdriver.chrome.driver",
                "/Users/lazarmatic/Downloads/chromedriver-mac-arm64/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");

        webDriver = new ChromeDriver(options);
        baseUrl = "https://www.bijelatehnika.com";
    }

    @BeforeEach
    public void setUpEach() {
        wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    /**
     * TC8.1: Verifying main navigation menu loads correctly
     * Expected: All menu items displayed
     */
    @Test
    public void testMainNavigationMenuLoads() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(3000);

        // Finding the header/navigation area
        WebElement header = webDriver.findElement(By.tagName("header"));
        assertTrue(header.isDisplayed(), "Header should be displayed");

        // Finding navigation links in the header
        List<WebElement> navLinks = header.findElements(By.tagName("a"));

        assertTrue(navLinks.size() > 0, "Navigation should contain links");
        System.out.println("Found " + navLinks.size() + " links in header/navigation");

        // Printing navigation items for verification
        // Including functional navigation ( cart counter - 0)
        int count = 0;
        for (WebElement link : navLinks) {
            String text = link.getText();
            if (!text.isEmpty()) {
                System.out.println("Nav link " + (++count) + ": " + text);
            }
        }

        assertTrue(count > 0, "Should have visible navigation links with text");
    }

    /**
     * TC8.2: Click on each main menu item
     * Expected: Correct pages load
     * Tests all main navigation links found in TC8.1 (13 links with visible text)
     * Link TCL.BA - not working, so test fails with 12 successfull and 1 failed link.
            */
    @Test
    public void testMainMenuItemClicks() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(3000);

        // Finding the header
        WebElement header = webDriver.findElement(By.tagName("header"));

        // Finding all navigation links in header
        List<WebElement> menuLinks = header.findElements(By.tagName("a"));

        System.out.println("Found " + menuLinks.size() + " total links in header");

        // Storing link data for all links with visible text (same logic as TC8.1)
        // This will capture all 13 main navigation links
        List<String> linkHrefs = new ArrayList<>();
        List<String> linkTexts = new ArrayList<>();

        for (WebElement link : menuLinks) {
            String text = link.getText();
            String href = link.getAttribute("href");

            // Only including links with visible text, skipping mailto: and tel: links because of going out of app.
            if (!text.isEmpty() && href != null && !href.isEmpty() &&
                    !href.startsWith("mailto:")) {
                linkHrefs.add(href);
                linkTexts.add(text);
            }
        }

        System.out.println("Found " + linkHrefs.size() +
                " main navigation links with visible text (without mailto)\n");

        // Test ALL main navigation links
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < linkHrefs.size(); i++) {
            System.out.println("--------------------");
            System.out.println("Testing menu item " + (i + 1) + "/" + linkHrefs.size() +
                    ": " + linkTexts.get(i));
            System.out.println("Target URL: " + linkHrefs.get(i));

            try {
                webDriver.get(linkHrefs.get(i));
                Thread.sleep(2000);

                String currentUrl = webDriver.getCurrentUrl();
                assertNotNull(currentUrl, "Page should load after clicking menu item");

                System.out.println("-- Successfully navigated to: " + currentUrl);
                successCount++;

            } catch (Exception e) {
                System.out.println("-- Failed to navigate: " + e.getMessage());
                failCount++;
            }

            System.out.println();
        }

        // Summary
        System.out.println("---------------");
        System.out.println("Total links tested: " + linkHrefs.size());
        System.out.println("Successful: " + successCount);
        System.out.println("Failed: " + failCount);
    }
    /**
     * TC8.3: Testing dropdown functionality
     * Expected: Dropdowns expand and navigate correctly
     * This test verifies that:
     * 1. Main category links have dropdown menus
     * 2. Hovering/clicking reveals subcategories
     * 3. Subcategory links are clickable and navigate correctly
     */
    @Test
    public void testDropdownFunctionality() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(3000);

        System.out.println("--- Testing dropdown functionality ---\n");

        // Finding main category links that should have dropdowns
        // These are the top-level navigation items (Veliki kućanski aparati, Mali kućanski aparati...)
        List<WebElement> mainCategoryLinks = webDriver.findElements(
                By.xpath("//ul[@class='main__cat']/li/a")
        );

        if (mainCategoryLinks.size() == 0) {
            System.out.println("No main category structure found, trying alternative selector...");
            mainCategoryLinks = webDriver.findElements(
                    By.xpath("//header//a[contains(@href, 'proizvodi')]")
            );
        }

        System.out.println("Found " + mainCategoryLinks.size() + " main category links");

        assertTrue(mainCategoryLinks.size() > 0, "Should find main category links");

        // Test dropdown for first main category
        if (mainCategoryLinks.size() > 0) {
            WebElement firstMainCategory = mainCategoryLinks.get(0);
            String mainCategoryText = firstMainCategory.getText();
            String mainCategoryHref = firstMainCategory.getAttribute("href");

            System.out.println("Testing dropdown for main category: " + mainCategoryText);
            System.out.println("Main category URL: " + mainCategoryHref);

            // Look for subcategories under this main category
            // Try to find nested <ul> or sibling dropdown elements
            List<WebElement> subcategoryLinks = webDriver.findElements(
                    By.xpath("//ul[@class='main__cat']/li[1]//ul//a")  // First main cat's subcategories
            );

            System.out.println("Found " + subcategoryLinks.size() +
                    " subcategory links under '" + mainCategoryText + "'");

            if (subcategoryLinks.size() > 0) {
                // Store subcategory data (avoid stale elements)
                List<String> subHrefs = new ArrayList<>();
                List<String> subTexts = new ArrayList<>();

                for (WebElement subLink : subcategoryLinks) {
                    String href = subLink.getAttribute("href");
                    String text = subLink.getText();

                    // If getText() is empty, try getting text from innerHTML or textContent
                    if (text.isEmpty()) {
                        try {
                            text = (String) ((JavascriptExecutor) webDriver)
                                    .executeScript("return arguments[0].textContent;", subLink);
                            text = text.trim();
                        } catch (Exception e) {
                            text = "";
                        }
                    }

                    // Accept links even with empty text, but only if they have valid href
                    if (href != null && !href.isEmpty() &&
                            !href.equals(mainCategoryHref) &&  // Skip if it's the main category link
                            !href.equals("#")) {
                        subHrefs.add(href);
                        subTexts.add(text.isEmpty() ? "Subcategory " + (subHrefs.size()) : text);
                        if (subHrefs.size() >= 3) break;  // Test first 3 subcategories
                    }
                }

                System.out.println("\nTesting " + subHrefs.size() + " subcategories:\n");

                // Test each subcategory
                for (int i = 0; i < subHrefs.size(); i++) {
                    System.out.println("Subcategory " + (i + 1) + ": " + subTexts.get(i));
                    System.out.println("  URL: " + subHrefs.get(i));

                    webDriver.get(subHrefs.get(i));
                    Thread.sleep(2000);

                    String currentUrl = webDriver.getCurrentUrl();
                    System.out.println("  Sucessfully navigated to: " + currentUrl);

                    System.out.println();
                }

            } else {
                System.out.println(" No subcategories found under '" + mainCategoryText + "'");
            }
        }
    }

    /**
     * TC8.4: Testing footer links
     * Expected: First 4 links from expected keywoard words footer links functional
     */
    @Test
    public void testFooterLinks() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(3000);

        // Find footer element
        WebElement footer = webDriver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed(), "Footer should be displayed");

        // Store footer link data (avoid stale elements)
        List<WebElement> footerLinks = footer.findElements(By.tagName("a"));
        List<String> footerHrefs = new ArrayList<>();
        List<String> footerTexts = new ArrayList<>();

        System.out.println("Found " + footerLinks.size() + " links in footer");

        String[] expectedKeywords = {"početna", "novosti", "o nama", "uslovi korištenja"};

        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            String text = link.getText().toLowerCase();

            if (href != null && !href.isEmpty()) {
                for (String keyword : expectedKeywords) {
                    if (href.toLowerCase().contains(keyword) || text.contains(keyword)) {
                        footerHrefs.add(href);
                        footerTexts.add(link.getText());
                        break;
                    }
                }
            }
        }

        // Test stored footer links
        for (int i = 0; i < Math.min(4, footerHrefs.size()); i++) {
            System.out.println("Testing footer link: " + footerTexts.get(i) +
                    " - should navigate to: " + footerHrefs.get(i));

            webDriver.get(footerHrefs.get(i));
            Thread.sleep(2000);

            String currentUrl = webDriver.getCurrentUrl();
            assertNotNull(currentUrl, "Footer link should navigate to a page");
            System.out.println("Successfully navigated to: " + currentUrl);

            // Return to homepage
            webDriver.get(baseUrl);
            Thread.sleep(1500);
        }

        assertTrue(footerHrefs.size() > 0, "Should find relevant footer links");
    }

    /**
     * TC8.5: Verify logo click returns to homepage
     * Expected: Homepage loads from any page
     */
    @Test
    public void testLogoClickReturnsToHomepage() {
        // Open a product page first
        webDriver.get("https://bijelatehnika.com/proizvodi/veliki-kucanski-aparati");
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

        String currentUrl = webDriver.getCurrentUrl();
        System.out.println("Navigated from homepage to: " + currentUrl);

        // Find and wait for logo to be clickable
        WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//header//a[img[@alt='BTH Logotip']]")
        ));

        String logoHref = logo.getAttribute("href");
        System.out.println("Found logo with href: " + logoHref);
        assertNotNull(logo, "Logo should be found in header");

        // Click the logo directly
        logo.click();

        // Wait for navigation to complete
        wait.until(ExpectedConditions.urlToBe("https://bijelatehnika.com/"));

        // Verify we're on the homepage
        String afterClickUrl = webDriver.getCurrentUrl();
        System.out.println("After clicking logo, navigated to: " + afterClickUrl);

        assertEquals("https://bijelatehnika.com/", afterClickUrl,
                "Clicking logo should return to homepage");
    }

    /**
     * TC8.6: Verify Back and Forward button functionality
     * Expected: Browser back and forward buttons working correctly
     */
    @Test
    public void testBrowserButtonsFunctionality() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(2000);

        String homeUrl = webDriver.getCurrentUrl();
        System.out.println("Starting at: " + homeUrl);

        // Navigate to another page
        List<WebElement> links = webDriver.findElements(
                By.xpath("//a[@href and @href!='#' and not(contains(@href, 'facebook')) " +
                        "and not(contains(@href, 'instagram'))]")
        );

        if (links.size() > 1) {
            String targetHref = links.get(1).getAttribute("href");
            webDriver.get(targetHref);
            Thread.sleep(2000);

            String secondPageUrl = webDriver.getCurrentUrl();
            System.out.println("Navigated to: " + secondPageUrl);

            assertNotEquals(homeUrl, secondPageUrl,
                    "Should navigate to a different page");

            // Use browser back button
            webDriver.navigate().back();
            Thread.sleep(2000);

            String afterBackUrl = webDriver.getCurrentUrl();
            System.out.println("After back button: " + afterBackUrl);

            assertEquals(homeUrl, afterBackUrl,
                    "Browser back button should return to homepage");

            // Test forward button
            webDriver.navigate().forward();
            Thread.sleep(2000);

            String afterForwardUrl = webDriver.getCurrentUrl();
            System.out.println("After forward button: " + afterForwardUrl);

            assertEquals(secondPageUrl, afterForwardUrl,
                    "Browser forward button should return to second page");
        } else {
            fail("Not enough links found to test navigation");
        }
    }

    /**
     * TC8.7: Test pagination on product listing pages
     * Expected: Page numbers work, correct products shown
     */
    @Test
    public void testPaginationFunctionality() throws InterruptedException {
        webDriver.get(baseUrl);
        Thread.sleep(3000);

        // Navigate to a product listing page
        List<WebElement> categoryLinks = webDriver.findElements(
                By.xpath("//a[contains(@href, 'proizvodi')]")
        );

        if (categoryLinks.size() > 0) {
            String categoryHref = categoryLinks.get(0).getAttribute("href");
            webDriver.get(categoryHref);
            Thread.sleep(3000);

            System.out.println("Looking for pagination on: " + webDriver.getCurrentUrl());

            // Look for pagination elements - various possible selectors
            List<WebElement> paginationElements = webDriver.findElements(
                    By.xpath("//div[contains(@class, 'pagination')]//a | " +
                            "//a[contains(text(), 'Next')]")
            );

            if (paginationElements.size() > 0) {
                System.out.println("Found pagination with " +
                        paginationElements.size() + " page elements");

                String firstPageUrl = webDriver.getCurrentUrl();

                // Try to find and click next page
                WebElement nextPageLink = null;
                for (WebElement page : paginationElements) {
                    String pageText = page.getText();
                    String pageHref = page.getAttribute("href");

                    if (pageText.equals("2")){
                        nextPageLink = page;
                        break;
                    }
                }

                if (nextPageLink != null) {
                    String nextHref = nextPageLink.getAttribute("href");
                    System.out.println("Clicking next page: " + nextHref);

                    webDriver.get(nextHref);
                    Thread.sleep(3000);

                    String secondPageUrl = webDriver.getCurrentUrl();
                    System.out.println("Navigated to page 2: " + secondPageUrl);

                    assertNotEquals(firstPageUrl, secondPageUrl,
                            "URL should change when navigating to page 2");
                } else {
                    System.out.println("Something went wrong");
                }
            }
        }
    }
}